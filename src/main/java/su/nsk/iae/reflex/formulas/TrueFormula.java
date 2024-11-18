package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.List;

public class TrueFormula implements Formula{
    @Override
    public List<String> toStrings(IStatementCreator creator) {
        return List.of("True");
    }

    @Override
    public String toNamedString(IStatementCreator creator) {
        return "true:\"True\"";
    }

    @Override
    public List<String> toNamedStrings(IStatementCreator creator) {
        return List.of(this.toNamedString(creator));
    }

    @Override
    public Formula trim() {
        return this;
    }

    @Override
    public String toString(IStatementCreator creator) {
        return "True";
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrueFormula;
    }

}
