package su.nsk.iae.reflex.vc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The extra-invariant stage does nothing by default, so what is worth testing is that
 * its hooks are actually reached and can influence the output - that is the point of
 * having them now.
 */
class ExtraInvariantGeneratorTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    @Test
    void defaultGeneratorChangesNothing(@TempDir Path output) throws IOException {
        int written = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx")).generate(output);

        assertEquals(3, written);
        String requirements = Files.readString(output.resolve("Requirements.thy"));
        assertTrue(requirements.contains("definition inv where"), requirements);
    }

    @Test
    void everyHookIsReached(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx"));

        List<String> calls = new ArrayList<>();
        generator.setExtraInvariantGenerator(new ExtraInvariantGenerator(
                generator.getProgram(), generator.getCfg(), generator.getAnnotations()) {
            @Override
            public void analyse() {
                calls.add("analyse");
            }

            @Override
            public List<String> extraDefinitions() {
                calls.add("extraDefinitions");
                return List.of("definition extra where\n\"extra s = True\"\n");
            }

            @Override
            public VerificationCondition process(VerificationCondition condition) {
                calls.add("process");
                return condition;
            }
        });

        int written = generator.generate(output);

        assertEquals(3, written);
        assertTrue(calls.contains("analyse"), calls.toString());
        assertTrue(calls.contains("extraDefinitions"), calls.toString());
        assertEquals(3, calls.stream().filter("process"::equals).count(),
                "process should be called once per condition, base case included");

        assertTrue(Files.readString(output.resolve("Requirements.thy")).contains("definition extra where"),
                "extra definitions should reach the requirements theory");
    }

    /** Returning null from process drops a condition, so the hook can filter. */
    @Test
    void processCanDropConditions(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rx"));
        generator.setExtraInvariantGenerator(new ExtraInvariantGenerator(
                generator.getProgram(), generator.getCfg(), generator.getAnnotations()) {
            private int seen;

            @Override
            public VerificationCondition process(VerificationCondition condition) {
                return seen++ % 2 == 0 ? condition : null;
            }
        });

        assertEquals(2, generator.generate(output), "every other condition is dropped");
    }

    @Test
    void annotationsAreAvailableToTheStage() throws IOException {
        Path source = Files.createTempFile("annotated", ".rx");
        Files.writeString(source, "//[invariant: a > 0]\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  //[assume: b > 0]\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        ReflexVcg generator = ReflexVcg.load(source);
        IrProgram program = generator.getProgram();
        Cfg cfg = generator.getCfg();
        AnnotationBinder binder = generator.getAnnotations();

        var stage = new ExtraInvariantGenerator(program, cfg, binder) {
            List<Annotation> invariants() {
                return annotationsOfKind(Annotation.Kind.INVARIANT);
            }

            List<Annotation> all() {
                return getAnnotations();
            }
        };

        assertEquals(2, stage.all().size());
        assertEquals(1, stage.invariants().size());
        assertFalse(stage.invariants().get(0).getText().isEmpty());
    }
}
