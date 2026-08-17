package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrCopier;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Rewrites constructs that VC generation would otherwise have to special-case.
 *
 * <p>Implements the "Нормализация" section of Preprocessing.tex. Three things happen:
 *
 * <ul>
 *   <li>{@code set next state} is resolved to the state that follows in declaration
 *       order, before any state is synthesised, so it still means what it did.</li>
 *   <li>Switch fall-through is expanded: a case without {@code break} gets a copy of the
 *       following cases' statements appended, up to and including the first that breaks,
 *       so every case becomes independent.</li>
 *   <li>{@code slice} and {@code wait} are eliminated in favour of ordinary states. Both
 *       suspend a state part-way through, which the cyclic execution model has no way to
 *       express, so the remainder of the state becomes a fresh "light" state.</li>
 * </ul>
 *
 * <p>After this pass no {@link IrStmt.Slice} or {@link IrStmt.Wait} remains, and states
 * synthesised here are marked {@link IrState#isSynthetic()}.
 *
 * <p><b>Deviations from the spec.</b> The pseudocode for the wait cases assigns
 * {@code newName1} twice where the second is plainly {@code newName2}, and calls
 * {@code createSetState} with no argument. This implementation reads those as
 * {@code newName2} in both places: when the tail does not itself transfer, the state
 * reached after a successful wait is the one holding the tail.
 */
public final class NormalizationPass {

    private int lightCounter;
    private String nextStateName;

    public void run(IrProgram program) {
        program.getProcesses().forEach(this::normalizeProcess);
    }

    private void normalizeProcess(IrProcess process) {
        List<IrState> original = new ArrayList<>(process.getStates());
        List<IrState> normalized = new ArrayList<>();
        lightCounter = 0;

        for (int i = 0; i < original.size(); i++) {
            IrState state = original.get(i);
            // Resolved against the original ordering: the light states appended below
            // must not become the target of a `set next state`.
            nextStateName = i + 1 < original.size() ? original.get(i + 1).getName() : null;
            normalized.addAll(normalizeState(state));
        }

        process.getStates().clear();
        process.getStates().addAll(normalized);
    }

    /** Returns the state itself followed by any light states split out of it. */
    private List<IrState> normalizeState(IrState state) {
        Split split = split(state.getName(), state.getStatements(), 0);

        state.getStatements().clear();
        state.getStatements().addAll(split.body);
        if (state.getTimeout() != null) {
            normalizeStatement(state.getTimeout().getBody());
        }

        List<IrState> result = new ArrayList<>();
        result.add(state);
        result.addAll(split.newStates);
        return result;
    }

    /**
     * Result of splitting a statement list at its first suspension point.
     *
     * @param body         statements that stay in the state being split
     * @param newStates    light states carrying what came after the suspension
     * @param mustTransfer whether {@code body} is guaranteed to end in a state change
     */
    private record Split(List<IrStmt> body, List<IrState> newStates, boolean mustTransfer) {
    }

    private Split split(String stateName, List<IrStmt> statements, int index) {
        if (index >= statements.size()) {
            return new Split(new ArrayList<>(), new ArrayList<>(), false);
        }

        IrStmt head = statements.get(index);
        normalizeStatement(head);
        Split tail = split(stateName, statements, index + 1);

        if (head instanceof IrStmt.Slice) {
            return splitAtSlice(stateName, tail);
        }
        if (head instanceof IrStmt.Wait wait) {
            return splitAtWait(stateName, wait, tail);
        }

        List<IrStmt> body = new ArrayList<>();
        body.add(head);
        body.addAll(tail.body());
        return new Split(body, tail.newStates(), tail.mustTransfer());
    }

    /**
     * Everything after a {@code slice} runs on the next cycle, so it moves wholesale
     * into a light state that the current body transfers to.
     */
    private Split splitAtSlice(String stateName, Split tail) {
        IrState continuation = lightState(stateName, tail.body());

        List<IrState> states = new ArrayList<>(tail.newStates());
        states.add(continuation);

        List<IrStmt> body = new ArrayList<>();
        body.add(new IrStmt.SetState(continuation.getName(), false));
        return new Split(body, states, true);
    }

    /**
     * A {@code wait} continues in the same cycle when its condition already holds, and
     * otherwise parks in a light state that re-tests it on each following cycle.
     */
    private Split splitAtWait(String stateName, IrStmt.Wait wait, Split tail) {
        List<IrState> states = new ArrayList<>(tail.newStates());

        String waitingName = nextLightName(stateName);
        List<IrStmt> continueBody;

        if (tail.mustTransfer()) {
            // The tail already ends in a state change, so it can be run as-is.
            continueBody = tail.body();
        } else {
            // Nothing downstream transfers, so the tail needs a state of its own to
            // continue in; the current cycle runs it and then moves there.
            IrState continuation = lightState(stateName, tail.body());
            states.add(continuation);
            continueBody = new ArrayList<>(IrCopier.copyStatements(tail.body()));
            continueBody.add(new IrStmt.SetState(continuation.getName(), false));
        }

        // The waiting state keeps testing the condition and proceeds once it holds.
        IrState waiting = new IrState(waitingName, false, new ArrayList<>(List.of(
                new IrStmt.If(IrCopier.copy(wait.getCondition()),
                        new IrStmt.Block(IrCopier.copyStatements(continueBody)), null))));
        waiting.setSynthetic(true);
        waiting.copyOriginFrom(wait);
        if (wait.hasTimeout()) {
            // A `wait ... on timeout` carries its timeout into the state that waits.
            waiting.setTimeout(new IrState.Timeout(
                    IrCopier.copy(wait.getTimeout()), IrCopier.copy(wait.getTimeoutBody())));
        }
        states.add(waiting);

        List<IrStmt> body = new ArrayList<>();
        body.add(new IrStmt.If(
                IrCopier.copy(wait.getCondition()),
                new IrStmt.Block(continueBody),
                new IrStmt.Block(new ArrayList<>(List.of(new IrStmt.SetState(waitingName, false))))));
        return new Split(body, states, true);
    }

    private IrState lightState(String stateName, List<IrStmt> body) {
        IrState state = new IrState(nextLightName(stateName), false, new ArrayList<>(body));
        state.setSynthetic(true);
        return state;
    }

    private String nextLightName(String stateName) {
        return stateName + "_light" + lightCounter++;
    }

    // ------------------------------------------------------------------ statements

    /** Rewrites within a statement: set-next-state resolution and switch expansion. */
    private void normalizeStatement(IrStmt statement) {
        if (statement == null) {
            return;
        }
        if (statement instanceof IrStmt.SetState setState) {
            if (setState.isNext()) {
                if (nextStateName == null) {
                    throw new IllegalStateException(
                            "`set next state` in the last state of a process has no successor");
                }
                setState.setState(nextStateName);
            }
        } else if (statement instanceof IrStmt.Block block) {
            block.getStatements().forEach(this::normalizeStatement);
        } else if (statement instanceof IrStmt.If ifStmt) {
            normalizeStatement(ifStmt.getThenBranch());
            normalizeStatement(ifStmt.getElseBranch());
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            normalizeSwitch(switchStmt);
        } else if (statement instanceof IrStmt.Wait wait) {
            normalizeStatement(wait.getTimeoutBody());
        } else if (statement instanceof IrStmt.For forStmt) {
            normalizeStatement(forStmt.getBody());
        }
    }

    /**
     * Makes every case self-contained by appending the statements it would fall through
     * into, so the graph builder can treat cases as independent branches.
     */
    private void normalizeSwitch(IrStmt.Switch switchStmt) {
        List<IrStmt.SwitchCase> cases = switchStmt.getCases();
        cases.forEach(c -> c.getStatements().forEach(this::normalizeStatement));

        List<IrStmt.SwitchCase> labelled = cases.stream().filter(c -> !c.isDefault()).toList();
        IrStmt.SwitchCase defaultCase = switchStmt.getDefaultCase();

        // Cases still falling through, awaiting the statements that follow them.
        List<IrStmt.SwitchCase> pending = new ArrayList<>();
        for (IrStmt.SwitchCase current : labelled) {
            for (IrStmt.SwitchCase open : pending) {
                open.getStatements().addAll(IrCopier.copyStatements(current.getStatements()));
            }
            if (current.isBreaks()) {
                pending.forEach(open -> open.setBreaks(true));
                pending.clear();
            } else {
                pending.add(current);
            }
        }

        if (defaultCase != null) {
            for (IrStmt.SwitchCase open : pending) {
                open.getStatements().addAll(IrCopier.copyStatements(defaultCase.getStatements()));
                open.setBreaks(true);
            }
            pending.clear();
        }
    }

    /** Exposed for tests: the condition a wait was rewritten into. */
    static IrExpr conditionOf(IrStmt statement) {
        return statement instanceof IrStmt.If ifStmt ? ifStmt.getCondition() : null;
    }
}
