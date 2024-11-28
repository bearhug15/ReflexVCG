package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.types.StateType;
import su.nsk.iae.reflex.formulas.Formula;

public class ExprGenRes {
    SymbolicExpression expr;
    String state;
    Formula domain;

    ExprGenRes(SymbolicExpression expression, String state, Formula domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = domain;
    }

    public SymbolicExpression getExpr() {
        return expr;
    }

    public String getState() {
        return state;
    }

    public Formula getDomain() {
        return domain;
    }
}
