package su.nsk.iae.reflex.term;

import java.util.List;

/**
 * An Isabelle/HOL term, built rather than printed.
 *
 * <p>Annotations translate into formulas that quantify over states and speak about
 * reachability, which cannot be assembled by concatenating strings without losing track of
 * where a state variable is bound and where it is free. The translation therefore builds a
 * term and {@link TermRenderer} prints it, which is also what makes
 * {@link #substitute} - the {@code substState} of the specification - straightforward:
 * a template is built once with a hole and instantiated at several states.
 */
public sealed interface Term {

    /** A name used as it stands: a state variable, a bound variable, a number. */
    record Var(String name) implements Term {
    }

    /** An Isabelle string literal, {@code ''name''}. */
    record Quoted(String text) implements Term {
    }

    /** Function application, {@code f a b}. */
    record App(String function, List<Term> arguments) implements Term {
        public App {
            arguments = List.copyOf(arguments);
        }
    }

    /** An infix operator, {@code a + b}. */
    record Infix(String operator, Term left, Term right) implements Term {
    }

    /** A prefix operator, {@code \<not> a}. */
    record Prefix(String operator, Term operand) implements Term {
    }

    /** {@code \<forall> v1 v2. body}. */
    record Forall(List<String> variables, Term body) implements Term {
        public Forall {
            variables = List.copyOf(variables);
        }
    }

    /** {@code \<exists> v1 v2. body}. */
    record Exists(List<String> variables, Term body) implements Term {
        public Exists {
            variables = List.copyOf(variables);
        }
    }

    /** A list, as the access path argument of getVarVal and setVarVal. */
    record ListTerm(List<Term> elements) implements Term {
        public ListTerm {
            elements = List.copyOf(elements);
        }
    }

    /**
     * Text carried through untouched: the body of an annotation written in another
     * language, which is not parsed and goes into the condition as it stands.
     */
    record Raw(String text) implements Term {
    }

    // ------------------------------------------------------------------ substitution

    /**
     * Replaces every occurrence of {@code hole} with {@code replacement} - the
     * {@code substState} of the specification.
     *
     * <p>A template is built once against a placeholder state and then instantiated at
     * each state it has to hold in, which is how an invariant is stated at several points
     * without being translated more than once.
     */
    static Term substitute(Term term, Term hole, Term replacement) {
        if (term.equals(hole)) {
            return replacement;
        }
        if (term instanceof App app) {
            return new App(app.function(),
                    app.arguments().stream().map(a -> substitute(a, hole, replacement)).toList());
        }
        if (term instanceof Infix infix) {
            return new Infix(infix.operator(),
                    substitute(infix.left(), hole, replacement),
                    substitute(infix.right(), hole, replacement));
        }
        if (term instanceof Prefix prefix) {
            return new Prefix(prefix.operator(), substitute(prefix.operand(), hole, replacement));
        }
        if (term instanceof Forall forall) {
            return new Forall(forall.variables(), substitute(forall.body(), hole, replacement));
        }
        if (term instanceof Exists exists) {
            return new Exists(exists.variables(), substitute(exists.body(), hole, replacement));
        }
        if (term instanceof ListTerm list) {
            return new ListTerm(
                    list.elements().stream().map(e -> substitute(e, hole, replacement)).toList());
        }
        // Var, Quoted and Raw have no interior to rewrite.
        return term;
    }
}
