package su.nsk.iae.reflex.frontend;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexALLexer;
import su.nsk.iae.reflex.antlr.ReflexALParser;
import su.nsk.iae.reflex.ann.AnnLowering;
import su.nsk.iae.reflex.ir.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts Reflex-AL annotations from comments and binds them to the constructs they
 * precede.
 *
 * <p>Comments sit on the hidden channel, so they can appear anywhere without disturbing
 * the parse. An annotation belongs to the construct whose first token it immediately
 * precedes. Each annotation is claimed exactly once, by the outermost construct that
 * starts at that token, so a comment before a statement does not also attach to the
 * expression inside it.
 *
 * <p>Comments that are not annotations - ordinary prose - are ignored. A comment that
 * looks like an annotation but does not parse is recorded in {@link #getDiagnostics()}
 * rather than failing the build.
 */
public final class AnnotationBinder {

    private final Map<Integer, List<Annotation>> byTokenIndex = new HashMap<>();
    private final Set<Integer> claimed = new HashSet<>();
    private final List<String> diagnostics = new ArrayList<>();
    private final AnnLowering lowering = new AnnLowering();

    /**
     * The head of an annotation: its kind and, optionally, the language its body is
     * written in. Read before parsing, because a body in another language is not
     * Reflex-AL and must not be put through that grammar.
     */
    private static final Pattern HEAD = Pattern.compile(
            "\\[\\s*(assume|assert|invariant|define)\\s*(?:\\(\\s*(\\w+)\\s*\\))?\\s*:");

    public AnnotationBinder(BufferedTokenStream tokens) {
        collect(tokens);
    }

    /** Convenience for callers that only have the source text. */
    public static BufferedTokenStream tokenize(String source) {
        NewReflexLexer lexer = new NewReflexLexer(CharStreams.fromString(source));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return tokens;
    }

    /**
     * Annotations attached to {@code ctx}, claiming them so no other construct also
     * receives them. Returns an empty list when there are none.
     */
    public List<Annotation> annotationsFor(ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null) {
            return List.of();
        }
        int index = ctx.getStart().getTokenIndex();
        if (!byTokenIndex.containsKey(index) || !claimed.add(index)) {
            return List.of();
        }
        return byTokenIndex.get(index);
    }

    /** Every annotation found, whether or not it was bound to a construct. */
    public List<Annotation> getAllAnnotations() {
        List<Annotation> all = new ArrayList<>();
        byTokenIndex.values().forEach(all::addAll);
        return all;
    }

    /** Comments that looked like annotations but could not be parsed. */
    public List<String> getDiagnostics() {
        return diagnostics;
    }

    // ------------------------------------------------------------------ collection

    private void collect(BufferedTokenStream tokens) {
        List<Token> all = tokens.getTokens();
        for (int i = 0; i < all.size(); i++) {
            Token token = all.get(i);
            if (!isComment(token)) {
                continue;
            }
            List<Annotation> parsed = parseAnnotations(token);
            if (parsed.isEmpty()) {
                continue;
            }
            int target = nextDefaultChannelToken(all, i);
            if (target < 0) {
                // A trailing comment at end of file belongs to no construct.
                continue;
            }
            byTokenIndex.computeIfAbsent(target, k -> new ArrayList<>()).addAll(parsed);
        }
    }

    private static boolean isComment(Token token) {
        return token.getType() == NewReflexLexer.LINE_COMMENT
                || token.getType() == NewReflexLexer.BLOCK_COMMENT;
    }

    private static int nextDefaultChannelToken(List<Token> tokens, int from) {
        for (int i = from + 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == Token.EOF) {
                return -1;
            }
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                return token.getTokenIndex();
            }
        }
        return -1;
    }

    private List<Annotation> parseAnnotations(Token comment) {
        String body = stripDelimiters(comment.getText());
        List<Annotation> result = new ArrayList<>();
        for (String candidate : bracketedGroups(body)) {
            Annotation annotation = parseAnnotation(candidate, comment.getLine());
            if (annotation != null) {
                result.add(annotation);
            }
        }
        return result;
    }

    private static String stripDelimiters(String text) {
        if (text.startsWith("//")) {
            return text.substring(2);
        }
        if (text.startsWith("/*")) {
            String inner = text.substring(2);
            return inner.endsWith("*/") ? inner.substring(0, inner.length() - 2) : inner;
        }
        return text;
    }

    /**
     * Splits out top-level {@code [...]} groups, tracking nesting so that array
     * subscripts inside an annotation body do not terminate it early.
     */
    private static List<String> bracketedGroups(String text) {
        List<String> groups = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0 && start >= 0) {
                    groups.add(text.substring(start, i + 1));
                    start = -1;
                } else if (depth < 0) {
                    return groups;
                }
            }
        }
        return groups;
    }

    private Annotation parseAnnotation(String text, int line) {
        Matcher head = HEAD.matcher(text);
        if (head.lookingAt() && head.group(2) != null) {
            // Written in another language: only the names inside are rewritten later, and
            // the text goes into the condition as it stands.
            String body = text.substring(head.end(), text.lastIndexOf(']')).trim();
            return new Annotation(Annotation.kindFromKeyword(head.group(1)),
                    head.group(2), body, null, line);
        }

        List<String> errors = new ArrayList<>();
        BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int l,
                                    int charPositionInLine, String msg, RecognitionException e) {
                errors.add(msg);
            }
        };

        ReflexALLexer lexer = new ReflexALLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        ReflexALParser parser = new ReflexALParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        ReflexALParser.AnnotationContext tree = parser.annotation();
        if (!errors.isEmpty()) {
            diagnostics.add("line " + line + ": not a valid Reflex-AL annotation: " + text
                    + " (" + errors.get(0) + ")");
            return null;
        }

        Annotation.Kind kind;
        try {
            kind = Annotation.kindFromKeyword(tree.annotationKind().getText());
        } catch (IllegalArgumentException e) {
            diagnostics.add("line " + line + ": unknown annotation kind in " + text);
            return null;
        }
        String languageSpec = tree.languageSpec() == null
                ? null
                : tree.languageSpec().Identifier().getText();

        Annotation annotation = new Annotation(kind, languageSpec, text, tree, line);
        if (languageSpec == null) {
            // A body in another language is passed through unparsed, so there is nothing
            // to lower.
            try {
                annotation.setBody(lowering.lowerBody(tree.annotationBody()));
                annotation.setDefinitions(lowering.lowerDefinitions(tree.annotationBody()));
            } catch (RuntimeException e) {
                diagnostics.add("line " + line + ": cannot interpret annotation " + text
                        + " (" + e.getMessage() + ")");
                return null;
            }
        }
        return annotation;
    }
}
