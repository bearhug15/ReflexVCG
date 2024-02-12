package su.nsk.iae.reflex.expression.types;

public class RealType implements ExprType{
    @Override
    public String toString(){
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
