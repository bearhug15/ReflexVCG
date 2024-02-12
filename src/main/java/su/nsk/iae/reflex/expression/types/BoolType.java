package su.nsk.iae.reflex.expression.types;

public class BoolType implements ExprType{
    public String toString(){
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
