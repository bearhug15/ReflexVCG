package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.ops.BinaryOp;

import java.util.ArrayList;
import java.util.List;

public class EqualityFormula implements Formula{
    String name;
    boolean isEqual;
    SymbolicExpression leftSide;
    SymbolicExpression rightSide;

    public EqualityFormula(String name, boolean is_equal, SymbolicExpression leftSide, SymbolicExpression rightSide) {
        this.name = name;
        this.isEqual = is_equal;
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    @Override
    public String toString(){
        String left = leftSide.toString();
        String right = rightSide.toString();
        if (isEqual){
            return "("+left+ BinaryOp.Eq+ right+")";
        }else{
            return "("+left+ BinaryOp.NotEq+ right+")";
        }
    }
    @Override
    public List<String> toStrings() {
        ArrayList<String> arr = new ArrayList<>();
        arr.add(toString());
        return arr;
    }

    @Override
    public String toNamedString() {
        String left = leftSide.toString();
        String right = rightSide.toString();
        if (isEqual){
            return name+":\""+left+ BinaryOp.Eq+ right+"\"";
        }else{
            return name+":\""+left+ BinaryOp.NotEq+ right+"\"";
        }
    }

    @Override
    public List<String> toNamedStrings() {
        ArrayList<String> arr = new ArrayList<>();
        String left = leftSide.toString();
        String right = rightSide.toString();
        if (isEqual){
            arr.add(name+":\""+left+ BinaryOp.Eq+ right+"\"");
        }else{
            arr.add(name+":\""+left+ BinaryOp.NotEq+ right+"\"");
        }
        return arr;
    }

    @Override
    public Formula trim() {
        return this;
    }
}
