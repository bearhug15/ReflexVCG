package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.analysis.ProcessFacts;
import su.nsk.iae.reflex.analysis.StaticAnalysis;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the static analysis takes for granted about processes, checked and exported
 * (priority mid).
 *
 * <p>{@link StaticAnalysis} discards paths on the strength of per-process facts -
 * StaticalAnalysis.tex rules 1 and 2, that a process which can never fail is never in
 * error and one which can never be stopped and does not begin stopped is never stopped,
 * and the group rules, that processes in one group stop and fail together. Those are
 * claims about every boundary, and a discarded path drops its conditions with it, so a
 * wrong claim silently loses proof obligations. The readings are provisional (CLAUDE.md).
 *
 * <p>So each claim is put through the same check as any other candidate. The ones that
 * survive become invariants, and so something Isabelle proves; the ones that do not are
 * reported by {@link #unconfirmed()}. An unconfirmed claim is not proof the analysis is
 * wrong - the check is conservative - but it is exactly where to look.
 */
public final class StaticAnalysisClaims implements CandidateSource<Candidate> {

    /** Two processes are in {@code state} together or not at all. */
    public record Together(String first, String second, String state) implements Candidate {

        @Override
        public Set<String> concerns() {
            return Set.of(first, second);
        }

        @Override
        public boolean assume(CycleStart start) {
            String a = start.initialState(first);
            String b = start.initialState(second);
            return a == null || b == null || a.equals(state) == b.equals(state);
        }

        @Override
        public boolean holdsAt(Boundary boundary) {
            String a = boundary.pstate(first);
            String b = boundary.pstate(second);
            return a != null && b != null && a.equals(state) == b.equals(state);
        }
    }

    /** What each claim says, in words, for the report. */
    private final Map<Candidate, String> claims = new LinkedHashMap<>();
    private final Map<Candidate, Integer> groups = new LinkedHashMap<>();
    private final List<String> unconfirmed = new ArrayList<>();

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.STATIC_ANALYSIS);
    }

    @Override
    public List<Candidate> guess(AnalysisContext context) {
        ProcessFacts facts = new StaticAnalysis(context.program()).getFacts();
        for (String process : context.processes()) {
            ProcessFacts.Facts of = facts.of(process);
            if (!of.reachE()) {
                claims.put(new ProcessStates.NotIn(process, "error"),
                        "rule 1: " + process + " can never fail, so is never in error");
            }
            if (!of.reachS() && !of.startS()) {
                claims.put(new ProcessStates.NotIn(process, "stop"),
                        "rule 2: " + process + " can never be stopped and does not begin stopped");
            }
        }
        Map<Integer, List<String>> members = new LinkedHashMap<>();
        for (String process : context.processes()) {
            members.computeIfAbsent(facts.of(process).group(), g -> new ArrayList<>()).add(process);
        }
        members.forEach((group, processes) -> {
            for (String other : processes.subList(1, processes.size())) {
                for (String state : List.of("stop", "error")) {
                    Together together = new Together(processes.get(0), other, state);
                    claims.put(together, "group " + processes + ": " + processes.get(0) + " and "
                            + other + " are in " + state + " together");
                    groups.put(together, group);
                }
            }
        });
        return new ArrayList<>(claims.keySet());
    }

    /** Each claim the check could not confirm, in words. Filled by {@link #build}. */
    public List<String> unconfirmed() {
        return List.copyOf(unconfirmed);
    }

    @Override
    public void build(List<Candidate> survivors, AnalysisContext context, ExtraInvariants into) {
        claims.forEach((claim, text) -> {
            if (!survivors.contains(claim)) {
                unconfirmed.add(text);
            }
        });

        Term s = ExtraInvariant.STATE;
        // Rules 1 and 2 add something only where the reachable states do not already say it.
        for (String process : context.processes()) {
            List<Term> never = new ArrayList<>();
            for (Candidate claim : survivors) {
                if (claim instanceof ProcessStates.NotIn notIn && notIn.process().equals(process)
                        && context.reachableStates(process).contains(notIn.state())) {
                    never.add(new Term.Infix("\\<noteq>", Terms.pstateOf(s, process),
                            new Term.Quoted(notIn.state())));
                }
            }
            if (!never.isEmpty()) {
                into.add(new ExtraInvariant(
                                AnalysisContext.uniqueName(into, "extra_static_" + AnalysisContext.identifier(process)),
                                ExtraInvariant.Kind.STATIC_ANALYSIS, Terms.conjunction(never),
                                "the static analysis's rules 1 and 2 for " + process),
                        Tag.process(process));
            }
        }

        Map<Integer, List<Together>> confirmed = new LinkedHashMap<>();
        for (Candidate claim : survivors) {
            if (claim instanceof Together together) {
                confirmed.computeIfAbsent(groups.get(together), g -> new ArrayList<>()).add(together);
            }
        }
        confirmed.forEach((group, pairs) -> {
            List<Term> equivalences = new ArrayList<>();
            List<Tag> tags = new ArrayList<>();
            for (Together pair : pairs) {
                equivalences.add(new Term.Infix("=",
                        Terms.pstateCompare(s, pair.first(), pair.state()),
                        Terms.pstateCompare(s, pair.second(), pair.state())));
                Tag first = Tag.process(pair.first());
                Tag second = Tag.process(pair.second());
                if (!tags.contains(first)) {
                    tags.add(first);
                }
                if (!tags.contains(second)) {
                    tags.add(second);
                }
            }
            into.add(new ExtraInvariant(
                    AnalysisContext.uniqueName(into, "extra_group_" + AnalysisContext.identifier(pairs.get(0).first())),
                    ExtraInvariant.Kind.STATIC_ANALYSIS, Terms.conjunction(equivalences),
                    "the static analysis's group rules: these processes stop and fail together"), tags);
        });
    }
}
