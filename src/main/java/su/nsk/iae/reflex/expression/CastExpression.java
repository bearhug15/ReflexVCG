package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.ExprType;

public class CastExpression implements SymbolicExpression{

    ExprType type;
    SymbolicExpression expr;

    public CastExpression(ExprType type, SymbolicExpression expr) {
        this.type = type;
        this.expr = expr;
    }

    @Override
    public ExprType exprType() {
        return type;
    }

    @Override
    public void actuate(String programState, IStatementCreator creator) {
        expr.actuate(programState, creator);
    }

    @Override
    public boolean isActuated() {
        return expr.isActuated();
    }

    @Override
    public String toString(IStatementCreator creator) {
        if (expr.exprType() == null){
            return creator.createCastExpression(expr.toString(creator),type);
            //return "("+type.toString(creator)+" "+expr.toString(creator)+")";
        }
        if (type.toString(creator).equals(expr.exprType().toString(creator))){
            return expr.toString(creator);
        }else{
            return creator.createCastExpression(expr.toString(creator),type);
            //return "("+type.toString(creator)+" "+expr.toString(creator)+")";
        }
    }
}
