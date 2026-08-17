package su.nsk.iae.reflex.analysis;

import su.nsk.iae.reflex.analysis.Attributes.Change;
import su.nsk.iae.reflex.analysis.Attributes.ProcessChange;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The per-process facts of StaticalAnalysis.tex: whether a process can reach stop or
 * error, whether it begins stopped, and which processes are started and stopped together.
 *
 * <p>Two readings were needed, both marked SPEC below:
 * <ul>
 *   <li>{@code setReachE} assigns {@code reachS} rather than {@code reachE}. Taken as a
 *       slip: left as written, reachE is never true and rule 1 would discard every path
 *       through an error state, dropping real obligations.</li>
 *   <li>{@code setStartS} tests {@code proc.active}, which is not defined anywhere. Taken
 *       as the previous implementation had it: the first declared process starts running,
 *       every other starts stopped.</li>
 * </ul>
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
        Map<String, Boolean> reachS = new LinkedHashMap<>();
        Map<String, Boolean> reachE = new LinkedHashMap<>();
        Map<String, Boolean> startS = new LinkedHashMap<>();

        // Any process that some process might stop can reach stop, as can one that moves
        // itself there.
        Set<String> mayBeStopped = processesMentionedWith(Change.STOP);
        Set<String> mayBeErrored = processesMentionedWith(Change.ERROR);

        for (IrProcess process : program.getProcesses()) {
            Attributes processAttributes = attributes.getOrDefault(process, Attributes.EMPTY);
            reachS.put(process.getName(),
                    processAttributes.changesTo().contains("stop") || mayBeStopped.contains(process.getName()));
            // SPEC: setReachE writes reachS; read as reachE.
            reachE.put(process.getName(),
                    processAttributes.changesTo().contains("error") || mayBeErrored.contains(process.getName()));
        }

        computeStartS(startS);

        Map<String, Integer> groups = computeGroups(startS);
        for (IrProcess process : program.getProcesses()) {
            String name = process.getName();
            facts.put(name, new Facts(
                    reachS.getOrDefault(name, false),
                    startS.getOrDefault(name, false),
                    reachE.getOrDefault(name, false),
                    groups.getOrDefault(name, processId(name))));
        }
    }

    private Set<String> processesMentionedWith(Change change) {
        Set<String> mentioned = new LinkedHashSet<>();
        for (IrProcess process : program.getProcesses()) {
            Attributes processAttributes = attributes.getOrDefault(process, Attributes.EMPTY);
            for (ProcessChange potential : processAttributes.potProcessChange()) {
                if (potential.change() == change) {
                    mentioned.add(potential.process());
                }
            }
        }
        return mentioned;
    }

    /**
     * Whether a process is stopped when the program begins. SPEC: the first declared
     * process runs from the start; the rest begin stopped. A process then loses startS if
     * an earlier process starts it from its own first state and nothing in between could
     * stop it again.
     */
    private void computeStartS(Map<String, Boolean> startS) {
        List<IrProcess> processes = program.getProcesses();
        for (int i = 0; i < processes.size(); i++) {
            startS.put(processes.get(i).getName(), i != 0);
        }

        for (IrProcess current : processes) {
            String name = current.getName();
            List<IrProcess> earlier = processes.subList(0, processes.indexOf(current));

            for (int i = 0; i < earlier.size(); i++) {
                IrProcess previous = earlier.get(i);
                if (!startS.getOrDefault(previous.getName(), false)) {
                    continue;
                }
                IrState firstState = previous.getStartState();
                if (firstState == null) {
                    continue;
                }
                Attributes stateAttributes = attributes.getOrDefault(firstState, Attributes.EMPTY);
                if (stateAttributes.changeFor(name) != Change.START) {
                    continue;
                }
                // Nothing declared between them may stop it again.
                boolean blocked = false;
                for (IrProcess between : earlier.subList(i + 1, earlier.size())) {
                    Attributes betweenAttributes = attributes.getOrDefault(between, Attributes.EMPTY);
                    if (betweenAttributes.mayChange(name, Change.STOP)) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    startS.put(name, false);
                    break;
                }
            }
        }
    }

    /**
     * Partitions processes into groups that are started, stopped and failed together.
     * Refines an initial split - those that begin stopped and those that do not - by the
     * changes each process makes, distinguishing processes declared before the acting one
     * from those declared after, since a change to an earlier process takes effect in the
     * same cycle and a change to a later one does not.
     */
    private Map<String, Integer> computeGroups(Map<String, Boolean> startS) {
        Set<String> begins = new LinkedHashSet<>();
        Set<String> doesNotBegin = new LinkedHashSet<>();
        for (IrProcess process : program.getProcesses()) {
            (startS.getOrDefault(process.getName(), false) ? begins : doesNotBegin)
                    .add(process.getName());
        }

        List<Set<String>> partition = new ArrayList<>();
        partition.add(doesNotBegin);
        partition.add(begins);

        for (IrProcess process : program.getProcesses()) {
            Attributes processAttributes = attributes.getOrDefault(process, Attributes.EMPTY);
            partition = refine(partition, processAttributes, process.getName(),
                    process.getStartState() == null ? null : process.getStartState().getName());
        }

        Map<String, Integer> groups = new LinkedHashMap<>();
        int index = 0;
        for (Set<String> part : partition) {
            for (String member : part) {
                groups.put(member, index);
            }
            index++;
        }
        return groups;
    }

    /** One refinement step: splits every part by each of the six change classes. */
    private List<Set<String>> refine(List<Set<String>> partition, Attributes attributes,
                                     String actingProcess, String actingFirstState) {
        int actingId = processId(actingProcess);

        List<Set<String>> classes = new ArrayList<>();
        for (Change change : Change.values()) {
            Set<String> earlier = new LinkedHashSet<>();
            Set<String> later = new LinkedHashSet<>();
            for (Map.Entry<String, Change> entry : attributes.processChange().entrySet()) {
                if (entry.getValue() != change) {
                    continue;
                }
                int id = processId(entry.getKey());
                if (id < actingId) {
                    earlier.add(entry.getKey());
                } else if (id > actingId
                        || change == Change.START && actingFirstState != null) {
                    later.add(entry.getKey());
                }
            }
            classes.add(earlier);
            classes.add(later);
        }

        List<Set<String>> refined = partition;
        for (Set<String> splitter : classes) {
            refined = split(refined, splitter);
        }
        return refined;
    }

    /**
     * SPEC: setsInter unions a set of processes into a collection of sets. Read as
     * splitting every part into the members inside {@code splitter} and those outside,
     * which is what the previous implementation did.
     */
    private static List<Set<String>> split(List<Set<String>> partition, Set<String> splitter) {
        if (splitter.isEmpty()) {
            return partition;
        }
        List<Set<String>> result = new ArrayList<>();
        for (Set<String> part : partition) {
            Set<String> inside = new LinkedHashSet<>(part);
            inside.retainAll(splitter);
            Set<String> outside = new LinkedHashSet<>(part);
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
}
