package su.nsk.iae.reflex.preprocess;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastInsertionPassTest {

    private static final IrType INT8 = IrType.of(IrType.BuiltinKind.INT8);
    private static final IrType INT16 = IrType.of(IrType.BuiltinKind.INT16);
    private static final IrType INT64 = IrType.of(IrType.BuiltinKind.INT64);
    private static final IrType DOUBLE = IrType.of(IrType.BuiltinKind.DOUBLE);
    private static final IrType FLOAT = IrType.of(IrType.BuiltinKind.FLOAT);

    private static IrProgram processed(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        IrProgram program = new AstBuilder().build(ctx);

        NameManglingPass mangling = new NameManglingPass();
        mangling.run(program);
        TypeEnvironment types = new TypeEnvironment(program, mangling.getDirectAccessNames());
        new CastInsertionPass(types).run(program);
        return program;
    }

    /** Builds a program whose single state contains the given declarations/statements. */
    private static IrProgram inState(String declarations, String statements) {
        return processed("program P {\n"
                + "  clock 100;\n"
                + declarations
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n" + statements + "\n    }\n"
                + "  }\n"
                + "}");
    }

    private static List<IrStmt> statementsOf(IrProgram program) {
        return program.getProcesses().get(0).getStates().get(0).getStatements();
    }

    private static IrExpr expressionAt(IrProgram program, int index) {
        return assertInstanceOf(IrStmt.ExprStatement.class, statementsOf(program).get(index)).getExpression();
    }

    // ------------------------------------------------------------------ defType

    @Test
    void promotesNarrowIntegerOperandsToInt32() {
        assertEquals(IrType.INT32, CastInsertionPass.defType("+", INT8, INT8));
        assertEquals(IrType.INT32, CastInsertionPass.defType("+", INT8, INT16));
        assertEquals(IrType.INT32, CastInsertionPass.defType("+", IrType.BOOL, INT8));
    }

    @Test
    void widensToTheLargerOperandBeyondInt32() {
        assertEquals(INT64, CastInsertionPass.defType("+", IrType.INT32, INT64));
        assertEquals(INT64, CastInsertionPass.defType("+", INT64, INT8));
    }

    @Test
    void floatingOperandsWin() {
        assertEquals(FLOAT, CastInsertionPass.defType("+", IrType.INT32, FLOAT));
        assertEquals(DOUBLE, CastInsertionPass.defType("+", FLOAT, DOUBLE));
        assertEquals(DOUBLE, CastInsertionPass.defType("*", DOUBLE, INT64));
    }

    @Test
    void logicalConnectivesAreAlwaysBool() {
        assertEquals(IrType.BOOL, CastInsertionPass.defType("&&", IrType.INT32, INT64));
        assertEquals(IrType.BOOL, CastInsertionPass.defType("||", IrType.BOOL, IrType.BOOL));
        assertEquals(IrType.BOOL, CastInsertionPass.defType("!.", null, IrType.INT32));
    }

    /** An undefined literal type adapts to the other operand rather than forcing int32. */
    @Test
    void literalsAdaptToTheOtherOperand() {
        assertEquals(INT8, CastInsertionPass.defType("+", INT8, IrType.UNDEFINED_INT));
        assertEquals(INT8, CastInsertionPass.defType("+", IrType.UNDEFINED_INT, INT8));
        assertEquals(IrType.UNDEFINED_INT,
                CastInsertionPass.defType("+", IrType.UNDEFINED_INT, IrType.UNDEFINED_INT));
        assertEquals(IrType.UNDEFINED_FLOAT,
                CastInsertionPass.defType("+", IrType.UNDEFINED_INT, IrType.UNDEFINED_FLOAT));
    }

    // ------------------------------------------------------------------ expressions

    @Test
    void recordsAResultTypeOnEveryExpression() {
        IrProgram program = inState("  int32 a;\n", "      a = a + 1;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        assertEquals(IrType.INT32, assign.getResultType());
        assertEquals(IrType.INT32, assign.getTarget().getResultType());
        assertEquals(IrType.INT32, assign.getValue().getResultType());
    }

    /** The point of adaptive literal types: `a + 1` on an int8 stays an int8 operation. */
    @Test
    void doesNotWidenAVariableToSuitALiteral() {
        IrProgram program = inState("  int8 a;\n", "      a = a + 1;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        IrExpr.Binary sum = assertInstanceOf(IrExpr.Binary.class, assign.getValue());
        assertEquals(INT8, sum.getResultType());
        assertInstanceOf(IrExpr.VarRef.class, sum.getLeft());
        assertInstanceOf(IrExpr.Literal.class, sum.getRight());
    }

    @Test
    void insertsImplicitCastOnTheNarrowerOperand() {
        IrProgram program = inState("  int8 a;\n  int64 b;\n", "      b = a + b;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        IrExpr.Binary sum = assertInstanceOf(IrExpr.Binary.class, assign.getValue());
        assertEquals(INT64, sum.getResultType());

        IrExpr.Cast widened = assertInstanceOf(IrExpr.Cast.class, sum.getLeft());
        assertTrue(widened.isImplicit(), "the pass inserted this cast");
        assertEquals(INT64, widened.getTargetType());
        assertEquals(INT8, widened.getPreType(), "the pre-type records what it was widened from");
        assertInstanceOf(IrExpr.VarRef.class, widened.getOperand());

        assertInstanceOf(IrExpr.VarRef.class, sum.getRight(), "the wider operand is untouched");
    }

    @Test
    void convertsAssignedValueToTheTargetType() {
        IrProgram program = inState("  int8 a;\n  int64 b;\n", "      a = b;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        IrExpr.Cast narrowed = assertInstanceOf(IrExpr.Cast.class, assign.getValue());
        assertEquals(INT8, narrowed.getTargetType());
        assertEquals(INT64, narrowed.getPreType());
        assertEquals(INT8, assign.getResultType());
    }

    /** A comparison happens at the promoted type but yields a bool. */
    @Test
    void comparisonsAreBoolButOperandsStillConverge() {
        IrProgram program = inState("  int8 a;\n  int64 b;\n  bool r;\n", "      r = a < b;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        IrExpr.Binary comparison = assertInstanceOf(IrExpr.Binary.class, assign.getValue());
        assertEquals(IrType.BOOL, comparison.getResultType());

        IrExpr.Cast left = assertInstanceOf(IrExpr.Cast.class, comparison.getLeft());
        assertEquals(INT64, left.getTargetType(), "operands still meet at the wider type");
    }

    @Test
    void convertsNonBooleanConditionsToBool() {
        IrProgram program = inState("  int32 a;\n", "      if (a) { a = 1; }");
        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class, statementsOf(program).get(0));

        IrExpr.Cast condition = assertInstanceOf(IrExpr.Cast.class, ifStmt.getCondition());
        assertEquals(IrType.BOOL, condition.getTargetType());
        assertEquals(IrType.INT32, condition.getPreType());
    }

    @Test
    void leavesAlreadyBooleanConditionsAlone() {
        IrProgram program = inState("  bool flag;\n", "      if (flag) { ; }");
        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class, statementsOf(program).get(0));

        assertInstanceOf(IrExpr.VarRef.class, ifStmt.getCondition(), "no cast should be added");
        assertEquals(IrType.BOOL, ifStmt.getCondition().getResultType());
    }

    @Test
    void keepsExplicitCastsAndRecordsTheirPreType() {
        IrProgram program = inState("  int64 b;\n  int32 a;\n", "      a = (int32) b;");
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));

        IrExpr.Cast cast = assertInstanceOf(IrExpr.Cast.class, assign.getValue());
        assertFalse(cast.isImplicit(), "a written cast stays explicit");
        assertEquals(IrType.INT32, cast.getTargetType());
        assertEquals(INT64, cast.getPreType());
    }

    @Test
    void typesProcessStateChecksAsBool() {
        IrProgram program = processed("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { if (process Other in state active) { ; } }\n"
                + "  }\n"
                + "  process Other :: node N { state only { ; } }\n"
                + "}");

        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class, statementsOf(program).get(0));
        assertEquals(IrType.BOOL, ifStmt.getCondition().getResultType());
        assertInstanceOf(IrExpr.CheckState.class, ifStmt.getCondition());
    }

    // ------------------------------------------------------------------ aggregates

    @Test
    void typesArrayAndStructAccessThroughTheAccessPath() {
        IrProgram program = processed("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int8 x; int64 y; }\n"
                + "  Point p;\n"
                + "  Point path[4];\n"
                + "  int32 a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { a = p.x; a = path[2].y; }\n"
                + "  }\n"
                + "}");

        IrExpr.Assign first = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));
        IrExpr.Cast fromField = assertInstanceOf(IrExpr.Cast.class, first.getValue());
        assertEquals(INT8, fromField.getPreType(), "p.x is the field's type");

        IrExpr.Assign second = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 1));
        IrExpr.Cast fromElement = assertInstanceOf(IrExpr.Cast.class, second.getValue());
        assertEquals(INT64, fromElement.getPreType(), "path[2].y walks array then field");
    }

    @Test
    void convertsArrayInitializerElementsToTheElementType() {
        IrProgram program = processed("program P {\n"
                + "  clock 100;\n"
                + "  int64 v[3] = {1, 2, 3};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        IrExpr initializer = ((su.nsk.iae.reflex.ir.IrDecl.Variable) program.getGlobalVariables().get(0))
                .getInitializer();
        IrExpr.Aggregate aggregate = assertInstanceOf(IrExpr.Aggregate.class, initializer);
        assertEquals(3, aggregate.getElements().size());
        // Literals adapt, so no cast is needed for a plain integer literal.
        aggregate.getElements().forEach(e -> assertNotNull(e.getValue().getResultType()));
    }

    @Test
    void resolvesStructAndEnumTypeNames() {
        IrProgram program = processed("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  enum Colour { Red, Green }\n"
                + "  Point p;\n"
                + "  Colour c;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        su.nsk.iae.reflex.ir.IrDecl.Variable p =
                (su.nsk.iae.reflex.ir.IrDecl.Variable) program.getGlobalVariables().get(0);
        su.nsk.iae.reflex.ir.IrDecl.Variable c =
                (su.nsk.iae.reflex.ir.IrDecl.Variable) program.getGlobalVariables().get(1);

        assertEquals(new IrType.Struct("Point"), p.getType(), "Named should resolve to Struct");
        assertEquals(new IrType.Enum("Colour"), c.getType(), "Named should resolve to Enum");
    }

    /** A direct binding's per-access names have no declaration but must still be typed. */
    @Test
    void typesPerAccessNamesOfDirectBindings() {
        IrProgram program = processed("program P {\n"
                + "  clock 100;\n"
                + "  input inp 0x00 0x00 24;\n"
                + "  direct bool sensor as (read = inp, bit = 1);\n"
                + "  bool a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { a = sensor; a = sensor; } }\n"
                + "}");

        IrExpr.Assign first = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 0));
        IrExpr.Assign second = assertInstanceOf(IrExpr.Assign.class, expressionAt(program, 1));

        // Both reads carry distinct mangled names (inp_1.0, inp_1.1) yet take the type
        // of the binding they came from.
        assertEquals("inp_1.0", ((IrExpr.VarRef) first.getValue()).getName());
        assertEquals("inp_1.1", ((IrExpr.VarRef) second.getValue()).getName());
        assertEquals(IrType.BOOL, first.getValue().getResultType());
        assertEquals(IrType.BOOL, second.getValue().getResultType());
    }
}
