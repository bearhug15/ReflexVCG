package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The states each process can be found in at a boundary (priority high).
 *
 * <p>Guesses, for every process and every state it has - the declared ones, stop and
 * error - that it is never found there. Whatever survives is a state it is never in; the
 * invariant lists the rest. {@code getPstate} is only ever changed by {@code setPstate}
 * with a literal state, so the proof of one is a matter of reading the process's state
 * off the path.
 */
public final class ProcessStates implements CandidateSource<ProcessStates.NotIn> {

    /** The process is never found in the state at a boundary. */
    public record NotIn(String process, String state) implements Candidate {

        @Override
        public Set<String> concerns() {
            return Set.of(process);
        }

        @Override
        public boolean assume(CycleStart start) {
            return !state.equals(start.initialState(process));
        }

        @Override
        public boolean holdsAt(Boundary boundary) {
            String now = boundary.pstate(process);
            return now != null && !now.equals(state);
        }
    }

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.PROCESS_STATES);
    }

    @Override
    public List<NotIn> guess(AnalysisContext context) {
        List<NotIn> guesses = new ArrayList<>();
        for (String process : context.processes()) {
            for (String state : context.allStates(process)) {
                guesses.add(new NotIn(process, state));
            }
        }
        return guesses;
    }

    /** The states left once the ones a process is never in are taken away. */
    static List<String> reachable(String process, List<NotIn> survivors, AnalysisContext context) {
        Set<String> never = new LinkedHashSet<>();
        survivors.stream().filter(s -> s.process().equals(process)).forEach(s -> never.add(s.state()));
        List<String> reachable = new ArrayList<>(context.allStates(process));
        reachable.removeAll(never);
        return reachable;
    }

    @Override
    public void build(List<NotIn> survivors, AnalysisContext context, ExtraInvariants into) {
        for (String process : context.processes()) {
            List<String> reachable = reachable(process, survivors, context);
            List<Term> options = new ArrayList<>();
            reachable.forEach(state -> options.add(Terms.pstateCompare(ExtraInvariant.STATE, process, state)));
            into.add(new ExtraInvariant(
                            AnalysisContext.uniqueName(into, "extra_states_" + AnalysisContext.identifier(process)),
                            ExtraInvariant.Kind.PROCESS_STATES, Terms.disjunction(options),
                            process + " is only ever found in " + String.join(", ", reachable)),
                    Tag.process(process));
        }
    }
}
