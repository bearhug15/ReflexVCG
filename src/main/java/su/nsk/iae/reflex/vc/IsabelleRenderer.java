package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.term.TermRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Renders verification conditions as Isabelle/HOL, against the semantics in
 * ReflexBase.thy.
 *
 * <p>This is the only place in the pipeline that knows Isabelle syntax. Everything
 * upstream stays symbolic, so retargeting another prover means another renderer rather
 * than another pipeline.
 *
 * <p>Values live in the {@code val} datatype and are read with {@code getVarVal}, then
 * projected into the HOL type the Reflex type maps onto - signed integers to {@code int},
 * unsigned integers and {@code time} to {@code nat}, {@code bool} to {@code bool}, and
 * the floating types to {@code real}. Writing goes the other way, through the matching
 * value constructor.
 */
public final class IsabelleRenderer {

    public static final String FINAL_STATE = "st_final";

    /** Conditions are numbered within a condition so their labels stay unique. */
    private int conditionCounter;

    private final TermRenderer terms = new TermRenderer();

    // ------------------------------------------------------------------ theory

    public String renderTheory(String name, List<String> imports, String body) {
        StringJoiner theory = new StringJoiner("\n");
        theory.add("theory " + name);
        theory.add("\timports " + String.join(" ", imports));
        theory.add("begin");
        theory.add(body);
        theory.add("end");
        return theory.toString();
    }

    /** Renders a condition as a lemma with one assumption per statement. */
    public String renderLemma(VerificationCondition condition) {
        conditionCounter = 0;
        List<String> assumptions = new ArrayList<>();
        for (VcStatement statement : condition.getStatements()) {
            assumptions.add(renderStatement(statement));
        }

        StringBuilder lemma = new StringBuilder();
        if (condition.getNote() != null) {
            // Says which annotation the condition came from, which is otherwise only
            // recoverable by reading the formula.
            lemma.append("(* ").append(condition.getNote()).append(" *)\n");
        }
        lemma.append("lemma\nassumes ");
        lemma.append(String.join("\n\tand ", assumptions));
        lemma.append("\nshows \"").append(renderConclusion(condition)).append("\"");
        return lemma.toString();
    }

    /**
     * What the condition shows: an annotation's formula where it has one, and otherwise
     * the invariant at the final state.
     */
    private String renderConclusion(VerificationCondition condition) {
        return condition.getConclusion() == null
                ? invariant(condition.getFinalState())
                : terms.render(condition.getConclusion());
    }

    // ------------------------------------------------------------------ statements

    /** Renders one assumption, label included. */
    public String renderStatement(VcStatement statement) {
        if (statement instanceof VcStatement.Invariant s) {
            return "base_inv:\"" + invariant(s.state()) + "\"";
        }
        if (statement instanceof VcStatement.Assumption s) {
            return s.label() + ":\"" + terms.render(s.formula()) + "\"";
        }
        if (statement instanceof VcStatement.OpaqueState s) {
            // How many iterations the loop ran is not known, so the state it left behind is
            // constrained only by being a boundary reachable from where it started.
            return s.target() + ":\"toEnvP " + s.target() + " \\<and> substate "
                    + s.source() + " " + s.target() + "\"";
        }
        if (statement instanceof VcStatement.EmptyState s) {
            return s.target() + ":\"" + s.target() + "=emptyState\"";
        }
        if (statement instanceof VcStatement.ProcessInState s) {
            return s.state() + "_state:\"getPstate " + s.state() + " " + quote(s.process())
                    + "=" + quote(s.pstate()) + "\"";
        }
        if (statement instanceof VcStatement.Condition s) {
            String label = s.state() + "_condition_" + conditionCounter++;
            return label + ":\"" + renderExpression(s.expr(), s.state()) + "\"";
        }
        if (statement instanceof VcStatement.Assign s) {
            return s.target() + ":\"" + s.target() + "=" + renderAssignment(s) + "\"";
        }
        if (statement instanceof VcStatement.InputChoice s) {
            // The free variable is named after the input, and a lemma leaves it
            // universally quantified: the condition has to hold for any value.
            return s.target() + ":\"" + s.target() + "=(setVarVal " + s.source() + " "
                    + quote(s.variable()) + " [] " + wrap(s.variable(), s.type()) + ")\"";
        }
        if (statement instanceof VcStatement.SetProcessState s) {
            return s.target() + ":\"" + s.target() + "=setPstate " + s.source() + " "
                    + quote(s.process()) + " " + quote(s.pstate()) + "\"";
        }
        if (statement instanceof VcStatement.ResetTimer s) {
            return s.target() + ":\"" + s.target() + "=reset " + s.source() + " "
                    + quote(s.process()) + "\"";
        }
        if (statement instanceof VcStatement.ToEnv s) {
            return s.target() + ":\"" + s.target() + "=toEnv " + s.source() + "\"";
        }
        if (statement instanceof VcStatement.Final s) {
            return s.target() + ":\"" + s.target() + "=" + s.source() + "\"";
        }
        if (statement instanceof VcStatement.TimeoutCheck s) {
            String comparison = s.exceeded() ? "\\<ge>" : "<";
            String label = s.state() + "_timeout_" + conditionCounter++;
            return label + ":\"(ltime " + s.state() + " " + quote(s.process()) + " "
                    + comparison + " " + renderDuration(s.duration()) + ")\"";
        }
        throw new IllegalStateException("Unhandled statement: " + statement.getClass().getSimpleName());
    }

