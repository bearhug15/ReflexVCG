package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
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
    public String toString(IStatementCreator creator){
        return creator.createUnaryExpression(exp.toString(creator),op);
    }

    @Override
    public void actuate(String programState, IStatementCreator creator) {
        exp.actuate(programState, creator);
    }

    @Override
    public boolean isActuated() {
        return exp.isActuated();
    }
}
