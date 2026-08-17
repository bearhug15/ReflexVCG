package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * The place where extra invariants, annotation-driven generation and post-processing of
 * generated conditions will live.
 *
 * <p>Every hook is a no-op today, deliberately: the aim for now is that these are
 * <em>reachable</em>, each with a real call site in the pipeline, so implementing one
 * later does not mean re-plumbing the generator. The three things it is meant to grow
 * into:
 *
 * <ul>
 *   <li>{@link #extraDefinitions()} - definitions derived from Reflex-AL annotations, or
 *       from properties of the program, written alongside the requirements.</li>
 *   <li>{@link #process(VerificationCondition)} - additional processing of each generated
 *       condition, for instance strengthening it with an annotation bound to the state
 *       the path went through.</li>
 *   <li>{@link #analyse()} - additional analysis over the graph, whose results the other
 *       two can then draw on.</li>
 * </ul>
 *
 * <p>Nothing generates Isabelle from annotations yet: the temporal operators of Reflex-AL
 * quantify over execution history, which the theory has no model for. The annotations are
 * parsed and bound and available here, which is as far as that goes for now.
 */
public class ExtraInvariantGenerator {

    private final IrProgram program;
    private final Cfg cfg;
    private final AnnotationBinder annotations;

    public ExtraInvariantGenerator(IrProgram program, Cfg cfg, AnnotationBinder annotations) {
        this.program = program;
        this.cfg = cfg;
        this.annotations = annotations;
    }

    protected IrProgram getProgram() {
        return program;
    }

    protected Cfg getCfg() {
        return cfg;
    }

    /** Annotations found in the source, whether or not they were bound to a construct. */
    protected List<Annotation> getAnnotations() {
        return annotations == null ? List.of() : annotations.getAllAnnotations();
    }

    /** Annotations of one kind, across the whole program. */
    protected List<Annotation> annotationsOfKind(Annotation.Kind kind) {
        List<Annotation> matching = new ArrayList<>();
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getKind() == kind) {
                matching.add(annotation);
            }
        }
        return matching;
    }

    /**
     * Extra analysis over the program graph, run once before conditions are generated.
     * Does nothing yet.
     */
    public void analyse() {
        // Intentionally empty.
    }

    /**
     * Definitions to write alongside the generated requirements, in the order given.
     * Empty for now.
     */
    public List<String> extraDefinitions() {
        return List.of();
    }

    /**
     * Additional processing of a generated condition, applied to each in turn. Returns
     * the condition to write, which may be the one passed in. Returning null drops it.
     *
     * <p>The identity for now.
     */
    public VerificationCondition process(VerificationCondition condition) {
        return condition;
    }
}
