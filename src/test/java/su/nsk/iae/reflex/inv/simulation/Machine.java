package su.nsk.iae.reflex.inv.simulation;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.term.Terms;
import su.nsk.iae.reflex.vc.InitialCondition;
import su.nsk.iae.reflex.vc.IsabelleRenderer;
import su.nsk.iae.reflex.vc.VcStatement;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Runs a program concretely, cycle by cycle, over the same control-flow graph the
 * conditions are generated from, building the ReflexBase state a run produces.
 *
 * <p>Inputs are drawn at random - mostly from the numbers the program itself mentions, so
 * its guards go both ways. Expressions are read the way the renderer writes them in HOL:
 * unbounded numbers, a nat subtraction stopping at zero, {@code div} rounding down. A loop
 * runs its body until its condition fails, each iteration ending in an environment step as
 * the conditions have it, and a loop that does not run still leaves the boundary state the
 * conditions give it.
 *
 * <p>After the start and after every cycle it reports the boundary reached, which is where
 * an invariant has to hold.
 */
final class Machine {

    private static final int MAX_ITERATIONS = 10_000;

    private final IrProgram program;
    private final Cfg cfg;
    private final Random random;
    private final long clock;
    private final List<BigInteger> interesting = new ArrayList<>();
    private final History history = new History();
    private History.Node state = history.empty();

    Machine(IrProgram program, Cfg cfg, Random random) {
        this.program = program;
        this.cfg = cfg;
        this.random = random;
        this.clock = program.getClock().getKind() == TimeRef.Kind.TIME_LITERAL
                ? IsabelleRenderer.parseTimeMillis(program.getClock().getText())
                : IsabelleRenderer.parseInteger(program.getClock().getText());
        collectInterestingNumbers();
    }

    long clock() {
        return clock;
    }

    History.Node state() {
        return state;
    }

    /** The base case: exactly the statements {@link InitialCondition} states. */
    void start(Consumer<History.Node> atBoundary) {
        VerificationCondition initial = InitialCondition.build(program);
        for (VcStatement statement : initial.getStatements()) {
            if (statement instanceof VcStatement.InputChoice input) {
                state = state.setVar(input.variable(), Val.wrap(randomValue(input.type()), input.type()));
            } else if (statement instanceof VcStatement.Assign assign) {
                assign(assign.variable(), assign.value());
            } else if (statement instanceof VcStatement.SetProcessState set) {
                state = state.setPstate(set.process(), set.pstate());
            } else if (statement instanceof VcStatement.ToEnv) {
                state = state.toEnv();
            }
        }
        atBoundary.accept(state);
    }

    /** One cycle: every process in its current state, then the environment step. */
    void cycle(Consumer<History.Node> atBoundary) {
        run(cfg.getEntry(), atBoundary);
    }

    private void run(CfgNode entry, Consumer<History.Node> atBoundary) {
        CfgNode node = entry;
        while (true) {
            apply(node, atBoundary);
            List<CfgNode> successors = node.getSuccessors();
            if (successors.isEmpty()) {
                return;
            }
            node = choose(successors);
        }
    }

    private CfgNode choose(List<CfgNode> successors) {
        if (successors.size() == 1) {
            return successors.get(0);
        }
        List<CfgNode> taken = new ArrayList<>();
        for (CfgNode successor : successors) {
            if (admits(successor)) {
                taken.add(successor);
            }
        }
        if (taken.size() != 1) {
            throw new IllegalStateException("expected exactly one way on, found " + taken + " of " + successors);
        }
        return taken.get(0);
    }

    private boolean admits(CfgNode node) {
        // A branch may open with joins before the guard that decides it.
        while (node instanceof CfgNode.Join && node.getSuccessors().size() == 1) {
            node = node.getSuccessors().get(0);
        }
        if (node instanceof CfgNode.InState inState) {
            return state.getPstate(inState.getProcess()).equals(inState.getState());
        }
        if (node instanceof CfgNode.Guard guard) {
            return (Boolean) evaluate(guard.getCondition(), state);
        }
        if (node instanceof CfgNode.TimeoutGuard timeout) {
            BigInteger elapsed = state.ltime(timeout.getProcess(), clock);
            return (elapsed.compareTo(duration(timeout.getDuration())) >= 0) == timeout.isExceeded();
        }
        return true;
    }

