package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.ir.IrExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * A fact a path has asserted, in the form the analysis rules reason about.
 */
public sealed interface Term {

    /** {@code getPstate s P = q}: process P is in state q. */
    record PstateCompare(String process, String pstate) implements Term {
    }

    /** {@code process P in state <activity>}, possibly negated. */
    record ProcessActivity(String process, Activity activity) implements Term {
    }

    /**
     * Process statuses the rules distinguish. The grammar offers four; negating a check
     * produces the other two, so {@code !(P in state stop)} is {@link #NONSTOP} rather
     * than a negated STOP.
     */
    enum Activity {
        ACTIVE, INACTIVE, STOP, ERROR, NONSTOP, NONERROR;

        public Activity negate() {
            return switch (this) {
                case ACTIVE -> INACTIVE;
                case INACTIVE -> ACTIVE;
                case STOP -> NONSTOP;
                case NONSTOP -> STOP;
                case ERROR -> NONERROR;
                case NONERROR -> ERROR;
            };
        }

        static Activity of(IrExpr.ProcessStatus status) {
            return switch (status) {
                case ACTIVE -> ACTIVE;
                case INACTIVE -> INACTIVE;
                case STOP -> STOP;
                case ERROR -> ERROR;
            };
        }
    }

    /**
     * The process-status facts a condition asserts.
     *
     * <p>Only conjunctions contribute: a disjunct is not asserted by the path, so
     * {@code a || P in state stop} tells us nothing about P. Under negation the
     * connectives swap, so the negation of a disjunction does contribute both sides.
     */
    static List<Term> assertedBy(IrExpr condition) {
        List<Term> terms = new ArrayList<>();
        collect(condition, true, terms);
        return terms;
    }

    private static void collect(IrExpr expr, boolean positive, List<Term> terms) {
        if (expr instanceof IrExpr.CheckState check) {
            Activity activity = Activity.of(check.getStatus());
            terms.add(new ProcessActivity(check.getProcess(), positive ? activity : activity.negate()));
            return;
        }
        if (expr instanceof IrExpr.Unary unary && unary.getOp() == IrExpr.UnaryOp.NOT) {
            collect(unary.getOperand(), !positive, terms);
            return;
        }
        if (expr instanceof IrExpr.Binary binary) {
            boolean conjunction = binary.getOp() == IrExpr.BinaryOp.AND;
            boolean disjunction = binary.getOp() == IrExpr.BinaryOp.OR;
            // A conjunction asserts both sides; a negated disjunction asserts both
            // negations. The other two cases assert neither.
            if (conjunction && positive || disjunction && !positive) {
                collect(binary.getLeft(), positive, terms);
                collect(binary.getRight(), positive, terms);
            }
            return;
        }
        if (expr instanceof IrExpr.Cast cast) {
            collect(cast.getOperand(), positive, terms);
            return;
        }
        if (expr instanceof IrExpr.At at) {
            // A read pinned to an earlier state, because a write inside the expression came
            // between. Which state it is read in does not change what it asserts.
            collect(at.getOperand(), positive, terms);
        }
    }
}
