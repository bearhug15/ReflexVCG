package su.nsk.iae.reflex.ir;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.ann.AnnDefinition;
import su.nsk.iae.reflex.ann.AnnExpr;

import java.util.List;

/**
 * A Reflex-AL annotation bound to an IR construct.
 *
 * <p>Annotations travel in comments and are parsed with ReflexAL.g4. This class keeps
 * both the classified kind and the parsed tree, so later stages can consume them without
 * re-parsing; nothing generates Isabelle output from annotations yet, by design - the
 * temporal operators need an execution-history semantics the theory does not have.
 * {@link ExtraInvariantGenerator} is the intended consumer.
 */
public final class Annotation {

    public enum Kind { ASSUME, ASSERT, INVARIANT, DEFINE }

    private final Kind kind;
    private final String languageSpec;
    private final String text;
    private final ParserRuleContext tree;
    private final int line;
    private AnnExpr body;
    private List<AnnDefinition> definitions = List.of();

    public Annotation(Kind kind, String languageSpec, String text, ParserRuleContext tree, int line) {
        this.kind = kind;
        this.languageSpec = languageSpec;
        this.text = text;
        this.tree = tree;
        this.line = line;
    }

    public Kind getKind() {
        return kind;
    }

    /** The optional {@code (lang)} qualifier, or null when absent. */
    public String getLanguageSpec() {
        return languageSpec;
    }

    /** The annotation as written, including its brackets. */
    public String getText() {
        return text;
    }

    /** Parse tree of the annotation body, produced by ReflexALParser. */
    public ParserRuleContext getTree() {
        return tree;
    }

    /**
     * The annotation's formula, lowered from the parse tree. Null for a {@code define},
     * which carries {@link #getDefinitions()} instead, and for an annotation written in
     * another language, which is passed through unparsed.
     */
    public AnnExpr getBody() {
        return body;
    }

    public void setBody(AnnExpr body) {
        this.body = body;
    }

    /** The definitions a {@code define} introduces; empty for every other kind. */
    public List<AnnDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<AnnDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    /** True when a language other than Reflex-AL was named, so the body is opaque. */
    public boolean isForeignLanguage() {
        return languageSpec != null;
    }

    /** Source line the annotation appeared on. */
    public int getLine() {
        return line;
    }

    public static Kind kindFromKeyword(String keyword) {
        return switch (keyword) {
            case "assume" -> Kind.ASSUME;
            case "assert" -> Kind.ASSERT;
            case "invariant" -> Kind.INVARIANT;
            case "define" -> Kind.DEFINE;
            default -> throw new IllegalArgumentException("Unknown annotation kind: " + keyword);
        };
    }

    @Override
    public String toString() {
        return "[" + kind.name().toLowerCase() + (languageSpec == null ? "" : "(" + languageSpec + ")")
                + ": " + text + "] @" + line;
    }
}
