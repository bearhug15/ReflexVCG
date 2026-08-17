package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.ir.IrCopier;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits an expression into the mutually exclusive ways it can be evaluated.
 *
 * <p>{@code &&} and {@code ||} short-circuit, so an expression does not have a single
 * evaluation: {@code a && b} either stops at a false {@code a}, never looking at
 * {@code b}, or goes on to evaluate it. That distinction matters to condition
 * generation, because whether {@code b} was evaluated determines whether its own
 * conditions apply.
 *
 * <p>Each {@link Outcome} carries the guards that select it and the value the expression
 * has along it. A leaf expression yields exactly one outcome with no guards, so
 * expressions without connectives cost nothing.
 */
final class ExprLowering {

    /** One way an expression can evaluate: the guards that select it, and its value. */
    record Outcome(List<IrExpr> guards, IrExpr value) {

        boolean isConstant(boolean expected) {
            return value instanceof IrExpr.Literal literal
                    && literal.getKind() == IrExpr.Literal.Kind.BOOL
                    && Boolean.parseBoolean(literal.getText()) == expected;
        }
    }

    private ExprLowering() {
    }

    static List<Outcome> lower(IrExpr expr) {
        if (expr instanceof IrExpr.Binary binary) {
            if (binary.getOp() == IrExpr.BinaryOp.AND) {
                return lowerShortCircuit(binary, false);
            }
            if (binary.getOp() == IrExpr.BinaryOp.OR) {
                return lowerShortCircuit(binary, true);
            }
            return lowerBinary(binary);
        }
        if (expr instanceof IrExpr.Unary unary) {
            List<Outcome> outcomes = new ArrayList<>();
            for (Outcome operand : lower(unary.getOperand())) {
                IrExpr value = new IrExpr.Unary(unary.getOp(), operand.value());
                value.setResultType(unary.getResultType());
                outcomes.add(new Outcome(operand.guards(), value));
            }
            return outcomes;
        }
        if (expr instanceof IrExpr.Cast cast) {
            List<Outcome> outcomes = new ArrayList<>();
            for (Outcome operand : lower(cast.getOperand())) {
                IrExpr value = new IrExpr.Cast(cast.getTargetType(), operand.value(),
                        cast.getPreType(), cast.isImplicit());
                outcomes.add(new Outcome(operand.guards(), value));
            }
            return outcomes;
        }
        // Leaves, and anything without a short-circuiting connective inside.
        return List.of(new Outcome(List.of(), expr));
    }

    /**
     * {@code a && b} and {@code a || b}. {@code shortCircuitOn} is the value of the left
     * operand that decides the result without evaluating the right one - true for
     * {@code ||}, false for {@code &&}.
     */
    private static List<Outcome> lowerShortCircuit(IrExpr.Binary binary, boolean shortCircuitOn) {
        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome left : lower(binary.getLeft())) {
            // The left operand settled it; the right is never evaluated.
            List<IrExpr> decided = new ArrayList<>(left.guards());
            decided.add(shortCircuitOn ? IrCopier.copy(left.value()) : not(left.value()));
            outcomes.add(new Outcome(decided, literal(shortCircuitOn)));

            // Otherwise the result is whatever the right operand evaluates to.
            for (Outcome right : lower(binary.getRight())) {
                List<IrExpr> guards = new ArrayList<>(left.guards());
                guards.add(shortCircuitOn ? not(left.value()) : IrCopier.copy(left.value()));
                guards.addAll(right.guards());
                outcomes.add(new Outcome(guards, right.value()));
            }
        }
        return outcomes;
    }

    /** Both operands are always evaluated, so the outcomes are their cross product. */
    private static List<Outcome> lowerBinary(IrExpr.Binary binary) {
        List<Outcome> lefts = lower(binary.getLeft());
        List<Outcome> rights = lower(binary.getRight());
        if (lefts.size() == 1 && rights.size() == 1
                && lefts.get(0).guards().isEmpty() && rights.get(0).guards().isEmpty()) {
            return List.of(new Outcome(List.of(), binary));
        }

        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome left : lefts) {
            for (Outcome right : rights) {
                List<IrExpr> guards = new ArrayList<>(left.guards());
                guards.addAll(right.guards());
                IrExpr value = new IrExpr.Binary(binary.getOp(), left.value(), right.value());
                value.setResultType(binary.getResultType());
                outcomes.add(new Outcome(guards, value));
            }
        }
        return outcomes;
    }

    static IrExpr not(IrExpr expr) {
        IrExpr negated = new IrExpr.Unary(IrExpr.UnaryOp.NOT, IrCopier.copy(expr));
        negated.setResultType(IrType.BOOL);
        return negated;
    }

    static IrExpr literal(boolean value) {
        IrExpr literal = new IrExpr.Literal(IrExpr.Literal.Kind.BOOL, Boolean.toString(value));
        literal.setResultType(IrType.BOOL);
        return literal;
    }

    /** Combines guards into one condition; null when there are none. */
    static IrExpr conjunction(List<IrExpr> guards) {
        IrExpr result = null;
        for (IrExpr guard : guards) {
            result = result == null ? guard : and(result, guard);
        }
        return result;
    }

    static IrExpr and(IrExpr left, IrExpr right) {
        IrExpr conjunction = new IrExpr.Binary(IrExpr.BinaryOp.AND, left, right);
        conjunction.setResultType(IrType.BOOL);
        return conjunction;
    }
}
