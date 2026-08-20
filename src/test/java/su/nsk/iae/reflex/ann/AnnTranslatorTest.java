package su.nsk.iae.reflex.ann;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Translation of annotations into Isabelle formulas - the parseA of Annotations.tex. */
class AnnTranslatorTest {

    private static final Term STATE = new Term.Var("s");
    private final TermRenderer renderer = new TermRenderer();

    /** Builds a program with one annotation on its single state and returns it. */
    private static Annotation annotationOn(String declarations, String annotation) {
        String source = "program P {\n"
                + "  clock 100;\n"
                + declarations
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    " + annotation + "\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "  process Other :: node N { state idle { ; } }\n"
                + "}";
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        AnnotationBinder binder = new AnnotationBinder(tokens);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse");
        assertTrue(binder.getDiagnostics().isEmpty(), binder.getDiagnostics().toString());

        IrProgram program = new AstBuilder(binder).build(ctx);
        Preprocessor.run(program);
        List<Annotation> annotations = program.getProcesses().get(0).getStates().get(0).getAnnotations();
        assertFalse(annotations.isEmpty(), "expected an annotation");
        return annotations.get(0);
    }

    /** Translates an assert, which is stated at one state and gets no wrapper. */
    private String translate(String declarations, String body) {
        Annotation annotation = annotationOn(declarations, "//[assert: " + body + "]");
        AnnTranslator translator = new AnnTranslator(100);
        translator.register(annotation);
        return renderer.render(translator.translateAt(annotation, STATE, STATE));
    }

    // ------------------------------------------------------------------ values

    @Test
    void readsAVariableThroughGetVarValAndProjectsIt() {
        assertEquals("(theInt (getVarVal s ''#x'' []))", translate("  int32 x;\n", "x"));
    }

    @Test
    void readsABooleanWithTheBool() {
        assertEquals("(theBool (getVarVal s ''#flag'' []))", translate("  bool flag;\n", "flag"));
    }

    @Test
    void rendersComparisonsAgainstLiterals() {
        assertEquals("((theInt (getVarVal s ''#x'' [])) > 0)", translate("  int32 x;\n", "x > 0"));
    }

    @Test
    void rendersConjunctionAndDisjunction() {
        assertTrue(translate("  bool a;\n  bool b;\n", "a && b").contains("\\<and>"));
        assertTrue(translate("  bool a;\n  bool b;\n", "a || b").contains("\\<or>"));
    }

    @Test
    void rendersImplicationAndEquivalence() {
        assertTrue(translate("  bool a;\n  bool b;\n", "a ==> b").contains("\\<longrightarrow>"));

        String equivalence = translate("  bool a;\n  bool b;\n", "a <==> b");
        // Both directions, joined.
        assertEquals(2, equivalence.split("\\\\<longrightarrow>", -1).length - 1, equivalence);
    }

    @Test
    void rendersAccessPaths() {
        String rendered = translate("  struct Point { int32 x; int32 y; }\n  Point p;\n", "p.y");
        assertEquals("(theInt (getVarVal s ''#p'' [(AccessField ''y'')]))", rendered);
    }

    // ------------------------------------------------------- process functions

    @Test
    void rendersInAndTime() {
        assertEquals("((getPstate s ''Other'') = ''idle'')", translate("", "in(Other, idle)"));
        assertEquals("(ltime s ''Other'')", translate("", "time(Other)"));
    }

    // ------------------------------------------------------------------ temporal

    @Test
    void previouslyShiftsToThePrecedingBoundary() {
        assertEquals("(theBool (getVarVal (predEnv s) ''#a'' []))",
                translate("  bool a;\n", "previously(a)"));
    }

    @Test
    void onceSearchesTheReachablePast() {
        String rendered = translate("  bool a;\n", "once(a)");
        assertTrue(rendered.startsWith("(\\<exists> "), rendered);
        assertTrue(rendered.contains("(toEnvP "), rendered);
        assertTrue(rendered.contains("(substate "), rendered);
    }

    @Test
    void nextSearchesForTheSuccessorState() {
        String rendered = translate("  bool a;\n", "next(a)");
        assertTrue(rendered.startsWith("(\\<exists> "), rendered);
        // The successor is expressed through predEnv, there being no forward step.
        assertTrue(rendered.contains("(predEnv "), rendered);
    }

    /** Outside a loop the timer counts cycles, so it is scaled by the clock. */
    @Test
    void timerMultipliesByTheClockOutsideALoop() {
        String rendered = translate("", "timer(500)");
        assertTrue(rendered.contains("(toEnvNum "), rendered);
        assertTrue(rendered.contains("* 100"), rendered);
        assertTrue(rendered.contains("> 500"), rendered);
    }

    @Test
    void onQuantifiesOverReachableStates() {
        String rendered = translate("  bool alarm;\n  bool siren;\n", "on(alarm, siren)");
        assertTrue(rendered.startsWith("(\\<forall> "), rendered);
        assertTrue(rendered.contains("\\<longrightarrow>"), rendered);
        assertTrue(rendered.contains("#alarm"), rendered);
        assertTrue(rendered.contains("#siren"), rendered);
    }