    private String renderAssignment(VcStatement.Assign s) {
        IrType type = s.variable().getResultType();
        String value = wrap(renderExpression(s.value(), s.source()), type);
        return "(setVarVal " + s.source() + " " + quote(s.variable().getName()) + " "
                + renderAccessPath(s.variable(), s.source()) + " " + value + ")";
    }

    // ------------------------------------------------------------------ expressions

    /** Renders an expression as an Isabelle term evaluated in {@code state}. */
    public String renderExpression(IrExpr expr, String state) {
        if (expr instanceof IrExpr.Literal literal) {
            return renderLiteral(literal);
        }
        if (expr instanceof IrExpr.VarRef ref) {
            return project("(getVarVal " + state + " " + quote(ref.getName()) + " "
                    + renderAccessPath(ref, state) + ")", ref.getResultType());
        }
        if (expr instanceof IrExpr.At at) {
            // A read pinned to an earlier state, because a write inside the expression
            // came between. Path enumeration has already named it.
            if (at.getState() == null) {
                throw new IllegalStateException(
                        "unresolved pinned read: " + at + "; path enumeration should have named it");
            }
            return renderExpression(at.getOperand(), at.getState());
        }
        if (expr instanceof IrExpr.CheckState check) {
            return renderCheckState(check, state);
        }
        if (expr instanceof IrExpr.Cast cast) {
            return renderCast(cast, state);
        }
        if (expr instanceof IrExpr.Unary unary) {
            return renderUnary(unary, state);
        }
        if (expr instanceof IrExpr.Binary binary) {
            return "(" + renderExpression(binary.getLeft(), state) + " "
                    + binaryOperator(binary.getOp()) + " "
                    + renderExpression(binary.getRight(), state) + ")";
        }
        if (expr instanceof IrExpr.Assign assign) {
            // An assignment used as a value denotes the value stored.
            return renderExpression(assign.getValue(), state);
        }
        if (expr instanceof IrExpr.IncDec incDec) {
            String one = "1";
            String operator = incDec.getOp() == IrExpr.IncDecOp.INCREMENT ? "+" : "-";
            return "(" + renderExpression(incDec.getTarget(), state) + " " + operator + " " + one + ")";
        }
        if (expr instanceof IrExpr.Call call) {
            StringJoiner args = new StringJoiner(" ");
            call.getArguments().forEach(a -> args.add(renderExpression(a, state)));
            return "(" + call.getFunction() + " " + args + ")";
        }
        throw new IllegalStateException("Cannot render expression: " + expr.getClass().getSimpleName());
    }

