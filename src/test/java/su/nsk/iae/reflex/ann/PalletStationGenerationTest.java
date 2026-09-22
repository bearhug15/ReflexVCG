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
 * Generation of {@code palletStation.rcs}, which carries every annotation kind and every
 * operator across four states: one without a loop, one with a loop, one with two loops in
 * a row, and one with a loop inside a loop.
 *
 * <p>What this pins that {@link AnnotatedGenerationTest} does not: the variant conditions,
 * the operators interacting with path enumeration, and what a condition inside a loop body
 * gets to assume - in particular that a nested loop's conditions know the enclosing loop's
 * invariant.
 */
class PalletStationGenerationTest {

    private static final Path SOURCE = Path.of("src/test/resources/programs-new/palletStation.rcs");
    private static final Path OUTPUT = Path.of("target/pallet-station-generation");

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

    private static List<String> ofKind(String prefix) {
        List<String> matching = new ArrayList<>();
        files.forEach((name, content) -> {
            if (name.startsWith("PalletStation_" + prefix)) {
                matching.add(content);
            }
        });
        return matching;
    }

    /** The conditions whose note mentions {@code text}. */
    private static List<String> noted(String text) {
        return files.values().stream()
                .filter(content -> content.contains("(* ") && content.contains(text))
                .toList();
    }

    private static String theOne(List<String> conditions, String what) {
        assertEquals(1, conditions.size(), "expected exactly one " + what + ", got " + conditions.size());
        return conditions.get(0);
    }

    // ------------------------------------------------------------------ shape

    /**
     * Five loops: one in scanning, two in sorting, two nested in packing. Each gets an entry
     * condition and one bound condition; a step and a decrease per path through its body,
     * and the sorting loops branch inside.
     */
    @Test
    void everyLoopGetsItsFourKindsOfCondition() {
        assertEquals(5, ofKind("LOOPENTRY").size());
        assertEquals(5, ofKind("LOOPBOUND").size());
        assertEquals(7, ofKind("LOOPSTEP").size());
        assertEquals(7, ofKind("LOOPDECREASE").size());

        String loops = files.get("LoopInvariants.thy");
        for (int i = 0; i < 5; i++) {
            assertTrue(loops.contains("definition loopInv" + i + " ::"), loops);
        }
    }

    // ------------------------------------------------------------------ variant

    /** The bound is stated once per loop, from the invariant and the condition alone. */
    @Test
    void theVariantBoundReadsNothingOfTheBody() {
        String bound = theOne(noted("stays at or above zero, loopInv1"), "bound for loopInv1");
        assertTrue(bound.contains("st0_boundary:"), bound);
        assertTrue(bound.contains("loop_invariant:"), bound);
        assertTrue(bound.contains("st0_condition_0:"), bound);
        // The branch inside the body is not a hypothesis of the bound.
        assertFalse(bound.contains("st0_condition_1:"), bound);
        assertFalse(bound.contains("st1:"), bound);
        assertTrue(bound.contains("shows \"(((theInt (getVarVal st0 ''#BAY_COUNT'' [])) "
                + "- (theInt (getVarVal st0 ''#i'' []))) \\<ge> 0)\""), bound);
    }

    /** The decrease compares the measure where the iteration started with where it ended. */
    @Test
    void theVariantDecreaseSpansTheIteration() {
        List<String> decreases = noted("loop variant decreases, loopInv1");
        assertEquals(2, decreases.size(), "one per path through the body");
        for (String decrease : decreases) {
            // Each path carries its own branch guard, and ends in a state of its own.
            assertTrue(decrease.contains("st0_condition_1:"), decrease);
            assertTrue(decrease.contains("shows \"(((theInt (getVarVal st0 ''#BAY_COUNT'' [])) "
                    + "- (theInt (getVarVal st0 ''#i'' []))) > ((theInt (getVarVal (toEnv st"),
                    decrease);
        }
    }

