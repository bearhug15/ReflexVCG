package su.nsk.iae.reflex.term;

import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * The term constructors the annotation translation is written against - the
 * {@code createXxxTerm} family of the specification.
 *
 * <p>Everything about how the state model is spelled in Isabelle lives here and in
 * {@link TermRenderer}: reading a variable, comparing a process's state, asking whether a
 * state is a boundary. Nothing above this layer writes Isabelle syntax.
 */
public final class Terms {

    private Terms() {
    }

    public static final Term TRUE = new Term.Var("True");
    public static final Term FALSE = new Term.Var("False");

    // ------------------------------------------------------------------ states

    /** A fresh bound state variable - {@code bState()} in the specification. */
    public static Term boundState(int index) {
        return new Term.Var("s" + index);
    }

    public static Term emptyState() {
        return new Term.Var("emptyState");
    }

    /** {@code toEnvP s}: s is a boundary between cycles. */
    public static Term toEnvP(Term state) {
        return new Term.App("toEnvP", List.of(state));
    }

    /** {@code substate a b}: a is reachable at or before b. */
    public static Term substate(Term earlier, Term later) {
        return new Term.App("substate", List.of(earlier, later));
    }

    /** {@code predEnv s}: the previous boundary. */
    public static Term predEnv(Term state) {
        return new Term.App("predEnv", List.of(state));
    }

    /** {@code toEnv s}: the state after yielding to the environment. */
    public static Term toEnv(Term state) {
        return new Term.App("toEnv", List.of(state));
    }

    /** {@code toEnvNum a b}: how many boundaries lie between a and b. */
    public static Term toEnvNum(Term from, Term to) {
        return new Term.App("toEnvNum", List.of(from, to));
    }

    // ------------------------------------------------------------------ processes

    /** {@code getPstate s ''p'' = ''q''}. */
    public static Term pstateCompare(Term state, String process, String pstate) {
        return new Term.Infix("=", pstateOf(state, process), new Term.Quoted(pstate));
    }

    public static Term pstateOf(Term state, String process) {
        return new Term.App("getPstate", List.of(state, new Term.Quoted(process)));
    }

    /**
     * Whether a process is active, stopped or failed. Active means neither of the
     * inactive states, which is how the program's own checks read it.
     */
    public static Term processActivity(Term state, String process, String activity) {
        Term current = pstateOf(state, process);
        return switch (activity) {
            case "stop" -> new Term.Infix("=", current, new Term.Quoted("stop"));
            case "error" -> new Term.Infix("=", current, new Term.Quoted("error"));
            case "inactive" -> disjunction(List.of(
                    new Term.Infix("=", current, new Term.Quoted("stop")),
                    new Term.Infix("=", current, new Term.Quoted("error"))));
            case "nonstop" -> new Term.Infix("\\<noteq>", current, new Term.Quoted("stop"));
            case "nonerror" -> new Term.Infix("\\<noteq>", current, new Term.Quoted("error"));
            default -> conjunction(List.of(
                    new Term.Infix("\\<noteq>", current, new Term.Quoted("stop")),
                    new Term.Infix("\\<noteq>", current, new Term.Quoted("error"))));
        };
    }

    /** {@code ltime s ''p''}: how long the process has been in its state. */
    public static Term localTime(Term state, String process) {
        return new Term.App("ltime", List.of(state, new Term.Quoted(process)));
    }

    // ------------------------------------------------------------------ values

    /**
     * Reads a variable and projects it into the HOL type its Reflex type maps onto.
     *
     * @param path the access path, of field names and index terms
     */
    public static Term valueGetter(Term state, IrType type, String name, List<Term> path) {
        Term read = new Term.App("getVarVal",
                List.of(state, new Term.Quoted(name), new Term.ListTerm(path)));
        return new Term.App(projectionOf(type), List.of(read));
    }

    public static Term accessField(String field) {
        return new Term.App("AccessField", List.of(new Term.Quoted(field)));
    }

    public static Term accessIndex(Term index) {
        return new Term.App("AccessIndex", List.of(index));
    }

