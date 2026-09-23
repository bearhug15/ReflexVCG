package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.TimeRef;

/**
 * How a program expression becomes part of an invariant's formula.
 *
 * <p>A transition condition quotes the program's own guards, and turning an
 * {@link IrExpr} into a prover's syntax is the renderer's business, not this package's.
 * The generator is handed one of these rather than reaching for the renderer itself.
 */
public interface ExpressionRendering {

    /** The expression's value, read in the state named {@code state}. */
    String expression(IrExpr expression, String state);

    /** A timeout's duration, a named one being read in the state named {@code state}. */
    String duration(TimeRef duration, String state);
}
