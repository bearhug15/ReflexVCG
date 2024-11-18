package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.ExprType;

public class RawExpression implements SymbolicExpression{
    String value;

    @Override
    public String toString(IStatementCreator creator){
        return value;
    }
    public RawExpression(String value) {
        this.value = value;
    }

    @Override
    public ExprType exprType() {
        return null;
    }

    @Override
    public void actuate(String programState, IStatementCreator creator) {}

    @Override
    public boolean isActuated() {
        return true;
    }
}
