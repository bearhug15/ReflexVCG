package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.inv.AbstractCycle.Const;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.preprocess.TypeEnvironment;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Derives the structural invariants of mainOverview.tex, "Extra Invariants", from the
 * program's control-flow graph.
 *
 * <p>Every one of them is a claim about all reachable boundaries, and only true of those:
 * an arbitrary state can have a process in any state and a variable at any value. So they
 * are found the way an inductive invariant has to be - by guessing and checking:
 *
 * <ol>
 *   <li>Guess. For every process and state, that the process is never found there; for
 *       every process state, variable and value the variable is ever given, that the
 *       variable holds that value whenever the process is in that state (<em>defined</em>),
 *       and whenever it has been there for two boundaries running (<em>stabilized</em>).</li>
 *   <li>Keep what holds at the program's first boundary.</li>
 *   <li>Walk every path of one cycle, assuming everything still kept held where it began,
 *       and drop whatever fails where it ends. Repeat until nothing is dropped: what is
 *       left then holds at the start, and is carried through every cycle by itself.</li>
 * </ol>
 *
 * <p>This is the "tracing of program paths" the overview asks for. The walk is abstract -
 * a variable is a known constant or unknown - but it follows the graph the conditions are
 * generated from, so a path it keeps is one a condition is generated for, and the
 * invariants it finds are meant to be provable from those conditions. Each one is still
 * proved: the generator emits an obligation per path saying so.
 *
 * <p>Transition conditions are not guessed. Once the rest are fixed, one more walk collects,
 * at every {@code set state} that actually changes a process's state, the guards that
 * still hold just before it; each becomes one way into the target state.
 */
public final class StructuralInvariants {

    /** The name of the state a transition was taken from, in a transition condition. */
    static final String TRANSITION_STATE = "s2";

    /** A cycle with more paths than this is not analysed; conditions are not either. */
    private static final int MAX_PATHS = 1_000_000;

    private static final Term S = ExtraInvariant.STATE;
    private static final Term S1 = new Term.Var("s1");
    private static final Term S2 = new Term.Var(TRANSITION_STATE);
    private static final Term S3 = new Term.Var("s3");

    private final IrProgram program;
    private final Cfg cfg;
    private final ExpressionRendering rendering;

    private final List<String> processes = new ArrayList<>();
    /** Declared constants and the values they are declared with. */
    private final Map<String, Const> constants = new LinkedHashMap<>();
    /** The variables whose values are tracked, and their types. */
    private final Map<String, IrType> tracked = new LinkedHashMap<>();
    /** What each loop's body writes and moves, read off the body's graph. */
    private final Map<CfgNode.LoopCut, LoopEffects> loopEffects = new LinkedHashMap<>();

    /** Candidates still standing: a process is never found in a state. */
    private final Set<NotIn> notIn = new LinkedHashSet<>();
    /** Candidates still standing: a variable's value while a process is in a state. */
    private final Set<VarFact> varFacts = new LinkedHashSet<>();

    private boolean collecting;
    private final Map<String, Set<Disjunct>> entries = new LinkedHashMap<>();
    private final Set<String> trivialEntries = new LinkedHashSet<>();
    private final Set<CfgNode.LoopCut> walkedBodies = new LinkedHashSet<>();
    private int paths;

    private record NotIn(String process, String state) {
    }

    private record VarFact(ExtraInvariant.Kind kind, String process, String state,
                           String variable, Const value) {
    }

    private record LoopEffects(Set<String> written, Set<String> moved) {
    }

    private record Disjunct(Term term, ExtraInvariant.Transition kind) {
    }

    public StructuralInvariants(IrProgram program, Cfg cfg, ExpressionRendering rendering) {
        this.program = program;
        this.cfg = cfg;
        this.rendering = rendering;
    }

    /** Runs the analysis and returns what it found, tagged. */
    public ExtraInvariants generate() {
        program.getProcesses().forEach(process -> processes.add(process.getName()));
        evaluateConstants();
        collectTrackedVariables();
        guess();
        dropWhatFailsAtStart();
        while (dropWhatACycleBreaks()) {
            // Each round assumes less than the one before, so this ends.
        }
        collectTransitions();
        return build();
    }

    // ------------------------------------------------------------------ setup

