package su.nsk.iae.reflex.analysis;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.analysis.Attributes.ProcessChange;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.program;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.statement;

/** Algorithms 1-3 of static-analysis.md, section 2, one construct at a time. */
class AttributeTest {

    private static final String OTHER = "process Q :: node N { state q1 { a = true; } }";

    /** The attributes of the {@code index}-th statement of P's state {@code state}. */
    private static Attributes of(IrProgram program, String state, int index) {
        return new StaticAnalysis(program).attributesOf(statement(program, "P", state, index));
    }

    private static Attributes ofState(IrProgram program, String state) {
        return new StaticAnalysis(program).attributesOf(program.findProcess("P").findState(state));
    }

    // ------------------------------------------------------------------ Algorithm 1

    @Test
    void startingAnotherProcessIsADefiniteStartAndNothingElse() {
        Attributes start = of(program("process P :: node N { state p1 { start Q; } }", OTHER), "p1", 0);
        assertEquals(Map.of("Q", Change.START), start.processChange());
        assertEquals(Set.of(new ProcessChange("Q", Change.START)), start.potProcessChange());
        assertFalse(start.reset());
        assertFalse(start.stateChanged());
        assertTrue(start.changesTo().isEmpty());
    }

    @Test
    void stoppingAndFailingOneselfResetsAndMovesToStopOrError() {
        IrProgram program = program("process P :: node N { state p1 { stop; } state p2 { error; } }");
        Attributes stop = of(program, "p1", 0);
        assertEquals(Map.of("P", Change.STOP), stop.processChange());
        assertTrue(stop.reset());
        assertTrue(stop.stateChanged());
        assertEquals(Set.of("stop"), stop.changesTo());
        assertEquals(Set.of("error"), of(program, "p2", 0).changesTo());
    }

    @Test
    void stoppingAnotherProcessDoesNotTouchOnesOwnState() {
        Attributes stop = of(program("process P :: node N { state p1 { stop Q; error Q; } }", OTHER), "p1", 0);
        assertEquals(Map.of("Q", Change.STOP), stop.processChange());
        assertFalse(stop.reset());
        assertTrue(stop.changesTo().isEmpty());
    }

    /** A restart always ends in the first state, but only away from it is it a change of state. */
    @Test
    void restartingEndsInTheFirstState() {
        IrProgram program = program("process P :: node N { state p1 { restart; } state p2 { restart; } }");
        Attributes inFirst = of(program, "p1", 0);
        Attributes elsewhere = of(program, "p2", 0);
        assertEquals(Map.of("P", Change.START), inFirst.processChange());
        assertTrue(inFirst.reset());
        assertFalse(inFirst.stateChanged());
        assertEquals(Set.of("p1"), inFirst.changesTo());
        assertTrue(elsewhere.stateChanged());
        assertEquals(Set.of("p1"), elsewhere.changesTo());
    }

    /** set state resets the timer either way; it changes the state only if it names another. */
    @Test
    void setStateChangesTheStateOnlyToAnotherOne() {
        IrProgram program = program("process P :: node N { state p1 { set state p1; } state p2 { set state p1; } }");
        Attributes same = of(program, "p1", 0);
        Attributes other = of(program, "p2", 0);
        assertTrue(same.reset());
        assertFalse(same.stateChanged());
        assertTrue(other.stateChanged());
        assertEquals(Set.of("p1"), other.changesTo());
    }

    @Test
    void resetTimerOnlyResets() {
        Attributes reset = of(program("process P :: node N { state p1 { reset timer; } }"), "p1", 0);
        assertTrue(reset.reset());
        assertFalse(reset.stateChanged());
        assertTrue(reset.processChange().isEmpty());
    }

    // ------------------------------------------------------------------ Algorithm 2

    /** A later change that only may happen and differs removes the definite one. */
    @Test
    void aPossibleDifferentChangeRemovesADefiniteOne() {
        Attributes state = ofState(program("process P :: node N { state p1 { start Q; if (x) { stop Q; } } }", OTHER), "p1");
        assertTrue(state.processChange().isEmpty());
        assertEquals(Set.of(new ProcessChange("Q", Change.START), new ProcessChange("Q", Change.STOP)),
                state.potProcessChange());
    }

