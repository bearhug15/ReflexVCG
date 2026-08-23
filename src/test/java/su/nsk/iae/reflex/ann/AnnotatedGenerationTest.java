package su.nsk.iae.reflex.ann;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generation of a program carrying every kind of Reflex-AL annotation.
 *
 * <p>Covers the four ways an annotation reaches the output: an {@code assume} both proves
 * itself and is available to the rest of the path, an {@code assert} only proves itself, an
 * {@code invariant} on the program, a process or a state joins the global invariant, and an
 * {@code invariant} on a {@code for} splits the loop into its entry and preservation
 * conditions.
 *
 * <p>Conditions are looked up by the note saying where they came from rather than by file
 * number, which shifts whenever the program does.
 */
class AnnotatedGenerationTest {

    private static final Path SOURCE = Path.of("src/test/resources/programs-new/annotatedTank.rx");
    private static final Path OUTPUT = Path.of("target/annotated-generation");

    /** File name to contents, for everything generation wrote. */
    private static Map<String, String> files;

    @BeforeAll
    static void generate() throws IOException {
        Files.createDirectories(OUTPUT);
        try (Stream<Path> existing = Files.list(OUTPUT)) {
            for (Path file : existing.toList()) {
                Files.deleteIfExists(file);
            }
        }

        ReflexVcg.load(SOURCE).generate(OUTPUT);

        files = new LinkedHashMap<>();
        try (Stream<Path> written = Files.list(OUTPUT)) {
            for (Path file : written.sorted().toList()) {
                files.put(file.getFileName().toString(), read(file));
            }
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Every condition of one kind, by the prefix its file name carries. */
    private static List<String> ofKind(String prefix) {
        List<String> matching = new ArrayList<>();
        files.forEach((name, content) -> {
            if (name.startsWith("AnnotatedTank_" + prefix)) {
                matching.add(content);
            }
        });
        return matching;
    }

    /** The one condition whose note says it came from {@code note}. */
    private static String from(String note) {
        List<String> matching = files.values().stream()
                .filter(content -> content.contains("(* " + note))
                .toList();
        assertEquals(1, matching.size(),
                "expected exactly one condition noted '" + note + "', got " + matching.size());
        return matching.get(0);
    }

    /**
     * The main conditions covering the cycle that begins in {@code state}. Matched without
     * naming the state variable, which shifts as the head of a cycle grows.
     */
    private static List<String> mainConditionsFrom(String state) {
        return ofKind("VC").stream()
                .filter(content -> content.contains("''Controller''=''" + state + "''"))
                .toList();
    }

    /**
     * One obligation per annotation, not one per path reaching it. An obligation depends
     * only on the path up to where it is stated, so the paths that continue past it would
     * otherwise each restate it.
     */
    @Test
    void eachAnnotationProducesExactlyOneObligation() {
        assertEquals(2, ofKind("ASSUME").size(), "two assumes are written in the source");
        assertEquals(2, ofKind("ASSERT").size(), "two asserts are written in the source");
        assertEquals(1, ofKind("LOOPENTRY").size(), "one loop, so one entry condition");
        assertEquals(1, ofKind("LOOPSTEP").size(), "one loop, so one preservation condition");
        assertFalse(ofKind("VC").isEmpty(), "the program's own conditions should still be there");
    }

    /** An obligation concludes the annotation's own formula, not the invariant. */
    @Test
    void anObligationConcludesTheFormulaItWasWrittenFor() {
        String assertion = from("assert at line 51");
        assertTrue(assertion.contains("shows \"((theInt (getVarVal st7 ''#total'' [])) \\<ge> 3)\""),
                assertion);
        assertFalse(assertion.contains("shows \"inv("), assertion);
    }

    /**
     * The difference between the two kinds: what follows an assume may rely on it, what
     * follows an assert may not. Both are stated where they are written.
     */
    @Test
    void anAssumeIsCarriedIntoTheMainConditionAndAnAssertIsNot() {
        List<String> filling = mainConditionsFrom("filling");
        assertFalse(filling.isEmpty(), "the filling state should produce conditions");

        String withAssume = filling.stream()
                .filter(content -> content.contains("_assume_0:"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no main condition carries the assume"));
        assertTrue(withAssume.contains(
                        "st7_assume_0:\"((theInt (getVarVal st7 ''#total'' [])) \\<ge> 0)\""),
                withAssume);
        assertTrue(withAssume.contains("shows \"inv(st_final)\""), withAssume);

        for (String condition : ofKind("VC")) {
            assertFalse(condition.contains("_assert_"),
                    "an assert must not weaken a main condition:\n" + condition);
        }
    }

    /** Program, process and state invariants land in one definition, and only there. */
    @Test
    void invariantsAreCollectedIntoTheirOwnTheory() {
        String requirements = files.get("Requirements.thy");
        assertTrue(requirements.contains("definition inv where"), requirements);

        // Program scale: stated at every reachable state, with no further condition.
        assertTrue(requirements.contains(
                "(\\<not> ((theBool (getVarVal sa1 ''valves_0'' [])) \\<and> "
                        + "(theBool (getVarVal sa1 ''valves_1'' []))))"), requirements);
        // Process scale: only while the process is neither stopped nor in error.
        assertTrue(requirements.contains("(getPstate sa3 ''Controller'') \\<noteq> ''stop''"),
                requirements);
        // State scale: only while the process is in that state.
        assertTrue(requirements.contains("((getPstate sa5 ''Controller'') = ''filling'')"),
                requirements);
        // A temporal operator, here `once`, becomes an existential over earlier states.
        assertTrue(requirements.contains("\\<exists> sa7."), requirements);

        // A loop invariant belongs to that loop's conditions, not to the global one.
        assertFalse(requirements.contains("''#i''"),
                "a for invariant is not global:\n" + requirements);

        // Every condition is stated against it.
        for (String condition : ofKind("VC")) {
            assertTrue(condition.contains("imports AnnotatedTankTheory Requirements"), condition);
        }
    }

    /**
     * A loop is cut into three: the invariant holds on entry, one iteration preserves it,
     * and the path past the loop knows the invariant and that the condition has failed.
     */
    @Test
    void aLoopIsCutIntoEntryPreservationAndExit() {
        String entry = from("loop invariant on entry, line 44");
        assertTrue(entry.contains("shows \"((theInt (getVarVal st6 ''#total'' [])) "
                + "\\<le> (theInt (getVarVal st6 ''#i'' [])))\""), entry);

        String step = from("loop invariant preserved, line 44");
        // Assumed up to where the body starts, shown up to where the iteration ends. The
        // body is numbered from its own st0, not the enclosing path's.
        assertTrue(step.contains("loop_invariant:\"(\\<forall> sa"), step);
        assertTrue(step.contains("(substate sa3 st0))"), step);
        assertTrue(step.contains("shows \"(\\<forall> sa4. (((toEnvP sa4) "
                + "\\<and> (substate sa4 (toEnv st2)))"), step);
        // The condition is assumed: an iteration only runs while it holds.
        assertTrue(step.contains("st0_condition_0:\"((theInt (getVarVal st0 ''#i'' [])) < "), step);
        // Both the body and the loop's update belong to the iteration.
        assertTrue(step.contains("st1:\"st1=(setVarVal st0 ''#total''"), step);
        assertTrue(step.contains("st2:\"st2=(setVarVal st1 ''#i''"), step);

        // Past the loop nothing is claimed of the state beyond the invariant and the
        // negated condition - how many iterations ran is not known.
        String past = mainConditionsFrom("filling").stream()
                .filter(content -> content.contains("st7_invariant:"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no condition passes the loop"));
        assertTrue(past.contains("st7:\"toEnvP st7 \\<and> substate st6 st7\""), past);
        assertTrue(past.contains("st7_condition_0:\"(\\<not> ((theInt (getVarVal st7 ''#i'' []))"),
                past);
    }

    /** A {@code define} is expanded where it is used, in the scope that declared it. */
    @Test
    void aDefinitionIsExpandedAtItsUse() {
        // `draining` is `pumpOut && !pumpIn`, over the addresses those two map to.
        String assertion = from("assert at line 60");
        assertTrue(assertion.contains("shows \"((theBool (getVarVal st4 ''valves_1'' [])) "
                + "\\<and> (\\<not> (theBool (getVarVal st4 ''valves_0'' []))))\""), assertion);
    }

    /**
     * Name mangling and typing reach annotations too: an unqualified name resolves in the
     * scope the annotation sits in, and a physical variable reads through its address.
     */
    @Test
    void annotationNamesArePreprocessedWithTheProgram() {
        String assumption = from("assume at line 68");
        assertTrue(assumption.contains("(theBool (getVarVal st2 ''sensors_0'' []))"), assumption);

        // A program-scope variable mangles to a bare `#name`, having no enclosing scope.
        assertTrue(from("assume at line 49").contains("''#total''"), files.toString());
    }

    /**
     * Every condition begins by letting the inputs be anything: a physical variable bound
     * with no {@code write =} takes its value from a free variable, which a lemma leaves
     * universally quantified. Without this a condition would only hold for whatever value
     * the input happened to have.
     */
    @Test
    void everyConditionLetsTheInputsBeAnything() {
        List<String> cycles = new ArrayList<>(ofKind("VC"));
        cycles.addAll(ofKind("ASSUME"));
        cycles.addAll(ofKind("ASSERT"));
        cycles.addAll(ofKind("LOOPENTRY"));
        assertFalse(cycles.isEmpty());

        for (String condition : cycles) {
            assertTrue(condition.contains(
                    "st1:\"st1=(setVarVal st0 ''sensors_0'' [] (ValBool sensors_0))\""), condition);
            assertTrue(condition.contains(
                    "st2:\"st2=(setVarVal st1 ''sensors_1'' [] (ValBool sensors_1))\""), condition);
        }

        // The outputs are driven by the program, so they are not free.
        for (String condition : cycles) {
            assertFalse(condition.contains("(ValBool valves_0)"), condition);
        }
    }

    /**
     * A loop body is not a cycle: the hardware is read once per cycle, so an iteration
     * does not get to resample it.
     */
    @Test
    void aLoopIterationDoesNotResampleTheInputs() {
        String step = from("loop invariant preserved, line 44");
        assertFalse(step.contains("sensors_0"), step);
    }
}
