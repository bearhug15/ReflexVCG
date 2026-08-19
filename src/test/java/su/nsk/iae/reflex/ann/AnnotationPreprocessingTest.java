package su.nsk.iae.reflex.ann;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Annotations are lowered to their own tree and then put through the same name mangling as
 * the program, which the translation to Isabelle depends on: it reads a variable under its
 * final name.
 */
class AnnotationPreprocessingTest {

    private static IrProgram process(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        AnnotationBinder binder = new AnnotationBinder(tokens);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse");
        IrProgram program = new AstBuilder(binder).build(ctx);
        Preprocessor.run(program);
        return program;
    }

    /** Every variable name mentioned anywhere in an annotation body. */
    private static List<String> namesIn(AnnExpr expr) {
        List<String> names = new ArrayList<>();
        collect(expr, names);
        return names;
    }

    private static void collect(AnnExpr expr, List<String> names) {
        if (expr == null) {
            return;
        }
        if (expr instanceof AnnExpr.VarRef ref) {
            names.add(ref.getName());
            ref.getAccesses().forEach(a -> {
                if (a instanceof AnnExpr.IndexAccess index) {
                    collect(index.getIndex(), names);
                }
            });
        } else if (expr instanceof AnnExpr.Binary binary) {
            collect(binary.getLeft(), names);
            collect(binary.getRight(), names);
        } else if (expr instanceof AnnExpr.Unary unary) {
            collect(unary.getOperand(), names);
        } else if (expr instanceof AnnExpr.Implication implication) {
            collect(implication.getLeft(), names);
            collect(implication.getRight(), names);
        } else if (expr instanceof AnnExpr.Equivalence equivalence) {
            collect(equivalence.getLeft(), names);
            collect(equivalence.getRight(), names);
        } else if (expr instanceof AnnExpr.Quantifier quantifier) {
            collect(quantifier.getBody(), names);
        } else if (expr instanceof AnnExpr.Temporal temporal) {
            collect(temporal.getFirst(), names);
            collect(temporal.getSecond(), names);
            collect(temporal.getThird(), names);
        } else if (expr instanceof AnnExpr.Scope scope) {
            collect(scope.getBase(), names);
            collect(scope.getPhi(), names);
        } else if (expr instanceof AnnExpr.Call call) {
            call.getArguments().forEach(a -> collect(a, names));
        }
    }

    private static AnnExpr firstStateAnnotationBody(IrProgram program) {
        List<Annotation> annotations =
                program.getProcesses().get(0).getStates().get(0).getAnnotations();
        assertFalse(annotations.isEmpty(), "expected an annotation on the state");
        return annotations.get(0).getBody();
    }

    // ------------------------------------------------------------------ lowering

