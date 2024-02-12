package su.nsk.iae.reflex.expression;

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
    public void actuate(String programState) {
        expr.actuate(programState);
    }

    @Override
    public boolean isActuated() {
        return expr.isActuated();
    }

    @Override
    public String toString() {
        if (expr.exprType() == null){
            return "("+type.toString()+" "+expr.toString()+")";
        }
        if (type.toString().equals(expr.exprType().toString())){
            return expr.toString();
        }else{
            return "("+type.toString()+" "+expr.toString()+")";
        }
    }
}
