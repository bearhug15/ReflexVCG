package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;

public class UnaryExpression implements SymbolicExpression{
    UnaryOp op;
    SymbolicExpression exp;
    ExprType type;

    public UnaryExpression(UnaryOp op, SymbolicExpression exp, ExprType type){
        this.op = op;
        this.exp = exp;
        this.type = type;
    }
    @Override
    public ExprType exprType() {
        return type;
    }
    public String toString(){
        return "("+op.toString()+exp.toString()+")";
    }

    @Override
    public void actuate(String programState) {
        exp.actuate(programState);
    }

    @Override
    public boolean isActuated() {
        return exp.isActuated();
    }
}
