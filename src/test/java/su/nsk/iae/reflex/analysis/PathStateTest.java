package su.nsk.iae.reflex.analysis;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.analysis.Attributes.Change;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What a path knows about a process, event by event (static-analysis.md, section 4.0). */
class PathStateTest {

    private static final Set<Status> ANY = EnumSet.allOf(Status.class);

    private static Set<Status> poss(PathState path) {
        return path.possibleStatuses("W", ANY);
    }

    @Test
    void nothingKnownLeavesTheStatusesTheCycleBeganWith() {
        assertEquals(ANY, poss(PathState.INITIAL));
        assertEquals(EnumSet.of(Status.ACTIVE), PathState.INITIAL.possibleStatuses("W", EnumSet.of(Status.ACTIVE)));
    }

    @Test
    void checksIntersect() {
        PathState path = PathState.INITIAL
                .asserting(new Event.StatusAsserted("W", Term.Activity.NONERROR))
                .asserting(new Event.StatusAsserted("W", Term.Activity.NONSTOP));
        assertEquals(EnumSet.of(Status.ACTIVE), poss(path));
        assertTrue(poss(path.asserting(new Event.StatusAsserted("W", Term.Activity.INACTIVE))).isEmpty());
    }

    @Test
    void aDispatchIsACheckOfItsState() {
        assertEquals(EnumSet.of(Status.STOP), poss(PathState.INITIAL.asserting(new Event.StateAsserted("W", "stop"))));
        assertEquals(EnumSet.of(Status.ACTIVE), poss(PathState.INITIAL.asserting(new Event.StateAsserted("W", "run"))));
    }

    @Test
    void aChangeReplacesWhatWasKnown() {
        PathState path = PathState.INITIAL
                .asserting(new Event.StatusAsserted("W", Term.Activity.STOP))
                .asserting(new Event.Changed("W", Change.START));
        assertEquals(EnumSet.of(Status.ACTIVE), poss(path));
    }

    @Test
    void aPossibleChangeAddsItsStatus() {
        PathState path = PathState.INITIAL
                .asserting(new Event.Changed("W", Change.START))
                .asserting(new Event.MayChange("W", Change.STOP));
        assertEquals(EnumSet.of(Status.ACTIVE, Status.STOP), poss(path));
    }

    @Test
    void factsAboutOtherProcessesDoNotCount() {
        PathState path = PathState.INITIAL
                .asserting(new Event.StatusAsserted("V", Term.Activity.STOP))
                .asserting(new Event.Changed("V", Change.ERROR));
        assertEquals(ANY, poss(path));
        assertFalse(path.changed("W"));
    }

    @Test
    void onlyADefiniteChangeCountsAsChanged() {
        assertFalse(PathState.INITIAL.asserting(new Event.MayChange("W", Change.STOP)).changed("W"));
        assertTrue(PathState.INITIAL.asserting(new Event.Changed("W", Change.STOP)).changed("W"));
    }

    /** A loop's possible changes come from the cut's attributes; a statement's are its own. */
    @Test
    void attributesBecomeDefiniteAndPossibleChanges() {
        Attributes loop = AttributeCalculus.optional(Attributes.change("W", Change.STOP, false, false, Set.of()));
        PathState path = PathState.INITIAL.andThen(Attributes.change("W", Change.START, false, false, Set.of()))
                .andThen(loop);
        assertTrue(path.changed("W"));
        assertEquals(EnumSet.of(Status.ACTIVE, Status.STOP), poss(path));
    }

    @Test
    void aTimerResetLastsUntilALoop() {
        PathState reset = PathState.INITIAL.asserting(new Event.TimerReset("W"));
        assertTrue(reset.timerReset("W"));
        assertFalse(reset.timerReset("V"));
        assertFalse(reset.asserting(new Event.TimePassed()).timerReset("W"));
        assertTrue(reset.asserting(new Event.TimePassed()).asserting(new Event.TimerReset("W")).timerReset("W"));
    }
}
