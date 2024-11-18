package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.List;

public interface Formula {
    String toString(IStatementCreator creator);
    List<String>toStrings(IStatementCreator creator);
    String toNamedString(IStatementCreator creator);
    List<String> toNamedStrings(IStatementCreator creator);
    Formula trim();
}
