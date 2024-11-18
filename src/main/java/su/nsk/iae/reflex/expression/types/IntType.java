package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public class IntType implements ExprType{
    public String toString(IStatementCreator creator){
        return "int";
    }
    public String takeGetter(){
        return "getVarInt";
    }

    public String takeSetter(){
        return "setVarInt";
    }

    @Override
    public String invertBorder() {
        throw new RuntimeException("Trying to invert default int type value");
    }

    @Override
    public String defaultValue() {
        return "0";
    }
}
