package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.preprocess.TypeEnvironment;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What every candidate source may read about the program: its processes and their states,
 * its variables, constants and clock, and - once the first stage has run - which states
 * each process can really be found in.
 *
 * <p>Built once per analysis and shared, so the sources agree on what a variable is and
 * which states count.
 */
public final class AnalysisContext {

    /** The state a formula names the one a transition was taken from. */
    public static final String TRANSITION_STATE = "s2";

    private final IrProgram program;
    private final Cfg cfg;
    private final ExpressionRendering rendering;
    private final List<String> processes = new ArrayList<>();
    private final long clock;
    private final Map<String, Value> constants = new LinkedHashMap<>();
    private final Map<String, IrType> types = new LinkedHashMap<>();
    private final Map<String, IrType> tracked = new LinkedHashMap<>();
    private final Set<String> written = new LinkedHashSet<>();
    private final Set<String> inputs = new LinkedHashSet<>();
    private final Map<String, Value> initialValues = new LinkedHashMap<>();
    private final Map<String, List<String>> reachable = new LinkedHashMap<>();

    public AnalysisContext(IrProgram program, Cfg cfg, ExpressionRendering rendering) {
        this.program = program;
        this.cfg = cfg;
        this.rendering = rendering;
        program.getProcesses().forEach(process -> processes.add(process.getName()));
        this.clock = clockTicks(program.getClock());
        program.inputVariables().forEach(input -> inputs.add(input.getName()));
        evaluateConstants();
        collectVariables();
        for (IrProcess process : program.getProcesses()) {
            reachable.put(process.getName(), allStates(process.getName()));
        }
    }

    // ------------------------------------------------------------------ the program

    public IrProgram program() {
        return program;
    }

    public Cfg cfg() {
        return cfg;
    }

    /** The processes, in declaration order - the order they run in a cycle. */
    public List<String> processes() {
        return Collections.unmodifiableList(processes);
    }

    /** How much {@code ltime} grows with each environment step. */
    public long clock() {
        return clock;
    }

    /** The process the program starts, and the state it starts it in; null if none. */
    public String firstProcess() {
        return processes.isEmpty() ? null : processes.get(0);
    }

    public String firstState() {
        IrProcess first = program.getProcesses().isEmpty() ? null : program.getProcesses().get(0);
        return first == null || first.getStartState() == null ? null : first.getStartState().getName();
    }

    /** The states the program declares for a process, in declaration order. */
    public List<String> declaredStates(String process) {
        IrProcess found = program.findProcess(process);
        List<String> states = new ArrayList<>();
        if (found != null) {
            found.getStates().forEach(state -> states.add(state.getName()));
        }
        return states;
    }

    /** The declared states, and the two every process has. */
    public List<String> allStates(String process) {
        List<String> states = declaredStates(process);
        states.add("stop");
        states.add("error");
        return states;
    }

    public IrState findState(String process, String state) {
        IrProcess found = program.findProcess(process);
        return found == null ? null : found.findState(state);
    }

    // ------------------------------------------------------------------ reachability

    /**
     * The states a process can be found in at a boundary - every state until the first
     * stage has settled which states each process is really found in.
     */
    public List<String> reachableStates(String process) {
        return reachable.getOrDefault(process, List.of());
    }

    /** The reachable states that are declared, which is what state invariants are about. */
    public List<String> reachableDeclaredStates(String process) {
        List<String> states = new ArrayList<>(reachableStates(process));
        states.retainAll(declaredStates(process));
        return states;
    }

    void setReachable(String process, List<String> states) {
        reachable.put(process, List.copyOf(states));
    }

    // ------------------------------------------------------------------ variables

    /** Declared constants that evaluate to a scalar, and their values. */
    public Map<String, Value> constants() {
        return Collections.unmodifiableMap(constants);
    }

    public boolean isConstant(String variable) {
        return constants.containsKey(variable);
    }

    /** A variable's type, or null when it is not known. */
    public IrType typeOf(String variable) {
        return types.get(variable);
    }

    /**
     * The scalar variables the program writes, and their types: bool, integer and time
     * ones, not inputs and not constants. These are the ones whose values are tracked.
     */
    public Map<String, IrType> tracked() {
        return Collections.unmodifiableMap(tracked);
    }

    /** Every variable some statement writes, whatever its type; inputs and constants not. */
    public Set<String> written() {
        return Collections.unmodifiableSet(written);
    }

