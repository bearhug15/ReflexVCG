package su.nsk.iae.reflex;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgBuilder;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.cfg.PathEnumerator;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.vc.ExtraInvariantGenerator;
import su.nsk.iae.reflex.vc.VcWriter;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The generation pipeline, end to end.
 *
 * <p>Each stage has one input and one output, and none of them knows about the others:
 * parsing produces a tree, lowering produces IR, preprocessing rewrites that IR into a
 * canonical form, graph building produces a control-flow graph, enumeration produces
 * verification conditions, and only the writer knows Isabelle.
 */
public final class ReflexVcg {

    private final IrProgram program;
    private final AnnotationBinder annotations;
    private Cfg cfg;
    private ExtraInvariantGenerator extraInvariants;

    private ReflexVcg(IrProgram program, AnnotationBinder annotations) {
        this.program = program;
        this.annotations = annotations;
    }

    /** Parses and preprocesses a source file, leaving it ready for generation. */
    public static ReflexVcg load(Path source) throws IOException {
        ErrorCollector errors = new ErrorCollector(source.getFileName().toString());

        NewReflexLexer lexer = new NewReflexLexer(CharStreams.fromPath(source, StandardCharsets.UTF_8));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errors);

        BufferedTokenStream tokens = new CommonTokenStream(lexer);
        NewReflexParser parser = new NewReflexParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errors);

        NewReflexParser.ProgramContext context = parser.program();
        errors.failIfAny();

        AnnotationBinder annotations = new AnnotationBinder(tokens);
        IrProgram program = new AstBuilder(annotations).build(context);
        Preprocessor.run(program);
        return new ReflexVcg(program, annotations);
    }

    public IrProgram getProgram() {
        return program;
    }

    /** Reflex-AL annotations found in the source, for later stages to consume. */
    public AnnotationBinder getAnnotations() {
        return annotations;
    }

    public Cfg getCfg() {
        if (cfg == null) {
            cfg = new CfgBuilder(program).build();
        }
        return cfg;
    }

    /**
     * Generates every condition into {@code destination}.
     *
     * @return the number of conditions written
     */
    public int generate(Path destination) {
        Cfg graph = getCfg();

        List<CfgNode.Unsupported> unsupported = graph.unsupportedNodes();
        if (!unsupported.isEmpty()) {
            // Reported rather than skipped: a condition that quietly omitted the
            // construct's effect would be unsound.
            throw new IllegalStateException("This program uses features that verification "
                    + "condition generation does not support: "
                    + unsupported.stream().map(CfgNode.Unsupported::getConstruct).distinct().toList());
        }

        ExtraInvariantGenerator extras = extraInvariants != null
                ? extraInvariants
                : new ExtraInvariantGenerator(program, graph, annotations);
        extras.analyse();

        VcWriter writer = new VcWriter(destination, program.getName());
        writer.writeSupportingTheories(program, extras.extraDefinitions());
        new PathEnumerator(graph).forEach(condition -> {
            VerificationCondition processed = extras.process(condition);
            if (processed != null) {
                writer.write(processed);
            }
        });
        return writer.getWritten();
    }

    /**
     * Replaces the extra-invariant stage. The default does nothing; supplying a subclass
     * is how annotation-driven generation, condition post-processing and extra graph
     * analysis get added without changing the pipeline.
     */
    public void setExtraInvariantGenerator(ExtraInvariantGenerator extraInvariants) {
        this.extraInvariants = extraInvariants;
    }

    /** Writes the control-flow graph in Graphviz format. */
    public void exportGraph(Path destination) throws IOException {
        Files.writeString(destination.resolve(program.getName() + "_program_graph.gv"),
                getCfg().toDot(), StandardCharsets.UTF_8);
    }

    /** Collects syntax errors so a bad source fails loudly rather than half-parsing. */
    private static final class ErrorCollector extends BaseErrorListener {
        private final String source;
        private final List<String> errors = new ArrayList<>();

        ErrorCollector(String source) {
            this.source = source;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                int charPositionInLine, String msg, RecognitionException e) {
            errors.add(source + ":" + line + ":" + charPositionInLine + " " + msg);
        }

        void failIfAny() {
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(
                        "Cannot parse " + source + ":\n  " + String.join("\n  ", errors));
            }
        }
    }
}