    private void evaluateConstants() {
        List<IrDecl.Constant> declared = new ArrayList<>(program.getConstants());
        program.getNodes().forEach(node -> declared.addAll(node.getConstants()));
        // In declaration order, so a constant defined in terms of an earlier one resolves.
        AbstractCycle nothingKnown = new AbstractCycle(true);
        for (IrDecl.Constant constant : declared) {
            Const value = nothingKnown.evaluate(constant.getValue(), constants);
            if (value != null && isScalar(constant.getType())) {
                constants.put(constant.getName(),
                        AbstractCycle.convert(value, sortOfValue(value), Terms.sortOf(constant.getType())));
            }
        }
    }

    /**
     * Scalars the program itself writes. Inputs are left out - the environment rewrites
     * them every cycle - and so are constants, which the global invariant already pins.
     */
    private void collectTrackedVariables() {
        TypeEnvironment environment = new TypeEnvironment(program);
        Set<String> inputs = new LinkedHashSet<>();
        program.inputVariables().forEach(input -> inputs.add(input.getName()));
        Set<String> constantNames = new LinkedHashSet<>();
        program.getConstants().forEach(c -> constantNames.add(c.getName()));
        program.getNodes().forEach(node -> node.getConstants().forEach(c -> constantNames.add(c.getName())));

        Map<String, IrType> candidates = new LinkedHashMap<>();
        for (String name : environment.variableNames()) {
            candidates.put(name, environment.resolve(environment.variableType(name)));
        }
        // Variables local to a state are not in the environment; their writes carry the type.
        for (CfgNode node : cfg.nodes()) {
            if (node instanceof CfgNode.Assign assign && assign.getTarget().getAccesses().isEmpty()) {
                candidates.putIfAbsent(assign.getTarget().getName(), assign.getTarget().getResultType());
            }
        }
        candidates.forEach((name, type) -> {
            if (isScalar(type) && !inputs.contains(name) && !constantNames.contains(name)) {
                tracked.put(name, type);
            }
        });
    }

    private static boolean isScalar(IrType type) {
        if (!(type instanceof IrType.Builtin builtin)) {
            return false;
        }
        return switch (builtin.kind()) {
            case VOID, FLOAT, DOUBLE -> false;
            default -> true;
        };
    }

    private static Terms.Sort sortOfValue(Const value) {
        return value.isBool() ? Terms.Sort.BOOL : Terms.Sort.INT;
    }

    // ------------------------------------------------------------------ guessing

    private void guess() {
        Map<String, Set<Const>> values = new LinkedHashMap<>();
        tracked.keySet().forEach(v -> values.put(v, new LinkedHashSet<>()));

        AbstractCycle start = startOfProgram();
        tracked.keySet().forEach(v -> {
            Const initial = start.valueOf(v, constants);
            if (initial != null) {
                values.get(v).add(initial);
            }
        });
        // Every constant the program ever writes into the variable.
        AbstractCycle nothingKnown = new AbstractCycle(true);
        for (CfgNode node : cfg.nodes()) {
            if (node instanceof CfgNode.Assign assign
                    && assign.getTarget().getAccesses().isEmpty()
                    && values.containsKey(assign.getTarget().getName())) {
                Const written = nothingKnown.evaluate(assign.getValue(), constants);
                if (written != null) {
                    values.get(assign.getTarget().getName()).add(written);
                }
            }
        }

        for (IrProcess process : program.getProcesses()) {
            for (String state : statesOf(process)) {
                notIn.add(new NotIn(process.getName(), state));
            }
            for (IrState state : process.getStates()) {
                values.forEach((variable, possible) -> {
                    for (Const value : possible) {
                        varFacts.add(new VarFact(ExtraInvariant.Kind.DEFINED_VARIABLES,
                                process.getName(), state.getName(), variable, value));
                        varFacts.add(new VarFact(ExtraInvariant.Kind.STABILIZED_VARIABLES,
                                process.getName(), state.getName(), variable, value));
                    }
                });
            }
        }
    }

    /** The declared states of a process, and the two every process has. */
    private static List<String> statesOf(IrProcess process) {
        List<String> states = new ArrayList<>();
        process.getStates().forEach(state -> states.add(state.getName()));
        states.add("stop");
        states.add("error");
        return states;
    }

