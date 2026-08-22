package su.nsk.iae.reflex.cfg;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.vc.IsabelleRenderer;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the whole new pipeline over the translated test programs: parse with the new
 * grammar, preprocess, build the control-flow graph, enumerate paths, render Isabelle.
 *
 * <p>The counts here are compared against the recorded behaviour of the old pipeline with
 * static analysis switched off, which is the like-for-like comparison: the new pipeline
 * does no pruning yet, by design, since the static analysis rework comes last.
 */
class NewPipelineEndToEndTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    /**
     * Programs that only generate with annotations enabled. This test loads without an
     * annotation binder, so a loop here has no invariant to be cut by; the annotated
     * pipeline is covered by {@link su.nsk.iae.reflex.ann.AnnotatedGenerationTest}.
     */
    private static final List<String> NEEDS_ANNOTATIONS = List.of("annotatedTank.rx");

    private static IrProgram load(Path file) throws IOException {
        NewReflexLexer lexer = new NewReflexLexer(CharStreams.fromPath(file));
        BufferedTokenStream tokens = new CommonTokenStream(lexer);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(),
                file.getFileName() + " should parse under the new grammar");
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    private static List<Path> programs() throws IOException {
        try (Stream<Path> files = Files.list(PROGRAMS)) {
            return files.filter(p -> p.toString().endsWith(".rx")).sorted().toList();
        }
    }

    @Test
    void everyTranslatedProgramSurvivesTheWholePipeline() throws IOException {
        List<Path> programs = programs();
        assertFalse(programs.isEmpty(), "no translated programs found in " + PROGRAMS.toAbsolutePath());

        IsabelleRenderer renderer = new IsabelleRenderer();
        for (Path file : programs) {
            IrProgram program = load(file);
            Cfg cfg = new CfgBuilder(program).build();

            if (NEEDS_ANNOTATIONS.contains(file.getFileName().toString())) {
                assertFalse(cfg.unsupportedNodes().isEmpty(),
                        file.getFileName() + " should report its loop when annotations are off");
                continue;
            }

            List<VerificationCondition> conditions = new PathEnumerator(cfg).enumerate();
            assertFalse(conditions.isEmpty(), file.getFileName() + " produced no conditions");
            System.out.printf("pipeline %-20s %5d conditions%n", file.getFileName(), conditions.size());

            for (VerificationCondition condition : conditions) {
                String lemma = renderer.renderLemma(condition);
                assertNotNull(lemma);
                assertTrue(lemma.startsWith("lemma\nassumes "), file + ": " + lemma);
                assertTrue(lemma.endsWith("shows \"inv(st_final)\""), file + ": " + lemma);
            }
        }
    }

    /**
     * With no pruning, the number of paths should match what the old pipeline produced
     * with static analysis off, less its one extra condition for program start-up, which
     * the new generator does not emit as a path.
     */
    @Test
    void pathCountsMatchTheUnprunedBaseline() throws IOException {
        /**
         * @param oldNoAnalysisVcs what the old pipeline emitted with -a false
         * @param knownDifference  0 where the two agree; otherwise how many conditions
         *                         the old pipeline emitted that this one deliberately
         *                         does not, with the reason
         */
        record Expected(String program, int oldNoAnalysisVcs, int knownDifference, String reason) {
        }
        List<Expected> expectations = List.of(
                new Expected("ifTest1", 5, 0, ""),
                // `var1/2>0`: the old generator attached a "domain" condition to each
                // division and emitted it as an extra condition in which the condition is
                // *assumed*. A division-by-zero requirement is something to prove, not to
                // assume, so it is not reproduced pending a decision on what it should be.
                new Expected("ifTest2", 7, 2, "division domain conditions"),
                new Expected("ifTest3", 6, 0, ""),
                new Expected("switchTest1", 6, 0, ""),
                new Expected("switchTest2", 7, 0, ""),
                // The realistic programs, the largest of which has 1008 paths.
                new Expected("newBarrier", 73, 0, ""),
                new Expected("newEscalator", 43, 0, ""),
                new Expected("newSmartLighting", 487, 0, ""),
                new Expected("newThermopot", 181, 0, ""),
                new Expected("newTurnstile", 1009, 0, ""));

        StringBuilder report = new StringBuilder();
        boolean mismatch = false;
        for (Expected expected : expectations) {
            IrProgram program = load(PROGRAMS.resolve(expected.program() + ".rx"));
            Cfg cfg = new CfgBuilder(program).build();
            int paths = new PathEnumerator(cfg).enumerate().size();
            // The old pipeline emitted one further condition for program start-up, which
            // is not a path through a cycle and so is not enumerated here.
            int want = expected.oldNoAnalysisVcs() - 1 - expected.knownDifference();

            boolean ok = paths == want;
            report.append(String.format("%-14s expected %3d  actual %3d  %s%s%n",
                    expected.program(), want, paths, ok ? "ok" : "MISMATCH",
                    expected.knownDifference() == 0 ? ""
                            : "  (excludes " + expected.knownDifference() + ": " + expected.reason() + ")"));
            mismatch |= !ok;
        }
        assertFalse(mismatch, "path counts differ from the unpruned baseline:\n" + report);
    }

    @Test
    void reportsUnsupportedConstructsRatherThanIgnoringThem() throws IOException {
        Path file = Files.createTempFile("unsupported", ".rx");
        Files.writeString(file, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                + "    state s { for (int32 i = 0; i < 3; i++) { a = i; } }\n"
                + "  }\n"
                + "}");

        IrProgram program = load(file);
        Cfg cfg = new CfgBuilder(program).build();

        assertEquals(1, cfg.unsupportedNodes().size(), "the for loop should appear as unsupported");
        assertEquals("for", cfg.unsupportedNodes().get(0).getConstruct());

        PathEnumerator.UnsupportedConstructException raised = org.junit.jupiter.api.Assertions
                .assertThrows(PathEnumerator.UnsupportedConstructException.class,
                        () -> new PathEnumerator(cfg).enumerate());
        assertTrue(raised.getMessage().contains("not supported"), raised.getMessage());
    }

    /**
     * A write nested in a larger expression is a state of its own, so the reads around it
     * do not all happen in the same state: {@code v++} yields the value from before its
     * own write, and the read beside it the value after.
     */
    @Test
    void modelsWritesNestedInsideExpressions() throws IOException {
        Path file = Files.createTempFile("nested", ".rx");
        Files.writeString(file, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int8 v = 0;\n"
                + "  process Proc :: node N { state s { v = v++ + v; } }\n"
                + "}");

        Cfg cfg = new CfgBuilder(load(file)).build();
        assertTrue(cfg.unsupportedNodes().isEmpty(),
                "a write inside an expression is modelled, not reported");

        // One path per state the process can be in; only the declared one runs anything.
        IsabelleRenderer renderer = new IsabelleRenderer();
        String lemma = new PathEnumerator(cfg).enumerate().stream()
                .map(renderer::renderLemma)
                .filter(l -> l.contains("''#v''"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no condition performs the writes"));

        // The increment is a write of its own, and the sum reads either side of it.
        assertTrue(lemma.contains("st1=(setVarVal st0 ''#v'' [] (ValInt "
                + "((theInt (getVarVal st0 ''#v'' [])) + 1)))"), lemma);
        assertTrue(lemma.contains("st2=(setVarVal st1 ''#v'' [] (ValInt "
                + "((theInt (getVarVal st0 ''#v'' [])) "
                + "+ (theInt (getVarVal st1 ''#v'' [])))))"), lemma);
    }

    /** A prefix increment reads the value its own write produced; a postfix one does not. */
    @Test
    void prefixAndPostfixDifferInWhichValueTheyRead() throws IOException {
        Path file = Files.createTempFile("prefix", ".rx");
        Files.writeString(file, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int8 v = 0;\n"
                + "  int8 w = 0;\n"
                + "  process Proc :: node N { state s { w = ++v; } }\n"
                + "}");

        Cfg cfg = new CfgBuilder(load(file)).build();
        String lemma = new IsabelleRenderer().renderLemma(
                new PathEnumerator(cfg).enumerate().get(0));

        // st1 performs the increment, and w is then given what st1 holds.
        assertTrue(lemma.contains("st2=(setVarVal st1 ''#w'' [] (ValInt "
                + "(theInt (getVarVal st1 ''#v'' []))))"), lemma);
    }

    /**
     * A write in the right operand of a short-circuiting connective happens only on the
     * paths that actually evaluate it - that is the whole point of short-circuiting, and
     * the reason such a write has to be modelled rather than lifted out of the condition.
     */
    @Test
    void aWriteBehindAShortCircuitHappensOnlyWhereItRuns() throws IOException {
        Path file = Files.createTempFile("shortcircuit", ".rx");
        Files.writeString(file, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  bool a = false;\n"
                + "  int8 v = 0;\n"
                + "  int8 seen = 0;\n"
                + "  process Proc :: node N { state s { if (a && (v++ < 3)) { seen = 1; } } }\n"
                + "}");

        Cfg cfg = new CfgBuilder(load(file)).build();
        assertTrue(cfg.unsupportedNodes().isEmpty(), "the write is modelled");

        IsabelleRenderer renderer = new IsabelleRenderer();
        List<String> lemmas = new ArrayList<>();
        new PathEnumerator(cfg).enumerate().forEach(c -> lemmas.add(renderer.renderLemma(c)));

        long incrementing = lemmas.stream().filter(l -> l.contains("''#v''")).count();
        assertTrue(incrementing > 0, "some path evaluates the right operand");
        assertTrue(incrementing < lemmas.size(),
                "the path where `a` is false must not perform the increment");
    }

    /** A write at the top of a statement is the ordinary case and stays supported. */
    @Test
    void allowsWritesAtTheTopOfAStatement() throws IOException {
        Path file = Files.createTempFile("plain", ".rx");
        Files.writeString(file, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int8 v = 0;\n"
                + "  process Proc :: node N { state s { v = v + 1; v++; } }\n"
                + "}");

        Cfg cfg = new CfgBuilder(load(file)).build();
        assertTrue(cfg.unsupportedNodes().isEmpty(), "both statements are ordinary writes");
    }

    @Test
    void exportsAGraphvizGraph() throws IOException {
        IrProgram program = load(PROGRAMS.resolve("ifTest1.rx"));
        String dot = new CfgBuilder(program).build().toDot();

        assertTrue(dot.startsWith("digraph program {"), dot);
        assertTrue(dot.contains("Init in begin"), dot);
        assertTrue(dot.contains("toEnv"), dot);
    }
}
