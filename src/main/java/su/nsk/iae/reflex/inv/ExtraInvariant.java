package su.nsk.iae.reflex.inv;

import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.Objects;

/**
 * One invariant a condition may rely on: written by the engineer as an annotation, or
 * derived from the structure of the program.
 *
 * <p>The formula is a predicate over {@link #STATE}. Every invariant, annotated or derived,
 * is written in the annotation invariants' wrap: its body stated at every boundary at or
 * below {@link #STATE} that its scale covers. A derived invariant also keeps its
 * {@link #claim}, the same statement about one boundary, which is what the analysis
 * establishes boundary by boundary. {@link #at} instantiates the formula elsewhere. Held as
 * a {@link Term}, so nothing here knows Isabelle.
 *
 * <p>What an invariant is about - its process, state, variables - is not part of it: those
 * are tags, held by the {@link ExtraInvariants} container, so a caller can attach its own
 * and search by any combination.
 *
 * @param name        unique within its container, and usable as an Isabelle identifier
 * @param kind        which rule produced it
 * @param priority    how far it reaches into generation; see {@link Priority}
 * @param description a sentence saying what it claims, for the comment above it
 */
public record ExtraInvariant(String name, Kind kind, Priority priority, Term formula, Term claim,
                             String description) {

    /** The state the formula is stated about. */
    public static final Term STATE = new Term.Var("s");

    /** The boundary a wrapped formula quantifies over. */
    public static final Term BOUND = new Term.Var("s1");

    /**
     * A derived invariant, wrapped exactly as an annotation invariant of the same scale is
     * ({@link AnnTranslator#invariantWrapper}): {@code body} is what it says about one
     * boundary, and the formula says it of every boundary at or below {@link #STATE} that
     * the scale covers. The claim is the same statement about {@link #STATE} alone, guard
     * included - what the formula requires at each boundary.
     *
     * @param body a formula about {@link #STATE}
     */
    public static ExtraInvariant wrapped(String name, Kind kind, AnnTranslator.Scale scale,
                                         String process, String state, Term body, String description) {
        Term formula = AnnTranslator.invariantWrapper(body, STATE, STATE, BOUND, scale, process, state);
        Term claim = Terms.implication(AnnTranslator.scopeGuard(STATE, scale, process, state), body);
        return new ExtraInvariant(name, kind, kind.priority(), formula, claim, description);
    }

    /**
     * How far an invariant reaches, decided by how hard it is to prove against how often a
     * condition needs it (extra-invariants.md, "Priorities").
     */
    public enum Priority {
        /**
         * Part of the global invariant: every condition assumes it at its first state and
         * every cycle proves it at its last. The annotation invariants and the states each
         * process can be in.
         */
        HIGH,
        /**
         * Asked for with {@code -x mid}: moderate proofs, needed often. Assumed by the
         * conditions whose paths concern them, proved by an obligation per cycle.
         */
        MID,
        /**
         * Asked for with {@code -x low}: large, relational, or rarely needed. Treated like
         * the middle priority once asked for.
         */
        LOW
    }

    /** The rules an invariant can come from (mainOverview.tex, "Extra Invariants", and on). */
    public enum Kind {
        /** An invariant written on the program, a process or a state. */
        ANNOTATION(Priority.HIGH, false),
        /** An invariant written on a loop. */
        LOOP_INVARIANT(Priority.HIGH, false),
        /** The states a process can be found in at a boundary. */
        PROCESS_STATES(Priority.HIGH, true),
        /** States of two processes that are never found together. */
        PROCESS_PAIRS(Priority.MID, true),
        /** What the static analysis assumes about processes, confirmed by the check. */
        STATIC_ANALYSIS(Priority.MID, true),
        /** Values variables hold whenever a process is in a state. */
        DEFINED_VARIABLES(Priority.MID, true),
        /** Values variables hold once a process has stayed in a state two boundaries running. */
        STABILIZED_VARIABLES(Priority.MID, true),
        /** Variables left as they were when a process entered its state. */
        UNCHANGED_SINCE_ENTRY(Priority.MID, true),
        /** How long a process can stay in a state before its timeout moves it on. */
        TIMER_BOUNDS(Priority.MID, true),
        /** The ways a process can have come to be in a state. */
        TRANSITION(Priority.LOW, true),
        /** Variables holding a value copied into them on the way into a state. */
        COPIED_ON_ENTRY(Priority.LOW, true),
        /** Produced by a candidate source added from outside. */
        CUSTOM(Priority.LOW, true);

        private final Priority priority;
        private final boolean derived;

        Kind(Priority priority, boolean derived) {
            this.priority = priority;
            this.derived = derived;
        }

        public Priority priority() {
            return priority;
        }

        /**
         * Whether it is derived from the program, and so defined in the extra-invariant
         * theory, as against written in the source and rendered where annotations are.
         */
        public boolean isDerived() {
            return derived;
        }
    }

    /** The forms a way into a state takes in a {@link Kind#TRANSITION} invariant. */
    public enum Transition {
        /** The process has been in the state since the program started. */
        INITIAL,
        /** A {@code set state} (or {@code start}) taken under some condition. */
        CONDITIONAL,
        /** A conditional transition taken from a {@code timeout}. */
        TIMED
    }

    public ExtraInvariant {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(formula, "formula");
    }

    /** An invariant at the priority its kind has, with no claim of its own. */
    public ExtraInvariant(String name, Kind kind, Term formula, String description) {
        this(name, kind, kind.priority(), formula, null, description);
    }

    /** An invariant with no claim of its own: an annotation, or one plugged in as it stands. */
    public ExtraInvariant(String name, Kind kind, Priority priority, Term formula, String description) {
        this(name, kind, priority, formula, null, description);
    }

    /**
     * What the invariant requires of one boundary: the claim, or the formula itself for an
     * invariant built without one.
     */
    public Term claimOrFormula() {
        return claim != null ? claim : formula;
    }

    /** The formula stated about {@code state} instead of {@link #STATE}. */
    public Term at(Term state) {
        return Term.substitute(formula, STATE, state);
    }
}