    /**
     * The first boundary, as the base case builds it: every variable at its declared
     * initial value or its type's default (what reading a variable never written gives),
     * the first process in its first state and every other stopped. Before it lies only
     * the empty state, where every process reads as stopped.
     *
     * <p>Mirrors {@link su.nsk.iae.reflex.vc.InitialCondition}; the two must agree.
     */
    private AbstractCycle startOfProgram() {
        AbstractCycle start = new AbstractCycle(false);
        for (String process : processes) {
            start.setPstate(process, "stop");
        }
        start.boundaryPassed(processes);
        IrProcess first = program.getProcesses().isEmpty() ? null : program.getProcesses().get(0);
        if (first != null && first.getStartState() != null) {
            start.setPstate(first.getName(), first.getStartState().getName());
        }

        tracked.forEach((variable, type) -> start.setValue(variable,
                Terms.sortOf(type) == Terms.Sort.BOOL ? Const.of(false) : Const.of(BigInteger.ZERO)));
        List<IrDecl> initialised = new ArrayList<>(program.getGlobalVariables());
        program.getNodes().forEach(node -> initialised.addAll(node.getVariables()));
        for (IrDecl declaration : initialised) {
            if (declaration instanceof IrDecl.Variable variable
                    && variable.getInitializer() != null
                    && tracked.containsKey(variable.getName())) {
                Const value = start.evaluate(variable.getInitializer(), constants);
                start.setValue(variable.getName(), value == null ? null
                        : AbstractCycle.convert(value, sortOfValue(value), Terms.sortOf(variable.getType())));
            }
        }
        return start;
    }

    // ------------------------------------------------------------------ checking

    private void dropWhatFailsAtStart() {
        AbstractCycle start = startOfProgram();
        notIn.removeIf(candidate -> fails(candidate, start));
        varFacts.removeIf(candidate -> fails(candidate, start, false));
    }

    /** One round: walks every path assuming what stands. Returns whether anything fell. */
    private boolean dropWhatACycleBreaks() {
        Set<NotIn> failedStates = new LinkedHashSet<>();
        Set<VarFact> failedFacts = new LinkedHashSet<>();
        paths = 0;
        walk(cfg.getEntry(), new AbstractCycle(false), (cycle, insideLoop) -> {
            for (NotIn candidate : notIn) {
                if (fails(candidate, cycle)) {
                    failedStates.add(candidate);
                }
            }
            for (VarFact candidate : varFacts) {
                if (fails(candidate, cycle, insideLoop)) {
                    failedFacts.add(candidate);
                }
            }
        });
        notIn.removeAll(failedStates);
        varFacts.removeAll(failedFacts);
        return !failedStates.isEmpty() || !failedFacts.isEmpty();
    }

    private static boolean fails(NotIn candidate, AbstractCycle at) {
        String state = at.pstateOf(candidate.process());
        return state == null || state.equals(candidate.state());
    }

    /**
     * Whether a boundary may break a fact about a variable.
     *
     * @param insideLoop the boundary ends an iteration of a loop, where the state the
     *                   process was in at the boundary before is not tracked
     */
    private boolean fails(VarFact candidate, AbstractCycle at, boolean insideLoop) {
        String state = at.pstateOf(candidate.process());
        if (state != null && !state.equals(candidate.state())) {
            return false;
        }
        if (candidate.kind() == ExtraInvariant.Kind.STABILIZED_VARIABLES && !insideLoop) {
            String before = at.boundaryPstateOf(candidate.process());
            if (before != null && !before.equals(candidate.state())) {
                return false;
            }
        }
        return !candidate.value().equals(at.valueOf(candidate.variable(), constants));
    }

    // ------------------------------------------------------------------ walking

    private interface Boundaries {
        /** A boundary is reached: the end of the cycle, or an iteration of a loop. */
        void reached(AbstractCycle cycle, boolean insideLoop);
    }

    private void walk(CfgNode node, AbstractCycle cycle, Boundaries boundaries) {
        step(node, cycle, boundaries);
        if (cycle.isImpossible()) {
            return;
        }
        List<CfgNode> successors = node.getSuccessors();
        if (successors.isEmpty()) {
            if (++paths > MAX_PATHS) {
                throw new IllegalStateException("extra invariant analysis exceeded "
                        + MAX_PATHS + " paths through one cycle");
            }
            if (collecting) {
                cycle.pending().forEach(entry -> recordEntry(entry, cycle));
            }
            return;
        }
        // The last successor takes the state itself; every other a copy.
        for (int i = 0; i < successors.size(); i++) {
            walk(successors.get(i), i == successors.size() - 1 ? cycle : cycle.copy(), boundaries);
        }
    }

