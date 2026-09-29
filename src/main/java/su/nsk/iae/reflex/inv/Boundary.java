package su.nsk.iae.reflex.inv;

/**
 * What the walk knows at a cycle boundary - the end of a cycle, or the program's first
 * boundary - which is where a {@link Candidate} is checked.
 *
 * <p>Every answer may be "not known", and a candidate has to hold whatever the unknown
 * turns out to be: that is what makes the check sound.
 */
public interface Boundary {

    /** The state the process is in, or null when it is not known. */
    String pstate(String process);

    /**
     * The state the process was in at the boundary before this one - what
     * {@code predEnv} reaches - or null when it is not known.
     */
    String previousPstate(String process);

    /** The constant the variable holds, or null when it is not known to hold one. */
    Value value(String variable);

    /** A strict upper bound on the process's {@code ltime}, or null when there is none. */
    Long timerBelow(String process);

    /**
     * Whether the variable may have been written since the process last changed state -
     * since the state {@code prevProcState} names. True whenever that cannot be ruled out.
     */
    boolean writtenSinceEntry(String process, String variable);
}
