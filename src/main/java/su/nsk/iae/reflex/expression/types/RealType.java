package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public class RealType implements ExprType{
    public String toString(IStatementCreator creator){
        return "real";
    }
    public String takeGetter(){
        return "getVarReal";
    }

    public String takeSetter(){
        return "setVarReal";
    }

    @Override
    public String invertBorder() {
        throw new RuntimeException("Trying to invert real type value");
    }

    @Override
    public String defaultValue() {
        return "0.0";
    }
}
