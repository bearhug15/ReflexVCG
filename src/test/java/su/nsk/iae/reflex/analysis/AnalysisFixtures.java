package su.nsk.iae.reflex.analysis;

import org.antlr.v4.runtime.BufferedTokenStream;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgBuilder;
import su.nsk.iae.reflex.cfg.PathEnumerator;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.vc.VcStatement;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Small programs and questions about the conditions generated for them, for the analysis tests. */
final class AnalysisFixtures {

    private AnalysisFixtures() {
    }

    /**
     * A program with the given processes, a clock of 100, three inputs {@code x}, {@code y},
     * {@code z}, a variable {@code a} and a counter {@code i}, and any extra declarations.
     */
    static IrProgram programWith(String declarations, String... processes) {
        List<String> lines = new ArrayList<>(List.of(
                "program P {",
                "  clock 100;",
                "  import IO { register inp }",
                "  node N { clock 100; }",
                "  bool x as (read = inp, bit = 0);",
                "  bool y as (read = inp, bit = 1);",
                "  bool z as (read = inp, bit = 2);",
                "  bool a;",
                "  int32 i;",
                declarations));
        for (String process : processes) {
            lines.add("  " + process);
        }
        lines.add("}");
        return parse(String.join("\n", lines));
    }

    static IrProgram program(String... processes) {
        return programWith("", processes);
    }

    static IrProgram parse(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse:\n" + source);
        IrProgram program = new AstBuilder().build(ctx);
        Preprocessor.run(program);
        return program;
    }

    static List<VerificationCondition> conditions(IrProgram program, boolean analyse) {
        AttributePreparation preparation = new AttributePreparation(program);
        preparation.run();
        Cfg cfg = new CfgBuilder(program, preparation).build();
        return new PathEnumerator(cfg, analyse ? new StaticAnalysis(program) : null).enumerate();
    }

    /** How many conditions, with or without the analysis, satisfy every predicate. */
    @SafeVarargs
    static long count(IrProgram program, boolean analyse, Predicate<VerificationCondition>... all) {
        return conditions(program, analyse).stream()
                .filter(condition -> {
                    for (Predicate<VerificationCondition> each : all) {
                        if (!each.test(condition)) {
                            return false;
                        }
                    }
                    return true;
                })
                .count();
    }

    /** The condition dispatches {@code process} in {@code state}. */
    static Predicate<VerificationCondition> inState(String process, String state) {
        return condition -> condition.getStatements().stream().anyMatch(s ->
                s instanceof VcStatement.ProcessInState p && p.process().equals(process) && p.pstate().equals(state));
    }

    /** The condition passes a guard asserting {@code process P in state <activity>}. */
    static Predicate<VerificationCondition> asserts(String process, Term.Activity activity) {
        Term wanted = new Term.ProcessActivity(process, activity);
        return condition -> condition.getStatements().stream().anyMatch(s ->
                s instanceof VcStatement.Condition c && Term.assertedBy(c.expr()).contains(wanted));
    }

    /** The condition takes the branch of {@code process}'s timeout where it elapsed. */
    static Predicate<VerificationCondition> timeoutElapsed(String process) {
        return condition -> condition.getStatements().stream().anyMatch(s ->
                s instanceof VcStatement.TimeoutCheck t && t.process().equals(process) && t.exceeded());
    }

    /** The condition moves {@code process} to {@code state}. */
    static Predicate<VerificationCondition> moves(String process, String state) {
        return condition -> condition.getStatements().stream().anyMatch(s ->
                s instanceof VcStatement.SetProcessState set && set.process().equals(process)
                        && set.pstate().equals(state));
    }

    static Predicate<VerificationCondition> not(Predicate<VerificationCondition> predicate) {
        return predicate.negate();
    }

    /** The {@code index}-th statement of a state. */
    static IrStmt statement(IrProgram program, String process, String state, int index) {
        return program.findProcess(process).findState(state).getStatements().get(index);
    }
}
