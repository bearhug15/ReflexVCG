package su.nsk.iae.reflex.inv;

import java.util.List;
import java.util.Set;

/**
 * One way of finding extra invariants: it guesses candidates, the checker keeps those that
 * are inductive, and it turns the survivors into invariants.
 *
 * <p>This is the extension point. A source knows nothing about paths or proofs; the
 * checker knows nothing about what a candidate means. Whatever a source guesses is safe to
 * guess - a wrong candidate is dropped, and a surviving one still has to be proved by the
 * conditions generation emits for it - so a source may be as speculative as it likes.
 *
 * <p>A source that also implements {@link EntryCollector} is shown every transition on
 * every possible path once the candidates are settled, and may build from those as well.
 *
 * @param <C> the candidates it guesses
 */
public interface CandidateSource<C extends Candidate> {

    /** The kinds of invariant it builds. */
    Set<ExtraInvariant.Kind> kinds();

    /** Its guesses. Called once, before any checking. */
    List<C> guess(AnalysisContext context);

    /**
     * Adds invariants built from the guesses that survived, in the order they were guessed,
     * to {@code into}.
     */
    void build(List<C> survivors, AnalysisContext context, ExtraInvariants into);
}
