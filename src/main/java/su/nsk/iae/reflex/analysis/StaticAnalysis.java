package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.TimeRef;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Decides whether a path is still possible, applying the rules of StaticalAnalysis.tex.
 *
 * <p>A path is checked as it is walked, so an impossible one is abandoned at the point it
 * becomes impossible rather than being generated and then discarded.
 *
 * <p>Readings taken where the specification is inconsistent are marked SPEC. The ones
 * here, beyond those in {@link AttributeCalculus} and {@link ProcessFacts}:
 * <ul>
 *   <li>Rule 7 is {@code !curAttr.reset}, which discards a timeout branch when the timer
 *       was <em>not</em> reset. Inverted: if the timer was reset this cycle then ltime is
 *       0, so it is the "timeout reached" branch that cannot happen.</li>
 *   <li>Rule 6's last clause is {@code p != "error" && nonerror}, which contradicts
 *       nothing - asserting "not in error" agrees with any state that is not error. Read
 *       as {@code p == "error"}.</li>
 *   <li>Rule 9's active/stop/error cases lack the check on curCond that its other cases
 *       have. Restated per the intent given: the rule fires when the path asserted a
 *       status for the process, the process was changed since, and the status now being
 *       asserted disagrees with that change.</li>
 *   <li>The group rules compare a state name against a process name. Read as comparing
 *       each process's asserted state against its own first state, which is what the
 *       previous implementation did.</li>
 * </ul>
 */
public final class StaticAnalysis {

    /** How the two rule sets are combined; see the note in the class comment. */
    public enum Combination {
        /** Discard when either rule set finds the path impossible. */
        EITHER,
        /** Discard only when both do - the specification's `||` read literally. */
        BOTH
    }

    private final IrProgram program;
    private final Map<IrNode, Attributes> attributes;
    private final ProcessFacts facts;
    private final Map<String, Set<String>> reachFrom;
    private final Set<String> constantNames;
    private final Combination combination;

    public StaticAnalysis(IrProgram program) {
        this(program, Combination.EITHER);
    }

    public StaticAnalysis(IrProgram program, Combination combination) {
        this.program = program;
        this.combination = combination;
        this.attributes = new AttributePreparation(program).run();
        this.facts = new ProcessFacts(program, attributes);
        this.reachFrom = computeReachFrom();
        this.constantNames = collectConstantNames();
    }

    public Attributes attributesOf(IrNode node) {
        return attributes.getOrDefault(node, Attributes.EMPTY);
    }

    public ProcessFacts getFacts() {
        return facts;
    }

    // ------------------------------------------------------------------ entry points

    /** Whether a path asserting {@code process} is in {@code state} is still possible. */
    public boolean allowsState(PathState path, String process, String state) {
        boolean simple = simpleAllowsState(path, process, state);
        boolean group = groupAllowsState(path, process, state);
        return combination == Combination.EITHER ? simple && group : simple || group;
    }

    /**
     * Section 4.2: whether a timeout branch is still possible.
     *
     * <p>Only the branch that fires is constrained, and only when the duration is fixed.
     * A duration held in a variable could be zero, in which case the timeout can elapse
     * even on a cycle that reset the timer - but a duration naming a constant is fixed and
     * the rule applies to it just as it does to a literal.
     */
    public boolean allowsTimeout(PathState path, boolean exceeded, TimeRef duration) {
        if (!exceeded || isVariableDuration(duration)) {
            return true;
        }
        // The timer was reset on this path, so ltime is zero and the timeout cannot have
        // elapsed.
        boolean simple = !path.curAttr().reset();
        return combination == Combination.EITHER ? simple : true;
    }

    /** A named duration is variable only when the name is not a declared constant. */
    private boolean isVariableDuration(TimeRef duration) {
        return duration.isName() && !constantNames.contains(duration.getText());
    }

    private Set<String> collectConstantNames() {
        Set<String> names = new LinkedHashSet<>();
        program.getConstants().forEach(constant -> names.add(constant.getName()));
        program.getNodes().forEach(node ->
                node.getConstants().forEach(constant -> names.add(constant.getName())));
        return names;
    }

