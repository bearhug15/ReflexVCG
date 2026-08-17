package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

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

    /** Whether a timeout branch is still possible. */
    public boolean allowsTimeout(PathState path, boolean exceeded, boolean variableDuration) {
        // Only the branch that fires is constrained, and only when the duration is fixed:
        // a variable one could be zero.
        if (!exceeded || variableDuration) {
            return true;
        }
        boolean simple = !path.curAttr().reset();
        return combination == Combination.EITHER ? simple : true;
    }

    /** Whether the activity facts a guard asserts are still possible. */
    public boolean allowsActivities(PathState path, List<Term> asserted) {
        for (Term term : asserted) {
            if (term instanceof Term.ProcessActivity activity) {
                boolean simple = !rule8(path, activity) && !rule9(path, activity);
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

    private boolean rule6(PathState path, String process, String state) {
        boolean inactive = state.equals("stop") || state.equals("error");
        for (Term.Activity activity : path.activitiesAsserted(process)) {
            boolean contradiction = switch (activity) {
                case ACTIVE -> inactive;
                case INACTIVE -> !inactive;
                case STOP -> !state.equals("stop");
                case ERROR -> !state.equals("error");
                case NONSTOP -> state.equals("stop");
                // SPEC: written as `p != error`; asserting "not in error" only
                // contradicts actually being in error.
                case NONERROR -> state.equals("error");
            };
            if (contradiction) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rule 8: the path asserted a status for the process, and the change recorded since
     * cannot have brought it to the status now being asserted.
     */
    private boolean rule8(PathState path, Term.ProcessActivity term) {
        String process = term.process();
        Change change = path.curAttr().changeFor(process);
        List<Term.Activity> asserted = path.activitiesAsserted(process);

        return switch (term.activity()) {
            case ACTIVE -> containsAny(asserted, Term.Activity.INACTIVE, Term.Activity.STOP,
                    Term.Activity.ERROR) && change != Change.START;
            case INACTIVE -> asserted.contains(Term.Activity.ACTIVE)
                    && change != Change.STOP && change != Change.ERROR;
            case STOP -> containsAny(asserted, Term.Activity.ACTIVE, Term.Activity.NONSTOP,
                    Term.Activity.ERROR) && change != Change.STOP;
            case ERROR -> containsAny(asserted, Term.Activity.ACTIVE, Term.Activity.NONERROR,
                    Term.Activity.STOP) && change != Change.ERROR;
            case NONSTOP -> asserted.contains(Term.Activity.STOP)
                    && change != Change.START && change != Change.ERROR;
            case NONERROR -> asserted.contains(Term.Activity.ERROR)
                    && change != Change.START && change != Change.STOP;
        };
    }

    /**
     * Rule 9, restated: if the path asserted some status for the process, and the process
     * was changed since, then a status now asserted that disagrees with that change is
     * impossible.
     */
    private boolean rule9(PathState path, Term.ProcessActivity term) {
        Change change = path.curAttr().changeFor(term.process());
        if (change == null || path.activitiesAsserted(term.process()).isEmpty()) {
            return false;
        }
        return !agreesWith(term.activity(), change);
    }

    private static boolean agreesWith(Term.Activity activity, Change change) {
        return switch (change) {
            case START -> activity == Term.Activity.ACTIVE
                    || activity == Term.Activity.NONSTOP
                    || activity == Term.Activity.NONERROR;
            case STOP -> activity == Term.Activity.INACTIVE
                    || activity == Term.Activity.STOP
                    || activity == Term.Activity.NONERROR;
            case ERROR -> activity == Term.Activity.INACTIVE
                    || activity == Term.Activity.ERROR
                    || activity == Term.Activity.NONSTOP;
        };
    }

    // ------------------------------------------------------------------ group rules

    /**
     * Processes in one group start, stop and fail together, so their asserted states have
     * to agree about all three.
     */
    private boolean groupAllowsState(PathState path, String process, String state) {
        int group = facts.of(process).group();
        for (Term.PstateCompare other : path.otherProcessStates(process)) {
            if (facts.of(other.process()).group() != group) {
                continue;
            }
            // Rules 1 and 2: one stopped or failed means both did.
            if (state.equals("stop") != other.pstate().equals("stop")) {
                return false;
            }
            if (state.equals("error") != other.pstate().equals("error")) {
                return false;
            }
            // Rules 3 and 4: one just started - it is in a first state nothing else
            // jumps to, and it got there by a state change - so the other must have too.
            if (justStarted(process, state) && !isFirstState(other.process(), other.pstate())) {
                return false;
            }
            if (justStarted(other.process(), other.pstate()) && !isFirstState(process, state)) {
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

    private static boolean containsAny(List<Term.Activity> asserted, Term.Activity... any) {
        for (Term.Activity activity : any) {
            if (asserted.contains(activity)) {
                return true;
            }
        }
        return false;
    }

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
