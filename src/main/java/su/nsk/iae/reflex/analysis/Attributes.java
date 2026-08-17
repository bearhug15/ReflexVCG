package su.nsk.iae.reflex.analysis;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * What executing a construct does to the processes of a program, as defined by the
 * attribute section of StaticalAnalysis.tex.
 *
 * <p>Immutable. The specification is written against persistent data - it clones an
 * attribute set and mutates the copy - and a record gives that for free: every operation
 * returns a new value, and backtracking during traversal simply drops it. There is
 * nothing to clone and no state to restore.
 *
 * @param processChange    processes this construct <em>definitely</em> changed, and how.
 *                         A process absent from the map was not definitely changed.
 * @param potProcessChange changes this construct <em>might</em> have made.
 * @param reset            the current process's timer was definitely reset.
 * @param stateChanged     the current process definitely left its state.
 * @param changesTo        states the current process may move to.
 * @param mayStay          the current process may finish without changing state. This is
 *                         the {@code null} that the specification stores inside the
 *                         changesTo list; kept as a separate flag so the set stays a set
 *                         of state names.
 */
public record Attributes(
        Map<String, Change> processChange,
        Set<ProcessChange> potProcessChange,
        boolean reset,
        boolean stateChanged,
        Set<String> changesTo,
        boolean mayStay) {

    /** What happened to a process. */
    public enum Change { START, STOP, ERROR }

    /** A (process, change) pair, as used by potProcessChange. */
    public record ProcessChange(String process, Change change) {
    }

    public static final Attributes EMPTY = new Attributes(
            Map.of(), Set.of(), false, false, Set.of(), false);

    public Attributes {
        processChange = Map.copyOf(processChange);
        potProcessChange = Set.copyOf(potProcessChange);
        changesTo = Set.copyOf(changesTo);
    }

    // ------------------------------------------------------------------ builders

    public static Attributes of(Map<String, Change> processChange,
                                Set<ProcessChange> potProcessChange,
                                boolean reset, boolean stateChanged,
                                Set<String> changesTo, boolean mayStay) {
        return new Attributes(processChange, potProcessChange, reset, stateChanged, changesTo, mayStay);
    }

    /** A construct that only resets the timer. */
    public static Attributes justReset() {
        return new Attributes(Map.of(), Set.of(), true, false, Set.of(), false);
    }

    /** A definite change to one process, which is also a potential change to it. */
    public static Attributes change(String process, Change change,
                                    boolean reset, boolean stateChanged, Set<String> changesTo) {
        return new Attributes(
                Map.of(process, change),
                Set.of(new ProcessChange(process, change)),
                reset, stateChanged, changesTo, false);
    }

    // ------------------------------------------------------------------ queries

    /** Processes with a definite change recorded - definedProcChange in the spec. */
    public Set<String> definitelyChanged() {
        return processChange.keySet();
    }

    /** Processes appearing anywhere in potProcessChange. */
    public Set<String> potentiallyChanged() {
        Set<String> processes = new LinkedHashSet<>();
        potProcessChange.forEach(change -> processes.add(change.process()));
        return processes;
    }

    public Change changeFor(String process) {
        return processChange.get(process);
    }

    public boolean mayChange(String process, Change change) {
        return potProcessChange.contains(new ProcessChange(process, change));
    }

    // ------------------------------------------------------------------ derivation

    public Attributes withReset(boolean value) {
        return new Attributes(processChange, potProcessChange, value, stateChanged, changesTo, mayStay);
    }

    public Attributes withStateChanged(boolean value) {
        return new Attributes(processChange, potProcessChange, reset, value, changesTo, mayStay);
    }

    public Attributes withChangesTo(Set<String> states, boolean stay) {
        return new Attributes(processChange, potProcessChange, reset, stateChanged, states, stay);
    }

    Attributes withProcessChanges(Map<String, Change> definite, Set<ProcessChange> potential) {
        return new Attributes(definite, potential, reset, stateChanged, changesTo, mayStay);
    }

    static Map<String, Change> mutableCopy(Map<String, Change> source) {
        return new LinkedHashMap<>(source);
    }

    static Set<ProcessChange> mutableCopy(Set<ProcessChange> source) {
        return new LinkedHashSet<>(source);
    }

    @Override
    public String toString() {
        return "Attributes[changed=" + processChange
                + ", may=" + potProcessChange
                + (reset ? ", reset" : "")
                + (stateChanged ? ", stateChanged" : "")
                + ", changesTo=" + changesTo + (mayStay ? "+stay" : "") + "]";
    }
}
