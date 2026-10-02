package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.analysis.Attributes.ProcessChange;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The derived per-process attributes of static-analysis.md, section 3: whether a process can
 * reach stop or error (Algorithm 4), whether it can be found stopped in the first cycle
 * (Algorithm 5) and which processes are started, stopped and failed together (Algorithm 6).
 */
public final class ProcessFacts {

    /** Facts about one process. */
    public record Facts(boolean reachS, boolean startS, boolean reachE, int group) {
    }

    private final Map<String, Facts> facts = new LinkedHashMap<>();
    private final Map<String, Integer> processIds = new LinkedHashMap<>();
    private final IrProgram program;
    private final Map<IrNode, Attributes> attributes;

    public ProcessFacts(IrProgram program, Map<IrNode, Attributes> attributes) {
        this.program = program;
        this.attributes = attributes;
        List<IrProcess> processes = program.getProcesses();
        for (int i = 0; i < processes.size(); i++) {
            processIds.put(processes.get(i).getName(), i);
        }
        compute();
    }

    public Facts of(String process) {
        return facts.getOrDefault(process, new Facts(false, false, false, -1));
    }

    public int processId(String process) {
        return processIds.getOrDefault(process, -1);
    }

    // ------------------------------------------------------------------ computation

    private void compute() {
        Set<String> mayBeStopped = processesMentionedWith(Change.STOP);
        Set<String> mayBeErrored = processesMentionedWith(Change.ERROR);

        Map<String, Boolean> reachS = new LinkedHashMap<>();
        Map<String, Boolean> reachE = new LinkedHashMap<>();
        for (IrProcess process : program.getProcesses()) {
            // Algorithm 4: some state of the process moves it there, or some process may
            // put it there. A process stopping itself is both.
            Attributes processAttributes = of(process);
            reachS.put(process.getName(),
                    processAttributes.changesTo().contains("stop") || mayBeStopped.contains(process.getName()));
            reachE.put(process.getName(),
                    processAttributes.changesTo().contains("error") || mayBeErrored.contains(process.getName()));
        }

        Map<String, Boolean> startS = computeStartS();
        Map<String, Integer> groups = new Grouping(startS).build();
        for (IrProcess process : program.getProcesses()) {
            String name = process.getName();
            facts.put(name, new Facts(reachS.get(name), startS.get(name), reachE.get(name), groups.get(name)));
        }
    }

    private Set<String> processesMentionedWith(Change change) {
        Set<String> mentioned = new LinkedHashSet<>();
        for (IrProcess process : program.getProcesses()) {
            for (ProcessChange potential : of(process).potProcessChange()) {
                if (potential.change() == change) {
                    mentioned.add(potential.process());
                }
            }
        }
        return mentioned;
    }

    /**
     * Algorithm 5, {@code ResolveStartStates}: whether a process may be found stopped at its
     * turn in the first cycle. The first process runs from the start; every other begins
     * stopped. A process {@code p} is nonetheless never found stopped in the first cycle if a
     * process {@code p'} declared before it, itself never found stopped then, starts it in its
     * first state, and no process declared between them may stop or fail it in <em>its</em>
     * first state. Every process running in the first cycle is in its first state, so those
     * are the only states that matter.
     *
     * <p>The paper prints {@code p'.startS = true}; its authors confirm {@code false} is meant.
     */
    private Map<String, Boolean> computeStartS() {
        List<IrProcess> processes = program.getProcesses();
        Map<String, Boolean> startS = new LinkedHashMap<>();
        for (int i = 0; i < processes.size(); i++) {
            startS.put(processes.get(i).getName(), i != 0);
        }
        for (int id = 1; id < processes.size(); id++) {
            String name = processes.get(id).getName();
            for (int starter = 0; starter < id; starter++) {
                IrProcess candidate = processes.get(starter);
                if (startS.get(candidate.getName()) || firstOf(candidate).changeFor(name) != Change.START) {
                    continue;
                }
                boolean blocked = false;
                for (int between = starter + 1; between < id && !blocked; between++) {
                    Attributes first = firstOf(processes.get(between));
                    blocked = first.mayChange(name, Change.STOP) || first.mayChange(name, Change.ERROR);
                }
                if (!blocked) {
                    startS.put(name, false);
                    break;
                }
            }
        }
        return startS;
    }

    private Attributes firstOf(IrProcess process) {
        return process.getStartState() == null ? Attributes.EMPTY : of(process.getStartState());
    }

    private Attributes of(IrNode node) {
        return attributes.getOrDefault(node, Attributes.EMPTY);
    }

