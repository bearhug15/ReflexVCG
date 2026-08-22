package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.ir.IrCopier;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates an expression into the ways it can run, the writes it performs, and its value.
 *
 * <p>Reflex takes its expression semantics from C, and two consequences drive this class:
 *
 * <ul>
 *   <li><b>A write may sit anywhere inside an expression.</b> {@code total = count++ + count}
 *       writes in the middle of a sum, {@code if (m && (light = LOW))} writes inside a
 *       condition. Each write is a state of its own, so the reads around it do not all
 *       happen in the same state, and the expression cannot be rendered against one.</li>
 *   <li><b>An expression statement need not be an assignment.</b> Whatever it is, its
 *       writes still happen and its value is then discarded.</li>
 * </ul>
 *
 * <p>An expression therefore lowers to a <em>sequence</em>: the writes it performs, in
 * order, and a value read against the state they leave behind. That sequence is returned
 * as {@link CfgNode}s, so the graph carries it the way it carries everything else.
 *
 * <p><b>Floating reads.</b> A read carries no state until something needs its value. Until
 * then it floats, and is <em>fixed</em> at whatever state is current then. This is what
 * separates {@code count++} from {@code ++count}: the first fixes its read before its own
 * write and so yields the old value, the second leaves it floating past the write and so
 * yields the new one. A read fixed once never moves again.
 *
 * <p><b>The incoming stage.</b> Evaluation is told how many writes are already behind it,
 * so a subexpression knows what ran before it: the right operand of
 * {@code count++ + count} is evaluated knowing the increment has happened, and the read it
 * is forced to make is therefore of the new value. Without this a cast or a negation
 * around that operand would fix it too early, since those need a value and so fix what
 * they wrap.
 *
 * <p><b>Several results.</b> One expression can evaluate more than one way, and each way is
 * a separate path with its own writes:
 *
 * <ul>
 *   <li>{@code &&} and {@code ||} short-circuit, so {@code a && b} either stops at a false
 *       {@code a} - never running {@code b}, nor the writes inside it - or goes on;</li>
 *   <li>an operand that itself evaluates several ways multiplies them out.</li>
 * </ul>
 *
 * <p>A leaf yields exactly one way, with no guards and no writes.
 */
final class ExprLowering {

    /**
     * One way an expression can evaluate.
     *
     * @param steps what running it does, in order: the guards that select this way and the
     *              writes it performs. Each {@link CfgNode.Assign} advances the state by
     *              one, which is what {@link IrExpr.At#getStepsBack()} counts.
     * @param value what it evaluates to, read against the state its steps leave behind
     * @param stage how many writes have happened once it has run, counted from the start
     *              of the whole expression rather than of this subexpression
     */
    record Outcome(List<CfgNode> steps, IrExpr value, int stage) {

        boolean isConstant(boolean expected) {
            return value instanceof IrExpr.Literal literal
                    && literal.getKind() == IrExpr.Literal.Kind.BOOL
                    && Boolean.parseBoolean(literal.getText()) == expected;
        }

        /** Whether running this way leaves the program state alone. */
        boolean isPure() {
            return steps.stream().noneMatch(step -> step instanceof CfgNode.Assign);
        }
    }

    private ExprLowering() {
    }

    // ------------------------------------------------------------------ entry point

    /**
     * The ways {@code expr} can evaluate, ready for the graph: every read fixed, and every
     * pin stated relative to the node that carries it.
     */
    static List<Outcome> lower(IrExpr expr) {
        List<Outcome> lowered = new ArrayList<>();
        for (Outcome outcome : evaluate(expr, 0)) {
            lowered.add(relativise(new Outcome(outcome.steps(),
                    fix(outcome.value(), outcome.stage()), outcome.stage())));
        }
        return lowered;
    }

    // ------------------------------------------------------------------ evaluation

