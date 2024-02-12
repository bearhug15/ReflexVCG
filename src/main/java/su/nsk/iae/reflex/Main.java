package su.nsk.iae.reflex;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.cli.*;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.vcg.VCGenerator;

import java.nio.file.Path;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("s","source",true,"");
        options.addOption("o","output",true,"");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options,args);

        String source = commandLine.getOptionValue("s");
        if (source==null) throw new RuntimeException("Source not defined");

        String destination = commandLine.getOptionValue("o");
        if (destination==null) destination="./";

        VCGenerator generator = new VCGenerator();
        generator.generateVC(Path.of(source),Path.of(destination));
        //generator.test();
    }
}
