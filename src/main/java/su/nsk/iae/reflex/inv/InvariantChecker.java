package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.inv.EntryCollector.Fact;
import su.nsk.iae.reflex.inv.EntryCollector.FactKind;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The one checker every candidate source goes through: Houdini over the cycle graph.
 *
 * <ol>
 *   <li>Keep the candidates that hold at the program's first boundary.</li>
 *   <li>Walk every path of one cycle, assuming everything still kept held where it began,
 *       and drop whatever may fail where it ends. Repeat until nothing is dropped: what is
 *       left then holds at the start, and is carried through every cycle by itself - an
 *       inductive invariant.</li>
 * </ol>
 *
 * <p>The walk is abstract - see {@link AbstractCycle} - but follows the same graph the
 * conditions are generated from, so a path it keeps is one a condition is generated for.
 * A candidate it keeps is meant to be provable from those conditions, and generation still
 * emits the obligations that prove it.
 *
 * <p>Candidates are checked at every boundary: the end of a cycle, and the ends of a loop's
 * iterations, which the model marks with an environment step as well. An invariant is
 * written in the wrap, which speaks about every boundary below the state it is stated at,
 * those inside loops included. A loop is only ever seen from outside, as what its body
 * cannot change, so what is checked at its iterations is what holds whatever the body did.
 */
final class InvariantChecker {

    /** A cycle with more paths than this is not analysed; conditions are not either. */
    private static final int MAX_PATHS = 1_000_000;

    private static final Term S2 = new Term.Var(AnalysisContext.TRANSITION_STATE);

    private final AnalysisContext context;
    private final Map<CfgNode.LoopCut, LoopEffects> loopEffects = new LinkedHashMap<>();
    private int paths;

    private record LoopEffects(Set<String> written, Set<String> moved) {
    }

    /** One walk's settings: what it assumes, what it checks, and whether it collects. */
    private record Walk(Map<String, List<Candidate>> hypotheses, Consumer<AbstractCycle> atBoundary,
                        List<EntryCollector> collectors, Set<CfgNode.LoopCut> walkedBodies) {

        boolean collecting() {
            return collectors != null;
        }
    }

    InvariantChecker(AnalysisContext context) {
        this.context = context;
    }

    // ------------------------------------------------------------------ Houdini

    /**
     * The candidates that are inductive, given that {@code fixed} - already known to be -
     * holds at the start of every cycle too. In the order given.
     */
    <C extends Candidate> List<C> houdini(List<C> candidates, List<? extends Candidate> fixed) {
        Set<C> standing = new LinkedHashSet<>(candidates);
        AbstractCycle start = startOfProgram();
        standing.removeIf(candidate -> !candidate.holdsAt(start));

        while (true) {
            List<Candidate> assumed = new ArrayList<>(fixed);
            assumed.addAll(standing);
            Set<C> failed = new LinkedHashSet<>();
            paths = 0;
            walk(context.cfg().getEntry(), new AbstractCycle(context, false),
                    new Walk(byProcess(assumed), cycle -> {
                        for (C candidate : standing) {
                            if (!candidate.holdsAt(cycle)) {
                                failed.add(candidate);
                            }
                        }
                    }, null, new LinkedHashSet<>()));
            if (failed.isEmpty()) {
                return new ArrayList<>(standing);
            }
            // Each round assumes less than the one before, so this ends.
            standing.removeAll(failed);
        }
    }

    /** Walks every possible path once more, telling each collector every way into a state. */
    void collect(List<? extends Candidate> hypotheses, List<EntryCollector> collectors) {
        paths = 0;
        walk(context.cfg().getEntry(), new AbstractCycle(context, false),
                new Walk(byProcess(hypotheses), cycle -> { }, collectors, new LinkedHashSet<>()));
    }