    private void step(CfgNode node, AbstractCycle cycle, Boundaries boundaries) {
        if (node instanceof CfgNode.InState inState) {
            String process = inState.getProcess();
            if (!cycle.enter(process, inState.getState(), state -> assumeAtStart(cycle, process, state))) {
                cycle.markImpossible();
            }
        } else if (node instanceof CfgNode.Guard guard) {
            IrExpr condition = guard.getCondition();
            Const value = cycle.evaluate(condition, constants);
            if (value != null && !value.truth()) {
                cycle.markImpossible();
            } else if (collecting && value == null && AbstractCycle.isPure(condition)) {
                cycle.addFact(new AbstractCycle.Fact(
                        new Term.Raw(rendering.expression(condition, TRANSITION_STATE)),
                        AbstractCycle.variablesRead(condition),
                        AbstractCycle.processesRead(condition), null, false));
            }
        } else if (node instanceof CfgNode.TimeoutGuard timeout) {
            if (collecting) {
                Term elapsed = Terms.localTime(S2, timeout.getProcess());
                Term duration = new Term.Raw(rendering.duration(timeout.getDuration(), TRANSITION_STATE));
                cycle.addFact(new AbstractCycle.Fact(
                        new Term.Infix(timeout.isExceeded() ? "\\<ge>" : "<", elapsed, duration),
                        timeout.getDuration().isName() ? Set.of(timeout.getDuration().getText()) : Set.of(),
                        Set.of(), timeout.getProcess(), timeout.isExceeded()));
            }
        } else if (node instanceof CfgNode.Assign assign) {
            IrExpr.VarRef target = assign.getTarget();
            Const value = target.getAccesses().isEmpty()
                    ? cycle.evaluate(assign.getValue(), constants) : null;
            cycle.setValue(target.getName(), value);
        } else if (node instanceof CfgNode.InputChoice input) {
            cycle.setValue(input.getVariable(), null);
        } else if (node instanceof CfgNode.SetState setState) {
            if (collecting) {
                cycle.recordEntry(setState.getProcess(), setState.getState());
            }
            cycle.setPstate(setState.getProcess(), setState.getState());
        } else if (node instanceof CfgNode.ResetTimer reset) {
            cycle.resetTimer(reset.getProcess());
        } else if (node instanceof CfgNode.ToEnv) {
            boundaries.reached(cycle, false);
            cycle.timePasses();
            cycle.boundaryPassed(processes);
        } else if (node instanceof CfgNode.LoopCut cut) {
            // Past the loop is known only what its body leaves alone - and every iteration
            // ends in a boundary, each of which has to satisfy the invariants as well.
            LoopEffects effects = effectsOf(cut);
            effects.written().forEach(variable -> cycle.setValue(variable, null));
            effects.moved().forEach(process -> cycle.setPstate(process, null));
            cycle.timePasses();
            boundaries.reached(cycle, true);
            cycle.boundaryPassed(processes);
            if (collecting && walkedBodies.add(cut)) {
                // A set state inside the body is a way into its target too. The body
                // starts mid-cycle, where nothing is known.
                AbstractCycle body = new AbstractCycle(true);
                body.setExecuting(cycle.executing());
                walk(cut.getBodyEntry(), body, (c, l) -> { });
            }
        }
        // Joins, checks, the entry and the exit change nothing.
    }

    /** Brings in what the hypothesis says about a process's starting state. */
    private boolean assumeAtStart(AbstractCycle cycle, String process, String state) {
        if (notIn.contains(new NotIn(process, state))) {
            return false;
        }
        for (VarFact fact : varFacts) {
            if (fact.kind() == ExtraInvariant.Kind.DEFINED_VARIABLES
                    && fact.process().equals(process) && fact.state().equals(state)
                    && !cycle.assumeAtStart(fact.variable(), fact.value())) {
                return false;
            }
        }
        return true;
    }

    private LoopEffects effectsOf(CfgNode.LoopCut cut) {
        return loopEffects.computeIfAbsent(cut, c -> {
            Set<String> written = new LinkedHashSet<>();
            Set<String> moved = new LinkedHashSet<>();
            Set<CfgNode> seen = new LinkedHashSet<>();
            Deque<CfgNode> pending = new ArrayDeque<>();
            pending.add(c.getBodyEntry());
            while (!pending.isEmpty()) {
                CfgNode node = pending.pop();
                if (!seen.add(node)) {
                    continue;
                }
                if (node instanceof CfgNode.Assign assign) {
                    written.add(assign.getTarget().getName());
                } else if (node instanceof CfgNode.SetState setState) {
                    moved.add(setState.getProcess());
                } else if (node instanceof CfgNode.LoopCut inner) {
                    pending.add(inner.getBodyEntry());
                }
                pending.addAll(node.getSuccessors());
            }
            return new LoopEffects(written, moved);
        });
    }

