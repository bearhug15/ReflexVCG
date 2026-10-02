package su.nsk.iae.reflex.inv.simulation;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ReflexVcg;
import su.nsk.iae.reflex.analysis.AttributePreparation;
import su.nsk.iae.reflex.analysis.Attributes;
import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.analysis.ProcessFacts;
import su.nsk.iae.reflex.cfg.CfgNode;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The grouping rules of the IVMEM 2026 paper (section 4.7, rules 1 and 2), checked on runs.
 *
 * <p>Two processes in one group must agree, at every point where the generator looks at
 * them, on being in {@code stop} and on being in {@code error}: a path on which they do not is
 * discarded. The generator looks at a process when it dispatches it on its state, so this
 * runs every test program with random inputs and compares, within each cycle, every pair of
 * processes dispatched in it. A disagreement is a feasible path the rule would discard.
 *
 * <p>The groups are computed here exactly as the paper's Algorithms 5 and 6 print them, over
 * the port's statement attributes (Algorithms 1-3), in four variants: as printed with the
 * corrected {@code ResolveStartStates} ({@code p'.startS = false}); as printed with its
 * literal {@code p'.startS = true}; with a process's stop and error of <em>itself</em> filed
 * with the processes declared before it, which is not enough; with every change a process
 * makes to itself filed so, restarts included, which is the repair; and the analysis's own
 * groups ({@code ProcessFacts}), which implement the repair.
 */
class GroupRuleSimulationTest {

    private static final int RUNS = 20;
    private static final int CYCLES = 120;

    enum Variant { PRINTED, PRINTED_LITERAL_STARTS, SELF_STOP_ERROR_AS_PREDECESSOR, SELF_AS_PREDECESSOR, PORT }

    @Test
    void theGroupRuleHoldsOnRunsOnlyOnceSelfChangesSplit() throws IOException {
        Map<Variant, Map<String, String>> broken = new LinkedHashMap<>();
        for (Variant variant : Variant.values()) {
            broken.put(variant, new LinkedHashMap<>());
        }
        for (Path program : programs()) {
            ReflexVcg generator = ReflexVcg.load(program);
            String name = program.getFileName().toString();
            for (Variant variant : Variant.values()) {
                Map<String, Integer> groups = groups(generator.getProgram(), variant);
                if (groups.size() > 1) {
                    System.out.printf("groups %-22s %-22s %s%n", name, variant, groups);
                }
                String violation = firstViolation(generator, groups);
                if (violation != null) {
                    broken.get(variant).put(name, groups + ": " + violation);
                }
            }
        }
        broken.forEach((variant, programs) -> {
            System.out.println("group rule, " + variant + ": " + programs.size() + " program(s) broken");
            programs.forEach((program, why) -> System.out.println("  " + program + " " + why));
        });

        // As printed, the rule discards feasible paths - and the start-state reading does not matter.
        for (Variant printed : List.of(Variant.PRINTED, Variant.PRINTED_LITERAL_STARTS)) {
            assertTrue(broken.get(printed).containsKey("groupRule.rcs"), printed.toString());
            assertTrue(broken.get(printed).containsKey("groups.rcs"), printed.toString());
        }
        // Filing only stops and errors of itself leaves a restart that undoes one unseen.
        assertEquals(Set.of("groupSelfRestart.rcs"), broken.get(Variant.SELF_STOP_ERROR_AS_PREDECESSOR).keySet());
        // Filing every change a process makes to itself with its predecessors is enough, and
        // it is what the analysis implements.
        assertEquals(Map.of(), broken.get(Variant.SELF_AS_PREDECESSOR));
        assertEquals(Map.of(), broken.get(Variant.PORT));
    }

    @Test
    void onTheExampleEveryPrintedVariantGroupsAWithB() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupRule.rcs")).getProgram();
        for (Variant variant : List.of(Variant.PRINTED, Variant.PRINTED_LITERAL_STARTS)) {
            Map<String, Integer> groups = groups(program, variant);
            assertEquals(groups.get("A"), groups.get("B"), variant.toString());
            assertFalse(groups.get("A").equals(groups.get("Starter")), variant.toString());
        }
        Map<String, Integer> repaired = groups(program, Variant.SELF_AS_PREDECESSOR);
        assertEquals(3, Set.copyOf(repaired.values()).size());
        assertEquals(3, Set.copyOf(groups(program, Variant.PORT).values()).size());
    }

    /** The analysis's own groups are the repaired Algorithm 6's, on every test program. */
    @Test
    void theAnalysisGroupsAsTheRepairedAlgorithm() throws IOException {
        for (Path path : programs()) {
            IrProgram program = ReflexVcg.load(path).getProgram();
            assertEquals(partition(groups(program, Variant.SELF_AS_PREDECESSOR)),
                    partition(groups(program, Variant.PORT)), path.toString());
        }
    }

    private static Set<Set<String>> partition(Map<String, Integer> groups) {
        Map<Integer, Set<String>> members = new LinkedHashMap<>();
        groups.forEach((process, group) -> members.computeIfAbsent(group, g -> new LinkedHashSet<>()).add(process));
        return Set.copyOf(members.values());
    }

    /** The runs of groupStress reach what it was written to exercise, so passing means something. */
    @Test
    void theStressRunsReachEveryStopAndError() throws IOException {
        ReflexVcg generator = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupStress.rcs"));
        Set<String> seen = new LinkedHashSet<>();
        for (int run = 0; run < RUNS; run++) {
            Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(run));
            machine.onActivation(now -> seen.add(now.getProcess() + "=" + now.getState()));
            machine.start(state -> { });
            for (int cycle = 1; cycle <= CYCLES; cycle++) {
                machine.cycle(state -> { });
            }
        }
        for (String expected : List.of("L1=stop", "L2=stop", "R1=stop", "R2=stop", "L1=a", "R1=a",
                "Before=error", "SelfErr=error", "Before=a", "SelfErr=a")) {
            assertTrue(seen.contains(expected), expected + " never seen in " + seen);
        }
    }

    /** The startS the corrected Algorithm 5 gives: false for all three processes of the example. */
    @Test
    void correctedStartStates() throws IOException {
        IrProgram program = ReflexVcg.load(Path.of("src/test/resources/programs-extra/groupRule.rcs")).getProgram();
        Map<IrNode, Attributes> attributes = new AttributePreparation(program).run();
        assertEquals(Map.of("Starter", false, "A", false, "B", false), startS(program, attributes, false));
        assertEquals(Map.of("Starter", false, "A", true, "B", true), startS(program, attributes, true));
    }

    // ------------------------------------------------------------------ the check

    /** The first pair of same-group processes seen to disagree in some cycle of some run. */
    private static String firstViolation(ReflexVcg generator, Map<String, Integer> groups) {
        for (int run = 0; run < RUNS; run++) {
            Machine machine = new Machine(generator.getProgram(), generator.getCfg(), new Random(run));
            List<CfgNode.InState> seen = new ArrayList<>();
            String[] found = {null};
            machine.onActivation(now -> {
                for (CfgNode.InState before : seen) {
                    if (found[0] == null && groups.get(before.getProcess()).equals(groups.get(now.getProcess()))
                            && (is(before, "stop") != is(now, "stop") || is(before, "error") != is(now, "error"))) {
                        found[0] = before.getProcess() + "=" + before.getState() + " then "
                                + now.getProcess() + "=" + now.getState();
                    }
                }
                seen.add(now);
            });
            machine.start(state -> { });
            for (int cycle = 1; cycle <= CYCLES && found[0] == null; cycle++) {
                seen.clear();
                machine.cycle(state -> { });
                if (found[0] != null) {
                    return "run " + run + ", cycle " + cycle + ": " + found[0];
                }
            }
        }
        return null;
    }

    private static boolean is(CfgNode.InState node, String state) {
        return node.getState().equals(state);
    }

    // ------------------------------------------------------------------ Algorithms 5 and 6

    private static Map<String, Integer> groups(IrProgram program, Variant variant) {
        Map<IrNode, Attributes> attributes = new AttributePreparation(program).run();
        if (variant == Variant.PORT) {
            ProcessFacts facts = new ProcessFacts(program, attributes);
            Map<String, Integer> groups = new LinkedHashMap<>();
            program.getProcesses().forEach(p -> groups.put(p.getName(), facts.of(p.getName()).group()));
            return groups;
        }
        Map<String, Boolean> startS = startS(program, attributes, variant == Variant.PRINTED_LITERAL_STARTS);
        return new Grouping(program, attributes, variant).build(startS);
    }

    /**
     * ResolveStartStates. {@code literal} takes the condition on the starting process as
     * printed, {@code p'.startS = true}; otherwise as corrected, {@code p'.startS = false}.
     */
    private static Map<String, Boolean> startS(IrProgram program, Map<IrNode, Attributes> attributes,
                                               boolean literal) {
        List<IrProcess> processes = program.getProcesses();
        Map<String, Boolean> startS = new LinkedHashMap<>();
        for (int i = 0; i < processes.size(); i++) {
            startS.put(processes.get(i).getName(), i != 0);
        }
        for (int id = 0; id < processes.size(); id++) {
            String p = processes.get(id).getName();
            for (int starter = 0; starter < id; starter++) {
                IrProcess candidate = processes.get(starter);
                if (startS.get(candidate.getName()) != literal) {
                    continue;
                }
                if (first(candidate, attributes).changeFor(p) != Change.START) {
                    continue;
                }
                boolean blocked = false;
                for (int between = starter + 1; between < id; between++) {
                    Attributes first = first(processes.get(between), attributes);
                    blocked |= first.mayChange(p, Change.STOP) || first.mayChange(p, Change.ERROR);
                }
                if (!blocked) {
                    startS.put(p, false);
                    break;
                }
            }
        }
        return startS;
    }

    private static Attributes first(IrProcess process, Map<IrNode, Attributes> attributes) {
        return attributes.getOrDefault(process.getStartState(), Attributes.EMPTY);
    }

    /** BuildGroups, SetsDiv and SetsInter. */
    private static final class Grouping {
        private final IrProgram program;
        private final Map<IrNode, Attributes> attributes;
        private final Variant variant;
        private final Map<String, Integer> ids = new LinkedHashMap<>();
        private IrProcess pcur;
        private IrState scur;

        Grouping(IrProgram program, Map<IrNode, Attributes> attributes, Variant variant) {
            this.program = program;
            this.attributes = attributes;
            this.variant = variant;
            program.getProcesses().forEach(p -> ids.put(p.getName(), ids.size()));
        }

        Map<String, Integer> build(Map<String, Boolean> startS) {
            Set<String> s1 = new LinkedHashSet<>();
            Set<String> s2 = new LinkedHashSet<>();
            startS.forEach((p, value) -> (value ? s2 : s1).add(p));
            List<Set<String>> sets = new ArrayList<>(List.of(s1, s2));
            for (IrProcess p : program.getProcesses()) {
                pcur = p;
                sets = setsDiv(sets, Map.of(), p);
            }
            Map<String, Integer> groups = new LinkedHashMap<>();
            for (int i = 0; i < sets.size(); i++) {
                for (String p : sets.get(i)) {
                    groups.put(p, i);
                }
            }
            return groups;
        }

        private List<Set<String>> setsDiv(List<Set<String>> sets, Map<String, Change> hPC, IrNode st) {
            if (st instanceof IrState state) {
                scur = state;
            }
            // nhPC := st.procChange u hPC - where both define a process, the enclosing
            // construct's value is kept: it is what the construct as a whole does to it.
            Map<String, Change> nhPC = new LinkedHashMap<>(attributes.getOrDefault(st, Attributes.EMPTY).processChange());
            nhPC.putAll(hPC);
            int cur = ids.get(pcur.getName());
            boolean firstState = scur == pcur.getStartState();
            for (Change change : Change.values()) {
                Set<String> pred = new LinkedHashSet<>();
                Set<String> succ = new LinkedHashSet<>();
                nhPC.forEach((p, c) -> {
                    if (c != change) {
                        return;
                    }
                    int id = ids.get(p);
                    boolean selfIsPredecessor = variant == Variant.SELF_AS_PREDECESSOR
                            || variant == Variant.SELF_STOP_ERROR_AS_PREDECESSOR && change != Change.START;
                    boolean selfIsSuccessor = variant != Variant.SELF_AS_PREDECESSOR
                            && change == Change.START && firstState;
                    if (id < cur || id == cur && selfIsPredecessor) {
                        pred.add(p);
                    } else if (id > cur || id == cur && selfIsSuccessor) {
                        succ.add(p);
                    }
                });
                sets = setsInter(sets, pred);
                sets = setsInter(sets, succ);
            }
            for (IrNode line : lines(st)) {
                sets = setsDiv(sets, nhPC, line);
            }
            return sets;
        }

        private static List<Set<String>> setsInter(List<Set<String>> sets, Set<String> set) {
            List<Set<String>> buff = new ArrayList<>();
            for (Set<String> s : sets) {
                Set<String> inter = new LinkedHashSet<>(s);
                inter.retainAll(set);
                Set<String> rest = new LinkedHashSet<>(s);
                rest.removeAll(set);
                if (!inter.isEmpty()) {
                    buff.add(inter);
                }
                if (!rest.isEmpty()) {
                    buff.add(rest);
                }
            }
            return buff;
        }

        /** The constructs directly inside {@code st}. */
        private static List<IrNode> lines(IrNode st) {
            List<IrNode> lines = new ArrayList<>();
            if (st instanceof IrProcess process) {
                lines.addAll(process.getStates());
            } else if (st instanceof IrState state) {
                lines.addAll(state.getStatements());
                if (state.getTimeout() != null) {
                    lines.add(state.getTimeout());
                }
            } else if (st instanceof IrState.Timeout timeout) {
                lines.add(timeout.getBody());
            } else if (st instanceof IrStmt.Block block) {
                lines.addAll(block.getStatements());
            } else if (st instanceof IrStmt.If ifStmt) {
                lines.add(ifStmt.getThenBranch());
                if (ifStmt.getElseBranch() != null) {
                    lines.add(ifStmt.getElseBranch());
                }
            } else if (st instanceof IrStmt.Switch switchStmt) {
                lines.addAll(switchStmt.getCases());
            } else if (st instanceof IrStmt.SwitchCase clause) {
                lines.addAll(clause.getStatements());
            } else if (st instanceof IrStmt.For forStmt) {
                lines.add(forStmt.getBody());
            }
            lines.removeIf(java.util.Objects::isNull);
            return lines;
        }
    }

    private static List<Path> programs() throws IOException {
        List<Path> programs = new ArrayList<>();
        for (String directory : List.of("src/test/resources/programs-new", "src/test/resources/programs-extra")) {
            try (Stream<Path> files = Files.list(Path.of(directory))) {
                files.filter(f -> f.toString().endsWith(".rcs")).sorted().forEach(programs::add);
            }
        }
        return programs;
    }
}
