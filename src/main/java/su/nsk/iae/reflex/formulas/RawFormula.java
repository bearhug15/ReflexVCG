package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.List;

public class RawFormula implements Formula{
    String value;
    String name;

    public RawFormula(String value) {
        this.value = value;
    }
    public RawFormula(String name, String value) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String toString(IStatementCreator creator){
        return value;
    }

    @Override
    public List<String> toStrings(IStatementCreator creator) {
        return List.of(value);
    }

    @Override
    public String toNamedString(IStatementCreator creator) {
        if (name == null) {
            throw new RuntimeException("Trying to make named string for nameless raw formula");
        }else {
            return name + ":\"" + value + "\"";
        }
    }

    @Override
    public List<String> toNamedStrings(IStatementCreator creator) {
        if (name == null) {
            throw new RuntimeException("Trying to make named string for nameless raw formula");
        }else {
            return List.of(name + ":\"" + value + "\"");
        }
    }

    @Override
    public Formula trim() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RawFormula)
            return this.name.equals(((RawFormula) obj).name) && this.value.equals(((RawFormula) obj).value);
        return false;
    }
}
