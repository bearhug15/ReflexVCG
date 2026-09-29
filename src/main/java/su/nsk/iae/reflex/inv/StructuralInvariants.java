package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Derives extra invariants from a program: the candidate sources the {@link Selection}
 * asks for, run through one {@link InvariantChecker}, in three stages.
 *
 * <ol>
 *   <li><b>The high priority, alone.</b> The states each process can be in go into the
 *       global invariant, which every condition assumes and every cycle proves; so they
 *       must be inductive by themselves, resting on nothing that is not in there with
 *       them. They are settled first, assuming nothing else, and what they leave
 *       reachable is what the other sources guess about.</li>
 *   <li><b>Everything else that guesses</b>, checked together, taking the first stage as
 *       given. Whatever survives is inductive relative to the first stage and itself - which
 *       is exactly what the obligation emitted per cycle proves.</li>
 *   <li><b>What is collected</b> rather than guessed - transitions, copied values - from
 *       one more walk that assumes everything settled before it.</li>
 * </ol>
 *
 * <p>Sources added with {@link #addSource} join the second stage, and a source never runs
 * unless the selection asks for one of its kinds, or it was added by hand.
 */
public final class StructuralInvariants {

    private final IrProgram program;
    private final Cfg cfg;
    private final ExpressionRendering rendering;
    private final List<CandidateSource<?>> custom = new ArrayList<>();
    private final List<String> diagnostics = new ArrayList<>();
    private AnalysisContext context;

    public StructuralInvariants(IrProgram program, Cfg cfg, ExpressionRendering rendering) {
        this.program = program;
        this.cfg = cfg;
        this.rendering = rendering;
    }

    /** Adds a source of candidates, run whatever the selection says. */
    public StructuralInvariants addSource(CandidateSource<?> source) {
        custom.add(source);
        return this;
    }

    /** What the analysis found worth reporting: static-analysis claims it could not confirm. */
    public List<String> getDiagnostics() {
        return List.copyOf(diagnostics);
    }

    /** The context the last run used, or null before one. */
    public AnalysisContext getContext() {
        return context;
    }

    public ExtraInvariants generate(Selection selection) {
        ExtraInvariants invariants = new ExtraInvariants();
        generate(selection, invariants);
        return invariants;
    }

    /** Runs the analysis, adding what it finds to {@code into}. */
    public void generate(Selection selection, ExtraInvariants into) {
        diagnostics.clear();
        if (selection.isEmpty() && custom.isEmpty()) {
            return;
        }
        context = new AnalysisContext(program, cfg, rendering);
        InvariantChecker checker = new InvariantChecker(context);
        AbstractCycle start = checker.startOfProgram();
        context.tracked().keySet().forEach(variable -> context.setInitialValue(variable, start.value(variable)));

        // Stage 1: where each process can be.
        ProcessStates states = new ProcessStates();
        List<Candidate> settled = new ArrayList<>();
        List<ProcessStates.NotIn> never = List.of();
        if (selection.includes(ExtraInvariant.Kind.PROCESS_STATES)) {
            never = checker.houdini(states.guess(context), List.of());
            for (String process : context.processes()) {
                context.setReachable(process, ProcessStates.reachable(process, never, context));
            }
            settled.addAll(never);
        }

        // Stage 2: every other source that guesses, together.
        List<Run<?>> runs = new ArrayList<>();
        sourcesFor(selection).forEach(source -> runs.add(run(source, context)));
        List<Candidate> guessed = new ArrayList<>();
        runs.forEach(run -> guessed.addAll(run.guesses));
        Set<Candidate> survivors = new LinkedHashSet<>(checker.houdini(guessed, settled));

        // Stage 3: what is collected from the ways into states.
        List<EntryCollector> collectors = new ArrayList<>();
        runs.forEach(run -> {
            if (run.source instanceof EntryCollector collector) {
                collectors.add(collector);
            }
        });
        if (!collectors.isEmpty()) {
            List<Candidate> assumed = new ArrayList<>(settled);
            assumed.addAll(survivors);
            checker.collect(assumed, collectors);
        }

        if (selection.includes(ExtraInvariant.Kind.PROCESS_STATES)) {
            states.build(never, context, into);
        }
        runs.forEach(run -> run.build(survivors, into));
        runs.forEach(run -> {
            if (run.source instanceof StaticAnalysisClaims claims) {
                claims.unconfirmed().forEach(claim -> diagnostics.add("static analysis " + claim
                        + " - not confirmed by the extra-invariant check"));
            }
        });
    }

    /** The sources the selection asks for, in the order their invariants are written. */
    private List<CandidateSource<?>> sourcesFor(Selection selection) {
        List<CandidateSource<?>> sources = new ArrayList<>();
        if (selection.includes(ExtraInvariant.Kind.PROCESS_PAIRS)) {
            sources.add(new ProcessPairs());
        }
        if (selection.includes(ExtraInvariant.Kind.STATIC_ANALYSIS)) {
            sources.add(new StaticAnalysisClaims());
        }
        Set<ExtraInvariant.Kind> values = new LinkedHashSet<>();
        if (selection.includes(ExtraInvariant.Kind.DEFINED_VARIABLES)) {
            values.add(ExtraInvariant.Kind.DEFINED_VARIABLES);
        }
        if (selection.includes(ExtraInvariant.Kind.STABILIZED_VARIABLES)) {
            values.add(ExtraInvariant.Kind.STABILIZED_VARIABLES);
        }
        if (!values.isEmpty()) {
            sources.add(new VariableValues(values));
        }
        // After the values: a variable they already pin is not restated.
        if (selection.includes(ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY)) {
            sources.add(new UnchangedSinceEntry());
        }
        if (selection.includes(ExtraInvariant.Kind.TIMER_BOUNDS)) {
            sources.add(new TimerBounds());
        }
        if (selection.includes(ExtraInvariant.Kind.TRANSITION)) {
            sources.add(new Transitions());
        }
        if (selection.includes(ExtraInvariant.Kind.COPIED_ON_ENTRY)) {
            sources.add(new CopiedOnEntry());
        }
        sources.addAll(custom);
        return sources;
    }

    private static <C extends Candidate> Run<C> run(CandidateSource<C> source, AnalysisContext context) {
        return new Run<>(source, context);
    }

    /** One source's guesses, kept with it so its survivors go back to it. */
    private static final class Run<C extends Candidate> {
        private final CandidateSource<C> source;
        private final AnalysisContext context;
        private final List<C> guesses;

        Run(CandidateSource<C> source, AnalysisContext context) {
            this.source = source;
            this.context = context;
            this.guesses = source.guess(context);
        }

        void build(Set<Candidate> survivors, ExtraInvariants into) {
            List<C> kept = new ArrayList<>();
            for (C guess : guesses) {
                if (survivors.contains(guess)) {
                    kept.add(guess);
                }
            }
            source.build(kept, context, into);
        }
    }
}
