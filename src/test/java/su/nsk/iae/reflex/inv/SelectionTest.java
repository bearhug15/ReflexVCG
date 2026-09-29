package su.nsk.iae.reflex.inv;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The -x key: priorities are cumulative, kinds add to them, and high is always in. */
class SelectionTest {

    @Test
    void theDefaultIsTheHighPriority() {
        assertEquals(Selection.high(), Selection.parse(null));
        assertEquals(Selection.high(), Selection.parse(""));
        assertEquals(EnumSet.of(ExtraInvariant.Kind.PROCESS_STATES), Selection.high().kinds());
    }

    @Test
    void prioritiesIncludeTheOnesAboveThem() {
        assertTrue(Selection.mid().kinds().containsAll(Selection.high().kinds()));
        assertTrue(Selection.low().kinds().containsAll(Selection.mid().kinds()));
        assertTrue(Selection.mid().includes(ExtraInvariant.Kind.TIMER_BOUNDS));
        assertFalse(Selection.mid().includes(ExtraInvariant.Kind.TRANSITION));
        assertTrue(Selection.low().includes(ExtraInvariant.Kind.COPIED_ON_ENTRY));
        assertEquals(Selection.low(), Selection.parse("all"));
    }

    @Test
    void everyDerivedKindHasAPriorityAndOnlyThoseAreSelectable() {
        for (ExtraInvariant.Kind kind : ExtraInvariant.Kind.values()) {
            boolean selectable = kind.isDerived() && kind != ExtraInvariant.Kind.CUSTOM;
            assertEquals(selectable, Selection.low().includes(kind), kind.name());
        }
        assertFalse(ExtraInvariant.Kind.ANNOTATION.isDerived());
        assertEquals(ExtraInvariant.Priority.HIGH, ExtraInvariant.Kind.ANNOTATION.priority());
    }

    @Test
    void kindsCanBeNamedAndTheHighPriorityStaysIn() {
        Selection selection = Selection.parse("timer_bounds, Process-Pairs");
        assertEquals(EnumSet.of(ExtraInvariant.Kind.PROCESS_STATES, ExtraInvariant.Kind.TIMER_BOUNDS,
                ExtraInvariant.Kind.PROCESS_PAIRS), selection.kinds());
        assertEquals(selection, Selection.of(ExtraInvariant.Kind.TIMER_BOUNDS, ExtraInvariant.Kind.PROCESS_PAIRS));
        assertTrue(Selection.parse("mid,transition").includes(ExtraInvariant.Kind.TRANSITION));
    }

    @Test
    void noneTurnsEverythingOff() {
        assertTrue(Selection.parse("none").isEmpty());
        assertTrue(Selection.parse(" NONE ").isEmpty());
    }

    @Test
    void aWordThatIsNeitherIsRefused() {
        IllegalArgumentException refused = assertThrows(IllegalArgumentException.class,
                () -> Selection.parse("mid,sometimes"));
        assertTrue(refused.getMessage().contains("sometimes"), refused.getMessage());
        assertThrows(IllegalArgumentException.class, () -> Selection.parse("annotation"));
        assertThrows(IllegalArgumentException.class, () -> Selection.parse("custom"));
    }
}
