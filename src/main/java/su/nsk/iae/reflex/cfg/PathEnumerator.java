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

    /** Stands for the state a loop was entered at, in the conditions about its body. */
    private static final String LOOP_ENTRY_STATE = "t0";

    private final Cfg cfg;
    private final StaticAnalysis analysis;
    private final AnnTranslator annotations;
    private final List<VcStatement> preamble;
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
        this(cfg, analysis, annotations, List.of());
    }

    /**
     * @param preamble what every path may assume before its first step - for a loop body,
     *                 the invariant and the condition, so that a condition derived inside
     *                 the body knows what the iteration knew
     */
    private PathEnumerator(Cfg cfg, StaticAnalysis analysis, AnnTranslator annotations,
                           List<VcStatement> preamble) {
        this.cfg = cfg;
        this.analysis = analysis;
        this.annotations = annotations;
        this.preamble = List.copyOf(preamble);
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
        preamble.forEach(builder.main::add);

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
        String name = cut.getInvariantName();

        String afterLoop = builder.next();

        // The invariant holds when the loop is reached, the run starting there.
        VerificationCondition entry = builder.main.copy();
        entry.setKind(VerificationCondition.Kind.LOOP_ENTRY);
        entry.setConclusion(translator.loopInvariantAt(name, preLoop, preLoop));
        entry.setFinalState(builder.current);
        entry.setNote("loop invariant on entry, " + describe(cut));
        builder.derived.add(entry);

        // One iteration preserves it.
        builder.derived.addAll(preservationConditions(cut, name));

        // Past the loop.
        builder.main.add(new VcStatement.OpaqueState(afterLoop, builder.current));
        builder.main.add(new VcStatement.Assumption(afterLoop + "_invariant",
                translator.loopInvariantUpTo(name, preLoop,
                        new su.nsk.iae.reflex.term.Term.Var(afterLoop))));
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
     *
     * <p>Which iteration this is, and so which state the run began at, is not known here:
     * {@value #LOOP_ENTRY_STATE} stands for it, free in the condition and therefore
     * universally quantified, and the same state bounds the invariant on both sides.
     *
     * <p>The invariant and the condition seed every path through the body, so a condition
     * derived inside it - an annotation, or a loop nested in it - is stated knowing what
     * the iteration knew. A nested loop's own body is enumerated in its own frame and seeded
     * with its own invariant, so the two never mix.
     */
    private List<VerificationCondition> preservationConditions(
            CfgNode.LoopCut cut, String name) {

        List<VerificationCondition> conditions = new ArrayList<>();
        if (cut.getVariant() != null && annotations != null) {
            conditions.add(variantBound(cut, name));
        }
        su.nsk.iae.reflex.term.Term entry = new su.nsk.iae.reflex.term.Term.Var(LOOP_ENTRY_STATE);
        su.nsk.iae.reflex.term.Term bodyStart = new su.nsk.iae.reflex.term.Term.Var("st0");

        Cfg bodyGraph = new Cfg(cut.getBodyEntry(), null, cfg.getProgram());
        // No pruning inside the body: the analysis reasons about whole cycles.
        new PathEnumerator(bodyGraph, null, annotations, loopHypotheses(cut, name))
                .forEach(bodyPath -> {
            if (bodyPath.getKind() != VerificationCondition.Kind.MAIN) {
                // An annotation or a nested loop inside the body keeps its own obligation.
                conditions.add(bodyPath);
                return;
            }
            su.nsk.iae.reflex.term.Term afterIteration = su.nsk.iae.reflex.term.Terms.toEnv(
                    new su.nsk.iae.reflex.term.Term.Var(lastStateOf(bodyPath)));

            VerificationCondition preserved = new VerificationCondition();
            preserved.setKind(VerificationCondition.Kind.LOOP_PRESERVED);
            preserved.setNote("loop invariant preserved, " + describe(cut));
            // The body's statements, less the invariant assumption a main path starts with.
            bodyPath.getStatements().stream()
                    .filter(statement -> !(statement instanceof VcStatement.Invariant))
                    .forEach(preserved::add);
            preserved.setConclusion(translator().loopInvariantUpTo(name, entry, afterIteration));
            preserved.setFinalState(bodyPath.getFinalState());
            conditions.add(preserved);

            if (cut.getVariant() != null && annotations != null) {
                conditions.add(variantDecrease(cut, preserved, bodyStart, afterIteration));
            }
        });
        return conditions;
    }

    /**
     * Half of what a {@code [variant: ...]} claims: wherever an iteration may start, the
     * measure is at or above zero. Nothing of the body is read, so there is one of these
     * per loop rather than one per path through it.
     *
     * <p>Together with {@link #variantDecrease} and the loop's entry and preservation
     * conditions, this is what an argument that the loop terminates rests on - and so what
     * lets a path past the loop rely on there being a state to continue from at all.
     */
    private VerificationCondition variantBound(CfgNode.LoopCut cut, String name) {
        su.nsk.iae.reflex.term.Term bodyStart = new su.nsk.iae.reflex.term.Term.Var("st0");

        VerificationCondition bounded = new VerificationCondition();
        bounded.setKind(VerificationCondition.Kind.LOOP_VARIANT_BOUND);
        bounded.setNote("loop variant stays at or above zero, " + describe(cut));
        loopHypotheses(cut, name).forEach(bounded::add);
        bounded.setConclusion(new su.nsk.iae.reflex.term.Term.Infix("\\<ge>",
                annotations.translateAt(cut.getVariant(), bodyStart, bodyStart),
                new su.nsk.iae.reflex.term.Term.Var("0")));
        bounded.setFinalState("st0");
        return bounded;
    }

    /**
     * What an iteration may assume where it starts: the invariant, up to the state the
     * body is numbered from, and the loop's condition, since an iteration only runs while
     * it holds.
     */
    private List<VcStatement> loopHypotheses(CfgNode.LoopCut cut, String name) {
        su.nsk.iae.reflex.term.Term entry = new su.nsk.iae.reflex.term.Term.Var(LOOP_ENTRY_STATE);
        su.nsk.iae.reflex.term.Term bodyStart = new su.nsk.iae.reflex.term.Term.Var("st0");
        List<VcStatement> hypotheses = new ArrayList<>();
        hypotheses.add(new VcStatement.Assumption("st0_boundary",
                translator().loopBoundary(entry, bodyStart)));
        hypotheses.add(new VcStatement.Assumption("loop_invariant",
                translator().loopInvariantUpTo(name, entry, bodyStart)));
        if (cut.getCondition() != null) {
            hypotheses.add(new VcStatement.Condition("st0", cut.getCondition()));
        }
        return hypotheses;
    }

    /**
     * The other half: every iteration leaves the measure strictly smaller. It shares the
     * preservation condition's hypotheses, the body included, and compares the measure
     * where the iteration started with where it ended.
     */
    private VerificationCondition variantDecrease(
            CfgNode.LoopCut cut, VerificationCondition step,
            su.nsk.iae.reflex.term.Term bodyStart, su.nsk.iae.reflex.term.Term afterIteration) {

        VerificationCondition decreases = step.copy();
        decreases.setKind(VerificationCondition.Kind.LOOP_VARIANT_DECREASE);
        decreases.setNote("loop variant decreases, " + describe(cut));
        decreases.setConclusion(new su.nsk.iae.reflex.term.Term.Infix(">",
                annotations.translateAt(cut.getVariant(), bodyStart, bodyStart),
                annotations.translateAt(cut.getVariant(), afterIteration, afterIteration)));
        return decreases;
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
