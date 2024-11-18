package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.ops.BinaryOp;

import java.util.List;

public class StateFormula implements Formula{
    String state;
    String value;
    public StateFormula(String state, String value){
        this.state = state;
        this.value = value;
    }
    @Override
    public String toString(IStatementCreator creator){
        return "("+state+ BinaryOp.Eq+value+")";
    }
    @Override
    public List<String> toStrings(IStatementCreator creator) {
        return List.of(toString(creator));
    }

    @Override
    public String toNamedString(IStatementCreator creator) {
        return state+":\"("+state+ BinaryOp.Eq+value+")\"";
    }

    @Override
    public List<String> toNamedStrings(IStatementCreator creator) {
        return List.of(toNamedString(creator));
    }

    @Override
    public Formula trim() {
        return this;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StateFormula)
            return this.state.equals(((StateFormula) obj).state) && this.value.equals(((StateFormula) obj).value);
        return false;
    }
}
