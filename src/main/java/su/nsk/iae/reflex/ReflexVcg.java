package su.nsk.iae.reflex;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import su.nsk.iae.reflex.antlr.NewReflexLexer;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.analysis.AttributePreparation;
import su.nsk.iae.reflex.ann.AnnTranslator;
import su.nsk.iae.reflex.analysis.StaticAnalysis;
import su.nsk.iae.reflex.cfg.Cfg;
import su.nsk.iae.reflex.cfg.CfgBuilder;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.cfg.PathEnumerator;
import su.nsk.iae.reflex.frontend.AnnotationBinder;
import su.nsk.iae.reflex.frontend.AstBuilder;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.preprocess.Preprocessor;
import su.nsk.iae.reflex.preprocess.WriteTargetCheck;
import su.nsk.iae.reflex.vc.ExtraInvariantGenerator;
import su.nsk.iae.reflex.vc.InitialCondition;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.vc.IsabelleRenderer;
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
    private AttributePreparation attributePreparation;
    private boolean staticAnalysis = true;
    private List<WriteTargetCheck.Finding> writeTargetWarnings = List.of();

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

        ReflexVcg generator = new ReflexVcg(program, annotations);
        generator.writeTargetWarnings = WriteTargetCheck.run(program);
        return generator;
    }

    public IrProgram getProgram() {
        return program;
    }

    /**
     * Writes the program makes to a physical variable bound with no {@code write =}
     * destination. Reported rather than refused: the condition is still generated.
     */
    public List<WriteTargetCheck.Finding> getWriteTargetWarnings() {
        return writeTargetWarnings;
    }

    /** Reflex-AL annotations found in the source, for later stages to consume. */
    public AnnotationBinder getAnnotations() {
        return annotations;
    }

    public Cfg getCfg() {
        if (cfg == null) {
            attributePreparation = new AttributePreparation(program);
            attributePreparation.run();
            cfg = new CfgBuilder(program, attributePreparation).build();
        }
        return cfg;
    }

    /** Whether impossible paths are discarded. On by default. */
    public void setStaticAnalysis(boolean enabled) {
        this.staticAnalysis = enabled;
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
        writer.writeSupportingTheories(program, extras.extraDefinitions(),
                globalInvariants(annotationTranslator()),
                loopInvariants(graph, annotationTranslator()));
        // The base case first: the inductive step below assumes the invariant holds,
        // so something has to establish that it holds to begin with.
        VerificationCondition initial = extras.process(InitialCondition.build(program));
        if (initial != null) {
            writer.write(initial);
        }

        AnnTranslator translator = annotationTranslator();
        StaticAnalysis analysis = staticAnalysis ? new StaticAnalysis(program) : null;
        new PathEnumerator(graph, analysis, translator).forEach(condition -> {
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

    /**
     * A translator holding every {@code define} the program introduces, so a definition is
     * usable wherever it is in scope.
     */
    private AnnTranslator annotationTranslator() {
        AnnTranslator translator = new AnnTranslator(clockTicks());
        forEachAnnotation((owner, annotation) -> translator.register(annotation));
        return translator;
    }

    /**
     * Invariants written on the program, a process or a state, translated and conjoined
     * into the global invariant.
     *
     * <p>Which one an invariant is decides how far it reaches: a program invariant holds at
     * every state, a process invariant only while that process runs, and a state invariant
     * only while it is in that state.
     */
    private List<String> globalInvariants(AnnTranslator translator) {
        TermRenderer renderer = new TermRenderer();
        Term state = new Term.Var("s");
        List<String> invariants = new ArrayList<>();

        forEachAnnotation((owner, annotation) -> {
            if (annotation.getKind() != Annotation.Kind.INVARIANT) {
                return;
            }
            if (owner instanceof LoopOwner) {
                // A loop invariant is not global; it belongs to that loop's conditions.
                return;
            }
            invariants.add(renderer.render(translator.translateInvariant(
                    annotation, state, owner.scale(), owner.process(), owner.state())));
        });
        return invariants;
    }

    /**
     * One entry per loop, for the theory that holds them.
     *
     * <p>A condition states a loop's invariant by name, so what the name means is settled
     * here rather than repeated in every condition mentioning it. A loop written with an
     * {@code [invariant: ...]} gets a definition; a loop written without one gets no
     * formula, and the writer leaves it uninterpreted.
     */
    private List<VcWriter.RenderedLoopInvariant> loopInvariants(Cfg graph,
                                                               AnnTranslator translator) {
        TermRenderer renderer = new TermRenderer();
        Term state = new Term.Var("s");
        List<VcWriter.RenderedLoopInvariant> rendered = new ArrayList<>();

        for (Cfg.LoopInvariant invariant : graph.getLoopInvariants()) {
            String formula = null;
            if (invariant.isDefined()) {
                AnnTranslator.Template template =
                        translator.translateLoopInvariant(invariant.annotation(), state);
                formula = renderer.render(
                        Term.substitute(template.body(), template.hole(), state));
            }
            rendered.add(new VcWriter.RenderedLoopInvariant(
                    invariant.name(), invariant.line(), formula));
        }
        return rendered;
    }

    /** Where an annotation sits, which decides the shape of an invariant built from it. */
    private interface Owner {
        AnnTranslator.Scale scale();

        default String process() {
            return null;
        }

        default String state() {
            return null;
        }
    }

    /** Marks a loop's own annotations, which are handled by the loop's conditions. */
    private interface LoopOwner extends Owner {
    }

    private void forEachAnnotation(java.util.function.BiConsumer<Owner, Annotation> visitor) {
        Owner programScope = () -> AnnTranslator.Scale.PROGRAM;
        program.getAnnotations().forEach(a -> visitor.accept(programScope, a));

        for (IrProcess process : program.getProcesses()) {
            Owner processScope = new Owner() {
                @Override
                public AnnTranslator.Scale scale() {
                    return AnnTranslator.Scale.PROCESS;
                }

                @Override
                public String process() {
                    return process.getName();
                }
            };
            process.getAnnotations().forEach(a -> visitor.accept(processScope, a));

            for (IrState state : process.getStates()) {
                Owner stateScope = new Owner() {
                    @Override
                    public AnnTranslator.Scale scale() {
                        return AnnTranslator.Scale.PSTATE;
                    }

                    @Override
                    public String process() {
                        return process.getName();
                    }

                    @Override
                    public String state() {
                        return state.getName();
                    }
                };
                state.getAnnotations().forEach(a -> visitor.accept(stateScope, a));
                state.getStatements().forEach(s -> statementAnnotations(s, stateScope, visitor));
            }
        }
    }

    /**
     * Annotations on statements. Those on a loop are reported against a loop owner so the
     * global invariant leaves them to the loop's own conditions.
     */
    private void statementAnnotations(su.nsk.iae.reflex.ir.IrStmt statement, Owner scope,
                                      java.util.function.BiConsumer<Owner, Annotation> visitor) {
        if (statement == null) {
            return;
        }
        Owner owner = statement instanceof su.nsk.iae.reflex.ir.IrStmt.For
                ? (LoopOwner) () -> AnnTranslator.Scale.FOR
                : scope;
        statement.getAnnotations().forEach(a -> visitor.accept(owner, a));

        if (statement instanceof su.nsk.iae.reflex.ir.IrStmt.Block block) {
            block.getStatements().forEach(s -> statementAnnotations(s, scope, visitor));
        } else if (statement instanceof su.nsk.iae.reflex.ir.IrStmt.If ifStmt) {
            statementAnnotations(ifStmt.getThenBranch(), scope, visitor);
            statementAnnotations(ifStmt.getElseBranch(), scope, visitor);
        } else if (statement instanceof su.nsk.iae.reflex.ir.IrStmt.Switch switchStmt) {
            switchStmt.getCases().forEach(
                    c -> c.getStatements().forEach(s -> statementAnnotations(s, scope, visitor)));
        } else if (statement instanceof su.nsk.iae.reflex.ir.IrStmt.For forStmt) {
            statementAnnotations(forStmt.getBody(), scope, visitor);
        }
    }

    private long clockTicks() {
        TimeRef clock = program.getClock();
        return clock.getKind() == TimeRef.Kind.TIME_LITERAL
                ? IsabelleRenderer.parseTimeMillis(clock.getText())
                : IsabelleRenderer.parseInteger(clock.getText());
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
