package su.nsk.iae.reflex.ann;

import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.preprocess.CastInsertionPass;
import su.nsk.iae.reflex.preprocess.TypeEnvironment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives every annotation expression a type.
 *
 * <p>The translation needs them: it converts an operand whose type differs from the type
 * the operation is carried out at, exactly as the program's cast pass does. Types are
 * taken from the same {@link TypeEnvironment} the program uses, which is why mangling has
 * to run first - a variable is looked up under its final name.
 *
 * <p>Quantified variables and the parameters of a {@code define} have no declaration to
 * look up; they take the type their domain implies, or int where nothing says.
 */
public final class AnnTyping {

    private final TypeEnvironment types;
    private final Deque<Map<String, IrType>> bound = new ArrayDeque<>();
    private final List<String> diagnostics = new ArrayList<>();

    public AnnTyping(TypeEnvironment types) {
        this.types = types;
    }

    /** Names an annotation used that could not be given a type. */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    /** Types every annotation in the program. */
    public void run(IrProgram program) {
        typeAll(program);
        program.getGlobalVariables().forEach(this::typeAll);
        program.getNodes().forEach(this::typeAll);
        for (IrProcess process : program.getProcesses()) {
            typeAll(process);
            for (IrState state : process.getStates()) {
                typeAll(state);
                state.getStatements().forEach(this::typeStatement);
                if (state.getTimeout() != null) {
                    typeStatement(state.getTimeout().getBody());
                }
            }
        }
    }

    private void typeStatement(IrStmt statement) {
        if (statement == null) {
            return;
        }
        typeAll(statement);
        if (statement instanceof IrStmt.Block block) {
            block.getStatements().forEach(this::typeStatement);
        } else if (statement instanceof IrStmt.If ifStmt) {
            typeStatement(ifStmt.getThenBranch());
            typeStatement(ifStmt.getElseBranch());
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            switchStmt.getCases().forEach(c -> c.getStatements().forEach(this::typeStatement));
        } else if (statement instanceof IrStmt.For forStmt) {
            typeStatement(forStmt.getBody());
        }
    }

    private void typeAll(su.nsk.iae.reflex.ir.IrNode node) {
        for (Annotation annotation : node.getAnnotations()) {
            type(annotation);
        }
    }

    /** Types one annotation: its formula, or the bodies of its definitions. */
    public void type(Annotation annotation) {
        if (annotation.isForeignLanguage()) {
            return;
        }
        for (AnnDefinition definition : annotation.getDefinitions()) {
            Map<String, IrType> parameters = new HashMap<>();
            // A define's parameters are untyped as far as the environment is concerned;
            // the definition's declared parameter types are not carried through lowering.
            definition.parameters().forEach(p -> parameters.put(p, IrType.INT32));
            bound.push(parameters);
            type(definition.body());
            bound.pop();
        }
        if (annotation.getBody() != null) {
            type(annotation.getBody());
        }
    }

    // ------------------------------------------------------------------ expressions

    /** Assigns {@code expr} its type and returns it. */
    public IrType type(AnnExpr expr) {
        IrType result = compute(expr);
        expr.setType(result);
        return result;
    }

    private IrType compute(AnnExpr expr) {
        if (expr instanceof AnnExpr.Literal literal) {
            return switch (literal.getKind()) {
                case BOOL -> IrType.BOOL;
                case REAL -> IrType.UNDEFINED_FLOAT;
                // A time literal is a count of milliseconds, which is a nat.
                case TIME -> IrType.TIME;
                case INTEGER -> IrType.UNDEFINED_INT;
            };
        }
        if (expr instanceof AnnExpr.VarRef ref) {
            return typeVarRef(ref);
        }
        if (expr instanceof AnnExpr.Binary binary) {
            return typeBinary(binary);
        }
        if (expr instanceof AnnExpr.Unary unary) {
            IrType operand = type(unary.getOperand());
            return unary.getOp() == AnnExpr.UnaryOp.NOT ? IrType.BOOL : operand;
        }
        if (expr instanceof AnnExpr.Implication implication) {
            type(implication.getLeft());
            type(implication.getRight());
            return IrType.BOOL;
        }
        if (expr instanceof AnnExpr.Equivalence equivalence) {
            type(equivalence.getLeft());
            type(equivalence.getRight());
            return IrType.BOOL;
        }
        if (expr instanceof AnnExpr.Quantifier quantifier) {
            return typeQuantifier(quantifier);
        }
        if (expr instanceof AnnExpr.Call call) {
            call.getArguments().forEach(this::type);
            // A definition's type is whatever its body has; it is resolved at expansion.
            return IrType.UNDEFINED_INT;
        }
        if (expr instanceof AnnExpr.InState) {
            return IrType.BOOL;
        }
        if (expr instanceof AnnExpr.LocalTime) {
            return IrType.TIME;
        }
        if (expr instanceof AnnExpr.Temporal temporal) {
            return typeTemporal(temporal);
        }
        if (expr instanceof AnnExpr.Scope scope) {
            // Scope shifts which state the expression is read in, not what it denotes.
            if (scope.getPhi() != null) {
                type(scope.getPhi());
            }
            return type(scope.getBase());
        }
        return IrType.UNDEFINED_INT;
    }

