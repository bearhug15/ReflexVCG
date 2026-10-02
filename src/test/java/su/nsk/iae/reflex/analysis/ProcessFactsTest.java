package su.nsk.iae.reflex.analysis;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.ir.IrProgram;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static su.nsk.iae.reflex.analysis.AnalysisFixtures.program;

/** Algorithms 4-6 of static-analysis.md, section 3: reachS, reachE, startS and groups. */
class ProcessFactsTest {

    private static ProcessFacts facts(IrProgram program) {
        return new StaticAnalysis(program).getFacts();
    }

    /** The groups as a partition, independent of how they are numbered. */
    private static Set<Set<String>> groups(IrProgram program) {
        ProcessFacts facts = facts(program);
        Map<Integer, Set<String>> members = new LinkedHashMap<>();
        program.getProcesses().forEach(p ->
                members.computeIfAbsent(facts.of(p.getName()).group(), g -> new LinkedHashSet<>()).add(p.getName()));
        return Set.copyOf(members.values());
    }

    // ------------------------------------------------------------------ Algorithm 4

    @Test
    void reachSAndReachEComeFromOwnMovesAndAnyonesPossibleChanges() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { if (x) { stop Q; } if (y) { error; } } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { stop; } }",
                "process S :: node N { state s1 { a = false; } }"));
        assertTrue(facts.of("Q").reachS(), "stopped by P, under a condition");
        assertFalse(facts.of("Q").reachE());
        assertTrue(facts.of("P").reachE(), "fails itself");
        assertTrue(facts.of("R").reachS(), "stops itself");
        assertFalse(facts.of("S").reachS());
        assertFalse(facts.of("S").reachE());
    }

    // ------------------------------------------------------------------ Algorithm 5

    @Test
    void onlyTheFirstProcessBeginsRunning() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { a = true; } }",
                "process Q :: node N { state q1 { a = false; } }"));
        assertFalse(facts.of("P").startS());
        assertTrue(facts.of("Q").startS());
    }

    /** Started in a first state by a process never stopped in the first cycle: never stopped then. */
    @Test
    void aStartInTheFirstStateOfARunningProcessClearsStartS() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { start Q; set next state; } state p2 { a = true; } }",
                "process Q :: node N { state q1 { start R; } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertFalse(facts.of("Q").startS());
        assertFalse(facts.of("R").startS(), "Q itself is never stopped in the first cycle");
    }

    /** A starter that may itself be stopped in the first cycle proves nothing - the misprint. */
    @Test
    void aStarterThatMayBeStoppedDoesNotClearStartS() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { a = true; } }",
                "process Q :: node N { state q1 { start R; } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertTrue(facts.of("Q").startS());
        assertTrue(facts.of("R").startS());
    }

    /** A start that may not happen, or not in the first state, proves nothing. */
    @Test
    void onlyADefiniteStartInTheFirstStateCounts() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { if (x) { start Q; } set next state; } state p2 { start R; } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertTrue(facts.of("Q").startS());
        assertTrue(facts.of("R").startS());
    }

    /** A process between the two that may stop or fail it in its first state blocks it. */
    @Test
    void aProcessBetweenThatMayStopOrFailItBlocks() {
        ProcessFacts facts = facts(program(
                "process P :: node N { state p1 { start Q; start R; start S; set next state; } state p2 { a = true; } }",
                "process B :: node N { state b1 { if (x) { stop Q; } if (y) { error R; } set next state; } state b2 { stop S; } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { a = false; } }",
                "process S :: node N { state s1 { a = false; } }"));
        assertTrue(facts.of("Q").startS(), "B may stop Q in its first state");
        assertTrue(facts.of("R").startS(), "B may fail R in its first state");
        assertFalse(facts.of("S").startS(), "B stops S only in a state it cannot be in during the first cycle");
    }

    // ------------------------------------------------------------------ Algorithm 6

    @Test
    void processesStartedTogetherOnOneSideShareAGroup() {
        Set<Set<String>> groups = groups(program(
                "process P :: node N { state p1 { start Q; start R; set next state; } state p2 { a = true; } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertEquals(Set.of(Set.of("P"), Set.of("Q", "R")), groups);
    }

    /** One declared before the actor, one after: they show the change in different cycles. */
    @Test
    void theSidesOfTheActorSplitAGroup() {
        Set<Set<String>> groups = groups(program(
                "process P :: node N { state p1 { start Q; start R; set next state; } state p2 { a = true; } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process M :: node N { state m1 { if (x) { stop Q; stop R; } } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertTrue(groups.stream().noneMatch(group -> group.containsAll(Set.of("Q", "R"))), groups.toString());
    }

    /** Every statement is visited: a change in one branch splits as well as a definite one. */
    @Test
    void aChangeInABranchSplits() {
        Set<Set<String>> groups = groups(program(
                "process P :: node N { state p1 { start Q; start R; set next state; } state p2 { if (x) { stop Q; } } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { a = false; } }"));
        assertTrue(groups.stream().noneMatch(group -> group.containsAll(Set.of("Q", "R"))), groups.toString());
    }

    /** The repair: a process stopping itself is split from its group (groupRule.rcs). */
    @Test
    void aProcessStoppingItselfIsSplit() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupRule.rcs")).getProgram();
        assertEquals(Set.of(Set.of("Starter"), Set.of("A"), Set.of("B")), groups(program));
    }

    /** The repair: a restart of oneself is filed too (groupSelfRestart.rcs). */
    @Test
    void aProcessRestartingItselfIsSplit() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupSelfRestart.rcs")).getProgram();
        ProcessFacts facts = facts(program);
        assertNotEquals(facts.of("P").group(), facts.of("Q").group());
    }

    /** A change to oneself sits with changes to earlier processes: failing one and then oneself. */
    @Test
    void aSelfChangeKeepsCompanyWithAChangeToAnEarlierProcess() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupStress.rcs")).getProgram();
        ProcessFacts facts = facts(program);
        assertEquals(facts.of("Before").group(), facts.of("SelfErr").group());
        assertEquals(facts.of("L1").group(), facts.of("L2").group());
        assertEquals(facts.of("R1").group(), facts.of("R2").group());
        assertNotEquals(facts.of("L1").group(), facts.of("R1").group());
    }

    /** The initial split: never stopped in the first cycle, or possibly stopped. */
    @Test
    void groupsStartFromTheStartSSplit() {
        Set<Set<String>> groups = groups(program(
                "process P :: node N { state p1 { start Q; set next state; } state p2 { a = true; } }",
                "process Q :: node N { state q1 { a = true; } }",
                "process R :: node N { state r1 { a = false; } }",
                "process S :: node N { state s1 { a = false; } }"));
        assertTrue(groups.contains(Set.of("R", "S")), groups.toString());
        assertTrue(groups.stream().noneMatch(group -> group.contains("Q") && group.contains("R")), groups.toString());
    }
}
