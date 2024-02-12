package su.nsk.iae.reflex.formulas;

import java.util.List;

public class TrueFormula implements Formula{
    @Override
    public List<String> toStrings() {
        return List.of("True");
    }

    @Override
    public String toNamedString() {
        return "true:\"True\"";
    }

    @Override
    public List<String> toNamedStrings() {
        return List.of(this.toNamedString());
    }

    @Override
    public Formula trim() {
        return this;
    }

    @Override
    public String toString() {
        return "True";
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TrueFormula;
    }

}
