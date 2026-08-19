package su.nsk.iae.reflex.ann;

import java.util.List;

/**
 * A {@code define} introduced by an annotation: a typed macro, expanded where it is used.
 *
 * <p>A variable definition is the degenerate case with no parameters.
 *
 * @param name       the name the definition is invoked by
 * @param parameters formal parameter names, empty for a variable definition
 * @param body       the expression substituted at each use
 */
public record AnnDefinition(String name, List<String> parameters, AnnExpr body) {

    public AnnDefinition {
        parameters = List.copyOf(parameters);
    }

    public boolean isVariable() {
        return parameters.isEmpty();
    }

    @Override
    public String toString() {
        return name + (isVariable() ? "" : parameters.toString()) + " = " + body;
    }
}
