package su.nsk.iae.reflex.cfg;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The exported graph is for a person to read, so a node has to say what it is.
 *
 * <p>Every construct used to collapse to an anonymous "join", which made the picture a mesh
 * of identical circles; and a loop's body never appeared at all, since it hangs off the loop
 * node rather than being one of its successors.
 */
class GraphExportTest {

    private static Cfg graphOf(String source) {
        return build(CharStreams.fromString(source), "the program");
    }

    private static Cfg graphOfFile(String name) throws Exception {
        return build(CharStreams.fromPath(Path.of("src/test/resources/programs-new", name)), name);
    }

    private static Cfg build(CharStream source, String what) {
        BufferedTokenStream tokens = new CommonTokenStream(new NewReflexLexer(source));
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext context = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), what + " should parse");

        IrProgram program = new AstBuilder(new AnnotationBinder(tokens)).build(context);
        Preprocessor.run(program);
        return new CfgBuilder(program).build();
    }

    @Test
    void namesTheConstructEachNodeBelongsTo() {
        String dot = graphOf("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n"
                + "      if (a > 0) { a = 1; } else { a = 2; }\n"
                + "      switch (a) { case 1: { a = 3; } default: { a = 4; } }\n"
                + "    }\n"
                + "  }\n"
                + "}").toDot();

        assertTrue(dot.contains("label=\"process Proc\""), dot);
        assertTrue(dot.contains("label=\"process Proc end\""), dot);
        assertTrue(dot.contains("label=\"state s\""), dot);
        assertTrue(dot.contains("label=\"if\""), dot);
        assertTrue(dot.contains("label=\"if end\""), dot);
        assertTrue(dot.contains("label=\"switch\""), dot);
        assertTrue(dot.contains("label=\"switch end\""), dot);
        assertTrue(dot.contains("label=\"Proc in s\""), dot);

        assertFalse(dot.contains("label=\"join\""),
                "no node should be an anonymous join:\n" + dot);
    }

    /** A guard is a decision and an assignment is a step; they should not look alike. */
    @Test
    void shapesNodesByWhatTheyDo() {
        String dot = graphOf("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N { state s { if (a > 0) { a = 1; } } }\n"
                + "}").toDot();

        assertTrue(dot.contains("shape=diamond"), "a guard is a decision:\n" + dot);
        assertTrue(dot.contains("shape=box"), "an assignment is a step:\n" + dot);
        assertTrue(dot.contains("shape=circle"), "entry and exit are endpoints:\n" + dot);
    }

    /**
     * The graph has no cycle - a loop body is proved separately - but it should read as a
     * loop: the body drawn apart, entered, and returning.
     */
    @Test
    void drawsALoopAsALoop() throws Exception {
        Cfg graph = graphOfFile("loopSum.rcs");
        String dot = graph.toDot();

        assertTrue(dot.contains("subgraph cluster_"), "the body is a box of its own:\n" + dot);
        assertTrue(dot.contains("label=\"one iteration of loopInv0\""), dot);
        assertTrue(dot.contains("[label=\"iterate\""), "an edge into the body:\n" + dot);
        assertTrue(dot.contains("[label=\"repeat\""), "and one back out of it:\n" + dot);
        assertTrue(dot.contains("[label=\"loop done\"]"), "and one past the loop:\n" + dot);

        // The body's own statements are drawn, which they never were before.
        assertTrue(dot.contains("#total := (#total + 2)"), dot);
        assertTrue(dot.contains("label=\"end of iteration\""),
                "the body ends the iteration, not the program:\n" + dot);
    }

    /**
     * The body is part of the program, so something unsupported inside one has to be found
     * before generation starts rather than when the body is enumerated.
     */
    @Test
    void reachesIntoLoopBodiesWhenLookingForUnsupportedConstructs() {
        Cfg graph = graphOf("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  int32 a;\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n"
                + "      for (int32 i = 0; i < 3; i++) {\n"
                + "        $ a = compute()\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}");

        assertEquals(1, graph.unsupportedNodes().size(),
                "the inline C inside the loop body should be reported");
        assertEquals("inline C", graph.unsupportedNodes().get(0).getConstruct());
    }
}
