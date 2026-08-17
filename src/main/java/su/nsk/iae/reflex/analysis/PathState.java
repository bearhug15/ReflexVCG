package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;

import java.util.ArrayList;
import java.util.List;

/**
 * What a path has established so far: the accumulated attributes and the ordered sequence
 * of what it assumed and did. This is {@code ctx} of StaticalAnalysis.tex with
 * {@code curAttr} and {@code curCond}, and {@code passed} of IvReadings2026.
 *
 * <p>Immutable, so the traversal carries it down the recursion and lets it go on the way
 * back out. There is nothing to undo.
 *
 * <p>The sequence is kept, not just a summary, because the incompatibility rules turn on
 * <em>when</em> a process changed relative to an assertion about it: an earlier "active"
 * and a later "stopped" contradict each other only if nothing in between stopped the
 * process.
 */
public record PathState(Attributes curAttr, List<Event> events) {

    public static final PathState INITIAL = new PathState(Attributes.EMPTY, List.of());

    public PathState {
        events = List.copyOf(events);
    }

    /** Extends the path with what a construct does. */
    public PathState andThen(Attributes attributes) {
        Attributes combined = AttributeCalculus.cons(curAttr, attributes);
        if (attributes.processChange().isEmpty()) {
            return new PathState(combined, events);
        }
        List<Event> extended = new ArrayList<>(events);
        attributes.processChange().forEach((process, change) ->
                extended.add(new Event.Changed(process, change)));
        return new PathState(combined, extended);
    }

    public PathState asserting(Event event) {
        List<Event> extended = new ArrayList<>(events);
        extended.add(event);
        return new PathState(curAttr, extended);
    }

    public PathState asserting(List<Event> newEvents) {
        if (newEvents.isEmpty()) {
            return this;
        }
        List<Event> extended = new ArrayList<>(events);
        extended.addAll(newEvents);
        return new PathState(curAttr, extended);
    }

    // ------------------------------------------------------------------ queries

    /**
     * The status a process is known to have at the end of the path, or null when nothing
     * on the path says.
     *
     * <p>This is the temporal core of the rules: the most recent assertion about the
     * process, overridden by any change made after it - and by the <em>last</em> such
     * change, since an earlier one it undoes no longer applies.
     */
    public Term.Activity impliedStatus(String process) {
        Term.Activity asserted = null;
        Change changedSince = null;

        for (Event event : events) {
            if (event instanceof Event.StatusAsserted status && status.process().equals(process)) {
                asserted = status.activity();
                changedSince = null;
            } else if (event instanceof Event.StateAsserted state && state.process().equals(process)) {
                asserted = statusOfState(state.state());
                changedSince = null;
            } else if (event instanceof Event.Changed changed && changed.process().equals(process)) {
                changedSince = changed.change();
            }
        }

        if (changedSince != null) {
            return statusOfChange(changedSince);
        }
        return asserted;
    }

    /** Whether the path has said anything at all about a process's status. */
    public boolean saysAnythingAbout(String process) {
        for (Event event : events) {
            if (event instanceof Event.StatusAsserted status && status.process().equals(process)
                    || event instanceof Event.StateAsserted state && state.process().equals(process)) {
                return true;
            }
        }
        return false;
    }

    /** State assertions made about processes other than {@code process}. */
    public List<Event.StateAsserted> otherProcessStates(String process) {
        List<Event.StateAsserted> states = new ArrayList<>();
        for (Event event : events) {
            if (event instanceof Event.StateAsserted state && !state.process().equals(process)) {
                states.add(state);
            }
        }
        return states;
    }

    /** The status being in a given state implies. */
    public static Term.Activity statusOfState(String state) {
        return switch (state) {
            case "stop" -> Term.Activity.STOP;
            case "error" -> Term.Activity.ERROR;
            default -> Term.Activity.ACTIVE;
        };
    }

    /** The status a change leaves a process in. */
    public static Term.Activity statusOfChange(Change change) {
        return switch (change) {
            case START -> Term.Activity.ACTIVE;
            case STOP -> Term.Activity.STOP;
            case ERROR -> Term.Activity.ERROR;
        };
    }
}
