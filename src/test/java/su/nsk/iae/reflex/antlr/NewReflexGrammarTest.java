package su.nsk.iae.reflex.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Grammar-level regression tests for NewReflex.g4. Each case covers a defect that made
 * the grammar unusable before the migration; see the comments on each test.
 */
class NewReflexGrammarTest {

    /** Collects syntax errors instead of printing them to stderr and carrying on. */
    private static final class ErrorCollector extends BaseErrorListener {
        final List<String> errors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                int charPositionInLine, String msg, RecognitionException e) {
            errors.add("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }

    private static List<String> parseErrors(String source) {
        ErrorCollector lexErrors = new ErrorCollector();
        ErrorCollector parseErrors = new ErrorCollector();

        NewReflexLexer lexer = new NewReflexLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexErrors);

        NewReflexParser parser = new NewReflexParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(parseErrors);
        parser.program();

        List<String> all = new ArrayList<>(lexErrors.errors);
        all.addAll(parseErrors.errors);
        return all;
    }

    private static void assertParses(String source) {
        List<String> errors = parseErrors(source);
        assertTrue(errors.isEmpty(), () -> "expected a clean parse but got:\n  " + String.join("\n  ", errors));
    }

    /** Wraps statements in the smallest complete program that contains them. */
    private static String inState(String statements) {
        return "program P {\n"
             + "  clock 100;\n"
             + "  node N { clock 100; }\n"
             + "  process Proc :: node N {\n"
             + "    state s {\n"
             + statements + "\n"
             + "    }\n"
             + "  }\n"
             + "}";
    }

    @Test
    void parsesMinimalProgram() {
        assertParses(inState("      ;"));
    }

    /**
     * A signed FLOAT/INTEGER token swallows the operator, so the addition cannot parse.
     * Signs now belong to the parser rules `integer` / `floatVal`.
     */
    @Test
    void parsesAdditionOfNumericLiterals() {
        assertParses(inState("      int32 a = 0; a = a + 1; a = a - 1;"));
        assertParses(inState("      double d = 0.0; d = d + 1.0; d = d - 1.0;"));
    }

    /** Negative literals must still parse where a value is expected. */
    @Test
    void parsesNegativeLiterals() {
        assertParses(inState("      int32 a = -1; double d = -1.5;"));
    }

    /**
     * 'stop' and 'error' are literals in stopProcStat/errorProcStat. As a STATE_QUAL
     * lexer token they were unreachable, because implicit literal tokens take priority.
     */
    @Test
    void parsesEveryProcessStateQualifier() {
        for (String qual : List.of("active", "inactive", "stop", "error")) {
            assertParses(inState("      if (process Proc in state " + qual + ") { ; }"));
        }
    }

    /** 'stop'/'error' must still work as statements, alongside the qualifier use above. */
    @Test
    void parsesProcessControlStatements() {
        assertParses(inState("      start Proc; stop; error; restart; reset timer;"));
        assertParses(inState("      stop Proc; error Proc;"));
    }

    /** The enum rule required exactly two members. */
    @Test
    void parsesEnumsOfAnyArity() {
        String prog = "program P { clock 100; enum E%s { %s } }";
        assertParses(String.format(prog, "1", "A"));
        assertParses(String.format(prog, "2", "A, B"));
        assertParses(String.format(prog, "3", "A, B, C"));
        assertParses(String.format(prog, "4", "A = 1, B = 2, C = 3, D = 4"));
    }

    /** functionCall required at least one argument, so `f()` was unparseable. */
    @Test
    void parsesCallsWithAndWithoutArguments() {
        assertParses(inState("      f(); g(1); h(1, 2);"));
    }

    /**
     * CSTRING matched any run of characters not containing '$', ';' or a newline,
     * anywhere in the input, which swallowed ordinary program text. It is now a
     * self-delimiting CCODE token that must start at '$'.
     */
    @Test
    void parsesInlineCCodeWithoutSwallowingTheProgram() {
        assertParses(inState("      $ printf(\"hi\")\n      int32 after = 1;"));
    }

    /** Comments are on the hidden channel, so they are legal anywhere. */
    @Test
    void acceptsCommentsInArbitraryPositions() {
        assertParses("// leading\n"
                + "program P { // after brace\n"
                + "  /* block */ clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { // trailing\n"
                + "    /* between */\n"
                + "    state s { int32 a = /* inside an expression */ 1 + 2; }\n"
                + "    // between states\n"
                + "    state t { ; }\n"
                + "  }\n"
                + "} // trailing\n");
    }

    /** Reflex-AL annotations ride in comments, so they must not disturb the parse. */
    @Test
    void acceptsReflexAlAnnotationsAsComments() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  //[invariant: forall(i in 0..10: a[i] > 0)]\n"
                + "  process Proc :: node N {\n"
                + "    /*[assume: x > 0]*/\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");
    }

    /** 'ms' was consumed as MINUTE plus a stray 's'. */
    @Test
    void parsesTimeLiterals() {
        for (String t : List.of("0t5ms", "0t30s", "0t5m", "0t1h", "0t2d", "0t1h30m", "0t1h30m15s")) {
            assertParses("program P { clock " + t + "; }");
        }
    }

    /**
     * In the legacy grammar HEX_SEQ preceded ID, so any identifier spelled only with
     * a-f lexed as a number. The helper rules are fragments now.
     */
    @Test
    void parsesIdentifiersThatLookLikeHexDigits() {
        assertParses(inState("      int32 abc = 0; int32 dead = 0; abc = dead;"));
    }

    @Test
    void parsesStructAndArrayAccess() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { a.x = 1; b[0] = 2; c[i].field = 3; d.e[1].f = 4; }\n"
                + "  }\n"
                + "}");
    }

    @Test
    void parsesGuardingStatements() {
        assertParses(inState("      wait (x > 0);"));
        assertParses(inState("      slice;"));
        assertParses(inState("      wait (x > 0) on timeout 0t5s { y = 1; };"));
    }

    @Test
    void parsesForLoop() {
        assertParses(inState("      for (int32 i = 0; i < 10; i++) { sum = sum + i; }"));
        assertParses(inState("      for (i = 0; i < 10; i++) ;"));
    }

    @Test
    void parsesSwitchWithFallthroughAndDefault() {
        assertParses(inState("      switch (x) { case 1: { y = 1; break; } case 2: y = 2; default: { y = 3; } }"));
    }

    @Test
    void parsesPhysicalVariableBindings() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  input inp 0x00 0x00 24;\n"
                + "  direct bool a as (read = inp);\n"
                + "  indirect bool b as (read = inp, write = outp);\n"
                + "  bool c as (read = inp, write = outp, config = cfg, bit = 3);\n"
                + "  bool d as (read = inp, bit = namedBit);\n"
                + "}");
    }

    @Test
    void parsesImportBlocksAndNodes() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  import Lib { vector v register r bit b }\n"
                + "  node N { clock 100; const int32 K = 1; int32 shared_var; }\n"
                + "  process Proc :: node N {\n"
                + "    shared x, y from process Other;\n"
                + "    state s { ; }\n"
                + "  }\n"
                + "}");
    }

    @Test
    void parsesTimeoutFunction() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n"
                + "      x = 1;\n"
                + "      timeout 0t5s { stop; }\n"
                + "    }\n"
                + "    state looping looped {\n"
                + "      timeout (0t1s) ;\n"
                + "    }\n"
                + "  }\n"
                + "}");
    }

    /** A struct or enum name is usable as a type, so struct declarations mean something. */
    @Test
    void parsesUserDefinedTypedVariables() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  enum Color { Red, Green, Blue }\n"
                + "  Point origin;\n"
                + "  Color c;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { Point local; local.x = 1; } }\n"
                + "}");
    }

    @Test
    void parsesArrayDeclarations() {
        assertParses(inState("      int32 buf[8];"));
        assertParses(inState("      int32 grid[4][4];"));
        assertParses(inState("      int32 buf[8]; buf[0] = 1;"));
    }

    /** `int32 a[] = {1,2,3}` takes its extent from the initialiser. */
    @Test
    void parsesArrayWithSizeDerivedFromInitializer() {
        assertParses(inState("      int32 a[] = {1, 2, 3};"));
        assertParses(inState("      int32 g[][2] = {{1, 2}, {3, 4}};"));
        assertParses(inState("      int32 a[] = {1, 2, 3,};"));
    }

    /** Aggregate initialisers are partial: omitted members keep their defaults. */
    @Test
    void parsesPartialInitialization() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point positional = {1};\n"
                + "  Point designated = {.y = 5};\n"
                + "  Point both = {.x = 1, .y = 2};\n"
                + "  Point empty = {};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { ; } }\n"
                + "}");
        assertParses(inState("      int32 v[4] = {1, 2};"));
        assertParses(inState("      int32 v[4] = {[2] = 7};"));
    }

    @Test
    void parsesArraysOfStructs() {
        assertParses("program P {\n"
                + "  clock 100;\n"
                + "  struct Point { int32 x; int32 y; }\n"
                + "  Point path[2] = {{1, 2}, {.y = 3}};\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { path[0].x = 9; } }\n"
                + "}");
    }

    /** Casts to builtin types must keep working now that `type` can be a bare ID. */
    @Test
    void parsesCastsToBuiltinTypes() {
        assertParses(inState("      int32 a = 0; a = (int32) b; a = (uint8) (b + 1);"));
    }

    /** With casts restricted to builtins, `(x) + 1` is unambiguously an expression. */
    @Test
    void parsesParenthesisedExpressionFollowedByOperator() {
        assertParses(inState("      a = (x) + 1; a = (x + y) * 2;"));
    }

    /** A genuinely malformed program must still be reported, not silently accepted. */
    @Test
    void reportsSyntaxErrorsForMalformedInput() {
        assertTrue(parseErrors("program P { clock ; }").size() > 0,
                "a missing clock value should be a syntax error");
        assertEquals(true, parseErrors("program { clock 100; }").size() > 0,
                "a missing program name should be a syntax error");
    }
}
