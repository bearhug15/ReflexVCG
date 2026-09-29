package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.term.Term;

import java.util.List;
import java.util.Set;

/**
 * Something that learns from the ways processes enter their states.
 *
 * <p>After the candidates are settled, one more walk visits every possible path, loop
 * bodies included, and reports each {@code set state} - or {@code start} - that actually
 * changed a process's state, with what still held just before it.
 */
public interface EntryCollector {

    void entry(Entry entry, AnalysisContext context);

    /**
     * A process moving into one of its declared states, on a path the walk found possible.
     *
     * @param before         the state it left, or null when not known
     * @param executing      the process whose body the move is in
     * @param executingState that process's state at the move, or null when not known
     * @param facts          what held just before the move, stated about the state
     *                       {@link AnalysisContext#TRANSITION_STATE} names
     * @param insideLoop     whether the move is in the body of a loop, where nothing from
     *                       before the loop is known
     */
    record Entry(String process, String state, String before, String executing,
                 String executingState, List<Fact> facts, boolean insideLoop) {
    }

    /**
     * Something that held just before a move: a guard passed, a timeout checked, a value
     * assigned. Dropped from the walk the moment anything it reads is written.
     *
     * @param reads     the variables it reads, any write to which invalidates it
     * @param processes the processes whose state it reads
     * @param timerOf   the process whose timer it compares against, or null
     * @param assigned  for an assignment, the variable assigned
     * @param value     for an assignment, the value it was given, read in the state before
     *                  the move
     */
    record Fact(FactKind kind, Term term, Set<String> reads, Set<String> processes,
                String timerOf, String assigned, Term value) {

        /** Whether it says a timeout has been reached. */
        public boolean timed() {
            return kind == FactKind.TIMEOUT_REACHED;
        }
    }

    enum FactKind { GUARD, TIMEOUT_REACHED, TIMEOUT_NOT_REACHED, ASSIGNMENT }
}
