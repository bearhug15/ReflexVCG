package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Deep copies of IR subtrees.
 *
 * <p>Normalisation duplicates program fragments: switch fall-through appends a copy of
 * the following case's statements to the preceding one, and a {@code wait} places its
 * condition both in the state it came from and in the light state that keeps testing it.
 * Since IR nodes are mutable, sharing a subtree between two places would let a later
 * rewrite of one silently change the other.
 *
 * <p>Source positions and annotations are carried over, so a copy still points at the
 * code it came from.
 */
public final class IrCopier {

    private IrCopier() {
    }

    public static List<IrStmt> copyStatements(List<IrStmt> statements) {
        List<IrStmt> copies = new ArrayList<>(statements.size());
        statements.forEach(s -> copies.add(copy(s)));
        return copies;
    }

    public static IrStmt copy(IrStmt statement) {
        if (statement == null) {
            return null;
        }
        IrStmt copy;
        if (statement instanceof IrStmt.Empty) {
            copy = new IrStmt.Empty();
        } else if (statement instanceof IrStmt.Block block) {
            copy = new IrStmt.Block(copyStatements(block.getStatements()));
        } else if (statement instanceof IrStmt.ProcessControl control) {
            copy = new IrStmt.ProcessControl(control.getKind(), control.getProcess());
        } else if (statement instanceof IrStmt.ResetTimer) {
            copy = new IrStmt.ResetTimer();
        } else if (statement instanceof IrStmt.SetState setState) {
            copy = new IrStmt.SetState(setState.getState(), setState.isNext());
        } else if (statement instanceof IrStmt.If ifStmt) {
            copy = new IrStmt.If(copy(ifStmt.getCondition()),
                    copy(ifStmt.getThenBranch()), copy(ifStmt.getElseBranch()));
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            List<IrStmt.SwitchCase> cases = new ArrayList<>();
            for (IrStmt.SwitchCase clause : switchStmt.getCases()) {
                IrStmt.SwitchCase clauseCopy = new IrStmt.SwitchCase(
                        copy(clause.getLabel()), copyStatements(clause.getStatements()), clause.isBreaks());
                clauseCopy.copyOriginFrom(clause);
                cases.add(clauseCopy);
            }
            copy = new IrStmt.Switch(copy(switchStmt.getSelector()), cases);
        } else if (statement instanceof IrStmt.ExprStatement expr) {
            copy = new IrStmt.ExprStatement(copy(expr.getExpression()));
        } else if (statement instanceof IrStmt.LocalVar local) {
            copy = new IrStmt.LocalVar(copy(local.getDeclaration()));
        } else if (statement instanceof IrStmt.Slice) {
            copy = new IrStmt.Slice();
        } else if (statement instanceof IrStmt.Wait wait) {
            copy = new IrStmt.Wait(copy(wait.getCondition()),
                    copy(wait.getTimeout()), copy(wait.getTimeoutBody()));
        } else if (statement instanceof IrStmt.For forStmt) {
            List<IrDecl.Variable> inits = new ArrayList<>();
            forStmt.getInitDeclarations().forEach(v -> inits.add(copy(v)));
            copy = new IrStmt.For(inits, copy(forStmt.getInitExpression()),
                    copy(forStmt.getCondition()), copy(forStmt.getUpdate()), copy(forStmt.getBody()));
        } else if (statement instanceof IrStmt.CCode ccode) {
            copy = new IrStmt.CCode(ccode.getCode());
        } else {
            throw new IllegalStateException("Cannot copy statement: " + statement.getClass().getSimpleName());
        }
        copy.copyOriginFrom(statement);
        return copy;
    }

    public static IrDecl.Variable copy(IrDecl.Variable variable) {
        IrDecl.Variable copy = new IrDecl.Variable(
                variable.getName(), variable.getType(), copy(variable.getInitializer()));
        copy.setShared(variable.isShared());
        copy.copyOriginFrom(variable);
        return copy;
    }

    public static TimeRef copy(TimeRef ref) {
        if (ref == null) {
            return null;
        }
        TimeRef copy = switch (ref.getKind()) {
            case TIME_LITERAL -> TimeRef.ofTimeLiteral(ref.getText());
            case INTEGER -> TimeRef.ofInteger(ref.getText());
            case NAME -> TimeRef.ofName(ref.getText());
        };
        copy.copyOriginFrom(ref);
        return copy;
    }

    public static IrExpr copy(IrExpr expr) {
        if (expr == null) {
            return null;
        }
        IrExpr copy;
        if (expr instanceof IrExpr.Literal literal) {
            copy = new IrExpr.Literal(literal.getKind(), literal.getText());
        } else if (expr instanceof IrExpr.VarRef ref) {
            copy = new IrExpr.VarRef(ref.getName(), copyAccesses(ref.getAccesses()));
        } else if (expr instanceof IrExpr.Binary binary) {
            copy = new IrExpr.Binary(binary.getOp(), copy(binary.getLeft()), copy(binary.getRight()));
        } else if (expr instanceof IrExpr.Unary unary) {
            copy = new IrExpr.Unary(unary.getOp(), copy(unary.getOperand()));
        } else if (expr instanceof IrExpr.Cast cast) {
            copy = new IrExpr.Cast(cast.getTargetType(), copy(cast.getOperand()),
                    cast.getPreType(), cast.isImplicit());
        } else if (expr instanceof IrExpr.Assign assign) {
            copy = new IrExpr.Assign(assign.getOp(),
                    (IrExpr.VarRef) copy(assign.getTarget()), copy(assign.getValue()));
        } else if (expr instanceof IrExpr.IncDec incDec) {
            copy = new IrExpr.IncDec(incDec.getOp(), incDec.isPrefix(),
                    (IrExpr.VarRef) copy(incDec.getTarget()));
        } else if (expr instanceof IrExpr.Call call) {
            List<IrExpr> args = new ArrayList<>();
            call.getArguments().forEach(a -> args.add(copy(a)));
            copy = new IrExpr.Call(call.getFunction(), args);
        } else if (expr instanceof IrExpr.CheckState check) {
            copy = new IrExpr.CheckState(check.getProcess(), check.getStatus());
        } else if (expr instanceof IrExpr.Aggregate aggregate) {
            List<IrExpr.Aggregate.Element> elements = new ArrayList<>();
            for (IrExpr.Aggregate.Element element : aggregate.getElements()) {
                elements.add(new IrExpr.Aggregate.Element(
                        copyAccess(element.getDesignator()), copy(element.getValue())));
            }
            copy = new IrExpr.Aggregate(elements);
        } else {
            throw new IllegalStateException("Cannot copy expression: " + expr.getClass().getSimpleName());
        }
        copy.setResultType(expr.getResultType());
        copy.copyOriginFrom(expr);
        return copy;
    }

    private static List<IrExpr.Access> copyAccesses(List<IrExpr.Access> accesses) {
        List<IrExpr.Access> copies = new ArrayList<>(accesses.size());
        accesses.forEach(a -> copies.add(copyAccess(a)));
        return copies;
    }

    private static IrExpr.Access copyAccess(IrExpr.Access access) {
        if (access == null) {
            return null;
        }
        if (access instanceof IrExpr.FieldAccess field) {
            return new IrExpr.FieldAccess(field.getField());
        }
        return new IrExpr.IndexAccess(copy(((IrExpr.IndexAccess) access).getIndex()));
    }
}