    // ------------------------------------------------------------------ nesting

    /**
     * The inner loop's entry condition is derived inside the outer loop's body, and so is
     * stated knowing what that body knew: the outer invariant and the outer condition.
     */
    @Test
    void aNestedLoopKnowsTheEnclosingInvariant() {
        String entry = theOne(noted("loop invariant on entry, loopInv4"), "inner loop entry");
        assertTrue(entry.contains("loop_invariant:"), entry);
        assertTrue(entry.contains("(loopInv3 t0 sa"), entry);
        assertTrue(entry.contains("st0_condition_0:\"((theInt (getVarVal st0 ''#r'' [])) < "), entry);
        assertTrue(entry.contains("shows \"(loopInv4 st1 st1)\""), entry);

        // The outer step sees the inner loop as opaque: only its invariant and its exit.
        String outerStep = theOne(noted("loop invariant preserved, loopInv3"), "outer loop step");
        assertTrue(outerStep.contains("st2:\"toEnvP st2 \\<and> substate st1 st2\""), outerStep);
        assertTrue(outerStep.contains("(loopInv4 st1 sa"), outerStep);
        assertTrue(outerStep.contains("st2_condition_1:\"(\\<not> ((theInt (getVarVal st2 ''#c'' []))"),
                outerStep);
    }

    /** What an iteration may assume of the state it starts from: that it is a boundary. */
    @Test
    void anIterationStartsAtABoundaryOfItsRun() {
        for (String step : ofKind("LOOPSTEP")) {
            assertTrue(step.contains(
                    "st0_boundary:\"((substate t0 st0) \\<and> ((st0 = t0) \\<or> (toEnvP st0)))\""),
                    step);
        }
    }

    // ------------------------------------------------------------------ operators

    /** Every operator reaches the output, in the shape the specification gives it. */
    @Test
    void everyOperatorReachesTheOutput() {
        String requirements = files.get("Requirements.thy");
        // time
        assertTrue(requirements.contains("(ltime sa14 ''Palletiser'') < 30000"), requirements);
        // stable(psi, t, phi): cooldown implies
        assertTrue(requirements.contains("(((toEnvNum sa3 sa4) * 100) < 2000))) \\<longrightarrow>"),
                requirements);
        // timer inside during, measured from the trigger
        assertTrue(requirements.contains("* 100) \\<ge> 1000)"), requirements);
        // within(t, phi) inside on: the condition, or time remaining
        assertTrue(requirements.contains("\\<or> (((toEnvNum sa32 sa34) * 100) < 500)"), requirements);
        // stable(t, phi) inside on
        assertTrue(requirements.contains("(((toEnvNum sa36 sa37) * 100) < 300) \\<longrightarrow>"),
                requirements);

        String asserts = String.join("\n", ofKind("ASSERT"));
        // previously and next: the adjacent boundary either side
        assertTrue(asserts.contains("(sa1 \\<noteq> st5)) \\<and> (toEnvP sa1))"), asserts);
        assertTrue(asserts.contains("[assert: next(in(Palletiser, scanning))]"), asserts);
        // scope(prev) and scope(past): a chosen state, read as a term
        assertTrue(asserts.contains("(getVarVal (SOME sa"), asserts);
        assertTrue(asserts.contains("''Palletiser'') = ''idle'')"), asserts);
        // a define with a parameter, expanded where it is used
        assertTrue(asserts.contains("[assert: remaining(placed) >= 0]"), asserts);
        assertTrue(asserts.contains("(theInt (getVarVal st7 ''#FULL'' [])) - (theInt (getVarVal st7 ''#placed'' []))) \\<ge> 0"),
                asserts);

        String assumes = String.join("\n", ofKind("ASSUME"));
        assertTrue(assumes.contains("(\\<forall> k."), assumes);
        assertTrue(asserts.contains("(\\<exists> k."), asserts);
    }
}
