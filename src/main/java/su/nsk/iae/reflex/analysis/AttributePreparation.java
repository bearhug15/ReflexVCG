package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes the attributes of every construct, bottom up - {@code attributePrepare} of
 * StaticalAnalysis.tex.
 *
 * <p>Runs on canonical IR rather than on the source tree, which is a deliberate
 * difference: by this point switch fall-through has been expanded and {@code wait} and
 * {@code slice} have become ordinary states, so a case's attributes cover the statements
 * it really executes.
 *
 * <p>Results are keyed by identity, since two distinct statements can be equal.
 */
public final class AttributePreparation {

    private final Map<IrNode, Attributes> attributes = new IdentityHashMap<>();
    private final IrProgram program;

    private String currentProcess;
    private String currentState;

    public AttributePreparation(IrProgram program) {
        this.program = program;
    }

    /** Prepares the whole program and returns the attributes of each construct. */
    public Map<IrNode, Attributes> run() {
        for (IrProcess process : program.getProcesses()) {
            currentProcess = process.getName();
            List<Attributes> states = new ArrayList<>();
            for (IrState state : process.getStates()) {
                currentState = state.getName();
                states.add(prepareState(state));
            }
            attributes.put(process, AttributeCalculus.par(states));
        }
        currentProcess = null;
        currentState = null;
        return attributes;
    }

    public Map<IrNode, Attributes> getAttributes() {
        return attributes;
    }

    public Attributes of(IrNode node) {
        return attributes.getOrDefault(node, Attributes.EMPTY);
    }

    private Attributes prepareState(IrState state) {
        List<Attributes> parts = new ArrayList<>();
        for (IrStmt statement : state.getStatements()) {
            parts.add(prepare(statement));
        }
        Attributes result = AttributeCalculus.cons(parts);
        if (state.getTimeout() != null) {
            // The timeout body may or may not run.
            Attributes timeout = AttributeCalculus.optional(prepare(state.getTimeout().getBody()));
            attributes.put(state.getTimeout(), timeout);
            result = AttributeCalculus.cons(result, timeout);
        }
        attributes.put(state, result);
        return result;
    }

    // ------------------------------------------------------------------ statements

    private Attributes prepare(IrStmt statement) {
        Attributes result = compute(statement);
        if (statement != null) {
            attributes.put(statement, result);
        }
        return result;
    }

    private Attributes compute(IrStmt statement) {
        if (statement == null) {
            return Attributes.EMPTY;
        }
        if (statement instanceof IrStmt.ResetTimer) {
            return Attributes.justReset();
        }
        if (statement instanceof IrStmt.SetState setState) {
            return Attributes.of(Map.of(), Set.of(), true, true,
                    Set.of(setState.getState()), false);
        }
        if (statement instanceof IrStmt.ProcessControl control) {
            return processControl(control);
        }
        if (statement instanceof IrStmt.Block block) {
            List<Attributes> parts = new ArrayList<>();
            block.getStatements().forEach(inner -> parts.add(prepare(inner)));
            return AttributeCalculus.cons(parts);
        }
        if (statement instanceof IrStmt.If ifStmt) {
            Attributes thenPart = prepare(ifStmt.getThenBranch());
            if (ifStmt.getElseBranch() == null) {
                // No else: the alternative is doing nothing.
                return AttributeCalculus.optional(thenPart);
            }
            Attributes elsePart = prepare(ifStmt.getElseBranch());
            return AttributeCalculus.concludePar(AttributeCalculus.par(thenPart, elsePart));
        }
        if (statement instanceof IrStmt.Switch switchStmt) {
            List<Attributes> branches = new ArrayList<>();
            boolean hasDefault = false;
            for (IrStmt.SwitchCase clause : switchStmt.getCases()) {
                List<Attributes> parts = new ArrayList<>();
                clause.getStatements().forEach(inner -> parts.add(prepare(inner)));
                Attributes clauseAttributes = AttributeCalculus.cons(parts);
                attributes.put(clause, clauseAttributes);
                branches.add(clauseAttributes);
                hasDefault |= clause.isDefault();
            }
            if (!hasDefault) {
                // Matching no case is a branch of its own, and it does nothing.
                branches.add(Attributes.EMPTY);
            }
            return AttributeCalculus.par(branches);
        }
        if (statement instanceof IrStmt.For forStmt) {
            // The body may run any number of times, including none.
            return AttributeCalculus.optional(prepare(forStmt.getBody()));
        }
        // Expression statements, empty statements, local declarations and inline C do
        // nothing to any process.
        return Attributes.EMPTY;
    }

    /**
     * {@code start}, {@code stop}, {@code error} and {@code restart}. Acting on the
     * enclosing process also resets its timer and moves it, which is what makes these
     * different from acting on another one.
     */
    private Attributes processControl(IrStmt.ProcessControl control) {
        String target = control.targetsEnclosingProcess() ? currentProcess : control.getProcess();
        boolean self = target.equals(currentProcess);

        return switch (control.getKind()) {
            case STOP -> self
                    ? Attributes.change(target, Change.STOP, true, true, Set.of("stop"))
                    : Attributes.change(target, Change.STOP, false, false, Set.of());
            case ERROR -> self
                    ? Attributes.change(target, Change.ERROR, true, true, Set.of("error"))
                    : Attributes.change(target, Change.ERROR, false, false, Set.of());
            case START, RESTART -> self ? restartSelf(target) : startOther(target);
        };
    }

    /**
     * Restarting the enclosing process moves it to its first state - unless it is already
     * there, in which case nothing about its state changes.
     */
    private Attributes restartSelf(String target) {
        String firstState = firstStateOf(target);
        boolean moves = !firstState.equals(currentState);
        return Attributes.change(target, Change.START, true, moves,
                moves ? Set.of(firstState) : Set.of());
    }

    private Attributes startOther(String target) {
        return Attributes.change(target, Change.START, false, false, Set.of());
    }

    private String firstStateOf(String processName) {
        IrProcess process = program.findProcess(processName);
        if (process == null || process.getStartState() == null) {
            throw new IllegalStateException("no start state for process " + processName);
        }
        return process.getStartState().getName();
    }
}
