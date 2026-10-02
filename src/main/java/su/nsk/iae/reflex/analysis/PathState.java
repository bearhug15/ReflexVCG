package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.ProcessChange;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * What a path has established so far within one cycle: the ordered sequence of what it
 * assumed and did - {@code passed} of static-analysis.md, section 4.
 *
 * <p>Immutable, so the traversal carries it down the recursion and lets it go on the way
 * back out. There is nothing to undo.
 *
 * <p>The sequence is kept, not just a summary, because the rules turn on <em>when</em> a
 * process changed relative to a fact about it: an earlier "active" and a later "stopped"
 * contradict each other only if nothing in between could have stopped the process.
 */
public record PathState(List<Event> events) {

    public static final PathState INITIAL = new PathState(List.of());

    public PathState {
        events = List.copyOf(events);
    }

    /**
     * Extends the path with what a construct does to processes: its definite changes, and,
     * for a construct the path does not walk into (a loop body), the changes it may make.
     */
    public PathState andThen(Attributes attributes) {
        List<Event> added = new ArrayList<>();
        attributes.processChange().forEach((process, change) -> added.add(new Event.Changed(process, change)));
        for (ProcessChange potential : attributes.potProcessChange()) {
            if (attributes.changeFor(potential.process()) != potential.change()) {
                added.add(new Event.MayChange(potential.process(), potential.change()));
            }
        }
        return asserting(added);
    }

    public PathState asserting(Event event) {
        return asserting(List.of(event));
    }

    public PathState asserting(List<Event> newEvents) {
        if (newEvents.isEmpty()) {
            return this;
        }
        List<Event> extended = new ArrayList<>(events);
        extended.addAll(newEvents);
        return new PathState(extended);
    }

    // ------------------------------------------------------------------ queries

    /**
     * The statuses {@code process} may have at the end of the path (section 4.3), given those
     * it may have when the cycle begins. Every check and every dispatch of the process
     * narrows the set; a change replaces it, since nothing said before the change constrains
     * what comes after; a change that may or may not have happened adds its status. An empty
     * set means the path is impossible.
     */
    public Set<Status> possibleStatuses(String process, Set<Status> atCycleStart) {
        EnumSet<Status> possible = EnumSet.noneOf(Status.class);
        possible.addAll(atCycleStart);
        for (Event event : events) {
            if (event instanceof Event.StatusAsserted status && status.process().equals(process)) {
                possible.retainAll(Status.of(status.activity()));
            } else if (event instanceof Event.StateAsserted state && state.process().equals(process)) {
                possible.retainAll(EnumSet.of(Status.ofState(state.state())));
            } else if (event instanceof Event.Changed changed && changed.process().equals(process)) {
                possible = EnumSet.of(Status.of(changed.change()));
            } else if (event instanceof Event.MayChange may && may.process().equals(process)) {
                possible.add(Status.of(may.change()));
            }
        }
        return possible;
    }

    /**
     * Whether the path definitely started, stopped or failed {@code process} (section 4.5).
     * If it did, the process's state at its turn is the result of the last change that
     * actually happened - a definite one, or a later one the path may have made.
     */
    public boolean changed(String process) {
        for (Event event : events) {
            if (event instanceof Event.Changed changed && changed.process().equals(process)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code process}'s local time is zero at the end of the path (section 4.2): the
     * path set it to zero and passed no loop since.
     */
    public boolean timerReset(String process) {
        boolean zero = false;
        for (Event event : events) {
            if (event instanceof Event.TimerReset reset && reset.process().equals(process)) {
                zero = true;
            } else if (event instanceof Event.TimePassed) {
                zero = false;
            }
        }
        return zero;
    }

    /** The processes other than {@code process} the path has dispatched, with their states. */
    public List<Event.StateAsserted> otherProcessStates(String process) {
        List<Event.StateAsserted> states = new ArrayList<>();
        for (Event event : events) {
            if (event instanceof Event.StateAsserted state && !state.process().equals(process)) {
                states.add(state);
            }
        }
        return states;
    }
}
