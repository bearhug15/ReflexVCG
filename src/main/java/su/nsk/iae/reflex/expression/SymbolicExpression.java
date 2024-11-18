package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.ExprType;

public interface SymbolicExpression {
    ExprType exprType();
    String toString(IStatementCreator creator);

    void actuate(String programState, IStatementCreator creator);
    boolean isActuated();

}
