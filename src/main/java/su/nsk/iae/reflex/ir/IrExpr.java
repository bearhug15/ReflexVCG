package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Expressions of the Reflex IR.
 *
 * <p>Fields are mutable because the preprocessing passes rewrite them in place: name
 * mangling replaces {@link VarRef#name}, and cast insertion wraps operands in
 * {@link Cast} nodes and fills in {@link #resultType} - {@code restype} in the
 * pseudocode of Preprocessing.tex.
 */
public abstract class IrExpr extends IrNode {

    private IrType resultType;

    /** Type of the value this expression produces; null until the cast pass runs. */
    public IrType getResultType() {
        return resultType;
    }

    public void setResultType(IrType resultType) {
        this.resultType = resultType;
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // ------------------------------------------------------------------ operators

    public enum BinaryOp {
        ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%"),
        SHL("<<"), SHR(">>"),
        LT("<"), GT(">"), LE("<="), GE(">="),
        EQ("=="), NE("!="),
        BIT_AND("&"), BIT_XOR("^"), BIT_OR("|"),
        AND("&&"), OR("||");

        private final String symbol;

        BinaryOp(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        /** True for the operators whose result is bool regardless of operand types. */
        public boolean isBoolean() {
            return switch (this) {
                case LT, GT, LE, GE, EQ, NE, AND, OR -> true;
                default -> false;
            };
        }

        public static BinaryOp fromSymbol(String symbol) {
            for (BinaryOp op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown binary operator: " + symbol);
        }
    }

    public enum UnaryOp {
        PLUS("+"), NEG("-"), BIT_NOT("~"), NOT("!");

        private final String symbol;

        UnaryOp(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        public static UnaryOp fromSymbol(String symbol) {
            for (UnaryOp op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown unary operator: " + symbol);
        }
    }

    public enum AssignOp {
        ASSIGN("="),
        ADD("+="), SUB("-="), MUL("*="), DIV("/="), MOD("%="),
        SHL("<<="), SHR(">>="),
        BIT_AND("&="), BIT_XOR("^="), BIT_OR("|=");

        private final String symbol;

        AssignOp(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        /** The arithmetic operator behind a compound assignment, null for plain '='. */
        public BinaryOp underlying() {
            return switch (this) {
                case ASSIGN -> null;
                case ADD -> BinaryOp.ADD;
                case SUB -> BinaryOp.SUB;
                case MUL -> BinaryOp.MUL;
                case DIV -> BinaryOp.DIV;
                case MOD -> BinaryOp.MOD;
                case SHL -> BinaryOp.SHL;
                case SHR -> BinaryOp.SHR;
                case BIT_AND -> BinaryOp.BIT_AND;
                case BIT_XOR -> BinaryOp.BIT_XOR;
                case BIT_OR -> BinaryOp.BIT_OR;
            };
        }

        public static AssignOp fromSymbol(String symbol) {
            for (AssignOp op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown assignment operator: " + symbol);
        }
    }

    public enum IncDecOp { INCREMENT, DECREMENT }

    public enum ProcessStatus { ACTIVE, INACTIVE, STOP, ERROR }

    // ------------------------------------------------------------------ access paths

    /** One step of an access path: {@code .field} or {@code [index]}. */
    public abstract static sealed class Access permits FieldAccess, IndexAccess {
    }

    public static final class FieldAccess extends Access {
        private String field;

        public FieldAccess(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return "." + field;
        }
    }

    public static final class IndexAccess extends Access {
        private IrExpr index;

        public IndexAccess(IrExpr index) {
            this.index = index;
        }

        public IrExpr getIndex() {
            return index;
        }

        public void setIndex(IrExpr index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    // ------------------------------------------------------------------ expressions

    /**
     * A variable use with its access path - {@code elementAccess} in Preprocessing.tex.
     * Name mangling rewrites {@link #name} and may append a direct-access counter.
     */
    public static final class VarRef extends IrExpr {
        private String name;
        private final List<Access> accesses;

        public VarRef(String name) {
            this(name, new ArrayList<>());
        }

        public VarRef(String name, List<Access> accesses) {
            this.name = name;
            this.accesses = accesses;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Access> getAccesses() {
            return accesses;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarRef(this);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name);
            accesses.forEach(sb::append);
            return sb.toString();
        }
    }

    /** A literal. {@code text} keeps the original spelling; time literals need it. */
    public static final class Literal extends IrExpr {
        public enum Kind { BOOL, INTEGER, FLOAT, TIME }

        private final Kind kind;
        private final String text;

        public Literal(Kind kind, String text) {
            this.kind = kind;
            this.text = text;
        }

        public Kind getKind() {
            return kind;
        }

        public String getText() {
            return text;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final class Binary extends IrExpr {
        private final BinaryOp op;
        private IrExpr left;
        private IrExpr right;

        public Binary(BinaryOp op, IrExpr left, IrExpr right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public BinaryOp getOp() {
            return op;
        }

        public IrExpr getLeft() {
            return left;
        }

        public void setLeft(IrExpr left) {
            this.left = left;
        }

        public IrExpr getRight() {
            return right;
        }

        public void setRight(IrExpr right) {
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinary(this);
        }

        @Override
        public String toString() {
            return "(" + left + " " + op.symbol() + " " + right + ")";
        }
    }

    public static final class Unary extends IrExpr {
        private final UnaryOp op;
        private IrExpr operand;

        public Unary(UnaryOp op, IrExpr operand) {
            this.op = op;
            this.operand = operand;
        }

        public UnaryOp getOp() {
            return op;
        }

        public IrExpr getOperand() {
            return operand;
        }

        public void setOperand(IrExpr operand) {
            this.operand = operand;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnary(this);
        }

        @Override
        public String toString() {
            return "(" + op.symbol() + operand + ")";
        }
    }

    /**
     * A conversion. {@code preType} records the operand's type before the conversion,
     * which the Isabelle backend needs to emit the right coercion.
     */
    public static final class Cast extends IrExpr {
        private final IrType targetType;
        private IrExpr operand;
        private IrType preType;
        private final boolean implicit;

        public Cast(IrType targetType, IrExpr operand, IrType preType, boolean implicit) {
            this.targetType = targetType;
            this.operand = operand;
            this.preType = preType;
            this.implicit = implicit;
            setResultType(targetType);
        }

        public IrType getTargetType() {
            return targetType;
        }

        public IrExpr getOperand() {
            return operand;
        }

        public void setOperand(IrExpr operand) {
            this.operand = operand;
        }

        public IrType getPreType() {
            return preType;
        }

        public void setPreType(IrType preType) {
            this.preType = preType;
        }

        /** True when the cast pass inserted this node rather than the programmer. */
        public boolean isImplicit() {
            return implicit;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCast(this);
        }

        @Override
        public String toString() {
            return "(" + targetType + ")" + operand;
        }
    }

    public static final class Assign extends IrExpr {
        private final AssignOp op;
        private VarRef target;
        private IrExpr value;

        public Assign(AssignOp op, VarRef target, IrExpr value) {
            this.op = op;
            this.target = target;
            this.value = value;
        }

        public AssignOp getOp() {
            return op;
        }

        public VarRef getTarget() {
            return target;
        }

        public void setTarget(VarRef target) {
            this.target = target;
        }

        public IrExpr getValue() {
            return value;
        }

        public void setValue(IrExpr value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssign(this);
        }

        @Override
        public String toString() {
            return target + " " + op.symbol() + " " + value;
        }
    }

    /** {@code ++x} / {@code x++} and their decrementing counterparts. */
    public static final class IncDec extends IrExpr {
        private final IncDecOp op;
        private final boolean prefix;
        private VarRef target;

        public IncDec(IncDecOp op, boolean prefix, VarRef target) {
            this.op = op;
            this.prefix = prefix;
            this.target = target;
        }

        public IncDecOp getOp() {
            return op;
        }

        public boolean isPrefix() {
            return prefix;
        }

        public VarRef getTarget() {
            return target;
        }

        public void setTarget(VarRef target) {
            this.target = target;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIncDec(this);
        }

        @Override
        public String toString() {
            String symbol = op == IncDecOp.INCREMENT ? "++" : "--";
            return prefix ? symbol + target : target + symbol;
        }
    }

    public static final class Call extends IrExpr {
        private final String function;
        private final List<IrExpr> arguments;

        public Call(String function, List<IrExpr> arguments) {
            this.function = function;
            this.arguments = arguments;
        }

        public String getFunction() {
            return function;
        }

        public List<IrExpr> getArguments() {
            return arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCall(this);
        }

        @Override
        public String toString() {
            return function + arguments;
        }
    }

    /** {@code process P in state <qualifier>}. */
    public static final class CheckState extends IrExpr {
        private String process;
        private final ProcessStatus status;

        public CheckState(String process, ProcessStatus status) {
            this.process = process;
            this.status = status;
            setResultType(IrType.BOOL);
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        public ProcessStatus getStatus() {
            return status;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCheckState(this);
        }

        @Override
        public String toString() {
            return "process " + process + " in state " + status.name().toLowerCase();
        }
    }

    /**
     * A subexpression read some number of states before the one its statement is stated
     * in - the pin an expression with side effects needs.
     *
     * <p>Reflex takes its expression semantics from C, where a write may sit anywhere
     * inside an expression: {@code total = count++ + count} both reads and writes
     * {@code count}. Each write is a state of its own, so the reads around it do not all
     * happen in the same state, and an expression can no longer be rendered against one.
     *
     * <p>A read carries no state of its own until something fixes it - it floats, and is
     * fixed at the state current when its value is needed. {@code stepsBack} counts from
     * the state the statement holding it is stated in: 0 is that state, 1 the state before
     * the last write, and so on. Only expressions that actually write ever carry these, so
     * an expression without side effects is untouched.
     *
     * <p>Produced by {@code cfg/ExprLowering} and read by the renderer. It never appears
     * in the IR the preprocessing passes see.
     */
    public static final class At extends IrExpr {
        private final IrExpr operand;
        private final int stepsBack;
        private final String state;

        /** As the graph carries it: a distance back, the state itself not yet named. */
        public At(IrExpr operand, int stepsBack) {
            if (stepsBack < 0) {
                throw new IllegalArgumentException("stepsBack must not be negative: " + stepsBack);
            }
            this.operand = operand;
            this.stepsBack = stepsBack;
            this.state = null;
            setResultType(operand.getResultType());
        }

        /**
         * As a condition carries it, once path enumeration has named the states. A graph
         * node is shared by every path through it, and the paths name their states
         * differently, so resolving produces a new node rather than filling this one in.
         */
        public At(IrExpr operand, String state) {
            this.operand = operand;
            this.stepsBack = 0;
            this.state = state;
            setResultType(operand.getResultType());
        }

        public IrExpr getOperand() {
            return operand;
        }

        /** How many states back from the one the statement is stated in. */
        public int getStepsBack() {
            return stepsBack;
        }

        /** The state this reads in, or null while the distance has not been resolved. */
        public String getState() {
            return state;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAt(this);
        }

        @Override
        public String toString() {
            return operand + "@" + (state != null ? state : "-" + stepsBack);
        }
    }

    /**
     * An aggregate initialiser, {@code {1, 2}} or {@code {.y = 5}}. Partial by
     * definition: members with no element keep their default value.
     */
    public static final class Aggregate extends IrExpr {
        /** One initialiser element, with an optional designator naming its target. */
        public static final class Element {
            private final Access designator;
            private IrExpr value;

            public Element(Access designator, IrExpr value) {
                this.designator = designator;
                this.value = value;
            }

            /** {@code .field} or {@code [index]}, or null for a positional element. */
            public Access getDesignator() {
                return designator;
            }

            public IrExpr getValue() {
                return value;
            }

            public void setValue(IrExpr value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return designator == null ? String.valueOf(value) : designator + " = " + value;
            }
        }

        private final List<Element> elements;

        public Aggregate(List<Element> elements) {
            this.elements = elements;
        }

        public List<Element> getElements() {
            return elements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAggregate(this);
        }

        @Override
        public String toString() {
            return "{" + elements.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("") + "}";
        }
    }

    // ------------------------------------------------------------------ visitor

    public interface Visitor<R> {
        R visitVarRef(VarRef expr);

        R visitLiteral(Literal expr);

        R visitBinary(Binary expr);

        R visitUnary(Unary expr);

        R visitCast(Cast expr);

        R visitAssign(Assign expr);

        R visitIncDec(IncDec expr);

        R visitCall(Call expr);

        R visitCheckState(CheckState expr);

        R visitAggregate(Aggregate expr);

        R visitAt(At expr);
    }
}
