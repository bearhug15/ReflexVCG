package su.nsk.iae.reflex.analysis;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.ir.IrProgram;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.asserts;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.conditions;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.count;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.inState;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.moves;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.not;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.program;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.programWith;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.timeoutElapsed;

/**
 * Every rule of static-analysis.md, section 4, end to end: on a program small enough to reason
 * about by hand, a path the rule discards - generated without the analysis, gone with it - and
 * a look-alike, differing in the one detail the rule turns on, that it keeps. A kept path here
 * is always one a run can take.
 */
class StaticAnalysisRulesTest {

    /** Generated without the analysis, and not with it. */
    @SafeVarargs
    private static void discarded(IrProgram program, java.util.function.Predicate<su.nsk.iae.reflex.vc.VerificationCondition>... path) {
        assertTrue(count(program, false, path) > 0, "the path exists without the analysis");
        assertEquals(0, count(program, true, path), "the analysis discards the path");
    }

    /** Generated with the analysis. */
    @SafeVarargs
    private static void kept(IrProgram program, java.util.function.Predicate<su.nsk.iae.reflex.vc.VerificationCondition>... path) {
        assertTrue(count(program, true, path) > 0, "the analysis keeps the path");
    }

    // ================================================================== 4.1 redundant states

    /** 4.1: a process that nothing can stop or fail is in neither stop nor error. */
    @Test
    void r41_discardsStopAndErrorOfAProcessNothingChanges() {
        IrProgram program = program("process Proc :: node N { state s { a = true; } }");
        assertEquals(3, conditions(program, false).size());
        assertEquals(1, conditions(program, true).size());
    }

    /** 4.1 look-alike: once something can stop it, stop is kept. */
    @Test
    void r41_keepsStopOnceSomethingCanStopIt() {
        IrProgram program = program("process Proc :: node N { state s { if (x) { stop; } } }");
        kept(program, inState("Proc", "stop"));
    }

    /** 4.1 look-alike: a process nobody starts begins stopped, and stays so. */
    @Test
    void r41_keepsStopOfAProcessThatBeginsStopped() {
        IrProgram program = program(
                "process First :: node N { state f { a = true; } }",
                "process Never :: node N { state n { a = false; } }");
        kept(program, inState("Never", "stop"));
    }

    /** 4.1 seeds the possible statuses: a check that a never-stopped process is inactive. */
    @Test
    void r41_discardsACheckThatANeverStoppedProcessIsInactive() {
        IrProgram program = program(
                "process Ctl :: node N { state init { start W; set next state; }"
                        + " state s { if (process W in state inactive) { a = true; } } }",
                "process W :: node N { state w { a = false; } }");
        discarded(program, asserts("W", Term.Activity.INACTIVE));
        kept(program, asserts("W", Term.Activity.ACTIVE));
    }

    // ================================================================== 4.2 timer reset

    /** 4.2: the process reset its own timer this cycle, so its timeout cannot have elapsed. */
    @Test
    void r42_discardsTheElapsedBranchAfterTheProcessResetItsTimer() {
        IrProgram program = program(
                "process Proc :: node N { state s { reset timer; timeout 0t5s { a = true; } } state t { a = false; } }");
        discarded(program, timeoutElapsed("Proc"));
    }

    /** 4.2: set state resets the timer too. */
    @Test
    void r42_discardsTheElapsedBranchAfterSetState() {
        IrProgram program = program(
                "process Proc :: node N { state s { set state t; timeout 0t5s { a = false; } } state t { a = false; } }");
        discarded(program, timeoutElapsed("Proc"));
    }

    /** 4.2 look-alike: without a reset the timeout may elapse. */
    @Test
    void r42_keepsTheElapsedBranchWithoutAReset() {
        IrProgram program = program(
                "process Proc :: node N { state s { a = true; timeout 0t5s { a = false; } } state t { a = false; } }");
        kept(program, timeoutElapsed("Proc"));
    }

