package su.nsk.iae.reflex.inv;

import java.util.Set;

/**
 * A guessed invariant, kept only if it holds at the first boundary and every cycle keeps
 * it (the Houdini scheme {@link InvariantChecker} runs).
 *
 * <p>Implementations should be records, or otherwise have value equality: two sources
 * guessing the same candidate then share one check.
 */
public interface Candidate {

    /**
     * The processes whose starting state brings this into play as a hypothesis. Empty for
     * a candidate that says nothing usable about a cycle's first state.
     */
    default Set<String> concerns() {
        return Set.of();
    }

    /**
     * Applies this, as a hypothesis, to the first state of a cycle, once the starting state
     * of a process it {@link #concerns() concerns} is known.
     *
     * @return false when the path is impossible under it
     */
    default boolean assume(CycleStart start) {
        return true;
    }

    /** Whether this certainly holds at the boundary. */
    boolean holdsAt(Boundary boundary);
}
