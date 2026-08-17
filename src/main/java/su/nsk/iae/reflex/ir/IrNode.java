package su.nsk.iae.reflex.ir;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Base of every Reflex IR node.
 *
 * <p>The IR is deliberately a separate, mutable tree rather than the ANTLR parse tree.
 * The preprocessing passes described in Preprocessing.tex rewrite the program in place:
 * name mangling renames declarations and every use, cast insertion replaces expression
 * operands with cast nodes, and normalisation creates entirely new states. None of that
 * is expressible against an ANTLR tree.
 *
 * <p>Every node keeps a link back to the parse-tree context it came from, so diagnostics
 * can still point at source positions, and carries the Reflex-AL annotations bound to it.
 */
public abstract class IrNode {

    private ParserRuleContext source;
    private List<Annotation> annotations;

    /** The parse-tree context this node was lowered from; null for synthesised nodes. */
    public ParserRuleContext getSource() {
        return source;
    }

    public void setSource(ParserRuleContext source) {
        this.source = source;
    }

    /**
     * Annotations attached to this construct, never null. Empty for the overwhelming
     * majority of nodes, so the list is only allocated on demand.
     */
    public List<Annotation> getAnnotations() {
        return annotations == null ? List.of() : annotations;
    }

    public void addAnnotation(Annotation annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>(1);
        }
        annotations.add(annotation);
    }

    public boolean hasAnnotations() {
        return annotations != null && !annotations.isEmpty();
    }

    /** Copies source position and annotations from {@code other}. */
    protected void copyOriginFrom(IrNode other) {
        this.source = other.source;
        if (other.annotations != null) {
            this.annotations = new ArrayList<>(other.annotations);
        }
    }
}