    /** 4.2 look-alike: a reset by another process says nothing about this one's timer. */
    @Test
    void r42_keepsTheElapsedBranchAfterAnotherProcessResetItsOwnTimer() {
        IrProgram program = program(
                "process A :: node N { state a1 { start B; set next state; } state a2 { reset timer; } }",
                "process B :: node N { state b1 { timeout 0t1s { set next state; } } state b2 { a = true; } }");
        kept(program, inState("A", "a2"), timeoutElapsed("B"));
    }

    /** 4.2: a process started this cycle by one dispatched before it is at local time zero. */
    @Test
    void r42_discardsTheElapsedBranchOfAProcessStartedEarlierInTheCycle() {
        IrProgram program = program(
                "process A :: node N { state a1 { start B; set next state; } state a2 { if (x) { start B; } } }",
                "process B :: node N { state b1 { timeout 0t1s { set next state; } } state b2 { a = true; } }");
        discarded(program, moves("B", "b1"), timeoutElapsed("B"));
        kept(program, not(moves("B", "b1")), timeoutElapsed("B"));
    }

    /** 4.2 look-alike: started by a process dispatched after it, its timeout was checked before. */
    @Test
    void r42_keepsTheElapsedBranchOfAProcessStartedLaterInTheCycle() {
        IrProgram program = program(
                "process B :: node N { state b1 { timeout 0t1s { set next state; } } state b2 { a = true; } }",
                "process A :: node N { state a1 { if (x) { start B; } } }");
        kept(program, moves("B", "b1"), timeoutElapsed("B"));
    }

    /** 4.2 look-alike: a duration of zero elapses at local time zero. */
    @Test
    void r42_keepsTheElapsedBranchOfAZeroDuration() {
        IrProgram program = programWith("const int32 ZERO = 0;",
                "process Proc :: node N { state s { reset timer; timeout ZERO { a = true; } } state t { a = false; } }");
        kept(program, timeoutElapsed("Proc"));
        IrProgram positive = programWith("const int32 FIVE = 2 + 3;",
                "process Proc :: node N { state s { reset timer; timeout FIVE { a = true; } } state t { a = false; } }");
        discarded(positive, timeoutElapsed("Proc"));
    }

    /** 4.2 look-alike: a loop between the reset and the timeout lets time pass in the model. */
    @Test
    void r42_keepsTheElapsedBranchAfterALoop() {
        IrProgram program = program(
                "process Proc :: node N { state s { reset timer; for (i = 0; i < 2; i = i + 1) { a = !a; }"
                        + " timeout 0t100ms { a = true; } } state t { a = false; } }");
        kept(program, timeoutElapsed("Proc"));
    }

    // ================================================================== 4.3 status against status

    private static final String CTL_CHECKS_W = "process Ctl :: node N { state init { start W; set next state; } state s { %s } }";
    private static final String W_STOPS_OR_FAILS =
            "process W :: node N { state run { if (x) { stop; } else { error; } } }";

