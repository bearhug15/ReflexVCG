package su.nsk.iae.reflex.analysis;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgBuilder;
import su.nsk.iae.reflex.cfg.PathEnumerator;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reports how many conditions each program produces unpruned and under each reading of
 * how the two rule sets combine, against what the old pipeline emitted with static
 * analysis on.
 *
 * <p>This exists to make the choice between those readings a matter of numbers rather
 * than argument; it prints a table and asserts only what is safe to assert.
 */
class StaticAnalysisMeasurementTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    /** What the old pipeline emitted with -a true, from src/test/baseline/vc-counts.txt. */
    private static final Map<String, Integer> OLD_WITH_ANALYSIS = new LinkedHashMap<>(Map.of(
            "ifTest1", 3,
            "ifTest2", 5,
            "ifTest3", 4,
            "switchTest1", 4,
            "switchTest2", 5,
            "newBarrier", 33,
            "newEscalator", 27,
            "newSmartLighting", 127,
            "newThermopot", 41,
            "newTurnstile", 134));

    private static IrProgram load(String name) throws IOException {
        NewReflexLexer lexer = new NewReflexLexer(CharStreams.fromPath(PROGRAMS.resolve(name + ".rx")));
        BufferedTokenStream tokens = new CommonTokenStream(lexer);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), name + " should parse");
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    private static int count(IrProgram program, StaticAnalysis analysis) {
        AttributePreparation preparation = new AttributePreparation(program);
        preparation.run();
        Cfg cfg = new CfgBuilder(program, preparation).build();
        return new PathEnumerator(cfg, analysis).enumerate().size();
    }

    @Test
    void reportsPruningAgainstTheOldAnalysisBaseline() throws IOException {
        StringBuilder report = new StringBuilder(String.format(
                "%n%-18s %8s %8s %8s %8s%n", "program", "none", "either", "both", "old(-a)"));

        for (Map.Entry<String, Integer> entry : OLD_WITH_ANALYSIS.entrySet()) {
            String name = entry.getKey();
            IrProgram program = load(name);

            int none = count(program, null);
            int either = count(program, new StaticAnalysis(program, StaticAnalysis.Combination.EITHER));
            int both = count(program, new StaticAnalysis(program, StaticAnalysis.Combination.BOTH));

            report.append(String.format("%-18s %8d %8d %8d %8d%n",
                    name, none, either, both, entry.getValue() - 1));
        }
        System.out.println(report);

        // Pruning must never invent conditions: whatever the reading, it can only remove.
        for (String name : OLD_WITH_ANALYSIS.keySet()) {
            IrProgram program = load(name);
            int none = count(program, null);
            int either = count(program, new StaticAnalysis(program, StaticAnalysis.Combination.EITHER));
            int both = count(program, new StaticAnalysis(program, StaticAnalysis.Combination.BOTH));

            assertTrue(either <= none, name + ": pruning produced more conditions than none");
            assertTrue(both <= none, name + ": pruning produced more conditions than none");
            assertTrue(either <= both,
                    name + ": discarding when either rule set objects should not keep more "
                            + "than discarding only when both do");
        }
    }

    /**
     * On the small programs the analysis reproduces the old pipeline's pruned output
     * exactly. ifTest2 matches once its two division domain conditions - which the old
     * generator emitted and this one deliberately does not - are subtracted.
     */
    @Test
    void reproducesTheOldPrunedCountsOnTheSmallPrograms() throws IOException {
        Map<String, Integer> expected = new LinkedHashMap<>(Map.of(
                "ifTest1", 2,
                "ifTest2", 2,
                "ifTest3", 3,
                "switchTest1", 3,
                "switchTest2", 4));

        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            IrProgram program = load(entry.getKey());
            int pruned = count(program, new StaticAnalysis(program));
            assertEquals(entry.getValue().intValue(), pruned, entry.getKey());
        }
    }

    /** The default reading is the one that reproduces those counts. */
    @Test
    void defaultsToDiscardingWhenEitherRuleSetObjects() throws IOException {
        IrProgram program = load("ifTest1");
        assertEquals(count(program, new StaticAnalysis(program, StaticAnalysis.Combination.EITHER)),
                count(program, new StaticAnalysis(program)));
    }
}
