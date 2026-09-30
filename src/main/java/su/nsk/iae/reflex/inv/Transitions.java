package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ways a process can have come to be in a state (mainOverview.tex, "Process state
 * transition condition"; priority low).
 *
 * <p>{@code getPstate s P = q \<longrightarrow> (let s2 = prevProcState s P in D1 \<or> ...)}
 * with one disjunct per way in: the guards still holding just before a {@code set state} -
 * or {@code start} - that actually moved the process to {@code q}, the timeout if it came
 * from one, and the process states known there. The initial state gets one more: nothing
 * before {@code s2} was a boundary, {@code toEnvNum emptyState s2 = 0}.
 *
 * <p>Not guessed: collected, once the other invariants are settled, from every possible
 * path, and complete because every way in is on one. A way in that asks for nothing makes
 * the whole invariant say nothing, and then it is left out.
 */
public final class Transitions implements CandidateSource<Candidate>, EntryCollector {

    private record Way(Term term, ExtraInvariant.Transition kind) {
    }

    private static final TermRenderer RENDERER = new TermRenderer();

    /**
     * Per state, its ways in, keyed as written: two paths through the same guards meet
     * the same claim, whether or not they share the graph nodes that made it.
     */
    private final Map<String, Map<String, Way>> ways = new LinkedHashMap<>();
    private final Set<String> trivial = new LinkedHashSet<>();

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.TRANSITION);
    }

    @Override
    public List<Candidate> guess(AnalysisContext context) {
        return List.of();
    }

    @Override
    public void entry(Entry entry, AnalysisContext context) {
        Term s2 = new Term.Var(AnalysisContext.TRANSITION_STATE);
        List<Term> parts = new ArrayList<>();
        boolean timed = false;
        for (Fact fact : entry.facts()) {
            if (fact.kind() == FactKind.ASSIGNMENT) {
                continue;
            }
            parts.add(fact.term());
            timed |= fact.timed();
        }
        if (entry.executingState() != null) {
            parts.add(Terms.pstateCompare(s2, entry.executing(), entry.executingState()));
        }
        if (entry.before() != null && !entry.process().equals(entry.executing())) {
            parts.add(Terms.pstateCompare(s2, entry.process(), entry.before()));
        }
        String key = key(entry.process(), entry.state());
        if (parts.isEmpty()) {
            trivial.add(key);
            return;
        }
        add(key, new Way(Terms.conjunction(parts),
                timed ? ExtraInvariant.Transition.TIMED : ExtraInvariant.Transition.CONDITIONAL));
    }

    private void add(String key, Way way) {
        ways.computeIfAbsent(key, k -> new LinkedHashMap<>()).putIfAbsent(RENDERER.render(way.term()), way);
    }

    @Override
    public void build(List<Candidate> survivors, AnalysisContext context, ExtraInvariants into) {
        if (context.firstProcess() != null && context.firstState() != null) {
            Term s2 = new Term.Var(AnalysisContext.TRANSITION_STATE);
            Term beforeAnyBoundary = new Term.Infix("=",
                    Terms.toEnvNum(Terms.emptyState(), s2), new Term.Var("0"));
            add(key(context.firstProcess(), context.firstState()),
                    new Way(beforeAnyBoundary, ExtraInvariant.Transition.INITIAL));
        }
        Term s = ExtraInvariant.STATE;
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                String key = key(process, state);
                Map<String, Way> found = ways.get(key);
                if (found == null || found.isEmpty() || trivial.contains(key)) {
                    continue;
                }
                List<Term> disjuncts = new ArrayList<>();
                List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
                for (Way way : found.values()) {
                    disjuncts.add(way.term());
                    Tag tag = Tag.transition(way.kind());
                    if (!tags.contains(tag)) {
                        tags.add(tag);
                    }
                }
                into.add(ExtraInvariant.wrapped(
                        AnalysisContext.uniqueName(into, "extra_trans_" + AnalysisContext.identifier(process)
                                + "_" + AnalysisContext.identifier(state)),
                        ExtraInvariant.Kind.TRANSITION, AnnTranslator.Scale.PSTATE, process, state,
                        UnchangedSinceEntry.sinceEntry(process, Terms.disjunction(disjuncts)),
                        process + " can be in " + state + " only by " + found.size() + " way(s) in"), tags);
            }
        }
    }

    private static String key(String process, String state) {
        return process + "\u0000" + state;
    }
}
