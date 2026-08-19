package su.nsk.iae.reflex.ann;

import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * Expressions of the Reflex-AL annotation language.
 *
 * <p>A separate tree from {@link su.nsk.iae.reflex.ir.IrExpr}: annotations may speak about
 * execution history - what held previously, what holds within some time, what is true at
 * every reachable state - which programs cannot, and they may quantify. What they share is
 * that both are rewritten by preprocessing, so names and types are mutable here too.
 *
 * <p>Annotations go through the same name mangling and typing as the program before they
 * are translated: an annotation naming {@code x} on a state means that state's {@code x},
 * and the translation needs its type to decide conversions.
 */
public abstract class AnnExpr {

    private IrType type;

    /** Type of the value this expression denotes; null until the typing pass runs. */
    public IrType getType() {
        return type;
    }

    public void setType(IrType type) {
        this.type = type;
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

        public boolean isComparison() {
            return switch (this) {
                case LT, GT, LE, GE, EQ, NE -> true;
                default -> false;
            };
        }

        public static BinaryOp fromSymbol(String symbol) {
            for (BinaryOp op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown annotation operator: " + symbol);
        }
    }

    public enum UnaryOp {
        PLUS("+"), NEG("-"), NOT("!"), BIT_NOT("~");

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
            throw new IllegalArgumentException("Unknown annotation operator: " + symbol);
        }
    }

    // ------------------------------------------------------------------ leaves

    public static final class Literal extends AnnExpr {
        public enum Kind { INTEGER, REAL, BOOL, TIME }

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

    /** One step of an access path: a struct field or an array index. */
    public abstract static sealed class Access permits FieldAccess, IndexAccess {
    }

    public static final class FieldAccess extends Access {
        private final String field;

        public FieldAccess(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        @Override
        public String toString() {
            return "." + field;
        }
    }

    public static final class IndexAccess extends Access {
        private AnnExpr index;

        public IndexAccess(AnnExpr index) {
            this.index = index;
        }

        public AnnExpr getIndex() {
            return index;
        }