    private void apply(CfgNode node, Consumer<History.Node> atBoundary) {
        if (node instanceof CfgNode.Assign assign) {
            assign(assign.getTarget(), assign.getValue());
        } else if (node instanceof CfgNode.InputChoice input) {
            state = state.setVar(input.getVariable(), Val.wrap(randomValue(input.getType()), input.getType()));
        } else if (node instanceof CfgNode.SetState set) {
            state = state.setPstate(set.getProcess(), set.getState());
        } else if (node instanceof CfgNode.ResetTimer reset) {
            state = state.reset(reset.getProcess());
        } else if (node instanceof CfgNode.ToEnv) {
            state = state.toEnv();
            atBoundary.accept(state);
        } else if (node instanceof CfgNode.LoopCut cut) {
            // Each iteration ends in an environment step, which the model makes a boundary.
            int iterations = 0;
            while (cut.getCondition() == null || (Boolean) evaluate(cut.getCondition(), state)) {
                run(cut.getBodyEntry(), atBoundary);
                state = state.toEnv();
                atBoundary.accept(state);
                if (++iterations > MAX_ITERATIONS) {
                    throw new IllegalStateException("a loop ran more than " + MAX_ITERATIONS + " times");
                }
            }
            if (iterations == 0) {
                state = state.toEnv();
                atBoundary.accept(state);
            }
        } else if (node instanceof CfgNode.Unsupported unsupported) {
            throw new UnsupportedOperationException(unsupported.getConstruct());
        }
    }

    private void assign(IrExpr.VarRef target, IrExpr value) {
        Object computed = evaluate(value, state);
        List<Val.Access> path = accessPath(target, state);
        state = state.setVarVal(target.getName(), path, Val.wrap(computed, target.getResultType()));
    }

    // ------------------------------------------------------------------ expressions

    /**
     * An expression's value in {@code at}, as the renderer writes it: a Boolean, a
     * BigInteger or a Double.
     */
    static Object evaluate(IrExpr expr, History.Node at) {
        if (expr instanceof IrExpr.Literal literal) {
            return switch (literal.getKind()) {
                case BOOL -> literal.getText().equals("true");
                case INTEGER -> BigInteger.valueOf(IsabelleRenderer.parseInteger(literal.getText()));
                case TIME -> BigInteger.valueOf(IsabelleRenderer.parseTimeMillis(literal.getText()));
                case FLOAT -> Double.parseDouble(literal.getText().replaceAll("[fFlL]$", ""));
            };
        }
        if (expr instanceof IrExpr.VarRef ref) {
            return Val.project(at.getVarVal(ref.getName(), accessPath(ref, at)), ref.getResultType());
        }
        if (expr instanceof IrExpr.At pinned) {
            return evaluate(pinned.getOperand(), at.back(pinned.getStepsBack()));
        }
        if (expr instanceof IrExpr.CheckState check) {
            String now = at.getPstate(check.getProcess());
            boolean stopped = now.equals("stop");
            boolean failed = now.equals("error");
            return switch (check.getStatus()) {
                case STOP -> stopped;
                case ERROR -> failed;
                case INACTIVE -> stopped || failed;
                case ACTIVE -> !stopped && !failed;
            };
        }
        if (expr instanceof IrExpr.Cast cast) {
            return convert(evaluate(cast.getOperand(), at), cast.getPreType(), cast.getTargetType());
        }
        if (expr instanceof IrExpr.Unary unary) {
            Object operand = evaluate(unary.getOperand(), at);
            return switch (unary.getOp()) {
                case NOT -> !(Boolean) operand;
                case PLUS -> operand;
                case NEG -> operand instanceof Double d ? -d : ((BigInteger) operand).negate();
                case BIT_NOT -> complementBase(unary.getOperand().getResultType()).subtract((BigInteger) operand);
            };
        }
        if (expr instanceof IrExpr.Binary binary) {
            return binary(binary, evaluate(binary.getLeft(), at), evaluate(binary.getRight(), at));
        }
        if (expr instanceof IrExpr.Assign assign) {
            return evaluate(assign.getValue(), at);
        }
        if (expr instanceof IrExpr.IncDec incDec) {
            BigInteger target = (BigInteger) evaluate(incDec.getTarget(), at);
            return incDec.getOp() == IrExpr.IncDecOp.INCREMENT ? target.add(BigInteger.ONE) : target.subtract(BigInteger.ONE);
        }
        throw new UnsupportedOperationException("cannot run " + expr.getClass().getSimpleName());
    }

