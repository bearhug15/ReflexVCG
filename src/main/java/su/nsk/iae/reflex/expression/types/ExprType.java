package su.nsk.iae.reflex.expression.types;

public interface ExprType {
    String toString();
    String takeGetter();
    String takeSetter();

    String invertBorder();
    String defaultValue();
}