    private static Map<String, List<Candidate>> byProcess(List<? extends Candidate> candidates) {
        Map<String, List<Candidate>> index = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            for (String process : candidate.concerns()) {
                index.computeIfAbsent(process, p -> new ArrayList<>()).add(candidate);
            }
        }
        return index;
    }

    // ------------------------------------------------------------------ the first boundary

    /**
     * The first boundary, as the base case builds it: every variable at its declared
     * initial value or its type's default (what reading a variable never written gives),
     * the first process in its first state and every other stopped, every timer one tick
     * old. Before it lies only the empty state, where every process reads as stopped.
     *
     * <p>Mirrors {@link su.nsk.iae.reflex.vc.InitialCondition}; the two must agree. The
     * writes come first, as they do there, so nothing reads as written since the first
     * process entered its state.
     */
    AbstractCycle startOfProgram() {
        AbstractCycle start = new AbstractCycle(context, false);
        context.tracked().forEach((variable, type) -> start.setValue(variable,
                Terms.sortOf(type) == Terms.Sort.BOOL ? Value.of(false) : Value.of(BigInteger.ZERO)));
        context.written().forEach(variable -> {
            if (!context.tracked().containsKey(variable)) {
                start.setValue(variable, null);
            }
        });
        List<IrDecl> initialised = new ArrayList<>(context.program().getGlobalVariables());
        context.program().getNodes().forEach(node -> initialised.addAll(node.getVariables()));
        for (IrDecl declaration : initialised) {
            if (declaration instanceof IrDecl.Variable variable
                    && variable.getInitializer() != null
                    && context.tracked().containsKey(variable.getName())) {
                Value value = start.evaluate(variable.getInitializer());
                start.setValue(variable.getName(), value == null ? null
                        : AbstractCycle.convert(value, value.isBool() ? Terms.Sort.BOOL : Terms.Sort.INT,
                        Terms.sortOf(variable.getType())));
            }
        }

        for (String process : context.processes()) {
            start.setPstate(process, "stop");
        }
        start.boundaryPassed();
        if (context.firstProcess() != null && context.firstState() != null) {
            start.setPstate(context.firstProcess(), context.firstState());
        }
        // Every process's ltime is 0 until the environment step closing initialisation.
        for (String process : context.processes()) {
            start.setTimerBelow(process, context.clock() + 1);
        }
        return start;
    }

    // ------------------------------------------------------------------ walking

    private void walk(CfgNode node, AbstractCycle cycle, Walk walk) {
        if (node instanceof CfgNode.SetState setState && cycle.startUnseen(setState.getProcess())) {
            // Moved before its own body ran, so where it began the cycle would never be
            // seen - and the hypotheses about it, never applied. Split over the states it
            // can have begun in instead.
            String process = setState.getProcess();
            for (String state : context.reachableStates(process)) {
                AbstractCycle branch = cycle.copy();
                if (branch.learnStart(process, state, c -> assumeAtStart(c, process, walk))) {
                    walk(node, branch, walk);
                }
            }
            return;
        }
        step(node, cycle, walk);
        if (cycle.isImpossible()) {
            return;
        }
        List<CfgNode> successors = node.getSuccessors();
        if (successors.isEmpty()) {
            if (++paths > MAX_PATHS) {
                throw new IllegalStateException("extra invariant analysis exceeded "
                        + MAX_PATHS + " paths through one cycle");
            }
            if (walk.collecting()) {
                deliverEntries(cycle, walk.collectors());
            }
            return;
        }
        // The last successor takes the state itself; every other a copy.
        for (int i = 0; i < successors.size(); i++) {
            walk(successors.get(i), i == successors.size() - 1 ? cycle : cycle.copy(), walk);
        }
    }

    private void step(CfgNode node, AbstractCycle cycle, Walk walk) {
        if (node instanceof CfgNode.InState inState) {
            String process = inState.getProcess();
            if (!cycle.enter(process, inState.getState(), c -> assumeAtStart(c, process, walk))) {
                cycle.markImpossible();
            }
        } else if (node instanceof CfgNode.Guard guard) {
            IrExpr condition = guard.getCondition();
            Value value = cycle.evaluate(condition);
            if (value != null && !value.truth()) {
                cycle.markImpossible();
            } else if (walk.collecting() && value == null && AbstractCycle.isPure(condition)) {
                cycle.addFact(new Fact(FactKind.GUARD,
                        context.expression(condition, AnalysisContext.TRANSITION_STATE),
                        AbstractCycle.variablesRead(condition), AbstractCycle.processesRead(condition),
                        null, null, null));
            }
        } else if (node instanceof CfgNode.TimeoutGuard timeout) {
            String process = timeout.getProcess();
            if (!cycle.timeout(process, context.durationOf(timeout.getDuration()), timeout.isExceeded())) {
                cycle.markImpossible();
            } else if (walk.collecting()) {
                Term elapsed = Terms.localTime(S2, process);
                Term duration = context.durationTerm(timeout.getDuration(), S2);
                cycle.addFact(new Fact(
                        timeout.isExceeded() ? FactKind.TIMEOUT_REACHED : FactKind.TIMEOUT_NOT_REACHED,
                        new Term.Infix(timeout.isExceeded() ? "\\<ge>" : "<", elapsed, duration),
                        timeout.getDuration().isName() ? Set.of(timeout.getDuration().getText()) : Set.of(),
                        Set.of(), process, null, null));
            }
        } else if (node instanceof CfgNode.Assign assign) {
            assign(assign, cycle, walk);
        } else if (node instanceof CfgNode.InputChoice input) {
            cycle.setValue(input.getVariable(), null);
        } else if (node instanceof CfgNode.SetState setState) {
            if (walk.collecting()) {
                cycle.recordEntry(setState.getProcess(), setState.getState());
            }
            cycle.setPstate(setState.getProcess(), setState.getState());
        } else if (node instanceof CfgNode.ResetTimer reset) {
            cycle.resetTimer(reset.getProcess());
        } else if (node instanceof CfgNode.ToEnv) {
            cycle.advanceTimers();
            walk.atBoundary().accept(cycle);
            cycle.timePasses();
            cycle.boundaryPassed();
        } else if (node instanceof CfgNode.LoopCut cut) {
            LoopEffects effects = effectsOf(cut);
            cycle.loopRan(effects.written(), effects.moved());
            // Every iteration ends in an environment step, so the ends of iterations are
            // boundaries too, and a wrapped invariant speaks about them. They are where the
            // state after the loop is: what the body writes unknown, and the boundary before
            // each - the previous iteration's, or the last before the loop - unknown too.
            cycle.boundaryUnknown();
            walk.atBoundary().accept(cycle);
            // The state past a loop is a boundary: predEnv of what follows reaches it.
            cycle.boundaryPassed();
            if (walk.collecting() && walk.walkedBodies().add(cut)) {
                // A set state inside the body is a way into its target too. The body
                // starts mid-cycle, where nothing is known.
                AbstractCycle body = new AbstractCycle(context, true);
                body.setExecuting(cycle.executing());
                walk(cut.getBodyEntry(), body, walk);
            }
        }
        // Joins, checks, the entry and the exit change nothing.
    }

    private void assign(CfgNode.Assign assign, AbstractCycle cycle, Walk walk) {
        IrExpr.VarRef target = assign.getTarget();
        String variable = target.getName();
        IrExpr value = assign.getValue();
        boolean whole = target.getAccesses().isEmpty();
        cycle.setValue(variable, whole ? cycle.evaluate(value) : null);

        IrType type = target.getResultType();
        if (walk.collecting() && whole && AnalysisContext.isScalar(type) && AbstractCycle.isPure(value)
                && !AbstractCycle.variablesRead(value).contains(variable)) {
            // Holds until the variable or anything the value reads is written again.
            Term read = context.expression(value, AnalysisContext.TRANSITION_STATE);
            Set<String> reads = new LinkedHashSet<>(AbstractCycle.variablesRead(value));
            reads.add(variable);
            cycle.addFact(new Fact(FactKind.ASSIGNMENT,
                    new Term.Infix("=", Terms.valueGetter(S2, type, variable, List.of()), read),
                    reads, AbstractCycle.processesRead(value), null, variable, read));
        }
    }

    /** Brings in what the hypotheses say about a process's starting state. */
    private static boolean assumeAtStart(AbstractCycle cycle, String process, Walk walk) {
        for (Candidate candidate : walk.hypotheses().getOrDefault(process, List.of())) {
            if (!candidate.assume(cycle)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The state changes on a path that turned out possible, each told to the collectors -
     * unless the process was already in the target state, in which case its state did not
     * change and the move is not one {@code prevProcState} stops at.
     */
    private void deliverEntries(AbstractCycle cycle, List<EntryCollector> collectors) {
        for (AbstractCycle.PendingEntry pending : cycle.pending()) {
            if (!context.declaredStates(pending.process()).contains(pending.state())) {
                continue;
            }
            String before = cycle.resolve(pending.before());
            if (pending.state().equals(before)) {
                continue;
            }
            EntryCollector.Entry entry = new EntryCollector.Entry(pending.process(), pending.state(),
                    before, pending.executing(), cycle.resolve(pending.executingState()),
                    pending.facts(), cycle.isInsideLoop());
            for (EntryCollector collector : collectors) {
                collector.entry(entry, context);
            }
        }
    }

    private LoopEffects effectsOf(CfgNode.LoopCut cut) {
        return loopEffects.computeIfAbsent(cut, c -> {
            Set<String> written = new LinkedHashSet<>();
            Set<String> moved = new LinkedHashSet<>();
            Set<CfgNode> seen = new LinkedHashSet<>();
            Deque<CfgNode> pending = new ArrayDeque<>();
            pending.add(c.getBodyEntry());
            while (!pending.isEmpty()) {
                CfgNode node = pending.pop();
                if (!seen.add(node)) {
                    continue;
                }
                if (node instanceof CfgNode.Assign assign) {
                    written.add(assign.getTarget().getName());
                } else if (node instanceof CfgNode.InputChoice input) {
                    written.add(input.getVariable());
                } else if (node instanceof CfgNode.SetState setState) {
                    moved.add(setState.getProcess());
                } else if (node instanceof CfgNode.LoopCut inner) {
                    pending.add(inner.getBodyEntry());
                }
                pending.addAll(node.getSuccessors());
            }
            return new LoopEffects(written, moved);
        });
    }
}
