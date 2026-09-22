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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /**
     * The nearest earlier boundary, asked for rather than constructed: where there is none
     * the existential has no witness, and the operator is false rather than reading a state
     * from before the scale began.
     */
    @Test
    void previouslyAsksForTheAdjacentEarlierBoundary() {
        String rendered = translate("  bool a;\n", "previously(a)");
        assertTrue(rendered.startsWith("(\\<exists> sa1."), rendered);
        // Strictly earlier, a boundary, and nothing between it and here is one.
        assertTrue(rendered.contains("(substate sa1 s) \\<and> (sa1 \\<noteq> s)"), rendered);
        assertTrue(rendered.contains("(toEnvP sa1)"), rendered);
        assertTrue(rendered.contains("(\\<not> (toEnvP sa2))"), rendered);
        assertTrue(rendered.contains("(getVarVal sa1 ''#a'' [])"), rendered);
        assertFalse(rendered.contains("predEnv"), rendered);
    }

    @Test
    void onceSearchesTheReachablePast() {
        String rendered = translate("  bool a;\n", "once(a)");
        assertTrue(rendered.startsWith("(\\<exists> "), rendered);
        assertTrue(rendered.contains("(toEnvP "), rendered);
        assertTrue(rendered.contains("(substate "), rendered);
    }

    /** next is the same question as previously, the other way round the order. */
    @Test
    void nextAsksForTheAdjacentLaterBoundary() {
        String rendered = translate("  bool a;\n", "next(a)");
        assertTrue(rendered.startsWith("(\\<exists> sa1."), rendered);
        assertTrue(rendered.contains("(substate s sa1) \\<and> (s \\<noteq> sa1)"), rendered);
        assertTrue(rendered.contains("(getVarVal sa1 ''#a'' [])"), rendered);
    }

    /** The property is asked about now; the trigger only says where to measure from. */
    @Test
    void onAsksThePropertyAtTheStateTheAnnotationSpeaksAbout() {
        String rendered = translate("  bool alarm;\n  bool siren;\n", "on(alarm, siren)");
        assertTrue(rendered.startsWith("(\\<forall> sa1."), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal sa1 ''#alarm'' []))"), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal s ''#siren'' []))"), rendered);
    }

    /** Outside a loop the timer counts cycles, so it is scaled by the clock. */
    @Test
    void timerMeasuresFromTheWindowTheTriggerOpens() {
        String rendered = translate("  bool alarm;\n", "on(alarm, timer(500))");
        assertTrue(rendered.contains("(toEnvNum sa1 s)"), rendered);
        assertTrue(rendered.contains("* 100"), rendered);
        assertTrue(rendered.contains("\\<ge> 500"), rendered);
    }

    /** within: the condition arrived, or there is still time for it. */
    @Test
    void withinIsSatisfiedByTheConditionOrByTimeRemaining() {
        String rendered = translate("  bool alarm;\n  bool siren;\n",
                "on(alarm, within(500, siren))");
        assertTrue(rendered.contains("\\<or>"), rendered);
        assertTrue(rendered.contains("(\\<exists> sa2."), rendered);
        assertTrue(rendered.contains("(substate sa1 sa2)"), rendered);
        assertTrue(rendered.contains("< 500"), rendered);
    }

    /** stable: while the window is young, the condition holds here. */
    @Test
    void stableConstrainsOnlyWhileTheWindowIsYoung() {
        String rendered = translate("  bool alarm;\n  bool siren;\n",
                "on(alarm, stable(500, siren))");
        assertTrue(rendered.contains("< 500) \\<longrightarrow>"), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal s ''#siren'' []))"), rendered);
        assertFalse(rendered.contains("(\\<forall> sa2"), rendered);
    }

    /** The measuring operators need a window, and say so rather than measuring nothing. */
    @Test
    void timerWithinAndStableAreRejectedOutsideAWindow() {
        for (String body : List.of("timer(500)", "within(500, a)", "stable(500, a)")) {
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> translate("  bool a;\n", body), body);
            assertTrue(failure.getMessage().contains("window"), failure.getMessage());
        }
    }

    /** cooldown: one boundary less than t ago where the condition held. */
    @Test
    void cooldownIsOneRecentEnoughWitness() {
        String rendered = translate("  bool a;\n", "cooldown(a, 500)");
        assertTrue(rendered.startsWith("(\\<exists> sa1."), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal sa1 ''#a'' []))"), rendered);
        assertTrue(rendered.contains("((toEnvNum sa1 s) * 100) < 500"), rendered);
        // One witness is enough: nothing is claimed of the other times it held.
        assertFalse(rendered.contains("\\<forall>"), rendered);
    }

    // ------------------------------------------- window-carrying within and stable

    /** within(psi, t, phi) carries its own window, so it needs no enclosing one. */
    @Test
    void withinWithATriggerNeedsNoEnclosingWindow() {
        String rendered = translate("  bool alarm;\n  bool siren;\n",
                "within(alarm, 500, siren)");
        assertTrue(rendered.startsWith("(\\<forall> sa1."), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal sa1 ''#alarm'' []))"), rendered);
        // The condition ends the wait, and until it does the time may not run out.
        assertTrue(rendered.contains("''#siren''"), rendered);
        assertTrue(rendered.contains("\\<ge> 500"), rendered);
    }

    /** stable(psi, t, phi) asks what holds now, not what has held throughout. */
    @Test
    void stableWithATriggerGoesThroughCooldown() {
        String rendered = translate("  bool alarm;\n  bool siren;\n",
                "stable(alarm, 500, siren)");
        assertTrue(rendered.startsWith("((\\<exists> sa1."), rendered);
        assertTrue(rendered.contains("\\<longrightarrow> (theBool (getVarVal s ''#siren'' []))"),
                rendered);
    }

    // ------------------------------------------------------------------ scope

    /**
     * A scope reads an expression at another state, so it needs that state as a term:
     * the same condition {@code previously} quantifies over, resolved by choice instead.
     */
    @Test
    void scopePrevReadsTheStateTheChoicePicksOut() {
        String rendered = translate("  int32 x;\n", "x.scope(prev) > 0");
        assertTrue(rendered.startsWith("((theInt (getVarVal (SOME sa1."), rendered);
        assertTrue(rendered.contains("(toEnvP sa1)"), rendered);
        assertTrue(rendered.endsWith("''#x'' [])) > 0)"), rendered);
    }

    @Test
    void scopePastPicksTheMostRecentStateWhereTheConditionHeld() {
        String rendered = translate("  int32 x;\n  bool a;\n", "x.scope(past(a)) > 0");
        assertTrue(rendered.contains("(SOME sa1."), rendered);
        assertTrue(rendered.contains("(theBool (getVarVal sa1 ''#a'' []))"), rendered);
        // Nothing later satisfies it, which is what makes the choice the most recent one.
        assertTrue(rendered.contains("(\\<not> (theBool (getVarVal sa2 ''#a'' [])))"), rendered);
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

    /**
     * A loop invariant reads history from the state the loop was entered at, so what
     * happened before the loop began cannot satisfy an operator inside it - nor can an
     * iteration of an earlier run of the same loop.
     */
    @Test
    void aLoopInvariantReadsHistoryOnlyFromWhereTheRunBegan() {
        Annotation annotation = annotationOn("  bool a;\n", "//[invariant: once(a)]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(translator.translateLoopInvariant(
                annotation, new Term.Var("t0"), STATE));

        assertTrue(rendered.contains("(substate t0 sa"), rendered);
        assertTrue(rendered.contains("''#a''"), rendered);
        // The state the loop was entered at counts as a boundary of the run: an iteration
        // ends in a toEnv, but the entry is mid-cycle and carries no marker of its own.
        assertTrue(rendered.contains("(sa1 = t0) \\<or> (toEnvP sa1)"), rendered);
    }

    /** Outside a loop history has no lower bound, so nothing bounds the search below. */
    @Test
    void aProgramInvariantSearchesHistoryWithoutALowerBound() {
        Annotation annotation = annotationOn("  bool a;\n", "//[invariant: once(a)]");
        AnnTranslator translator = new AnnTranslator(100);
        String rendered = renderer.render(translator.translateInvariant(
                annotation, STATE, AnnTranslator.Scale.PROGRAM, null, null));

        assertFalse(rendered.contains("substate t0"), rendered);
    }

    /**
     * An invariant of next(true) asks for a boundary beyond the last one. It is caught
     * here rather than left to fail at the prover, where it would look like a property
     * that merely did not go through.
     */
    @Test
    void anInvariantOfBareNextIsRejected() {
        Annotation annotation = annotationOn("", "//[invariant: next(true)]");
        AnnTranslator translator = new AnnTranslator(100);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> translator.translateInvariant(
                        annotation, STATE, AnnTranslator.Scale.PROGRAM, null, null));
        assertTrue(failure.getMessage().contains("next(true)"), failure.getMessage());
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