    /** A later change that may happen and agrees leaves the definite one standing. */
    @Test
    void aPossibleSameChangeKeepsADefiniteOne() {
        Attributes state = ofState(program("process P :: node N { state p1 { start Q; if (x) { start Q; } } }", OTHER), "p1");
        assertEquals(Map.of("Q", Change.START), state.processChange());
    }

    /** A later definite change overrides, and the earlier one is no longer even possible. */
    @Test
    void aLaterDefiniteChangeOverrides() {
        Attributes state = ofState(program("process P :: node N { state p1 { stop Q; start Q; } }", OTHER), "p1");
        assertEquals(Map.of("Q", Change.START), state.processChange());
        assertEquals(Set.of(new ProcessChange("Q", Change.START)), state.potProcessChange());
    }

    /** Where a sequence leaves the process: the last construct that always moves it decides. */
    @Test
    void theSequenceEndsWhereItsLastMoveLeavesIt() {
        IrProgram program = program("process P :: node N { state p1 { set state p2; set state p3; }"
                + " state p2 { set state p3; if (x) { set state p1; } } state p3 { a = true; if (x) { set state p1; } } }");
        Attributes always = ofState(program, "p1");
        assertEquals(Set.of("p3"), always.changesTo());
        assertFalse(always.mayStay());
        Attributes either = ofState(program, "p2");
        assertEquals(Set.of("p1", "p3"), either.changesTo());
        assertFalse(either.mayStay());
        // Nothing before the if moves the process, so it may stay where it is.
        assertTrue(ofState(program, "p3").mayStay());
    }

    // ------------------------------------------------------------------ Algorithm 3

    @Test
    void aChangeOnBothBranchesIsDefinite() {
        Attributes state = ofState(program("process P :: node N { state p1 { if (x) { stop Q; } else { a = true; stop Q; } } }",
                OTHER), "p1");
        assertEquals(Map.of("Q", Change.STOP), state.processChange());
    }

    @Test
    void aChangeOnOneBranchOnlyMayHappen() {
        Attributes state = ofState(program("process P :: node N { state p1 { if (x) { stop Q; } else { start Q; } } }",
                OTHER), "p1");
        assertTrue(state.processChange().isEmpty());
        assertEquals(Set.of(new ProcessChange("Q", Change.STOP), new ProcessChange("Q", Change.START)),
                state.potProcessChange());
    }

    /** A switch without default, a loop body and a timeout body may all not run. */
    @Test
    void constructsThatMayNotRunMakeNothingDefinite() {
        IrProgram program = program("process P :: node N {"
                + " state p1 { switch (i) { case 1: stop Q; break; case 2: stop Q; break; } }"
                + " state p2 { for (i = 0; i < 2; i = i + 1) { stop Q; reset timer; } }"
                + " state p3 { a = true; timeout 0t1s { stop Q; } } }", OTHER);
        for (String state : new String[] {"p1", "p2", "p3"}) {
            Attributes attributes = ofState(program, state);
            assertTrue(attributes.processChange().isEmpty(), state);
            assertTrue(attributes.potProcessChange().contains(new ProcessChange("Q", Change.STOP)), state);
            assertFalse(attributes.reset(), state);
        }
    }

    @Test
    void aSwitchWithDefaultChangingOnEveryCaseIsDefinite() {
        Attributes state = ofState(program("process P :: node N {"
                + " state p1 { switch (i) { case 1: stop Q; break; default: stop Q; break; } } }", OTHER), "p1");
        assertEquals(Map.of("Q", Change.STOP), state.processChange());
    }

    @Test
    void theProcessKeepsOnlyWhatEveryStateAgreesOn() {
        IrProgram program = program("process P :: node N { state p1 { stop Q; } state p2 { stop Q; start R; } }",
                OTHER, "process R :: node N { state r1 { a = true; } }");
        Attributes process = new StaticAnalysis(program).attributesOf(program.findProcess("P"));
        assertEquals(Map.of("Q", Change.STOP), process.processChange());
        assertTrue(process.potProcessChange().contains(new ProcessChange("R", Change.START)));
    }
}
