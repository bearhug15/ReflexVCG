package su.nsk.iae.reflex.expression.types;

public class IntType implements ExprType{
    public String toString(){
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
