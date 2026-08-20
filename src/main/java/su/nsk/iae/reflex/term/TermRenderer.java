package su.nsk.iae.reflex.term;

import java.util.StringJoiner;

/**
 * Prints a {@link Term} as Isabelle/HOL.
 *
 * <p>Parenthesises every compound term rather than tracking operator precedence: the
 * result is noisier to read but cannot be reassociated by accident, which matters more for
 * something a prover consumes than for something a person does.
 */
public final class TermRenderer {

    public String render(Term term) {
        if (term instanceof Term.Var var) {
            return var.name();
        }
        if (term instanceof Term.Quoted quoted) {
            return "''" + quoted.text() + "''";
        }
        if (term instanceof Term.Raw raw) {
            return raw.text();
        }
        if (term instanceof Term.App app) {
            return renderApplication(app);
        }
        if (term instanceof Term.Infix infix) {
            return "(" + render(infix.left()) + " " + infix.operator() + " " + render(infix.right()) + ")";
        }
        if (term instanceof Term.Prefix prefix) {
            return "(" + prefix.operator() + " " + render(prefix.operand()) + ")";
        }
        if (term instanceof Term.Forall forall) {
            return "(\\<forall> " + String.join(" ", forall.variables()) + ". "
                    + render(forall.body()) + ")";
        }
        if (term instanceof Term.Exists exists) {
            return "(\\<exists> " + String.join(" ", exists.variables()) + ". "
                    + render(exists.body()) + ")";
        }
        return renderList((Term.ListTerm) term);
    }

    private String renderApplication(Term.App app) {
        // `if` is written in its own syntax rather than as a prefix application.
        if (app.function().equals("if") && app.arguments().size() == 5) {
            return "(if " + render(app.arguments().get(0))
                    + " then " + render(app.arguments().get(2))
                    + " else " + render(app.arguments().get(4)) + ")";
        }
        StringJoiner joiner = new StringJoiner(" ", "(", ")");
        joiner.add(app.function());
        app.arguments().forEach(argument -> joiner.add(render(argument)));
        return joiner.toString();
    }

    private String renderList(Term.ListTerm list) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        list.elements().forEach(element -> joiner.add(render(element)));
        return joiner.toString();
    }
}
