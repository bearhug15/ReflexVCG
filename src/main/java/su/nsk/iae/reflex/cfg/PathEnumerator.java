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
    private AnnTranslator fallbackTranslator;

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
        /** Every state named so far, in order, so a read pinned to an earlier one resolves. */
        private final List<String> states = new ArrayList<>(List.of("st0"));
        private int stateIndex;
        private int checks;
        private String current = "st0";

        String next() {
            String name = "st" + (++stateIndex);
            states.add(name);
            return name;
        }

        /**
         * The state {@code stepsBack} before {@link #current}, which is what a read pinned
         * by {@code ExprLowering} names.
         */
        String stateBefore(int stepsBack) {
            int index = states.size() - 1 - stepsBack;
            if (index < 0) {
                throw new IllegalStateException(
                        "a read pinned " + stepsBack + " states back, but only "
                                + states.size() + " have been named");
            }
            return states.get(index);
        }

        /**
         * Names the states an expression's pinned reads refer to.
         *
         * <p>A graph node is shared by every path through it and the paths number their
         * states differently, so this copies rather than filling the node's own
         * expression in. An expression with no pins - anything without a write inside it -
         * is returned untouched.
         */
        IrExpr resolve(IrExpr expr) {
            return hasPins(expr) ? pin(expr) : expr;
        }

        private IrExpr pin(IrExpr expr) {
            if (expr instanceof IrExpr.At at) {
                IrExpr resolved = new IrExpr.At(pin(at.getOperand()), stateBefore(at.getStepsBack()));
                resolved.setResultType(at.getResultType());
                return resolved;
            }
            if (expr instanceof IrExpr.VarRef ref) {
                List<IrExpr.Access> path = new ArrayList<>();
                for (IrExpr.Access access : ref.getAccesses()) {
                    path.add(access instanceof IrExpr.IndexAccess index
                            ? new IrExpr.IndexAccess(pin(index.getIndex()))
                            : access);
                }
                IrExpr.VarRef resolved = new IrExpr.VarRef(ref.getName(), path);
                resolved.setResultType(ref.getResultType());
                return resolved;
            }
            if (expr instanceof IrExpr.Binary binary) {
                IrExpr resolved = new IrExpr.Binary(binary.getOp(),
                        pin(binary.getLeft()), pin(binary.getRight()));
                resolved.setResultType(binary.getResultType());
                return resolved;
            }
            if (expr instanceof IrExpr.Unary unary) {
                IrExpr resolved = new IrExpr.Unary(unary.getOp(), pin(unary.getOperand()));
                resolved.setResultType(unary.getResultType());
                return resolved;
            }
            if (expr instanceof IrExpr.Cast cast) {
                IrExpr resolved = new IrExpr.Cast(cast.getTargetType(), pin(cast.getOperand()),
                        cast.getPreType(), cast.isImplicit());
                resolved.setResultType(cast.getResultType());
                return resolved;
            }
            if (expr instanceof IrExpr.Call call) {
                List<IrExpr> arguments = new ArrayList<>();
                call.getArguments().forEach(a -> arguments.add(pin(a)));
                IrExpr resolved = new IrExpr.Call(call.getFunction(), arguments);
                resolved.setResultType(call.getResultType());
                return resolved;
            }
            return expr;
        }

        private static boolean hasPins(IrExpr expr) {
            if (expr == null) {
                return false;
            }
            if (expr instanceof IrExpr.At) {
                return true;
            }
            if (expr instanceof IrExpr.VarRef ref) {
                return ref.getAccesses().stream()
                        .anyMatch(access -> access instanceof IrExpr.IndexAccess index
                                && hasPins(index.getIndex()));
            }
            if (expr instanceof IrExpr.Binary binary) {
                return hasPins(binary.getLeft()) || hasPins(binary.getRight());
            }
            if (expr instanceof IrExpr.Unary unary) {
                return hasPins(unary.getOperand());
            }
            if (expr instanceof IrExpr.Cast cast) {
                return hasPins(cast.getOperand());
            }
            if (expr instanceof IrExpr.Call call) {
                return call.getArguments().stream().anyMatch(Builder::hasPins);
            }
            return false;
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
            builder.main.add(new VcStatement.Condition(
                    builder.current, builder.resolve(guard.getCondition())));
        } else if (node instanceof CfgNode.TimeoutGuard timeout) {
            builder.main.add(new VcStatement.TimeoutCheck(builder.current, timeout.getProcess(),
                    timeout.getDuration(), timeout.isExceeded()));
        } else if (node instanceof CfgNode.Assign assign) {
            // Resolved before the write, since the value is read in the state before it.
            IrExpr value = builder.resolve(assign.getValue());
            IrExpr.VarRef into = (IrExpr.VarRef) builder.resolve(assign.getTarget());
            String target = builder.next();
            builder.main.add(new VcStatement.Assign(target, builder.current, into, value));
            builder.current = target;
        } else if (node instanceof CfgNode.InputChoice input) {
            String target = builder.next();
            builder.main.add(new VcStatement.InputChoice(
                    target, builder.current, input.getVariable(), input.getType()));
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
        AnnTranslator translator = translator();
        su.nsk.iae.reflex.term.Term preLoop = new su.nsk.iae.reflex.term.Term.Var(builder.current);
        AnnTranslator.Template template =
                translator.loopInvariantReference(cut.getInvariantName());

        String afterLoop = builder.next();
        AnnTranslator.LoopInvariant outer = translator.instantiateLoopInvariant(
                template, preLoop, preLoop, new su.nsk.iae.reflex.term.Term.Var(afterLoop));

        // The invariant holds when the loop is reached.
        VerificationCondition entry = builder.main.copy();
        entry.setKind(VerificationCondition.Kind.LOOP_ENTRY);
        entry.setConclusion(outer.onEntry());
        entry.setFinalState(builder.current);
        entry.setNote("loop invariant on entry, " + describe(cut));
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
                    translator().instantiateLoopInvariant(template, bodyStart, bodyEnd, bodyEnd);

            VerificationCondition preserved = new VerificationCondition();
            preserved.setKind(VerificationCondition.Kind.LOOP_PRESERVED);
            preserved.setNote("loop invariant preserved, " + describe(cut));

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

    /**
     * Which loop a derived condition came from, for the note it carries. The name is
     * enough to find it: the theory declaring it says which loop it belongs to.
     */
    private static String describe(CfgNode.LoopCut cut) {
        return cut.getInvariant() != null
                ? cut.getInvariantName()
                : cut.getInvariantName() + ", which no invariant was written for";
    }

    /**
     * The translator to build terms with. A loop with no invariant needs one even when the
     * caller supplied none, since the placeholder is still a term; nothing of the program's
     * own annotations is read to build it.
     */
    private AnnTranslator translator() {
        if (annotations != null) {
            return annotations;
        }
        if (fallbackTranslator == null) {
            fallbackTranslator = new AnnTranslator(1);
        }
        return fallbackTranslator;
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
