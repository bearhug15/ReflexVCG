package su.nsk.iae.reflex.vcg;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.ProgramAnalyzer2;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.RuleChecker;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.StatementsCreator.IsabelleCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class VCGenerator2 {

    VariableMapper mapper;
    ProgramMetaData metaData;
    RuleChecker checker;
    VCPrinter printer;

    IStatementCreator creator;
    ProgramGraph graph;
    AttributeCollector collector;
    ASTGraphProjection projection;
    ReflexParser.ProgramContext context;

    boolean exportGraph;

    public VCGenerator2(ReflexParser.ProgramContext context, boolean isSimpleExprVisitor, boolean exportGraph){
        this.context = context;
        this.creator = new IsabelleCreator();
        this.mapper = new VariableMapper(context,creator);
        this.metaData = new ProgramMetaData(context,mapper,creator);
        this.exportGraph = exportGraph;

        GraphBuilder builder = new GraphBuilder(metaData,mapper,creator, isSimpleExprVisitor);
        this.graph = builder.buildProgramGraph(context);
        this.projection = builder.getASTtoGraphProjection();

        ProgramAnalyzer2 analyzer = new ProgramAnalyzer2(metaData,projection,graph);

        this.collector = analyzer.generateAttributes(context);

        this.checker = new RuleChecker(metaData,collector);
        System.out.println("Completed program analysis.");
    }

    public void generateVC(Path source, Path destination){
        System.out.println("Starting verification conditions generation.");
        this.printer = new VCPrinter(destination,source,metaData,creator);

        if(exportGraph){
            DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString().replace("\\<and>","\n\\<and>")));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(this.graph, writer);
            printer.printGraph(writer.toString());
        }

        printer.printBaseVCInDir();
        VCGeneratorIterator gen = new VCGeneratorIterator(graph,collector,checker,creator);
        while(gen.hasNext()){
            IReflexNode next = gen.next();
            if (next==null){
                break;
            }
            if(graph.getOutgoingNeighbours(next).isEmpty()){
                printer.printVCInDir(gen.getVCStrings(),gen.getStateCounter());
            }
        }
        System.out.println("Completed verification conditions generation. Conditions generated: " + VCGenerated());
    }



    public int VCGenerated(){
        return printer.getLemmasPrinted();
    }



}