    private IrType typeVarRef(AnnExpr.VarRef ref) {
        ref.getAccesses().forEach(access -> {
            if (access instanceof AnnExpr.IndexAccess index) {
                type(index.getIndex());
            }
        });

        IrType boundType = lookupBound(ref.getName());
        if (boundType != null) {
            return boundType;
        }

        IrType declared = types.variableType(ref.getName());
        if (declared == null) {
            diagnostics.add("annotation names an undeclared variable: " + ref.getName());
            return IrType.UNDEFINED_INT;
        }
        return walkAccesses(declared, ref);
    }

    /** Follows an access path through arrays and structs, as the program's typing does. */
    private IrType walkAccesses(IrType declared, AnnExpr.VarRef ref) {
        IrType current = declared;
        for (AnnExpr.Access access : ref.getAccesses()) {
            if (access instanceof AnnExpr.FieldAccess field) {
                current = types.fieldType(current, field.getField());
            } else {
                current = types.elementType(current);
            }
            if (current == null) {
                diagnostics.add("annotation access path does not fit the type of " + ref.getName());
                return IrType.UNDEFINED_INT;
            }
        }
        return current instanceof IrType.Enum ? IrType.INT32 : current;
    }

    private IrType typeBinary(AnnExpr.Binary binary) {
        IrType left = type(binary.getLeft());
        IrType right = type(binary.getRight());
        if (binary.getOp() == AnnExpr.BinaryOp.AND || binary.getOp() == AnnExpr.BinaryOp.OR) {
            return IrType.BOOL;
        }
        IrType at = CastInsertionPass.defType(binary.getOp().symbol(), left, right);
        return binary.getOp().isComparison() ? IrType.BOOL : at;
    }

    private IrType typeQuantifier(AnnExpr.Quantifier quantifier) {
        Map<String, IrType> variables = new HashMap<>();
        for (AnnExpr.BoundVar variable : quantifier.getVariables()) {
            variables.put(variable.getName(), domainType(variable.getDomain()));
        }
        bound.push(variables);
        type(quantifier.getBody());
        bound.pop();
        return IrType.BOOL;
    }

    /** The type a quantified variable takes from where it ranges. */
    private IrType domainType(AnnExpr.Domain domain) {
        if (domain instanceof AnnExpr.RangeDomain range) {
            type(range.getFrom());
            type(range.getTo());
            return IrType.INT32;
        }
        if (domain instanceof AnnExpr.SetDomain set) {
            IrType first = type(set.getFirst());
            if (set.getSecond() != null) {
                type(set.getSecond());
            }
            return first;
        }
        if (domain instanceof AnnExpr.TypeDomain named) {
            return typeByName(named.getTypeName());
        }
        if (domain instanceof AnnExpr.ExprDomain expr) {
            IrType collection = type(expr.getExpr());
            IrType element = types.elementType(collection);
            return element == null ? IrType.INT32 : element;
        }
        return IrType.INT32;
    }

    private IrType typeByName(String name) {
        return switch (name) {
            case "bool" -> IrType.BOOL;
            case "real" -> IrType.of(IrType.BuiltinKind.DOUBLE);
            case "nat" -> IrType.of(IrType.BuiltinKind.UINT32);
            case "time" -> IrType.TIME;
            default -> IrType.INT32;
        };
    }

    /** Every temporal operator is a predicate; timer compares a count against a bound. */
    private IrType typeTemporal(AnnExpr.Temporal temporal) {
        if (temporal.getFirst() != null) {
            type(temporal.getFirst());
        }
        if (temporal.getSecond() != null) {
            type(temporal.getSecond());
        }
        if (temporal.getThird() != null) {
            type(temporal.getThird());
        }
        return IrType.BOOL;
    }

    private IrType lookupBound(String name) {
        for (Map<String, IrType> scope : bound) {
            IrType type = scope.get(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }
}
