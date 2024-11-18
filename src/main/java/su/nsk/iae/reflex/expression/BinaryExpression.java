package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;

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
    public String toString(IStatementCreator creator){
        return "("+leftExp.toString(creator)+" "+op.toString(creator)+" "+rightExp.toString(creator)+")";
    }

    @Override
    public void actuate(String programState, IStatementCreator creator) {
        leftExp.actuate(programState, creator);
        rightExp.actuate(programState, creator);
    }

    @Override
    public boolean isActuated() {
        return leftExp.isActuated() && rightExp.isActuated();
    }
}
