package su.nsk.iae.reflex.inv.simulation;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.analysis.PathState;
import su.nsk.iae.reflex.analysis.StaticAnalysis;
import su.nsk.iae.reflex.cfg.CfgNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The static analysis never discards a cycle a run performs (static-analysis.md, section 5).
 *
 * <p>Every test program is run many times with random inputs, over the graph the conditions
 * come from. Each cycle passes a sequence of nodes - a path of the graph, with a loop as its
 * cut - and that path is replayed through {@link StaticAnalysis#step}, the very check the
 * enumerator applies. A cycle the analysis rejects is a feasible path whose condition would
 * never be generated: a lost proof obligation. Every rule of section 4 is exercised this way;
 * {@code analysisStress.rcs} is written to reach all of them.
 */
class StaticAnalysisSoundnessTest {

    private static final int RUNS = 30;
    private static final int CYCLES = 150;

    @Test
    void noCycleARunPerformsIsDiscarded() throws IOException {
        List<String> failures = new ArrayList<>();
        Map<String, Integer> distinct = new LinkedHashMap<>();
        for (Path program : programs()) {
            ReflexVcg generator = ReflexVcg.load(program);
            StaticAnalysis analysis = new StaticAnalysis(generator.getProgram());
            Set<List<CfgNode>> paths = new LinkedHashSet<>();
            boolean failed = false;
            for (int run = 0; run < RUNS && !failed; run++) {
                Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(run));
                List<CfgNode> cycle = new ArrayList<>();
                machine.onStep(cycle::add);
                machine.start(state -> { });
                for (int number = 1; number <= CYCLES; number++) {
                    cycle.clear();
                    machine.cycle(state -> { });
                    String rejected = replay(analysis, cycle);
                    if (rejected != null) {
                        failures.add(program.getFileName() + " run " + run + " cycle " + number + ": " + rejected);
                        failed = true;
                        break;
                    }
                    paths.add(List.copyOf(cycle));
                }
            }
            distinct.put(program.getFileName().toString(), paths.size());
        }
        distinct.forEach((program, count) ->
                System.out.printf("soundness %-24s %5d distinct cycles replayed%n", program, count));
        assertEquals(List.of(), failures);
        assertTrue(distinct.get("analysisStress.rcs") > 50, "the stress program should vary: " + distinct);
    }

    /** The node at which the analysis rejects the path, and the path up to it; null if none. */
    static String replay(StaticAnalysis analysis, List<CfgNode> cycle) {
        PathState path = PathState.INITIAL;
        List<String> sofar = new ArrayList<>();
        for (CfgNode node : cycle) {
            sofar.add(node.describe());
            path = analysis.step(path, node);
            if (path == null) {
                return "rejected at " + node.describe() + " after " + sofar;
            }
        }
        return null;
    }

    static List<Path> programs() throws IOException {
        List<Path> programs = new ArrayList<>();
        for (String directory : List.of("src/test/resources/programs-new", "src/test/resources/programs-extra")) {
            try (Stream<Path> files = Files.list(Path.of(directory))) {
                files.filter(f -> f.toString().endsWith(".rcs")).sorted().forEach(programs::add);
            }
        }
        return programs;
    }
}
