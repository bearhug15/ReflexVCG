package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.analysis.Event;
import su.nsk.iae.reflex.analysis.PathState;
import su.nsk.iae.reflex.analysis.StaticAnalysis;
import su.nsk.iae.reflex.analysis.Term;
import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrCopier;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.vc.IsabelleRenderer;
import su.nsk.iae.reflex.vc.VcStatement;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Enumerates the paths of a {@link Cfg}, turning each into verification conditions.
 *
 * <p>A plain depth-first walk, emitting on reaching the exit. The previous generator
 * extended jgrapht's AbstractGraphIterator while firing its own traversal events and
 * keeping the accumulated state in listener fields; here the recursion carries its own
 * state and nothing is shared.
 *
 * <p>State variables are numbered along the path: an effect consumes the current one and
 * produces the next, so a condition reads as a chain {@code st0, st1, ... st_final}.
 *
 * <p>A path may produce more than one condition. An annotation contributes an obligation
 * discharging it, and a loop contributes the conditions that its invariant holds on entry
 * and survives an iteration.
 */
public final class PathEnumerator {

    /** Guards against runaway enumeration if the graph ever contains a cycle. */
    private static final int MAX_PATHS = 500_000;

    private final Cfg cfg;
    private final StaticAnalysis analysis;
    private final AnnTranslator annotations;
    private int emitted;
    private int pruned;

    public PathEnumerator(Cfg cfg) {
        this(cfg, null, null);
    }

    public PathEnumerator(Cfg cfg, StaticAnalysis analysis) {
        this(cfg, analysis, null);
    }

    /**
     * @param analysis    discards paths shown to be impossible; null keeps every path
     * @param annotations translates the annotations met along a path; null ignores them
     */
    public PathEnumerator(Cfg cfg, StaticAnalysis analysis, AnnTranslator annotations) {
        this.cfg = cfg;
        this.analysis = analysis;
        this.annotations = annotations;
    }

    /** How many times a subtree was abandoned because the path became impossible. */
    public int getPruned() {
        return pruned;
    }

    public int getEmitted() {
        return emitted;
    }

    /** Collects every condition. Convenient for tests and small programs. */
    public List<VerificationCondition> enumerate() {
        List<VerificationCondition> conditions = new ArrayList<>();
        forEach(conditions::add);
        return conditions;
    }

    /** Streams conditions to {@code sink}, so large programs need not be held in memory. */
    public void forEach(Consumer<VerificationCondition> sink) {
        emitted = 0;
        pruned = 0;
        walk(cfg.getEntry(), PathState.INITIAL, new ArrayList<>(), sink);
    }

    private void walk(CfgNode node, PathState state, List<CfgNode> path,
                      Consumer<VerificationCondition> sink) {
        if (emitted >= MAX_PATHS) {
            throw new IllegalStateException(
                    "path enumeration exceeded " + MAX_PATHS + " conditions; the graph may contain a cycle");
        }

        // Checked before descending, so an impossible path costs nothing beyond the node
        // that made it impossible - the whole subtree below is skipped.
        PathState next = admit(node, state);
        if (next == null) {
            pruned++;
            return;
        }
        next = next.andThen(node.getAttributes());

        path.add(node);
        if (node.isTerminal()) {
            for (VerificationCondition condition : build(path)) {
                sink.accept(condition);
                emitted++;
            }
        } else {
            for (CfgNode successor : node.getSuccessors()) {
                walk(successor, next, path, sink);
            }
        }
        path.remove(path.size() - 1);
    }

    /**
     * The path state after passing {@code node}, or null when the analysis shows the path
     * cannot happen.
     */
    private PathState admit(CfgNode node, PathState state) {
        if (node instanceof CfgNode.InState inState) {
            if (analysis != null
                    && !analysis.allowsState(state, inState.getProcess(), inState.getState())) {
                return null;
            }
            return state.asserting(new Event.StateAsserted(inState.getProcess(), inState.getState()));
        }
        if (node instanceof CfgNode.Guard guard) {
            List<Term> asserted = Term.assertedBy(guard.getCondition());
            if (analysis != null && !analysis.allowsActivities(state, asserted)) {
                return null;
            }
            List<Event> events = new ArrayList<>();
            for (Term term : asserted) {
                if (term instanceof Term.ProcessActivity activity) {
                    events.add(new Event.StatusAsserted(activity.process(), activity.activity()));
                }
            }
            return state.asserting(events);
        }
        if (node instanceof CfgNode.TimeoutGuard timeout) {
            if (analysis != null && !analysis.allowsTimeout(
                    state, timeout.isExceeded(), timeout.getDuration())) {
                return null;
            }
        }
        return state;
    }