        public void setIndex(AnnExpr index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    /**
     * A program variable. {@code name} is rewritten by mangling, exactly as in the
     * program; annotations may also write the qualified form themselves, since the
     * annotation grammar spells qualification with the same {@code #}.
     */
    public static final class VarRef extends AnnExpr {
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

        /** True when the annotation wrote the name already qualified. */
        public boolean isQualified() {
            return name.contains("#");
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

    // ------------------------------------------------------------------ operators

    public static final class Binary extends AnnExpr {
        private final BinaryOp op;
        private AnnExpr left;
        private AnnExpr right;

        public Binary(BinaryOp op, AnnExpr left, AnnExpr right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public BinaryOp getOp() {
            return op;
        }

        public AnnExpr getLeft() {
            return left;
        }

        public void setLeft(AnnExpr left) {
            this.left = left;
        }

        public AnnExpr getRight() {
            return right;
        }

        public void setRight(AnnExpr right) {
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

    public static final class Unary extends AnnExpr {
        private final UnaryOp op;
        private AnnExpr operand;

        public Unary(UnaryOp op, AnnExpr operand) {
            this.op = op;
            this.operand = operand;
        }

        public UnaryOp getOp() {
            return op;
        }

        public AnnExpr getOperand() {
            return operand;
        }

        public void setOperand(AnnExpr operand) {
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

    /** {@code ==>}: only in annotations, never in programs. */
    public static final class Implication extends AnnExpr {
        private AnnExpr left;
        private AnnExpr right;

        public Implication(AnnExpr left, AnnExpr right) {
            this.left = left;
            this.right = right;
        }

        public AnnExpr getLeft() {
            return left;
        }

        public AnnExpr getRight() {
            return right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImplication(this);
        }

        @Override
        public String toString() {
            return "(" + left + " ==> " + right + ")";
        }
    }

    /** {@code <==>}, translated as implication both ways. */
    public static final class Equivalence extends AnnExpr {
        private AnnExpr left;
        private AnnExpr right;

        public Equivalence(AnnExpr left, AnnExpr right) {
            this.left = left;
            this.right = right;
        }

        public AnnExpr getLeft() {
            return left;
        }

        public AnnExpr getRight() {
            return right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitEquivalence(this);
        }

        @Override
        public String toString() {
            return "(" + left + " <==> " + right + ")";
        }
    }

    // ------------------------------------------------------------------ quantifiers

    /** Where a quantified variable ranges. */
    public abstract static sealed class Domain
            permits RangeDomain, SetDomain, TypeDomain, ExprDomain {
    }

    public static final class RangeDomain extends Domain {
        private final AnnExpr from;
        private final AnnExpr to;

        public RangeDomain(AnnExpr from, AnnExpr to) {
            this.from = from;
            this.to = to;
        }

        public AnnExpr getFrom() {
            return from;
        }

        public AnnExpr getTo() {
            return to;
        }
    }

    public static final class SetDomain extends Domain {
        private final AnnExpr first;
        private final AnnExpr second;

        public SetDomain(AnnExpr first, AnnExpr second) {
            this.first = first;
            this.second = second;
        }

        public AnnExpr getFirst() {
            return first;
        }

        /** Null when the set has a single member. */
        public AnnExpr getSecond() {
            return second;
        }
    }

    /** A type name: membership is carried by the bound variable's HOL type. */
    public static final class TypeDomain extends Domain {
        private final String typeName;

        public TypeDomain(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    /** An array or set named by an expression. */
    public static final class ExprDomain extends Domain {
        private final AnnExpr expr;

        public ExprDomain(AnnExpr expr) {
            this.expr = expr;
        }

        public AnnExpr getExpr() {
            return expr;
        }
    }

    public static final class BoundVar {
        private final String name;
        private final Domain domain;

        public BoundVar(String name, Domain domain) {
            this.name = name;
            this.domain = domain;
        }

        public String getName() {
            return name;
        }

        /** Null when the variable ranges over everything. */
        public Domain getDomain() {
            return domain;
        }
    }

    public static final class Quantifier extends AnnExpr {
        public enum Kind { FORALL, EXISTS }

        private final Kind kind;
        private final List<BoundVar> variables;
        private AnnExpr body;

        public Quantifier(Kind kind, List<BoundVar> variables, AnnExpr body) {
            this.kind = kind;
            this.variables = variables;
            this.body = body;
        }

        public Kind getKind() {
            return kind;
        }

        public List<BoundVar> getVariables() {
            return variables;
        }

        public AnnExpr getBody() {
            return body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitQuantifier(this);
        }

        @Override
        public String toString() {
            return kind.name().toLowerCase() + "(...)";
        }
    }

    // ------------------------------------------------------------------ calls

    /** An application of a {@code define}. */
    public static final class Call extends AnnExpr {
        private final String name;
        private final List<AnnExpr> arguments;

        public Call(String name, List<AnnExpr> arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public List<AnnExpr> getArguments() {
            return arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCall(this);
        }

        @Override
        public String toString() {
            return name + arguments;
        }
    }

    // ------------------------------------------------------- process-oriented

    /** {@code in(process, state)}. */
    public static final class InState extends AnnExpr {
        private String process;
        private final String pstate;

        public InState(String process, String pstate) {
            this.process = process;
            this.pstate = pstate;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        public String getPstate() {
            return pstate;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitInState(this);
        }

        @Override
        public String toString() {
            return "in(" + process + ", " + pstate + ")";
        }
    }

    /** {@code time(process)}: how long the process has been in its state. */
    public static final class LocalTime extends AnnExpr {
        private String process;

        public LocalTime(String process) {
            this.process = process;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLocalTime(this);
        }

        @Override
        public String toString() {
            return "time(" + process + ")";
        }
    }

    // ------------------------------------------------------------------ temporal

    /** The operators that speak about other states than the current one. */
    public static final class Temporal extends AnnExpr {
        public enum Kind {
            /** previously(phi): phi at the previous boundary. */
            PREVIOUSLY,
            /** next(phi): phi at the successor, if one exists. */
            NEXT,
            /** once(phi): phi at some reachable earlier state. */
            ONCE,
            /** during(trigger, interrupt, body). */
            DURING,
            /** timer(t): the window has lasted longer than t. */
            TIMER,
            /** within(phi, t): phi somewhere in the next t. */
            WITHIN,
            /** stable(phi, t): phi throughout the next t. */
            STABLE,
            /** cooldown(phi, t): phi happened, and not again within t. */
            COOLDOWN,
            /** on(trigger, property): wherever the trigger holds, so does the property. */
            ON
        }

        private final Kind kind;
        private AnnExpr first;
        private AnnExpr second;
        private AnnExpr third;

        public Temporal(Kind kind, AnnExpr first, AnnExpr second, AnnExpr third) {
            this.kind = kind;
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public Kind getKind() {
            return kind;
        }

        /** phi, or the trigger for during and on. */
        public AnnExpr getFirst() {
            return first;
        }

        /**
         * The duration for timer/within/stable/cooldown, during's interrupt, or on's
         * property.
         */
        public AnnExpr getSecond() {
            return second;
        }

        /** during's body only. */
        public AnnExpr getThird() {
            return third;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTemporal(this);
        }

        @Override
        public String toString() {
            return kind.name().toLowerCase() + "(...)";
        }
    }

    /** {@code expr.scope(pre)}, {@code .scope(prev)} or {@code .scope(past(phi))}. */
    public static final class Scope extends AnnExpr {
        public enum Kind { PRE, PREV, PAST }

        private final Kind kind;
        private AnnExpr base;
        private AnnExpr phi;

        public Scope(Kind kind, AnnExpr base, AnnExpr phi) {
            this.kind = kind;
            this.base = base;
            this.phi = phi;
        }

        public Kind getKind() {
            return kind;
        }

        public AnnExpr getBase() {
            return base;
        }

        /** The condition for {@code past(phi)}; null otherwise. */
        public AnnExpr getPhi() {
            return phi;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitScope(this);
        }

        @Override
        public String toString() {
            return base + ".scope(" + kind.name().toLowerCase() + ")";
        }
    }

    // ------------------------------------------------------------------ visitor

    public interface Visitor<R> {
        R visitLiteral(Literal expr);

        R visitVarRef(VarRef expr);

        R visitBinary(Binary expr);

        R visitUnary(Unary expr);

        R visitImplication(Implication expr);

        R visitEquivalence(Equivalence expr);

        R visitQuantifier(Quantifier expr);

        R visitCall(Call expr);

        R visitInState(InState expr);

        R visitLocalTime(LocalTime expr);

        R visitTemporal(Temporal expr);

        R visitScope(Scope expr);
    }
}