    // ------------------------------------------------------------------ transitions

    private void collectTransitions() {
        IrProcess first = program.getProcesses().isEmpty() ? null : program.getProcesses().get(0);
        if (first != null && first.getStartState() != null) {
            // Entered when the program started: nothing before the transition was a boundary.
            Term beforeAnyBoundary = Terms.forall(S3,
                    Terms.implication(Terms.substate(S3, S2), Terms.not(Terms.toEnvP(S3))));
            entriesOf(first.getName(), first.getStartState().getName())
                    .add(new Disjunct(beforeAnyBoundary, ExtraInvariant.Transition.INITIAL));
        }
        collecting = true;
        paths = 0;
        walk(cfg.getEntry(), new AbstractCycle(false), (cycle, insideLoop) -> { });
        collecting = false;
    }

    /**
     * One {@code set state} on a path that turned out possible: a way into its target,
     * unless the process was already there, in which case the state did not change and
     * the transition is not the one that brought it there.
     */
    private void recordEntry(AbstractCycle.PendingEntry entry, AbstractCycle cycle) {
        IrProcess target = program.findProcess(entry.process());
        if (target == null || target.findState(entry.state()) == null) {
            return;
        }
        String before = cycle.resolve(entry.before());
        if (entry.state().equals(before)) {
            return;
        }
        List<Term> parts = new ArrayList<>();
        boolean timed = false;
        for (AbstractCycle.Fact fact : entry.facts()) {
            parts.add(fact.term());
            timed |= fact.timed();
        }
        String executing = entry.executing();
        String executingState = executing == null ? null : cycle.resolve(entry.executingState());
        if (executingState != null) {
            parts.add(Terms.pstateCompare(S2, executing, executingState));
        }
        if (before != null && !entry.process().equals(executing)) {
            parts.add(Terms.pstateCompare(S2, entry.process(), before));
        }
        String key = key(entry.process(), entry.state());
        if (parts.isEmpty()) {
            // A way in that asks for nothing: the invariant would say nothing either.
            trivialEntries.add(key);
            return;
        }
        entriesOf(entry.process(), entry.state()).add(new Disjunct(Terms.conjunction(parts),
                timed ? ExtraInvariant.Transition.TIMED : ExtraInvariant.Transition.CONDITIONAL));
    }

    private Set<Disjunct> entriesOf(String process, String state) {
        return entries.computeIfAbsent(key(process, state), k -> new LinkedHashSet<>());
    }

    private static String key(String process, String state) {
        return process + "\u0000" + state;
    }

    // ------------------------------------------------------------------ building

    private ExtraInvariants build() {
        ExtraInvariants invariants = new ExtraInvariants();
        for (IrProcess process : program.getProcesses()) {
            String name = process.getName();
            List<String> reachable = new ArrayList<>();
            for (String state : statesOf(process)) {
                if (!notIn.contains(new NotIn(name, state))) {
                    reachable.add(state);
                }
            }
            addProcessStates(invariants, name, reachable);
            for (IrState state : process.getStates()) {
                if (reachable.contains(state.getName())) {
                    addVariables(invariants, name, state.getName(), ExtraInvariant.Kind.DEFINED_VARIABLES);
                    addVariables(invariants, name, state.getName(), ExtraInvariant.Kind.STABILIZED_VARIABLES);
                    addTransition(invariants, name, state.getName());
                }
            }
        }
        return invariants;
    }

