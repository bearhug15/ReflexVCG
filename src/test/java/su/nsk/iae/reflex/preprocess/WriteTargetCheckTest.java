package su.nsk.iae.reflex.preprocess;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A physical variable bound with only a {@code read =} has nowhere for an assignment to
 * go, so writing to it is reported. The write is still generated - this is a warning about
 * the program, not a refusal to check it.
 */
class WriteTargetCheckTest {

    private static final String HEAD = "program P {\n"
            + "  clock 100;\n"
            + "  node N { clock 100; }\n"
            + "  import IO { register inp register out }\n";

    private static List<WriteTargetCheck.Finding> check(String body) {
        NewReflexParser parser = new NewReflexParser(new CommonTokenStream(
                new NewReflexLexer(CharStreams.fromString(HEAD + body + "}"))));
        NewReflexParser.ProgramContext context = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "the program should parse");

        IrProgram program = new AstBuilder().build(context);
        Preprocessor.run(program);
        return WriteTargetCheck.run(program);
    }

    @Test
    void reportsAWriteToAReadOnlyBinding() {
        List<WriteTargetCheck.Finding> findings = check(
                "  bool sensor as (read = inp, bit = 0);\n"
                        + "  process Proc :: node N { state s { sensor = true; } }\n");

        assertEquals(1, findings.size(), findings.toString());
        assertEquals("sensor", findings.get(0).variable());
        assertEquals("inp", findings.get(0).readPort());
        assertTrue(findings.get(0).toString().contains("no 'write ='"), findings.toString());
    }

    /** The binding an output uses: it reads and writes the same address. */
    @Test
    void saysNothingWhenTheBindingHasAWriteDestination() {
        assertEquals(List.of(), check(
                "  bool lamp as (read = out, write = out, bit = 0);\n"
                        + "  process Proc :: node N { state s { lamp = true; } }\n"));
    }

    @Test
    void saysNothingWhenAReadOnlyBindingIsOnlyRead() {
        assertEquals(List.of(), check(
                "  bool sensor as (read = inp, bit = 0);\n"
                        + "  bool lamp as (read = out, write = out, bit = 0);\n"
                        + "  process Proc :: node N { state s { lamp = sensor; } }\n"));
    }

    /**
     * A write can sit anywhere inside an expression, so the check looks at the whole tree
     * rather than only the top of a statement.
     */
    @Test
    void findsAWriteNestedInsideAnExpression() {
        List<WriteTargetCheck.Finding> findings = check(
                "  int8 count as (read = inp, bit = 0);\n"
                        + "  int8 total = 0;\n"
                        + "  process Proc :: node N { state s { total = count++ + 1; } }\n");

        assertEquals(1, findings.size(), findings.toString());
        assertEquals("count", findings.get(0).variable());
    }

    /** An increment is a write too, wherever it appears. */
    @Test
    void findsAnIncrementOfAReadOnlyBinding() {
        List<WriteTargetCheck.Finding> findings = check(
                "  int8 count as (read = inp, bit = 0);\n"
                        + "  process Proc :: node N { state s { count++; } }\n");

        assertEquals(1, findings.size(), findings.toString());
        assertEquals("count", findings.get(0).variable());
    }

    /** Two writes to the same variable on different lines are two findings. */
    @Test
    void reportsEachWriteOnce() {
        List<WriteTargetCheck.Finding> findings = check(
                "  bool sensor as (read = inp, bit = 0);\n"
                        + "  process Proc :: node N { state s {\n"
                        + "    sensor = true;\n"
                        + "    sensor = false;\n"
                        + "  } }\n");

        assertEquals(2, findings.size(), findings.toString());
        assertEquals(findings.get(0).line() + 1, findings.get(1).line());
    }

    /** An ordinary program variable has no binding at all, so nothing is reported. */
    @Test
    void saysNothingAboutOrdinaryVariables() {
        assertEquals(List.of(), check(
                "  int8 v = 0;\n"
                        + "  process Proc :: node N { state s { v = 1; } }\n"));
    }
}
