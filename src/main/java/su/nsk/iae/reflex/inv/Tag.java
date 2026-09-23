package su.nsk.iae.reflex.inv;

import java.util.Objects;

/**
 * A label attached to an extra invariant, by which it can be found again.
 *
 * <p>A tag is a key and a value, so the same key can be asked about with different values -
 * {@code process=Controller}, {@code process=Pump} - and a caller can make up keys of its
 * own. The factories below are the ones the structural analysis attaches; searching by a
 * set of them is how a condition picks out the invariants that concern the states it
 * passes through.
 */
public record Tag(String key, String value) {

    public Tag {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }

    /** What kind of structural invariant this is. */
    public static Tag kind(ExtraInvariant.Kind kind) {
        return new Tag("kind", kind.name());
    }

    /** Which group of lemmas it belongs to, which decides whether it is used by default. */
    public static Tag group(ExtraInvariant.Group group) {
        return new Tag("group", group.name());
    }

    /** The process the invariant is about. */
    public static Tag process(String process) {
        return new Tag("process", process);
    }

    /**
     * One state of one process. Qualified by the process, since two processes may well
     * declare states of the same name.
     */
    public static Tag state(String process, String state) {
        return new Tag("state", process + "." + state);
    }

    /** A variable the invariant states the value of. */
    public static Tag variable(String variable) {
        return new Tag("variable", variable);
    }

    /** One way a transition condition says a state can be entered. */
    public static Tag transition(ExtraInvariant.Transition transition) {
        return new Tag("transition", transition.name());
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
