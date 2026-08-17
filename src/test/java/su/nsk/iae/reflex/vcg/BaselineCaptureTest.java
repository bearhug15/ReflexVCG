package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.stream.Stream;

/**
 * Regenerates the VC output for every program in src/test/resources/programs into
 * target/baseline/{analysis,noanalysis}/&lt;program&gt;/, one directory per program so the
 * runs no longer overwrite each other.
 *
 * This is the reference capture for the grammar migration / pipeline rework: the digest
 * built from these directories (see tools/baseline_digest.py) is the structural contract
 * the reworked pipeline must reproduce.
 */
class BaselineCaptureTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs");
    private static final Path OUT = Path.of("target/baseline");

    @Test
    void captureBaseline() throws IOException {
        deleteFilesUnder(OUT);
        List<Path> programs;
        try (Stream<Path> s = Files.list(PROGRAMS)) {
            programs = s.filter(p -> p.getFileName().toString().endsWith(".rx"))
                        .sorted()
                        .toList();
        }
        if (programs.isEmpty()) {
            throw new IllegalStateException("No .rx programs found in " + PROGRAMS.toAbsolutePath());
        }
        for (Path program : programs) {
            capture(program, true);
            capture(program, false);
        }
    }

    private void capture(Path program, boolean staticAnalysis) throws IOException {
        String name = program.getFileName().toString().replaceFirst("\\.rx$", "");
        Path dest = OUT.resolve(staticAnalysis ? "analysis" : "noanalysis").resolve(name);
        Files.createDirectories(dest);

        CharStream inputStream = CharStreams.fromPath(program);
        ReflexLexer lexer = new ReflexLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReflexParser parser = new ReflexParser(tokenStream);
        ReflexParser.ProgramContext context = parser.program();

        VCGenerator2 gen = new VCGenerator2(context, false, false, false, staticAnalysis);
        gen.generateVC(program, dest);

        System.out.println("baseline " + (staticAnalysis ? "analysis  " : "noanalysis")
                + " " + name + ": " + gen.VCGenerated() + " VCs");
    }

    /**
     * Clears previously generated output without removing directories: on Windows a
     * directory that any process has as its working directory cannot be deleted, and
     * stale .thy files would otherwise distort the VC counts.
     */
    private static void deleteFilesUnder(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> s = Files.walk(root)) {
            for (Path p : s.filter(Files::isRegularFile).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}