    private static Object binary(IrExpr.Binary binary, Object left, Object right) {
        switch (binary.getOp()) {
            case AND:
                return (Boolean) left && (Boolean) right;
            case OR:
                return (Boolean) left || (Boolean) right;
            case EQ:
                return compare(left, right) == 0;
            case NE:
                return compare(left, right) != 0;
            case LT:
                return compare(left, right) < 0;
            case LE:
                return compare(left, right) <= 0;
            case GT:
                return compare(left, right) > 0;
            case GE:
                return compare(left, right) >= 0;
            default:
                break;
        }
        Terms.Sort sort = Terms.sortOf(binary.getResultType());
        if (sort == Terms.Sort.REAL || left instanceof Double || right instanceof Double) {
            double a = number(left);
            double b = number(right);
            return switch (binary.getOp()) {
                case ADD -> a + b;
                case SUB -> a - b;
                case MUL -> a * b;
                case DIV -> b == 0 ? 0.0 : a / b;
                default -> throw new UnsupportedOperationException(binary.getOp().name() + " on reals");
            };
        }
        BigInteger a = Val.asInteger(left);
        BigInteger b = Val.asInteger(right);
        boolean nat = sort == Terms.Sort.NAT;
        return switch (binary.getOp()) {
            case ADD -> a.add(b);
            case SUB -> nat ? a.subtract(b).max(BigInteger.ZERO) : a.subtract(b);
            case MUL -> a.multiply(b);
            // HOL: x div 0 = 0 and x mod 0 = x; on int, div rounds down and mod takes the
            // divisor's sign.
            case DIV -> b.signum() == 0 ? BigInteger.ZERO : floorDiv(a, b);
            case MOD -> b.signum() == 0 ? a : a.subtract(b.multiply(floorDiv(a, b)));
            case SHL -> a.shiftLeft(b.intValueExact());
            case SHR -> a.shiftRight(b.intValueExact());
            case BIT_AND -> a.and(b);
            case BIT_OR -> a.or(b);
            case BIT_XOR -> a.xor(b);
            default -> throw new IllegalStateException(binary.getOp().name());
        };
    }

    private static BigInteger floorDiv(BigInteger a, BigInteger b) {
        BigInteger[] qr = a.divideAndRemainder(b);
        return qr[1].signum() != 0 && (qr[1].signum() != b.signum()) ? qr[0].subtract(BigInteger.ONE) : qr[0];
    }

    private static int compare(Object left, Object right) {
        if (left instanceof Boolean a && right instanceof Boolean b) {
            return Boolean.compare(a, b);
        }
        if (left instanceof Double || right instanceof Double) {
            return Double.compare(number(left), number(right));
        }
        return Val.asInteger(left).compareTo(Val.asInteger(right));
    }

    private static double number(Object value) {
        if (value instanceof Double d) {
            return d;
        }
        return Val.asInteger(value).doubleValue();
    }

