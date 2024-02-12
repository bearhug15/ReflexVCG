package su.nsk.iae.reflex.expression.types;

public class NatType implements ExprType{

    public String toString(){
        return "nat";
    }
    public String takeGetter(){
        return "getVarNat";
    }

    public String takeSetter(){
        return "setVarNat";
    }

    @Override
    public String invertBorder() {
        throw new RuntimeException("Trying to invert default nat type value");
    }

    @Override
    public String defaultValue() {
        return "0";
    }

}
