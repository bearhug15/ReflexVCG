package su.nsk.iae.reflex.vc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.inv.ExtraInvariant;
import su.nsk.iae.reflex.inv.ExtraInvariants;
import su.nsk.iae.reflex.inv.Selection;
import su.nsk.iae.reflex.inv.Tag;
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

    // ------------------------------------------------------------------ priorities

    /** -x none: the output exactly as it was before there were extra invariants. */
    @Test
    void noneLeavesTheOutputAsItWas(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantSelection(Selection.none());
        generator.generate(output);

        assertFalse(Files.exists(output.resolve("ExtraInvariants.thy")));
        assertTrue(Files.readString(output.resolve("Requirements.thy")).contains("imports ThermopotTheory"));
        for (Path theory : theories(output, "_")) {
            assertFalse(Files.readString(theory).contains("extra"), theory.toString());
        }
    }

    /**
     * The default, high priority: which states each process can be in joins the global
     * invariant, so every condition assumes and proves it - and no condition is added.
     */
    @Test
    void theHighPriorityJoinsTheGlobalInvariant(@TempDir Path output) throws IOException {
        Path withoutExtras = Files.createDirectories(output.resolve("none"));
        Path withHigh = Files.createDirectories(output.resolve("high"));
        ReflexVcg none = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        none.setExtraInvariantSelection(Selection.none());
        int before = none.generate(withoutExtras);
        int after = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs")).generate(withHigh);

        assertEquals(before, after, "no condition is added");
        assertEquals(names(theories(withoutExtras, "_")), names(theories(withHigh, "_")));

        String requirements = Files.readString(withHigh.resolve("Requirements.thy"));
        assertTrue(requirements.contains("imports ExtraInvariants"), requirements);
        assertTrue(requirements.contains("\\<and> (extra_states_HeaterController s)"), requirements);
        String extra = Files.readString(withHigh.resolve("ExtraInvariants.thy"));
        assertTrue(extra.contains("definition extra_states_HeaterController :: \"state \\<Rightarrow> bool\" where"), extra);
        assertTrue(extra.contains("\"extraInv s =\n(True)\""), "nothing mid or low: " + extra);
    }

    /**
     * Mid and low: each is assumed only where it is proved - every condition assuming the
     * global invariant has a companion showing them kept, and the base case one showing
     * they hold to begin with. Numbered on their own, so no other condition is renamed.
     */
    @Test
    void everyCycleProvesTheMidInvariantsItMayAssume(@TempDir Path output) throws IOException {
        Path withHigh = Files.createDirectories(output.resolve("high"));
        Path withMid = Files.createDirectories(output.resolve("mid"));
        ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs")).generate(withHigh);
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantSelection(Selection.mid());
        generator.generate(withMid);

        String extra = Files.readString(withMid.resolve("ExtraInvariants.thy"));
        assertTrue(extra.contains("definition extraInv :: \"state \\<Rightarrow> bool\" where"), extra);
        assertTrue(extra.contains("(extra_vars_HeaterController_heating s)"), extra);
        assertFalse(extra.contains("extra_trans_"), "transitions are low: " + extra);
        assertFalse(extra.contains("(extra_states_HeaterController s)\n\\<and>"),
                "the high ones are in inv, not in extraInv: " + extra);

        List<Path> cycles = theories(withMid, "_VC");
        List<Path> obligations = theories(withMid, "_EXTRA");
        assertEquals(cycles.size(), obligations.size(), "one obligation per main condition");
        assertEquals(names(theories(withHigh, "_VC")), names(cycles), "no main condition is renamed");

        int bases = 0;
        for (Path obligation : obligations) {
            String text = Files.readString(obligation);
            assertTrue(text.contains("imports ThermopotTheory LoopInvariants Requirements\n"), text);
            assertTrue(text.contains("shows \"(extraInv st_final)\""), text);
            if (text.contains("base_inv:")) {
                assertTrue(text.contains("extra_inv:\"(extraInv st0)\""), text);
            } else {
                bases++;
            }
        }
        assertEquals(1, bases, "the base case");
    }

    /** A condition gets the invariants about the states its path passes through, found by tag. */
    @Test
    void aConditionAssumesWhatConcernsItsStates(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantSelection(Selection.low());
        generator.generate(output);

        int checked = 0;
        for (Path condition : theories(output, "_VC")) {
            String text = Files.readString(condition);
            if (!text.contains("base_inv:")) {
                continue;
            }
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

    /** Diagnostics reach the caller: none for thermopot, whose static-analysis claims all hold. */
    @Test
    void theDiagnosticsAreReported(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("newThermopot.rcs"));
        generator.setExtraInvariantSelection(Selection.mid());
        generator.generate(output);
        assertEquals(List.of(), generator.getExtraInvariantDiagnostics());
    }

    /** Annotation invariants are registered with the rest, tagged with their scope. */
    @Test
    void annotationInvariantsAreInTheContainer(@TempDir Path output) throws IOException {
        ReflexVcg generator = ReflexVcg.load(PROGRAMS.resolve("annotatedTank.rcs"));
        List<ExtraInvariantGenerator> seen = new ArrayList<>();
        generator.setExtraInvariantGenerator(new ExtraInvariantGenerator(
                generator.getProgram(), generator.getCfg(), generator.getAnnotations()) {
            @Override
            public void analyse() {
                super.analyse();
                seen.add(this);
            }
        });
        generator.generate(output);

        ExtraInvariants all = seen.get(0).getInvariants();
        assertEquals(4, all.find(Tag.kind(ExtraInvariant.Kind.ANNOTATION)).size());
        assertEquals(1, all.find(Tag.kind(ExtraInvariant.Kind.ANNOTATION), Tag.state("Controller", "filling")).size());
        assertEquals(1, all.find(Tag.kind(ExtraInvariant.Kind.LOOP_INVARIANT)).size());
        assertTrue(all.find(Tag.priority(ExtraInvariant.Priority.HIGH)).size() >= 6,
                "annotations, the loop invariant and the process states");
    }

    /**
     * Past a loop, a process the loop never moves still has the prevProcState it had:
     * without that, nothing stated since a process's entry into its state could be carried
     * past a loop.
     */
    @Test
    void aLoopKeepsWhereTheProcessesItDoesNotMoveLastChangedState(@TempDir Path output) throws IOException {
        ReflexVcg.load(PROGRAMS.resolve("loopSum.rcs")).generate(output);
        boolean found = false;
        for (Path condition : theories(output, "_VC")) {
            String text = Files.readString(condition);
            if (text.contains("_frame:")) {
                assertTrue(text.matches("(?s).*\\(\\(prevProcState st\\d+ ''\\w+''\\) = \\(prevProcState st\\d+ ''\\w+''\\)\\).*"),
                        text);
                found = true;
            }
        }
        assertTrue(found, "loopSum has a path past its loop");
    }

    private static List<Path> theories(Path directory, String marker) throws IOException {
        try (var files = Files.list(directory)) {
            return files.filter(f -> f.getFileName().toString().contains(marker)
                            && f.getFileName().toString().endsWith(".thy"))
                    .sorted().toList();
        }
    }

    private static List<String> names(List<Path> files) {
        return files.stream().map(f -> f.getFileName().toString()).toList();
    }
}