    // ------------------------------------------------------------------ conditions

    /** Numbers the states along one path and collects what it produces. */
    private static final class Builder {
        private final VerificationCondition main = new VerificationCondition();
        private final List<VerificationCondition> derived = new ArrayList<>();
        private int stateIndex;
        private int checks;
        private String current = "st0";

        String next() {
            return "st" + (++stateIndex);
        }
    }

    /** Converts one path into the conditions describing it. */
    private List<VerificationCondition> build(List<CfgNode> path) {
        Builder builder = new Builder();
        builder.main.add(new VcStatement.Invariant(builder.current));

        for (CfgNode node : path) {
            step(builder, node);
        }

        builder.main.add(new VcStatement.Final(IsabelleRenderer.FINAL_STATE, builder.current));
        builder.main.setFinalState(IsabelleRenderer.FINAL_STATE);

        List<VerificationCondition> conditions = new ArrayList<>();
        conditions.add(builder.main);
        conditions.addAll(builder.derived);
        return conditions;
    }

    private void step(Builder builder, CfgNode node) {
        if (node instanceof CfgNode.InState inState) {
            builder.main.add(new VcStatement.ProcessInState(
                    builder.current, inState.getProcess(), inState.getState()));
        } else if (node instanceof CfgNode.Guard guard) {
            builder.main.add(new VcStatement.Condition(builder.current, guard.getCondition()));
        } else if (node instanceof CfgNode.TimeoutGuard timeout) {
            builder.main.add(new VcStatement.TimeoutCheck(builder.current, timeout.getProcess(),
                    timeout.getDuration(), timeout.isExceeded()));
        } else if (node instanceof CfgNode.Assign assign) {
            String target = builder.next();
            builder.main.add(new VcStatement.Assign(
                    target, builder.current, assign.getTarget(), assign.getValue()));
            builder.current = target;
        } else if (node instanceof CfgNode.SetState setState) {
            String target = builder.next();
            builder.main.add(new VcStatement.SetProcessState(
                    target, builder.current, setState.getProcess(), setState.getState()));
            builder.current = target;
        } else if (node instanceof CfgNode.ResetTimer reset) {
            String target = builder.next();
            builder.main.add(new VcStatement.ResetTimer(target, builder.current, reset.getProcess()));
            builder.current = target;
        } else if (node instanceof CfgNode.ToEnv) {
            String target = builder.next();
            builder.main.add(new VcStatement.ToEnv(target, builder.current));
            builder.current = target;
        } else if (node instanceof CfgNode.Check check) {
            annotationCheck(builder, check);
        } else if (node instanceof CfgNode.LoopCut cut) {
            loopCut(builder, cut);
        } else if (node instanceof CfgNode.Unsupported unsupported) {
            throw new UnsupportedConstructException(unsupported);
        }
        // Entry, Exit and Join carry no assumption.
    }

    /**
     * An annotation met along the path. Both kinds produce an obligation discharging the
     * formula where it is written; an {@code assume} additionally lets the rest of the
     * path rely on it.
     */
    private void annotationCheck(Builder builder, CfgNode.Check check) {
        if (annotations == null) {
            return;
        }
        Annotation annotation = check.getAnnotation();
        su.nsk.iae.reflex.term.Term state = new su.nsk.iae.reflex.term.Term.Var(builder.current);
        su.nsk.iae.reflex.term.Term formula = annotations.translateAt(annotation, state, state);

        VerificationCondition obligation = builder.main.copy();
        obligation.setKind(annotation.getKind() == Annotation.Kind.ASSUME
                ? VerificationCondition.Kind.ASSUME
                : VerificationCondition.Kind.ASSERT);
        obligation.setConclusion(formula);
        obligation.setFinalState(builder.current);
        obligation.setNote(annotation.getKind().name().toLowerCase()
                + " at line " + annotation.getLine() + ": " + annotation.getText());
        builder.derived.add(obligation);

        if (annotation.getKind() == Annotation.Kind.ASSUME) {
            builder.main.add(new VcStatement.Assumption(
                    builder.current + "_assume_" + builder.checks++, formula));
        }
    }

