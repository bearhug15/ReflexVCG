package su.nsk.iae.reflex.expression.ops;

import su.nsk.iae.reflex.expression.types.*;

public class UnInvert implements UnaryOp{
    //Bit inversion implemented as substruction from value defined by type
    String mirrorValue;
    public UnInvert(ExprType t){
        mirrorValue = t.invertBorder();
    }
    public String toString(){
        return mirrorValue+"-";
    }
}
