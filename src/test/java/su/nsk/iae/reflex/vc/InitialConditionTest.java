package su.nsk.iae.reflex.vc;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The base case of the induction: the condition for the program's starting state.
 */
class InitialConditionTest {

    private static IrProgram parse(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse");
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    private static VerificationCondition initialFor(String source) {
        return InitialCondition.build(parse(source));
    }

    private static final String MINIMAL = "program P {\n"
            + "  clock 100;\n"
            + "  node N { clock 100; }\n"
            + "  process First :: node N { state begin { ; } state other { ; } }\n"
            + "  process Second :: node N { state only { ; } }\n"
            + "}";

    /**
     * The distinguishing feature: every other condition assumes the invariant and shows it
     * is preserved. This one has to establish it, so it assumes nothing.
     */
    @Test
    void makesNoInvariantAssumption() {
        VerificationCondition condition = initialFor(MINIMAL);

        assertFalse(condition.getStatements().stream()
                        .anyMatch(s -> s instanceof VcStatement.Invariant),
                "the base case cannot assume what it is meant to prove");
        assertInstanceOf(VcStatement.EmptyState.class, condition.getStatements().get(0));
    }

    /** Only the first process is started; the rest are stopped in emptyState already. */
    @Test
    void startsOnlyTheFirstProcess() {
        VerificationCondition condition = initialFor(MINIMAL);

        List<VcStatement.SetProcessState> starts = condition.getStatements().stream()
                .filter(VcStatement.SetProcessState.class::isInstance)
                .map(VcStatement.SetProcessState.class::cast)
                .toList();

        assertEquals(1, starts.size());
        assertEquals("First", starts.get(0).process());
        assertEquals("begin", starts.get(0).pstate(), "its first declared state");
    }

    @Test
    void endsWithAnEnvironmentStepAndConcludesAboutTheFinalState() {
        VerificationCondition condition = initialFor(MINIMAL);
        List<VcStatement> statements = condition.getStatements();

        assertInstanceOf(VcStatement.ToEnv.class, statements.get(statements.size() - 2));
        VcStatement.Final last = assertInstanceOf(VcStatement.Final.class,
                statements.get(statements.size() - 1));
        assertEquals(condition.getFinalState(), last.target());
    }

    @Test
    void appliesScalarInitialisers() {
        VerificationCondition condition = initialFor("program P {\n"
                + "  clock 100;\n"
                + "  int32 counter = 5;\n"
                + "  bool flag;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        List<VcStatement.Assign> writes = condition.getStatements().stream()
                .filter(VcStatement.Assign.class::isInstance)
                .map(VcStatement.Assign.class::cast)
                .toList();

        assertEquals(1, writes.size(), "only the variable with an initialiser is written");
        assertEquals("#counter", writes.get(0).variable().getName());
    }

    /** Aggregate initialisers are partial: only the members actually given are written. */
    @Test
    void appliesDesignatedStructInitialisersOnly() {
        VerificationCondition condition = initialFor("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point p = {.y = 7};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        List<VcStatement.Assign> writes = condition.getStatements().stream()
                .filter(VcStatement.Assign.class::isInstance)
                .map(VcStatement.Assign.class::cast)
                .toList();

        assertEquals(1, writes.size(), "x has no element, so it keeps its default");
        assertEquals("y", assertInstanceOf(su.nsk.iae.reflex.ir.IrExpr.FieldAccess.class,
                writes.get(0).variable().getAccesses().get(0)).getField());
    }

    @Test
    void fillsPositionalStructAndArrayElementsInOrder() {
        VerificationCondition condition = initialFor("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point p = {1, 2};\n"
                + "  int32 v[3] = {10, 20};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        List<VcStatement.Assign> writes = condition.getStatements().stream()
                .filter(VcStatement.Assign.class::isInstance)
                .map(VcStatement.Assign.class::cast)
                .toList();

        // Two struct fields, then two of the three array elements.
        assertEquals(4, writes.size());
        assertEquals("x", assertInstanceOf(su.nsk.iae.reflex.ir.IrExpr.FieldAccess.class,
                writes.get(0).variable().getAccesses().get(0)).getField());
        assertEquals("y", assertInstanceOf(su.nsk.iae.reflex.ir.IrExpr.FieldAccess.class,
                writes.get(1).variable().getAccesses().get(0)).getField());
        assertInstanceOf(su.nsk.iae.reflex.ir.IrExpr.IndexAccess.class,
                writes.get(2).variable().getAccesses().get(0));
    }

    @Test
    void rendersAsALemmaWithoutABaseInvariant() {
        String lemma = new IsabelleRenderer().renderLemma(initialFor(MINIMAL));

        assertTrue(lemma.startsWith("lemma\nassumes st0:\"st0=emptyState\""), lemma);
        assertFalse(lemma.contains("base_inv"), lemma);
        assertTrue(lemma.endsWith("shows \"inv(st_final)\""), lemma);
    }
}
