package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;

/**
 * Makes C's implicit conversions explicit and records the type of every expression.
 *
 * <p>Implements the "Проставление cast" section of Preprocessing.tex. After this pass
 * every {@link IrExpr} has a {@link IrExpr#getResultType() result type}, and wherever an
 * operand's type differs from the type the operation is performed at, the operand is
 * wrapped in an implicit {@link IrExpr.Cast} carrying the type it had before. The
 * Isabelle backend can then emit conversions without re-deriving C's promotion rules.
 *
 * <p>Literals start out with an adaptive type ({@link IrType.Undefined}) and take the
 * type of their context, so {@code x + 1} does not widen {@code x} to int32.
 */
public final class CastInsertionPass {

    private final TypeEnvironment types;

    public CastInsertionPass(TypeEnvironment types) {
        this.types = types;
    }

    public void run(IrProgram program) {
        for (IrDecl.Constant constant : program.getConstants()) {
            constant.setValue(specifyDeclaredValue(constant.getValue(), constant.getType()));
        }
        for (IrDecl declaration : program.getGlobalVariables()) {
            specifyDeclaration(declaration);
        }
        for (IrDecl.Node node : program.getNodes()) {
            node.getConstants().forEach(c -> c.setValue(specifyDeclaredValue(c.getValue(), c.getType())));
            node.getVariables().forEach(this::specifyDeclaration);
        }
        for (IrProcess process : program.getProcesses()) {
            process.getVariables().forEach(this::specifyDeclaration);
            for (IrState state : process.getStates()) {
                state.getStatements().forEach(this::specify);
                if (state.getTimeout() != null) {
                    specify(state.getTimeout().getBody());
                }
            }
        }
    }

    // ------------------------------------------------------------------ type algebra

    /**
     * Type an operation is carried out at, given its operands - {@code defType} of the
     * spec. A null {@code left} means a unary operator.
     */
    public static IrType defType(String op, IrType left, IrType right) {
        if (left != null && left.isUndefined()) {
            if (right.isUndefined()) {
                return left == IrType.UNDEFINED_FLOAT || right == IrType.UNDEFINED_FLOAT
                        ? IrType.UNDEFINED_FLOAT
                        : IrType.UNDEFINED_INT;
            }
            return right;
        }
        if (left != null && right.isUndefined()) {
            return left;
        }
        if (left == null && right.isUndefined()) {
            return right;
        }
        if ((left != null && left.isFloating()) || right.isFloating()) {
            boolean either = isDouble(left) || isDouble(right);
            return IrType.of(either ? IrType.BuiltinKind.DOUBLE : IrType.BuiltinKind.FLOAT);
        }
        // time is outside the spec's integer order; an operation involving one is a time.
        if (isTime(left) || isTime(right)) {
            return IrType.TIME;
        }
        // Comparing two booleans: the spec's promotion rules send these to int32, since
        // bool takes part in the integer order, which renders as
        // `(if a then 1 else 0) = (if b then 1 else 0)`. That is equivalent to `a = b`
        // and much harder to work with in a proof, so the comparison stays at bool.
        if (("==".equals(op) || "!=".equals(op)) && left != null && left.isBool() && right.isBool()) {
            return IrType.BOOL;
        }
        return switch (op) {
            case "&&", "||", "!." -> IrType.BOOL;
            case "&", "|", "^", "&=", "|=", "^=" -> wider(left, right);
            case "-.", "~." -> integerRankOf(right) > integerRankOf(IrType.INT32) ? right : IrType.INT32;
            case "++.", ".++", "--.", ".--" -> right;
            default -> {
                // Arithmetic, shifts, comparisons and assignments promote to at least
                // int32, then to the wider operand.
                if (integerRankOf(left) < integerRankOf(IrType.INT32)
                        && integerRankOf(right) < integerRankOf(IrType.INT32)) {
                    yield IrType.INT32;
                }
                yield wider(left, right);
            }
        };
    }

    private static IrType wider(IrType left, IrType right) {
        if (left == null) {
            return right;
        }
        return integerRankOf(left) < integerRankOf(right) ? right : left;
    }

