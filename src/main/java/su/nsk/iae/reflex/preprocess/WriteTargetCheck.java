package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reports writes to a physical variable that has nowhere to write to.
 *
 * <p>A physical variable binds a name to an address: {@code read =} says where its value
 * is sampled from and {@code write =} where an assignment to it goes. A variable declared
 * with only a {@code read =} is an input, and assigning to it cannot reach the hardware -
 * the generated condition would claim a state change the program cannot actually perform.
 *
 * <p>This is a warning rather than an error: generation carries on and emits the setter,
 * so the program is still checked, but the mismatch is worth saying out loud. A binding
 * that writes to the same address it reads is written {@code as (read = p, write = p)}.
 *
 * <p>Run over canonical IR, after name mangling has resolved every use to a declaration.
 * Normalisation copies statements - switch fall-through, {@code wait} - so the same write
 * can be reached twice; findings are collected per variable and source line, and each is
 * reported once.
 */
public final class WriteTargetCheck {

    /** A write that cannot reach the hardware. */
    public record Finding(String variable, String readPort, int line) {

        @Override
        public String toString() {
            return "line " + line + ": assignment to '" + variable + "', bound to '"
                    + readPort + "' for reading with no 'write =' destination";
        }
    }

    private final Map<String, IrDecl.PhysicalVariable> readOnly = new LinkedHashMap<>();
    private final Set<Finding> findings = new LinkedHashSet<>();

    /** The writes {@code program} makes to variables that cannot be written. */
    public static List<Finding> run(IrProgram program) {
        WriteTargetCheck check = new WriteTargetCheck();
        check.collectDeclarations(program);
        if (check.readOnly.isEmpty()) {
            return List.of();
        }
        check.walk(program);
        return new ArrayList<>(check.findings);
    }

    // ------------------------------------------------------------------ declarations

    private void collectDeclarations(IrProgram program) {
        program.getGlobalVariables().forEach(this::collectDeclaration);
        for (IrDecl.Node node : program.getNodes()) {
            node.getVariables().forEach(this::collectDeclaration);
        }
        for (IrProcess process : program.getProcesses()) {
            process.getVariables().forEach(this::collectDeclaration);
        }
    }

    private void collectDeclaration(IrDecl declaration) {
        if (declaration instanceof IrDecl.PhysicalVariable physical
                && physical.getWritePort() == null) {
            readOnly.put(physical.getName(), physical);
        }
    }

    // ------------------------------------------------------------------ walking

    private void walk(IrProgram program) {
        for (IrProcess process : program.getProcesses()) {
            for (IrState state : process.getStates()) {
                state.getStatements().forEach(this::statement);
                if (state.getTimeout() != null) {
                    statement(state.getTimeout().getBody());
                }
            }
        }
    }

    private void statement(IrStmt statement) {
        if (statement == null) {
            return;
        }
        if (statement instanceof IrStmt.Block block) {
            block.getStatements().forEach(this::statement);
        } else if (statement instanceof IrStmt.ExprStatement expr) {
            expression(expr.getExpression());
        } else if (statement instanceof IrStmt.LocalVar local) {
            expression(local.getDeclaration().getInitializer());
        } else if (statement instanceof IrStmt.If ifStmt) {
            expression(ifStmt.getCondition());
            statement(ifStmt.getThenBranch());
            statement(ifStmt.getElseBranch());
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            expression(switchStmt.getSelector());
            switchStmt.getCases().forEach(c -> c.getStatements().forEach(this::statement));
        } else if (statement instanceof IrStmt.For forStmt) {
            forStmt.getInitDeclarations().forEach(d -> expression(d.getInitializer()));
            expression(forStmt.getInitExpression());
            expression(forStmt.getCondition());
            expression(forStmt.getUpdate());
            statement(forStmt.getBody());
        }
        // Everything else - set state, process control, timers, inline C - writes no
        // variable of its own.
    }

    /**
     * A write may sit anywhere inside an expression, so the whole tree is walked rather
     * than only the top of a statement.
     */
    private void expression(IrExpr expr) {
        if (expr == null) {
            return;
        }
        if (expr instanceof IrExpr.Assign assign) {
            report(assign.getTarget());
            expression(assign.getValue());
            indices(assign.getTarget());
        } else if (expr instanceof IrExpr.IncDec incDec) {
            report(incDec.getTarget());
            indices(incDec.getTarget());
        } else if (expr instanceof IrExpr.Binary binary) {
            expression(binary.getLeft());
            expression(binary.getRight());
        } else if (expr instanceof IrExpr.Unary unary) {
            expression(unary.getOperand());
        } else if (expr instanceof IrExpr.Cast cast) {
            expression(cast.getOperand());
        } else if (expr instanceof IrExpr.Call call) {
            call.getArguments().forEach(this::expression);
        } else if (expr instanceof IrExpr.VarRef ref) {
            indices(ref);
        }
    }

    private void indices(IrExpr.VarRef ref) {
        ref.getAccesses().forEach(access -> {
            if (access instanceof IrExpr.IndexAccess index) {
                expression(index.getIndex());
            }
        });
    }

    private void report(IrExpr.VarRef target) {
        IrDecl.PhysicalVariable declaration = readOnly.get(target.getName());
        if (declaration == null) {
            return;
        }
        int line = target.getSource() != null
                ? target.getSource().getStart().getLine()
                : lineOf(declaration);
        findings.add(new Finding(
                declaration.getOriginalName(), declaration.getReadPort(), line));
    }

    private static int lineOf(IrDecl declaration) {
        return declaration.getSource() != null ? declaration.getSource().getStart().getLine() : 0;
    }
}
