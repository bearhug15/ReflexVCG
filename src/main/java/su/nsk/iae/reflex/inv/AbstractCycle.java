package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.term.Term;
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
 * <p>It also carries what the transition analysis needs: the guards passed since the
 * running process's body began, dropped as soon as something they read is written, and
 * each {@code set state} met, to be judged when the path is known to be possible.
 *
 * <p>Copied at every branch, so the two sides of a branch never see each other's effects.
 */
final class AbstractCycle {

    /** A constant value: a bool, or a number of whichever HOL sort the variable has. */
    record Const(boolean isBool, BigInteger number) {
        static Const of(boolean value) {
            return new Const(true, value ? BigInteger.ONE : BigInteger.ZERO);
        }

        static Const of(BigInteger value) {
            return new Const(false, value);
        }

        boolean truth() {
            return number.signum() != 0;
        }

        Term term() {
            if (isBool) {
                return truth() ? Terms.TRUE : Terms.FALSE;
            }
            return number.signum() < 0
                    ? new Term.Prefix("-", new Term.Var(number.negate().toString()))
                    : new Term.Var(number.toString());
        }

        @Override
        public String toString() {
            return isBool ? Boolean.toString(truth()) : number.toString();
        }
    }

    /**
     * A guard that held on the way through the running process's body, stated about the
     * state {@link StructuralInvariants#TRANSITION_STATE} names.
     *
     * @param reads     the variables it reads, any write to which invalidates it
     * @param processes the processes whose state it reads
     * @param timerOf   the process whose timer it compares against, or null
     * @param timed     whether it says a timeout has been reached
     */
    record Fact(Term term, Set<String> reads, Set<String> processes, String timerOf,
                boolean timed) {
    }

    /**
     * A {@code set state} met on the path, kept until the path is known to be possible.
     *
     * @param before    the target's state just before it
     * @param executing the process whose body the statement is in, and its state
     */
    record PendingEntry(String process, String state, PState before,
                        String executing, PState executingState, List<Fact> facts) {
    }

    /** A process's state at some point: known, unknown, or as it was when the cycle began. */
    record PState(String process, String known, boolean initial) {
        static final boolean INITIAL = true;
    }

    // ------------------------------------------------------------------ state

    /**
     * True for a walk starting somewhere nothing is known about: the body of a loop, which
     * starts mid-cycle. Then "as the cycle began" means unknown.
     */
    private final boolean opaqueStart;

    /** Variables known to hold a constant. */
    private final Map<String, Const> values;
    /** Variables written with something not known. Neither here nor above: as at the start. */
    private final Set<String> unknownValues;
    /** Processes whose state is known. */
    private final Map<String, String> pstates;
    /** Processes whose state is not known. Neither here nor above: as at the start. */
    private final Set<String> unknownPstates;
    /** The state each process was in when the cycle began, once its body is reached. */
    private final Map<String, String> initialPstates;
    /**
     * The state each process was in at the last boundary passed - the start of the cycle,
     * or the state past a loop. Absent means as at the start.
     */
    private final Map<String, String> boundaryPstates;
    private final Set<String> boundaryUnknown;
    /** What the induction hypothesis says the variables held when the cycle began. */
    private final Map<String, Const> startValues;

    private String executing;
    private final List<Fact> facts;
    private final List<PendingEntry> pending;
    private boolean impossible;

    AbstractCycle(boolean opaqueStart) {
        this.opaqueStart = opaqueStart;
        this.values = new LinkedHashMap<>();
        this.unknownValues = new LinkedHashSet<>();
        this.pstates = new LinkedHashMap<>();
        this.unknownPstates = new LinkedHashSet<>();
        this.initialPstates = new LinkedHashMap<>();
        this.boundaryPstates = new LinkedHashMap<>();
        this.boundaryUnknown = new LinkedHashSet<>();
        this.startValues = new LinkedHashMap<>();
        this.facts = new ArrayList<>();
        this.pending = new ArrayList<>();
    }

