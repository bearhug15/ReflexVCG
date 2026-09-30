package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Values variables hold in a process state (mainOverview.tex, "Extra Invariants"; priority
 * mid): <em>defined</em> - whenever the process is in the state - and <em>stabilized</em> -
 * once it has been there two boundaries running.
 *
 * <p>Guesses, for every reachable state, every tracked variable and every constant the
 * variable is ever given - its initial value and each constant assigned to it - that the
 * variable holds that constant. A defined fact is also a hypothesis: once the walk knows a
 * process began a cycle in the state, it knows the variable's value then. A stabilized one
 * is not, since it depends on the boundary before the cycle's first.
 */
public final class VariableValues implements CandidateSource<VariableValues.Holds> {

    /** The variable holds the value while the process is in the state. */
    public record Holds(ExtraInvariant.Kind kind, String process, String state, String variable,
                        Value value) implements Candidate {

        @Override
        public Set<String> concerns() {
            return kind == ExtraInvariant.Kind.DEFINED_VARIABLES ? Set.of(process) : Set.of();
        }

        @Override
        public boolean assume(CycleStart start) {
            if (kind == ExtraInvariant.Kind.DEFINED_VARIABLES && state.equals(start.initialState(process))) {
                return start.assumeValue(variable, value);
            }
            return true;
        }

        @Override
        public boolean holdsAt(Boundary boundary) {
            String now = boundary.pstate(process);
            if (now != null && !now.equals(state)) {
                return true;
            }
            if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES) {
                String before = boundary.previousPstate(process);
                if (before != null && !before.equals(state)) {
                    return true;
                }
            }
            return value.equals(boundary.value(variable));
        }
    }

    private final Set<ExtraInvariant.Kind> kinds;

    /** Produces the kinds given: defined, stabilized, or both. */
    public VariableValues(Set<ExtraInvariant.Kind> kinds) {
        this.kinds = EnumSet.copyOf(kinds);
        this.kinds.retainAll(EnumSet.of(ExtraInvariant.Kind.DEFINED_VARIABLES,
                ExtraInvariant.Kind.STABILIZED_VARIABLES));
    }

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return kinds;
    }

    @Override
    public List<Holds> guess(AnalysisContext context) {
        Map<String, Set<Value>> possible = possibleValues(context);
        List<Holds> guesses = new ArrayList<>();
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                possible.forEach((variable, values) -> {
                    for (Value value : values) {
                        for (ExtraInvariant.Kind kind : kinds) {
                            guesses.add(new Holds(kind, process, state, variable, value));
                        }
                    }
                });
            }
        }
        return guesses;
    }

    /** Every constant a tracked variable is ever given, its initial value first. */
    static Map<String, Set<Value>> possibleValues(AnalysisContext context) {
        Map<String, Set<Value>> values = new LinkedHashMap<>();
        context.tracked().keySet().forEach(variable -> {
            Set<Value> possible = new LinkedHashSet<>();
            Value initial = context.initialValue(variable);
            if (initial != null) {
                possible.add(initial);
            }
            values.put(variable, possible);
        });
        AbstractCycle nothingKnown = new AbstractCycle(context, true);
        for (CfgNode node : context.cfg().nodes()) {
            if (node instanceof CfgNode.Assign assign
                    && assign.getTarget().getAccesses().isEmpty()
                    && values.containsKey(assign.getTarget().getName())) {
                Value written = nothingKnown.evaluate(assign.getValue());
                if (written != null) {
                    values.get(assign.getTarget().getName()).add(written);
                }
            }
        }
        return values;
    }

    @Override
    public void build(List<Holds> survivors, AnalysisContext context, ExtraInvariants into) {
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                if (kinds.contains(ExtraInvariant.Kind.DEFINED_VARIABLES)) {
                    add(into, context, survivors, process, state, ExtraInvariant.Kind.DEFINED_VARIABLES);
                }
                if (kinds.contains(ExtraInvariant.Kind.STABILIZED_VARIABLES)
                        && !neverStabilizes(survivors, process, state)) {
                    add(into, context, survivors, process, state, ExtraInvariant.Kind.STABILIZED_VARIABLES);
                }
            }
        }
    }

    private void add(ExtraInvariants into, AnalysisContext context, List<Holds> survivors,
                     String process, String state, ExtraInvariant.Kind kind) {
        Term s = ExtraInvariant.STATE;
        List<Term> values = new ArrayList<>();
        List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
        List<String> described = new ArrayList<>();
        for (Holds fact : survivors) {
            if (fact.kind() != kind || !fact.process().equals(process) || !fact.state().equals(state)) {
                continue;
            }
            if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES && survivors.contains(new Holds(
                    ExtraInvariant.Kind.DEFINED_VARIABLES, process, state, fact.variable(), fact.value()))) {
                // Holds from the moment the state is entered, so already said.
                continue;
            }
            values.add(equality(context, fact.variable(), fact.value()));
            tags.add(Tag.variable(fact.variable()));
            described.add(fact.variable() + " = " + fact.value());
        }
        if (values.isEmpty()) {
            return;
        }
        Term body = Terms.conjunction(values);
        String prefix = "extra_vars_";
        String when = "whenever " + process + " is in " + state;
        if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES) {
            body = Terms.implication(Terms.pstateCompare(new Term.App("predEnv", List.of(s)), process, state), body);
            prefix = "extra_stable_";
            when = "once " + process + " has been in " + state + " for two boundaries running";
        }
        into.add(ExtraInvariant.wrapped(
                AnalysisContext.uniqueName(into, prefix + AnalysisContext.identifier(process) + "_"
                        + AnalysisContext.identifier(state)),
                kind, AnnTranslator.Scale.PSTATE, process, state, body,
                String.join(", ", described) + " " + when), tags);
    }

    /**
     * Whether the facts left standing for a process in a state contradict each other: then
     * it never stays there two boundaries running, and every stabilized value survived only
     * because nothing ever tested it. True, but saying nothing, so it is left out. Dropping
     * it costs nothing: stabilized facts are never assumed, so nothing rests on them.
     */
    private static boolean neverStabilizes(List<Holds> survivors, String process, String state) {
        Map<String, Value> seen = new LinkedHashMap<>();
        for (Holds fact : survivors) {
            if (!fact.process().equals(process) || !fact.state().equals(state)) {
                continue;
            }
            Value previous = seen.putIfAbsent(fact.variable(), fact.value());
            if (previous != null && !previous.equals(fact.value())) {
                return true;
            }
        }
        return false;
    }

    /** {@code v = c}, or {@code v} and {@code \<not> v} for a bool. */
    static Term equality(AnalysisContext context, String variable, Value value) {
        Term read = Terms.valueGetter(ExtraInvariant.STATE, context.tracked().get(variable), variable, List.of());
        if (value.isBool()) {
            return value.truth() ? read : Terms.not(read);
        }
        return new Term.Infix("=", read, value.term());
    }
}
