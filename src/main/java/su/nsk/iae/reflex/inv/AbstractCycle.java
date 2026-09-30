package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.inv.EntryCollector.Fact;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.term.Terms;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * What a walk through one cycle knows about the program state, abstractly.
 *
 * <p>A variable is either a known constant, unknown, or <em>as it was at the start of the
 * cycle</em> - the last is what lets a fact assumed about the cycle's first state (the
 * induction hypothesis) be used once the walk learns which state each process began in.
 * A process's state is known, unknown, or likewise as it began. The walk learns where a
 * process began when it reaches that process's {@link CfgNode.InState}: every path passes
 * exactly one per process.
 *
 * <p>Alongside that it keeps a strict upper bound on each process's {@code ltime}, the
 * order of every write and state change - which is what says whether a variable was
 * written since a process last changed state - and, for the transition analysis, what still
 * holds of the running process's body and each state change met.
 *
 * <p>Copied at every branch, so the two sides of a branch never see each other's effects.
 */
final class AbstractCycle implements Boundary, CycleStart {

    /** A process's state at some point: known, unknown, or as it was when the cycle began. */
    record PState(String process, String known, boolean initial) {
    }

    /** A state change, and where it fell among the writes. */
    private record PstateEvent(String process, String target, PState before, int position) {
    }

    /** A state change met on the path, kept until the path is known to be possible. */
    record PendingEntry(String process, String state, PState before,
                        String executing, PState executingState, List<Fact> facts) {
    }

    private final AnalysisContext context;
    /**
     * True for a walk starting somewhere nothing is known about: the body of a loop, which
     * starts mid-cycle. Then "as the cycle began" means unknown.
     */
    private final boolean opaqueStart;

    private final Map<String, Value> values;
    private final Set<String> unknownValues;
    private final Map<String, String> pstates;
    private final Set<String> unknownPstates;
    private final Map<String, String> initialPstates;
    private final Map<String, String> boundaryPstates;
    private final Set<String> boundaryUnknown;
    private final Map<String, Value> startValues;
    /** Every variable written, in order. */
    private final List<String> writes;
    private final List<PstateEvent> events;
    /** Bounds on ltime: absent means as at the start, a null value unknown. */
    private final Map<String, Long> timers;
    /** Bounds on ltime as it was at the start of the cycle. */
    private final Map<String, Long> startTimers;

    private String executing;
    private final List<Fact> facts;
    private final List<PendingEntry> pending;
    private boolean impossible;

    AbstractCycle(AnalysisContext context, boolean opaqueStart) {
        this.context = context;
        this.opaqueStart = opaqueStart;
        this.values = new LinkedHashMap<>();
        this.unknownValues = new LinkedHashSet<>();
        this.pstates = new LinkedHashMap<>();
        this.unknownPstates = new LinkedHashSet<>();
        this.initialPstates = new LinkedHashMap<>();
        this.boundaryPstates = new LinkedHashMap<>();
        this.boundaryUnknown = new LinkedHashSet<>();
        this.startValues = new LinkedHashMap<>();
        this.writes = new ArrayList<>();
        this.events = new ArrayList<>();
        this.timers = new LinkedHashMap<>();
        this.startTimers = new LinkedHashMap<>();
        this.facts = new ArrayList<>();
        this.pending = new ArrayList<>();
    }

    private AbstractCycle(AbstractCycle other) {
        this.context = other.context;
        this.opaqueStart = other.opaqueStart;
        this.values = new LinkedHashMap<>(other.values);
        this.unknownValues = new LinkedHashSet<>(other.unknownValues);
        this.pstates = new LinkedHashMap<>(other.pstates);
        this.unknownPstates = new LinkedHashSet<>(other.unknownPstates);
        this.initialPstates = new LinkedHashMap<>(other.initialPstates);
        this.boundaryPstates = new LinkedHashMap<>(other.boundaryPstates);
        this.boundaryUnknown = new LinkedHashSet<>(other.boundaryUnknown);
        this.startValues = new LinkedHashMap<>(other.startValues);
        this.writes = new ArrayList<>(other.writes);
        this.events = new ArrayList<>(other.events);
        this.timers = new LinkedHashMap<>(other.timers);
        this.startTimers = new LinkedHashMap<>(other.startTimers);
        this.executing = other.executing;
        this.facts = new ArrayList<>(other.facts);
        this.pending = new ArrayList<>(other.pending);
        this.impossible = other.impossible;
    }

