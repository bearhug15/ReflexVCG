package su.nsk.iae.reflex.preprocess;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalizationPassTest {

    private static IrProgram normalized(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        IrProgram program = new AstBuilder().build(ctx);

        NameManglingPass mangling = new NameManglingPass();
        mangling.run(program);
        new CastInsertionPass(new TypeEnvironment(program, mangling.getDirectAccessNames())).run(program);
        new NormalizationPass().run(program);
        return program;
    }

    private static IrProgram withStates(String states) {
        return normalized("program P {\n"
                + "  clock 100;\n"
                + "  bool cond;\n"
                + "  int32 a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n" + states + "\n  }\n"
                + "}");
    }

    private static IrProcess proc(IrProgram program) {
        return program.getProcesses().get(0);
    }

    private static List<String> stateNames(IrProgram program) {
        return proc(program).getStates().stream().map(IrState::getName).toList();
    }

    /** Every statement in the subtree, flattened, so rewrites can be inspected. */
    private static List<IrStmt> flatten(List<IrStmt> statements) {
        List<IrStmt> all = new ArrayList<>();
        statements.forEach(s -> flattenInto(s, all));
        return all;
    }

    private static void flattenInto(IrStmt stmt, List<IrStmt> out) {
        out.add(stmt);
        if (stmt instanceof IrStmt.Block block) {
            block.getStatements().forEach(s -> flattenInto(s, out));
        } else if (stmt instanceof IrStmt.If ifStmt) {
            flattenInto(ifStmt.getThenBranch(), out);
            if (ifStmt.getElseBranch() != null) {
                flattenInto(ifStmt.getElseBranch(), out);
            }
        } else if (stmt instanceof IrStmt.Switch switchStmt) {
            switchStmt.getCases().forEach(c -> c.getStatements().forEach(s -> flattenInto(s, out)));
        }
    }

    private static List<IrStmt> allStatements(IrProgram program) {
        List<IrStmt> all = new ArrayList<>();
        proc(program).getStates().forEach(s -> all.addAll(flatten(s.getStatements())));
        return all;
    }

    // ------------------------------------------------------------------ set next state

    @Test
    void resolvesSetNextStateToTheFollowingState() {
        IrProgram program = withStates(
                "    state first { set next state; }\n"
              + "    state second { ; }");

        IrStmt.SetState setState = assertInstanceOf(IrStmt.SetState.class,
                proc(program).getStates().get(0).getStatements().get(0));
        assertEquals("second", setState.getState());
    }

    /** Resolution uses the original ordering, before light states are appended. */
    @Test
    void resolvesSetNextStateAgainstOriginalOrderNotSynthesisedStates() {
        IrProgram program = withStates(
                "    state first { slice; a = 1; }\n"
              + "    state second { set next state; }\n"
              + "    state third { ; }");

        IrState second = proc(program).findState("second");
        IrStmt.SetState setState = assertInstanceOf(IrStmt.SetState.class, second.getStatements().get(0));
        assertEquals("third", setState.getState(),
                "the light state split out of `first` must not become the successor");
    }

    @Test
    void rejectsSetNextStateInTheLastState() {
        assertThrows(IllegalStateException.class,
                () -> withStates("    state only { set next state; }"));
    }

    // ------------------------------------------------------------------ slice

    @Test
    void splitsStateAtSliceIntoALightState() {
        IrProgram program = withStates("    state s { a = 1; slice; a = 2; }");

        assertEquals(2, proc(program).getStates().size());
        List<String> names = stateNames(program);
        assertEquals("s", names.get(0));
        assertTrue(names.get(1).startsWith("s_light"), "got " + names.get(1));

        // The original keeps what came before the slice and then transfers.
        List<IrStmt> body = proc(program).getStates().get(0).getStatements();
        assertEquals(2, body.size());
        assertInstanceOf(IrStmt.ExprStatement.class, body.get(0));
        assertEquals(names.get(1),
                assertInstanceOf(IrStmt.SetState.class, body.get(1)).getState());

        // The light state carries what came after it.
        IrState light = proc(program).getStates().get(1);
        assertTrue(light.isSynthetic());
        assertEquals(1, light.getStatements().size());
        assertInstanceOf(IrStmt.ExprStatement.class, light.getStatements().get(0));
    }

    @Test
    void removesEverySliceAndWaitFromTheProgram() {
        IrProgram program = withStates(
                "    state s { a = 1; slice; wait (cond); a = 2; }");

        allStatements(program).forEach(stmt -> {
            assertFalse(stmt instanceof IrStmt.Slice, "a slice survived normalisation");
            assertFalse(stmt instanceof IrStmt.Wait, "a wait survived normalisation");
        });
    }

    @Test
    void splitsConsecutiveSlicesIntoAChainOfStates() {
        IrProgram program = withStates("    state s { a = 1; slice; a = 2; slice; a = 3; }");

        assertEquals(3, proc(program).getStates().size(), stateNames(program).toString());
        assertTrue(proc(program).getStates().get(1).isSynthetic());
        assertTrue(proc(program).getStates().get(2).isSynthetic());
    }

    // ------------------------------------------------------------------ wait

    @Test
    void turnsWaitIntoAConditionalWithAWaitingState() {
        IrProgram program = withStates("    state s { wait (cond); a = 1; }");

        List<String> names = stateNames(program);
        assertTrue(names.size() >= 2, "a waiting state should have been created: " + names);

        // The state now branches on the condition instead of blocking.
        IrStmt.If branch = assertInstanceOf(IrStmt.If.class,
                proc(program).getStates().get(0).getStatements().get(0));
        assertNotNull(branch.getCondition());
        assertNotNull(branch.getElseBranch(), "the else branch parks in the waiting state");

        IrStmt.Block elseBlock = assertInstanceOf(IrStmt.Block.class, branch.getElseBranch());
        IrStmt.SetState park = assertInstanceOf(IrStmt.SetState.class, elseBlock.getStatements().get(0));
        assertTrue(park.getState().startsWith("s_light"), "got " + park.getState());

        // The waiting state re-tests the same condition.
        IrState waiting = proc(program).findState(park.getState());
        assertNotNull(waiting);
        assertTrue(waiting.isSynthetic());
        assertInstanceOf(IrStmt.If.class, waiting.getStatements().get(0));
    }

    /** The condition must be copied, not shared, or rewriting one would change both. */
    @Test
    void doesNotShareTheConditionBetweenTheStateAndItsWaitingState() {
        IrProgram program = withStates("    state s { wait (cond); a = 1; }");

        IrStmt.If branch = assertInstanceOf(IrStmt.If.class,
                proc(program).getStates().get(0).getStatements().get(0));
        IrStmt.Block elseBlock = assertInstanceOf(IrStmt.Block.class, branch.getElseBranch());
        String waitingName = assertInstanceOf(IrStmt.SetState.class,
                elseBlock.getStatements().get(0)).getState();

        IrStmt.If waitingBranch = assertInstanceOf(IrStmt.If.class,
                proc(program).findState(waitingName).getStatements().get(0));

        assertFalse(branch.getCondition() == waitingBranch.getCondition(),
                "the two conditions must be distinct objects");
    }

    @Test
    void carriesAWaitTimeoutIntoTheWaitingState() {
        IrProgram program = withStates("    state s { wait (cond) on timeout 0t5s { a = 9; }; a = 1; }");

        IrState waiting = proc(program).getStates().stream()
                .filter(IrState::isSynthetic)
                .filter(s -> s.getTimeout() != null)
                .findFirst()
                .orElse(null);

        assertNotNull(waiting, "the waiting state should carry the timeout: " + stateNames(program));
        assertEquals("0t5s", waiting.getTimeout().getDuration().getText());
        assertNotNull(waiting.getTimeout().getBody());
    }

    // ------------------------------------------------------------------ switch

    @Test
    void appendsFallthroughStatementsToTheCaseAbove() {
        IrProgram program = withStates(
                "    state s { switch (a) { case 1: { a = 10; } case 2: { a = 20; break; } } }");

        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class,
                proc(program).getStates().get(0).getStatements().get(0));

        IrStmt.SwitchCase first = switchStmt.getCases().get(0);
        assertEquals(2, first.getStatements().size(),
                "case 1 falls through, so it gains case 2's statement");
        assertTrue(first.isBreaks(), "the chain ends where the fallthrough target breaks");

        IrStmt.SwitchCase second = switchStmt.getCases().get(1);
        assertEquals(1, second.getStatements().size(), "the breaking case is unchanged");
    }

    @Test
    void leavesCasesThatAlreadyBreakAlone() {
        IrProgram program = withStates(
                "    state s { switch (a) { case 1: { a = 10; break; } case 2: { a = 20; break; } } }");

        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class,
                proc(program).getStates().get(0).getStatements().get(0));

        assertEquals(1, switchStmt.getCases().get(0).getStatements().size());
        assertEquals(1, switchStmt.getCases().get(1).getStatements().size());
    }

    @Test
    void fallsThroughIntoTheDefaultCase() {
        IrProgram program = withStates(
                "    state s { switch (a) { case 1: { a = 10; } default: { a = 30; } } }");

        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class,
                proc(program).getStates().get(0).getStatements().get(0));

        IrStmt.SwitchCase first = switchStmt.getCases().get(0);
        assertEquals(2, first.getStatements().size(), "case 1 falls into default");
        assertTrue(first.isBreaks());
    }

    @Test
    void copiesFallthroughStatementsRatherThanSharingThem() {
        IrProgram program = withStates(
                "    state s { switch (a) { case 1: { a = 10; } case 2: { a = 20; break; } } }");

        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class,
                proc(program).getStates().get(0).getStatements().get(0));

        IrStmt appended = switchStmt.getCases().get(0).getStatements().get(1);
        IrStmt original = switchStmt.getCases().get(1).getStatements().get(0);
        assertFalse(appended == original, "the appended statement must be a copy");
    }

    @Test
    void expandsAChainOfSeveralFallingThroughCases() {
        IrProgram program = withStates(
                "    state s { switch (a) { case 1: { a = 1; } case 2: { a = 2; } case 3: { a = 3; break; } } }");

        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class,
                proc(program).getStates().get(0).getStatements().get(0));

        assertEquals(3, switchStmt.getCases().get(0).getStatements().size(), "case 1 gains 2 and 3");
        assertEquals(2, switchStmt.getCases().get(1).getStatements().size(), "case 2 gains 3");
        assertEquals(1, switchStmt.getCases().get(2).getStatements().size());
    }

    // ------------------------------------------------------------------ interaction

    @Test
    void normalisesSwitchesNestedInsideSplitStates() {
        IrProgram program = withStates(
                "    state s { slice; switch (a) { case 1: { a = 10; } case 2: { a = 20; break; } } }");

        IrState light = proc(program).getStates().get(1);
        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class, light.getStatements().get(0));
        assertEquals(2, switchStmt.getCases().get(0).getStatements().size(),
                "fallthrough is expanded even inside a synthesised state");
    }

    @Test
    void leavesStatesWithoutSuspensionPointsUntouched() {
        IrProgram program = withStates("    state s { a = 1; a = 2; }");

        assertEquals(1, proc(program).getStates().size());
        assertEquals(2, proc(program).getStates().get(0).getStatements().size());
        assertFalse(proc(program).getStates().get(0).isSynthetic());
    }
}