    private String renderCheckState(IrExpr.CheckState check, String state) {
        String current = "getPstate " + state + " " + quote(check.getProcess());
        return switch (check.getStatus()) {
            case STOP -> "(" + current + " = " + quote("stop") + ")";
            case ERROR -> "(" + current + " = " + quote("error") + ")";
            // "inactive" is stopped or failed; "active" is neither.
            case INACTIVE -> "(" + current + " = " + quote("stop") + " \\<or> "
                    + current + " = " + quote("error") + ")";
            case ACTIVE -> "(" + current + " \\<noteq> " + quote("stop") + " \\<and> "
                    + current + " \\<noteq> " + quote("error") + ")";
        };
    }

    private String renderUnary(IrExpr.Unary unary, String state) {
        String operand = renderExpression(unary.getOperand(), state);
        return switch (unary.getOp()) {
            case NOT -> "(\\<not> " + operand + ")";
            case PLUS -> operand;
            case NEG -> "(- " + operand + ")";
            // Bitwise complement within the type's width: for unsigned types that is
            // max - x, and for signed two's complement it is -1 - x.
            case BIT_NOT -> "(" + complementBase(unary.getOperand().getResultType()) + " - " + operand + ")";
        };
    }

    private String renderCast(IrExpr.Cast cast, String state) {
        String operand = renderExpression(cast.getOperand(), state);
        Sort from = sortOf(cast.getPreType());
        Sort to = sortOf(cast.getTargetType());
        if (from == to) {
            // Both Reflex types map onto the same HOL type. Narrowing between widths is
            // not modelled here, exactly as the previous generator did not model it.
            return operand;
        }
        return switch (to) {
            case BOOL -> "(" + operand + " \\<noteq> 0)";
            case INT -> from == Sort.BOOL
                    ? "(if " + operand + " then 1 else 0)"
                    : from == Sort.NAT ? "(int " + operand + ")" : "(\\<lfloor>" + operand + "\\<rfloor>)";
            case NAT -> from == Sort.BOOL
                    ? "(if " + operand + " then 1 else 0)"
                    : from == Sort.INT ? "(nat " + operand + ")"
                    : "(nat \\<lfloor>" + operand + "\\<rfloor>)";
            case REAL -> from == Sort.BOOL
                    ? "(if " + operand + " then 1 else 0)"
                    : from == Sort.INT ? "(real_of_int " + operand + ")" : "(real " + operand + ")";
        };
    }

    private String renderLiteral(IrExpr.Literal literal) {
        return switch (literal.getKind()) {
            case BOOL -> literal.getText().equals("true") ? "True" : "False";
            case INTEGER -> Long.toString(parseInteger(literal.getText()));
            case FLOAT -> literal.getText();
            // A time literal denotes a count of milliseconds.
            case TIME -> Long.toString(parseTimeMillis(literal.getText()));
        };
    }

    private String renderDuration(TimeRef duration) {
        return switch (duration.getKind()) {
            case TIME_LITERAL -> Long.toString(parseTimeMillis(duration.getText()));
            case INTEGER -> Long.toString(parseInteger(duration.getText()));
            // A named duration is a variable or constant, read as a count.
            case NAME -> "(theNat (getVarVal " + FINAL_STATE + " " + quote(duration.getText()) + " []))";
        };
    }

    /** The {@code access list} argument of getVarVal / setVarVal. */
    private String renderAccessPath(IrExpr.VarRef ref, String state) {
        if (ref.getAccesses().isEmpty()) {
            return "[]";
        }
        StringJoiner path = new StringJoiner(", ", "[", "]");
        for (IrExpr.Access access : ref.getAccesses()) {
            if (access instanceof IrExpr.FieldAccess field) {
                path.add("AccessField " + quote(field.getField()));
            } else {
                IrExpr index = ((IrExpr.IndexAccess) access).getIndex();
                path.add("AccessIndex " + asNat(renderExpression(index, state), index.getResultType()));
            }
        }
        return path.toString();
    }

    /** An array index must be a nat regardless of the type the index expression has. */
    private String asNat(String rendered, IrType type) {
        return switch (sortOf(type)) {
            case NAT -> "(" + rendered + ")";
            case INT -> "(nat " + rendered + ")";
            case BOOL -> "(if " + rendered + " then 1 else 0)";
            case REAL -> "(nat \\<lfloor>" + rendered + "\\<rfloor>)";
        };
    }

    // ------------------------------------------------------------------ value sorts

