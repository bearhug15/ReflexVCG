package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.ExprType;

public class ConstantExpression implements SymbolicExpression{
    String value;
    ExprType type;

    public ConstantExpression(String value, ExprType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public ExprType exprType() {
        return type;
    }

    @Override
    public void actuate(String programState, IStatementCreator creator) {}

    @Override
    public boolean isActuated() {
        return true;
    }

    public String toString(IStatementCreator creator){
        return value;
    }
}
