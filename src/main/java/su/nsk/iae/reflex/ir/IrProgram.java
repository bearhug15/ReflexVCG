package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * A whole Reflex program: the root of the IR.
 *
 * <p>This is the representation every preprocessing pass consumes and produces. After
 * name mangling, cast insertion and normalisation it is <em>canonical</em>: names are
 * globally unique, every implicit conversion is an explicit {@link IrExpr.Cast}, and no
 * {@code wait} or {@code slice} remains - they have become ordinary states.
 */
public final class IrProgram extends IrNode {

    private String name;
    private TimeRef clock;

    private final List<IrDecl.Constant> constants = new ArrayList<>();
    private final List<IrDecl.Enum> enums = new ArrayList<>();
    private final List<IrDecl.Struct> structs = new ArrayList<>();
    private final List<IrDecl.Function> functions = new ArrayList<>();
    private final List<IrDecl> globalVariables = new ArrayList<>();
    private final List<IrDecl.Node> nodes = new ArrayList<>();
    private final List<IrDecl.ImportBlock> imports = new ArrayList<>();
    private final List<IrProcess> processes = new ArrayList<>();

    public IrProgram(String name, TimeRef clock) {
        this.name = name;
        this.clock = clock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeRef getClock() {
        return clock;
    }

    public void setClock(TimeRef clock) {
        this.clock = clock;
    }

    public List<IrDecl.Constant> getConstants() {
        return constants;
    }

    public List<IrDecl.Enum> getEnums() {
        return enums;
    }

    public List<IrDecl.Struct> getStructs() {
        return structs;
    }

    public List<IrDecl.Function> getFunctions() {
        return functions;
    }

    /** Program-level variables: {@link IrDecl.Variable} or {@link IrDecl.PhysicalVariable}. */
    public List<IrDecl> getGlobalVariables() {
        return globalVariables;
    }

    public List<IrDecl.Node> getNodes() {
        return nodes;
    }

    public List<IrDecl.ImportBlock> getImports() {
        return imports;
    }

    public List<IrProcess> getProcesses() {
        return processes;
    }

    public IrProcess findProcess(String processName) {
        return processes.stream().filter(p -> p.getName().equals(processName)).findFirst().orElse(null);
    }

    public IrDecl.Struct findStruct(String structName) {
        return structs.stream().filter(s -> s.getName().equals(structName)).findFirst().orElse(null);
    }

    public IrDecl.Enum findEnum(String enumName) {
        return enums.stream().filter(e -> e.getName().equals(enumName)).findFirst().orElse(null);
    }

    public IrDecl.Node findNode(String nodeName) {
        return nodes.stream().filter(n -> n.getName().equals(nodeName)).findFirst().orElse(null);
    }

    /**
     * The program's inputs: physical variables bound for reading with no {@code write =}
     * destination, wherever they are declared. Nothing in the program decides their value,
     * so a condition has to let them be anything the environment might supply.
     *
     * <p>In declaration order - program level, then each node's, then each process's - so
     * generation stays deterministic.
     */
    public List<IrDecl.PhysicalVariable> inputVariables() {
        List<IrDecl.PhysicalVariable> inputs = new ArrayList<>();
        globalVariables.forEach(d -> collectInput(d, inputs));
        nodes.forEach(node -> node.getVariables().forEach(d -> collectInput(d, inputs)));
        processes.forEach(process -> process.getVariables().forEach(d -> collectInput(d, inputs)));
        return inputs;
    }

    private static void collectInput(IrDecl declaration, List<IrDecl.PhysicalVariable> inputs) {
        if (declaration instanceof IrDecl.PhysicalVariable physical
                && physical.getWritePort() == null) {
            inputs.add(physical);
        }
    }

    @Override
    public String toString() {
        return "program " + name + " (" + processes.size() + " processes)";
    }
}
