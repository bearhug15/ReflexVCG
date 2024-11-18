package su.nsk.iae.reflex.expression.ops;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.*;

public class UnInvert implements UnaryOp{
    //Bit inversion implemented as substruction from value defined by type
    String mirrorValue;
    public UnInvert(ExprType t){
        mirrorValue = t.invertBorder();
    }
    public String toString(IStatementCreator creator){
        return mirrorValue+"-";
    }
}
