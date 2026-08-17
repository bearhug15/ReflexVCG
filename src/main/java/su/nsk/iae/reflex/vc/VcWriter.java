package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.TimeRef;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Writes generated verification conditions and their supporting theories to a directory.
 *
 * <p>One file per condition, plus the program's own theory (which fixes the clock and the
 * timing lemmas that depend on it), a Requirements theory holding the invariant to be
 * proved, and a copy of ReflexBase.
 */
public final class VcWriter {

    private final Path destination;
    private final String programName;
    private final IsabelleRenderer renderer = new IsabelleRenderer();
    private int written;

    public VcWriter(Path destination, String programName) {
        if (!Files.isDirectory(destination)) {
            throw new IllegalArgumentException("Not a directory: " + destination);
        }
        this.destination = destination;
        this.programName = programName;
    }

    public int getWritten() {
        return written;
    }

    /** Copies ReflexBase and writes the program and requirements theories. */
    public void writeSupportingTheories(IrProgram program) {
        copyResource("ReflexTheory/ReflexBase.thy", "ReflexBase.thy");
        write(baseTheoryName() + ".thy", renderer.renderTheory(
                baseTheoryName(), List.of("ReflexBase"), programTheoryBody(program)));
        write("Requirements.thy", renderer.renderTheory(
                "Requirements", List.of("ReflexBase"), requirementsBody()));
    }

    /** Writes one condition, numbered in the order they were generated. */
    public void write(VerificationCondition condition) {
        String name = programName + "_VC" + written;
        write(name + ".thy", renderer.renderTheory(
                name, List.of(baseTheoryName(), "Requirements"), renderer.renderLemma(condition)));
        written++;
    }

    private String baseTheoryName() {
        return programName + "Theory";
    }

    /**
     * The program's clock, and the timing function built on it. ltime counts how long a
     * process has been in its current state, which is what a timeout is compared against.
     */
    private String programTheoryBody(IrProgram program) {
        String clock = Long.toString(clockTicks(program.getClock()));
        return "fun ltime:: \"state \\<Rightarrow> process \\<Rightarrow> nat\" where\n"
                + "\"ltime emptyState _ = 0\"\n"
                + "| \"ltime (toEnv s) p = (ltime s p) + " + clock + "\"\n"
                // ReflexBase has a single setVar constructor, where the previous model
                // had one per scalar type.
                + "| \"ltime (setVar s _ _) p = ltime s p\"\n"
                + "| \"ltime (setPstate s p1 _) p = (if p=p1 then 0 else ltime s p)\"\n"
                + "| \"ltime (reset s p1) p = (if p=p1 then 0 else ltime s p)\"\n"
                + "\n"
                + "lemma ltime_mult:\n"
                + "\"ltime s p mod " + clock + " = 0\"\n"
                + "  by (induction s) (auto)\n";
    }

    private String requirementsBody() {
        return "definition inv where\n\"inv s =\nTrue\n\"\n";
    }

    private static long clockTicks(TimeRef clock) {
        return clock.getKind() == TimeRef.Kind.TIME_LITERAL
                ? IsabelleRenderer.parseTimeMillis(clock.getText())
                : IsabelleRenderer.parseInteger(clock.getText());
    }

    private void write(String fileName, String content) {
        try {
            Files.writeString(destination.resolve(fileName), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + fileName, e);
        }
    }

    private void copyResource(String resource, String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource: " + resource);
            }
            Files.copy(input, destination.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot copy " + resource, e);
        }
    }
}
