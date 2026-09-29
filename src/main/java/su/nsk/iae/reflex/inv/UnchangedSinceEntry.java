package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Variables left as they were when a process entered its state (priority mid).
 *
 * <p>{@code getPstate s P = q \<longrightarrow> (let s2 = prevProcState s P in
 * getVarVal s v [] = getVarVal s2 v [])}: nothing writes {@code v} while {@code P} is in
 * {@code q}, whichever process runs. Unlike the defined values it needs no constant - the
 * variable keeps whatever it had on entry - and it reads the whole value, so arrays and
 * structs count too.
 *
 * <p>Guessed for every reachable state and every variable some statement writes. It holds
 * at a boundary when the variable was not written since the process last changed state:
 * within the cycle if it changed state in it, and over the whole cycle otherwise - which,
 * with the hypothesis at the cycle's start, carries it on.
 */
public final class UnchangedSinceEntry implements CandidateSource<UnchangedSinceEntry.Unchanged> {

    /** {@code variable} is not written while {@code process} is in {@code state}. */
    public record Unchanged(String process, String state, String variable) implements Candidate {

        @Override
        public boolean holdsAt(Boundary boundary) {
            String now = boundary.pstate(process);
            if (now != null && !now.equals(state)) {
                return true;
            }
            return !boundary.writtenSinceEntry(process, variable);
        }
    }

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY);
    }

    @Override
    public List<Unchanged> guess(AnalysisContext context) {
        return guesses(context, context.written());
    }

    static List<Unchanged> guesses(AnalysisContext context, Iterable<String> variables) {
        List<Unchanged> guesses = new ArrayList<>();
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                for (String variable : variables) {
                    guesses.add(new Unchanged(process, state, variable));
                }
            }
        }
        return guesses;
    }

    /** {@code let s2 = prevProcState s P in body}. */
    static Term sinceEntry(String process, Term body) {
        return new Term.Let(AnalysisContext.TRANSITION_STATE,
                new Term.App("prevProcState", List.of(ExtraInvariant.STATE, new Term.Quoted(process))), body);
    }

    /** Whether a defined value already says what {@code variable} holds in the state. */
    static boolean definedAlready(ExtraInvariants into, String process, String state, String variable) {
        return !into.find(Tag.kind(ExtraInvariant.Kind.DEFINED_VARIABLES), Tag.state(process, state),
                Tag.variable(variable)).isEmpty();
    }

    @Override
    public void build(List<Unchanged> survivors, AnalysisContext context, ExtraInvariants into) {
        Term s = ExtraInvariant.STATE;
        Term s2 = new Term.Var(AnalysisContext.TRANSITION_STATE);
        Map<String, List<Unchanged>> byState = new LinkedHashMap<>();
        for (Unchanged unchanged : survivors) {
            if (!definedAlready(into, unchanged.process(), unchanged.state(), unchanged.variable())) {
                byState.computeIfAbsent(unchanged.process() + "\u0000" + unchanged.state(),
                        k -> new ArrayList<>()).add(unchanged);
            }
        }
        byState.values().forEach(facts -> {
            String process = facts.get(0).process();
            String state = facts.get(0).state();
            List<Term> equalities = new ArrayList<>();
            List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
            List<String> names = new ArrayList<>();
            for (Unchanged fact : facts) {
                equalities.add(new Term.Infix("=", rawValue(s, fact.variable()), rawValue(s2, fact.variable())));
                tags.add(Tag.variable(fact.variable()));
                names.add(fact.variable());
            }
            into.add(new ExtraInvariant(
                    AnalysisContext.uniqueName(into, "extra_unchanged_" + AnalysisContext.identifier(process)
                            + "_" + AnalysisContext.identifier(state)),
                    ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY,
                    Terms.implication(Terms.pstateCompare(s, process, state),
                            sinceEntry(process, Terms.conjunction(equalities))),
                    String.join(", ", names) + " keep the value they had when " + process + " entered " + state),
                    tags);
        });
    }

    /** {@code getVarVal s ''v'' []}: the whole value, whatever its type or shape. */
    static Term rawValue(Term state, String variable) {
        return new Term.App("getVarVal", List.of(state, new Term.Quoted(variable), new Term.ListTerm(List.of())));
    }
}