    /**
     * The ways {@code expr} can evaluate when it is reached with {@code stageIn} writes
     * already behind it, with reads left floating wherever nothing has needed their value
     * yet and every fixed read holding the absolute stage it was fixed at.
     * {@link #relativise} turns those stages into offsets once the total is known.
     */
    private static List<Outcome> evaluate(IrExpr expr, int stageIn) {
        if (expr instanceof IrExpr.Binary binary) {
            if (binary.getOp() == IrExpr.BinaryOp.AND) {
                return evaluateShortCircuit(binary, stageIn, false);
            }
            if (binary.getOp() == IrExpr.BinaryOp.OR) {
                return evaluateShortCircuit(binary, stageIn, true);
            }
            return evaluateBinary(binary, stageIn);
        }
        if (expr instanceof IrExpr.Unary unary) {
            return map(evaluate(unary.getOperand(), stageIn), operand -> {
                IrExpr value = new IrExpr.Unary(unary.getOp(),
                        fix(operand.value(), operand.stage()));
                value.setResultType(unary.getResultType());
                return value;
            });
        }
        if (expr instanceof IrExpr.Cast cast) {
            return map(evaluate(cast.getOperand(), stageIn), operand -> {
                IrExpr value = new IrExpr.Cast(cast.getTargetType(),
                        fix(operand.value(), operand.stage()),
                        cast.getPreType(), cast.isImplicit());
                value.setResultType(cast.getResultType());
                return value;
            });
        }
        if (expr instanceof IrExpr.Assign assign) {
            return evaluateAssign(assign, stageIn);
        }
        if (expr instanceof IrExpr.IncDec incDec) {
            return evaluateIncDec(incDec, stageIn);
        }
        if (expr instanceof IrExpr.Call call) {
            return evaluateCall(call, stageIn);
        }
        // A leaf, or a subtree with nothing inside that runs: its reads stay floating.
        return List.of(new Outcome(List.of(), expr, stageIn));
    }

