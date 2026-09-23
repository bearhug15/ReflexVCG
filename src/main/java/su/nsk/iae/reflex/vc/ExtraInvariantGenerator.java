package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.inv.ExpressionRendering;
import su.nsk.iae.reflex.inv.ExtraInvariant;
import su.nsk.iae.reflex.inv.ExtraInvariants;
import su.nsk.iae.reflex.inv.StructuralInvariants;
import su.nsk.iae.reflex.inv.Tag;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.term.Term;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Extra invariants: derived from the structure of the program, stated alongside the
 * engineer's, and proved like them.
 *
 * <p>{@link #analyse()} runs {@link StructuralInvariants} over the graph and keeps the
 * invariants the {@link Level} asks for. The overview (mainOverview.tex, "Auxiliary
 * lemmas") splits them into an <em>advanced</em> group used by default - the states a
 * process can be in, and the values variables hold in them - and an <em>optional</em> group
 * that has to be asked for, the transition conditions.
 *
 * <p>They reach the output three ways:
 * <ul>
 *   <li>Each is a definition in {@value VcWriter#EXTRA_THEORY}, and their conjunction is
 *       {@value #COMBINED}.</li>
 *   <li>{@link #process} gives a condition the ones that concern it as assumptions about
 *       its first state - those about the process states the path passes through, found
 *       by their tags, since a condition only ever meets a couple of states of each
 *       process.</li>
 *   <li>{@link #obligations} adds, for every cycle and for the base case, a condition
 *       showing that {@value #COMBINED} holds at its end. That is what makes assuming them
 *       sound: together with the main conditions it is the inductive step for the global
 *       invariant and the extra ones combined. Nothing is taken on trust from the
 *       analysis.</li>
 * </ul>
 *
 * <p>The default level is {@link Level#NONE}, which leaves the output exactly as it was.
 * Every hook can still be overridden, which is how further analyses or post-processing of
 * conditions get added without re-plumbing the generator.
 */
public class ExtraInvariantGenerator {

    /** The predicate holding every extra invariant in use. */
    public static final String COMBINED = "extraInv";

    /** Which extra invariants the conditions are given. */
    public enum Level {
        /** None: nothing is generated, and the output is as without this stage. */
        NONE,
        /** The advanced group: process states, and defined and stabilized variables. */
        ADVANCED,
        /** Both groups: the transition conditions as well. */
        ALL
    }

    private final IrProgram program;
    private final Cfg cfg;
    private final AnnotationBinder annotations;
    private final Level level;

    private ExtraInvariants invariants = new ExtraInvariants();
    private List<ExtraInvariant> selected = List.of();

    public ExtraInvariantGenerator(IrProgram program, Cfg cfg, AnnotationBinder annotations) {
        this(program, cfg, annotations, Level.NONE);
    }

    public ExtraInvariantGenerator(IrProgram program, Cfg cfg, AnnotationBinder annotations,
                                   Level level) {
        this.program = program;
        this.cfg = cfg;
        this.annotations = annotations;
        this.level = level;
    }

    protected IrProgram getProgram() {
        return program;
    }

    protected Cfg getCfg() {
        return cfg;
    }

    public Level getLevel() {
        return level;
    }

    /** Annotations found in the source, whether or not they were bound to a construct. */
    protected List<Annotation> getAnnotations() {
        return annotations == null ? List.of() : annotations.getAllAnnotations();
    }

    /** Annotations of one kind, across the whole program. */
    protected List<Annotation> annotationsOfKind(Annotation.Kind kind) {
        List<Annotation> matching = new ArrayList<>();
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getKind() == kind) {
                matching.add(annotation);
            }
        }
        return matching;
    }

    /**
     * Every invariant the analysis found, tagged, whether or not the level uses it. Empty
     * until {@link #analyse()} has run.
     */
    public ExtraInvariants getInvariants() {
        return invariants;
    }

    /** The invariants the conditions are given, in the order they are written. */
    public List<ExtraInvariant> selectedInvariants() {
        return selected;
    }

    /** Whether anything is in use - whether there is a theory to write and import at all. */
    public boolean isEnabled() {
        return !selected.isEmpty();
    }

    /** Derives the extra invariants, run once before conditions are generated. */
    public void analyse() {
        if (level == Level.NONE) {
            return;
        }
        IsabelleRenderer renderer = new IsabelleRenderer();
        invariants = new StructuralInvariants(program, cfg, new ExpressionRendering() {
            @Override
            public String expression(IrExpr expression, String state) {
                return renderer.renderExpression(expression, state);
            }

            @Override
            public String duration(TimeRef duration, String state) {
                return renderer.renderDuration(duration, state);
            }
        }).generate();
        selected = level == Level.ALL
                ? invariants.all()
                : invariants.find(Tag.group(ExtraInvariant.Group.ADVANCED));
    }

    /**
     * Definitions to write into the requirements theory, after the invariant. The extra
     * invariants have a theory of their own, so none by default.
     */
    public List<String> extraDefinitions() {
        return List.of();
    }

    /**
     * Gives a condition the extra invariants that concern it, as assumptions about the
     * state it starts from. Returns the condition to write; returning null drops it.
     *
     * <p>Only a condition that assumes the global invariant gets them: the two are
     * established together, and a condition inside a loop body assumes neither, the
     * boundaries below it being ends of iterations rather than of cycles.
     */
    public VerificationCondition process(VerificationCondition condition) {
        int at = invariantPosition(condition);
        if (!isEnabled() || at < 0) {
            return condition;
        }
        VerificationCondition processed = condition.copy();
        String start = ((VcStatement.Invariant) condition.getStatements().get(at)).state();
        List<VcStatement> assumptions = new ArrayList<>();
        for (ExtraInvariant invariant : relevantTo(condition)) {
            assumptions.add(new VcStatement.Assumption(invariant.name(), applied(invariant.name(), start)));
        }
        processed.getStatements().addAll(at + 1, assumptions);
        return processed;
    }

    /**
     * The conditions showing the extra invariants are kept: one for each cycle, and one
     * for the base case. Each assumes all of them where it starts - not only the relevant
     * ones - since that is the induction hypothesis.
     */
    public List<VerificationCondition> obligations(VerificationCondition condition) {
        if (!isEnabled() || condition.getKind() != VerificationCondition.Kind.MAIN
                || condition.getConclusion() != null) {
            return List.of();
        }
        VerificationCondition obligation = condition.copy();
        obligation.setKind(VerificationCondition.Kind.EXTRA_INVARIANT);
        obligation.setConclusion(applied(COMBINED, condition.getFinalState()));
        int at = invariantPosition(condition);
        if (at >= 0) {
            String start = ((VcStatement.Invariant) condition.getStatements().get(at)).state();
            obligation.getStatements().add(at + 1,
                    new VcStatement.Assumption("extra_inv", applied(COMBINED, start)));
            obligation.setNote("the extra invariants are kept by this cycle");
        } else {
            obligation.setNote("the extra invariants hold when the program starts");
        }
        return List.of(obligation);
    }

    /**
     * The selected invariants concerning a condition: which states each process can be
     * in, and those about a state some process is in, or moved to, on the path.
     */
    protected List<ExtraInvariant> relevantTo(VerificationCondition condition) {
        Set<ExtraInvariant> relevant = new LinkedHashSet<>(
                invariants.find(Tag.kind(ExtraInvariant.Kind.PROCESS_STATES)));
        for (VcStatement statement : condition.getStatements()) {
            if (statement instanceof VcStatement.ProcessInState inState) {
                relevant.addAll(invariants.find(Tag.state(inState.process(), inState.pstate())));
            } else if (statement instanceof VcStatement.SetProcessState moved) {
                relevant.addAll(invariants.find(Tag.state(moved.process(), moved.pstate())));
            }
        }
        List<ExtraInvariant> inUse = new ArrayList<>();
        for (ExtraInvariant invariant : selected) {
            if (relevant.contains(invariant)) {
                inUse.add(invariant);
            }
        }
        return inUse;
    }

    private static int invariantPosition(VerificationCondition condition) {
        List<VcStatement> statements = condition.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) instanceof VcStatement.Invariant) {
                return i;
            }
        }
        return -1;
    }

    private static Term applied(String predicate, String state) {
        return new Term.App(predicate, List.of(new Term.Var(state)));
    }
}
