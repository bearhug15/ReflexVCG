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
 * The extra-invariant stage: its hooks are reached and can influence the output, it
 * changes nothing unless asked, and when asked every invariant it lets a condition assume
 * is also proved.
 */
class ExtraInvariantGeneratorTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    @Test
    void defaultGeneratorChangesNothing(@TempDir Path output) throws IOException {
        int written = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs")).generate(output);

        assertEquals(3, written);
        String requirements = Files.readString(output.resolve("Requirements.thy"));
        assertTrue(requirements.contains("definition inv where"), requirements);
    }

    @Test
    void everyHookIsReached(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs"));

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
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("ifTest1.rcs"));
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
        Path source = Files.createTempFile("annotated", ".rcs");
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

    // ------------------------------------------------------------------ extra invariants

    /** The stage does nothing unless asked: no theory, no obligations, no imports. */
    @Test
    void noExtraInvariantsUnlessAskedFor(@TempDir Path output) throws IOException {
        ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs")).generate(output);

        assertFalse(Files.exists(output.resolve("ExtraInvariants.thy")));
        for (Path theory : theories(output, "_")) {
            assertFalse(Files.readString(theory).contains("ExtraInvariants"), theory.toString());
        }
    }

    /**
     * Each extra invariant is assumed only where it is proved: every condition assuming the
     * global invariant has a companion showing the extra ones kept, and the base case one
     * showing they hold to begin with.
     */
    @Test
    void everyCycleProvesTheExtraInvariantsItMayAssume(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantLevel(ExtraInvariantGenerator.Level.ADVANCED);
        generator.generate(output);

        String extra = Files.readString(output.resolve("ExtraInvariants.thy"));
        assertTrue(extra.contains("definition extraInv :: \"state \\<Rightarrow> bool\" where"), extra);
        assertTrue(extra.contains("definition extra_states_HeaterController"), extra);
        assertFalse(extra.contains("extra_trans_"), "transitions are optional: " + extra);

        List<Path> cycles = theories(output, "_VC");
        List<Path> obligations = theories(output, "_EXTRA");
        assertEquals(cycles.size(), obligations.size(), "one obligation per main condition");

        int bases = 0;
        for (Path obligation : obligations) {
            String text = Files.readString(obligation);
            assertTrue(text.contains("imports ThermopotTheory LoopInvariants Requirements ExtraInvariants"), text);
            assertTrue(text.contains("shows \"(extraInv st_final)\""), text);
            if (text.contains("base_inv:")) {
                assertTrue(text.contains("extra_inv:\"(extraInv st0)\""), text);
            } else {
                bases++;
            }
        }
        assertEquals(1, bases, "the base case");
    }

    /** A condition gets the invariants about the states its path passes through. */
    @Test
    void aConditionAssumesWhatConcernsItsStates(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantLevel(ExtraInvariantGenerator.Level.ALL);
        generator.generate(output);

        int checked = 0;
        for (Path condition : theories(output, "_VC")) {
            String text = Files.readString(condition);
            if (!text.contains("base_inv:")) {
                continue;
            }
            assertTrue(text.contains("extra_states_HeaterController:\"(extra_states_HeaterController st0)\""),
                    text);
            boolean maintaining = text.contains("''HeaterController''=''maintaining''")
                    || text.contains("''HeaterController'' ''maintaining''");
            boolean heating = text.contains("''HeaterController''=''heating''")
                    || text.contains("''HeaterController'' ''heating''");
            assertEquals(maintaining, text.contains("extra_vars_HeaterController_maintaining:"), text);
            assertEquals(heating, text.contains("extra_trans_HeaterController_heating:"), text);
            checked++;
        }
        assertTrue(checked > 10, "cycles checked: " + checked);
    }

    private static List<Path> theories(Path directory, String marker) throws IOException {
        try (var files = Files.list(directory)) {
            return files.filter(f -> f.getFileName().toString().contains(marker)
                            && f.getFileName().toString().endsWith(".thy"))
                    .sorted().toList();
        }
    }
}
