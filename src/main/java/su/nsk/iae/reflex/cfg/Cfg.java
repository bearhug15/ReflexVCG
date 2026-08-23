package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The control-flow graph of one execution cycle, with its entry and exit.
 *
 * <p>Every path from {@link #getEntry()} to {@link #getExit()} is one possible cycle, and
 * becomes one verification condition.
 */
public final class Cfg {

    /**
     * A loop's invariant: the name the conditions state it under, and what defines it.
     *
     * <p>Every loop gets a name, so a condition mentions {@code loopInv0 st3} rather than
     * carrying the formula itself. The theory holding those names then either defines one -
     * when an {@code [invariant: ...]} says what the loop preserves - or leaves it
     * uninterpreted, when nothing does. {@code line} is the loop's line in the source, so
     * the declaration can say which loop it belongs to.
     *
     * @param annotation what the loop was written to preserve, or null when nothing was
     */
    public record LoopInvariant(String name, int line, Annotation annotation) {

        /** Whether a formula defines this, as against it standing uninterpreted. */
        public boolean isDefined() {
            return annotation != null;
        }
    }

    private final CfgNode entry;
    private final CfgNode exit;
    private final IrProgram program;
    private final List<LoopInvariant> loopInvariants;

    public Cfg(CfgNode entry, CfgNode exit, IrProgram program) {
        this(entry, exit, program, List.of());
    }

    public Cfg(CfgNode entry, CfgNode exit, IrProgram program,
               List<LoopInvariant> loopInvariants) {
        this.entry = entry;
        this.exit = exit;
        this.program = program;
        this.loopInvariants = List.copyOf(loopInvariants);
    }

    /**
     * One entry per loop, in the order the loops appear. Each needs a name in the theory
     * the conditions are stated against.
     */
    public List<LoopInvariant> getLoopInvariants() {
        return loopInvariants;
    }

    public CfgNode getEntry() {
        return entry;
    }

    public CfgNode getExit() {
        return exit;
    }

    public IrProgram getProgram() {
        return program;
    }

    /** All reachable nodes, in a deterministic order. */
    public List<CfgNode> nodes() {
        Set<CfgNode> seen = new LinkedHashSet<>();
        Deque<CfgNode> pending = new ArrayDeque<>();
        pending.add(entry);
        while (!pending.isEmpty()) {
            CfgNode node = pending.removeFirst();
            if (!seen.add(node)) {
                continue;
            }
            // Pushed in reverse so successors are visited in declaration order.
            List<CfgNode> successors = node.getSuccessors();
            for (int i = successors.size() - 1; i >= 0; i--) {
                pending.addFirst(successors.get(i));
            }
        }
        return new ArrayList<>(seen);
    }

    /** Constructs the graph reached that VC generation cannot handle. */
    public List<CfgNode.Unsupported> unsupportedNodes() {
        List<CfgNode.Unsupported> unsupported = new ArrayList<>();
        for (CfgNode node : nodes()) {
            if (node instanceof CfgNode.Unsupported u) {
                unsupported.add(u);
            }
        }
        return unsupported;
    }

    /** Graphviz export, for inspecting the graph a program produces. */
    public String toDot() {
        List<CfgNode> nodes = nodes();
        StringBuilder dot = new StringBuilder("digraph program {\n");
        for (int i = 0; i < nodes.size(); i++) {
            dot.append("  n").append(i).append(" [label=\"")
               .append(nodes.get(i).describe().replace("\"", "\\\""))
               .append("\"];\n");
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (CfgNode successor : nodes.get(i).getSuccessors()) {
                dot.append("  n").append(i).append(" -> n").append(nodes.indexOf(successor)).append(";\n");
            }
        }
        return dot.append("}\n").toString();
    }
}
