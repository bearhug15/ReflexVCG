package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;

/**
 * Something a path did, in the order it did it - the {@code passed} sequence the
 * incompatibility rules are stated over (static-analysis.md, section 4).
 *
 * <p>The order matters. A fact about a process constrains a later one only if nothing in
 * between could have changed the process, so the path keeps the sequence rather than a
 * summary.
 */
public sealed interface Event {

    /** The path assumed a process has a particular status: a {@code process P in state} check. */
    record StatusAsserted(String process, Term.Activity activity) implements Event {
    }

    /** The path dispatched a process in a particular state. */
    record StateAsserted(String process, String state) implements Event {
    }

    /** The path started, stopped or failed a process. */
    record Changed(String process, Change change) implements Event {
    }

    /**
     * The path ran a construct that <em>may</em> have started, stopped or failed a process -
     * a loop body, which the main path does not walk.
     */
    record MayChange(String process, Change change) implements Event {
    }

    /**
     * The path set a process's local time to zero: it moved the process to a state, whoever
     * did it, or the process reset its timer. In ReflexBase both are what makes
     * {@code ltime} zero, and it stays zero until the cycle ends.
     */
    record TimerReset(String process) implements Event {
    }

    /**
     * The path passed a loop. Each iteration ends in an environment step in the model the
     * conditions are stated in, and the state past the loop is opaque, so local time may have
     * grown: a reset before the loop no longer says it is zero.
     */
    record TimePassed() implements Event {
    }
}
