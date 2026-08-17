package su.nsk.iae.reflex.vc;

import java.util.ArrayList;
import java.util.List;

/**
 * One verification condition: a chain of assumptions describing a single path through the
 * program, and the state the conclusion is stated about.
 *
 * <p>Held symbolically. Rendering to Isabelle happens in {@link IsabelleRenderer}, and
 * the same condition could be rendered for another prover without regenerating it - which
 * is also what lets annotations and extra invariants be folded in after generation.
 */
public final class VerificationCondition {

    private final List<VcStatement> statements = new ArrayList<>();
    private String finalState;

    public List<VcStatement> getStatements() {
        return statements;
    }

    public void add(VcStatement statement) {
        statements.add(statement);
    }

    /** The state variable the conclusion is about, bound by the closing Final statement. */
    public String getFinalState() {
        return finalState;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }

    public int size() {
        return statements.size();
    }

    @Override
    public String toString() {
        return "VC(" + statements.size() + " assumptions, concludes about " + finalState + ")";
    }
}
