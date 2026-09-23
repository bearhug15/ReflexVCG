package su.nsk.iae.reflex.inv;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.TimeRef;
import su.nsk.iae.reflex.term.TermRenderer;
import su.nsk.iae.reflex.vc.IsabelleRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The structural rules of mainOverview.tex, "Extra Invariants", one at a time, on a
 * program small enough to work each answer out by hand, and then on the test programs.
 */
class StructuralInvariantsTest {

    private static final Path PROGRAMS = Path.of("src/test/resources/programs-new");

    /**
     * A lamp: {@code dark} turns it on when the button is pressed, {@code lit} times out back
     * to {@code dark}, and {@code never} is declared but nothing ever moves there.
     */
    private static final String LAMP = """
            program Lamp {
            	clock 100;
            	import IO {
            		register inp
            		register outp
            	}
            	node Main { clock 100; }
            	const int16 BRIGHT = 5;
            	bool button as (read = inp, bit = 0);
            	bool lamp as (read = outp, write = outp, bit = 0);
            	int16 level = 0;
            	process Switch :: node Main {
            		state dark {
            			lamp = false;
            			if (button) {
            				level = BRIGHT;
            				set state lit;
            			}
            		}
            		state lit {
            			lamp = true;
            			timeout 0t2s {
            				level = 0;
            				set state dark;
            			}
            		}
            		state never {
            			level = 7;
            		}
            	}
            }
            """;

    private final TermRenderer terms = new TermRenderer();

    @Test
    void aProcessIsOnlyFoundInTheStatesSomethingMovesItTo() throws IOException {
        ExtraInvariants found = analyse(LAMP);
        List<ExtraInvariant> states = found.find(Tag.kind(ExtraInvariant.Kind.PROCESS_STATES),
                Tag.process("Switch"));

        assertEquals(1, states.size());
        String formula = render(states.get(0));
        assertTrue(formula.contains("= ''dark''") && formula.contains("= ''lit''"), formula);
        // Nothing moves it to never, stops it or fails it.
        assertFalse(formula.contains("''never''"), formula);
        assertFalse(formula.contains("''stop''"), formula);
        assertFalse(formula.contains("''error''"), formula);
    }

    @Test
    void aStateNeverEnteredGetsNothingSaidAboutIt() throws IOException {
        assertTrue(analyse(LAMP).find(Tag.state("Switch", "never")).isEmpty());
    }

    /**
     * {@code level} is set to BRIGHT on the way into {@code lit} and nothing touches it
     * there, and to 0 on the way into {@code dark} - where it also starts.
     */
    @Test
    void aValueSetOnTheWayInAndLeftAloneIsDefinedForTheState() throws IOException {
        ExtraInvariants found = analyse(LAMP);

        ExtraInvariant on = only(found.find(Tag.kind(ExtraInvariant.Kind.DEFINED_VARIABLES),
                Tag.state("Switch", "lit")));
        assertTrue(render(on).contains("= 5)"), render(on));

        ExtraInvariant off = only(found.find(Tag.kind(ExtraInvariant.Kind.DEFINED_VARIABLES),
                Tag.state("Switch", "dark")));
        assertTrue(render(off).contains("= 0)"), render(off));
        // The lamp (bound as outp_0) is still lit the moment the timeout moves it to dark.
        assertFalse(off.description().contains("outp_0"), off.description());
    }

    /**
     * {@code lit} lights the lamp every time it runs, but the cycle that enters it ran
     * {@code dark}, which put it out: so it is lit only once the switch has stayed on.
     */
    @Test
    void aValueWrittenOnEveryPassIsStabilized() throws IOException {
        ExtraInvariants found = analyse(LAMP);
        ExtraInvariant stable = only(found.find(Tag.kind(ExtraInvariant.Kind.STABILIZED_VARIABLES),
                Tag.state("Switch", "lit")));

        String formula = render(stable);
        assertTrue(formula.contains("(getPstate (predEnv s1) ''Switch'') = ''lit''"), formula);
        assertTrue(stable.description().startsWith("outp_0") && stable.description().contains("= true"),
                stable.description());
        // Not restated: level is already defined for lit.
        assertFalse(stable.description().contains("level"), stable.description());
    }

