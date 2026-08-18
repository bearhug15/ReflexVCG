package su.nsk.iae.reflex.vc;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.preprocess.Preprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsabelleRendererTest {

    private final IsabelleRenderer renderer = new IsabelleRenderer();

    /** Renders the value expression of the first assignment in the single state. */
    private String renderFirstAssignedValue(String declarations, String statement) {
        IrProgram program = build(declarations, statement);
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());
        return renderer.renderExpression(assign.getValue(), "st0");
    }

    private String renderFirstCondition(String declarations, String statement) {
        IrProgram program = build(declarations, statement);
        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0));
        return renderer.renderExpression(ifStmt.getCondition(), "st0");
    }

    private static IrProgram build(String declarations, String statements) {
        String source = "program P {\n"
                + "  clock 100;\n"
                + declarations
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n" + statements + "\n    }\n"
                + "  }\n"
                + "  process Other :: node N { state only { ; } }\n"
                + "}";
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    // ------------------------------------------------------------------ value sorts

    @Test
    void mapsReflexTypesOntoTheirHolSorts() {
        assertEquals(IsabelleRenderer.Sort.BOOL, IsabelleRenderer.sortOf(IrType.BOOL));
        assertEquals(IsabelleRenderer.Sort.INT, IsabelleRenderer.sortOf(IrType.INT32));
        assertEquals(IsabelleRenderer.Sort.INT,
                IsabelleRenderer.sortOf(IrType.of(IrType.BuiltinKind.INT8)));
        assertEquals(IsabelleRenderer.Sort.NAT,
                IsabelleRenderer.sortOf(IrType.of(IrType.BuiltinKind.UINT16)));
        assertEquals(IsabelleRenderer.Sort.NAT, IsabelleRenderer.sortOf(IrType.TIME));
        assertEquals(IsabelleRenderer.Sort.REAL,
                IsabelleRenderer.sortOf(IrType.of(IrType.BuiltinKind.DOUBLE)));
    }

    @Test
    void picksTheMatchingProjectionAndConstructor() {
        assertEquals("theBool", IsabelleRenderer.projection(IrType.BOOL));
        assertEquals("ValBool", IsabelleRenderer.constructor(IrType.BOOL));
        assertEquals("theInt", IsabelleRenderer.projection(IrType.INT32));
        assertEquals("ValInt", IsabelleRenderer.constructor(IrType.INT32));
        assertEquals("theNat", IsabelleRenderer.projection(IrType.of(IrType.BuiltinKind.UINT8)));
        assertEquals("theReal", IsabelleRenderer.projection(IrType.of(IrType.BuiltinKind.FLOAT)));
    }

    // ------------------------------------------------------------------ expressions

    @Test
    void readsAScalarThroughGetVarValAndProjectsIt() {
        String rendered = renderFirstAssignedValue("  int32 a;\n  int32 b;\n", "      a = b;");
        assertEquals("(theInt (getVarVal st0 ''#b'' []))", rendered);
    }

    @Test
    void readsABooleanWithTheBool() {
        String rendered = renderFirstAssignedValue("  bool a;\n  bool b;\n", "      a = b;");
        assertEquals("(theBool (getVarVal st0 ''#b'' []))", rendered);
    }

    @Test
    void rendersArithmeticInTheProjectedSort() {
        String rendered = renderFirstAssignedValue("  int32 a;\n  int32 b;\n", "      a = b + 1;");
        assertEquals("((theInt (getVarVal st0 ''#b'' [])) + 1)", rendered);
    }

    @Test
    void rendersComparisonsAndConnectives() {
        String rendered = renderFirstCondition("  int32 a;\n  int32 b;\n",
                "      if (a < b) { ; }");
        assertEquals("((theInt (getVarVal st0 ''#a'' [])) < (theInt (getVarVal st0 ''#b'' [])))",
                rendered);

        String conjunction = renderFirstCondition("  bool x;\n  bool y;\n", "      if (x && y) { ; }");
        assertTrue(conjunction.contains("\\<and>"), conjunction);
    }

    @Test
    void rendersNegationAsHolNot() {
        String rendered = renderFirstCondition("  bool x;\n", "      if (!x) { ; }");
        assertEquals("(\\<not> (theBool (getVarVal st0 ''#x'' [])))", rendered);
    }

    @Test
    void rendersFieldAndIndexAccessAsAnAccessPath() {
        IrProgram program = build("  struct Point { int32 x; int32 y; }\n"
                        + "  Point p;\n  int32 a;\n",
                "      a = p.y;");
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        assertEquals("(theInt (getVarVal st0 ''#p'' [AccessField ''y'']))",
                renderer.renderExpression(assign.getValue(), "st0"));
    }

    @Test
    void coercesArrayIndicesToNat() {
        IrProgram program = build("  int32 v[4];\n  int32 a;\n  int32 i;\n", "      a = v[i];");
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        String rendered = renderer.renderExpression(assign.getValue(), "st0");
        assertTrue(rendered.contains("AccessIndex (nat "), "an int index needs nat coercion: " + rendered);
    }

    @Test
    void rendersProcessStateChecks() {
        String active = renderFirstCondition("", "      if (process Other in state active) { ; }");
        assertEquals("(getPstate st0 ''Other'' \\<noteq> ''stop'' \\<and> "
                + "getPstate st0 ''Other'' \\<noteq> ''error'')", active);

        String stopped = renderFirstCondition("", "      if (process Other in state stop) { ; }");
        assertEquals("(getPstate st0 ''Other'' = ''stop'')", stopped);
    }

    @Test
    void rendersCastsBetweenSorts() {
        // int32 -> uint8 crosses from int to nat.
        String rendered = renderFirstAssignedValue("  uint8 a;\n  int32 b;\n", "      a = b;");
        assertEquals("(nat (theInt (getVarVal st0 ''#b'' [])))", rendered);
    }

    @Test
    void omitsCastsWithinOneSort() {
        // int8 and int32 both map onto int, so no conversion is emitted.
        String rendered = renderFirstAssignedValue("  int8 a;\n  int32 b;\n", "      a = b;");
        assertEquals("(theInt (getVarVal st0 ''#b'' []))", rendered);
    }

    // ------------------------------------------------------------------ literals

    @Test
    void parsesIntegerLiteralsInEveryBase() {
        assertEquals(42, IsabelleRenderer.parseInteger("42"));
        assertEquals(255, IsabelleRenderer.parseInteger("0xFF"));
        assertEquals(8, IsabelleRenderer.parseInteger("010"));
        assertEquals(42, IsabelleRenderer.parseInteger("42L"));
        assertEquals(42, IsabelleRenderer.parseInteger("42u"));
        assertEquals(-7, IsabelleRenderer.parseInteger("-7"));
    }

    @Test
    void convertsTimeLiteralsToMilliseconds() {
        assertEquals(5, IsabelleRenderer.parseTimeMillis("0t5ms"));
        assertEquals(30_000, IsabelleRenderer.parseTimeMillis("0t30s"));
        assertEquals(300_000, IsabelleRenderer.parseTimeMillis("0t5m"));
        assertEquals(3_600_000, IsabelleRenderer.parseTimeMillis("0t1h"));
        assertEquals(86_400_000, IsabelleRenderer.parseTimeMillis("0t1d"));
        assertEquals(5_400_000, IsabelleRenderer.parseTimeMillis("0t1h30m"));
        assertEquals(5_415_000, IsabelleRenderer.parseTimeMillis("0t1h30m15s"));
    }

    /** 'ms' must win over 'm', or milliseconds would be read as minutes. */
    @Test
    void prefersMillisecondsOverMinutes() {
        assertEquals(500, IsabelleRenderer.parseTimeMillis("0t500ms"));
        assertEquals(500 * 60 * 1000L, IsabelleRenderer.parseTimeMillis("0t500m"));
    }

    // ------------------------------------------------------------------ statements

    @Test
    void rendersEachStatementKind() {
        assertEquals("base_inv:\"inv(st0)\"",
                renderer.renderStatement(new VcStatement.Invariant("st0")));
        assertEquals("st0:\"st0=emptyState\"",
                renderer.renderStatement(new VcStatement.EmptyState("st0")));
        assertEquals("st0_state:\"getPstate st0 ''Proc''=''idle''\"",
                renderer.renderStatement(new VcStatement.ProcessInState("st0", "Proc", "idle")));
        assertEquals("st1:\"st1=setPstate st0 ''Proc'' ''next''\"",
                renderer.renderStatement(new VcStatement.SetProcessState("st1", "st0", "Proc", "next")));
        assertEquals("st1:\"st1=reset st0 ''Proc''\"",
                renderer.renderStatement(new VcStatement.ResetTimer("st1", "st0", "Proc")));
        assertEquals("st1:\"st1=toEnv st0\"",
                renderer.renderStatement(new VcStatement.ToEnv("st1", "st0")));
        assertEquals("st_final:\"st_final=st3\"",
                renderer.renderStatement(new VcStatement.Final("st_final", "st3")));
    }

    @Test
    void rendersAssignmentThroughSetVarVal() {
        IrProgram program = build("  int32 a;\n", "      a = 1;");
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        String rendered = renderer.renderStatement(
                new VcStatement.Assign("st1", "st0", assign.getTarget(), assign.getValue()));
        assertEquals("st1:\"st1=(setVarVal st0 ''#a'' [] (ValInt 1))\"", rendered);
    }

    @Test
    void numbersConditionsWithinALemma() {
        IrProgram program = build("  bool x;\n", "      if (x) { ; }");
        IrExpr condition = assertInstanceOf(IrStmt.If.class,
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0)).getCondition();

        VerificationCondition vc = new VerificationCondition();
        vc.add(new VcStatement.Invariant("st0"));
        vc.add(new VcStatement.Condition("st0", condition));
        vc.add(new VcStatement.Condition("st0", condition));
        vc.add(new VcStatement.Final("st_final", "st0"));
        vc.setFinalState("st_final");

        String lemma = renderer.renderLemma(vc);
        assertTrue(lemma.contains("st0_condition_0:"), lemma);
        assertTrue(lemma.contains("st0_condition_1:"), lemma);
        assertTrue(lemma.startsWith("lemma\nassumes base_inv:"), lemma);
        assertTrue(lemma.endsWith("shows \"inv(st_final)\""), lemma);
    }

    @Test
    void rendersATheoryWrapper() {
        String theory = renderer.renderTheory("Demo_VC0", java.util.List.of("DemoTheory", "Requirements"), "lemma X");
        assertEquals("theory Demo_VC0\n\timports DemoTheory Requirements\nbegin\nlemma X\nend", theory);
    }
}