    @Test
    void lowersAnAnnotationBodyIntoItsOwnTree() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: x > 0]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        AnnExpr body = firstStateAnnotationBody(program);
        AnnExpr.Binary comparison = assertInstanceOf(AnnExpr.Binary.class, body);
        assertEquals(AnnExpr.BinaryOp.GT, comparison.getOp());
        assertInstanceOf(AnnExpr.VarRef.class, comparison.getLeft());
        assertInstanceOf(AnnExpr.Literal.class, comparison.getRight());
    }

    @Test
    void lowersImplicationAndEquivalence() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: a ==> b]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");
        assertInstanceOf(AnnExpr.Implication.class, firstStateAnnotationBody(program));

        IrProgram other = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: a <==> b]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");
        assertInstanceOf(AnnExpr.Equivalence.class, firstStateAnnotationBody(other));
    }

    @Test
    void lowersTemporalOperators() {
        record Case(String text, AnnExpr.Temporal.Kind kind) {
        }
        List<Case> cases = List.of(
                new Case("previously(a)", AnnExpr.Temporal.Kind.PREVIOUSLY),
                new Case("next(a)", AnnExpr.Temporal.Kind.NEXT),
                new Case("once(a)", AnnExpr.Temporal.Kind.ONCE),
                new Case("timer(5)", AnnExpr.Temporal.Kind.TIMER),
                new Case("during(a, b, c)", AnnExpr.Temporal.Kind.DURING),
                new Case("within(a, 5)", AnnExpr.Temporal.Kind.WITHIN),
                new Case("stable(a, 5)", AnnExpr.Temporal.Kind.STABLE),
                new Case("cooldown(a, 5)", AnnExpr.Temporal.Kind.COOLDOWN),
                // on() is specified in the translation document but was missing from the
                // grammar, so it could not be written until now.
                new Case("on(a, b)", AnnExpr.Temporal.Kind.ON));

        for (Case testCase : cases) {
            IrProgram program = process("program P {\n"
                    + "  clock 100;\n"
                    + "  node N { clock 100; }\n"
                    + "  process Proc :: node N {\n"
                    + "    //[assert: " + testCase.text() + "]\n"
                    + "    state s { ; }\n"
                    + "  }\n"
                    + "}");
            AnnExpr.Temporal temporal =
                    assertInstanceOf(AnnExpr.Temporal.class, firstStateAnnotationBody(program),
                            testCase.text());
            assertEquals(testCase.kind(), temporal.getKind(), testCase.text());
        }
    }

    @Test
    void lowersQuantifiersWithTheirDomains() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: forall(i in 0..10: i > 0)]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        AnnExpr.Quantifier quantifier =
                assertInstanceOf(AnnExpr.Quantifier.class, firstStateAnnotationBody(program));
        assertEquals(AnnExpr.Quantifier.Kind.FORALL, quantifier.getKind());
        assertEquals(1, quantifier.getVariables().size());
        assertEquals("i", quantifier.getVariables().get(0).getName());
        assertInstanceOf(AnnExpr.RangeDomain.class, quantifier.getVariables().get(0).getDomain());
    }

    @Test
    void lowersProcessOrientedFunctions() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: in(Other, idle)]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "  process Other :: node N { state idle { ; } }\n"
                + "}");

        AnnExpr.InState inState =
                assertInstanceOf(AnnExpr.InState.class, firstStateAnnotationBody(program));
        assertEquals("Other", inState.getProcess());
        assertEquals("idle", inState.getPstate());
    }

    @Test
    void lowersDefinitions() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[define: int limit = 10, bool over(int v) = v > limit]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        List<Annotation> annotations =
                program.getProcesses().get(0).getStates().get(0).getAnnotations();
        List<AnnDefinition> definitions = annotations.get(0).getDefinitions();

        assertEquals(2, definitions.size());
        assertEquals("limit", definitions.get(0).name());
        assertTrue(definitions.get(0).isVariable());
        assertEquals("over", definitions.get(1).name());
        assertEquals(List.of("v"), definitions.get(1).parameters());
        assertNull(annotations.get(0).getBody(), "a define carries definitions, not a formula");
    }

    // ------------------------------------------------------------------ mangling

    /** The point of the pass: an annotation's names are resolved like the program's. */
    @Test
    void manglesAnnotationVariablesToTheScopeTheyAppearIn() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  int32 global;\n"
                + "  node N { clock 100; int32 nodeVar; }\n"
                + "  process Proc :: node N {\n"
                + "    int32 procVar;\n"
                + "    //[assert: global > 0 && nodeVar > 0 && procVar > 0]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        assertEquals(List.of("#global", "N#nodeVar", "N#Proc#procVar"),
                namesIn(firstStateAnnotationBody(program)));
    }

    /** Written qualified, the name is already final and must not be rewritten again. */
    @Test
    void leavesAlreadyQualifiedNamesAlone() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    int32 v;\n"
                + "    //[assert: N#Proc#v > 0]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        assertEquals(List.of("N#Proc#v"), namesIn(firstStateAnnotationBody(program)));
    }

    /** A quantified variable is the annotation's own name, not a program variable. */
    @Test
    void leavesQuantifiedVariablesAlone() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  int32 limit;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: forall(i: i < limit)]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        assertEquals(List.of("i", "#limit"), namesIn(firstStateAnnotationBody(program)),
                "i is bound by the quantifier; limit is a program variable");
    }

    /** on(trigger, property) carries both arguments, and both are mangled. */
    @Test
    void lowersAndManglesBothArgumentsOfOn() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  bool alarm;\n"
                + "  bool siren;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[invariant: on(alarm, siren)]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        AnnExpr.Temporal on =
                assertInstanceOf(AnnExpr.Temporal.class, firstStateAnnotationBody(program));
        assertEquals(AnnExpr.Temporal.Kind.ON, on.getKind());
        assertEquals("#alarm", assertInstanceOf(AnnExpr.VarRef.class, on.getFirst()).getName());
        assertEquals("#siren", assertInstanceOf(AnnExpr.VarRef.class, on.getSecond()).getName());
    }

    @Test
    void manglesInsideTemporalOperatorsAndScopes() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  int32 v;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert: once(v > 0) && v.scope(pre) > 0]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        assertEquals(List.of("#v", "#v"), namesIn(firstStateAnnotationBody(program)));
    }

    @Test
    void manglesAnnotationsOnStatements() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  int32 v;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n"
                + "      //[assert: v > 0]\n"
                + "      v = 1;\n"
                + "    }\n"
                + "  }\n"
                + "}");

        List<Annotation> annotations =
                program.getProcesses().get(0).getStates().get(0).getStatements().get(0)
                        .getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals(List.of("#v"), namesIn(annotations.get(0).getBody()));
    }

    @Test
    void manglesDefinitionBodiesButNotTheirParameters() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  int32 limit;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[define: bool over(int v) = v > limit]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        AnnDefinition definition = program.getProcesses().get(0).getStates().get(0)
                .getAnnotations().get(0).getDefinitions().get(0);
        assertEquals(List.of("v", "#limit"), namesIn(definition.body()),
                "v is a formal parameter; limit is a program variable");
    }

    @Test
    void keepsAnnotationsInAnotherLanguageUnparsed() {
        IrProgram program = process("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[assert(isabelle): whatever the prover understands]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");

        List<Annotation> annotations =
                program.getProcesses().get(0).getStates().get(0).getAnnotations();
        assertEquals(1, annotations.size());
        assertTrue(annotations.get(0).isForeignLanguage());
        assertNull(annotations.get(0).getBody(), "a foreign body is passed through unparsed");
        assertNotNull(annotations.get(0).getText());
    }
}