    /** The HOL type a Reflex type is represented by. */
    public enum Sort { BOOL, INT, NAT, REAL }

    static Sort sortOf(IrType type) {
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
        // Enums are integers; anything else has no scalar reading, so int is the default.
        return Sort.INT;
    }

    /** Projection out of val for a Reflex type. */
    static String projection(IrType type) {
        return switch (sortOf(type)) {
            case BOOL -> "theBool";
            case INT -> "theInt";
            case NAT -> "theNat";
            case REAL -> "theReal";
        };
    }

    /** Value constructor into val for a Reflex type. */
    static String constructor(IrType type) {
        return switch (sortOf(type)) {
            case BOOL -> "ValBool";
            case INT -> "ValInt";
            case NAT -> "ValNat";
            case REAL -> "ValReal";
        };
    }

    private static String project(String term, IrType type) {
        return "(" + projection(type) + " " + term + ")";
    }

    private static String wrap(String term, IrType type) {
        return "(" + constructor(type) + " " + term + ")";
    }

    private static String complementBase(IrType type) {
        if (type instanceof IrType.Builtin builtin) {
            return switch (builtin.kind()) {
                case UINT8 -> "255";
                case UINT16 -> "65535";
                case UINT32 -> "4294967295";
                case UINT64, TIME -> "18446744073709551615";
                default -> "-1";
            };
        }
        return "-1";
    }

    private static String binaryOperator(IrExpr.BinaryOp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "div";
            case MOD -> "mod";
            // Isabelle's bitwise operators; using them needs Bit_Operations in scope.
            case SHL -> "<<";
            case SHR -> ">>";
            case BIT_AND -> "AND";
            case BIT_OR -> "OR";
            case BIT_XOR -> "XOR";
            case LT -> "<";
            case GT -> ">";
            case LE -> "\\<le>";
            case GE -> "\\<ge>";
            case EQ -> "=";
            case NE -> "\\<noteq>";
            case AND -> "\\<and>";
            case OR -> "\\<or>";
        };
    }

    // ------------------------------------------------------------------ literals

    /** Parses a Reflex integer literal: decimal, hex or octal, with optional suffixes. */
    public static long parseInteger(String text) {
        String value = text.trim();
        boolean negative = value.startsWith("-");
        if (negative || value.startsWith("+")) {
            value = value.substring(1);
        }
        while (!value.isEmpty() && "lLuU".indexOf(value.charAt(value.length() - 1)) >= 0) {
            value = value.substring(0, value.length() - 1);
        }
        long parsed;
        if (value.startsWith("0x") || value.startsWith("0X")) {
            parsed = Long.parseLong(value.substring(2), 16);
        } else if (value.length() > 1 && value.startsWith("0")) {
            parsed = Long.parseLong(value.substring(1), 8);
        } else {
            parsed = value.isEmpty() ? 0 : Long.parseLong(value);
        }
        return negative ? -parsed : parsed;
    }

    /**
     * Converts a time literal such as {@code 0t1h30m} to milliseconds. Milliseconds are
     * matched before minutes, since 'ms' also starts with 'm'.
     */
    public static long parseTimeMillis(String text) {
        String value = text.trim();
        if (value.length() < 2 || (value.charAt(0) != '0')
                || (value.charAt(1) != 't' && value.charAt(1) != 'T')) {
            return parseInteger(value);
        }
        value = value.substring(2);

        long total = 0;
        int i = 0;
        while (i < value.length()) {
            int start = i;
            while (i < value.length() && Character.isDigit(value.charAt(i))) {
                i++;
            }
            if (start == i) {
                break;
            }
            long amount = Long.parseLong(value.substring(start, i));
            String unit = value.substring(i).toLowerCase();
            if (unit.startsWith("ms")) {
                total += amount;
                i += 2;
            } else if (unit.startsWith("d")) {
                total += amount * 24 * 60 * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("h")) {
                total += amount * 60 * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("m")) {
                total += amount * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("s")) {
                total += amount * 1000L;
                i += 1;
            } else {
                break;
            }
        }
        return total;
    }

    static String quote(String name) {
        return "''" + name + "''";
    }

    private static String invariant(String state) {
        return "inv(" + state + ")";
    }
}
