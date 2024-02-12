package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.expression.ops.BinaryOp;

import java.util.List;
import java.util.StringJoiner;

public class ImplicationFormula implements Formula{
    Formula left;
    Formula right;
    public ImplicationFormula(Formula from, Formula to){
        left = from;
        right = to;
    }

    @Override
    public String toString(){
        String leftS = left.toString();
        String rightS = right.toString();
        return "("+leftS+ BinaryOp.Implication+rightS +")";
    }
    @Override
    public List<String> toStrings() {
        List<String> strings = left.toStrings();
        strings.add(right.toString());
        return strings;
    }

    @Override
    public String toNamedString() {
        throw new RuntimeException("Trying to make named string for implication");
    }

    @Override
    public List<String> toNamedStrings() {
        throw new RuntimeException("Trying to make named string for implication");
    }

    @Override
    public Formula trim() {
        return this;
    }

    public Formula left(){return left;}
    public Formula right(){return right;}
}
