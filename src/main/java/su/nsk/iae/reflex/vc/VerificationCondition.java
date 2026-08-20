package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.term.Term;

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

    /** What a condition is for, which decides how it is named and what it shows. */
    public enum Kind {
        /** One cycle preserves the invariant, or the program starts satisfying it. */
        MAIN,
        /** An `assume` has to be discharged before it may be relied on. */
        ASSUME,
        /** An `assert` states something that must hold where it is written. */
        ASSERT,
        /** A loop invariant holds on entry. */
        LOOP_ENTRY,
        /** A loop invariant survives one iteration. */
        LOOP_PRESERVED
    }

    private final List<VcStatement> statements = new ArrayList<>();
    private String finalState;
    private Kind kind = Kind.MAIN;
    private Term conclusion;
    private String note;

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

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    /**
     * What the condition shows. Null means the invariant at the final state, which is
     * what a main condition concludes.
     */
    public Term getConclusion() {
        return conclusion;
    }

    public void setConclusion(Term conclusion) {
        this.conclusion = conclusion;
    }

    /** Where the condition came from, for the comment above the generated lemma. */
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /** A copy holding the same statements, for deriving an obligation from a prefix. */
    public VerificationCondition copy() {
        VerificationCondition copy = new VerificationCondition();
        copy.statements.addAll(statements);
        copy.finalState = finalState;
        copy.kind = kind;
        copy.conclusion = conclusion;
        copy.note = note;
        return copy;
    }

    public int size() {
        return statements.size();
    }

    @Override
    public String toString() {
        return "VC(" + statements.size() + " assumptions, concludes about " + finalState + ")";
    }
}
