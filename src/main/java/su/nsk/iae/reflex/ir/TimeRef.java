package su.nsk.iae.reflex.ir;

/**
 * A duration, as written in a {@code timeout} clause: a time literal, a plain integer
 * count of clock ticks, or the name of a constant or variable holding one.
 *
 * <p>A named reference is kept as a name rather than resolved on the spot because name
 * mangling rewrites it later, and because whether the reference is variable decides how
 * the timeout is modelled - a variable duration cannot be compared against a constant
 * bound at generation time.
 */
public final class TimeRef extends IrNode {

    public enum Kind { TIME_LITERAL, INTEGER, NAME }

    private final Kind kind;
    private String text;

    private TimeRef(Kind kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    public static TimeRef ofTimeLiteral(String literal) {
        return new TimeRef(Kind.TIME_LITERAL, literal);
    }

    public static TimeRef ofInteger(String value) {
        return new TimeRef(Kind.INTEGER, value);
    }

    public static TimeRef ofName(String name) {
        return new TimeRef(Kind.NAME, name);
    }

    public Kind getKind() {
        return kind;
    }

    /** The literal text, or the referenced name. Rewritten by name mangling. */
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isName() {
        return kind == Kind.NAME;
    }

    @Override
    public String toString() {
        return text;
    }
}
