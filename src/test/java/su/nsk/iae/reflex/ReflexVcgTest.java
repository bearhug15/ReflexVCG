package su.nsk.iae.reflex;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Covers the pipeline as it is actually invoked: a source file in, theory files out. */
class ReflexVcgTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    @Test
    void generatesTheoriesForAProgram(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx"));
        int written = generator.generate(output);

        assertEquals(4, written);
        for (String expected : List.of("ReflexBase.thy", "Requirements.thy", "ifTestTheory.thy",
                "ifTest_VC0.thy", "ifTest_VC3.thy")) {
            assertTrue(Files.exists(output.resolve(expected)), "missing " + expected);
        }

        String condition = Files.readString(output.resolve("ifTest_VC0.thy"));
        assertTrue(condition.startsWith("theory ifTest_VC0"), condition);
        assertTrue(condition.contains("imports ifTestTheory Requirements"), condition);
        assertTrue(condition.contains("shows \"inv(st_final)\""), condition);
        // Values go through ReflexBase's single-constructor state, not the old typed one.
        assertTrue(condition.contains("getVarVal"), condition);
        assertTrue(condition.contains("setVarVal"), condition);
        assertTrue(condition.contains("ValBool"), condition);
    }

    @Test
    void programTheoryFixesTheClock(@TempDir Path output) throws IOException {
        ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx")).generate(output);

        String theory = Files.readString(output.resolve("ifTestTheory.thy"));
        assertTrue(theory.contains("imports ReflexPatterns"), theory);
        assertTrue(theory.contains("(ltime s p) + 100"), theory);
        // ReflexBase has a single setVar constructor where the old model had four.
        assertTrue(theory.contains("ltime (setVar s _ _) p"), theory);
    }

    @Test
    void exportsTheProgramGraph(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx"));
        generator.exportGraph(output);

        Path graph = output.resolve("ifTest_program_graph.gv");
        assertTrue(Files.exists(graph));
        assertTrue(Files.readString(graph).startsWith("digraph program {"));
    }

    @Test
    void rejectsAProgramThatDoesNotParse(@TempDir Path output) throws IOException {
        Path bad = output.resolve("bad.rx");
        Files.writeString(bad, "program P { clock ; }");

        IllegalArgumentException raised =
                assertThrows(IllegalArgumentException.class, () -> ReflexVcg.load(bad));
        assertTrue(raised.getMessage().contains("Cannot parse"), raised.getMessage());
    }

    @Test
    void refusesToGenerateForUnsupportedConstructs(@TempDir Path output) throws IOException {
        Path source = output.resolve("loop.rx");
        Files.writeString(source, "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                + "    state s { for (int32 i = 0; i < 3; i++) { a = i; } }\n"
                + "  }\n"
                + "}");

        ReflexVcg generator = ReflexVcg.load(source);
        IllegalStateException raised =
                assertThrows(IllegalStateException.class, () -> generator.generate(output));
        assertTrue(raised.getMessage().contains("does not support"), raised.getMessage());
        assertTrue(raised.getMessage().contains("for"), raised.getMessage());
    }

    @Test
    void bindsAnnotationsWhileLoading() throws IOException {
        Path source = Files.createTempFile("annotated", ".rx");
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
