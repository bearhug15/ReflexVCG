package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.nio.file.Path;

class VCGeneratorTest {

    /*@Test
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
    }*/
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

    /*@Test
    void turnstile(){
        CharStream inputStream = CharStreams.fromString("program Turnsile{\n" +
                "\tclock 100; \n" +
                "\tbool passed;\n" +
                "\tinput inp 0x00 0x00 24;\n" +
                "\toutput out 0x00 0x03 24;\n" +
                "\tbool PdOut = inp[0];\n" +
                "\tbool paid = inp[1];\n" +
                "\tbool opened = inp[2];\n" +
                "\tbool open  = out[0];\n" +
                "\tbool rst = out[1];\n" +
                "\tbool enter = out[2];\n" +
                "\t\n" +
                "\tprocess Init {\n" +
                "\t\tstate init {\n" +
                "\t\t\tstart Controller;\n" +
                "\t\t\tstart EntranceController;\n" +
                "\t\t\tstop;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess Controller{\n" +
                "\t\tstate isClosed{\n" +
                "\t\t\tif (paid){\n" +
                "\t\t\t\topen = true;\n" +
                "\t\t\t\tpassed = false;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\t\t\t\n" +
                "\t\t}\n" +
                "\t\tstate minimalOpened{\n" +
                "\t\t\tif (PdOut){\n" +
                "\t\t\t\tpassed = true;\n" +
                "\t\t\t}\n" +
                "\t\t\t\n" +
                "\t\t\ttimeout 0t1s{\n" +
                "\t\t\t\tif (passed) {\n" +
                "\t\t\t\t\topen = false;\n" +
                "\t\t\t\t\tset state isClosed;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tset next state;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\tstate isOpened{\n" +
                "\t\t\tif (PdOut){\n" +
                "\t\t\t\topen = false;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t\ttimeout 0t9s{\n" +
                "\t\t\t\topen = false;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess EntranceController{\n" +
                "\t\tstate isClosed{\n" +
                "\t\t\tif (opened){\n" +
                "\t\t\t\tenter = true;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\tstate isOpened{\n" +
                "\t\t\tif (!opened){\n" +
                "\t\t\t\tenter = false;\n" +
                "\t\t\t\trst = true;\n" +
                "\t\t\t\tstart Unlocker;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess Unlocker{\n" +
                "\t\tstate unlock{\n" +
                "\t\t\ttimeout 0t1s{\n" +
                "\t\t\t\trst = false;\n" +
                "\t\t\t\tstop;\n" +
                "\t\t\t}\n" +
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
    }*/
    @Test
    void newEscalator(){
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
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            VCGenerator2 gen = new VCGenerator2(context);

            /*DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(gen.graph, writer);
            System.out.println(writer.toString());//*/

            gen.generateVC(Path.of("./src/test/testResult"),Path.of("./src/test/testResult"));
            System.out.println("VC conditions generated: " + gen.VCGenerated());

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @Test
    void newTurnstile(){
        CharStream inputStream = CharStreams.fromString("program Turnsile{\n" +
                "\tclock 100; \n" +
                "\tbool passed;\n" +
                "\tinput inp 0x00 0x00 24;\n" +
                "\toutput out 0x00 0x03 24;\n" +
                "\tbool PdOut = inp[0];\n" +
                "\tbool paid = inp[1];\n" +
                "\tbool opened = inp[2];\n" +
                "\tbool open  = out[0];\n" +
                "\tbool rst = out[1];\n" +
                "\tbool enter = out[2];\n" +
                "\t\n" +
                "\tprocess Init {\n" +
                "\t\tstate init {\n" +
                "\t\t\tstart Controller;\n" +
                "\t\t\tstart EntranceController;\n" +
                "\t\t\tstop;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess Controller{\n" +
                "\t\tstate isClosed{\n" +
                "\t\t\tif (paid){\n" +
                "\t\t\t\topen = true;\n" +
                "\t\t\t\tpassed = false;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\t\t\t\n" +
                "\t\t}\n" +
                "\t\tstate minimalOpened{\n" +
                "\t\t\tif (PdOut){\n" +
                "\t\t\t\tpassed = true;\n" +
                "\t\t\t}\n" +
                "\t\t\t\n" +
                "\t\t\ttimeout 0t1s{\n" +
                "\t\t\t\tif (passed) {\n" +
                "\t\t\t\t\topen = false;\n" +
                "\t\t\t\t\tset state isClosed;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tset next state;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\tstate isOpened{\n" +
                "\t\t\tif (PdOut){\n" +
                "\t\t\t\topen = false;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t\ttimeout 0t9s{\n" +
                "\t\t\t\topen = false;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess EntranceController{\n" +
                "\t\tstate isClosed{\n" +
                "\t\t\tif (opened){\n" +
                "\t\t\t\tenter = true;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\tstate isOpened{\n" +
                "\t\t\tif (!opened){\n" +
                "\t\t\t\tenter = false;\n" +
                "\t\t\t\trst = true;\n" +
                "\t\t\t\tstart Unlocker;\n" +
                "\t\t\t\tset state isClosed;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tprocess Unlocker{\n" +
                "\t\tstate unlock{\n" +
                "\t\t\ttimeout 0t1s{\n" +
                "\t\t\t\trst = false;\n" +
                "\t\t\t\tstop;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}");
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            VCGenerator2 gen = new VCGenerator2(context);

            /*DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(gen.graph, writer);
            System.out.println(writer.toString());*/

            gen.generateVC(Path.of("./src/test/testResult"),Path.of("./src/test/testResult"));
            System.out.println("VC conditions generated: " + gen.VCGenerated());
            /*IsabelleCreator creator = new IsabelleCreator();
            VariableMapper mapper = new VariableMapper(context,creator);
            ProgramMetaData metaData = new ProgramMetaData(context,mapper,creator);
            GraphBuilder builder = new GraphBuilder(metaData,mapper,creator);
            ProgramGraph graph = builder.buildProgramGraph(context);

            DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(graph, writer);
            System.out.println(writer.toString());
            /*
            generator.printer = new VCPrinter(Path.of("./src/test/testResult"),"test",generator.metaData);
            generator.checker = new RuleChecker(generator.metaData);
            ProgramAnalyzer2 analyzer2 = new ProgramAnalyzer2(generator.metaData);
            generator.collector = analyzer2.generateAttributes(context);

            generator.visitProgram(context);
            generator.visitStack();*/
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        //System.out.println("VC conditions generated: " + generator.conditionsGenerated);
    }

    @Test
    void newThermopot(){
        CharStream inputStream = CharStreams.fromString("program Thermopot {\n" +
                "\tclock 100;\n" +
                "    input temperature 0x00 0x00 8;\n" +
                "\tinput buttons 0x00 0x00 8;\n" +
                "\tinput boilingButton 0x00 0x00 8;\n" +
                "\t\n" +
                "    output selectedTemp 0x00 0x01 8;\n" +
                "\toutput heater 0x00 0x01 8;\n" +
                "\toutput lid 0x00 0x01 8;\n" +
                "\toutput mods 0x00 0x01 8;\n" +
                "\t\n" +
                "\tconst bool LOCKED = true;\n" +
                "\tconst bool UNLOCKED = false;\n" +
                "\tconst bool PRESSED = true;\n" +
                "\t\n" +
                "\tconst int8 BOILING_POINT = 100;\n" +
                "\tconst int8 TEMP1 = 98;\n" +
                "\tconst int8 TEMP2 = 85;\n" +
                "\tconst int8 TEMP3 = 60;\n" +
                "\t\n" +
                "\tint8 temperature = temperature[0];\n" +
                "\tbool button1 = buttons[1];\n" +
                "\tbool button2 = buttons[2];\n" +
                "\tbool button3 = buttons[3];\n" +
                "\tbool boilingButton = boilingButton[0];\n" +
                "\t\n" +
                "\tint8 selectedTemp = selectedTemp[0];\n" +
                "\tbool heater = heater[0];\n" +
                "\tbool lid = lid[0];\n" +
                "\tbool boilingMode = mods[0];\n" +
                "\tbool maintainingMode = mods[1];\n" +
                "\t\n" +
                "\tprocess Init {\n" +
                "\t\tstate begin {\n" +
                "\t\t\tstart TempSelection;\n" +
                "\t\t\tstart HeaterController;\n" +
                "\t\t\tstop;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\t\n" +
                "\tprocess TempSelection{\n" +
                "\t\tstate tempSelection {\n" +
                "\t\t\tif (button1 == PRESSED){\n" +
                "\t\t\t\tselectedTemp = TEMP1;\n" +
                "\t\t\t} else {\n" +
                "\t\t\t\tif (button2 == PRESSED){\n" +
                "\t\t\t\t\tselectedTemp = TEMP2;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tif (button3 == PRESSED){\n" +
                "\t\t\t\t\t\tselectedTemp = TEMP3;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\t\n" +
                "\tprocess HeaterController {\n" +
                "\t\tstate begin {\n" +
                "\t\t\tif (boilingButton == PRESSED){\n" +
                "\t\t\t\tboilingMode = true;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate heating {\n" +
                "\t\t\theater = true;\n" +
                "\t\t\tlid = LOCKED;\n" +
                "\t\t\tif (temperature >= BOILING_POINT){\n" +
                "\t\t\t\theater = false;\n" +
                "\t\t\t\tlid = UNLOCKED;\n" +
                "\t\t\t\tboilingMode = false;\n" +
                "\t\t\t\tmaintainingMode = true;\n" +
                "\t\t\t\tset next state;\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\t\n" +
                "\t\tstate maintaining {\n" +
                "\t\t\tif (boilingButton==PRESSED){\n" +
                "\t\t\t\tmaintainingMode = false;\n" +
                "\t\t\t\tboilingMode = true;\n" +
                "\t\t\t\tset state heating;\n" +
                "\t\t\t} else {\n" +
                "\t\t\t\tif (temperature >= selectedTemp){\n" +
                "\t\t\t\t\theater = false;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tif (temperature < selectedTemp - 5) {\n" +
                "\t\t\t\t\t\theater = true;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\t\n" +
                "}");
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            VCGenerator2 gen = new VCGenerator2(context);

            /*DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(gen.graph, writer);
            System.out.println(writer.toString());*/

            gen.generateVC(Path.of("./src/test/testResult"),Path.of("./src/test/testResult"));
            System.out.println("VC conditions generated: " + gen.VCGenerated());
            /*IsabelleCreator creator = new IsabelleCreator();
            VariableMapper mapper = new VariableMapper(context,creator);
            ProgramMetaData metaData = new ProgramMetaData(context,mapper,creator);
            GraphBuilder builder = new GraphBuilder(metaData,mapper,creator);
            ProgramGraph graph = builder.buildProgramGraph(context);

            DOTExporter<IReflexNode, DefaultEdge> exporter =
                    new DOTExporter<>();
            exporter.setVertexAttributeProvider((v) -> {
                Map<String, Attribute> map = new LinkedHashMap<>();
                map.put("label", DefaultAttribute.createAttribute(v.toString()));
                return map;
            });
            Writer writer = new StringWriter();
            exporter.exportGraph(graph, writer);
            System.out.println(writer.toString());
            /*
            generator.printer = new VCPrinter(Path.of("./src/test/testResult"),"test",generator.metaData);
            generator.checker = new RuleChecker(generator.metaData);
            ProgramAnalyzer2 analyzer2 = new ProgramAnalyzer2(generator.metaData);
            generator.collector = analyzer2.generateAttributes(context);

            generator.visitProgram(context);
            generator.visitStack();*/
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        //System.out.println("VC conditions generated: " + generator.conditionsGenerated);
    }
}