    /** The conversions the renderer's casts make. */
    private static Object convert(Object value, IrType from, IrType to) {
        Terms.Sort source = Terms.sortOf(from);
        Terms.Sort target = Terms.sortOf(to);
        if (source == target) {
            return value;
        }
        return switch (target) {
            case BOOL -> value instanceof Double d ? d != 0 : Val.asInteger(value).signum() != 0;
            case INT -> Val.asInteger(value);
            case NAT -> Val.asInteger(value).max(BigInteger.ZERO);
            case REAL -> number(value);
        };
    }

    private static BigInteger complementBase(IrType type) {
        if (type instanceof IrType.Builtin builtin) {
            return switch (builtin.kind()) {
                case UINT8 -> BigInteger.valueOf(255);
                case UINT16 -> BigInteger.valueOf(65535);
                case UINT32 -> BigInteger.valueOf(4294967295L);
                case UINT64, TIME -> new BigInteger("18446744073709551615");
                default -> BigInteger.valueOf(-1);
            };
        }
        return BigInteger.valueOf(-1);
    }

    static List<Val.Access> accessPath(IrExpr.VarRef ref, History.Node at) {
        List<Val.Access> path = new ArrayList<>();
        for (IrExpr.Access access : ref.getAccesses()) {
            if (access instanceof IrExpr.FieldAccess field) {
                path.add(new Val.Field(field.getField()));
            } else {
                Object index = evaluate(((IrExpr.IndexAccess) access).getIndex(), at);
                path.add(new Val.Index(Val.asInteger(index).max(BigInteger.ZERO)));
            }
        }
        return path;
    }

    private BigInteger duration(TimeRef duration) {
        return switch (duration.getKind()) {
            case TIME_LITERAL -> BigInteger.valueOf(IsabelleRenderer.parseTimeMillis(duration.getText()));
            case INTEGER -> BigInteger.valueOf(IsabelleRenderer.parseInteger(duration.getText()));
            case NAME -> Val.theNat(state.getVarVal(duration.getText(), List.of()));
        };
    }

    // ------------------------------------------------------------------ inputs

    private Object randomValue(IrType type) {
        return switch (Terms.sortOf(type)) {
            case BOOL -> random.nextBoolean();
            case INT -> randomNumber();
            case NAT -> randomNumber().abs();
            case REAL -> random.nextDouble() * 200 - 50;
        };
    }

    private BigInteger randomNumber() {
        if (!interesting.isEmpty() && random.nextInt(3) > 0) {
            BigInteger near = interesting.get(random.nextInt(interesting.size()));
            return near.add(BigInteger.valueOf(random.nextInt(3) - 1));
        }
        return BigInteger.valueOf(random.nextInt(351) - 50);
    }

    /** The numbers the program's own code mentions, which its guards compare against. */
    private void collectInterestingNumbers() {
        Set<BigInteger> numbers = new LinkedHashSet<>(List.of(BigInteger.ZERO, BigInteger.ONE));
        program.getConstants().forEach(c -> literals(c.getValue(), numbers));
        program.getNodes().forEach(n -> n.getConstants().forEach(c -> literals(c.getValue(), numbers)));
        for (CfgNode node : cfg.nodes()) {
            if (node instanceof CfgNode.Guard guard) {
                literals(guard.getCondition(), numbers);
            } else if (node instanceof CfgNode.Assign assign) {
                literals(assign.getValue(), numbers);
            }
        }
        interesting.addAll(numbers);
    }

    private static void literals(IrExpr expr, Set<BigInteger> into) {
        if (expr instanceof IrExpr.Literal literal && literal.getKind() == IrExpr.Literal.Kind.INTEGER) {
            into.add(BigInteger.valueOf(IsabelleRenderer.parseInteger(literal.getText())));
        } else if (expr instanceof IrExpr.Binary binary) {
            literals(binary.getLeft(), into);
            literals(binary.getRight(), into);
        } else if (expr instanceof IrExpr.Unary unary) {
            literals(unary.getOperand(), into);
        } else if (expr instanceof IrExpr.Cast cast) {
            literals(cast.getOperand(), into);
        }
    }
}
