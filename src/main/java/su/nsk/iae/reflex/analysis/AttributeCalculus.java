package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.analysis.Attributes.ProcessChange;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combines attribute sets, as StaticalAnalysis.tex defines: {@code consAttributes} for
 * constructs executed one after another, {@code parAttributes} for alternatives of which
 * exactly one runs.
 *
 * <p>Several readings were needed where the specification is inconsistent; each is marked
 * SPEC below and listed in the class comment so they can be checked in one place:
 *
 * <ul>
 *   <li>{@code addConsAttributes} computes {@code newChangesTo} and never assigns it, so
 *       changesTo would never propagate. Taken as assigning it.</li>
 *   <li>The same function selects {@code procs} using
 *       {@code definedProcChange(newAttr.potProcessChange)} - definedProcChange takes a
 *       map, potProcessChange is a list. Read as: processes the new construct
 *       <em>might</em> change but does not <em>definitely</em> change.</li>
 *   <li>Its conflict checks then test the <em>old</em> potential changes, which cannot
 *       invalidate old definite knowledge. Read as testing the new ones: knowledge
 *       survives only if what might have happened since agrees with it.</li>
 *   <li>{@code addParAttributes} has {@code if (newAttr.reset == null) newAttr.reset =
 *       null}, which does nothing, and likewise for stateChanged. Read as conjunction:
 *       something is definite after a choice only if it is definite in every branch.</li>
 *   <li>{@code setsInter} unions a set of processes into a collection of sets. Read as
 *       adding the intersection and the difference as two elements, which is what the
 *       previous implementation did.</li>
 * </ul>
 */
public final class AttributeCalculus {

    private AttributeCalculus() {
    }

    // ------------------------------------------------------------------ sequential

    /** Attributes of {@code first} followed by {@code second}. */
    public static Attributes cons(Attributes first, Attributes second) {
        Map<String, Change> definite = Attributes.mutableCopy(first.processChange());

        // SPEC: processes the second construct might change, but does not definitely
        // change. Definite knowledge about those may no longer hold.
        Set<String> uncertain = new LinkedHashSet<>(first.definitelyChanged());
        uncertain.retainAll(second.potentiallyChanged());
        uncertain.removeAll(second.definitelyChanged());

        for (String process : uncertain) {
            // SPEC: tested against the second construct's potential changes. A definite
            // "started" survives a construct that might only start it again, but not one
            // that might have stopped it.
            boolean mayStart = second.mayChange(process, Change.START);
            boolean mayStop = second.mayChange(process, Change.STOP);
            boolean mayError = second.mayChange(process, Change.ERROR);
            Change known = definite.get(process);
            if (known == Change.START && (mayStop || mayError)
                    || known == Change.STOP && (mayStart || mayError)
                    || known == Change.ERROR && (mayStart || mayStop)) {
                definite.remove(process);
            }
        }

        // What the second construct definitely did overrides what the first did.
        definite.putAll(second.processChange());

        // A potential change is superseded once a later construct settled that process.
        Set<ProcessChange> potential = new LinkedHashSet<>();
        first.potProcessChange().stream()
                .filter(change -> !second.definitelyChanged().contains(change.process()))
                .forEach(potential::add);
        potential.addAll(second.potProcessChange());

        Set<String> changesTo;
        boolean mayStay;
        if (!second.changesTo().isEmpty() || second.mayStay()) {
            if (second.mayStay()) {
                // The second construct might not move; the first one's targets remain
                // possible.
                changesTo = union(second.changesTo(), first.changesTo());
                mayStay = first.mayStay();
            } else {
                // The second construct always moves, so it decides where.
                changesTo = second.changesTo();
                mayStay = false;
            }
        } else {
            changesTo = first.changesTo();
            mayStay = first.mayStay();
        }

        return Attributes.of(definite, potential,
                first.reset() || second.reset(),
                first.stateChanged() || second.stateChanged(),
                changesTo, mayStay);
    }

    public static Attributes cons(List<Attributes> attributes) {
        Attributes result = Attributes.EMPTY;
        for (Attributes next : attributes) {
            result = cons(result, next);
        }
        return result;
    }

    // ------------------------------------------------------------------ alternatives

    /**
     * Attributes of a choice between {@code first} and {@code second}. Only what holds in
     * both is definite; everything either might do stays possible.
     */
    public static Attributes par(Attributes first, Attributes second) {
        Map<String, Change> definite = Attributes.mutableCopy(first.processChange());
        definite.entrySet().removeIf(entry ->
                second.changeFor(entry.getKey()) != entry.getValue());

        Set<ProcessChange> potential = union(first.potProcessChange(), second.potProcessChange());

        boolean firstMoves = !first.changesTo().isEmpty() || first.mayStay();
        boolean secondMoves = !second.changesTo().isEmpty() || second.mayStay();
        Set<String> changesTo = union(first.changesTo(), second.changesTo());
        // A branch that says nothing about state means the process may stay put.
        boolean mayStay = first.mayStay() || second.mayStay() || !firstMoves || !secondMoves;
        if (!firstMoves && !secondMoves) {
            changesTo = Set.of();
            mayStay = false;
        }

        return Attributes.of(definite, potential,
                // SPEC: definite only if definite in every branch.
                first.reset() && second.reset(),
                first.stateChanged() && second.stateChanged(),
                changesTo, mayStay);
    }

    /**
     * Folds the definite changes back into the potential ones, so that after a choice a
     * process that is definitely changed is not also listed as potentially changed some
     * other way. This is {@code addParAttributesConc}.
     */
    public static Attributes concludePar(Attributes attributes) {
        Set<ProcessChange> potential = new LinkedHashSet<>();
        for (ProcessChange change : attributes.potProcessChange()) {
            Change definite = attributes.changeFor(change.process());
            potential.add(definite == null ? change : new ProcessChange(change.process(), definite));
        }
        return attributes.withProcessChanges(attributes.processChange(), potential);
    }

    public static Attributes par(List<Attributes> attributes) {
        if (attributes.isEmpty()) {
            return Attributes.EMPTY;
        }
        Attributes result = attributes.get(0);
        for (int i = 1; i < attributes.size(); i++) {
            result = par(result, attributes.get(i));
        }
        return concludePar(result);
    }

    /** A branch that may or may not be taken: the alternative is doing nothing. */
    public static Attributes optional(Attributes taken) {
        return concludePar(par(Attributes.EMPTY, taken));
    }

    private static <T> Set<T> union(Set<T> left, Set<T> right) {
        Set<T> result = new LinkedHashSet<>(left);
        result.addAll(right);
        return result;
    }
}
