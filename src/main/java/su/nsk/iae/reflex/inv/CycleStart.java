package su.nsk.iae.reflex.inv;

/**
 * The first state of a cycle, as seen by a candidate taken as a hypothesis.
 *
 * <p>The walk learns which state a process began the cycle in when it reaches that
 * process's body. At that moment every candidate concerning the process is asked what it
 * implies about the cycle's first state, and may add what it knows or declare the path
 * impossible.
 */
public interface CycleStart {

    /** The state the process began the cycle in, or null when not learnt yet. */
    String initialState(String process);

    /**
     * The variable held {@code value} when the cycle began.
     *
     * @return false when something already said otherwise - the path is impossible
     */
    boolean assumeValue(String variable, Value value);

    /** The process's {@code ltime} was below {@code bound} when the cycle began. */
    void assumeTimerBelow(String process, long bound);
}
