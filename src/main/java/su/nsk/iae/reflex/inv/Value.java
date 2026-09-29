package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.math.BigInteger;

/** A constant value the analysis can know a variable to hold: a bool or a number. */
public record Value(boolean isBool, BigInteger number) {

    public static Value of(boolean value) {
        return new Value(true, value ? BigInteger.ONE : BigInteger.ZERO);
    }

    public static Value of(BigInteger value) {
        return new Value(false, value);
    }

    public static Value of(long value) {
        return of(BigInteger.valueOf(value));
    }

    public boolean truth() {
        return number.signum() != 0;
    }

    /** The value as a HOL term. */
    public Term term() {
        if (isBool) {
            return truth() ? Terms.TRUE : Terms.FALSE;
        }
        return number.signum() < 0
                ? new Term.Prefix("-", new Term.Var(number.negate().toString()))
                : new Term.Var(number.toString());
    }

    @Override
    public String toString() {
        return isBool ? Boolean.toString(truth()) : number.toString();
    }
}
