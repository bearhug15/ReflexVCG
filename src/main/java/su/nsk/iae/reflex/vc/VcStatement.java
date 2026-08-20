package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.term.Term;

/**
 * One assumption of a verification condition, held symbolically.
 *
 * <p>The old pipeline built these as Isabelle strings while walking the AST, which fused
 * code generation into graph building and left nothing later stages could inspect. Here a
 * statement records <em>what</em> happened - a variable was assigned, a process changed
 * state, the program yielded to the environment - and rendering it is a separate step.
 *
 * <p>Each statement that advances the program state names the state variable it produces
 * ({@code target}) and the one it consumes ({@code source}), so a condition is a chain
 * {@code st0, st1, ... st_final}.
 */
public sealed interface VcStatement {

    /** The invariant is assumed to hold in the state the condition starts from. */
    record Invariant(String state) implements VcStatement {
    }

    /** {@code getPstate st ''P'' = ''s''}: the process is in a particular state. */
    record ProcessInState(String state, String process, String pstate) implements VcStatement {
    }

    /**
     * A formula an annotation contributes. An {@code assume} adds one to the main
     * condition; a loop invariant adds one for the state after the loop.
     */
    record Assumption(String label, Term formula) implements VcStatement {
    }

    /**
     * A state the condition knows nothing about beyond what is assumed of it - the
     * state a loop leaves behind, whose body is proved separately.
     */
    record OpaqueState(String target, String source) implements VcStatement {
    }

    /** A path condition: the branch taken required this to hold. */
    record Condition(String state, IrExpr expr) implements VcStatement {
    }

    /** A variable assignment, including the access path being written through. */
    record Assign(String target, String source, IrExpr.VarRef variable, IrExpr value)
            implements VcStatement {
    }

    /** A process moved to another state, including stop and error. */
    record SetProcessState(String target, String source, String process, String pstate)
            implements VcStatement {
    }

    /** The process timer was reset. */
    record ResetTimer(String target, String source, String process) implements VcStatement {
    }

    /** Control returned to the environment, ending a cycle. */
    record ToEnv(String target, String source) implements VcStatement {
    }

    /** The starting point of a condition that begins from an empty state. */
    record EmptyState(String target) implements VcStatement {
    }

    /** Binds the final state variable, which the conclusion is stated about. */
    record Final(String target, String source) implements VcStatement {
    }

    /**
     * A timeout branch: whether the time spent in the state has reached the timeout.
     * {@code exceeded} distinguishes the branch that fires from the one that does not.
     */
    record TimeoutCheck(String state, String process, TimeRef duration, boolean exceeded)
            implements VcStatement {
    }
}
