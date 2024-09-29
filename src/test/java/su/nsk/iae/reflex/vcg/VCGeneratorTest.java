package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.staticAnalysis.ProgramAnalyzer2;
import su.nsk.iae.reflex.staticAnalysis.RuleChecker;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class VCGeneratorTest {

    @Test
    void escalator(){
        CharStream inputStream = CharStreams.fromString("program Escalator{\n" +
                "\tclock 100;\n" +
                "\tinput inp 0x00 0x00 8;\n" +
                "\toutput out 0x00 0x01 8;\n" +
                "\t\n" +
                "\tbool userAtTop = inp[1];\n" +
                "\tbool userAtBottom = inp[2];\n" +
                "\tbool directionSwitch = inp[3];\n" +
                "\tbool alarmButton = inp[4];\n" +
                "\tbool stuck = inp[5];\n" +
                "\t\n" +
                "\tbool up = out[1];\n" +
                "\tbool down = out[2];\n" +
                "\t\n" +
                "\tconst bool UP = true;\n" +
                "\tconst bool DOWN = false;\n" +
                "\tconst time DELAY = 0t2m;\n" +
                "\tconst time SUSPENSION_TIME = 0t1s;\n" +
                "\t\n" +
                "\tbool direction;\n" +
                "\tbool moving;\n" +
                "\t\n" +
                "\tprocess Ctrl{\n" +
                "\t\tstate motionless{\n" +
                "\t\t\tif (alarmButton) {\n" +
                "\t\t\t\tset state emergency;\n" +
                "\t\t\t} else {\n" +
                "\t\t\t\tif (stuck) {\n" +
                "\t\t\t\t\tset state stuckState;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tif (userAtTop || userAtBottom){\n" +
                "\t\t\t\t\t\tif (directionSwitch == UP){\n" +
                "\t\t\t\t\t\t\tup = true;\n" +
                "\t\t\t\t\t\t\tmoving = true;\n" +
                "\t\t\t\t\t\t\tdirection = UP;\n" +
                "\t\t\t\t\t\t\tset state goUp;\n" +
                "\t\t\t\t\t\t} else{\n" +
                "\t\t\t\t\t\t\tdown = true;\n" +
                "\t\t\t\t\t\t\tmoving = true;\n" +
                "\t\t\t\t\t\t\tdirection = DOWN;\n" +
                "\t\t\t\t\t\t\tset state goDown;\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t} else{\n" +
                "\t\t\t\t\t\tdirection = directionSwitch;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate goUp{\n" +
                "\t\t\tif (alarmButton){\n" +
                "\t\t\t\tset state emergency;\n" +
                "\t\t\t} else{\n" +
                "\t\t\t\tif (stuck){\n" +
                "\t\t\t\t\tset state stuckState;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tif (userAtTop || userAtBottom){\n" +
                "\t\t\t\t\t\treset timer;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\ttimeout DELAY{\n" +
                "\t\t\t\tup = false;\n" +
                "\t\t\t\tmoving = false;\n" +
                "\t\t\t\tdirection = directionSwitch;\n" +
                "\t\t\t\tset state motionless;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate goDown{\n" +
                "\t\t\tif (alarmButton){\n" +
                "\t\t\t\tset state emergency;\n" +
                "\t\t\t} else{\n" +
                "\t\t\t\tif (stuck){\n" +
                "\t\t\t\t\tset state stuckState;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tif (userAtTop || userAtBottom){\n" +
                "\t\t\t\t\t\treset timer;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\ttimeout DELAY{\n" +
                "\t\t\t\tdown = false;\n" +
                "\t\t\t\tmoving = false;\n" +
                "\t\t\t\tdirection = directionSwitch;\n" +
                "\t\t\t\tset state motionless;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate stuckState{\n" +
                "\t\t\tup = false;\n" +
                "\t\t\tdown = false;\n" +
                "\t\t\tif (alarmButton){\n" +
                "\t\t\t\tset state emergency;\n" +
                "\t\t\t} else {\n" +
                "\t\t\t\tif (stuck){\n" +
                "\t\t\t\t\treset timer;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\ttimeout SUSPENSION_TIME{\n" +
                "\t\t\t\tif (moving){\n" +
                "\t\t\t\t\tif (direction == UP){\n" +
                "\t\t\t\t\t\tup = true;\n" +
                "\t\t\t\t\t\tset state goUp;\n" +
                "\t\t\t\t\t} else {\n" +
                "\t\t\t\t\t\tdown = true;\n" +
                "\t\t\t\t\t\tset state goDown;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tset state motionless;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate emergency{\n" +
                "\t\t\tup = false;\n" +
                "\t\t\tdown = false;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}");
        VCGenerator generator = new VCGenerator();
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            generator.mapper = new VariableMapper(context);
            generator.metaData = new ProgramMetaData(context,generator.mapper);
            generator.printer = new VCPrinter(Path.of("./src/test/testResult"),"test",generator.metaData);
            generator.checker = new RuleChecker(generator.metaData);
            ProgramAnalyzer2 analyzer2 = new ProgramAnalyzer2(generator.metaData);
            generator.collector = analyzer2.generateAttributes(context);

            generator.visitProgram(context);
            generator.visitStack();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
        System.out.println("VC conditions generated: " + generator.conditionsGenerated);
    }
/*
    @Test
    void HandDryer(){
        //String sourceName = source.getFileName().toString();
        CharStream inputStream = CharStreams.fromString("program Dryer {\n" +
                "clock 100;\n" +
                "input inp 0x00 0x00 8;\n" +
                "output out 0x00 0x01 8;\n" +
                "const bool ON = true;\n" +
                "const bool OFF = false;\n" +
                "const time TIMEOUT = 0t2s;\n" +
                "process Dryer {\n" +
                "bool hands_under_dryer = inp[1];\n" +
                "bool dryer_control = out[1];\n" +
                "state Wait {\n" +
                "if (hands_under_dryer) {\n" +
                "dryer_control = ON;\n" +
                "set state Work;\n" +
                "}\n" +
                "}\n" +
                "state Work {\n" +
                "if (hands_under_dryer) reset timer;timeout (TIMEOUT) {\n" +
                "dryer_control = OFF;\n" +
                "set state Wait;\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}");
        VCGenerator generator = new VCGenerator();
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            generator.mapper = new VariableMapper(context);
            generator.metaData = new ProgramMetaData(context,generator.mapper);
            generator.printer = new VCPrinter(Path.of("./"),"test",generator.metaData);
            generator.checker = new RuleChecker(generator.metaData);
            ProgramAnalyzer2 analyzer2 = new ProgramAnalyzer2(generator.metaData);
            generator.collector = analyzer2.generateAttributes(context);

            generator.visitProgram(context);
            generator.visitStack();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
*/
}