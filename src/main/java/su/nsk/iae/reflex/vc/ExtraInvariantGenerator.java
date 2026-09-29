package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.inv.CandidateSource;
import su.nsk.iae.reflex.inv.ExtraInvariant;
import su.nsk.iae.reflex.inv.ExtraInvariants;
import su.nsk.iae.reflex.inv.Selection;
import su.nsk.iae.reflex.inv.StructuralInvariants;
import su.nsk.iae.reflex.inv.Tag;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.term.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Every invariant a condition may rely on besides the ones it is proving: the annotation
 * invariants, and those derived from the program's structure (extra-invariants.md).
 *
 * <p>All of them live in one {@link ExtraInvariants} container, tagged, and reach the
 * output by priority:
 * <ul>
 *   <li><b>High</b> - the annotation invariants and the states each process can be in -
 *       are part of {@code inv} itself: every condition assumes them at its first state
 *       and every cycle proves them at its last, exactly as it always has for annotations.
 *       No condition is added for them.</li>
 *   <li><b>Mid</b> and <b>low</b>, when the {@link Selection} asks for them, are
 *       conjoined into {@value #COMBINED}. {@link #process} gives each condition those
 *       tagged with a state its path passes through, as assumptions about its first state;
 *       {@link #obligations} adds, for every cycle and for the base case, a condition
 *       showing {@value #COMBINED} holds at its end. With the main conditions that is the
 *       induction for {@code inv} and {@value #COMBINED} together: nothing is taken on
 *       trust from the analysis.</li>
 * </ul>
 *
 * <p>Derived invariants are defined in {@value VcWriter#EXTRA_THEORY}; annotation
 * invariants are written where they always were. Every hook can be overridden, and
 * {@link #addSource} plugs in another way of finding invariants without touching the rest.
 */
public class ExtraInvariantGenerator {

    /** The predicate holding every mid and low invariant in use. */
    public static final String COMBINED = "extraInv";

    private final IrProgram program;
    private final Cfg cfg;
    private final AnnotationBinder annotations;
    private final Selection selection;
    private final List<CandidateSource<?>> sources = new ArrayList<>();
    private final ExtraInvariants invariants = new ExtraInvariants();
    private List<String> diagnostics = List.of();
    private int annotationCount;

    /** At the default selection: the high priority only. */
    public ExtraInvariantGenerator(IrProgram program, Cfg cfg, AnnotationBinder annotations) {
        this(program, cfg, annotations, Selection.high());
    }

    public ExtraInvariantGenerator(IrProgram program, Cfg cfg, AnnotationBinder annotations,
                                   Selection selection) {
        this.program = program;
        this.cfg = cfg;
        this.annotations = annotations;
        this.selection = selection;
    }

    protected IrProgram getProgram() {
        return program;
    }

    protected Cfg getCfg() {
        return cfg;
    }

    public Selection getSelection() {
        return selection;
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

    /** Plugs in another source of candidates; it runs whatever the selection says. */
    public void addSource(CandidateSource<?> source) {
        sources.add(source);
    }

    // ------------------------------------------------------------------ the container

    /** Every invariant known: the annotations registered, and what the analysis found. */
    public ExtraInvariants getInvariants() {
        return invariants;
    }

    /**
     * Records an invariant written on the program, a process or a state, already
     * translated. It becomes part of {@code inv}, where it always was.
     */
    public ExtraInvariant registerAnnotationInvariant(Annotation annotation, Term formula,
                                                      String process, String state) {
        List<Tag> tags = new ArrayList<>();
        if (process != null) {
            tags.add(Tag.process(process));
        }
        if (state != null) {
            tags.add(Tag.state(process, state));
        }
        String text = annotation == null ? "" : annotation.getText();
        return invariants.add(new ExtraInvariant("annotation_" + annotationCount++,
                ExtraInvariant.Kind.ANNOTATION, formula, text), tags);
    }

    /**
     * Records the invariant written on a loop, already translated against the loop's entry
     * and the state it is stated at. It is written to the loop invariant theory, as always.
     */
    public ExtraInvariant registerLoopInvariant(String name, Annotation annotation, Term formula) {
        return invariants.add(new ExtraInvariant(name, ExtraInvariant.Kind.LOOP_INVARIANT, formula,
                annotation == null ? "" : annotation.getText()), Tag.loop(name));
    }

    /** Derives the structural invariants the selection asks for; run once, before generation. */
    public void analyse() {
        IsabelleRenderer renderer = new IsabelleRenderer();
        StructuralInvariants analysis = new StructuralInvariants(program, cfg, renderer::renderExpression);
        sources.forEach(analysis::addSource);
        analysis.generate(selection, invariants);
        diagnostics = analysis.getDiagnostics();
    }

    /** What the analysis found worth reporting, such as static-analysis claims it could not confirm. */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    /** Derived invariants that go into {@code inv}: the high priority. */
    public List<ExtraInvariant> invariantsInInv() {
        List<ExtraInvariant> high = new ArrayList<>();
        for (ExtraInvariant invariant : invariants) {
            if (invariant.kind().isDerived() && invariant.kind() != ExtraInvariant.Kind.CUSTOM
                    && invariant.priority() == ExtraInvariant.Priority.HIGH) {
                high.add(invariant);
            }
        }
        return high;
    }

    /** Derived invariants that go into {@value #COMBINED}: mid, low, and anything plugged in. */
    public List<ExtraInvariant> selectedInvariants() {
        List<ExtraInvariant> extra = new ArrayList<>();
        for (ExtraInvariant invariant : invariants) {
            if (invariant.kind().isDerived() && !invariantsInInv().contains(invariant)) {
                extra.add(invariant);
            }
        }
        return extra;
    }

    /** Whether there is anything for {@value VcWriter#EXTRA_THEORY} to hold. */
    public boolean hasTheory() {
        return !invariantsInInv().isEmpty() || !selectedInvariants().isEmpty();
    }

    /** Whether conditions get extra assumptions and obligations: whether {@value #COMBINED} says anything. */
    public boolean isEnabled() {
        return !selectedInvariants().isEmpty();
    }

    /** What the writer needs to write the extra-invariant theory, or null when there is none. */
    public VcWriter.ExtraTheory theory() {
        return hasTheory()
                ? new VcWriter.ExtraTheory(invariants, invariantsInInv(), selectedInvariants(), COMBINED)
                : null;
    }

    // ------------------------------------------------------------------ hooks

    /**
     * Definitions to write into the requirements theory, after the invariant. The extra
     * invariants have a theory of their own, so none by default.
     */
    public List<String> extraDefinitions() {
        return List.of();
    }

    /**
     * Gives a condition the mid and low invariants that concern it, as assumptions about
     * the state it starts from. Returns the condition to write; returning null drops it.
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
     * The conditions showing the mid and low invariants are kept: one for each cycle, and
     * one for the base case. Each assumes all of them where it starts - not only the
     * relevant ones - since that is the induction hypothesis.
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
     * The mid and low invariants concerning a condition: those not tied to a state, and
     * those tied to a state some process is in, or moved to, on the path. A condition only
     * ever meets a couple of states of each process, so this is what keeps its context
     * small (mainOverview.tex, "Auxiliary lemmas").
     */
    protected List<ExtraInvariant> relevantTo(VerificationCondition condition) {
        Set<Tag> onPath = new java.util.LinkedHashSet<>();
        for (VcStatement statement : condition.getStatements()) {
            if (statement instanceof VcStatement.ProcessInState inState) {
                onPath.add(Tag.state(inState.process(), inState.pstate()));
            } else if (statement instanceof VcStatement.SetProcessState moved) {
                onPath.add(Tag.state(moved.process(), moved.pstate()));
            }
        }
        List<ExtraInvariant> relevant = new ArrayList<>();
        for (ExtraInvariant invariant : selectedInvariants()) {
            List<Tag> states = invariants.tagsOf(invariant).stream()
                    .filter(tag -> tag.key().equals("state")).toList();
            if (states.isEmpty() || states.stream().anyMatch(onPath::contains)) {
                relevant.add(invariant);
            }
        }
        return relevant;
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