    /** 4.3: two checks that cannot both hold. */
    @Test
    void r43_discardsContradictoryChecks() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "if (process W in state stop) { if (process W in state active) { a = true; } }"), W_STOPS_OR_FAILS);
        discarded(program, asserts("W", Term.Activity.STOP), asserts("W", Term.Activity.ACTIVE));
        kept(program, asserts("W", Term.Activity.STOP), asserts("W", Term.Activity.INACTIVE));
    }

    /**
     * 4.3, the set: not-error, then not-stop, then inactive. Every two of the three are
     * compatible, all three are not - the example the possible-status set was chosen for.
     */
    @Test
    void r43_discardsThreeChecksNoTwoOfWhichContradict() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "if (!(process W in state error)) { if (!(process W in state stop)) {"
                        + " if (process W in state inactive) { start W; } } }"), W_STOPS_OR_FAILS);
        discarded(program, asserts("W", Term.Activity.NONERROR), asserts("W", Term.Activity.NONSTOP),
                asserts("W", Term.Activity.INACTIVE));
        kept(program, asserts("W", Term.Activity.NONERROR), asserts("W", Term.Activity.NONSTOP),
                asserts("W", Term.Activity.ACTIVE));
    }

    /** 4.3 look-alike: a change between two checks reconciles them. */
    @Test
    void r43_keepsChecksAChangeBetweenReconciles() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "if (process W in state stop) { start W; if (process W in state active) { a = true; } }"),
                W_STOPS_OR_FAILS);
        kept(program, asserts("W", Term.Activity.STOP), asserts("W", Term.Activity.ACTIVE));
    }

    // ================================================================== 4.4 status after a change

    /** 4.4: a check contradicting the change just made. */
    @Test
    void r44_discardsACheckContradictingAChange() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "stop W; if (process W in state active) { a = true; }"), W_STOPS_OR_FAILS);
        discarded(program, asserts("W", Term.Activity.ACTIVE));
        kept(program, asserts("W", Term.Activity.INACTIVE));
    }

    /** 4.4 look-alike: a later change replaces the earlier one. */
    @Test
    void r44_keepsACheckAgreeingWithTheLastChange() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "stop W; start W; if (process W in state active) { a = true; }"), W_STOPS_OR_FAILS);
        kept(program, asserts("W", Term.Activity.ACTIVE));
    }

    /** 4.4 look-alike: a loop that may stop the process weakens what is known. */
    @Test
    void r44_keepsACheckAfterALoopThatMayChangeTheProcess() {
        IrProgram program = program(String.format(CTL_CHECKS_W,
                "start W; for (i = 0; i < 2; i = i + 1) { if (x) { stop W; } }"
                        + " if (process W in state stop) { a = true; }"), W_STOPS_OR_FAILS);
        kept(program, asserts("W", Term.Activity.STOP));
    }

    // ================================================================== 4.5 state after a change

    private static final String W_TWO_STATES =
            "process W :: node N { state one { if (y) { set next state; } } state two { if (x) { stop; } else { error; } } }";

    /** 4.5.1: started this cycle, so dispatched in its first state. */
    @Test
    void r45_discardsANonFirstStateAfterAStart() {
        IrProgram program = program("process Ctl :: node N { state s { if (z) { start W; } } }", W_TWO_STATES);
        discarded(program, moves("W", "one"), inState("W", "two"));
        kept(program, moves("W", "one"), inState("W", "one"));
        kept(program, not(moves("W", "one")), inState("W", "two"));
    }

    /** 4.5.2: stopped this cycle, so dispatched in stop. */
    @Test
    void r45_discardsARunningStateAfterAStop() {
        IrProgram program = program("process Ctl :: node N { state s { if (z) { stop W; } } }", W_TWO_STATES);
        discarded(program, moves("W", "stop"), inState("W", "one"));
        kept(program, moves("W", "stop"), inState("W", "stop"));
    }

    /**
     * 4.5 look-alike: started, then stopped - dispatched in stop. As printed, the paper's
     * condition ("no stop followed by a start in between") would discard this.
     */
    @Test
    void r45_keepsTheStateOfTheLastChange() {
        IrProgram program = program("process Ctl :: node N { state s { start W; if (z) { stop W; } } }", W_TWO_STATES);
        kept(program, moves("W", "stop"), inState("W", "stop"));
        discarded(program, moves("W", "stop"), inState("W", "one"));
    }

    /** 4.5 look-alike: a loop that may stop the process after its start. */
    @Test
    void r45_keepsAStateALoopMayHaveCaused() {
        IrProgram program = program(
                "process Ctl :: node N { state s { start W; for (i = 0; i < 2; i = i + 1) { if (x) { stop W; } } } }",
                W_TWO_STATES);
        kept(program, inState("W", "stop"));
        kept(program, inState("W", "one"));
        discarded(program, inState("W", "two"));
    }

    // ================================================================== 4.6 state against status

    /** 4.6: dispatched in a state the earlier check excludes. */
    @Test
    void r46_discardsAStateTheCheckExcludes() {
        IrProgram program = program(
                "process Ctl :: node N { state init { start W; set next state; }"
                        + " state s { if (process W in state stop) { a = true; } } }", W_TWO_STATES);
        discarded(program, inState("Ctl", "s"), asserts("W", Term.Activity.STOP), inState("W", "one"));
        kept(program, inState("Ctl", "s"), asserts("W", Term.Activity.STOP), inState("W", "stop"));
    }

    /**
     * 4.6 look-alike: checked active, then stopped, then dispatched in stop. As printed the
     * rule has no condition on what happens between, and would discard this.
     */
    @Test
    void r46_keepsAStateAChangeSinceTheCheckExplains() {
        IrProgram program = program(
                "process Ctl :: node N { state init { start W; set next state; }"
                        + " state s { if (process W in state active) { stop W; } } }", W_TWO_STATES);
        kept(program, asserts("W", Term.Activity.ACTIVE), inState("W", "stop"));
    }

    // ================================================================== 4.7 groups

    /** A and B are started together and stopped together, so they share a group. */
    private static final String STARTER_PAIR =
            "process Starter :: node N { state s1 { start A; start B; set next state; }"
                    + " state s2 { if (x) { stop A; stop B; } if (y) { error A; error B; } if (z) { start A; start B; } } }";

    @Test
    void r47_theFixtureGroupsTheTwo() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { a = true; } }",
                "process B :: node N { state b1 { a = false; } }");
        ProcessFacts facts = new StaticAnalysis(program).getFacts();
        assertEquals(facts.of("A").group(), facts.of("B").group());
        assertTrue(facts.of("A").group() != facts.of("Starter").group());
    }

    /** 4.7.2: one stopped, the other running, with nothing this cycle to tell them apart. */
    @Test
    void r47_discardsAGroupHalfStopped() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { a = true; } }",
                "process B :: node N { state b1 { a = false; } }");
        discarded(program, inState("A", "a1"), inState("B", "stop"));
        kept(program, inState("A", "stop"), inState("B", "stop"));
        kept(program, inState("A", "a1"), inState("B", "b1"));
    }

    /** 4.7.1: one failed, the other not. */
    @Test
    void r47_discardsAGroupHalfFailed() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { a = true; } }",
                "process B :: node N { state b1 { a = false; } }");
        discarded(program, inState("A", "error"), inState("B", "b1"));
        kept(program, inState("A", "error"), inState("B", "error"));
    }

    /**
     * 4.7.3: A is in its first state, which nothing leads back to and which it always leaves,
     * so it was just started - and B with it, which therefore is in its first state too.
     */
    @Test
    void r47_discardsAJustStartedProcessWhoseGroupMateIsNotInItsFirstState() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { set next state; } state a2 { a = true; } }",
                "process B :: node N { state b1 { set next state; } state b2 { a = false; } }");
        discarded(program, inState("Starter", "s2"), not(moves("A", "a1")), inState("A", "a1"), inState("B", "b2"));
        kept(program, inState("A", "a2"), inState("B", "b2"));
    }

    /** 4.7.3 look-alike: a first state the process may stay in says nothing about a start. */
    @Test
    void r47_keepsAFirstStateTheProcessMayStayIn() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { if (y) { set next state; } } state a2 { a = true; } }",
                "process B :: node N { state b1 { set next state; } state b2 { a = false; } }");
        kept(program, inState("Starter", "s2"), not(moves("A", "a1")), inState("A", "a1"), inState("B", "b2"));
    }

    /** 4.7 with the repaired groups: the program's steady state is kept (groupRule.rcs). */
    @Test
    void r47_keepsTheSteadyStateOfTheCounterexample() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupRule.rcs")).getProgram();
        kept(program, inState("Starter", "stop"), inState("A", "stop"), inState("B", "open"));
    }

    // ================================================================== overall

    /** Pruning never adds conditions. */
    @Test
    void pruningOnlyEverRemoves() {
        IrProgram program = program(STARTER_PAIR,
                "process A :: node N { state a1 { set next state; } state a2 { stop; } }",
                "process B :: node N { state b1 { a = false; } }");
        assertTrue(conditions(program, true).size() <= conditions(program, false).size());
    }
}
