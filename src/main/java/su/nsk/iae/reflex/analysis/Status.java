package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;

import java.util.EnumSet;
import java.util.Set;

/**
 * What a process is at a given point of a cycle: running in one of its own states, stopped or
 * failed. Every fact the analysis reasons about narrows the set of these a process may have.
 */
public enum Status {
    ACTIVE, STOP, ERROR;

    /** The statuses compatible with a {@code process P in state <activity>} check. */
    public static Set<Status> of(Term.Activity activity) {
        return switch (activity) {
            case ACTIVE -> EnumSet.of(ACTIVE);
            case INACTIVE -> EnumSet.of(STOP, ERROR);
            case STOP -> EnumSet.of(STOP);
            case ERROR -> EnumSet.of(ERROR);
            case NONSTOP -> EnumSet.of(ACTIVE, ERROR);
            case NONERROR -> EnumSet.of(ACTIVE, STOP);
        };
    }

    /** The status of a process found in {@code state}: {@code stop}, {@code error} or its own. */
    public static Status ofState(String state) {
        return switch (state) {
            case "stop" -> STOP;
            case "error" -> ERROR;
            default -> ACTIVE;
        };
    }

    /** The status a change leaves a process in. */
    public static Status of(Change change) {
        return switch (change) {
            case START -> ACTIVE;
            case STOP -> STOP;
            case ERROR -> ERROR;
        };
    }
}
