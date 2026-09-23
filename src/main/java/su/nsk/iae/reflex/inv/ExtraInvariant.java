package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.term.Term;

import java.util.Objects;

/**
 * One invariant derived from the structure of the program rather than written by the
 * engineer.
 *
 * <p>The formula is a predicate over {@link #STATE}: it is stated about a state {@code s}
 * and, like the global invariant, speaks about every boundary at or below it. {@link #at}
 * instantiates it elsewhere. Held as a {@link Term}, so nothing here knows Isabelle.
 *
 * <p>What an invariant is about - its kind, process, state, variables - is not part of it:
 * those are tags, held by the {@link ExtraInvariants} container, so a caller can attach its
 * own and search by any combination.
 *
 * @param name        unique within its container, and usable as an Isabelle identifier
 * @param kind        which of the structural rules produced it
 * @param description a sentence saying what it claims, for the comment above it
 */
public record ExtraInvariant(String name, Kind kind, Term formula, String description) {

    /** The state the formula is stated about. */
    public static final Term STATE = new Term.Var("s");

    /**
     * The kinds of structural invariant (mainOverview.tex, "Extra Invariants").
     */
    public enum Kind {
        /** The states a process can be found in at a boundary. */
        PROCESS_STATES(Group.ADVANCED),
        /**
         * Values variables hold whenever a process is in a state - set before the state is
         * entered and left alone while it is active.
         */
        DEFINED_VARIABLES(Group.ADVANCED),
        /**
         * Values variables hold once a process has stayed in a state for more than one
         * cycle - written the same way on every pass through the state.
         */
        STABILIZED_VARIABLES(Group.ADVANCED),
        /** The ways a process can have come to be in a state. */
        TRANSITION(Group.OPTIONAL);

        private final Group group;

        Kind(Group group) {
            this.group = group;
        }

        public Group group() {
            return group;
        }
    }

    /**
     * Which lemmas a condition gets by default. The advanced ones are cheap and nearly
     * always useful; the optional ones are bigger and have to be asked for.
     */
    public enum Group { ADVANCED, OPTIONAL }

    /** The forms a way into a state takes in a {@link Kind#TRANSITION} invariant. */
    public enum Transition {
        /** The process has been in the state since the program started. */
        INITIAL,
        /** A {@code set state} (or {@code start}) taken under some condition. */
        CONDITIONAL,
        /** A conditional transition taken from a {@code timeout}. */
        TIMED
    }

    public ExtraInvariant {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(formula, "formula");
    }

    /** The formula stated about {@code state} instead of {@link #STATE}. */
    public Term at(Term state) {
        return Term.substitute(formula, STATE, state);
    }

    public Group group() {
        return kind.group();
    }
}