    /** Whether the activity facts a guard asserts are still possible. */
    public boolean allowsActivities(PathState path, List<Term> asserted) {
        for (Term term : asserted) {
            if (term instanceof Term.ProcessActivity activity) {
                boolean simple = !contradictsKnownStatus(path, activity);
                boolean allowed = combination == Combination.EITHER ? simple : true;
                if (!allowed) {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ simple rules

    private boolean simpleAllowsState(PathState path, String process, String state) {
        ProcessFacts.Facts processFacts = facts.of(process);

        // Rule 1: the process can never fail, so it cannot be in the error state.
        if (state.equals("error") && !processFacts.reachE()) {
            return false;
        }
        // Rule 2: it can never be stopped and does not begin stopped.
        if (state.equals("stop") && !processFacts.reachS() && !processFacts.startS()) {
            return false;
        }

        Change change = path.curAttr().changeFor(process);
        // Rule 3: it was started on this path, so it is in its first state.
        if (change == Change.START && !state.equals(firstStateOf(process))) {
            return false;
        }
        // Rules 4 and 5: it was stopped, or it failed.
        if (change == Change.STOP && !state.equals("stop")) {
            return false;
        }
        if (change == Change.ERROR && !state.equals("error")) {
            return false;
        }
        // Rule 6: the path already asserted a status that this state contradicts.
        return !rule6(path, process, state);
    }

    /**
     * Rule 6 / section 4.6: a state assertion that contradicts what the path already
     * knows about the process's status.
     *
     * <p>The status used is the implied one - the most recent assertion, overridden by
     * any change made since. Section 4.6 as printed omits the intermediate-change check
     * that its neighbours carry, which would make it fire on paths where a change between
     * the two assertions reconciles them.
     */
    private boolean rule6(PathState path, String process, String state) {
        Term.Activity implied = path.impliedStatus(process);
        return implied != null && contradicts(implied, PathState.statusOfState(state));
    }

    /**
     * Whether two statements about a process's status cannot both hold. The table is
     * section 4.6: active excludes stop and error, stop excludes everything but stop,
     * nonstop excludes stop, and so on.
     */
    private static boolean contradicts(Term.Activity known, Term.Activity asserted) {
        return excludes(known, asserted) || excludes(asserted, known);
    }

    private static boolean excludes(Term.Activity known, Term.Activity asserted) {
        return switch (known) {
            case ACTIVE -> asserted == Term.Activity.INACTIVE
                    || asserted == Term.Activity.STOP
                    || asserted == Term.Activity.ERROR;
            case INACTIVE -> asserted == Term.Activity.ACTIVE;
            case STOP -> asserted == Term.Activity.ACTIVE
                    || asserted == Term.Activity.NONSTOP
                    || asserted == Term.Activity.ERROR;
            case ERROR -> asserted == Term.Activity.ACTIVE
                    || asserted == Term.Activity.NONERROR
                    || asserted == Term.Activity.STOP;
            case NONSTOP -> asserted == Term.Activity.STOP;
            case NONERROR -> asserted == Term.Activity.ERROR;
        };
    }

    /**
     * Sections 4.3 and 4.4: a status assertion that contradicts what the path already
     * knows about the process.
     *
     * <p>Both sections have the same shape - an earlier statement about the process, a
     * status now being asserted that disagrees with it, and no change in between that
     * reconciles the two. 4.3 starts from an earlier status assertion and 4.4 from a
     * change; the implied status covers both, because it is the most recent assertion
     * overridden by the last change made since.
     */
    private boolean contradictsKnownStatus(PathState path, Term.ProcessActivity term) {
        Term.Activity implied = path.impliedStatus(term.process());
        return implied != null && contradicts(implied, term.activity());
    }

    // ------------------------------------------------------------------ group rules

    /**
     * Processes in one group start, stop and fail together, so their asserted states have
     * to agree about all three.
     */
    private boolean groupAllowsState(PathState path, String process, String state) {
        int group = facts.of(process).group();
        for (Event.StateAsserted other : path.otherProcessStates(process)) {
            if (facts.of(other.process()).group() != group) {
                continue;
            }
            // Rules 1 and 2: one stopped or failed means both did.
            if (state.equals("stop") != other.state().equals("stop")) {
                return false;
            }
            if (state.equals("error") != other.state().equals("error")) {
                return false;
            }
            // Rules 3 and 4: one just started - it is in a first state nothing else
            // jumps to, and it got there by a state change - so the other must have too.
            if (justStarted(process, state) && !isFirstState(other.process(), other.state())) {
                return false;
            }
            if (justStarted(other.process(), other.state()) && !isFirstState(process, state)) {
                return false;
            }
        }
        return true;
    }

    private boolean justStarted(String process, String state) {
        if (!isFirstState(process, state)) {
            return false;
        }
        IrState first = firstState(process);
        return first != null
                && reachFrom.getOrDefault(qualify(process, state), Set.of()).isEmpty()
                && attributesOf(first).stateChanged();
    }

    private boolean isFirstState(String process, String state) {
        return state.equals(firstStateOf(process));
    }

    // ------------------------------------------------------------------ helpers

    private IrState firstState(String process) {
        IrProcess found = program.findProcess(process);
        return found == null ? null : found.getStartState();
    }

    private String firstStateOf(String process) {
        IrState first = firstState(process);
        return first == null ? null : first.getName();
    }

    private static String qualify(String process, String state) {
        return process + "::" + state;
    }

    /**
     * For each state, the states that can move to it. A state nothing jumps to can only
     * be entered by the process being started, which is what the group rules rely on.
     */
    private Map<String, Set<String>> computeReachFrom() {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (IrProcess process : program.getProcesses()) {
            for (IrState state : process.getStates()) {
                for (String target : targetsOf(state)) {
                    result.computeIfAbsent(qualify(process.getName(), target), k -> new LinkedHashSet<>())
                            .add(state.getName());
                }
            }
        }
        return result;
    }

    private Set<String> targetsOf(IrState state) {
        Set<String> targets = new LinkedHashSet<>();
        collectTargets(state.getStatements(), targets);
        if (state.getTimeout() != null) {
            collectTargets(List.of(state.getTimeout().getBody()), targets);
        }
        return targets;
    }

    private void collectTargets(List<IrStmt> statements, Set<String> targets) {
        for (IrStmt statement : statements) {
            if (statement instanceof IrStmt.SetState setState && setState.getState() != null) {
                targets.add(setState.getState());
            } else if (statement instanceof IrStmt.Block block) {
                collectTargets(block.getStatements(), targets);
            } else if (statement instanceof IrStmt.If ifStmt) {
                collectTargets(List.of(ifStmt.getThenBranch()), targets);
                if (ifStmt.getElseBranch() != null) {
                    collectTargets(List.of(ifStmt.getElseBranch()), targets);
                }
            } else if (statement instanceof IrStmt.Switch switchStmt) {
                switchStmt.getCases().forEach(c -> collectTargets(c.getStatements(), targets));
            } else if (statement instanceof IrStmt.For forStmt) {
                collectTargets(List.of(forStmt.getBody()), targets);
            }
        }
    }
}
