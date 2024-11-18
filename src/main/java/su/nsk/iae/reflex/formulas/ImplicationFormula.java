package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.ops.BinaryOp;

import java.util.List;

public class ImplicationFormula implements Formula{
    Formula left;
    Formula right;
    public ImplicationFormula(Formula from, Formula to){
        left = from;
        right = to;
    }

    @Override
    public String toString(IStatementCreator creator){
        String leftS = left.toString(creator);
        String rightS = right.toString(creator);
        return "("+leftS+ BinaryOp.Implication+rightS +")";
    }
    @Override
    public List<String> toStrings(IStatementCreator creator) {
        List<String> strings = left.toStrings(creator);
        strings.add(right.toString(creator));
        return strings;
    }

    @Override
    public String toNamedString(IStatementCreator creator) {
        throw new RuntimeException("Trying to make named string for implication");
    }

    @Override
    public List<String> toNamedStrings(IStatementCreator creator) {
        throw new RuntimeException("Trying to make named string for implication");
    }

    @Override
    public Formula trim() {
        return this;
    }

    public Formula left(){return left;}
    public Formula right(){return right;}
}
