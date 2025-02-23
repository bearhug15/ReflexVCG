package su.nsk.iae.reflex;


import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.vcg.VCGenerator2;

import java.io.FileInputStream;
import java.nio.file.Path;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws ParseException {

        Options options = new Options();
        options.addOption("s","source",true,"");
        options.addOption("o","output",true,"");
        options.addOption("e","expr",true,"");
        options.addOption("g","graph",true,"");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options,args);

        String source = commandLine.getOptionValue("s");
        if (source==null) throw new RuntimeException("Source not defined");

        String destination = commandLine.getOptionValue("o");
        if (destination==null) destination="./";


        Path sourcePath = Path.of(source);
        Path destPath = Path.of(destination);
        System.out.println("Starting program parsing.");
        FileInputStream fileInput = null;
        ANTLRInputStream input;
        try {
            fileInput = new FileInputStream(sourcePath.toFile());
            input = new ANTLRInputStream(fileInput);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflexLexer lexer = new ReflexLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReflexParser parser = new ReflexParser(tokenStream);
        ReflexParser.ProgramContext context = parser.program();
        System.out.println("Completed program parsing. Starting program analysis.");
        VCGenerator2 generator;


        boolean isSimpleExp;
        String expressionKind = commandLine.getOptionValue("e");
        if(expressionKind==null){
            isSimpleExp = false;
        }else if(expressionKind.equals("regular")){
            isSimpleExp = false;
        }else if(expressionKind.equals("simple")){
            isSimpleExp = true;
        }else{
            throw new RuntimeException("Unknown argument for expression processing type");
        }

        boolean exportGraph;
        String exportG = commandLine.getOptionValue("g");
        if(exportG==null){
            exportGraph = false;
        }else if(exportG.equals("false")){
            exportGraph = false;
        }else if(exportG.equals("true")){
            exportGraph = true;
        }else{
            throw new RuntimeException("Unknown argument for graph export");
        }

        generator = new VCGenerator2(context,isSimpleExp,exportGraph);
        generator.generateVC(sourcePath,destPath);

        //VCGenerator generator = new VCGenerator();
        //generator.generateVC(Path.of(source),Path.of(destination));

        /*
        VCGenerator generator = new VCGenerator();
        generator.test();*/
    }
}