    AbstractCycle copy() {
        return new AbstractCycle(this);
    }

    boolean isImpossible() {
        return impossible;
    }

    void markImpossible() {
        impossible = true;
    }

    boolean isInsideLoop() {
        return opaqueStart;
    }

    List<PendingEntry> pending() {
        return pending;
    }

    String executing() {
        return executing;
    }

    void setExecuting(String process) {
        executing = process;
    }

    // ------------------------------------------------------------------ Boundary

    @Override
    public String pstate(String process) {
        return resolve(stateNow(process));
    }

    @Override
    public String previousPstate(String process) {
        if (boundaryUnknown.contains(process)) {
            return null;
        }
        String known = boundaryPstates.get(process);
        return known != null ? known : resolve(new PState(process, null, true));
    }

    @Override
    public Value value(String variable) {
        if (unknownValues.contains(variable)) {
            return null;
        }
        Value known = values.get(variable);
        if (known != null) {
            return known;
        }
        // Untouched since the cycle began. A constant has its declared value at every
        // boundary - the global invariant says so - and anything else has whatever the
        // induction hypothesis says it had.
        Value constant = context.constants().get(variable);
        if (constant != null) {
            return constant;
        }
        return opaqueStart ? null : startValues.get(variable);
    }

    @Override
    public Long timerBelow(String process) {
        if (timers.containsKey(process)) {
            return timers.get(process);
        }
        return opaqueStart ? null : startTimers.get(process);
    }

    /**
     * The window starts at the last state change of the process known to have changed its
     * state - the one {@code prevProcState} stops at, or a later one. A change whose effect
     * cannot be told is left out, which only makes the window longer than it has to be.
     */
    @Override
    public boolean writtenSinceEntry(String process, String variable) {
        int start = 0;
        boolean lost = false;
        for (PstateEvent event : events) {
            if (!event.process().equals(process)) {
                continue;
            }
            if (event.target() == null) {
                // Moved inside a loop, past which prevProcState is not known at all.
                lost = true;
                continue;
            }
            String before = resolve(event.before());
            if (before != null && !before.equals(event.target())) {
                start = event.position();
                lost = false;
            }
        }
        return lost || writes.subList(start, writes.size()).contains(variable);
    }

    // ------------------------------------------------------------------ CycleStart

    @Override
    public String initialState(String process) {
        return initialPstates.get(process);
    }

    @Override
    public boolean assumeValue(String variable, Value value) {
        Value previous = startValues.putIfAbsent(variable, value);
        return previous == null || previous.equals(value);
    }

    @Override
    public void assumeTimerBelow(String process, long bound) {
        startTimers.merge(process, bound, Math::min);
    }

    // ------------------------------------------------------------------ reading

    PState stateNow(String process) {
        if (unknownPstates.contains(process)) {
            return new PState(process, null, false);
        }
        String known = pstates.get(process);
        return known != null
                ? new PState(process, known, false)
                : new PState(process, null, true);
    }

    /** What a recorded process state turned out to be, now more of the path is known. */
    String resolve(PState state) {
        if (state == null) {
            return null;
        }
        if (state.known() != null) {
            return state.known();
        }
        if (!state.initial() || opaqueStart) {
            return null;
        }
        return initialPstates.get(state.process());
    }

    // ------------------------------------------------------------------ effects

    /** A write: a known constant, or null for a value not known. */
    void setValue(String variable, Value value) {
        if (value == null) {
            values.remove(variable);
            unknownValues.add(variable);
        } else {
            unknownValues.remove(variable);
            values.put(variable, value);
        }
        writes.add(variable);
        facts.removeIf(fact -> fact.reads().contains(variable));
    }