    private static int integerRankOf(IrType type) {
        return type == null ? -1 : type.integerRank();
    }

    private static boolean isDouble(IrType type) {
        return type != null && type.isBuiltin(IrType.BuiltinKind.DOUBLE);
    }

    private static boolean isTime(IrType type) {
        return type != null && type.isBuiltin(IrType.BuiltinKind.TIME);
    }

    // ------------------------------------------------------------------ declarations

    private void specifyDeclaration(IrDecl declaration) {
        if (declaration instanceof IrDecl.Variable variable) {
            variable.setInitializer(specifyDeclaredValue(variable.getInitializer(), variable.getType()));
        }
    }

    /**
     * Types an initialiser and converts it to the declared type, as an assignment would.
     * Returns the expression to store, which is a cast when a conversion was needed.
     */
    private IrExpr specifyDeclaredValue(IrExpr value, IrType declared) {
        if (value == null) {
            return null;
        }
        if (value instanceof IrExpr.Aggregate aggregate) {
            specifyAggregate(aggregate, declared);
            return aggregate;
        }
        IrType actual = specify(value);
        if (!declared.equals(actual) && !actual.isUndefined()) {
            return implicitCast(value, declared, actual);
        }
        return value;
    }

    /**
     * Aggregate initialisers are converted element by element. Working out which member
     * each positional element belongs to needs the struct's field order, so elements
     * whose target cannot be established keep their own type.
     */
    private void specifyAggregate(IrExpr.Aggregate aggregate, IrType declared) {
        IrType elementType = declared instanceof IrType.Array array ? array.element() : null;
        for (IrExpr.Aggregate.Element element : aggregate.getElements()) {
            if (element.getValue() instanceof IrExpr.Aggregate nested) {
                specifyAggregate(nested, elementType == null ? declared : elementType);
            } else if (elementType != null) {
                IrType actual = specify(element.getValue());
                if (!elementType.equals(actual) && !actual.isUndefined()) {
                    element.setValue(implicitCast(element.getValue(), elementType, actual));
                }
            } else {
                specify(element.getValue());
            }
            if (element.getDesignator() instanceof IrExpr.IndexAccess index) {
                specify(index.getIndex());
            }
        }
        aggregate.setResultType(declared);
    }

    // ------------------------------------------------------------------ statements

    private void specify(IrStmt statement) {
        if (statement == null) {
            return;
        }
        if (statement instanceof IrStmt.Block block) {
            block.getStatements().forEach(this::specify);
        } else if (statement instanceof IrStmt.ExprStatement expr) {
            specify(expr.getExpression());
        } else if (statement instanceof IrStmt.If ifStmt) {
            IrType condition = specify(ifStmt.getCondition());
            if (!condition.isBool()) {
                ifStmt.setCondition(implicitCast(ifStmt.getCondition(), IrType.BOOL, condition));
            }
            specify(ifStmt.getThenBranch());
            specify(ifStmt.getElseBranch());
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            specify(switchStmt.getSelector());
            for (IrStmt.SwitchCase clause : switchStmt.getCases()) {
                if (clause.getLabel() != null) {
                    specify(clause.getLabel());
                }
                clause.getStatements().forEach(this::specify);
            }
        } else if (statement instanceof IrStmt.LocalVar local) {
            specifyDeclaration(local.getDeclaration());
        } else if (statement instanceof IrStmt.Wait wait) {
            IrType condition = specify(wait.getCondition());
            if (!condition.isBool()) {
                wait.setCondition(implicitCast(wait.getCondition(), IrType.BOOL, condition));
            }
            specify(wait.getTimeoutBody());
        } else if (statement instanceof IrStmt.For forStmt) {
            forStmt.getInitDeclarations().forEach(this::specifyDeclaration);
            if (forStmt.getInitExpression() != null) {
                specify(forStmt.getInitExpression());
            }
            IrType condition = specify(forStmt.getCondition());
            if (!condition.isBool()) {
                forStmt.setCondition(implicitCast(forStmt.getCondition(), IrType.BOOL, condition));
            }
            specify(forStmt.getUpdate());
            specify(forStmt.getBody());
        }
        // Empty, ProcessControl, ResetTimer, SetState, Slice and CCode hold no expressions.
    }

