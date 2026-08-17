package su.nsk.iae.reflex.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * What a path has established so far: the accumulated attributes and the facts it has
 * asserted. This is {@code ctx} of StaticalAnalysis.tex, with {@code curAttr} and
 * {@code curCond}.
 *
 * <p>Immutable, so the traversal carries it down the recursion and lets it go on the way
 * back out. There is nothing to undo.
 */
public record PathState(Attributes curAttr, List<Term> curCond) {

    public static final PathState INITIAL = new PathState(Attributes.EMPTY, List.of());

    public PathState {
        curCond = List.copyOf(curCond);
    }

    /** Extends the path with what a construct does. */
    public PathState andThen(Attributes attributes) {
        return new PathState(AttributeCalculus.cons(curAttr, attributes), curCond);
    }

    /** Extends the path with a fact it has asserted. */
    public PathState asserting(Term term) {
        List<Term> extended = new ArrayList<>(curCond);
        extended.add(term);
        return new PathState(curAttr, extended);
    }

    public PathState asserting(List<Term> terms) {
        if (terms.isEmpty()) {
            return this;
        }
        List<Term> extended = new ArrayList<>(curCond);
        extended.addAll(terms);
        return new PathState(curAttr, extended);
    }

    /** Activity facts asserted about a process, in the order they were asserted. */
    public List<Term.Activity> activitiesAsserted(String process) {
        List<Term.Activity> activities = new ArrayList<>();
        for (Term term : curCond) {
            if (term instanceof Term.ProcessActivity activity && activity.process().equals(process)) {
                activities.add(activity.activity());
            }
        }
        return activities;
    }

    /** State assertions made about processes other than {@code process}. */
    public List<Term.PstateCompare> otherProcessStates(String process) {
        List<Term.PstateCompare> states = new ArrayList<>();
        for (Term term : curCond) {
            if (term instanceof Term.PstateCompare compare && !compare.process().equals(process)) {
                states.add(compare);
            }
        }
        return states;
    }
}
