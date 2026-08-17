package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;

/**
 * Something a path did, in the order it did it - the {@code passed} sequence the
 * incompatibility rules of IvReadings2026 are stated over.
 *
 * <p>The order matters. Each rule there asks not merely whether a process was changed,
 * but whether it was changed <em>between</em> an earlier assertion and the point being
 * checked, and whether that change was itself undone since. A single accumulated
 * "what happened to process p" cannot answer that, so the path keeps the sequence.
 */
public sealed interface Event {

    /** The path assumed a process has a particular status. */
    record StatusAsserted(String process, Term.Activity activity) implements Event {
    }

    /** The path assumed a process is in a particular state. */
    record StateAsserted(String process, String state) implements Event {
    }

    /** The path changed a process. */
    record Changed(String process, Change change) implements Event {
    }
}
