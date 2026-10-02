package su.nsk.iae.reflex.inv;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.term.Terms;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every way of deriving invariants, one at a time, on programs small enough to work each
 * answer out by hand (extra-invariants.md has them worked out):
 * {@code programs-extra/lamp.rcs} for what a single process's states say, and
 * {@code programs-extra/crew.rcs} for what relates processes. Then the stages, the
 * pluggable sources, and the test programs.
 */
class StructuralInvariantsTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");
    private static final Path LAMP = Path.of("src/test/resources/programs-extra/lamp.rcs");
    private static final Path CREW = Path.of("src/test/resources/programs-extra/crew.rcs");

    private final TermRenderer terms = new TermRenderer();

    // ------------------------------------------------------------------ process states (high)

    @Test
    void aProcessIsOnlyFoundInTheStatesSomethingMovesItTo() throws IOException {
        ExtraInvariant states = only(analyse(LAMP, Selection.high()), ExtraInvariant.Kind.PROCESS_STATES);

        assertEquals("(((getPstate s ''Switch'') = ''dark'') \\<or> ((getPstate s ''Switch'') = ''lit''))",
                render(states), "never, stop and error are never reached");
        assertEquals(ExtraInvariant.Priority.HIGH, states.priority());
    }

    @Test
    void theHighPriorityIsAllTheDefaultDerives() throws IOException {
        ExtraInvariants found = analyse(CREW, Selection.high());
        assertEquals(3, found.size(), "one per process");
        assertTrue(found.all().stream().allMatch(i -> i.kind() == ExtraInvariant.Kind.PROCESS_STATES));
    }

    /**
     * The high priority joins the global invariant, so it has to stand on its own: asking
     * for more must not change it.
     */
    @Test
    void processStatesAreTheSameWhateverElseIsAskedFor() throws IOException {
        for (Path program : List.of(LAMP, CREW, PROGRAMS.resolve("newTurnstile.rcs"))) {
            List<String> high = rendered(analyse(program, Selection.high()), ExtraInvariant.Kind.PROCESS_STATES);
            List<String> low = rendered(analyse(program, Selection.low()), ExtraInvariant.Kind.PROCESS_STATES);
            assertEquals(high, low, program.toString());
        }
    }

    // ------------------------------------------------------------------ variable values (mid)

    /** level is set to BRIGHT on the way into lit and to 0 on the way into dark. */
    @Test
    void aConstantSetOnTheWayInAndLeftAloneIsDefined() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.mid());

        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> ((theInt (getVarVal s ''#level'' [])) = 5))",
                render(only(found, ExtraInvariant.Kind.DEFINED_VARIABLES, Tag.state("Switch", "lit"))));
        ExtraInvariant dark = only(found, ExtraInvariant.Kind.DEFINED_VARIABLES, Tag.state("Switch", "dark"));
        assertTrue(render(dark).contains("= 0)"), render(dark));
        // The lamp is still lit the cycle the timeout moves the switch back to dark.
        assertFalse(dark.description().contains("outp_0"), dark.description());
    }

    /** lit lights the lamp every pass, but the cycle entering lit ran dark, which put it out. */
    @Test
    void aConstantWrittenOnEveryPassIsStabilized() throws IOException {
        ExtraInvariant lit = only(analyse(LAMP, Selection.mid()), ExtraInvariant.Kind.STABILIZED_VARIABLES,
                Tag.state("Switch", "lit"));
        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> "
                + "(((getPstate (predEnv s) ''Switch'') = ''lit'') \\<longrightarrow> (theBool (getVarVal s ''outp_0'' []))))",
                render(lit));
    }

    /**
     * Stabilized facts that contradict each other mean the process never stays: kept, they
     * would be vacuous. Thermopot's Init stops itself in the cycle it starts.
     */
    @Test
    void aStateNeverStayedInGetsNoStabilizedValues() throws IOException {
        ExtraInvariants found = analyse(PROGRAMS.resolve("newThermopot.rcs"), Selection.mid());
        assertTrue(found.find(Tag.kind(ExtraInvariant.Kind.STABILIZED_VARIABLES), Tag.state("Init", "begin")).isEmpty());
    }

    // ------------------------------------------------------------------ unchanged since entry (mid)

    @Test
    void aVariableNothingWritesWhileInAStateIsUnchangedSinceEntry() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.mid());
        ExtraInvariant lit = only(found, ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY, Tag.state("Switch", "lit"));
        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> (let s2 = (prevProcState s ''Switch'') in "
                + "((getVarVal s ''#setpoint'' []) = (getVarVal s2 ''#setpoint'' []))))", render(lit));
        // level is written on the way out of lit, but lit's defined value already pins it.
        assertFalse(lit.description().contains("level"), lit.description());
    }

    /** jobs is incremented on the way into busy, and not while Worker is in either state. */
    @Test
    void aVariableWrittenOnTheWayInIsStillUnchangedSinceEntry() throws IOException {
        ExtraInvariants found = analyse(CREW, Selection.mid());
        assertEquals(2, found.find(Tag.kind(ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY), Tag.variable("#jobs")).size());
    }

    @Test
    void aVariableWrittenInsideTheStateIsNotUnchanged() throws IOException {
        ExtraInvariants found = analyse(CREW, Selection.mid());
        // blink is written on every pass through idle and busy.
        assertTrue(found.find(Tag.kind(ExtraInvariant.Kind.UNCHANGED_SINCE_ENTRY), Tag.variable("outp_0")).isEmpty());
    }

    // ------------------------------------------------------------------ timer bounds (mid)

    @Test
    void aTimeoutThatMovesTheProcessOnBoundsItsTime() throws IOException {
        ExtraInvariant lit = only(analyse(LAMP, Selection.mid()), ExtraInvariant.Kind.TIMER_BOUNDS);
        // 2 s timeout, one 100 ms tick past it at the most.
        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> ((ltime s ''Switch'') < 2100))", render(lit));
        ExtraInvariant busy = only(analyse(CREW, Selection.mid()), ExtraInvariant.Kind.TIMER_BOUNDS);
        assertTrue(render(busy).contains("((ltime s ''Worker'') < 600)"), render(busy));
    }

    @Test
    void aTimeoutThatLeavesTheProcessWhereItIsBoundsNothing() throws IOException {
        Path program = write("""
                program Stay {
                	clock 100;
                	node N { clock 100; }
                	int16 count = 0;
                	process P :: node N {
                		state waiting {
                			timeout 0t1s {
                				count = 1;
                			}
                		}
                	}
                }
                """);
        assertTrue(analyse(program, Selection.mid()).find(Tag.kind(ExtraInvariant.Kind.TIMER_BOUNDS)).isEmpty());
    }

    // ------------------------------------------------------------------ process pairs (mid)

    @Test
    void statesOfTwoProcessesNeverFoundTogetherAreExcluded() throws IOException {
        ExtraInvariants found = analyse(CREW, Selection.mid());
        assertEquals("(((getPstate s ''Starter'') = ''begin'') \\<longrightarrow> "
                        + "(((getPstate s ''Worker'') = ''stop'') \\<and> ((getPstate s ''Helper'') = ''stop'')))",
                render(only(found, ExtraInvariant.Kind.PROCESS_PAIRS, Tag.state("Starter", "begin"))));
        assertEquals("(((getPstate s ''Worker'') = ''idle'') \\<longrightarrow> ((getPstate s ''Helper'') = ''run''))",
                render(only(found, ExtraInvariant.Kind.PROCESS_PAIRS, Tag.state("Worker", "idle"))));
    }

    @Test
    void aSingleProcessHasNoPairs() throws IOException {
        assertTrue(analyse(LAMP, Selection.mid()).find(Tag.kind(ExtraInvariant.Kind.PROCESS_PAIRS)).isEmpty());
    }

    // ------------------------------------------------------------------ static analysis (mid)

    @Test
    void theStaticAnalysisGroupsAreConfirmedAndExported() throws IOException {
        Path source = CREW;
        ReflexVcg generator = ReflexVcg.load(source);
        StructuralInvariants analysis = analysis(generator);
        ExtraInvariants found = analysis.generate(Selection.mid());

        ExtraInvariant group = only(found, ExtraInvariant.Kind.STATIC_ANALYSIS);
        assertEquals("((((getPstate s ''Worker'') = ''stop'') = ((getPstate s ''Helper'') = ''stop'')) \\<and> "
                + "(((getPstate s ''Worker'') = ''error'') = ((getPstate s ''Helper'') = ''error'')))", render(group));
        assertTrue(analysis.getDiagnostics().isEmpty(), analysis.getDiagnostics().toString());
    }

    /** A claim the check does not keep is reported, not exported. */
    @Test
    void anUnconfirmedClaimIsReported() throws IOException {
        ReflexVcg generator = ReflexVcg.load(CREW);
        AnalysisContext context = new AnalysisContext(generator.getProgram(), generator.getCfg(),
                new IsabelleRenderer()::renderExpression);
        StaticAnalysisClaims claims = new StaticAnalysisClaims();
        List<Candidate> guessed = claims.guess(context);
        assertFalse(guessed.isEmpty());

        ExtraInvariants into = new ExtraInvariants();
        claims.build(List.of(), context, into);
        assertTrue(into.isEmpty());
        assertEquals(guessed.size(), claims.unconfirmed().size());
        assertTrue(claims.unconfirmed().stream().anyMatch(c -> c.startsWith("group ")), claims.unconfirmed().toString());
    }

    @Test
    void theMultiProcessProgramsClaimsAreAllConfirmed() throws IOException {
        for (String program : List.of("newThermopot", "newTurnstile", "newSmartLighting", "newBarrier")) {
            StructuralInvariants analysis = analysis(ReflexVcg.load(PROGRAMS.resolve(program + ".rcs")));
            analysis.generate(Selection.of(ExtraInvariant.Kind.STATIC_ANALYSIS));
            assertEquals(List.of(), analysis.getDiagnostics(), program);
        }
    }

    // ------------------------------------------------------------------ transitions (low)

    @Test
    void aTransitionIsTheGuardsThatHeldJustBeforeIt() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.low());
        ExtraInvariant lit = only(found, ExtraInvariant.Kind.TRANSITION, Tag.state("Switch", "lit"));
        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> (let s2 = (prevProcState s ''Switch'') in "
                + "((theBool (getVarVal s2 ''inp_0'' [])) \\<and> ((getPstate s2 ''Switch'') = ''dark''))))", render(lit));
        assertTrue(found.tagsOf(lit).contains(Tag.transition(ExtraInvariant.Transition.CONDITIONAL)));
    }

    @Test
    void theStartStateIsEnteredInitiallyAndATimeoutIsATimedTransition() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.low());
        ExtraInvariant dark = only(found, ExtraInvariant.Kind.TRANSITION, Tag.state("Switch", "dark"));
        assertTrue(render(dark).contains("((toEnvNum emptyState s2) = 0)"), render(dark));
        assertTrue(render(dark).contains("((ltime s2 ''Switch'') \\<ge> 2000)"), render(dark));
        assertTrue(found.tagsOf(dark).contains(Tag.transition(ExtraInvariant.Transition.INITIAL)));
        assertTrue(found.tagsOf(dark).contains(Tag.transition(ExtraInvariant.Transition.TIMED)));
    }

    /**
     * A process started by another before its own body runs: the state it was in is
     * still known, because the walk splits over the states it can have begun in.
     */
    @Test
    void aStartByAnotherProcessSaysWhereTheTargetCameFrom() throws IOException {
        ExtraInvariant run = only(analyse(CREW, Selection.low()), ExtraInvariant.Kind.TRANSITION,
                Tag.state("Helper", "run"));
        assertTrue(render(run).contains("((getPstate s2 ''Starter'') = ''begin'') \\<and> ((getPstate s2 ''Helper'') = ''stop'')"),
                render(run));
    }

    // ------------------------------------------------------------------ copied on entry (low)

    @Test
    void aValueLatchedOnTheWayInIsCopiedOnEntry() throws IOException {
        ExtraInvariant lit = only(analyse(LAMP, Selection.low()), ExtraInvariant.Kind.COPIED_ON_ENTRY);
        assertEquals("(((getPstate s ''Switch'') = ''lit'') \\<longrightarrow> (let s2 = (prevProcState s ''Switch'') in "
                + "((theInt (getVarVal s ''#setpoint'' [])) = (theInt (getVarVal s2 ''dial_0'' [])))))", render(lit));
    }

    @Test
    void aValueTheWaysInDisagreeOnIsNotCopied() throws IOException {
        Path program = write("""
                program Two {
                	clock 100;
                	import IO { register inp }
                	node N { clock 100; }
                	bool a as (read = inp, bit = 0);
                	bool b as (read = inp, bit = 1);
                	bool latched = false;
                	process P :: node N {
                		state idle {
                			if (a) { latched = a; set state armed; }
                			if (b) { latched = b; set state armed; }
                		}
                		state armed { ; }
                	}
                }
                """);
        assertTrue(analyse(program, Selection.low()).find(Tag.kind(ExtraInvariant.Kind.COPIED_ON_ENTRY)).isEmpty());
    }

    // ------------------------------------------------------------------ the wrap

    /**
     * Every derived invariant is written in the wrap an annotation invariant of the same
     * scale gets: at every boundary at or below s that its scope covers.
     */
    @Test
    void everyDerivedInvariantIsWrappedLikeAnAnnotation() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.low());
        String boundaries = "(\\<forall> s1. ((((toEnvP s1) \\<and> (substate s1 s))";
        for (ExtraInvariant invariant : found) {
            String formula = terms.render(invariant.formula());
            if (invariant.kind() == ExtraInvariant.Kind.PROCESS_STATES) {
                // A program-scale invariant: no scope beyond being a boundary.
                assertTrue(formula.startsWith("(\\<forall> s1. (((toEnvP s1) \\<and> (substate s1 s)) \\<longrightarrow> "),
                        formula);
            } else {
                String state = found.tagsOf(invariant).stream().filter(t -> t.key().equals("state"))
                        .findFirst().orElseThrow().value().split("\\.")[1];
                assertTrue(formula.startsWith(boundaries + " \\<and> ((getPstate s1 ''Switch'') = ''" + state
                        + "'')) \\<longrightarrow> "), formula);
            }
        }
    }

    /** The wrap is the annotation translator's own, applied to the claim's body. */
    @Test
    void theWrapIsTheAnnotationWrapper() {
        Term body = new Term.Infix("<", Terms.localTime(ExtraInvariant.STATE, "P"), new Term.Var("600"));
        ExtraInvariant wrapped = ExtraInvariant.wrapped("t", ExtraInvariant.Kind.TIMER_BOUNDS,
                su.nsk.iae.reflex.ann.AnnTranslator.Scale.PSTATE, "P", "busy", body, "");
        assertEquals(su.nsk.iae.reflex.ann.AnnTranslator.invariantWrapper(body, ExtraInvariant.STATE,
                ExtraInvariant.STATE, ExtraInvariant.BOUND, su.nsk.iae.reflex.ann.AnnTranslator.Scale.PSTATE,
                "P", "busy"), wrapped.formula());
        assertEquals("(((getPstate s ''P'') = ''busy'') \\<longrightarrow> ((ltime s ''P'') < 600))",
                terms.render(wrapped.claim()));
    }

    // ------------------------------------------------------------------ the group attribute

    /**
     * groups.rcs: A may stop itself, B never stops. As printed, the static analysis's group
     * computation put them in one group, and the claim that they stop together was false.
     * The repaired computation keeps them apart, so nothing about the two is claimed, and
     * every claim the analysis does make is confirmed.
     */
    @Test
    void theRepairedGroupsMakeNoWrongClaim() throws IOException {
        for (String name : List.of("groups.rcs", "groupRule.rcs", "groupSelfRestart.rcs", "groupStress.rcs")) {
            StructuralInvariants analysis = analysis(ReflexVcg.load(Path.of("src/test/resources/programs-extra/" + name)));
            ExtraInvariants found = analysis.generate(Selection.of(ExtraInvariant.Kind.STATIC_ANALYSIS));
            assertEquals(List.of(), analysis.getDiagnostics(), name);
            assertTrue(found.find(Set.of(Tag.kind(ExtraInvariant.Kind.STATIC_ANALYSIS), Tag.process("A"),
                    Tag.process("B"))).isEmpty(), name);
        }
    }

    // ------------------------------------------------------------------ selection and sources

    @Test
    void onlyTheKindsSelectedAreDerived() throws IOException {
        ExtraInvariants found = analyse(LAMP, Selection.of(ExtraInvariant.Kind.TIMER_BOUNDS));
        for (ExtraInvariant invariant : found) {
            assertTrue(Set.of(ExtraInvariant.Kind.PROCESS_STATES, ExtraInvariant.Kind.TIMER_BOUNDS)
                    .contains(invariant.kind()), invariant.name());
        }
        assertTrue(analyse(LAMP, Selection.none()).isEmpty());
    }

    /**
     * A source plugged in from outside goes through the same check: a true guess is kept,
     * a false one dropped, and the survivor becomes an invariant.
     */
    @Test
    void aPluggedInSourceIsCheckedLikeAnyOther() throws IOException {
        // "In lit, level is this" - with the hypothesis a real source would give it.
        record LevelAtMost(long bound) implements Candidate {
            @Override
            public Set<String> concerns() {
                return Set.of("Switch");
            }

            @Override
            public boolean assume(CycleStart start) {
                return !"lit".equals(start.initialState("Switch")) || start.assumeValue("#level", Value.of(bound));
            }

            @Override
            public boolean holdsAt(Boundary boundary) {
                if (!"lit".equals(boundary.pstate("Switch"))) {
                    return boundary.pstate("Switch") != null;
                }
                Value level = boundary.value("#level");
                return level != null && level.number().longValue() == bound;
            }
        }
        List<Long> kept = new ArrayList<>();
        CandidateSource<LevelAtMost> source = new CandidateSource<>() {
            @Override
            public Set<ExtraInvariant.Kind> kinds() {
                return Set.of(ExtraInvariant.Kind.CUSTOM);
            }

            @Override
            public List<LevelAtMost> guess(AnalysisContext context) {
                return List.of(new LevelAtMost(5), new LevelAtMost(4));
            }

            @Override
            public void build(List<LevelAtMost> survivors, AnalysisContext context, ExtraInvariants into) {
                survivors.forEach(s -> kept.add(s.bound()));
                for (LevelAtMost survivor : survivors) {
                    Term level = Terms.valueGetter(ExtraInvariant.STATE, context.typeOf("#level"), "#level", List.of());
                    into.add(new ExtraInvariant("level_at_most_" + survivor.bound(), ExtraInvariant.Kind.CUSTOM,
                            ExtraInvariant.Priority.MID,
                            new Term.Infix("\\<le>", level, new Term.Var(Long.toString(survivor.bound()))),
                            "level never exceeds " + survivor.bound()));
                }
            }
        };
        ReflexVcg generator = ReflexVcg.load(LAMP);
        ExtraInvariants found = analysis(generator).addSource(source).generate(Selection.high());

        assertEquals(List.of(5L), kept, "level is 5 in lit, not 4");
        assertEquals(1, found.find(Tag.kind(ExtraInvariant.Kind.CUSTOM)).size());
    }

    @Test
    void theAnalysisIsDeterministic() throws IOException {
        Path source = PROGRAMS.resolve("newTurnstile.rcs");
        assertEquals(renderAll(analyse(source, Selection.low())), renderAll(analyse(source, Selection.low())));
    }

    @Test
    void everyTestProgramIsAnalysed() throws IOException {
        try (var files = Files.list(PROGRAMS)) {
            for (Path program : files.filter(f -> f.toString().endsWith(".rcs")).sorted().toList()) {
                ExtraInvariants found = analyse(program, Selection.low());
                assertFalse(found.find(Tag.kind(ExtraInvariant.Kind.PROCESS_STATES)).isEmpty(), program.toString());
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    static ExtraInvariants analyse(Path source, Selection selection) throws IOException {
        return analysis(ReflexVcg.load(source)).generate(selection);
    }

    static StructuralInvariants analysis(ReflexVcg generator) {
        return new StructuralInvariants(generator.getProgram(), generator.getCfg(),
                new IsabelleRenderer()::renderExpression);
    }

    private static Path write(String source) throws IOException {
        Path file = Files.createTempFile("extra", ".rcs");
        Files.writeString(file, source);
        return file;
    }

    /** What the invariant says about one boundary: the claim, without the wrap. */
    private String render(ExtraInvariant invariant) {
        return terms.render(invariant.claimOrFormula());
    }

    private List<String> rendered(ExtraInvariants found, ExtraInvariant.Kind kind) {
        return found.find(Tag.kind(kind)).stream().map(this::render).toList();
    }

    private String renderAll(ExtraInvariants invariants) {
        StringBuilder all = new StringBuilder();
        for (ExtraInvariant invariant : invariants) {
            all.append(invariant.name()).append(invariants.tagsOf(invariant))
                    .append(render(invariant)).append('\n');
        }
        return all.toString();
    }

    private static ExtraInvariant only(ExtraInvariants found, ExtraInvariant.Kind kind, Tag... more) {
        List<Tag> tags = new ArrayList<>(List.of(more));
        tags.add(Tag.kind(kind));
        List<ExtraInvariant> matching = found.find(tags);
        assertEquals(1, matching.size(), kind + " " + List.of(more) + ": " + matching);
        return matching.get(0);
    }
}