    /**
     * {@code \<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow> claim}: the claim
     * holds at every boundary at or below s.
     *
     * <p>Every extra invariant has exactly this shape, whatever it claims, so a single
     * lemma - {@code boundaries_step} in ReflexLemmas - carries any of them across a
     * cycle: it holds at the cycle's end if it held up to the cycle's start and the claim
     * holds at the end itself.
     */
    private static Term atEveryBoundary(Term claim) {
        return Terms.forall(S1, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(S1), Terms.substate(S1, S))), claim));
    }

    private void addProcessStates(ExtraInvariants invariants, String process, List<String> reachable) {
        List<Term> options = new ArrayList<>();
        reachable.forEach(state -> options.add(Terms.pstateCompare(S1, process, state)));
        Term formula = atEveryBoundary(Terms.disjunction(options));
        invariants.add(new ExtraInvariant(uniqueName(invariants, "extra_states_" + identifier(process)),
                        ExtraInvariant.Kind.PROCESS_STATES, formula,
                        process + " is only ever found in " + String.join(", ", reachable)),
                Tag.process(process));
    }

    private void addVariables(ExtraInvariants invariants, String process, String state,
                              ExtraInvariant.Kind kind) {
        if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES && neverStabilizes(process, state)) {
            return;
        }
        List<Term> values = new ArrayList<>();
        List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
        List<String> described = new ArrayList<>();
        for (VarFact fact : varFacts) {
            if (fact.kind() != kind || !fact.process().equals(process) || !fact.state().equals(state)) {
                continue;
            }
            if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES && varFacts.contains(new VarFact(
                    ExtraInvariant.Kind.DEFINED_VARIABLES, process, state, fact.variable(), fact.value()))) {
                // Holds from the moment the state is entered, so already said.
                continue;
            }
            values.add(equals(fact.variable(), fact.value()));
            tags.add(Tag.variable(fact.variable()));
            described.add(fact.variable() + " = " + fact.value());
        }
        if (values.isEmpty()) {
            return;
        }
        List<Term> premises = new ArrayList<>(List.of(Terms.pstateCompare(S1, process, state)));
        String prefix = "extra_vars_";
        String when = "whenever " + process + " is in " + state;
        if (kind == ExtraInvariant.Kind.STABILIZED_VARIABLES) {
            premises.add(Terms.pstateCompare(new Term.App("predEnv", List.of(S1)), process, state));
            prefix = "extra_stable_";
            when = "once " + process + " has been in " + state + " for two boundaries running";
        }
        Term formula = atEveryBoundary(Terms.implication(Terms.conjunction(premises),
                Terms.conjunction(values)));
        invariants.add(new ExtraInvariant(
                uniqueName(invariants, prefix + identifier(process) + "_" + identifier(state)),
                kind, formula, String.join(", ", described) + " " + when), tags);
    }

    /**
     * Whether the facts left standing for a process staying in a state contradict each
     * other, or what holds on entering it: then it never stays there two boundaries
     * running, and every value survived only because nothing ever tested it. True, but
     * saying nothing, so it is left out. Dropping it costs nothing: stabilized facts are
     * never assumed by the analysis, so nothing else rests on them.
     */
    private boolean neverStabilizes(String process, String state) {
        Map<String, Const> seen = new LinkedHashMap<>();
        for (VarFact fact : varFacts) {
            if (!fact.process().equals(process) || !fact.state().equals(state)) {
                continue;
            }
            Const previous = seen.putIfAbsent(fact.variable(), fact.value());
            if (previous != null && !previous.equals(fact.value())) {
                return true;
            }
        }
        return false;
    }

    private Term equals(String variable, Const value) {
        Term read = Terms.valueGetter(S1, tracked.get(variable), variable, List.of());
        if (value.isBool()) {
            return value.truth() ? read : Terms.not(read);
        }
        return new Term.Infix("=", read, value.term());
    }

    private void addTransition(ExtraInvariants invariants, String process, String state) {
        String key = key(process, state);
        Set<Disjunct> ways = entries.get(key);
        if (ways == null || ways.isEmpty() || trivialEntries.contains(key)) {
            return;
        }
        List<Term> disjuncts = new ArrayList<>();
        Set<ExtraInvariant.Transition> kinds = new LinkedHashSet<>();
        for (Disjunct way : ways) {
            disjuncts.add(way.term());
            kinds.add(way.kind());
        }
        Term entered = new Term.Let(TRANSITION_STATE,
                new Term.App("prevProcState", List.of(S1, new Term.Quoted(process))),
                Terms.disjunction(disjuncts));
        Term formula = atEveryBoundary(Terms.implication(
                Terms.pstateCompare(S1, process, state), entered));
        List<Tag> tags = new ArrayList<>(List.of(Tag.process(process), Tag.state(process, state)));
        kinds.forEach(kind -> tags.add(Tag.transition(kind)));
        invariants.add(new ExtraInvariant(
                uniqueName(invariants, "extra_trans_" + identifier(process) + "_" + identifier(state)),
                ExtraInvariant.Kind.TRANSITION, formula,
                process + " can be in " + state + " only by " + ways.size() + " way(s) in"), tags);
    }

    /** A name Isabelle accepts as an identifier. */
    private static String identifier(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String uniqueName(ExtraInvariants invariants, String base) {
        String name = base;
        for (int i = 2; invariants.get(name) != null; i++) {
            name = base + "_" + i;
        }
        return name;
    }
}
