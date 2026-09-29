package su.nsk.iae.reflex.inv.simulation;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.inv.ExtraInvariant;
import su.nsk.iae.reflex.inv.ExtraInvariants;
import su.nsk.iae.reflex.inv.Selection;
import su.nsk.iae.reflex.inv.StructuralInvariants;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.term.Terms;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every invariant the analysis derives, checked on concrete runs.
 *
 * <p>Isabelle is the real judge, but it is slow and has to be run by hand. This runs every
 * test program many times with random inputs, over the same graph the conditions come
 * from, and evaluates every derived invariant's formula - as rendered, {@code let},
 * {@code prevProcState} and all - at every cycle boundary. A wrong invariant is a claim a
 * run can refute, and would be caught here long before a proof fails on it.
 */
class SimulationTest {

    private static final int RUNS = 20;
    private static final int CYCLES = 120;

    private final TermRenderer terms = new TermRenderer();

    @Test
    void everyDerivedInvariantHoldsOnEveryRun() throws IOException {
        Map<String, Integer> checked = new LinkedHashMap<>();
        List<String> failures = new ArrayList<>();
        for (Path program : programs()) {
            ReflexVcg generator = ReflexVcg.load(program);
            ExtraInvariants found = new StructuralInvariants(generator.getProgram(), generator.getCfg(),
                    new IsabelleRenderer()::renderExpression).generate(Selection.low());
            List<ExtraInvariant> invariants = found.all();
            int[] count = {0};
            for (int run = 0; run < RUNS; run++) {
                Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(run));
                TermEvaluator evaluator = new TermEvaluator(machine.clock());
                int[] cycle = {0};
                int seed = run;
                java.util.function.Consumer<History.Node> check = state -> {
                    for (ExtraInvariant invariant : invariants) {
                        count[0]++;
                        if (!evaluator.holds(invariant.formula(), "s", state)
                                && failures.size() < 20) {
                            failures.add(program.getFileName() + " run " + seed + " cycle " + cycle[0] + ": "
                                    + invariant.name() + " = " + terms.render(invariant.formula()));
                        }
                    }
                };
                machine.start(check);
                for (cycle[0] = 1; cycle[0] <= CYCLES; cycle[0]++) {
                    machine.cycle(check);
                }
            }
            checked.put(program.getFileName().toString(), count[0]);
        }
        checked.forEach((program, count) ->
                System.out.printf("simulation %-22s %9d invariant checks%n", program, count));
        assertEquals(List.of(), failures);
        checked.forEach((program, count) -> assertTrue(count > 0, program + " checked nothing"));
    }

    /** The check can fail: a claim that is false is caught on the first runs. */
    @Test
    void aFalseInvariantIsCaught() throws IOException {
        ReflexVcg generator = ReflexVcg.load(Path.of("src/test/resources/programs-extra/lamp.rcs"));
        // The switch is never lit - false as soon as the button is pressed.
        Term neverLit = new Term.Infix("\\<noteq>", Terms.pstateOf(ExtraInvariant.STATE, "Switch"),
                new Term.Quoted("lit"));
        Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(0));
        TermEvaluator evaluator = new TermEvaluator(machine.clock());
        boolean[] refuted = {false};
        machine.start(state -> refuted[0] |= !evaluator.holds(neverLit, "s", state));
        for (int cycle = 0; cycle < 50 && !refuted[0]; cycle++) {
            machine.cycle(state -> refuted[0] |= !evaluator.holds(neverLit, "s", state));
        }
        assertTrue(refuted[0]);
    }

    /** The interpreter agrees with the model on the basics the invariants rest on. */
    @Test
    void theRunIsTheModelsRun() throws IOException {
        ReflexVcg generator = ReflexVcg.load(Path.of("src/test/resources/programs-extra/lamp.rcs"));
        Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(1));
        List<History.Node> boundaries = new ArrayList<>();
        machine.start(boundaries::add);
        for (int cycle = 0; cycle < 60; cycle++) {
            machine.cycle(boundaries::add);
        }
        History.Node last = boundaries.get(boundaries.size() - 1);
        assertTrue(last.toEnvP());
        assertEquals(boundaries.get(boundaries.size() - 2), last.predEnv());
        // Time in a state grows one tick per cycle and never reaches past the timeout.
        assertTrue(boundaries.stream().allMatch(b -> !b.getPstate("Switch").equals("lit")
                || b.ltime("Switch", machine.clock()).intValue() < 2100));
        assertTrue(boundaries.stream().anyMatch(b -> b.getPstate("Switch").equals("lit")),
                "the inputs are varied enough to reach lit");
        assertFalse(boundaries.stream().anyMatch(b -> b.getPstate("Switch").equals("never")));
    }

    private static List<Path> programs() throws IOException {
        List<Path> programs = new ArrayList<>();
        for (String directory : List.of("src/test/resources/programs-new", "src/test/resources/programs-extra")) {
            try (Stream<Path> files = Files.list(Path.of(directory))) {
                files.filter(f -> f.toString().endsWith(".rcs")).sorted().forEach(programs::add);
            }
        }
        return programs;
    }
}
