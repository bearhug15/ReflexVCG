package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * A Reflex process: a sequence of states, one of which is current at any time.
 *
 * <p>The first declared state is the one a process starts in, so state order is
 * significant and {@link #getStates()} preserves it. Normalisation appends the light
 * states it derives from {@code wait} and {@code slice} rather than inserting them, so
 * the original states keep their positions.
 */
public final class IrProcess extends IrNode {

    private String name;
    private String nodeName;

    private final List<IrDecl> variables = new ArrayList<>();
    private final List<IrDecl.SharedImport> sharedImports = new ArrayList<>();
    private final List<IrState> states = new ArrayList<>();

    public IrProcess(String name, String nodeName) {
        this.name = name;
        this.nodeName = nodeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** The node this process is bound to; null if the program declares no nodes. */
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /** Process-local variables: {@link IrDecl.Variable} or {@link IrDecl.PhysicalVariable}. */
    public List<IrDecl> getVariables() {
        return variables;
    }

    public List<IrDecl.SharedImport> getSharedImports() {
        return sharedImports;
    }

    /** States in declaration order; the first is the start state. */
    public List<IrState> getStates() {
        return states;
    }

    public IrState getStartState() {
        return states.isEmpty() ? null : states.get(0);
    }

    public IrState findState(String stateName) {
        return states.stream().filter(s -> s.getName().equals(stateName)).findFirst().orElse(null);
    }

    /**
     * The state following {@code stateName} in declaration order - the target of
     * {@code set next state} - or null if it is the last one.
     */
    public IrState nextState(String stateName) {
        for (int i = 0; i < states.size() - 1; i++) {
            if (states.get(i).getName().equals(stateName)) {
                return states.get(i + 1);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "process " + name + " (" + states.size() + " states)";
    }
}
