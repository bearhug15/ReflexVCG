package su.nsk.iae.reflex.expression.ops;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public class UnMinus implements UnaryOp {

    public String toString(IStatementCreator creator){
        return "-";
    }
}
