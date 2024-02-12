package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.types.ExprType;

public interface SymbolicExpression {
    ExprType exprType();
    String toString();

    void actuate(String programState);
    boolean isActuated();

}
