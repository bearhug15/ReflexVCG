package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.analysis.AttributePreparation;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the control-flow graph of one execution cycle.
 *
 * <p>A Reflex cycle runs every process once, in declaration order, each in whichever
 * state it currently occupies, and then yields to the environment. The graph mirrors
 * that: it is a sequence of per-process fragments, where each fragment branches over the
 * states the process could be in, and every path through it is one possible cycle.
 *
 * <p>The graph is built from canonical IR, so it never has to deal with {@code wait},
 * {@code slice}, switch fall-through, or implicit conversions - preprocessing has already
 * removed them.
 */
public final class CfgBuilder {

    /** A subgraph with a single way in and a single way out. */
    private record Fragment(CfgNode entry, CfgNode exit) {
    }

    private final IrProgram program;
    private final AttributePreparation attributes;

    public CfgBuilder(IrProgram program) {
        this(program, null);
    }

    /**
     * @param attributes prepared attributes to attach to the nodes that change
     *                   something, so traversal can accumulate them; null to build a
     *                   graph without them
     */
    public CfgBuilder(IrProgram program, AttributePreparation attributes) {
        this.program = program;
        this.attributes = attributes;
    }

    public Cfg build() {
        CfgNode.Entry entry = new CfgNode.Entry();
        CfgNode current = entry;

        for (IrProcess process : program.getProcesses()) {
            Fragment fragment = buildProcess(process);
            current.addSuccessor(fragment.entry());
            current = fragment.exit();
        }

        CfgNode.ToEnv toEnv = new CfgNode.ToEnv();
        current.addSuccessor(toEnv);
        CfgNode.Exit exit = new CfgNode.Exit();
        toEnv.addSuccessor(exit);

        return new Cfg(entry, exit, program);
    }

    /**
     * A process contributes a branch over its states: on any given cycle it is in exactly
     * one of them, so each is an alternative path.
     *
     * <p>The stop and error states are included even though the program does not declare
     * them - a stopped process still takes part in the cycle, doing nothing.
     */
    private Fragment buildProcess(IrProcess process) {
        CfgNode.Join entry = new CfgNode.Join();
        CfgNode.Join exit = new CfgNode.Join();

        for (IrState state : process.getStates()) {
            CfgNode.InState inState = new CfgNode.InState(process.getName(), state.getName());
            entry.addSuccessor(inState);

            Fragment body = buildState(process, state);
            inState.addSuccessor(body.entry());
            body.exit().addSuccessor(exit);
        }

        for (String inactive : List.of("stop", "error")) {
            CfgNode.InState inState = new CfgNode.InState(process.getName(), inactive);
            entry.addSuccessor(inState);
            inState.addSuccessor(exit);
        }

        return new Fragment(entry, exit);
    }

    private Fragment buildState(IrProcess process, IrState state) {
        CfgNode.Join entry = new CfgNode.Join();
        CfgNode current = entry;

        for (IrStmt statement : state.getStatements()) {
            Fragment fragment = buildStatement(process, statement);
            current.addSuccessor(fragment.entry());
            current = fragment.exit();
        }

        if (state.getTimeout() != null) {
            current = buildTimeout(process, state, current);
        }
        return new Fragment(entry, current);
    }

    /**
     * A timeout splits the path: either the time spent in the state has reached the
     * bound, and the timeout body runs, or it has not and the cycle continues.
     */
    private CfgNode buildTimeout(IrProcess process, IrState state, CfgNode current) {
        CfgNode.Join join = new CfgNode.Join();

        CfgNode.TimeoutGuard reached =
                new CfgNode.TimeoutGuard(process.getName(), state.getTimeout().getDuration(), true);
        current.addSuccessor(reached);
        Fragment body = buildStatement(process, state.getTimeout().getBody());
        reached.addSuccessor(body.entry());
        body.exit().addSuccessor(join);

        CfgNode.TimeoutGuard notReached =
                new CfgNode.TimeoutGuard(process.getName(), state.getTimeout().getDuration(), false);
        current.addSuccessor(notReached);
        notReached.addSuccessor(join);

        return join;
    }

    // ------------------------------------------------------------------ statements