    /** A state change, or - with a null state - one the walk cannot see, inside a loop. */
    void setPstate(String process, String state) {
        if (state == null) {
            events.add(new PstateEvent(process, null, null, writes.size()));
            pstates.remove(process);
            unknownPstates.add(process);
        } else {
            events.add(new PstateEvent(process, state, stateNow(process), writes.size()));
            unknownPstates.remove(process);
            pstates.put(process, state);
            // setPstate restarts the process's time in its state.
            timers.put(process, 1L);
        }
        facts.removeIf(fact -> fact.processes().contains(process) || process.equals(fact.timerOf()));
    }

    void resetTimer(String process) {
        timers.put(process, 1L);
        facts.removeIf(fact -> process.equals(fact.timerOf()));
    }

    void setTimerBelow(String process, Long bound) {
        timers.put(process, bound);
    }

    /**
     * A timeout's check: whether the time in the state has reached {@code duration}.
     *
     * @return false when that contradicts what is known of the timer
     */
    boolean timeout(String process, Long duration, boolean reached) {
        if (duration == null) {
            return true;
        }
        Long below = timerBelow(process);
        if (reached) {
            return below == null || below > duration;
        }
        // Unchanged since it was last known, so what holds now holds from here on too.
        timers.put(process, below == null ? duration : Math.min(below, duration));
        return true;
    }

    /** An environment step: every process's time in its state grows by one tick. */
    void advanceTimers() {
        for (String process : context.processes()) {
            Long below = timerBelow(process);
            timers.put(process, below == null ? null : below + context.clock());
        }
    }

    /**
     * A loop has run, some unknown number of times: what its body writes is unknown, the
     * processes it moves are in unknown states, and every timer has moved on by an unknown
     * number of iterations, each of which ends in an environment step.
     */
    void loopRan(Set<String> written, Set<String> moved) {
        written.forEach(variable -> setValue(variable, null));
        moved.forEach(process -> setPstate(process, null));
        context.processes().forEach(process -> timers.put(process, null));
        timePasses();
    }

    /** Every timer has moved on: facts about them no longer hold. */
    void timePasses() {
        facts.removeIf(fact -> fact.timerOf() != null);
    }

    /**
     * The process's body begins, and it is in {@code state}. When the process had not been
     * moved since the cycle began, that is also where it began, which is what brings the
     * hypothesis about that state into play; {@code onLearnt} is told so.
     *
     * @return false when this contradicts what is already known
     */
    boolean enter(String process, String state, Predicate<AbstractCycle> onLearnt) {
        executing = process;
        facts.clear();
        if (unknownPstates.contains(process)) {
            unknownPstates.remove(process);
            pstates.put(process, state);
            return true;
        }
        String known = pstates.get(process);
        if (known != null) {
            return known.equals(state);
        }
        pstates.put(process, state);
        if (opaqueStart) {
            return true;
        }
        initialPstates.put(process, state);
        return onLearnt.test(this);
    }

    /**
     * Whether the process is still as it began the cycle, without the walk knowing where
     * that was - so a state change now would hide it for good.
     */
    boolean startUnseen(String process) {
        return !opaqueStart && !unknownPstates.contains(process) && !pstates.containsKey(process)
                && !initialPstates.containsKey(process);
    }

    /**
     * The process began the cycle in {@code state}, which brings the hypotheses about that
     * state into play; its state now is still that one.
     *
     * @return false when that contradicts what is already known
     */
    boolean learnStart(String process, String state, Predicate<AbstractCycle> onLearnt) {
        initialPstates.put(process, state);
        return onLearnt.test(this);
    }

    /** The boundary before this one is not known, for any process. */
    void boundaryUnknown() {
        for (String process : context.processes()) {
            boundaryPstates.remove(process);
            boundaryUnknown.add(process);
        }
    }

