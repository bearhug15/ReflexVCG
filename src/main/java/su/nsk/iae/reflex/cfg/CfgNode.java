package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.analysis.Attributes;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.TimeRef;

import java.util.ArrayList;
import java.util.List;

/**
 * A node of the program control-flow graph.
 *
 * <p>Nodes carry IR, not rendered text. The previous graph stored Isabelle strings
 * produced during graph building, which is what fused code generation into the traversal
 * and left later stages nothing to inspect.
 *
 * <p>Branching is expressed by a node having several successors, each beginning with a
 * {@link Guard}; following a guard means assuming its condition. A path through the graph
 * is therefore a conjunction of the guards it passed and a sequence of the effects it
 * performed.
 */
public abstract class CfgNode {

    private final List<CfgNode> successors = new ArrayList<>();
    private Attributes attributes = Attributes.EMPTY;

    public List<CfgNode> getSuccessors() {
        return successors;
    }

    public void addSuccessor(CfgNode node) {
        successors.add(node);
    }

    /**
     * What passing through this node does to the processes, for static analysis.
     * Only the nodes that actually change something carry a non-empty value; the
     * summary attributes of a composite statement are not applied here, since a path
     * goes through one branch of it rather than all.
     */
    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public boolean isTerminal() {
        return successors.isEmpty();
    }

    /** Short description used for graph export and debugging. */
    public abstract String describe();

    @Override
    public String toString() {
        return describe();
    }

    // ------------------------------------------------------------------ node kinds

    /** Start of the graph. */
    public static final class Entry extends CfgNode {
        @Override
        public String describe() {
            return "entry";
        }
    }

    /** End of a cycle: control returns to the environment. */
    public static final class ToEnv extends CfgNode {
        @Override
        public String describe() {
            return "toEnv";
        }
    }

    /** End of the graph, after the environment step. */
    public static final class Exit extends CfgNode {
        @Override
        public String describe() {
            return "exit";
        }
    }

    /** Carries no effect; used to join branches back together. */
    public static final class Join extends CfgNode {
        @Override
        public String describe() {
            return "join";
        }
    }

    /**
     * Assumes a process is in a particular state. Every path through a process passes
     * exactly one of these, one per state the process might be in.
     */
    public static final class InState extends CfgNode {
        private final String process;
        private final String state;

        public InState(String process, String state) {
            this.process = process;
            this.state = state;
        }

        public String getProcess() {
            return process;
        }

        public String getState() {
            return state;
        }

        @Override
        public String describe() {
            return process + " in " + state;
        }
    }

    /** A path condition. */
    public static final class Guard extends CfgNode {
        private final IrExpr condition;

        public Guard(IrExpr condition) {
            this.condition = condition;
        }

        public IrExpr getCondition() {
            return condition;
        }

        @Override
        public String describe() {
            return "guard " + condition;
        }
    }

    /** The two sides of a state's timeout clause. */
    public static final class TimeoutGuard extends CfgNode {
        private final String process;
        private final TimeRef duration;
        private final boolean exceeded;

        public TimeoutGuard(String process, TimeRef duration, boolean exceeded) {
            this.process = process;
            this.duration = duration;
            this.exceeded = exceeded;
        }

        public String getProcess() {
            return process;
        }

        public TimeRef getDuration() {
            return duration;
        }

        public boolean isExceeded() {
            return exceeded;
        }

        @Override
        public String describe() {
            return (exceeded ? "timeout reached " : "timeout not reached ") + duration;
        }
    }

    /** A variable assignment. */
    public static final class Assign extends CfgNode {
        private final IrExpr.VarRef target;
        private final IrExpr value;

        public Assign(IrExpr.VarRef target, IrExpr value) {
            this.target = target;
            this.value = value;
        }

        public IrExpr.VarRef getTarget() {
            return target;
        }

        public IrExpr getValue() {
            return value;
        }

        @Override
        public String describe() {
            return target + " := " + value;
        }
    }

    /** A process moves to a state, including the stop and error states. */
    public static final class SetState extends CfgNode {
        private final String process;
        private final String state;

        public SetState(String process, String state) {
            this.process = process;
            this.state = state;
        }

        public String getProcess() {
            return process;
        }

        public String getState() {
            return state;
        }

        @Override
        public String describe() {
            return process + " := " + state;
        }
    }

    /** The process timer is reset. */
    public static final class ResetTimer extends CfgNode {
        private final String process;

        public ResetTimer(String process) {
            this.process = process;
        }

        public String getProcess() {
            return process;
        }

        @Override
        public String describe() {
            return "reset " + process;
        }
    }

    /**
     * A construct VC generation does not support. Reaching one during traversal is
     * reported rather than silently producing a condition that omits its effect.
     */
    public static final class Unsupported extends CfgNode {
        private final String construct;
        private final String detail;

        public Unsupported(String construct, String detail) {
            this.construct = construct;
            this.detail = detail;
        }

        public String getConstruct() {
            return construct;
        }

        public String getDetail() {
            return detail;
        }

        @Override
        public String describe() {
            return "unsupported: " + construct;
        }
    }
}
