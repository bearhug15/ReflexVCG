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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes generated verification conditions and their supporting theories to a directory.
 *
 * <p>One file per condition, plus the program's own theory (which fixes the clock and the
 * timing lemmas that depend on it), a Requirements theory holding the invariant to be
 * proved, and a copy of ReflexBase.
 */
public final class VcWriter {

    /** The theory holding one invariant per loop, which every condition imports. */
    public static final String LOOP_THEORY = "LoopInvariants";

    /** The names the annotation translator gives to the states it binds. */
    private static final Pattern BOUND_STATE = Pattern.compile("\\bsa\\d+\\b");

    private final Path destination;
    private final String programName;
    private final IsabelleRenderer renderer = new IsabelleRenderer();
    /** Lemmas already written, so a repeated obligation is not written twice. */
    private final Set<String> seen = new HashSet<>();
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
        writeSupportingTheories(program, List.of(), List.of(), List.of());
    }

    public void writeSupportingTheories(IrProgram program, List<String> extraDefinitions) {
        writeSupportingTheories(program, extraDefinitions, List.of(), List.of());
    }

    public void writeSupportingTheories(IrProgram program, List<String> extraDefinitions,
                                        List<String> globalInvariants) {
        writeSupportingTheories(program, extraDefinitions, globalInvariants, List.of());
    }

    /**
     * @param extraDefinitions definitions contributed by the extra-invariant stage,
     *                         written into the requirements theory after the invariant
     * @param globalInvariants invariants written on the program, its processes or their
     *                         states, conjoined into the invariant itself
     * @param loopInvariants   one per loop, each rendered into {@value #LOOP_THEORY}
     */
    public void writeSupportingTheories(IrProgram program, List<String> extraDefinitions,
                                        List<String> globalInvariants,
                                        List<RenderedLoopInvariant> loopInvariants) {
        // ReflexBase defines the state and its values, ReflexLemmas the facts about them,
        // ReflexPatterns the reusable proof patterns built on those.
        copyResource("ReflexTheory/ReflexBase.thy", "ReflexBase.thy");
        copyResource("ReflexTheory/ReflexLemmas.thy", "ReflexLemmas.thy");
        copyResource("ReflexTheory/ReflexPatterns.thy", "ReflexPatterns.thy");
        write(baseTheoryName() + ".thy", renderer.renderTheory(
                baseTheoryName(), List.of("ReflexPatterns"), programTheoryBody(program)));
        write(LOOP_THEORY + ".thy", renderer.renderTheory(
                LOOP_THEORY, List.of("ReflexPatterns"), loopInvariantBody(loopInvariants)));
        write("Requirements.thy", renderer.renderTheory(
                "Requirements", List.of("ReflexPatterns"),
                requirementsBody(globalInvariants) + String.join("\n", extraDefinitions)));
    }

    /**
     * A loop invariant ready to be written: its name, the loop it belongs to, and the
     * formula defining it - null when no {@code [invariant: ...]} was written for the loop.
     */
    public record RenderedLoopInvariant(String name, int line, String formula) {
    }

    /**
     * Writes one condition, numbered in the order they were generated. The name says what
     * the condition is for, so a failing proof points at the right thing: VC for a cycle,
     * ASSUME and ASSERT for the annotations that have to be discharged, LOOP for the two
     * halves of a loop invariant.
     *
     * <p>A condition that has already been written is skipped. An obligation depends only
     * on the path up to where it is stated, so every path that continues past it would
     * otherwise repeat it word for word.
     */
    public void write(VerificationCondition condition) {
        String lemma = renderer.renderLemma(condition);
        if (!seen.add(withCanonicalBoundNames(lemma))) {
            return;
        }
        String name = programName + "_" + prefixOf(condition.getKind()) + written;
        write(name + ".thy", renderer.renderTheory(
                name, List.of(baseTheoryName(), LOOP_THEORY, "Requirements"), lemma));
        written++;
    }

    /**
     * The lemma with its bound state names renumbered from one, used as the key deciding
     * whether it has been written before.
     *
     * <p>Bound names are drawn from a counter that runs across the whole program, so two
     * statements of the same obligation differ in nothing but those names. Renaming them
     * makes such a pair compare equal; the file itself keeps the names it was given.
     */
    private static String withCanonicalBoundNames(String lemma) {
        Map<String, String> renamed = new LinkedHashMap<>();
        Matcher matcher = BOUND_STATE.matcher(lemma);
        StringBuilder canonical = new StringBuilder();
        while (matcher.find()) {
            String name = renamed.computeIfAbsent(matcher.group(), n -> "sa" + (renamed.size() + 1));
            matcher.appendReplacement(canonical, name);
        }
        matcher.appendTail(canonical);
        return canonical.toString();
    }

    private static String prefixOf(VerificationCondition.Kind kind) {
        switch (kind) {
            case ASSUME:
                return "ASSUME";
            case ASSERT:
                return "ASSERT";
            case LOOP_ENTRY:
                return "LOOPENTRY";
            case LOOP_PRESERVED:
                return "LOOPSTEP";
            default:
                return "VC";
        }
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

    /**
     * Declares an invariant for each loop that was written without one.
     *
     * <p>Left uninterpreted on purpose. The loop's conditions say what such an invariant
     * would have to satisfy - it holds on entry, an iteration keeps it, and it is all the
     * path past the loop may assume - and giving it a definition here is what makes them
     * provable. Defining it as True would make the first two trivial and say nothing about
     * the state the loop leaves behind, which is a weaker claim than it looks.
     */
    private String loopInvariantBody(List<RenderedLoopInvariant> loopInvariants) {
        if (loopInvariants.isEmpty()) {
            return "(* The program has no loops. *)\n";
        }
        StringBuilder body = new StringBuilder();
        for (RenderedLoopInvariant invariant : loopInvariants) {
            body.append("(* the loop at line ").append(invariant.line()).append(" *)\n");
            if (invariant.formula() == null) {
                body.append("(* No [invariant: ...] was written for it, so this stands\n")
                        .append("   uninterpreted. Give it a definition saying what the loop\n")
                        .append("   preserves; until then the loop's conditions cannot be proved. *)\n")
                        .append("consts ").append(invariant.name())
                        .append(" :: \"state \\<Rightarrow> bool\"\n\n");
            } else {
                body.append("definition ").append(invariant.name())
                        .append(" :: \"state \\<Rightarrow> bool\" where\n")
                        .append("\"").append(invariant.name()).append(" s =\n")
                        .append(invariant.formula()).append("\"\n\n");
            }
        }
        return body.toString();
    }

    /**
     * The invariant the conditions are stated against. Invariants written on the program,
     * a process or a state are conjoined into it, which is what makes them global: every
     * condition then carries them without restating them.
     */
    private String requirementsBody(List<String> invariants) {
        String body = invariants.isEmpty() ? "True" : String.join("\n\\<and> ", invariants);
        return "definition inv where\n\"inv s =\n" + body + "\n\"\n";
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
