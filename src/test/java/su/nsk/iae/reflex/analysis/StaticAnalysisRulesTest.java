package su.nsk.iae.reflex.analysis;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgBuilder;
import su.nsk.iae.reflex.cfg.PathEnumerator;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Each incompatibility rule on a program small enough that the expected outcome can be
 * counted by hand. Section numbers refer to IvReadings2026.
 */
class StaticAnalysisRulesTest {

    private static IrProgram parse(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse");
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    private static int paths(IrProgram program, boolean analyse) {
        AttributePreparation preparation = new AttributePreparation(program);
        preparation.run();
        Cfg cfg = new CfgBuilder(program, preparation).build();
        StaticAnalysis analysis = analyse ? new StaticAnalysis(program) : null;
        return new PathEnumerator(cfg, analysis).enumerate().size();
    }

    private static List<VerificationCondition> conditions(IrProgram program) {
        AttributePreparation preparation = new AttributePreparation(program);
        preparation.run();
        Cfg cfg = new CfgBuilder(program, preparation).build();
        return new PathEnumerator(cfg, new StaticAnalysis(program)).enumerate();
    }

    /** 4.1: a process that nothing can stop or fail cannot be in stop or error. */
    @Test
    void discardsUnreachableStopAndErrorStates() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { a = true; } }\n"
                + "}");

        // Unpruned: the one real state plus the stop and error pseudo-states.
        assertEquals(3, paths(program, false));
        assertEquals(1, paths(program, true), "stop and error are unreachable here");
    }

    /** 4.1 again: once something can stop the process, stop becomes reachable. */
    @Test
    void keepsStopWhenTheProcessCanBeStopped() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { stop; } }\n"
                + "}");

        assertTrue(paths(program, true) > 1, "the stop state is reachable and must be kept");
    }

    /**
     * 4.2: a timeout cannot have elapsed on a path that reset the timer, since ltime is
     * then zero.
     */
    @Test
    void discardsTimeoutBranchAfterATimerReset() {
        IrProgram withReset = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { reset timer; timeout 0t5s { a = true; } }\n"
                + "    state t { a = false; }\n"
                + "  }\n"
                + "}");

        List<VerificationCondition> kept = conditions(withReset);
        boolean anyTimeoutReached = kept.stream()
                .flatMap(c -> c.getStatements().stream())
                .anyMatch(s -> s instanceof su.nsk.iae.reflex.vc.VcStatement.TimeoutCheck check
                        && check.exceeded());
        assertTrue(!anyTimeoutReached,
                "no surviving condition should both reset the timer and reach the timeout");
    }

    /** Without the reset, both sides of the timeout survive. */
    @Test
    void keepsBothTimeoutBranchesWithoutAReset() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { a = true; timeout 0t5s { a = false; } }\n"
                + "    state t { a = false; }\n"
                + "  }\n"
                + "}");

        boolean anyTimeoutReached = conditions(program).stream()
                .flatMap(c -> c.getStatements().stream())
                .anyMatch(s -> s instanceof su.nsk.iae.reflex.vc.VcStatement.TimeoutCheck check
                        && check.exceeded());
        assertTrue(anyTimeoutReached, "the timeout branch is possible when nothing reset the timer");
    }

    /** `set state` resets the timer too, so it blocks the timeout branch the same way. */
    @Test
    void setStateAlsoBlocksTheTimeoutBranch() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { set state t; timeout 0t5s { a = false; } }\n"
                + "    state t { a = false; }\n"
                + "  }\n"
                + "}");

        boolean anyTimeoutReached = conditions(program).stream()
                .flatMap(c -> c.getStatements().stream())
                .anyMatch(s -> s instanceof su.nsk.iae.reflex.vc.VcStatement.TimeoutCheck check
                        && check.exceeded());
        assertTrue(!anyTimeoutReached, "set state resets the timer, so the timeout cannot have elapsed");
    }

    /** 4.6: a state assertion that contradicts a status the path already assumed. */
    @Test
    void discardsStateContradictingAnAssumedStatus() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Watcher :: node N {\n"
                + "    state w { if (process Worker in state stop) { a = true; } }\n"
                + "  }\n"
                + "  process Worker :: node N { state one { a = false; } state two { stop; } }\n"
                + "}");

        // Every surviving condition that assumed Worker is stopped must place it in stop.
        // The assertion has to be read off the IR: the else branch's guard is the
        // negation, whose text also mentions "in state stop" but asserts the opposite.
        for (VerificationCondition condition : conditions(program)) {
            boolean assumedStopped = condition.getStatements().stream()
                    .anyMatch(s -> s instanceof su.nsk.iae.reflex.vc.VcStatement.Condition c
                            && Term.assertedBy(c.expr()).contains(
                                    new Term.ProcessActivity("Worker", Term.Activity.STOP)));
            if (!assumedStopped) {
                continue;
            }
            boolean placesWorkerElsewhere = condition.getStatements().stream()
                    .anyMatch(s -> s instanceof su.nsk.iae.reflex.vc.VcStatement.ProcessInState p
                            && p.process().equals("Worker") && !p.pstate().equals("stop"));
            assertTrue(!placesWorkerElsewhere,
                    "a condition assuming Worker is stopped cannot also place it in another state");
        }
    }

    /** Pruning never adds conditions, whatever the program. */
    @Test
    void pruningOnlyEverRemoves() {
        IrProgram program = parse("program P {\n"
                + "  clock 100;\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process First :: node N { state s { start Second; a = true; } }\n"
                + "  process Second :: node N { state one { a = false; } state two { stop; } }\n"
                + "}");

        assertTrue(paths(program, true) <= paths(program, false));
    }
}