    public boolean isInput(String variable) {
        return inputs.contains(variable);
    }

    /** A tracked variable's value at the program's first boundary, or null if not constant. */
    public Value initialValue(String variable) {
        return initialValues.get(variable);
    }

    void setInitialValue(String variable, Value value) {
        if (value == null) {
            initialValues.remove(variable);
        } else {
            initialValues.put(variable, value);
        }
    }

    // ------------------------------------------------------------------ terms

    /** A program expression read in the state {@code state} names. */
    public Term expression(IrExpr expression, String state) {
        return new Term.Expr(expression, state, rendering.expression(expression, state));
    }

    /**
     * A timeout's duration as a number, in the units {@code ltime} counts in, or null when
     * it is held in a variable that is not a constant.
     */
    public Long durationOf(TimeRef duration) {
        return switch (duration.getKind()) {
            case TIME_LITERAL -> IsabelleRenderer.parseTimeMillis(duration.getText());
            case INTEGER -> IsabelleRenderer.parseInteger(duration.getText());
            case NAME -> {
                Value value = constants.get(duration.getText());
                yield value == null || value.isBool() ? null : value.number().longValue();
            }
        };
    }

    /** A timeout's duration as a term read in {@code state}, as the renderer writes it. */
    public Term durationTerm(TimeRef duration, Term state) {
        if (duration.getKind() == TimeRef.Kind.NAME) {
            return Terms.valueGetter(state, IrType.TIME, duration.getText(), List.of());
        }
        return new Term.Var(Long.toString(durationOf(duration)));
    }

    /** A name Isabelle accepts as an identifier. */
    public static String identifier(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /** {@code base}, or {@code base_2}, {@code base_3}... whichever is free in {@code into}. */
    public static String uniqueName(ExtraInvariants into, String base) {
        String name = base;
        for (int i = 2; into.get(name) != null; i++) {
            name = base + "_" + i;
        }
        return name;
    }

    // ------------------------------------------------------------------ setup

    private void evaluateConstants() {
        List<IrDecl.Constant> declared = new ArrayList<>(program.getConstants());
        program.getNodes().forEach(node -> declared.addAll(node.getConstants()));
        // In declaration order, so a constant defined in terms of an earlier one resolves.
        AbstractCycle nothingKnown = new AbstractCycle(this, true);
        for (IrDecl.Constant constant : declared) {
            Value value = nothingKnown.evaluate(constant.getValue());
            if (value != null && isScalar(constant.getType())) {
                constants.put(constant.getName(), AbstractCycle.convert(value,
                        value.isBool() ? Terms.Sort.BOOL : Terms.Sort.INT, Terms.sortOf(constant.getType())));
            }
        }
    }

    private void collectVariables() {
        TypeEnvironment environment = new TypeEnvironment(program);
        for (String name : environment.variableNames()) {
            types.put(name, environment.resolve(environment.variableType(name)));
        }
        // Variables local to a state are not in the environment; their writes carry the type.
        for (CfgNode node : cfg.nodes()) {
            if (node instanceof CfgNode.Assign assign) {
                String name = assign.getTarget().getName();
                if (assign.getTarget().getAccesses().isEmpty()) {
                    types.putIfAbsent(name, assign.getTarget().getResultType());
                }
                if (!inputs.contains(name) && !constants.containsKey(name)) {
                    written.add(name);
                }
            }
        }
        types.forEach((name, type) -> {
            if (isScalar(type) && !inputs.contains(name) && !isDeclaredConstant(name)) {
                tracked.put(name, type);
            }
        });
    }

    private boolean isDeclaredConstant(String name) {
        return program.getConstants().stream().anyMatch(c -> c.getName().equals(name))
                || program.getNodes().stream().anyMatch(
                        node -> node.getConstants().stream().anyMatch(c -> c.getName().equals(name)));
    }

    static boolean isScalar(IrType type) {
        if (!(type instanceof IrType.Builtin builtin)) {
            return false;
        }
        return switch (builtin.kind()) {
            case VOID, FLOAT, DOUBLE -> false;
            default -> true;
        };
    }

    private static long clockTicks(TimeRef clock) {
        return clock.getKind() == TimeRef.Kind.TIME_LITERAL
                ? IsabelleRenderer.parseTimeMillis(clock.getText())
                : IsabelleRenderer.parseInteger(clock.getText());
    }
}
