package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * How long a process can stay in a state with a timeout (priority mid).
 *
 * <p>A timeout of {@code T} is checked at the end of every cycle the process spends in the
 * state, and {@code ltime} only grows at the environment step after it. If the timeout
 * moves the process on, or resets its timer, then whenever the process is still in the
 * state at a boundary the check before it found {@code ltime < T}, and one tick has passed
 * since: {@code ltime s P < T + clock}. The guess is made for every state with a timeout
 * of fixed duration, and a timeout body that does neither is what makes it fail.
 */
public final class TimerBounds implements CandidateSource<TimerBounds.Below> {

    /** While the process is in the state, its ltime is below {@code bound}. */
    public record Below(String process, String state, long bound) implements Candidate {

        @Override
        public Set<String> concerns() {
            return Set.of(process);
        }

        @Override
        public boolean assume(CycleStart start) {
            if (state.equals(start.initialState(process))) {
                start.assumeTimerBelow(process, bound);
            }
            return true;
        }

        @Override
        public boolean holdsAt(Boundary boundary) {
            String now = boundary.pstate(process);
            if (now != null && !now.equals(state)) {
                return true;
            }
            Long below = boundary.timerBelow(process);
            return below != null && below <= bound;
        }
    }

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.TIMER_BOUNDS);
    }

    @Override
    public List<Below> guess(AnalysisContext context) {
        List<Below> guesses = new ArrayList<>();
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                IrState declared = context.findState(process, state);
                if (declared == null || declared.getTimeout() == null) {
                    continue;
                }
                Long duration = context.durationOf(declared.getTimeout().getDuration());
                if (duration != null) {
                    guesses.add(new Below(process, state, duration + context.clock()));
                }
            }
        }
        return guesses;
    }

    @Override
    public void build(List<Below> survivors, AnalysisContext context, ExtraInvariants into) {
        Term s = ExtraInvariant.STATE;
        for (Below bound : survivors) {
            into.add(new ExtraInvariant(
                            AnalysisContext.uniqueName(into, "extra_timer_" + AnalysisContext.identifier(bound.process())
                                    + "_" + AnalysisContext.identifier(bound.state())),
                            ExtraInvariant.Kind.TIMER_BOUNDS,
                            Terms.implication(Terms.pstateCompare(s, bound.process(), bound.state()),
                                    new Term.Infix("<", Terms.localTime(s, bound.process()),
                                            new Term.Var(Long.toString(bound.bound())))),
                            bound.process() + " spends less than " + bound.bound() + " in " + bound.state()
                                    + ": its timeout moves it on"),
                    Tag.process(bound.process()), Tag.state(bound.process(), bound.state()));
        }
    }
}
