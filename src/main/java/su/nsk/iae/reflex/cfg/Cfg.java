package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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

    /**
     * All reachable nodes, in a deterministic order, including the bodies of loops.
     *
     * <p>A loop's body hangs off {@link CfgNode.LoopCut#getBodyEntry()} rather than being a
     * successor - the enclosing path steps over the loop - but it is still part of the
     * program, and something unsupported inside one has to be found before generation
     * starts rather than when the body is enumerated.
     */
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
            if (node instanceof CfgNode.LoopCut cut) {
                pending.addLast(cut.getBodyEntry());
            }
        }
        return new ArrayList<>(seen);
    }

    /** The nodes of one region: reachable by successors, not descending into loop bodies. */
    private static List<CfgNode> region(CfgNode from) {
        Set<CfgNode> seen = new LinkedHashSet<>();
        Deque<CfgNode> pending = new ArrayDeque<>();
        pending.add(from);
        while (!pending.isEmpty()) {
            CfgNode node = pending.removeFirst();
            if (!seen.add(node)) {
                continue;
            }
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

    /**
     * Graphviz export, for inspecting the graph a program produces.
     *
     * <p>Nodes are shaped and labelled by what they are - a process, a state, an
     * {@code if}, a guard, an assignment - so the picture reads as the program. A loop is
     * drawn as one: its body is a box of its own with an edge back to the loop, even though
     * the graph itself has no cycle, the body being proved separately.
     */
    public String toDot() {
        List<CfgNode> nodes = nodes();
        Map<CfgNode, Integer> ids = new LinkedHashMap<>();
        for (CfgNode node : nodes) {
            ids.put(node, ids.size());
        }

        StringBuilder dot = new StringBuilder("digraph program {\n");
        dot.append("  rankdir=TB;\n")
           .append("  node [fontname=\"Helvetica\", fontsize=10];\n")
           .append("  edge [fontname=\"Helvetica\", fontsize=9];\n\n");

        Set<CfgNode> drawn = new LinkedHashSet<>();
        appendRegion(dot, region(entry), ids, drawn, "  ", false);
        appendEdges(dot, nodes, ids);
        return dot.append("}\n").toString();
    }

    /**
     * Draws one region's nodes, putting each loop body it contains into a cluster of its
     * own. Nested loops nest their clusters the same way.
     */
    private void appendRegion(StringBuilder dot, List<CfgNode> nodes, Map<CfgNode, Integer> ids,
                              Set<CfgNode> drawn, String indent, boolean inLoopBody) {
        for (CfgNode node : nodes) {
            if (!drawn.add(node)) {
                continue;
            }
            // A loop body ends where the iteration does, not where the program does.
            String label = inLoopBody && node instanceof CfgNode.Exit
                    ? "end of iteration"
                    : node.describe();
            dot.append(indent).append("n").append(ids.get(node))
               .append(" [label=\"").append(escape(label)).append("\"")
               .append(styleOf(node)).append("];\n");

            if (node instanceof CfgNode.LoopCut cut) {
                dot.append(indent).append("subgraph cluster_").append(ids.get(node)).append(" {\n")
                   .append(indent).append("  label=\"one iteration of ")
                   .append(escape(cut.getInvariantName())).append("\";\n")
                   .append(indent).append("  style=dashed;\n")
                   .append(indent).append("  color=\"#7f7f7f\";\n")
                   .append(indent).append("  fontsize=10;\n");
                appendRegion(dot, region(cut.getBodyEntry()), ids, drawn, indent + "  ", true);
                dot.append(indent).append("}\n");
            }
        }
    }

    private void appendEdges(StringBuilder dot, List<CfgNode> nodes, Map<CfgNode, Integer> ids) {
        dot.append("\n");
        for (CfgNode node : nodes) {
            for (CfgNode successor : node.getSuccessors()) {
                dot.append("  n").append(ids.get(node))
                   .append(" -> n").append(ids.get(successor));
                // A loop's successors are what happens once it is done.
                dot.append(node instanceof CfgNode.LoopCut ? " [label=\"loop done\"];\n" : ";\n");
            }
            if (node instanceof CfgNode.LoopCut cut) {
                dot.append("  n").append(ids.get(node))
                   .append(" -> n").append(ids.get(cut.getBodyEntry()))
                   .append(" [label=\"iterate\", style=dashed];\n");
                // The back edge is drawn but not laid out, so the body stays below the
                // loop rather than being pulled around it.
                for (CfgNode end : region(cut.getBodyEntry())) {
                    if (end.isTerminal()) {
                        dot.append("  n").append(ids.get(end))
                           .append(" -> n").append(ids.get(node))
                           .append(" [label=\"repeat\", style=dashed, constraint=false];\n");
                    }
                }
            }
        }
    }

    /** Shape and colour by what the node is, so the kinds are told apart at a glance. */
    private static String styleOf(CfgNode node) {
        if (node instanceof CfgNode.Entry || node instanceof CfgNode.Exit) {
            return ", shape=circle, style=filled, fillcolor=\"#d9d9d9\"";
        }
        if (node instanceof CfgNode.ToEnv) {
            return ", shape=box, style=filled, fillcolor=\"#d9d9d9\"";
        }
        if (node instanceof CfgNode.Guard || node instanceof CfgNode.TimeoutGuard) {
            return ", shape=diamond";
        }
        if (node instanceof CfgNode.InState) {
            return ", shape=box, style=\"rounded,filled\", fillcolor=\"#e8f0fe\"";
        }
        if (node instanceof CfgNode.LoopCut) {
            return ", shape=hexagon, style=filled, fillcolor=\"#fff3cd\"";
        }
        if (node instanceof CfgNode.Check) {
            return ", shape=note, style=filled, fillcolor=\"#e6f4ea\"";
        }
        if (node instanceof CfgNode.Unsupported) {
            return ", shape=octagon, style=filled, fillcolor=\"#fce8e6\"";
        }
        if (node instanceof CfgNode.Join) {
            return ", shape=ellipse, style=dashed, color=\"#7f7f7f\"";
        }
        // Assignments, state changes, timer resets: the things that do something.
        return ", shape=box";
    }

    private static String escape(String label) {
        return label.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