    // ------------------------------------------------------------------ expressions

    /** Assigns {@code expr} its result type and returns it. */
    private IrType specify(IrExpr expr) {
        IrType type = compute(expr);
        expr.setResultType(type);
        return type;
    }

    private IrType compute(IrExpr expr) {
        if (expr instanceof IrExpr.Literal literal) {
            return switch (literal.getKind()) {
                case BOOL -> IrType.BOOL;
                // Integer and time literals adapt to their context, as the spec has them.
                case INTEGER, TIME -> IrType.UNDEFINED_INT;
                case FLOAT -> IrType.UNDEFINED_FLOAT;
            };
        }
        if (expr instanceof IrExpr.VarRef ref) {
            for (IrExpr.Access access : ref.getAccesses()) {
                if (access instanceof IrExpr.IndexAccess index) {
                    specify(index.getIndex());
                }
            }
            IrType type = types.accessType(ref.getName(), ref.getAccesses());
            // An undeclared name or a path that does not fit its type is left untyped
            // here; diagnosing it belongs to a checking stage, not to this rewrite.
            return type == null ? IrType.VOID : type;
        }
        if (expr instanceof IrExpr.CheckState) {
            return IrType.BOOL;
        }
        if (expr instanceof IrExpr.Cast cast) {
            IrType operand = specify(cast.getOperand());
            cast.setPreType(operand);
            return cast.getTargetType();
        }
        if (expr instanceof IrExpr.Unary unary) {
            IrType operand = specify(unary.getOperand());
            IrType result = defType(unaryOpKey(unary.getOp()), null, operand);
            if (!result.equals(operand) && (!operand.isUndefined() || result.isUndefined())) {
                unary.setOperand(implicitCast(unary.getOperand(), result, operand));
            }
            return result;
        }
        if (expr instanceof IrExpr.Binary binary) {
            IrType left = specify(binary.getLeft());
            IrType right = specify(binary.getRight());
            IrType at = defType(binary.getOp().symbol(), left, right);
            if (!at.equals(left) && (!left.isUndefined() || at.isUndefined())) {
                binary.setLeft(implicitCast(binary.getLeft(), at, left));
            }
            if (!at.equals(right) && (!right.isUndefined() || at.isUndefined())) {
                binary.setRight(implicitCast(binary.getRight(), at, right));
            }
            // Comparisons and the logical connectives are performed at `at` but produce
            // a bool.
            return binary.getOp().isBoolean() ? IrType.BOOL : at;
        }
        if (expr instanceof IrExpr.Assign assign) {
            IrType value = specify(assign.getValue());
            IrType target = specify(assign.getTarget());
            if (!target.equals(value) && !value.isUndefined()) {
                assign.setValue(implicitCast(assign.getValue(), target, value));
            }
            return target;
        }
        if (expr instanceof IrExpr.IncDec incDec) {
            return specify(incDec.getTarget());
        }
        if (expr instanceof IrExpr.Call call) {
            call.getArguments().forEach(this::specify);
            // Reflex declares function signatures without names, so a call cannot be
            // matched to one; the result type is left open.
            return IrType.VOID;
        }
        if (expr instanceof IrExpr.Aggregate aggregate) {
            aggregate.getElements().forEach(e -> specify(e.getValue()));
            return IrType.VOID;
        }
        throw new IllegalStateException("Unhandled expression: " + expr.getClass().getSimpleName());
    }

    /** The spec keys unary operators with a trailing dot to separate them from binary. */
    private static String unaryOpKey(IrExpr.UnaryOp op) {
        return switch (op) {
            case NOT -> "!.";
            case NEG -> "-.";
            case BIT_NOT -> "~.";
            case PLUS -> "+.";
        };
    }

    private static IrExpr.Cast implicitCast(IrExpr operand, IrType target, IrType actual) {
        IrExpr.Cast cast = new IrExpr.Cast(target, operand, actual, true);
        cast.setSource(operand.getSource());
        cast.setResultType(target);
        return cast;
    }
}
