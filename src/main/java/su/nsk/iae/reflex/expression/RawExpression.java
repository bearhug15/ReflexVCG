package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.IntType;

public class RawExpression implements SymbolicExpression{
    String value;

    @Override
    public String toString(){
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
    public void actuate(String programState) {}

    @Override
    public boolean isActuated() {
        return true;
    }
}
