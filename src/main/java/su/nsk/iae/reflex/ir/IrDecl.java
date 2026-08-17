package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Declarations of the Reflex IR.
 *
 * <p>Names are mutable: the name-mangling pass of Preprocessing.tex rewrites every
 * declaration to a globally unique name, which is what makes the old VariableMapper's
 * (process, variable) lookup table unnecessary.
 */
public abstract class IrDecl extends IrNode {

    private String name;

    protected IrDecl(String name) {
        this.name = name;
    }

    /** The declared name; globally unique after name mangling. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A program, node, process or local variable - {@code simpleVariableDeclaration},
     * {@code arrayVariableDeclaration} and {@code structureVariableDeclaration} of
     * Preprocessing.tex, distinguished here by {@link #getType()} rather than by class.
     */
    public static final class Variable extends IrDecl {
        private IrType type;
        private IrExpr initializer;
        private boolean shared;

        public Variable(String name, IrType type, IrExpr initializer) {
            super(name);
            this.type = type;
            this.initializer = initializer;
        }

        public IrType getType() {
            return type;
        }

        public void setType(IrType type) {
            this.type = type;
        }

        /** Null when undeclared; an {@link IrExpr.Aggregate} for array/struct initialisers. */
        public IrExpr getInitializer() {
            return initializer;
        }

        public void setInitializer(IrExpr initializer) {
            this.initializer = initializer;
        }

        /** True for a process variable marked {@code shared}. */
        public boolean isShared() {
            return shared;
        }

        public void setShared(boolean shared) {
            this.shared = shared;
        }

        @Override
        public String toString() {
            return type + " " + getName() + (initializer == null ? "" : " = " + initializer);
        }
    }

    /** A variable bound to a physical register or bit. */
    public static final class PhysicalVariable extends IrDecl {
        private IrType type;
        private final boolean direct;
        private String readPort;
        private String writePort;
        private String configPort;
        private String bit;

        public PhysicalVariable(String name, IrType type, boolean direct,
                                String readPort, String writePort, String configPort, String bit) {
            super(name);
            this.type = type;
            this.direct = direct;
            this.readPort = readPort;
            this.writePort = writePort;
            this.configPort = configPort;
            this.bit = bit;
        }

        public IrType getType() {
            return type;
        }

        public void setType(IrType type) {
            this.type = type;
        }

        /**
         * True for {@code direct}. Direct bindings read the hardware on every access, so
         * mangling gives each access its own name (newDirectName(name, num)); indirect
         * ones are sampled once (newIndirectName(name, pos)).
         */
        public boolean isDirect() {
            return direct;
        }

        public String getReadPort() {
            return readPort;
        }

        public void setReadPort(String readPort) {
            this.readPort = readPort;
        }

        public String getWritePort() {
            return writePort;
        }

        public void setWritePort(String writePort) {
            this.writePort = writePort;
        }

        public String getConfigPort() {
            return configPort;
        }

        public void setConfigPort(String configPort) {
            this.configPort = configPort;
        }

        /** Bit name or numeric position within the register; null when unbound. */
        public String getBit() {
            return bit;
        }

        public void setBit(String bit) {
            this.bit = bit;
        }

        @Override
        public String toString() {
            return (direct ? "direct " : "") + type + " " + getName() + " as (read = " + readPort + ")";
        }
    }

    public static final class Constant extends IrDecl {
        private IrType type;
        private IrExpr value;

        public Constant(String name, IrType type, IrExpr value) {
            super(name);
            this.type = type;
            this.value = value;
        }

        public IrType getType() {
            return type;
        }

        public void setType(IrType type) {
            this.type = type;
        }

        public IrExpr getValue() {
            return value;
        }

        public void setValue(IrExpr value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "const " + type + " " + getName() + " = " + value;
        }
    }

    public static final class Struct extends IrDecl {
        private final List<Variable> fields;

        public Struct(String name, List<Variable> fields) {
            super(name);
            this.fields = fields;
        }

        public List<Variable> getFields() {
            return fields;
        }

        public Variable findField(String fieldName) {
            return fields.stream().filter(f -> f.getName().equals(fieldName)).findFirst().orElse(null);
        }

