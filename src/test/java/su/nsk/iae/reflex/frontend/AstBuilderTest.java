package su.nsk.iae.reflex.frontend;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AstBuilderTest {

    private static IrProgram build(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        return new AstBuilder().build(ctx);
    }

    private static IrProgram buildAnnotated(String source, AnnotationBinder[] binderOut) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        AnnotationBinder binder = new AnnotationBinder(tokens);
        binderOut[0] = binder;
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        return new AstBuilder(binder).build(ctx);
    }

    private static String inState(String statements) {
        return "program P {\n"
             + "  clock 100;\n"
             + "  node N { clock 100; }\n"
             + "  process Proc :: node N {\n"
             + "    state s {\n" + statements + "\n    }\n"
             + "  }\n"
             + "}";
    }

    private static List<IrStmt> statementsOf(IrProgram program) {
        return program.getProcesses().get(0).getStates().get(0).getStatements();
    }

    // ------------------------------------------------------------------ structure

    @Test
    void buildsProgramSkeleton() {
        IrProgram program = build("program Demo {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process A :: node N { state one { ; } state two { ; } }\n"
                + "  process B :: node N { state only { ; } }\n"
                + "}");

        assertEquals("Demo", program.getName());
        assertEquals("100", program.getClock().getText());
        assertEquals(2, program.getProcesses().size());
        assertEquals(1, program.getNodes().size());

        IrProcess a = program.findProcess("A");
        assertNotNull(a);
        assertEquals("N", a.getNodeName());
        assertEquals(List.of("one", "two"), a.getStates().stream().map(IrState::getName).toList());
        assertEquals("one", a.getStartState().getName(), "the first declared state is the start state");
        assertEquals("two", a.nextState("one").getName());
        assertNull(a.nextState("two"), "the last state has no successor");
    }

    @Test
    void buildsStateModifiersAndTimeout() {
        IrProgram program = build("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s looped { ; timeout 0t5s { stop; } }\n"
                + "  }\n"
                + "}");

        IrState state = program.getProcesses().get(0).getStates().get(0);
        assertTrue(state.isLooped());
        assertFalse(state.isSynthetic());
        assertNotNull(state.getTimeout());
        assertEquals("0t5s", state.getTimeout().getDuration().getText());
    }

    @Test
    void lowersProcessControlStatements() {
        IrProgram program = build(inState("      start Other; stop; error Other; restart; reset timer;"));
        List<IrStmt> statements = statementsOf(program);

        IrStmt.ProcessControl start = assertInstanceOf(IrStmt.ProcessControl.class, statements.get(0));
        assertEquals(IrStmt.ControlKind.START, start.getKind());
        assertEquals("Other", start.getProcess());

        IrStmt.ProcessControl stop = assertInstanceOf(IrStmt.ProcessControl.class, statements.get(1));
        assertEquals(IrStmt.ControlKind.STOP, stop.getKind());
        assertTrue(stop.targetsEnclosingProcess(), "a bare stop targets the enclosing process");

        IrStmt.ProcessControl error = assertInstanceOf(IrStmt.ProcessControl.class, statements.get(2));
        assertEquals(IrStmt.ControlKind.ERROR, error.getKind());
        assertEquals("Other", error.getProcess());

        assertEquals(IrStmt.ControlKind.RESTART,
                assertInstanceOf(IrStmt.ProcessControl.class, statements.get(3)).getKind());
        assertInstanceOf(IrStmt.ResetTimer.class, statements.get(4));
    }

    @Test
    void lowersSetState() {
        List<IrStmt> statements = statementsOf(build(inState("      set state other; set next state;")));

        IrStmt.SetState explicit = assertInstanceOf(IrStmt.SetState.class, statements.get(0));
        assertEquals("other", explicit.getState());
        assertFalse(explicit.isNext());

        IrStmt.SetState next = assertInstanceOf(IrStmt.SetState.class, statements.get(1));
        assertTrue(next.isNext());
        assertNull(next.getState(), "set next state is resolved later, by normalisation");
    }

    @Test
    void lowersIfElse() {
        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class,
                statementsOf(build(inState("      if (a > 1) { b = 1; } else { b = 2; }"))).get(0));

        IrExpr.Binary condition = assertInstanceOf(IrExpr.Binary.class, ifStmt.getCondition());
        assertEquals(IrExpr.BinaryOp.GT, condition.getOp());
        assertInstanceOf(IrStmt.Block.class, ifStmt.getThenBranch());
        assertNotNull(ifStmt.getElseBranch());

        IrStmt.If noElse = assertInstanceOf(IrStmt.If.class,
                statementsOf(build(inState("      if (a) { b = 1; }"))).get(0));
        assertNull(noElse.getElseBranch());
    }

    @Test
    void lowersSwitchIncludingFallthroughAndDefault() {
        IrStmt.Switch switchStmt = assertInstanceOf(IrStmt.Switch.class, statementsOf(build(inState(
                "      switch (x) { case 1: { a = 1; } case 2: { a = 2; break; } default: { a = 3; } }"))).get(0));

        assertEquals(3, switchStmt.getCases().size());

        IrStmt.SwitchCase first = switchStmt.getCases().get(0);
        assertFalse(first.isDefault());
        assertFalse(first.isBreaks(), "a case without break falls through");

        assertTrue(switchStmt.getCases().get(1).isBreaks());

        IrStmt.SwitchCase last = switchStmt.getCases().get(2);
        assertTrue(last.isDefault());
        assertNull(last.getLabel());
        assertSameCase(switchStmt.getDefaultCase(), last);
    }

    private static void assertSameCase(IrStmt.SwitchCase actual, IrStmt.SwitchCase expected) {
        assertTrue(actual == expected, "getDefaultCase should return the default clause");
    }

    @Test
    void lowersGuardingStatements() {
        List<IrStmt> statements = statementsOf(build(inState(
                "      slice; wait (a > 0); wait (b) on timeout 0t5s { c = 1; };")));

        assertInstanceOf(IrStmt.Slice.class, statements.get(0));

        IrStmt.Wait plain = assertInstanceOf(IrStmt.Wait.class, statements.get(1));
        assertFalse(plain.hasTimeout());

        IrStmt.Wait guarded = assertInstanceOf(IrStmt.Wait.class, statements.get(2));
        assertTrue(guarded.hasTimeout());
        assertEquals("0t5s", guarded.getTimeout().getText());
        assertNotNull(guarded.getTimeoutBody());
    }

    @Test
    void lowersUnsupportedConstructsRatherThanRejectingThem() {
        List<IrStmt> statements = statementsOf(build(inState(
                "      for (int32 i = 0; i < 3; i++) { a = i; }\n      $ raw c code")));

        IrStmt.For forStmt = assertInstanceOf(IrStmt.For.class, statements.get(0));
        assertEquals(1, forStmt.getInitDeclarations().size());
        assertEquals("i", forStmt.getInitDeclarations().get(0).getName());
        assertNotNull(forStmt.getCondition());
        assertNotNull(forStmt.getUpdate());

        IrStmt.CCode ccode = assertInstanceOf(IrStmt.CCode.class, statements.get(1));
        assertEquals(" raw c code", ccode.getCode(), "the '$' delimiter is stripped");
    }

    // ------------------------------------------------------------------ expressions

    @Test
    void lowersAccessPaths() {
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                statementsOf(build(inState("      a.b[2].c = 1;"))).get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        IrExpr.VarRef target = assign.getTarget();
        assertEquals("a", target.getName());
        assertEquals(3, target.getAccesses().size());
        assertEquals("b", assertInstanceOf(IrExpr.FieldAccess.class, target.getAccesses().get(0)).getField());
        assertInstanceOf(IrExpr.IndexAccess.class, target.getAccesses().get(1));
        assertEquals("c", assertInstanceOf(IrExpr.FieldAccess.class, target.getAccesses().get(2)).getField());
    }

    @Test
    void lowersCompoundAssignmentWithItsUnderlyingOperator() {
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                statementsOf(build(inState("      a += 2;"))).get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        assertEquals(IrExpr.AssignOp.ADD, assign.getOp());
        assertEquals(IrExpr.BinaryOp.ADD, assign.getOp().underlying());
        assertNull(IrExpr.AssignOp.ASSIGN.underlying(), "plain '=' has no underlying operator");
    }

    @Test
    void lowersIncrementAndDecrement() {
        List<IrStmt> statements = statementsOf(build(inState("      a++; --b;")));

        IrExpr.IncDec postfix = assertInstanceOf(IrExpr.IncDec.class,
                assertInstanceOf(IrStmt.ExprStatement.class, statements.get(0)).getExpression());
        assertEquals(IrExpr.IncDecOp.INCREMENT, postfix.getOp());
        assertFalse(postfix.isPrefix());

        IrExpr.IncDec prefix = assertInstanceOf(IrExpr.IncDec.class,
                assertInstanceOf(IrStmt.ExprStatement.class, statements.get(1)).getExpression());
        assertEquals(IrExpr.IncDecOp.DECREMENT, prefix.getOp());
        assertTrue(prefix.isPrefix());
    }

    @Test
    void lowersProcessStateChecks() {
        IrStmt.If ifStmt = assertInstanceOf(IrStmt.If.class,
                statementsOf(build(inState("      if (process Other in state error) { ; }"))).get(0));

        IrExpr.CheckState check = assertInstanceOf(IrExpr.CheckState.class, ifStmt.getCondition());
        assertEquals("Other", check.getProcess());
        assertEquals(IrExpr.ProcessStatus.ERROR, check.getStatus());
        assertEquals(IrType.BOOL, check.getResultType());
    }

    @Test
    void lowersExplicitCasts() {
        IrStmt.ExprStatement stmt = assertInstanceOf(IrStmt.ExprStatement.class,
                statementsOf(build(inState("      a = (int32) b;"))).get(0));
        IrExpr.Assign assign = assertInstanceOf(IrExpr.Assign.class, stmt.getExpression());

        IrExpr.Cast cast = assertInstanceOf(IrExpr.Cast.class, assign.getValue());
        assertEquals(IrType.of(IrType.BuiltinKind.INT32), cast.getTargetType());
        assertFalse(cast.isImplicit(), "a written cast is not implicit");
    }

    // ------------------------------------------------------------------ types

    @Test
    void lowersArrayDimensionsOutermostFirst() {
        IrProgram program = build(inState("      int32 g[2][3];"));
        IrStmt.LocalVar local = assertInstanceOf(IrStmt.LocalVar.class, statementsOf(program).get(0));

        // int32 g[2][3] is an array of 2 whose elements are arrays of 3.
        IrType.Array outer = assertInstanceOf(IrType.Array.class, local.getDeclaration().getType());
        assertEquals(2, outer.size());
        IrType.Array inner = assertInstanceOf(IrType.Array.class, outer.element());
        assertEquals(3, inner.size());
        assertEquals(IrType.of(IrType.BuiltinKind.INT32), inner.element());
    }

    @Test
    void leavesArrayExtentOpenWhenTakenFromInitializer() {
        IrProgram program = build(inState("      int32 a[] = {1, 2, 3};"));
        IrStmt.LocalVar local = assertInstanceOf(IrStmt.LocalVar.class, statementsOf(program).get(0));

        IrType.Array type = assertInstanceOf(IrType.Array.class, local.getDeclaration().getType());
        assertNull(type.size(), "an omitted extent is resolved from the initialiser later");

        IrExpr.Aggregate init = assertInstanceOf(IrExpr.Aggregate.class,
                local.getDeclaration().getInitializer());
        assertEquals(3, init.getElements().size());
    }

    @Test
    void lowersStructTypesAsUnresolvedNames() {
        IrProgram program = build("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point origin;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        IrDecl.Struct struct = program.findStruct("Point");
        assertNotNull(struct);
        assertEquals(2, struct.getFields().size());
        assertNotNull(struct.findField("y"));

        IrDecl.Variable origin = assertInstanceOf(IrDecl.Variable.class, program.getGlobalVariables().get(0));
        // Resolution to IrType.Struct happens once all declarations are known.
        assertEquals(new IrType.Named("Point"), origin.getType());
    }

    @Test
    void lowersPartialAndDesignatedInitializers() {
        IrProgram program = build("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point p = {.y = 5};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        IrDecl.Variable p = assertInstanceOf(IrDecl.Variable.class, program.getGlobalVariables().get(0));
        IrExpr.Aggregate init = assertInstanceOf(IrExpr.Aggregate.class, p.getInitializer());

        assertEquals(1, init.getElements().size(), "omitted members simply have no element");
        IrExpr.Aggregate.Element element = init.getElements().get(0);
        assertEquals("y", assertInstanceOf(IrExpr.FieldAccess.class, element.getDesignator()).getField());
    }

    @Test
    void lowersPhysicalVariableBindings() {
        IrProgram program = build("program P {\n"
                + "  clock 100;\n"
                + "  import IO { register inp }\n"
                + "  direct bool a as (read = inp, write = outp, bit = 3);\n"
                + "  bool b as (read = inp);\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");

        IrDecl.PhysicalVariable a =
                assertInstanceOf(IrDecl.PhysicalVariable.class, program.getGlobalVariables().get(0));
        assertTrue(a.isDirect());
        assertEquals("inp", a.getReadPort());
        assertEquals("outp", a.getWritePort());
        assertEquals("3", a.getBit());

        IrDecl.PhysicalVariable b =
                assertInstanceOf(IrDecl.PhysicalVariable.class, program.getGlobalVariables().get(1));
        assertFalse(b.isDirect());
        assertNull(b.getBit());
    }

    // ------------------------------------------------------------------ annotations

    @Test
    void bindsAnnotationsToTheConstructsTheyPrecede() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated("//[invariant: x > 0]\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  //[assume: y > 0]\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: z > 0]\n"
                + "    state s {\n"
                + "      //[assert: w > 0]\n"
                + "      a = 1;\n"
                + "    }\n"
                + "  }\n"
                + "}", binder);

        assertEquals(1, program.getAnnotations().size());
        assertEquals(Annotation.Kind.INVARIANT, program.getAnnotations().get(0).getKind());

        IrProcess process = program.getProcesses().get(0);
        assertEquals(1, process.getAnnotations().size());
        assertEquals(Annotation.Kind.ASSUME, process.getAnnotations().get(0).getKind());

        IrState state = process.getStates().get(0);
        assertEquals(1, state.getAnnotations().size());
        assertEquals(Annotation.Kind.ASSERT, state.getAnnotations().get(0).getKind());

        IrStmt statement = state.getStatements().get(0);
        assertEquals(1, statement.getAnnotations().size());
        assertEquals(Annotation.Kind.ASSERT, statement.getAnnotations().get(0).getKind());

        assertEquals(4, binder[0].getAllAnnotations().size());
        assertTrue(binder[0].getDiagnostics().isEmpty());
    }

    /** A statement and the expression inside it share a start token; only one may claim. */
    @Test
    void claimsEachAnnotationExactlyOnce() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated(inState("      //[assert: a > 0]\n      a = 1;"), binder);

        IrStmt statement = statementsOf(program).get(0);
        assertEquals(1, statement.getAnnotations().size());

        IrExpr expression = assertInstanceOf(IrStmt.ExprStatement.class, statement).getExpression();
        assertTrue(expression.getAnnotations().isEmpty(),
                "the statement claims the annotation, not the expression inside it");
        assertEquals(1, binder[0].getAllAnnotations().size());
    }

    @Test
    void ignoresOrdinaryComments() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated("// just a note\n"
                + "program P {\n"
                + "  clock 100; /* explanatory aside */\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}", binder);

        assertTrue(program.getAnnotations().isEmpty());
        assertTrue(binder[0].getAllAnnotations().isEmpty());
        assertTrue(binder[0].getDiagnostics().isEmpty(), "prose is not a malformed annotation");
    }

    @Test
    void reportsMalformedAnnotationsWithoutFailing() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated("//[nonsense: ]\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}", binder);

        assertEquals("P", program.getName(), "a bad annotation must not stop the build");
        assertTrue(program.getAnnotations().isEmpty());
        assertEquals(1, binder[0].getDiagnostics().size());
    }

    @Test
    void parsesBlockCommentsCarryingSeveralAnnotations() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated("/*[assume: a > 0][assert: b > 0]*/\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}", binder);

        assertEquals(2, program.getAnnotations().size());
        assertEquals(Annotation.Kind.ASSUME, program.getAnnotations().get(0).getKind());
        assertEquals(Annotation.Kind.ASSERT, program.getAnnotations().get(1).getKind());
    }

    /** Array subscripts inside an annotation body must not terminate it early. */
    @Test
    void handlesNestedBracketsInAnnotationBodies() {
        AnnotationBinder[] binder = new AnnotationBinder[1];
        IrProgram program = buildAnnotated("//[invariant: a[0] > 0]\n"
                + "program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}", binder);

        assertEquals(1, program.getAnnotations().size());
        assertTrue(binder[0].getDiagnostics().isEmpty());
        assertTrue(program.getAnnotations().get(0).getText().contains("a[0]"));
    }
}
