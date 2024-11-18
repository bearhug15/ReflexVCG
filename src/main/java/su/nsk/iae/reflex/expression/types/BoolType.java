package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public class BoolType implements ExprType{
    public String toString(IStatementCreator creator){
        return "bool";
    }
    public String takeGetter(){
        return "getVarBool";
    }

    public String takeSetter(){
        return "setVarBool";
    }

    @Override
    public String invertBorder() {return "True";}

    @Override
    public String defaultValue() {
        return "False";
    }
}
