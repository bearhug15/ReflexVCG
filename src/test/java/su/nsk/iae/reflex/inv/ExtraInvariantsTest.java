package su.nsk.iae.reflex.inv;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.term.Term;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The container: tags attached and detached, and searching by a set of them. */
class ExtraInvariantsTest {

    private final ExtraInvariants container = new ExtraInvariants();

    private final ExtraInvariant pumpStates = invariant("pump_states", ExtraInvariant.Kind.PROCESS_STATES);
    private final ExtraInvariant pumpOn = invariant("pump_on", ExtraInvariant.Kind.DEFINED_VARIABLES);
    private final ExtraInvariant pumpOnStable = invariant("pump_on_stable", ExtraInvariant.Kind.STABILIZED_VARIABLES);
    private final ExtraInvariant pumpOnEntry = invariant("pump_on_entry", ExtraInvariant.Kind.TRANSITION);
    private final ExtraInvariant valveOpen = invariant("valve_open", ExtraInvariant.Kind.DEFINED_VARIABLES);

    ExtraInvariantsTest() {
        container.add(pumpStates, Tag.process("Pump"));
        container.add(pumpOn, Tag.process("Pump"), Tag.state("Pump", "on"), Tag.variable("x"));
        container.add(pumpOnStable, Tag.process("Pump"), Tag.state("Pump", "on"), Tag.variable("y"));
        container.add(pumpOnEntry, Tag.process("Pump"), Tag.state("Pump", "on"));
        container.add(valveOpen, Tag.process("Valve"), Tag.state("Valve", "open"), Tag.variable("x"));
    }

    @Test
    void kindAndGroupAreTaggedWithoutBeingAskedFor() {
        assertEquals(List.of(pumpStates, pumpOn, pumpOnStable, valveOpen),
                container.find(Tag.group(ExtraInvariant.Group.ADVANCED)));
        assertEquals(List.of(pumpOnEntry), container.find(Tag.group(ExtraInvariant.Group.OPTIONAL)));
        assertEquals(List.of(pumpOn, valveOpen),
                container.find(Tag.kind(ExtraInvariant.Kind.DEFINED_VARIABLES)));
    }

    @Test
    void findReturnsWhatCarriesEveryTag() {
        assertEquals(List.of(pumpOn, pumpOnStable, pumpOnEntry), container.find(Tag.state("Pump", "on")));
        assertEquals(List.of(pumpOn, pumpOnStable), container.find(Tag.state("Pump", "on"),
                Tag.group(ExtraInvariant.Group.ADVANCED)));
        assertEquals(List.of(pumpOn, valveOpen), container.find(Tag.variable("x")));
        assertEquals(List.of(pumpOn), container.find(Tag.variable("x"), Tag.process("Pump")));
        assertTrue(container.find(Tag.variable("x"), Tag.state("Pump", "off")).isEmpty());
    }

    @Test
    void noTagsMatchesEverythingInTheOrderAdded() {
        assertEquals(List.of(pumpStates, pumpOn, pumpOnStable, pumpOnEntry, valveOpen), container.find());
        assertEquals(container.all(), container.find(List.of()));
    }

    @Test
    void findAnyReturnsWhatCarriesSomeTag() {
        assertEquals(List.of(pumpOn, pumpOnStable, valveOpen),
                container.findAny(List.of(Tag.variable("x"), Tag.variable("y"))));
    }

    @Test
    void tagsCanBeAttachedAndDetachedLater() {
        Tag reviewed = Tag.of("reviewed", "yes");
        container.attach(valveOpen, reviewed);
        container.attach(pumpStates, reviewed);
        assertEquals(List.of(pumpStates, valveOpen), container.find(reviewed));
        assertTrue(container.tagsOf(valveOpen).contains(reviewed));

        container.detach(pumpStates, reviewed);
        assertEquals(List.of(valveOpen), container.find(reviewed));
        assertFalse(container.tagsOf(pumpStates).contains(reviewed));
    }

    @Test
    void removingAnInvariantRemovesItFromEverySearch() {
        assertTrue(container.remove(pumpOn));
        assertEquals(List.of(valveOpen), container.find(Tag.variable("x")));
        assertEquals(4, container.size());
        assertFalse(container.remove(pumpOn));
    }

    @Test
    void namesAreUnique() {
        assertSame(pumpOn, container.get("pump_on"));
        assertThrows(IllegalArgumentException.class,
                () -> container.add(invariant("pump_on", ExtraInvariant.Kind.TRANSITION)));
    }

    @Test
    void anInvariantNotHeldCannotBeTagged() {
        assertThrows(IllegalArgumentException.class,
                () -> container.attach(invariant("elsewhere", ExtraInvariant.Kind.TRANSITION), Tag.process("P")));
    }

    @Test
    void tagsOfIsReadOnly() {
        Set<Tag> tags = container.tagsOf(pumpOn);
        assertThrows(UnsupportedOperationException.class, () -> tags.add(Tag.of("a", "b")));
    }

    @Test
    void anInvariantIsStatedAtAnotherStateBySubstitution() {
        ExtraInvariant invariant = new ExtraInvariant("p", ExtraInvariant.Kind.PROCESS_STATES,
                new Term.App("toEnvP", List.of(ExtraInvariant.STATE)), "");
        assertEquals(new Term.App("toEnvP", List.of(new Term.Var("st0"))), invariant.at(new Term.Var("st0")));
    }

    private static ExtraInvariant invariant(String name, ExtraInvariant.Kind kind) {
        return new ExtraInvariant(name, kind, new Term.Var("True"), name);
    }
}