    /** The state reached is a boundary, so the next one's {@code predEnv} is this one. */
    void boundaryPassed() {
        for (String process : context.processes()) {
            PState now = stateNow(process);
            if (now.known() != null) {
                boundaryUnknown.remove(process);
                boundaryPstates.put(process, now.known());
            } else if (!now.initial()) {
                boundaryPstates.remove(process);
                boundaryUnknown.add(process);
            }
            // As at the start: the boundary state is where it began, which it still reads.
        }
    }

    void addFact(Fact fact) {
        facts.add(fact);
    }

    void recordEntry(String process, String state) {
        pending.add(new PendingEntry(process, state, stateNow(process), executing,
                executing == null ? null : stateNow(executing), List.copyOf(facts)));
    }

    // ------------------------------------------------------------------ evaluation

    /**
     * Evaluates an expression as far as what is known allows: a constant, or null when
     * the value depends on something unknown or is not modelled here.
     *
     * <p>Follows the HOL reading the renderer gives the expression, not C: numbers are
     * unbounded, and a subtraction whose result is a nat stops at zero.
     */
    Value evaluate(IrExpr expr) {
        if (expr instanceof IrExpr.Literal literal) {
            return switch (literal.getKind()) {
                case BOOL -> Value.of(literal.getText().equals("true"));
                case INTEGER -> Value.of(IsabelleRenderer.parseInteger(literal.getText()));
                case TIME -> Value.of(IsabelleRenderer.parseTimeMillis(literal.getText()));
                case FLOAT -> null;
            };
        }
        if (expr instanceof IrExpr.VarRef ref) {
            return ref.getAccesses().isEmpty() ? value(ref.getName()) : null;
        }
        if (expr instanceof IrExpr.Cast cast) {
            return convert(evaluate(cast.getOperand()),
                    Terms.sortOf(cast.getPreType()), Terms.sortOf(cast.getTargetType()));
        }
        if (expr instanceof IrExpr.Unary unary) {
            Value operand = evaluate(unary.getOperand());
            if (operand == null) {
                return null;
            }
            return switch (unary.getOp()) {
                case NOT -> Value.of(!operand.truth());
                case PLUS -> operand;
                case NEG -> Terms.sortOf(unary.getResultType()) == Terms.Sort.INT
                        ? Value.of(operand.number().negate()) : null;
                case BIT_NOT -> null;
            };
        }
        if (expr instanceof IrExpr.Binary binary) {
            return evaluateBinary(binary);
        }
        if (expr instanceof IrExpr.CheckState check) {
            String state = pstate(check.getProcess());
            if (state == null) {
                return null;
            }
            boolean stopped = state.equals("stop");
            boolean failed = state.equals("error");
            return Value.of(switch (check.getStatus()) {
                case STOP -> stopped;
                case ERROR -> failed;
                case INACTIVE -> stopped || failed;
                case ACTIVE -> !stopped && !failed;
            });
        }
        // Reads pinned to an earlier state, writes, calls: not followed.
        return null;
    }

    private Value evaluateBinary(IrExpr.Binary binary) {
        Value left = evaluate(binary.getLeft());
        Value right = evaluate(binary.getRight());
        switch (binary.getOp()) {
            case AND:
                if ((left != null && !left.truth()) || (right != null && !right.truth())) {
                    return Value.of(false);
                }
                return left == null || right == null ? null : Value.of(true);
            case OR:
                if ((left != null && left.truth()) || (right != null && right.truth())) {
                    return Value.of(true);
                }
                return left == null || right == null ? null : Value.of(false);
            default:
                break;
        }
        if (left == null || right == null) {
            return null;
        }
        BigInteger a = left.number();
        BigInteger b = right.number();
        return switch (binary.getOp()) {
            case ADD -> Value.of(a.add(b));
            case MUL -> Value.of(a.multiply(b));
            case SUB -> {
                BigInteger difference = a.subtract(b);
                yield Value.of(Terms.sortOf(binary.getResultType()) == Terms.Sort.NAT
                        && difference.signum() < 0 ? BigInteger.ZERO : difference);
            }
            case LT -> Value.of(a.compareTo(b) < 0);
            case LE -> Value.of(a.compareTo(b) <= 0);
            case GT -> Value.of(a.compareTo(b) > 0);
            case GE -> Value.of(a.compareTo(b) >= 0);
            case EQ -> Value.of(a.equals(b));
            case NE -> Value.of(!a.equals(b));
            // Division rounds differently in HOL and C, and the bitwise operators are not
            // modelled; neither matters for the constants invariants are made of.
            default -> null;
        };
    }

