package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * States of two processes never found together at a boundary (priority mid).
 *
 * <p>Guesses, for every pair of processes and every pair of their reachable states, that
 * the two are never found together. Written from the earlier process's side, as what its
 * being in a state allows the later ones to be in:
 * {@code getPstate s P = a \<longrightarrow> getPstate s Q = b1 \<or> ...}. Like the
 * reachable states it reads nothing but process states, so it is as easy to prove - there
 * are just more of them.
 */
public final class ProcessPairs implements CandidateSource<ProcessPairs.Excluded> {

    /** {@code first} in {@code firstState} and {@code second} in {@code secondState} never coincide. */
    public record Excluded(String first, String firstState, String second, String secondState)
            implements Candidate {

        @Override
        public Set<String> concerns() {
            return Set.of(first, second);
        }

        @Override
        public boolean assume(CycleStart start) {
            return !(firstState.equals(start.initialState(first))
                    && secondState.equals(start.initialState(second)));
        }

        @Override
        public boolean holdsAt(Boundary boundary) {
            String a = boundary.pstate(first);
            String b = boundary.pstate(second);
            return (a != null && !a.equals(firstState)) || (b != null && !b.equals(secondState));
        }
    }

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.PROCESS_PAIRS);
    }

    @Override
    public List<Excluded> guess(AnalysisContext context) {
        List<Excluded> guesses = new ArrayList<>();
        List<String> processes = context.processes();
        for (int i = 0; i < processes.size(); i++) {
            for (int j = i + 1; j < processes.size(); j++) {
                for (String a : context.reachableStates(processes.get(i))) {
                    for (String b : context.reachableStates(processes.get(j))) {
                        guesses.add(new Excluded(processes.get(i), a, processes.get(j), b));
                    }
                }
            }
        }
        return guesses;
    }

    @Override
    public void build(List<Excluded> survivors, AnalysisContext context, ExtraInvariants into) {
        Term s = ExtraInvariant.STATE;
        List<String> processes = context.processes();
        for (int i = 0; i < processes.size(); i++) {
            String first = processes.get(i);
            for (String a : context.reachableStates(first)) {
                List<Term> constraints = new ArrayList<>();
                List<Tag> tags = new ArrayList<>(List.of(Tag.process(first), Tag.state(first, a)));
                List<String> described = new ArrayList<>();
                for (String second : processes.subList(i + 1, processes.size())) {
                    Set<String> excluded = new LinkedHashSet<>();
                    for (Excluded pair : survivors) {
                        if (pair.first().equals(first) && pair.firstState().equals(a)
                                && pair.second().equals(second)) {
                            excluded.add(pair.secondState());
                        }
                    }
                    if (excluded.isEmpty()) {
                        continue;
                    }
                    List<String> allowed = new ArrayList<>(context.reachableStates(second));
                    allowed.removeAll(excluded);
                    List<Term> options = new ArrayList<>();
                    allowed.forEach(b -> options.add(Terms.pstateCompare(s, second, b)));
                    constraints.add(Terms.disjunction(options));
                    tags.add(Tag.process(second));
                    described.add(second + " in " + (allowed.isEmpty() ? "nothing" : String.join("/", allowed)));
                }
                if (constraints.isEmpty()) {
                    continue;
                }
                into.add(ExtraInvariant.wrapped(
                        AnalysisContext.uniqueName(into, "extra_pairs_" + AnalysisContext.identifier(first)
                                + "_" + AnalysisContext.identifier(a)),
                        ExtraInvariant.Kind.PROCESS_PAIRS, AnnTranslator.Scale.PSTATE, first, a, Terms.conjunction(constraints),
                        "while " + first + " is in " + a + ": " + String.join(", ", described)), tags);
            }
        }
    }
}
