package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public interface ExprType {
    String toString(IStatementCreator creator);
    String takeGetter();
    String takeSetter();

    String invertBorder();
    String defaultValue();
}
