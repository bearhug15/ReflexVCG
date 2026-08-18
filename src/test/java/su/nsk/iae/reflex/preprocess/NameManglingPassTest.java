package su.nsk.iae.reflex.preprocess;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameManglingPassTest {

    private static IrProgram mangled(String source) {
        BufferedTokenStream tokens = AnnotationBinder.tokenize(source);
        NewReflexParser parser = new NewReflexParser(tokens);
        NewReflexParser.ProgramContext ctx = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), "source should parse cleanly");
        IrProgram program = new AstBuilder().build(ctx);
        new NameManglingPass().run(program);
        return program;
    }

    /** Every variable name appearing anywhere in the program's expressions. */
    private static List<String> variableUses(IrProgram program) {
        List<String> names = new ArrayList<>();
        program.getProcesses().forEach(process ->
                process.getStates().forEach(state ->
                        state.getStatements().forEach(stmt -> collectUses(stmt, names))));
        return names;
    }

    private static void collectUses(IrStmt stmt, List<String> names) {
        if (stmt instanceof IrStmt.ExprStatement expr) {
            collectUses(expr.getExpression(), names);
        } else if (stmt instanceof IrStmt.Block block) {
            block.getStatements().forEach(s -> collectUses(s, names));
        } else if (stmt instanceof IrStmt.If ifStmt) {
            collectUses(ifStmt.getCondition(), names);
            collectUses(ifStmt.getThenBranch(), names);
            if (ifStmt.getElseBranch() != null) {
                collectUses(ifStmt.getElseBranch(), names);
            }
        } else if (stmt instanceof IrStmt.LocalVar local) {
            names.add(local.getDeclaration().getName());
            if (local.getDeclaration().getInitializer() != null) {
                collectUses(local.getDeclaration().getInitializer(), names);
            }
        }
    }

    private static void collectUses(IrExpr expr, List<String> names) {
        if (expr instanceof IrExpr.VarRef ref) {
            names.add(ref.getName());
            ref.getAccesses().forEach(a -> {
                if (a instanceof IrExpr.IndexAccess index) {
                    collectUses(index.getIndex(), names);
                }
            });
        } else if (expr instanceof IrExpr.Binary binary) {
            collectUses(binary.getLeft(), names);
            collectUses(binary.getRight(), names);
        } else if (expr instanceof IrExpr.Unary unary) {
            collectUses(unary.getOperand(), names);
        } else if (expr instanceof IrExpr.Assign assign) {
            collectUses(assign.getTarget(), names);
            collectUses(assign.getValue(), names);
        } else if (expr instanceof IrExpr.Cast cast) {
            collectUses(cast.getOperand(), names);
        }
    }

    // ------------------------------------------------------------------ tests

    @Test
    void manglesProgramLevelVariables() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  int32 counter;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { counter = 1; } }\n"
                + "}");

        IrDecl.Variable counter = assertInstanceOf(IrDecl.Variable.class, program.getGlobalVariables().get(0));
        assertEquals("#counter", counter.getName(), "a program-level name has no enclosing scope");
        assertEquals(List.of("#counter"), variableUses(program), "uses follow the declaration");
    }

    /**
     * The point of the pass: the same source name in two processes becomes two names, so
     * the (process, variable) lookup table VariableMapper kept is no longer needed.
     */
    @Test
    void givesSameNamedProcessVariablesDistinctNames() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process A :: node N { int32 v; state s { v = 1; } }\n"
                + "  process B :: node N { int32 v; state s { v = 2; } }\n"
                + "}");

        String inA = program.findProcess("A").getVariables().get(0).getName();
        String inB = program.findProcess("B").getVariables().get(0).getName();

        assertEquals("N#A#v", inA);
        assertEquals("N#B#v", inB);
        assertNotEquals(inA, inB);
        assertEquals(List.of("N#A#v", "N#B#v"), variableUses(program));
    }

    @Test
    void manglesStateAndBlockScopes() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { int32 local = 0; local = 1; }\n"
                + "  }\n"
                + "}");

        // Scopes read outermost first: node N, process Proc, state s.
        assertEquals(List.of("N#Proc#s#local", "N#Proc#s#local"), variableUses(program));
    }

    /** An inner declaration shadows an outer one, and the outer name returns after. */
    @Test
    void innerDeclarationShadowsOuterOne() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  int32 v;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s {\n"
                + "      v = 1;\n"
                + "      { int32 v = 0; v = 2; }\n"
                + "      v = 3;\n"
                + "    }\n"
                + "  }\n"
                + "}");

        List<String> uses = variableUses(program);
        assertEquals("#v", uses.get(0), "before the inner declaration, the global is in scope");
        assertTrue(uses.get(1).contains("block"), "the inner declaration is scoped to its block");
        assertEquals(uses.get(1), uses.get(2), "the inner use resolves to the inner declaration");
        assertEquals("#v", uses.get(3), "after the block the global is in scope again");
    }

    @Test
    void nodeVariablesAreVisibleToBoundProcesses() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; int32 nodeVar; const int32 K = 1; }\n"
                + "  process Proc :: node N { state s { nodeVar = K; } }\n"
                + "}");

        IrDecl.Node node = program.findNode("N");
        String declared = node.getVariables().get(0).getName();
        assertEquals("N#nodeVar", declared);

        List<String> uses = variableUses(program);
        assertEquals(declared, uses.get(0), "the process sees the node's variable by its mangled name");
        assertEquals("N#K", uses.get(1));
    }

    /** An imported name refers to the very same variable, so it gets the owner's name. */
    @Test
    void sharedImportsResolveToTheProvidingProcessName() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Consumer :: node N {\n"
                + "    shared v from process Producer;\n"
                + "    state s { v = 1; }\n"
                + "  }\n"
                + "  process Producer :: node N { int32 v; state s { ; } }\n"
                + "}");

        String owner = program.findProcess("Producer").getVariables().get(0).getName();
        assertEquals("N#Producer#v", owner);
        assertEquals(List.of(owner), variableUses(program),
                "the consumer's use points at the producer's variable, even though "
                        + "Producer is declared after Consumer");
    }

    @Test
    void rejectsSharedImportsThatCannotBeResolved() {
        assertThrows(IllegalStateException.class, () -> mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Consumer :: node N {\n"
                + "    shared v from process Missing;\n"
                + "    state s { v = 1; }\n"
                + "  }\n"
                + "}"));

        assertThrows(IllegalStateException.class, () -> mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Consumer :: node N {\n"
                + "    shared absent from process Producer;\n"
                + "    state s { absent = 1; }\n"
                + "  }\n"
                + "  process Producer :: node N { int32 other; state s { ; } }\n"
                + "}"));
    }

    @Test
    void physicalVariablesAreNamedAfterTheirPort() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  input inp 0x00 0x00 24;\n"
                + "  bool flag as (read = inp, bit = 3);\n"
                + "  bool whole as (read = inp);\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { flag = true; whole = false; } }\n"
                + "}");

        assertEquals("inp_3", program.getGlobalVariables().get(0).getName());
        assertEquals("inp", program.getGlobalVariables().get(1).getName());
        assertEquals(List.of("inp_3", "inp"), variableUses(program));
    }

    /**
     * A direct binding re-reads the hardware, so each read is a distinct value and gets
     * its own name; writes do not advance the counter.
     */
    @Test
    void everyReadOfADirectBindingGetsItsOwnName() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  input inp 0x00 0x00 24;\n"
                + "  direct bool sensor as (read = inp, bit = 1);\n"
                + "  int32 a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { a = sensor; a = sensor; sensor = true; }\n"
                + "  }\n"
                + "}");

        List<String> uses = variableUses(program);
        // a = sensor; a = sensor; sensor = true;
        assertEquals("inp_1.0", uses.get(1), "first read");
        assertEquals("inp_1.1", uses.get(3), "second read sees a possibly different value");
        assertEquals("inp_1", uses.get(4), "a write is not a re-read, so it is not counted");

        assertEquals(Boolean.TRUE, program.getGlobalVariables().get(0) instanceof IrDecl.PhysicalVariable);
    }

    @Test
    void indirectBindingsKeepOneNameAcrossReads() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  input inp 0x00 0x00 24;\n"
                + "  indirect bool sampled as (read = inp, bit = 2);\n"
                + "  int32 a;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N { state s { a = sampled; a = sampled; } }\n"
                + "}");

        List<String> uses = variableUses(program);
        assertEquals("inp_2", uses.get(1));
        assertEquals("inp_2", uses.get(3), "an indirect binding is sampled once, so reads agree");
    }

    @Test
    void manglesTimeoutReferencesToConstants() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  const int32 Delay = 5;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state s { ; timeout Delay { stop; } }\n"
                + "  }\n"
                + "}");

        assertEquals("#Delay", program.getConstants().get(0).getName());
        assertEquals("#Delay",
                program.getProcesses().get(0).getStates().get(0).getTimeout().getDuration().getText(),
                "a timeout naming a constant follows it through mangling");
    }

    /** Process and state names live in their own namespaces and must not be rewritten. */
    @Test
    void leavesProcessAndStateNamesAlone() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  node N { clock 100; }\n"
                + "  process Proc :: node N {\n"
                + "    state first { start Other; set state second; }\n"
                + "    state second { ; }\n"
                + "  }\n"
                + "  process Other :: node N { state only { ; } }\n"
                + "}");

        assertEquals("Proc", program.getProcesses().get(0).getName());
        assertEquals("first", program.getProcesses().get(0).getStates().get(0).getName());

        List<IrStmt> statements = program.getProcesses().get(0).getStates().get(0).getStatements();
        assertEquals("Other",
                assertInstanceOf(IrStmt.ProcessControl.class, statements.get(0)).getProcess());
        assertEquals("second",
                assertInstanceOf(IrStmt.SetState.class, statements.get(1)).getState());
    }

    /** The whole point: after the pass, no name denotes two different variables. */
    @Test
    void producesGloballyUniqueDeclarationNames() {
        IrProgram program = mangled("program P {\n"
                + "  clock 100;\n"
                + "  int32 v;\n"
                + "  node N { clock 100; int32 v; }\n"
                + "  process A :: node N { int32 v; state s { int32 v = 0; v = 1; } }\n"
                + "  process B :: node N { int32 v; state s { int32 v = 0; v = 2; } }\n"
                + "}");

        List<String> declared = new ArrayList<>();
        declared.add(program.getGlobalVariables().get(0).getName());
        declared.add(program.findNode("N").getVariables().get(0).getName());
        program.getProcesses().forEach(process -> {
            process.getVariables().forEach(v -> declared.add(v.getName()));
            process.getStates().forEach(state -> state.getStatements().forEach(stmt -> {
                if (stmt instanceof IrStmt.LocalVar local) {
                    declared.add(local.getDeclaration().getName());
                }
            }));
        });

        Set<String> unique = new HashSet<>(declared);
        assertEquals(declared.size(), unique.size(),
                "every declaration should have a distinct name, but got " + declared);
    }
}