    /**
     * Both operands run, so the ways each can evaluate multiply out. The right operand
     * runs against what the left leaves behind, and the reads of both are then fixed
     * against what the pair leaves behind: C sequences neither before the other, so a read
     * not already fixed by a write of its own sees the state after both.
     */
    private static List<Outcome> evaluateBinary(IrExpr.Binary binary, int stageIn) {
        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome left : evaluate(binary.getLeft(), stageIn)) {
            for (Outcome right : evaluate(binary.getRight(), left.stage())) {
                IrExpr value = new IrExpr.Binary(binary.getOp(),
                        fix(left.value(), right.stage()), fix(right.value(), right.stage()));
                value.setResultType(binary.getResultType());
                outcomes.add(new Outcome(
                        concat(left.steps(), right.steps()), value, right.stage()));
            }
        }
        return outcomes;
    }

    /**
     * {@code a && b} and {@code a || b}. {@code shortCircuitOn} is the value of the left
     * operand that settles the result without running the right one - true for {@code ||},
     * false for {@code &&}.
     *
     * <p>The right operand's writes belong to the way that runs it and to no other, which
     * is the whole point of short-circuiting and the reason a write inside one has to be
     * modelled rather than lifted out.
     */
    private static List<Outcome> evaluateShortCircuit(IrExpr.Binary binary, int stageIn,
                                                      boolean shortCircuitOn) {
        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome left : evaluate(binary.getLeft(), stageIn)) {
            // Choosing which way to go reads the left operand, which fixes it here.
            IrExpr decided = fix(left.value(), left.stage());

            // The left operand settled it; the right never runs.
            if (!left.isConstant(!shortCircuitOn)) {
                List<CfgNode> steps = new ArrayList<>(left.steps());
                steps.add(new CfgNode.Guard(
                        shortCircuitOn ? IrCopier.copy(decided) : not(decided)));
                outcomes.add(new Outcome(steps, literal(shortCircuitOn), left.stage()));
            }

            // Otherwise the result is whatever the right operand evaluates to.
            if (left.isConstant(shortCircuitOn)) {
                continue;
            }
            for (Outcome right : evaluate(binary.getRight(), left.stage())) {
                List<CfgNode> steps = new ArrayList<>(left.steps());
                steps.add(new CfgNode.Guard(
                        shortCircuitOn ? not(decided) : IrCopier.copy(decided)));
                steps.addAll(right.steps());
                outcomes.add(new Outcome(steps,
                        fix(right.value(), right.stage()), right.stage()));
            }
        }
        return outcomes;
    }

    /**
     * An assignment. The value is worked out first and fixed against the state before the
     * write, since that is where it is read; the write follows; and the assignment itself
     * evaluates to the variable, left floating so an enclosing expression reads it in
     * whatever state that settles on. {@code a = b = c} and {@code if ((x = y))} both
     * follow from this.
     */
    private static List<Outcome> evaluateAssign(IrExpr.Assign assign, int stageIn) {
        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome path : evaluateAccesses(assign.getTarget(), stageIn)) {
            IrExpr.VarRef target = (IrExpr.VarRef) path.value();
            for (Outcome value : evaluate(assign.getValue(), path.stage())) {
                int beforeWrite = value.stage();
                IrExpr stored = stored(assign, target,
                        fix(value.value(), beforeWrite), beforeWrite);

                List<CfgNode> steps = concat(path.steps(), value.steps());
                steps.add(new CfgNode.Assign(target, stored));

                IrExpr result = new IrExpr.VarRef(target.getName(), target.getAccesses());
                result.setResultType(target.getResultType());
                outcomes.add(new Outcome(steps, result, beforeWrite + 1));
            }
        }
        return outcomes;
    }

    /**
     * {@code v++} and {@code ++v}. Both write the same thing and differ only in when the
     * value they produce is read: a postfix read is fixed before the write and so is the
     * old value, a prefix read floats past it and so is the new one.
     */
    private static List<Outcome> evaluateIncDec(IrExpr.IncDec incDec, int stageIn) {
        IrExpr.BinaryOp op = incDec.getOp() == IrExpr.IncDecOp.INCREMENT
                ? IrExpr.BinaryOp.ADD
                : IrExpr.BinaryOp.SUB;

        List<Outcome> outcomes = new ArrayList<>();
        for (Outcome path : evaluateAccesses(incDec.getTarget(), stageIn)) {
            IrExpr.VarRef target = (IrExpr.VarRef) path.value();

            IrExpr one = new IrExpr.Literal(IrExpr.Literal.Kind.INTEGER, "1");
            one.setResultType(target.getResultType());
            IrExpr updated = new IrExpr.Binary(op,
                    fix(IrCopier.copy(target), path.stage()), one);
            updated.setResultType(target.getResultType());

            List<CfgNode> steps = new ArrayList<>(path.steps());
            steps.add(new CfgNode.Assign(target, updated));

            IrExpr value = new IrExpr.VarRef(target.getName(), target.getAccesses());
            value.setResultType(target.getResultType());
            outcomes.add(new Outcome(steps,
                    incDec.isPrefix() ? value : fix(value, path.stage()),
                    path.stage() + 1));
        }
        return outcomes;
    }

    /** A call runs its arguments in order and has no effect of its own. */
    private static List<Outcome> evaluateCall(IrExpr.Call call, int stageIn) {
        List<Sequence> sequences = Sequence.start(stageIn);
        for (IrExpr argument : call.getArguments()) {
            sequences = Sequence.then(sequences, argument);
        }

        List<Outcome> outcomes = new ArrayList<>();
        for (Sequence sequence : sequences) {
            List<IrExpr> arguments = new ArrayList<>();
            sequence.values().forEach(a -> arguments.add(fix(a, sequence.stage())));
            IrExpr value = new IrExpr.Call(call.getFunction(), arguments);
            value.setResultType(call.getResultType());
            outcomes.add(new Outcome(sequence.steps(), value, sequence.stage()));
        }
        return outcomes;
    }

    /**
     * The index expressions of a target's access path, which run like any other. Yields
     * the target with its indices resolved, so {@code a[i++] = 0} increments once.
     */
    private static List<Outcome> evaluateAccesses(IrExpr.VarRef target, int stageIn) {
        boolean writesInside = target.getAccesses().stream()
                .anyMatch(access -> access instanceof IrExpr.IndexAccess index
                        && writes(index.getIndex()));
        if (!writesInside) {
            return List.of(new Outcome(List.of(), target, stageIn));
        }

        List<Sequence> sequences = Sequence.start(stageIn);
        for (IrExpr.Access access : target.getAccesses()) {
            sequences = access instanceof IrExpr.IndexAccess index
                    ? Sequence.then(sequences, index.getIndex())
                    : Sequence.thenNothing(sequences);
        }

        List<Outcome> outcomes = new ArrayList<>();
        for (Sequence sequence : sequences) {
            List<IrExpr.Access> path = new ArrayList<>();
            for (int i = 0; i < target.getAccesses().size(); i++) {
                IrExpr index = sequence.values().get(i);
                path.add(index == null
                        ? target.getAccesses().get(i)
                        : new IrExpr.IndexAccess(fix(index, sequence.stage())));
            }
            IrExpr.VarRef resolved = new IrExpr.VarRef(target.getName(), path);
            resolved.setResultType(target.getResultType());
            outcomes.add(new Outcome(sequence.steps(), resolved, sequence.stage()));
        }
        return outcomes;
    }

    /** A compound assignment stores {@code target op value}, not just {@code value}. */
    private static IrExpr stored(IrExpr.Assign assign, IrExpr.VarRef target,
                                 IrExpr value, int stage) {
        IrExpr.BinaryOp underlying = assign.getOp().underlying();
        if (underlying == null) {
            return value;
        }
        IrExpr combined = new IrExpr.Binary(underlying,
                fix(IrCopier.copy(target), stage), value);
        combined.setResultType(target.getResultType());
        return combined;
    }

    /**
     * Several subexpressions run one after another, each against what the last left
     * behind. Used for the arguments of a call and for the indices of an access path,
     * both of which multiply out the way an operand pair does.
     */
    private record Sequence(List<CfgNode> steps, List<IrExpr> values, int stage) {

        static List<Sequence> start(int stageIn) {
            return List.of(new Sequence(List.of(), List.of(), stageIn));
        }

        static List<Sequence> then(List<Sequence> sequences, IrExpr next) {
            List<Sequence> extended = new ArrayList<>();
            for (Sequence sequence : sequences) {
                for (Outcome outcome : evaluate(next, sequence.stage())) {
                    List<IrExpr> values = new ArrayList<>(sequence.values());
                    values.add(outcome.value());
                    extended.add(new Sequence(concat(sequence.steps(), outcome.steps()),
                            values, outcome.stage()));
                }
            }
            return extended;
        }

        /** Records a position that runs nothing, keeping the values aligned by index. */
        static List<Sequence> thenNothing(List<Sequence> sequences) {
            List<Sequence> extended = new ArrayList<>();
            for (Sequence sequence : sequences) {
                List<IrExpr> values = new ArrayList<>(sequence.values());
                values.add(null);
                extended.add(new Sequence(sequence.steps(), values, sequence.stage()));
            }
            return extended;
        }
    }

    // ------------------------------------------------------------------ fixing reads

    /**
     * Fixes every read in {@code expr} that is still floating, at the state reached after
     * {@code stage} writes. A read already fixed keeps the state it was fixed at, which is
     * what makes {@code count++} read the old value while everything around it reads the
     * new one.
     *
     * <p>Stage 0 is a pin like any other: {@code count++ + count} fixes its first read
     * before any write has happened, and that is exactly what makes it the old value.
     * {@link #relativise} drops the pins that turn out to name the state the node is
     * rendered against anyway, so an expression that writes nothing keeps none of them.
     */
    private static IrExpr fix(IrExpr expr, int stage) {
        return expr == null ? null : pin(expr, stage);
    }

    private static IrExpr pin(IrExpr expr, int stage) {
        if (expr instanceof IrExpr.At) {
            // Fixed already, by a write of its own.
            return expr;
        }
        if (expr instanceof IrExpr.VarRef || expr instanceof IrExpr.CheckState) {
            IrExpr at = new IrExpr.At(expr, stage);
            at.setResultType(expr.getResultType());
            return at;
        }
        if (expr instanceof IrExpr.Binary binary) {
            IrExpr value = new IrExpr.Binary(binary.getOp(),
                    pin(binary.getLeft(), stage), pin(binary.getRight(), stage));
            value.setResultType(binary.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Unary unary) {
            IrExpr value = new IrExpr.Unary(unary.getOp(), pin(unary.getOperand(), stage));
            value.setResultType(unary.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Cast cast) {
            IrExpr value = new IrExpr.Cast(cast.getTargetType(), pin(cast.getOperand(), stage),
                    cast.getPreType(), cast.isImplicit());
            value.setResultType(cast.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Call call) {
            List<IrExpr> arguments = new ArrayList<>();
            call.getArguments().forEach(a -> arguments.add(pin(a, stage)));
            IrExpr value = new IrExpr.Call(call.getFunction(), arguments);
            value.setResultType(call.getResultType());
            return value;
        }
        // Literals and aggregates read nothing.
        return expr;
    }

    /**
     * Restates every pin as an offset from the node that carries it.
     *
     * <p>Evaluation records the stage a read was fixed at, counting writes forwards, since
     * how many more will follow is not yet known. A node is rendered against the state it
     * sits in, so what it needs is the distance back to that stage: a guard with {@code k}
     * writes before it and a read fixed at stage {@code s} reads {@code k - s} states back,
     * and the value of the {@code k + 1}th write likewise.
     */
    private static Outcome relativise(Outcome outcome) {
        List<CfgNode> steps = new ArrayList<>();
        int stage = 0;
        for (CfgNode step : outcome.steps()) {
            if (step instanceof CfgNode.Guard guard) {
                steps.add(new CfgNode.Guard(offset(guard.getCondition(), stage)));
            } else if (step instanceof CfgNode.Assign assign) {
                steps.add(new CfgNode.Assign(
                        (IrExpr.VarRef) offset(assign.getTarget(), stage),
                        offset(assign.getValue(), stage)));
                stage++;
            } else {
                steps.add(step);
            }
        }
        return new Outcome(steps, offset(outcome.value(), outcome.stage()), outcome.stage());
    }

    /** Rewrites the pins in {@code expr}, which is rendered at stage {@code here}. */
    private static IrExpr offset(IrExpr expr, int here) {
        if (expr instanceof IrExpr.At at) {
            int stepsBack = here - at.getStepsBack();
            if (stepsBack == 0) {
                // Read in the very state the node sits in; the pin says nothing.
                return at.getOperand();
            }
            IrExpr moved = new IrExpr.At(at.getOperand(), stepsBack);
            moved.setResultType(at.getResultType());
            return moved;
        }
        if (expr instanceof IrExpr.VarRef ref) {
            if (ref.getAccesses().isEmpty()) {
                return ref;
            }
            List<IrExpr.Access> path = new ArrayList<>();
            for (IrExpr.Access access : ref.getAccesses()) {
                path.add(access instanceof IrExpr.IndexAccess index
                        ? new IrExpr.IndexAccess(offset(index.getIndex(), here))
                        : access);
            }
            IrExpr.VarRef moved = new IrExpr.VarRef(ref.getName(), path);
            moved.setResultType(ref.getResultType());
            return moved;
        }
        if (expr instanceof IrExpr.Binary binary) {
            IrExpr value = new IrExpr.Binary(binary.getOp(),
                    offset(binary.getLeft(), here), offset(binary.getRight(), here));
            value.setResultType(binary.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Unary unary) {
            IrExpr value = new IrExpr.Unary(unary.getOp(), offset(unary.getOperand(), here));
            value.setResultType(unary.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Cast cast) {
            IrExpr value = new IrExpr.Cast(cast.getTargetType(), offset(cast.getOperand(), here),
                    cast.getPreType(), cast.isImplicit());
            value.setResultType(cast.getResultType());
            return value;
        }
        if (expr instanceof IrExpr.Call call) {
            List<IrExpr> arguments = new ArrayList<>();
            call.getArguments().forEach(a -> arguments.add(offset(a, here)));
            IrExpr value = new IrExpr.Call(call.getFunction(), arguments);
            value.setResultType(call.getResultType());
            return value;
        }
        return expr;
    }

    /** Whether an expression writes to anything: an assignment, or an increment. */
    static boolean writes(IrExpr expr) {
        if (expr == null) {
            return false;
        }
        if (expr instanceof IrExpr.Assign || expr instanceof IrExpr.IncDec) {
            return true;
        }
        if (expr instanceof IrExpr.Binary binary) {
            return writes(binary.getLeft()) || writes(binary.getRight());
        }
        if (expr instanceof IrExpr.Unary unary) {
            return writes(unary.getOperand());
        }
        if (expr instanceof IrExpr.Cast cast) {
            return writes(cast.getOperand());
        }
        if (expr instanceof IrExpr.Call call) {
            return call.getArguments().stream().anyMatch(ExprLowering::writes);
        }
        if (expr instanceof IrExpr.VarRef ref) {
            return ref.getAccesses().stream()
                    .anyMatch(access -> access instanceof IrExpr.IndexAccess index
                            && writes(index.getIndex()));
        }
        return false;
    }

    // ------------------------------------------------------------------ helpers

    /** Rebuilds each outcome's value, leaving its steps and its stage alone. */
    private static List<Outcome> map(List<Outcome> outcomes, ValueBuilder builder) {
        List<Outcome> mapped = new ArrayList<>();
        for (Outcome outcome : outcomes) {
            mapped.add(new Outcome(outcome.steps(), builder.build(outcome), outcome.stage()));
        }
        return mapped;
    }

    private interface ValueBuilder {
        IrExpr build(Outcome operand);
    }

    private static List<CfgNode> concat(List<CfgNode> first, List<CfgNode> second) {
        List<CfgNode> steps = new ArrayList<>(first);
        steps.addAll(second);
        return steps;
    }

    static IrExpr not(IrExpr expr) {
        IrExpr negated = new IrExpr.Unary(IrExpr.UnaryOp.NOT, IrCopier.copy(expr));
        negated.setResultType(IrType.BOOL);
        return negated;
    }

    static IrExpr literal(boolean value) {
        IrExpr literal = new IrExpr.Literal(IrExpr.Literal.Kind.BOOL, Boolean.toString(value));
        literal.setResultType(IrType.BOOL);
        return literal;
    }

    /** Combines guards into one condition; null when there are none. */
    static IrExpr conjunction(List<IrExpr> guards) {
        IrExpr result = null;
        for (IrExpr guard : guards) {
            result = result == null ? guard : and(result, guard);
        }
        return result;
    }

    static IrExpr and(IrExpr left, IrExpr right) {
        IrExpr conjunction = new IrExpr.Binary(IrExpr.BinaryOp.AND, left, right);
        conjunction.setResultType(IrType.BOOL);
        return conjunction;
    }
}
