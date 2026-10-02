package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Decides whether a path is still possible, applying the incompatibility rules of
 * static-analysis.md, section 4.
 *
 * <p>A path is checked as it is walked, node by node ({@link #step}), so an impossible one is
 * abandoned at the point it becomes impossible rather than being generated and then
 * discarded. Every rule is sound for the program's own runs: a cycle a run performs is never
 * discarded. {@code StaticAnalysisSoundnessTest} replays random runs of every test program
 * through {@link #step} to check exactly that.
 */
public final class StaticAnalysis {

    /** How the two rule sets are combined. */
    public enum Combination {
        /** Discard when either rule set finds the path impossible. */
        EITHER,
        /** Discard only when both do - the old specification's `||` read literally. */
        BOTH
    }

    private final IrProgram program;
    private final Map<IrNode, Attributes> attributes;
    private final ProcessFacts facts;
    private final Map<String, Set<String>> reachFrom;
    private final Map<String, IrExpr> constants;
    private final Combination combination;

    public StaticAnalysis(IrProgram program) {
        this(program, Combination.EITHER);
    }

    public StaticAnalysis(IrProgram program, Combination combination) {
        this.program = program;
        this.combination = combination;
        this.attributes = new AttributePreparation(program).run();
        this.facts = new ProcessFacts(program, attributes);
        this.reachFrom = computeReachFrom();
        this.constants = collectConstants();
    }

    public Attributes attributesOf(IrNode node) {
        return attributes.getOrDefault(node, Attributes.EMPTY);
    }

    public ProcessFacts getFacts() {
        return facts;
    }

    // ------------------------------------------------------------------ walking a path

    /**
     * The path after passing {@code node}, or null when the node makes it impossible. A node
     * is first checked against what the path knows - a dispatch, a guard, a timeout branch -
     * and then what it does is recorded: the process changes its statement carries and the
     * timers it zeroes.
     */
    public PathState step(PathState path, CfgNode node) {
        PathState next = path;
        if (node instanceof CfgNode.InState inState) {
            if (!allowsState(path, inState.getProcess(), inState.getState())) {
                return null;
            }
            next = path.asserting(new Event.StateAsserted(inState.getProcess(), inState.getState()));
        } else if (node instanceof CfgNode.Guard guard) {
            List<Term> asserted = Term.assertedBy(guard.getCondition());
            if (!allowsActivities(path, asserted)) {
                return null;
            }
            List<Event> events = new ArrayList<>();
            for (Term term : asserted) {
                if (term instanceof Term.ProcessActivity activity) {
                    events.add(new Event.StatusAsserted(activity.process(), activity.activity()));
                }
            }
            next = path.asserting(events);
        } else if (node instanceof CfgNode.TimeoutGuard timeout) {
            if (!allowsTimeout(path, timeout.getProcess(), timeout.isExceeded(), timeout.getDuration())) {
                return null;
            }
        }

        next = next.andThen(node.getAttributes());
        if (node instanceof CfgNode.SetState set) {
            next = next.asserting(new Event.TimerReset(set.getProcess()));
        } else if (node instanceof CfgNode.ResetTimer reset) {
            next = next.asserting(new Event.TimerReset(reset.getProcess()));
        } else if (node instanceof CfgNode.LoopCut) {
            next = next.asserting(new Event.TimePassed());
        }
        return next;
    }

    // ------------------------------------------------------------------ entry points

    /** Whether a path dispatching {@code process} in {@code state} is still possible. */
    public boolean allowsState(PathState path, String process, String state) {
        boolean simple = simpleAllowsState(path, process, state);
        boolean group = groupAllowsState(path, process, state);
        return combination == Combination.EITHER ? simple && group : simple || group;
    }

    /**
     * Section 4.2: whether a timeout branch of {@code process} is still possible.
     *
     * <p>The branch where the timeout has elapsed is impossible if the process's local time
     * was set to zero earlier in the cycle - by the process resetting its timer or changing
     * its own state, or by a process dispatched before it starting it - with no loop passed
     * since, and the duration is a known positive number. A duration held in a variable could
     * be zero, and so could a constant one, in which case the timeout elapses even at local
     * time zero. A loop lets time pass in the model the conditions are stated in: each
     * iteration ends in an environment step.
     */
    public boolean allowsTimeout(PathState path, String process, boolean exceeded, TimeRef duration) {
        if (!exceeded) {
            return true;
        }
        Long value = durationValue(duration);
        if (value == null || value <= 0) {
            return true;
        }
        boolean simple = !path.timerReset(process);
        return combination == Combination.EITHER ? simple : true;
    }

    /**
     * Sections 4.3 and 4.4: whether the status checks a guard asserts are still possible. A
     * guard asserting several facts about one process asserts all of them at once.
     */
    public boolean allowsActivities(PathState path, List<Term> asserted) {
        if (combination == Combination.BOTH) {
            return true;
        }
        PathState sofar = path;
        for (Term term : asserted) {
            if (term instanceof Term.ProcessActivity activity) {
                Set<Status> possible = sofar.possibleStatuses(activity.process(), atCycleStart(activity.process()));
                possible.retainAll(Status.of(activity.activity()));
                if (possible.isEmpty()) {
                    return false;
                }
                sofar = sofar.asserting(new Event.StatusAsserted(activity.process(), activity.activity()));
            }
        }
        return true;
    }

    /**
     * The statuses a process may have when a cycle begins - section 4.1. Every process may be
     * running; it may be stopped only if something can stop it or it may begin stopped, and
     * failed only if something can fail it.
     */
    public Set<Status> atCycleStart(String process) {
        ProcessFacts.Facts of = facts.of(process);
        Set<Status> statuses = EnumSet.of(Status.ACTIVE);
        if (of.reachS() || of.startS()) {
            statuses.add(Status.STOP);
        }
        if (of.reachE()) {
            statuses.add(Status.ERROR);
        }
        return statuses;
    }

    // ------------------------------------------------------------------ simple rules

    private boolean simpleAllowsState(PathState path, String process, String state) {
        ProcessFacts.Facts processFacts = facts.of(process);

        // 4.1.1: nothing can fail the process, so it is never in error.
        if (state.equals("error") && !processFacts.reachE()) {
            return false;
        }
        // 4.1.2: nothing can stop it and it is never found stopped in the first cycle.
        if (state.equals("stop") && !processFacts.reachS() && !processFacts.startS()) {
            return false;
        }
        // 4.5.1: the path changed the process this cycle, and it is running at its turn, so
        // the last change that happened was a start - it is in its first state.
        if (path.changed(process) && Status.ofState(state) == Status.ACTIVE
                && !state.equals(firstStateOf(process))) {
            return false;
        }
        // 4.5.2, 4.5.3 and 4.6: the state's status is not one the path still allows - the
        // last change put the process elsewhere, or a check since contradicts it.
        Set<Status> possible = path.possibleStatuses(process, atCycleStart(process));
        return possible.contains(Status.ofState(state));
    }

    // ------------------------------------------------------------------ group rules

    /** Section 4.7: processes of one group are started, stopped and failed together. */
    private boolean groupAllowsState(PathState path, String process, String state) {
        int group = facts.of(process).group();
        for (Event.StateAsserted other : path.otherProcessStates(process)) {
            if (facts.of(other.process()).group() != group) {
                continue;
            }
            // 4.7.1 and 4.7.2: one in error, or in stop, means both.
            if (state.equals("error") != other.state().equals("error")) {
                return false;
            }
            if (state.equals("stop") != other.state().equals("stop")) {
                return false;
            }
            // 4.7.3 and 4.7.4: one was just started, so the other was too, and has not run
            // since.
            if (justStarted(process, state) && !isFirstState(other.process(), other.state())) {
                return false;
            }
            if (justStarted(other.process(), other.state()) && !isFirstState(process, state)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether being in {@code state} means the process was started since its last turn: the
     * state is its first, no {@code set state} leads to it, and every path through it ends
     * in another state - so the process cannot have been there at its last turn and still be
     * there now.
     */
    private boolean justStarted(String process, String state) {
        if (!isFirstState(process, state)) {
            return false;
        }
        IrState first = firstState(process);
        return first != null
                && reachFrom.getOrDefault(qualify(process, state), Set.of()).isEmpty()
                && alwaysLeaves(first);
    }

    /** Every path through the state ends in another state (or in stop or error). */
    private boolean alwaysLeaves(IrState state) {
        Attributes of = attributesOf(state);
        return !of.mayStay() && !of.changesTo().isEmpty() && !of.changesTo().contains(state.getName());
    }

    private boolean isFirstState(String process, String state) {
        return state.equals(firstStateOf(process));
    }

    // ------------------------------------------------------------------ helpers

    private IrState firstState(String process) {
        IrProcess found = program.findProcess(process);
        return found == null ? null : found.getStartState();
    }

    private String firstStateOf(String process) {
        IrState first = firstState(process);
        return first == null ? null : first.getName();
    }

    private static String qualify(String process, String state) {
        return process + "::" + state;
    }

    /**
     * For each state, the states whose {@code set state} can move to it. A first state none
     * leads to is entered only by a start or a restart.
     */
    private Map<String, Set<String>> computeReachFrom() {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (IrProcess process : program.getProcesses()) {
            for (IrState state : process.getStates()) {
                Set<String> targets = new LinkedHashSet<>();
                collectTargets(state, targets);
                for (String target : targets) {
                    result.computeIfAbsent(qualify(process.getName(), target), k -> new LinkedHashSet<>())
                            .add(state.getName());
                }
            }
        }
        return result;
    }

    private static void collectTargets(IrNode construct, Set<String> targets) {
        if (construct instanceof IrStmt.SetState setState && setState.getState() != null) {
            targets.add(setState.getState());
        }
        for (IrNode line : ProcessFacts.lines(construct)) {
            collectTargets(line, targets);
        }
    }

    // ------------------------------------------------------------------ durations

    private Map<String, IrExpr> collectConstants() {
        Map<String, IrExpr> values = new LinkedHashMap<>();
        List<IrDecl.Constant> declared = new ArrayList<>(program.getConstants());
        program.getNodes().forEach(node -> declared.addAll(node.getConstants()));
        declared.forEach(constant -> values.put(constant.getName(), constant.getValue()));
        return values;
    }

    /** A timeout's duration as a number, or null when it is not a known constant. */
    Long durationValue(TimeRef duration) {
        return switch (duration.getKind()) {
            case TIME_LITERAL -> IsabelleRenderer.parseTimeMillis(duration.getText());
            case INTEGER -> IsabelleRenderer.parseInteger(duration.getText());
            case NAME -> constants.containsKey(duration.getText())
                    ? evaluate(constants.get(duration.getText()), 0) : null;
        };
    }

    /** A constant's value: literals, other constants, and + - * over them. */
    private Long evaluate(IrExpr expr, int depth) {
        if (expr == null || depth > 32) {
            return null;
        }
        if (expr instanceof IrExpr.Literal literal) {
            return switch (literal.getKind()) {
                case INTEGER -> IsabelleRenderer.parseInteger(literal.getText());
                case TIME -> IsabelleRenderer.parseTimeMillis(literal.getText());
                default -> null;
            };
        }
        if (expr instanceof IrExpr.Cast cast) {
            return evaluate(cast.getOperand(), depth + 1);
        }
        if (expr instanceof IrExpr.VarRef ref && ref.getAccesses().isEmpty() && constants.containsKey(ref.getName())) {
            return evaluate(constants.get(ref.getName()), depth + 1);
        }
        if (expr instanceof IrExpr.Unary unary) {
            Long operand = evaluate(unary.getOperand(), depth + 1);
            return operand == null ? null : switch (unary.getOp()) {
                case PLUS -> operand;
                case NEG -> -operand;
                default -> null;
            };
        }
        if (expr instanceof IrExpr.Binary binary) {
            Long left = evaluate(binary.getLeft(), depth + 1);
            Long right = evaluate(binary.getRight(), depth + 1);
            if (left == null || right == null) {
                return null;
            }
            return switch (binary.getOp()) {
                case ADD -> left + right;
                case SUB -> left - right;
                case MUL -> left * right;
                default -> null;
            };
        }
        return null;
    }
}