    private Fragment buildStatement(IrProcess process, IrStmt statement) {
        if (statement == null || statement instanceof IrStmt.Empty) {
            CfgNode.Join node = new CfgNode.Join();
            return new Fragment(node, node);
        }
        if (statement instanceof IrStmt.Block block) {
            CfgNode.Join entry = new CfgNode.Join();
            CfgNode current = entry;
            for (IrStmt inner : block.getStatements()) {
                Fragment fragment = buildStatement(process, inner);
                current.addSuccessor(fragment.entry());
                current = fragment.exit();
            }
            return new Fragment(entry, current);
        }
        if (statement instanceof IrStmt.ExprStatement expr) {
            return buildExpressionStatement(expr.getExpression());
        }
        if (statement instanceof IrStmt.LocalVar local) {
            return buildLocalDeclaration(local.getDeclaration());
        }
        if (statement instanceof IrStmt.If ifStmt) {
            return buildIf(process, ifStmt);
        }
        if (statement instanceof IrStmt.Switch switchStmt) {
            return buildSwitch(process, switchStmt);
        }
        if (statement instanceof IrStmt.SetState setState) {
            return carrying(statement, effect(
                    new CfgNode.SetState(process.getName(), setState.getState()),
                    new CfgNode.ResetTimer(process.getName())));
        }
        if (statement instanceof IrStmt.ResetTimer) {
            return carrying(statement, effect(new CfgNode.ResetTimer(process.getName())));
        }
        if (statement instanceof IrStmt.ProcessControl control) {
            return carrying(statement, buildProcessControl(process, control));
        }
        if (statement instanceof IrStmt.For forStmt) {
            return effect(new CfgNode.Unsupported("for",
                    "loops are not supported by verification condition generation"));
        }
        if (statement instanceof IrStmt.CCode ccode) {
            return effect(new CfgNode.Unsupported("inline C",
                    "inline C is opaque to verification condition generation"));
        }
        if (statement instanceof IrStmt.Slice || statement instanceof IrStmt.Wait) {
            throw new IllegalStateException(
                    "wait/slice should have been removed by normalisation: " + statement);
        }
        throw new IllegalStateException("Unhandled statement: " + statement.getClass().getSimpleName());
    }

    /**
     * {@code start}, {@code stop}, {@code error} and {@code restart}. Each moves a
     * process to a state; the ones targeting the enclosing process also reset its timer.
     */
    private Fragment buildProcessControl(IrProcess process, IrStmt.ProcessControl control) {
        String target = control.targetsEnclosingProcess() ? process.getName() : control.getProcess();
        String state = switch (control.getKind()) {
            case STOP -> "stop";
            case ERROR -> "error";
            case START, RESTART -> startStateOf(target);
        };

        List<CfgNode> effects = new ArrayList<>();
        effects.add(new CfgNode.SetState(target, state));
        if (target.equals(process.getName())) {
            effects.add(new CfgNode.ResetTimer(process.getName()));
        }
        return effect(effects.toArray(new CfgNode[0]));
    }

    private String startStateOf(String processName) {
        IrProcess target = program.findProcess(processName);
        if (target == null || target.getStartState() == null) {
            throw new IllegalStateException("no start state for process " + processName);
        }
        return target.getStartState().getName();
    }

    /** An expression statement contributes an effect only when it assigns something. */
    private Fragment buildExpressionStatement(IrExpr expr) {
        if (expr instanceof IrExpr.Assign assign) {
            return effect(new CfgNode.Assign(assign.getTarget(), valueOf(assign)));
        }
        if (expr instanceof IrExpr.IncDec incDec) {
            IrExpr.BinaryOp op = incDec.getOp() == IrExpr.IncDecOp.INCREMENT
                    ? IrExpr.BinaryOp.ADD
                    : IrExpr.BinaryOp.SUB;
            IrExpr one = new IrExpr.Literal(IrExpr.Literal.Kind.INTEGER, "1");
            one.setResultType(incDec.getTarget().getResultType());
            IrExpr updated = new IrExpr.Binary(op, incDec.getTarget(), one);
            updated.setResultType(incDec.getTarget().getResultType());
            return effect(new CfgNode.Assign(incDec.getTarget(), updated));
        }
        // Any other expression has no observable effect on the program state.
        CfgNode.Join node = new CfgNode.Join();
        return new Fragment(node, node);
    }

    /** A compound assignment stores {@code target op value}, not just {@code value}. */
    private IrExpr valueOf(IrExpr.Assign assign) {
        IrExpr.BinaryOp underlying = assign.getOp().underlying();
        if (underlying == null) {
            return assign.getValue();
        }
        IrExpr combined = new IrExpr.Binary(underlying, assign.getTarget(), assign.getValue());
        combined.setResultType(assign.getTarget().getResultType());
        return combined;
    }

    private Fragment buildLocalDeclaration(IrDecl.Variable variable) {
        if (variable.getInitializer() == null) {
            CfgNode.Join node = new CfgNode.Join();
            return new Fragment(node, node);
        }
        IrExpr.VarRef target = new IrExpr.VarRef(variable.getName());
        target.setResultType(variable.getType());
        return effect(new CfgNode.Assign(target, variable.getInitializer()));
    }

