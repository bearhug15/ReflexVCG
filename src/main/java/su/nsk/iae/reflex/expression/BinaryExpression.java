package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.vcg.ExpressionVisitor;

public class BinaryExpression implements SymbolicExpression{
    BinaryOp op;
    SymbolicExpression leftExp;
    SymbolicExpression rightExp;
    ExprType type;
    public BinaryExpression(BinaryOp op, SymbolicExpression left, SymbolicExpression right, ExprType type){
        this.op = op;
        this.leftExp = left;
        this.rightExp = right;
        this.type = type;
    }


    @Override
    public ExprType exprType() {
        return type;
    }
    public String toString(){
        return "("+leftExp.toString()+" "+op.toString()+" "+rightExp.toString()+")";
    }

    @Override
    public void actuate(String programState) {
        leftExp.actuate(programState);
        rightExp.actuate(programState);
    }

    @Override
    public boolean isActuated() {
        return leftExp.isActuated() && rightExp.isActuated();
    }
}