    /** The projection out of {@code val} for a Reflex type. */
    public static String projectionOf(IrType type) {
        return switch (sortOf(type)) {
            case BOOL -> "theBool";
            case NAT -> "theNat";
            case REAL -> "theReal";
            case INT -> "theInt";
        };
    }

    /** The HOL type a Reflex type is represented by. */
    public enum Sort { BOOL, INT, NAT, REAL }

    public static Sort sortOf(IrType type) {
        if (type == null) {
            return Sort.INT;
        }
        if (type.isBool()) {
            return Sort.BOOL;
        }
        if (type.isFloating() || type == IrType.UNDEFINED_FLOAT) {
            return Sort.REAL;
        }
        if (type instanceof IrType.Builtin builtin) {
            return switch (builtin.kind()) {
                case UINT8, UINT16, UINT32, UINT64, TIME -> Sort.NAT;
                default -> Sort.INT;
            };
        }
        return Sort.INT;
    }

    /** Converts between HOL types where a Reflex conversion crosses sorts. */
    public static Term cast(Term value, IrType from, IrType to) {
        Sort source = sortOf(from);
        Sort target = sortOf(to);
        if (source == target) {
            // Both map onto the same HOL type; narrowing between widths is not modelled.
            return value;
        }
        return switch (target) {
            case BOOL -> new Term.Infix("\\<noteq>", value, new Term.Var("0"));
            case INT -> source == Sort.BOOL
                    ? ifThenElse(value, new Term.Var("1"), new Term.Var("0"))
                    : new Term.App(source == Sort.NAT ? "int" : "floor", List.of(value));
            case NAT -> source == Sort.BOOL
                    ? ifThenElse(value, new Term.Var("1"), new Term.Var("0"))
                    : new Term.App("nat", List.of(
                            source == Sort.INT ? value : new Term.App("floor", List.of(value))));
            case REAL -> source == Sort.BOOL
                    ? ifThenElse(value, new Term.Var("1"), new Term.Var("0"))
                    : new Term.App(source == Sort.INT ? "real_of_int" : "real", List.of(value));
        };
    }

    public static Term ifThenElse(Term condition, Term whenTrue, Term whenFalse) {
        return new Term.App("if", List.of(condition, new Term.Var("then"), whenTrue,
                new Term.Var("else"), whenFalse));
    }

    // ------------------------------------------------------------------ logic

    public static Term conjunction(List<Term> parts) {
        return fold(parts, "\\<and>", TRUE);
    }

    public static Term disjunction(List<Term> parts) {
        return fold(parts, "\\<or>", FALSE);
    }

    public static Term implication(Term from, Term to) {
        if (TRUE.equals(from)) {
            return to;
        }
        return new Term.Infix("\\<longrightarrow>", from, to);
    }

    public static Term equivalence(Term left, Term right) {
        return conjunction(List.of(implication(left, right), implication(right, left)));
    }

    public static Term not(Term operand) {
        return new Term.Prefix("\\<not>", operand);
    }

    public static Term forall(Term variable, Term body) {
        return new Term.Forall(List.of(nameOf(variable)), body);
    }

    public static Term forall(List<Term> variables, Term body) {
        return new Term.Forall(variables.stream().map(Terms::nameOf).toList(), body);
    }

    public static Term exists(Term variable, Term body) {
        return new Term.Exists(List.of(nameOf(variable)), body);
    }

    public static Term exists(List<Term> variables, Term body) {
        return new Term.Exists(variables.stream().map(Terms::nameOf).toList(), body);
    }

    private static String nameOf(Term variable) {
        if (variable instanceof Term.Var var) {
            return var.name();
        }
        throw new IllegalArgumentException("Not a bindable variable: " + variable);
    }

    private static Term fold(List<Term> parts, String operator, Term unit) {
        List<Term> meaningful = new ArrayList<>();
        for (Term part : parts) {
            if (part != null && !unit.equals(part)) {
                meaningful.add(part);
            }
        }
        if (meaningful.isEmpty()) {
            return unit;
        }
        Term result = meaningful.get(0);
        for (int i = 1; i < meaningful.size(); i++) {
            result = new Term.Infix(operator, result, meaningful.get(i));
        }
        return result;
    }
}
