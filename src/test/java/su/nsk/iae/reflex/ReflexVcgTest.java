package su.nsk.iae.reflex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers the pipeline as it is actually invoked: a source file in, theory files out. */
class ReflexVcgTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    @Test
    void generatesTheoriesForAProgram(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs"));
        int written = generator.generate(output);

        // Two of the four cycle paths survive static analysis, plus the base case.
        assertEquals(3, written);
        for (String expected : List.of("ReflexBase.thy", "Requirements.thy", "ifTestTheory.thy",
                "ifTest_VC0.thy", "ifTest_VC1.thy")) {
            assertTrue(Files.exists(output.resolve(expected)), "missing " + expected);
        }

        // VC0 is the base case: it starts from emptyState and assumes no invariant.
        String base = Files.readString(output.resolve("ifTest_VC0.thy"));
        assertTrue(base.contains("st0=emptyState"), base);
        assertFalse(base.contains("base_inv"), base);

        // The rest are inductive steps over one cycle.
        String condition = Files.readString(output.resolve("ifTest_VC1.thy"));
        assertTrue(condition.startsWith("theory ifTest_VC1"), condition);
        assertTrue(condition.contains("imports ifTestTheory LoopInvariants Requirements"), condition);
        assertTrue(condition.contains("base_inv:\"inv(st0)\""), condition);
        assertTrue(condition.contains("shows \"inv(st_final)\""), condition);
        // Values go through ReflexBase's single-constructor state, not the old typed one.
        assertTrue(condition.contains("getVarVal"), condition);
        assertTrue(condition.contains("setVarVal"), condition);
        assertTrue(condition.contains("ValBool"), condition);
    }

    @Test
    void staticAnalysisCanBeSwitchedOff(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs"));
        generator.setStaticAnalysis(false);

        assertEquals(5, generator.generate(output), "every path, plus the base case");
    }

    @Test
    void programTheoryFixesTheClock(@TempDir Path output) throws IOException {
        ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs")).generate(output);

        String theory = Files.readString(output.resolve("ifTestTheory.thy"));
        assertTrue(theory.contains("imports ReflexPatterns"), theory);
        assertTrue(theory.contains("(ltime s p) + 100"), theory);
        // ReflexBase has a single setVar constructor where the old model had four.
        assertTrue(theory.contains("ltime (setVar s _ _) p"), theory);
    }

    @Test
    void exportsTheProgramGraph(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs"));
        generator.exportGraph(output);

        Path graph = output.resolve("ifTest_program_graph.gv");
        assertTrue(Files.exists(graph));
        assertTrue(Files.readString(graph).startsWith("digraph program {"));
    }

    @Test
    void rejectsAProgramThatDoesNotParse(@TempDir Path output) throws IOException {
        Path bad = output.resolve("bad.rcs");
        Files.writeString(bad, "program P { clock ; }");

        IllegalArgumentException raised =
                assertThrows(IllegalArgumentException.class, () -> ReflexVcg.load(bad));
        assertTrue(raised.getMessage().contains("Cannot parse"), raised.getMessage());
    }

    @Test
    void refusesToGenerateForUnsupportedConstructs(@TempDir Path output) throws IOException {
        Path source = output.resolve("ccode.rcs");
        Files.writeString(source, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                // The C code token runs to the end of the line, so the brace goes below it.
                + "    state s {\n      $ a = compute()\n    }\n"
                + "  }\n"
                + "}");

        ReflexVcg generator = ReflexVcg.load(source);
        IllegalStateException raised =
                assertThrows(IllegalStateException.class, () -> generator.generate(output));
        assertTrue(raised.getMessage().contains("does not support"), raised.getMessage());
        assertTrue(raised.getMessage().contains("inline C"), raised.getMessage());
    }

    /**
     * A loop with no {@code [invariant: ...]} is generated all the same, against an
     * invariant named for it and left uninterpreted in the loop theory.
     */
    @Test
    void generatesALoopWithNoInvariantAgainstAPlaceholder(@TempDir Path output) throws IOException {
        Path source = output.resolve("loop.rcs");
        Files.writeString(source, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                + "    state s { for (int32 i = 0; i < 3; i++) { a = i; } }\n"
                + "  }\n"
                + "}");

        ReflexVcg generator = ReflexVcg.load(source);
        assertTrue(generator.generate(output) > 0);

        String loops = Files.readString(output.resolve("LoopInvariants.thy"));
        assertTrue(loops.contains("consts loopInv0 :: \"state \\<Rightarrow> bool\""), loops);
        assertFalse(loops.contains("definition loopInv0"),
                "nothing said what this loop preserves, so nothing should define it");

        assertTrue(anyNamed(output, "LOOPENTRY"),
                "the loop should still produce its entry condition");
        assertTrue(anyNamed(output, "LOOPSTEP"),
                "the loop should still produce its preservation condition");
    }

    /** A loop that was written with an invariant gets it defined, not left open. */
    @Test
    void definesALoopInvariantThatWasWritten(@TempDir Path output) throws IOException {
        ReflexVcg.load(PROGRAMS.resolve("loopSum.rcs")).generate(output);

        String loops = Files.readString(output.resolve("LoopInvariants.thy"));
        assertTrue(loops.contains("definition loopInv0 :: \"state \\<Rightarrow> bool\" where"), loops);
        assertTrue(loops.contains("(theInt (getVarVal s ''#total'' []))"), loops);
        assertFalse(loops.contains("consts"), loops);

        // The conditions name it rather than repeating the formula.
        String entry = Files.readString(output.resolve("LoopSum_LOOPENTRY2.thy"));
        assertTrue(entry.contains("shows \"(loopInv0 st2)\""), entry);
        // `total >= i` is what the invariant says; only the loop theory should spell it.
        assertFalse(entry.contains("\\<ge>"),
                "the formula belongs in the loop theory, not in the condition:\n" + entry);
    }

    private static boolean anyNamed(Path directory, String kind) throws IOException {
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            return files.anyMatch(p -> p.getFileName().toString().contains(kind));
        }
    }

    @Test
    void bindsAnnotationsWhileLoading() throws IOException {
        Path source = Files.createTempFile("annotated", ".rcs");
        Files.writeString(source, "//[invariant: a > 0]\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        ReflexVcg generator = ReflexVcg.load(source);
        assertEquals(1, generator.getProgram().getAnnotations().size());
        assertTrue(generator.getAnnotations().getDiagnostics().isEmpty());
    }
}