    private AbstractCycle(AbstractCycle other) {
        this.opaqueStart = other.opaqueStart;
        this.values = new LinkedHashMap<>(other.values);
        this.unknownValues = new LinkedHashSet<>(other.unknownValues);
        this.pstates = new LinkedHashMap<>(other.pstates);
        this.unknownPstates = new LinkedHashSet<>(other.unknownPstates);
        this.initialPstates = new LinkedHashMap<>(other.initialPstates);
        this.boundaryPstates = new LinkedHashMap<>(other.boundaryPstates);
        this.boundaryUnknown = new LinkedHashSet<>(other.boundaryUnknown);
        this.startValues = new LinkedHashMap<>(other.startValues);
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

    List<PendingEntry> pending() {
        return pending;
    }

    String executing() {
        return executing;
    }

    // ------------------------------------------------------------------ reading

    /** A variable's value now, or null when it is not known. */
    Const valueOf(String variable, Map<String, Const> constants) {
        if (unknownValues.contains(variable)) {
            return null;
        }
        Const known = values.get(variable);
        if (known != null) {
            return known;
        }
        // Untouched since the cycle began. A constant has its declared value at every
        // boundary - the global invariant says so - and anything else has whatever the
        // induction hypothesis says it had.
        Const constant = constants.get(variable);
        if (constant != null) {
            return constant;
        }
        return opaqueStart ? null : startValues.get(variable);
    }

    /** A process's state now, or null when it is not known. */
    String pstateOf(String process) {
        return resolve(stateNow(process));
    }

    /** A process's state at the last boundary passed, or null when it is not known. */
    String boundaryPstateOf(String process) {
        if (boundaryUnknown.contains(process)) {
            return null;
        }
        String known = boundaryPstates.get(process);
        return known != null ? known : resolve(new PState(process, null, PState.INITIAL));
    }

    PState stateNow(String process) {
        if (unknownPstates.contains(process)) {
            return new PState(process, null, false);
        }
        String known = pstates.get(process);
        return known != null
                ? new PState(process, known, false)
                : new PState(process, null, PState.INITIAL);
    }

    /** What a recorded process state turned out to be, now more of the path is known. */
    String resolve(PState state) {
        if (state.known() != null) {
            return state.known();
        }
        if (!state.initial() || opaqueStart) {
            return null;
        }
        return initialPstates.get(state.process());
    }

    // ------------------------------------------------------------------ effects

    /** Sets a variable, as the start of a cycle would have it. */
    void setValue(String variable, Const value) {
        if (value == null) {
            values.remove(variable);
            unknownValues.add(variable);
        } else {
            unknownValues.remove(variable);
            values.put(variable, value);
        }
        facts.removeIf(fact -> fact.reads().contains(variable));
    }

    void setPstate(String process, String state) {
        if (state == null) {
            pstates.remove(process);
            unknownPstates.add(process);
        } else {
            unknownPstates.remove(process);
            pstates.put(process, state);
        }
        // Moving a process also restarts its timer.
        facts.removeIf(fact -> fact.processes().contains(process) || process.equals(fact.timerOf()));
    }

    void resetTimer(String process) {
        facts.removeIf(fact -> process.equals(fact.timerOf()));
    }

    /** Every timer has moved on: an environment step has happened. */
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
    boolean enter(String process, String state, Predicate<String> onLearnt) {
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
        return onLearnt.test(state);
    }

    /**
     * The hypothesis says {@code variable} held {@code value} when the cycle began.
     *
     * @return false when it already said something else
     */
    boolean assumeAtStart(String variable, Const value) {
        Const previous = startValues.putIfAbsent(variable, value);
        return previous == null || previous.equals(value);
    }

    /** The state reached is a boundary, so what comes next measures from it. */
    void boundaryPassed(Iterable<String> processes) {
        for (String process : processes) {
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

    void setExecuting(String process) {
        executing = process;
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
    Const evaluate(IrExpr expr, Map<String, Const> constants) {
        if (expr instanceof IrExpr.Literal literal) {
            return switch (literal.getKind()) {
                case BOOL -> Const.of(literal.getText().equals("true"));
                case INTEGER -> Const.of(BigInteger.valueOf(
                        IsabelleRenderer.parseInteger(literal.getText())));
                case TIME -> Const.of(BigInteger.valueOf(
                        IsabelleRenderer.parseTimeMillis(literal.getText())));
                case FLOAT -> null;
            };
        }
        if (expr instanceof IrExpr.VarRef ref) {
            return ref.getAccesses().isEmpty() ? valueOf(ref.getName(), constants) : null;
        }
        if (expr instanceof IrExpr.Cast cast) {
            return convert(evaluate(cast.getOperand(), constants),
                    Terms.sortOf(cast.getPreType()), Terms.sortOf(cast.getTargetType()));
        }
        if (expr instanceof IrExpr.Unary unary) {
            Const operand = evaluate(unary.getOperand(), constants);
            if (operand == null) {
                return null;
            }
            return switch (unary.getOp()) {
                case NOT -> Const.of(!operand.truth());
                case PLUS -> operand;
                case NEG -> Terms.sortOf(unary.getResultType()) == Terms.Sort.INT
                        ? Const.of(operand.number().negate()) : null;
                case BIT_NOT -> null;
            };
        }
        if (expr instanceof IrExpr.Binary binary) {
            return evaluateBinary(binary, constants);
        }
        if (expr instanceof IrExpr.CheckState check) {
            String state = pstateOf(check.getProcess());
            if (state == null) {
                return null;
            }
            boolean stopped = state.equals("stop");
            boolean failed = state.equals("error");
            return Const.of(switch (check.getStatus()) {
                case STOP -> stopped;
                case ERROR -> failed;
                case INACTIVE -> stopped || failed;
                case ACTIVE -> !stopped && !failed;
            });
        }
        // Reads pinned to an earlier state, writes, calls: not followed.
        return null;
    }

    private Const evaluateBinary(IrExpr.Binary binary, Map<String, Const> constants) {
        Const left = evaluate(binary.getLeft(), constants);
        Const right = evaluate(binary.getRight(), constants);
        switch (binary.getOp()) {
            case AND:
                if ((left != null && !left.truth()) || (right != null && !right.truth())) {
                    return Const.of(false);
                }
                return left == null || right == null ? null : Const.of(true);
            case OR:
                if ((left != null && left.truth()) || (right != null && right.truth())) {
                    return Const.of(true);
                }
                return left == null || right == null ? null : Const.of(false);
            default:
                break;
        }
        if (left == null || right == null) {
            return null;
        }
        BigInteger a = left.number();
        BigInteger b = right.number();
        return switch (binary.getOp()) {
            case ADD -> Const.of(a.add(b));
            case MUL -> Const.of(a.multiply(b));
            case SUB -> {
                BigInteger difference = a.subtract(b);
                yield Const.of(Terms.sortOf(binary.getResultType()) == Terms.Sort.NAT
                        && difference.signum() < 0 ? BigInteger.ZERO : difference);
            }
            case LT -> Const.of(a.compareTo(b) < 0);
            case LE -> Const.of(a.compareTo(b) <= 0);
            case GT -> Const.of(a.compareTo(b) > 0);
            case GE -> Const.of(a.compareTo(b) >= 0);
            case EQ -> Const.of(a.equals(b));
            case NE -> Const.of(!a.equals(b));
            // Division rounds differently in HOL and C, and the bitwise operators are not
            // modelled; neither matters for the constants invariants are made of.
            default -> null;
        };
    }

    /** The conversions {@link Terms#cast} makes, on values. */
    static Const convert(Const value, Terms.Sort from, Terms.Sort to) {
        if (value == null || to == Terms.Sort.REAL || from == Terms.Sort.REAL) {
            return null;
        }
        if (from == to) {
            return value;
        }
        return switch (to) {
            case BOOL -> Const.of(value.truth());
            case INT -> Const.of(value.number());
            case NAT -> Const.of(value.number().signum() < 0 ? BigInteger.ZERO : value.number());
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