    @Test
    void withinIsExistentialAndStableIsUniversal() {
        assertTrue(translate("  bool a;\n", "within(a, 500)").startsWith("(\\<exists> "));
        assertTrue(translate("  bool a;\n", "stable(a, 500)").startsWith("(\\<forall> "));
    }

    @Test
    void cooldownCombinesOnceAndDuring() {
        String rendered = translate("  bool a;\n", "cooldown(a, 500)");
        assertTrue(rendered.contains("\\<exists>"), rendered);
        assertTrue(rendered.contains("\\<forall>"), rendered);
    }

    // ------------------------------------------------------------------ scope

    @Test
    void scopePrevReadsThePrecedingBoundary() {
        assertEquals("((theInt (getVarVal (predEnv s) ''#x'' [])) > 0)",
                translate("  int32 x;\n", "x.scope(prev) > 0"));
    }

    /** The classic use: compare a value against what it was before the statement. */
    @Test
    void scopePreReadsTheStateBeforeTheStatement() {
        Annotation annotation = annotationOn("  int32 x;\n", "//[assert: x > x.scope(pre)]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(
                translator.translateAt(annotation, new Term.Var("s_post"), new Term.Var("s_pre")));

        assertEquals("((theInt (getVarVal s_post ''#x'' [])) > (theInt (getVarVal s_pre ''#x'' [])))",
                rendered);
    }

    // ------------------------------------------------------------------ quantifiers

    @Test
    void forallBecomesAnImplicationOverItsDomain() {
        String rendered = translate("  int32 v[4];\n", "forall(i in 0..4: v[i] > 0)");
        assertTrue(rendered.startsWith("(\\<forall> i."), rendered);
        assertTrue(rendered.contains("\\<longrightarrow>"), rendered);
        assertTrue(rendered.contains("(0 \\<le> i)"), rendered);
        assertTrue(rendered.contains("(i < 4)"), rendered);
        assertTrue(rendered.contains("AccessIndex"), rendered);
    }

    @Test
    void existsBecomesAConjunctionOverItsDomain() {
        String rendered = translate("  int32 v[4];\n", "exists(i in 0..4: v[i] > 0)");
        assertTrue(rendered.startsWith("(\\<exists> i."), rendered);
        assertFalse(rendered.contains("\\<longrightarrow>"), rendered);
    }

    // ------------------------------------------------------------------ define

    @Test
    void expandsAVariableDefinition() {
        String source = "program P {\n"
                + "  clock 100;\n"
                + "  int32 x;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    //[define: int limit = 10]\n"
                + "    //[assert: x < limit]\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}";
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        AnnotationBinder binder = new AnnotationBinder(tokens);
        NewReflexParser parser = new NewReflexParser(tokens);
        IrProgram program = new AstBuilder(binder).build(parser.program());
        Preprocessor.run(program);

        List<Annotation> annotations =
                program.getProcesses().get(0).getStates().get(0).getAnnotations();
        assertEquals(2, annotations.size());

        AnnTranslator translator = new AnnTranslator(100);
        annotations.forEach(translator::register);
        Annotation assertion = annotations.get(1);

        assertEquals("((theInt (getVarVal s ''#x'' [])) < 10)",
                renderer.render(translator.translateAt(assertion, STATE, STATE)));
    }

    // ------------------------------------------------------------------ invariants

    /** An invariant is stated at every reachable state, not just the current one. */
    @Test
    void programInvariantIsWrappedOverReachableStates() {
        Annotation annotation = annotationOn("  bool safe;\n", "//[invariant: safe]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(translator.translateInvariant(
                annotation, STATE, AnnTranslator.Scale.PROGRAM, null, null));

        assertTrue(rendered.startsWith("(\\<forall> "), rendered);
        assertTrue(rendered.contains("(toEnvP "), rendered);
        assertTrue(rendered.contains("(substate "), rendered);
        assertTrue(rendered.contains("#safe"), rendered);
    }

    /** A process invariant only claims anything while the process is running. */
    @Test
    void processInvariantIsNarrowedToWhenTheProcessIsActive() {
        Annotation annotation = annotationOn("  bool safe;\n", "//[invariant: safe]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(translator.translateInvariant(
                annotation, STATE, AnnTranslator.Scale.PROCESS, "Proc", null));

        assertTrue(rendered.contains("''stop''"), rendered);
        assertTrue(rendered.contains("''error''"), rendered);
        assertTrue(rendered.contains("''Proc''"), rendered);
    }

    /** A state invariant only claims anything while the process is in that state. */
    @Test
    void stateInvariantIsNarrowedToThatState() {
        Annotation annotation = annotationOn("  bool safe;\n", "//[invariant: safe]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(translator.translateInvariant(
                annotation, STATE, AnnTranslator.Scale.PSTATE, "Proc", "s"));

        assertTrue(rendered.contains("(getPstate"), rendered);
        assertTrue(rendered.contains("''s''"), rendered);
    }

    // ------------------------------------------------------------------ foreign

    @Test
    void passesAForeignBodyThroughUnchanged() {
        Annotation annotation =
                annotationOn("", "//[assert(isabelle): whatever the prover understands]");
        AnnTranslator translator = new AnnTranslator(100);

        assertEquals("whatever the prover understands",
                renderer.render(translator.translateAt(annotation, STATE, STATE)));
    }
}