    @Test
    void aTransitionIsTheGuardsThatHeldJustBeforeIt() throws IOException {
        ExtraInvariants found = analyse(LAMP);

        ExtraInvariant on = only(found.find(Tag.kind(ExtraInvariant.Kind.TRANSITION),
                Tag.state("Switch", "lit")));
        String formula = render(on);
        assertTrue(formula.contains("let s2 = (prevProcState s1 ''Switch'') in"), formula);
        assertTrue(formula.contains("(getPstate s2 ''Switch'') = ''dark''"), formula);
        assertTrue(formula.contains("theBool (getVarVal s2"), "the button's guard: " + formula);
        assertEquals(List.of(on), found.find(Tag.transition(ExtraInvariant.Transition.CONDITIONAL),
                Tag.state("Switch", "lit")));
    }

    @Test
    void theStartStateIsEnteredInitiallyAndATimeoutIsATimedTransition() throws IOException {
        ExtraInvariants found = analyse(LAMP);
        ExtraInvariant off = only(found.find(Tag.kind(ExtraInvariant.Kind.TRANSITION),
                Tag.state("Switch", "dark")));
        String formula = render(off);

        assertTrue(formula.contains("(\\<forall> s3. ((substate s3 s2) \\<longrightarrow> (\\<not> (toEnvP s3))))"),
                formula);
        assertTrue(formula.contains("((ltime s2 ''Switch'') \\<ge> 2000)"), formula);
        assertTrue(found.tagsOf(off).contains(Tag.transition(ExtraInvariant.Transition.INITIAL)));
        assertTrue(found.tagsOf(off).contains(Tag.transition(ExtraInvariant.Transition.TIMED)));
    }

    // ------------------------------------------------------------------ the test programs

    @Test
    void thermopot() throws IOException {
        ExtraInvariants found = analyse(PROGRAMS.resolve("newThermopot.rcs"));

        String states = render(only(found.find(Tag.kind(ExtraInvariant.Kind.PROCESS_STATES),
                Tag.process("HeaterController"))));
        for (String state : List.of("begin", "heating", "maintaining", "stop")) {
            assertTrue(states.contains("''" + state + "''"), states);
        }
        assertFalse(states.contains("''error''"), states);

        // Entered only from heating, once the water boils.
        String maintaining = render(only(found.find(Tag.kind(ExtraInvariant.Kind.TRANSITION),
                Tag.state("HeaterController", "maintaining"))));
        assertTrue(maintaining.contains("''temperature_0''") && maintaining.contains("''heating''"),
                maintaining);

        // Init never stays in begin - it stops itself - so nothing is stabilized there.
        assertTrue(found.find(Tag.kind(ExtraInvariant.Kind.STABILIZED_VARIABLES),
                Tag.state("Init", "begin")).isEmpty());
    }

    @Test
    void annotatedTankNeverStops() throws IOException {
        ExtraInvariants found = analyse(PROGRAMS.resolve("annotatedTank.rcs"));
        String states = render(only(found.find(Tag.kind(ExtraInvariant.Kind.PROCESS_STATES))));
        assertFalse(states.contains("''stop''"), states);
    }

    @Test
    void theAnalysisIsDeterministic() throws IOException {
        Path source = PROGRAMS.resolve("newTurnstile.rcs");
        assertEquals(renderAll(analyse(source)), renderAll(analyse(source)));
    }

    // ------------------------------------------------------------------ helpers

    private ExtraInvariants analyse(String source) throws IOException {
        Path file = Files.createTempFile("lamp", ".rcs");
        Files.writeString(file, source);
        return analyse(file);
    }

    private static ExtraInvariants analyse(Path source) throws IOException {
        ReflexVcg generator = ReflexVcg.load(source);
        IsabelleRenderer renderer = new IsabelleRenderer();
        return new StructuralInvariants(generator.getProgram(), generator.getCfg(),
                new ExpressionRendering() {
                    @Override
                    public String expression(IrExpr expression, String state) {
                        return renderer.renderExpression(expression, state);
                    }

                    @Override
                    public String duration(TimeRef duration, String state) {
                        return renderer.renderDuration(duration, state);
                    }
                }).generate();
    }

    private String render(ExtraInvariant invariant) {
        return terms.render(invariant.formula());
    }

    private String renderAll(ExtraInvariants invariants) {
        StringBuilder all = new StringBuilder();
        for (ExtraInvariant invariant : invariants) {
            all.append(invariant.name()).append(invariants.tagsOf(invariant))
                    .append(render(invariant)).append('\n');
        }
        return all.toString();
    }

    private static ExtraInvariant only(List<ExtraInvariant> found) {
        assertEquals(1, found.size(), found.toString());
        assertNotNull(found.get(0));
        return found.get(0);
    }
}
