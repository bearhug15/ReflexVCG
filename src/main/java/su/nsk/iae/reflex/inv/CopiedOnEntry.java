package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.inv.UnchangedSinceEntry.Unchanged;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Variables holding a value copied into them on the way into a state (priority low).
 *
 * <p>{@code getPstate s P = q \<longrightarrow> (let s2 = prevProcState s P in
 * v(s) = e(s2))}: every way into {@code q} assigns {@code v := e} before the move, with
 * nothing {@code e} reads written in between, and nothing writes {@code v} while the process
 * stays. It is the relational generalisation of a defined value - {@code e} need not be a
 * constant, so a setpoint latched from an input on entry is caught.
 *
 * <p>Two halves, checked differently. That nothing writes {@code v} after entry is
 * guessed and checked like {@link UnchangedSinceEntry}; which value every way in gives it
 * is collected from the walk, as {@link Transitions} are. A state entered when the
 * program starts is left out: nothing was assigned on the way in.
 */
public final class CopiedOnEntry implements CandidateSource<Unchanged>, EntryCollector {

    private static final TermRenderer RENDERER = new TermRenderer();

    /** Per state, what every way in so far agrees each variable was given. */
    private final Map<String, Map<String, Term>> agreed = new LinkedHashMap<>();

    @Override
    public Set<ExtraInvariant.Kind> kinds() {
        return Set.of(ExtraInvariant.Kind.COPIED_ON_ENTRY);
    }

    @Override
    public List<Unchanged> guess(AnalysisContext context) {
        List<String> scalars = new ArrayList<>(context.tracked().keySet());
        scalars.retainAll(context.written());
        return UnchangedSinceEntry.guesses(context, scalars);
    }

    @Override
    public void entry(Entry entry, AnalysisContext context) {
        Map<String, Term> given = new LinkedHashMap<>();
        for (Fact fact : entry.facts()) {
            if (fact.kind() == FactKind.ASSIGNMENT) {
                given.put(fact.assigned(), fact.value());
            }
        }
        String key = key(entry.process(), entry.state());
        Map<String, Term> sofar = agreed.get(key);
        if (sofar == null) {
            agreed.put(key, given);
        } else {
            // Compared as written: two branches assigning the same value are different
            // nodes of the graph, but the same claim.
            sofar.entrySet().removeIf(e -> !given.containsKey(e.getKey())
                    || !RENDERER.render(e.getValue()).equals(RENDERER.render(given.get(e.getKey()))));
        }
    }

    @Override
    public void build(List<Unchanged> survivors, AnalysisContext context, ExtraInvariants into) {
        Term s = ExtraInvariant.STATE;
        for (String process : context.processes()) {
            for (String state : context.reachableDeclaredStates(process)) {
                if (process.equals(context.firstProcess()) && state.equals(context.firstState())) {
                    continue;
                }
                Map<String, Term> values = agreed.get(key(process, state));
                if (values == null) {
                    continue;
                }
                List<Term> copies = new ArrayList<>();
                List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
                List<String> names = new ArrayList<>();
                values.forEach((variable, value) -> {
                    if (!survivors.contains(new Unchanged(process, state, variable))
                            || UnchangedSinceEntry.definedAlready(into, process, state, variable)) {
                        return;
                    }
                    copies.add(new Term.Infix("=",
                            Terms.valueGetter(s, context.tracked().get(variable), variable, List.of()), value));
                    tags.add(Tag.variable(variable));
                    names.add(variable);
                });
                if (copies.isEmpty()) {
                    continue;
                }
                into.add(ExtraInvariant.wrapped(
                        AnalysisContext.uniqueName(into, "extra_copied_" + AnalysisContext.identifier(process)
                                + "_" + AnalysisContext.identifier(state)),
                        ExtraInvariant.Kind.COPIED_ON_ENTRY, AnnTranslator.Scale.PSTATE, process, state,
                        UnchangedSinceEntry.sinceEntry(process, Terms.conjunction(copies)),
                        String.join(", ", names) + " hold what they were given on the way into " + state),
                        tags);
            }
        }
    }

    private static String key(String process, String state) {
        return process + "\u0000" + state;
    }
}
