package su.nsk.iae.reflex;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Command line entry point.
 *
 * <p>Reads a Reflex program, generates the verification conditions for it, and writes
 * them as Isabelle theories.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("s", "source", true, "Path to the Reflex program (required).");
        options.addOption("o", "output", true, "Directory to write conditions into (default: the source's).");
        options.addOption("g", "graph", false, "Also export the program graph in Graphviz format.");
        options.addOption("a", "analysis", true,
                "Discard conditions for impossible paths: true/false (default true).");
        options.addOption("h", "help", false, "Show this help.");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            usage(options);
            System.exit(2);
            return;
        }

        if (commandLine.hasOption("h") || !commandLine.hasOption("s")) {
            usage(options);
            System.exit(commandLine.hasOption("h") ? 0 : 2);
            return;
        }

        Path source = Path.of(commandLine.getOptionValue("s"));
        if (!Files.isRegularFile(source)) {
            System.err.println("No such file: " + source);
            System.exit(2);
            return;
        }

        Path destination = commandLine.hasOption("o")
                ? Path.of(commandLine.getOptionValue("o"))
                : source.toAbsolutePath().getParent();
        Files.createDirectories(destination);

        try {
            System.out.println("Parsing " + source);
            ReflexVcg generator = ReflexVcg.load(source);

            if (!generator.getAnnotations().getDiagnostics().isEmpty()) {
                System.out.println("Annotation warnings:");
                generator.getAnnotations().getDiagnostics().forEach(d -> System.out.println("  " + d));
            }

            if (!generator.getWriteTargetWarnings().isEmpty()) {
                System.out.println("Warning: a write cannot reach the hardware:");
                generator.getWriteTargetWarnings().forEach(w -> System.out.println("  " + w));
            }

            if ("false".equalsIgnoreCase(commandLine.getOptionValue("a"))) {
                generator.setStaticAnalysis(false);
                System.out.println("Static analysis disabled; every path will be emitted.");
            }

            if (commandLine.hasOption("g")) {
                generator.exportGraph(destination);
                System.out.println("Wrote the program graph to " + destination);
            }

            int generated = generator.generate(destination);
            System.out.println("Wrote " + generated + " verification conditions to " + destination);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void usage(Options options) {
        new HelpFormatter().printHelp(
                "ReflexVCG -s <program.rcs> [-o <dir>] [-g] [-a true|false]",
                "Generates Isabelle/HOL verification conditions for a Reflex program.",
                options, "");
    }
}
