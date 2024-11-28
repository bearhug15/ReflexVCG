package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.ProgramAnalyzer2;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.RuleChecker;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.StatementsCreator.IsabelleCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.nio.file.Path;

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

    public VCGenerator2(ReflexParser.ProgramContext context){
        this.context = context;
        this.creator = new IsabelleCreator();
        this.mapper = new VariableMapper(context,creator);
        this.metaData = new ProgramMetaData(context,mapper,creator);

        GraphBuilder builder = new GraphBuilder(metaData,mapper,creator);
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
