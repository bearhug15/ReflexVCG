package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.ops.BinaryOp;

import java.util.ArrayList;
import java.util.List;

public class GreaterFormula implements Formula{
    String name;
    boolean isGreater;
    String leftSide;
    String rightSide;

    public GreaterFormula(String name, boolean isGreater, String leftSide, String rightSide) {
        this.name = name;
        this.isGreater = isGreater;
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    @Override
    public String toString(){
        if (isGreater){
            return "("+leftSide + (BinaryOp.More) +rightSide+")";
        }else{
            return "("+leftSide + BinaryOp.LessEq +rightSide+")";
        }

    }
    @Override
    public List<String> toStrings() {
        ArrayList<String> arr= new ArrayList<>();
        arr.add(toString());
        return arr;
    }

    @Override
    public String toNamedString() {
        if (isGreater){
            return name + ":\""+ leftSide + (BinaryOp.More) +rightSide + "\"";
        }else{
            return name + ":\""+ leftSide + BinaryOp.LessEq +rightSide + "\"";
        }
    }

    @Override
    public List<String> toNamedStrings() {
        ArrayList<String> arr= new ArrayList<>();
        arr.add(toNamedString());
        return arr;
    }

    @Override
    public Formula trim() {
        return this;
    }
}