    /** The conversions {@link Terms#cast} makes, on values. */
    static Value convert(Value value, Terms.Sort from, Terms.Sort to) {
        if (value == null || to == Terms.Sort.REAL || from == Terms.Sort.REAL) {
            return null;
        }
        if (from == to) {
            return value;
        }
        return switch (to) {
            case BOOL -> Value.of(value.truth());
            case INT -> Value.of(value.number());
            case NAT -> Value.of(value.number().signum() < 0 ? BigInteger.ZERO : value.number());
            case REAL -> null;
        };
    }

    // ------------------------------------------------------------------ expression shape

    /** The variables an expression reads, wherever they appear in it. */
    static Set<String> variablesRead(IrExpr expr) {
        Set<String> read = new LinkedHashSet<>();
        collect(expr, read, new LinkedHashSet<>());
        return read;
    }

    /** The processes whose state an expression reads. */
    static Set<String> processesRead(IrExpr expr) {
        Set<String> processes = new LinkedHashSet<>();
        collect(expr, new LinkedHashSet<>(), processes);
        return processes;
    }

    /**
     * Whether an expression can be restated at another state as it is: it reads, and
     * does nothing else, and reads only the state it is evaluated in.
     */
    static boolean isPure(IrExpr expr) {
        if (expr == null) {
            return true;
        }
        if (expr instanceof IrExpr.Literal || expr instanceof IrExpr.CheckState) {
            return true;
        }
        if (expr instanceof IrExpr.VarRef ref) {
            for (IrExpr.Access access : ref.getAccesses()) {
                if (access instanceof IrExpr.IndexAccess index && !isPure(index.getIndex())) {
                    return false;
                }
            }
            return true;
        }
        if (expr instanceof IrExpr.Cast cast) {
            return isPure(cast.getOperand());
        }
        if (expr instanceof IrExpr.Unary unary) {
            return isPure(unary.getOperand());
        }
        if (expr instanceof IrExpr.Binary binary) {
            return isPure(binary.getLeft()) && isPure(binary.getRight());
        }
        // Pinned reads, writes and calls to functions no theory defines.
        return false;
    }

    private static void collect(IrExpr expr, Set<String> variables, Set<String> processes) {
        if (expr == null) {
            return;
        }
        if (expr instanceof IrExpr.VarRef ref) {
            variables.add(ref.getName());
            for (IrExpr.Access access : ref.getAccesses()) {
                if (access instanceof IrExpr.IndexAccess index) {
                    collect(index.getIndex(), variables, processes);
                }
            }
        } else if (expr instanceof IrExpr.CheckState check) {
            processes.add(check.getProcess());
        } else if (expr instanceof IrExpr.Cast cast) {
            collect(cast.getOperand(), variables, processes);
        } else if (expr instanceof IrExpr.Unary unary) {
            collect(unary.getOperand(), variables, processes);
        } else if (expr instanceof IrExpr.Binary binary) {
            collect(binary.getLeft(), variables, processes);
            collect(binary.getRight(), variables, processes);
        } else if (expr instanceof IrExpr.Assign assign) {
            collect(assign.getTarget(), variables, processes);
            collect(assign.getValue(), variables, processes);
        } else if (expr instanceof IrExpr.IncDec incDec) {
            collect(incDec.getTarget(), variables, processes);
        } else if (expr instanceof IrExpr.At at) {
            collect(at.getOperand(), variables, processes);
        } else if (expr instanceof IrExpr.Call call) {
            call.getArguments().forEach(argument -> collect(argument, variables, processes));
        }
    }
}