        @Override
        public String toString() {
            return "struct " + getName() + " { " + fields.size() + " fields }";
        }
    }

    public static final class EnumMember extends IrDecl {
        private IrExpr value;

        public EnumMember(String name, IrExpr value) {
            super(name);
            this.value = value;
        }

        /** Explicit value, or null when it takes the implicit successor value. */
        public IrExpr getValue() {
            return value;
        }

        public void setValue(IrExpr value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return getName() + (value == null ? "" : " = " + value);
        }
    }

    public static final class Enum extends IrDecl {
        private final List<EnumMember> members;

        public Enum(String name, List<EnumMember> members) {
            super(name);
            this.members = members;
        }

        public List<EnumMember> getMembers() {
            return members;
        }

        @Override
        public String toString() {
            return "enum " + getName() + " { " + members.size() + " members }";
        }
    }

    /** An {@code input} or {@code output} port. */
    public static final class Port extends IrDecl {
        public enum Direction { INPUT, OUTPUT }

        private final Direction direction;
        private final String address1;
        private final String address2;
        private final String size;

        public Port(String name, Direction direction, String address1, String address2, String size) {
            super(name);
            this.direction = direction;
            this.address1 = address1;
            this.address2 = address2;
            this.size = size;
        }

        public Direction getDirection() {
            return direction;
        }

        public String getAddress1() {
            return address1;
        }

        public String getAddress2() {
            return address2;
        }

        public String getSize() {
            return size;
        }

        @Override
        public String toString() {
            return direction.name().toLowerCase() + " " + getName();
        }
    }

    /** A declared function signature. Reflex declares types only, not parameter names. */
    public static final class Function extends IrDecl {
        private final IrType returnType;
        private final List<IrType> parameterTypes;

        public Function(String name, IrType returnType, List<IrType> parameterTypes) {
            super(name);
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        public IrType getReturnType() {
            return returnType;
        }

        public List<IrType> getParameterTypes() {
            return parameterTypes;
        }

        @Override
        public String toString() {
            return returnType + " " + getName() + parameterTypes;
        }
    }

    /**
     * {@code shared x, y from process P}: names another process's variables into scope.
     */
    public static final class SharedImport extends IrDecl {
        private final List<String> variables;
        private String sourceProcess;

        public SharedImport(List<String> variables, String sourceProcess) {
            super(sourceProcess);
            this.variables = variables;
            this.sourceProcess = sourceProcess;
        }

        public List<String> getVariables() {
            return variables;
        }

        public String getSourceProcess() {
            return sourceProcess;
        }

        public void setSourceProcess(String sourceProcess) {
            this.sourceProcess = sourceProcess;
        }

        @Override
        public String toString() {
            return "shared " + variables + " from process " + sourceProcess;
        }
    }

    /**
     * An {@code import} block. Ignored by VC generation, kept so the IR round-trips the
     * whole program.
     */
    public static final class ImportBlock extends IrDecl {
        public enum ElementKind { VECTOR, REGISTER, BIT }

        public record Element(ElementKind kind, String name) {
        }

        private final List<Element> elements;

        public ImportBlock(String name, List<Element> elements) {
            super(name);
            this.elements = elements;
        }

        public List<Element> getElements() {
            return elements;
        }

        @Override
        public String toString() {
            return "import " + getName() + " { " + elements.size() + " elements }";
        }
    }

    /**
     * A {@code node}. Treated purely as a namespace: it contributes its constants and
     * variables to the processes bound to it and otherwise does not affect generation.
     */
    public static final class Node extends IrDecl {
        private TimeRef clock;
        private final List<Constant> constants = new ArrayList<>();
        private final List<IrDecl> variables = new ArrayList<>();

        public Node(String name, TimeRef clock) {
            super(name);
            this.clock = clock;
        }

        public TimeRef getClock() {
            return clock;
        }

        public void setClock(TimeRef clock) {
            this.clock = clock;
        }

        public List<Constant> getConstants() {
            return constants;
        }

        /** Variable or PhysicalVariable declarations belonging to this node. */
        public List<IrDecl> getVariables() {
            return variables;
        }

        @Override
        public String toString() {
            return "node " + getName();
        }
    }
}
