package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.expression.ConstantExpression;
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
    public String toString(){
        return "("+state+ BinaryOp.Eq+value+")";
    }
    @Override
    public List<String> toStrings() {
        return List.of(toString());
    }

    @Override
    public String toNamedString() {
        return state+":\"("+state+ BinaryOp.Eq+value+")\"";
    }

    @Override
    public List<String> toNamedStrings() {
        return List.of(toNamedString());
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