    /**
     * Algorithm 6, {@code BuildGroups}, with the repair of static-analysis.md, section 3.4.
     *
     * <p>Starts from the split by {@code startS} and walks every construct of every process -
     * the process, its states, every statement, branches and loop bodies included. At each
     * construct it takes the changes in force there, {@code nhPC} (the construct's own
     * definite changes and those of the constructs enclosing it), and splits every group by
     * six classes: processes started, stopped and failed, each on one side of the acting
     * process. The sides say when a process shows the change: one declared after the actor
     * in this cycle, one declared before it - <em>or the actor itself</em>, which has already
     * had its turn - in the next. As printed, the paper files a change of the actor to
     * itself only for a restart in its first state, so a process stopping or failing itself
     * never splits a group; that is the repair.
     */
    private final class Grouping {
        private final Map<String, Boolean> startS;
        private int actor;

        Grouping(Map<String, Boolean> startS) {
            this.startS = startS;
        }

        Map<String, Integer> build() {
            Set<String> neverStopped = new LinkedHashSet<>();
            Set<String> mayBeStopped = new LinkedHashSet<>();
            startS.forEach((process, value) -> (value ? mayBeStopped : neverStopped).add(process));
            List<Set<String>> sets = new ArrayList<>(List.of(neverStopped, mayBeStopped));
            sets.removeIf(Set::isEmpty);

            for (IrProcess process : program.getProcesses()) {
                actor = processId(process.getName());
                sets = divide(sets, Map.of(), process);
            }

            Map<String, Integer> groups = new LinkedHashMap<>();
            for (int i = 0; i < sets.size(); i++) {
                for (String member : sets.get(i)) {
                    groups.put(member, i);
                }
            }
            return groups;
        }

        /** {@code SetsDiv}. */
        private List<Set<String>> divide(List<Set<String>> sets, Map<String, Change> enclosing, IrNode construct) {
            // nhPC := st.procChange u hPC. Where both define a process the enclosing value is
            // kept; the grouping is sound either way (static-analysis.md, section 3.4).
            Map<String, Change> inForce = new LinkedHashMap<>(of(construct).processChange());
            inForce.putAll(enclosing);

            for (Change change : Change.values()) {
                Set<String> declaredBefore = new LinkedHashSet<>();
                Set<String> declaredAfter = new LinkedHashSet<>();
                inForce.forEach((process, value) -> {
                    if (value == change) {
                        (processId(process) <= actor ? declaredBefore : declaredAfter).add(process);
                    }
                });
                sets = intersect(sets, declaredBefore);
                sets = intersect(sets, declaredAfter);
            }
            for (IrNode line : lines(construct)) {
                sets = divide(sets, inForce, line);
            }
            return sets;
        }
    }

    /** {@code SetsInter}: every set split into its part inside {@code splitter} and the rest. */
    private static List<Set<String>> intersect(List<Set<String>> sets, Set<String> splitter) {
        if (splitter.isEmpty()) {
            return sets;
        }
        List<Set<String>> result = new ArrayList<>();
        for (Set<String> set : sets) {
            Set<String> inside = new LinkedHashSet<>(set);
            inside.retainAll(splitter);
            Set<String> outside = new LinkedHashSet<>(set);
            outside.removeAll(splitter);
            if (!inside.isEmpty()) {
                result.add(inside);
            }
            if (!outside.isEmpty()) {
                result.add(outside);
            }
        }
        return result;
    }

    /** {@code lines}: the constructs directly inside one. */
    static List<IrNode> lines(IrNode construct) {
        List<IrNode> lines = new ArrayList<>();
        if (construct instanceof IrProcess process) {
            lines.addAll(process.getStates());
        } else if (construct instanceof IrState state) {
            lines.addAll(state.getStatements());
            lines.add(state.getTimeout());
        } else if (construct instanceof IrState.Timeout timeout) {
            lines.add(timeout.getBody());
        } else if (construct instanceof IrStmt.Block block) {
            lines.addAll(block.getStatements());
        } else if (construct instanceof IrStmt.If ifStmt) {
            lines.add(ifStmt.getThenBranch());
            lines.add(ifStmt.getElseBranch());
        } else if (construct instanceof IrStmt.Switch switchStmt) {
            lines.addAll(switchStmt.getCases());
        } else if (construct instanceof IrStmt.SwitchCase clause) {
            lines.addAll(clause.getStatements());
        } else if (construct instanceof IrStmt.For forStmt) {
            lines.add(forStmt.getBody());
        }
        lines.removeIf(Objects::isNull);
        return lines;
    }
}