    /**
     * Branches once per way the condition can be evaluated. Short-circuiting means
     * {@code a && b} has three: {@code a} false and {@code b} never evaluated, both true,
     * and {@code a} true with {@code b} false. An outcome whose value is already a
     * constant contributes only the branch it can actually take.
     */
    private Fragment buildIf(IrProcess process, IrStmt.If ifStmt) {
        CfgNode.Join entry = new CfgNode.Join();
        CfgNode.Join exit = new CfgNode.Join();

        for (ExprLowering.Outcome outcome : ExprLowering.lower(ifStmt.getCondition())) {
            if (!outcome.isConstant(false)) {
                List<IrExpr> guards = new ArrayList<>(outcome.guards());
                guards.add(outcome.value());
                addBranch(process, entry, exit, guards, ifStmt.getThenBranch());
            }
            if (!outcome.isConstant(true)) {
                List<IrExpr> guards = new ArrayList<>(outcome.guards());
                guards.add(ExprLowering.not(outcome.value()));
                addBranch(process, entry, exit, guards, ifStmt.getElseBranch());
            }
        }
        return new Fragment(entry, exit);
    }

    private void addBranch(IrProcess process, CfgNode entry, CfgNode exit,
                           List<IrExpr> guards, IrStmt body) {
        CfgNode.Guard guard = new CfgNode.Guard(ExprLowering.conjunction(guards));
        entry.addSuccessor(guard);
        Fragment fragment = buildStatement(process, body);
        guard.addSuccessor(fragment.entry());
        fragment.exit().addSuccessor(exit);
    }

    /**
     * Every case is independent after normalisation, so each becomes a branch guarded by
     * the selector matching its label. The default is guarded by the selector matching
     * none of them.
     */
    private Fragment buildSwitch(IrProcess process, IrStmt.Switch switchStmt) {
        CfgNode.Join entry = new CfgNode.Join();
        CfgNode.Join exit = new CfgNode.Join();

        List<IrExpr> labels = new ArrayList<>();
        boolean hasDefault = false;

        for (IrStmt.SwitchCase clause : switchStmt.getCases()) {
            IrExpr guard;
            if (clause.isDefault()) {
                hasDefault = true;
                guard = matchesNone(switchStmt.getSelector(), labels);
            } else {
                guard = comparison(IrExpr.BinaryOp.EQ, switchStmt.getSelector(), clause.getLabel());
                labels.add(clause.getLabel());
            }

            CfgNode.Guard guardNode = new CfgNode.Guard(guard);
            entry.addSuccessor(guardNode);

            CfgNode current = guardNode;
            for (IrStmt statement : clause.getStatements()) {
                Fragment fragment = buildStatement(process, statement);
                current.addSuccessor(fragment.entry());
                current = fragment.exit();
            }
            current.addSuccessor(exit);
        }

        if (!hasDefault) {
            // With no default, matching nothing simply falls through the switch.
            CfgNode.Guard none = new CfgNode.Guard(matchesNone(switchStmt.getSelector(), labels));
            entry.addSuccessor(none);
            none.addSuccessor(exit);
        }

        if (switchStmt.getCases().isEmpty()) {
            entry.addSuccessor(exit);
        }
        return new Fragment(entry, exit);
    }

    private IrExpr matchesNone(IrExpr selector, List<IrExpr> labels) {
        if (labels.isEmpty()) {
            IrExpr trueLiteral = new IrExpr.Literal(IrExpr.Literal.Kind.BOOL, "true");
            trueLiteral.setResultType(IrType.BOOL);
            return trueLiteral;
        }
        IrExpr result = null;
        for (IrExpr label : labels) {
            IrExpr differs = comparison(IrExpr.BinaryOp.NE, selector, label);
            result = result == null ? differs : and(result, differs);
        }
        return result;
    }

    private IrExpr comparison(IrExpr.BinaryOp op, IrExpr left, IrExpr right) {
        IrExpr comparison = new IrExpr.Binary(op,
                su.nsk.iae.reflex.ir.IrCopier.copy(left), su.nsk.iae.reflex.ir.IrCopier.copy(right));
        comparison.setResultType(IrType.BOOL);
        return comparison;
    }

    private IrExpr and(IrExpr left, IrExpr right) {
        IrExpr conjunction = new IrExpr.Binary(IrExpr.BinaryOp.AND, left, right);
        conjunction.setResultType(IrType.BOOL);
        return conjunction;
    }

    @SuppressWarnings("unused")
    private IrExpr negate(IrExpr condition) {
        IrExpr negated = new IrExpr.Unary(IrExpr.UnaryOp.NOT,
                su.nsk.iae.reflex.ir.IrCopier.copy(condition));
        negated.setResultType(IrType.BOOL);
        return negated;
    }

    /**
     * Attaches a statement's attributes to the first node of its fragment. Only the
     * leading node carries them, so accumulating along a path counts each effect once.
     */
    private Fragment carrying(IrStmt statement, Fragment fragment) {
        if (attributes != null) {
            fragment.entry().setAttributes(attributes.of(statement));
        }
        return fragment;
    }

    /** A straight-line fragment performing the given effects in order. */
    private Fragment effect(CfgNode... nodes) {
        CfgNode first = nodes[0];
        CfgNode current = first;
        for (int i = 1; i < nodes.length; i++) {
            current.addSuccessor(nodes[i]);
            current = nodes[i];
        }
        return new Fragment(first, current);
    }
}