    /**
     * A loop, split by its invariant and its condition into three parts, as the loop
     * section of the specification has it:
     *
     * <ul>
     *   <li>the invariant holds on entry;</li>
     *   <li>assuming it and the loop condition, one iteration preserves it;</li>
     *   <li>the path continues past the loop knowing the invariant, and that the condition
     *       has become false.</li>
     * </ul>
     *
     * <p>The state after the loop is opaque: how many iterations ran is not known, so
     * nothing is claimed of it beyond the invariant.
     */
    private void loopCut(Builder builder, CfgNode.LoopCut cut) {
        if (annotations == null) {
            throw new UnsupportedConstructException(new CfgNode.Unsupported("for",
                    "a loop needs its invariant translated, which needs annotations enabled"));
        }

        su.nsk.iae.reflex.term.Term preLoop = new su.nsk.iae.reflex.term.Term.Var(builder.current);
        AnnTranslator.Template template =
                annotations.translateLoopInvariant(cut.getInvariant(), preLoop);

        String afterLoop = builder.next();
        AnnTranslator.LoopInvariant outer = annotations.instantiateLoopInvariant(
                template, preLoop, preLoop, new su.nsk.iae.reflex.term.Term.Var(afterLoop));

        // The invariant holds when the loop is reached.
        VerificationCondition entry = builder.main.copy();
        entry.setKind(VerificationCondition.Kind.LOOP_ENTRY);
        entry.setConclusion(outer.onEntry());
        entry.setFinalState(builder.current);
        entry.setNote("loop invariant on entry, line " + cut.getInvariant().getLine());
        builder.derived.add(entry);

        // One iteration preserves it.
        builder.derived.addAll(preservationConditions(cut, template));

        // Past the loop.
        builder.main.add(new VcStatement.OpaqueState(afterLoop, builder.current));
        builder.main.add(new VcStatement.Assumption(afterLoop + "_invariant", outer.onExit()));
        builder.current = afterLoop;
        if (cut.getCondition() != null) {
            builder.main.add(new VcStatement.Condition(builder.current, negated(cut.getCondition())));
        }
    }

    /**
     * One condition per path through the loop body: it keeps the invariant.
     *
     * <p>The body is numbered from its own {@code st0}, so the invariant is instantiated
     * against those states rather than the enclosing path's - it is assumed up to where the
     * body starts and shown up to the environment step that ends the iteration.
     */
    private List<VerificationCondition> preservationConditions(
            CfgNode.LoopCut cut, AnnTranslator.Template template) {

        List<VerificationCondition> conditions = new ArrayList<>();
        Cfg bodyGraph = new Cfg(cut.getBodyEntry(), null, cfg.getProgram());
        // No pruning inside the body: the analysis reasons about whole cycles.
        new PathEnumerator(bodyGraph, null, annotations).forEach(bodyPath -> {
            if (bodyPath.getKind() != VerificationCondition.Kind.MAIN) {
                // An annotation inside the body keeps its own obligation.
                conditions.add(bodyPath);
                return;
            }
            su.nsk.iae.reflex.term.Term bodyStart = new su.nsk.iae.reflex.term.Term.Var("st0");
            su.nsk.iae.reflex.term.Term bodyEnd =
                    new su.nsk.iae.reflex.term.Term.Var(lastStateOf(bodyPath));
            AnnTranslator.LoopInvariant parts =
                    annotations.instantiateLoopInvariant(template, bodyStart, bodyEnd, bodyEnd);

            VerificationCondition preserved = new VerificationCondition();
            preserved.setKind(VerificationCondition.Kind.LOOP_PRESERVED);
            preserved.setNote("loop invariant preserved, line " + cut.getInvariant().getLine());

            preserved.add(new VcStatement.Assumption("loop_invariant", parts.assumedBeforeBody()));
            if (cut.getCondition() != null) {
                preserved.add(new VcStatement.Condition("st0", cut.getCondition()));
            }
            // The body's statements, less the invariant assumption a main path starts with.
            bodyPath.getStatements().stream()
                    .filter(statement -> !(statement instanceof VcStatement.Invariant))
                    .forEach(preserved::add);
            preserved.setConclusion(parts.shownAfterBody());
            preserved.setFinalState(bodyPath.getFinalState());
            conditions.add(preserved);
        });
        return conditions;
    }

    /** The last state a path actually reached, which its Final statement binds. */
    private static String lastStateOf(VerificationCondition condition) {
        for (VcStatement statement : condition.getStatements()) {
            if (statement instanceof VcStatement.Final last) {
                return last.source();
            }
        }
        return "st0";
    }

    private static IrExpr negated(IrExpr condition) {
        IrExpr result = new IrExpr.Unary(IrExpr.UnaryOp.NOT, IrCopier.copy(condition));
        result.setResultType(IrType.BOOL);
        return result;
    }

    /** Raised when a path reaches a construct VC generation does not support. */
    public static final class UnsupportedConstructException extends RuntimeException {
        private final transient CfgNode.Unsupported node;

        UnsupportedConstructException(CfgNode.Unsupported node) {
            super("This feature is not supported: " + node.getConstruct() + " - " + node.getDetail());
            this.node = node;
        }

        public CfgNode.Unsupported getNode() {
            return node;
        }
    }
}
