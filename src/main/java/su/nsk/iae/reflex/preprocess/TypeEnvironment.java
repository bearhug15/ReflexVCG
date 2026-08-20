package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Types of everything the program declares: the {@code variableTypes} and
 * {@code structFields} environment of the cast-insertion section of Preprocessing.tex.
 *
 * <p>A single flat map suffices because this runs after {@link NameManglingPass}, where
 * every declaration acquired a globally unique name.
 *
 * <p>The spec assumes types are already known; the parser cannot know them, because a
 * declared type may be written as a bare identifier naming a struct or an enum. This
 * class therefore also resolves {@link IrType.Named} to {@link IrType.Struct} or
 * {@link IrType.Enum}, rewriting the declarations in place.
 */
public final class TypeEnvironment {

    private final Map<String, IrType> variableTypes = new LinkedHashMap<>();
    private final Map<String, Map<String, IrType>> structFields = new LinkedHashMap<>();
    private final Map<String, IrType> enumMemberTypes = new LinkedHashMap<>();

    private final IrProgram program;

    public TypeEnvironment(IrProgram program) {
        this(program, Map.of());
    }

    /**
     * @param directAccessNames per-access names generated for direct bindings, mapped to
     *                          the binding they came from - see
     *                          {@link NameManglingPass#getDirectAccessNames()}. Those
     *                          names appear in expressions but have no declaration.
     */
    public TypeEnvironment(IrProgram program, Map<String, String> directAccessNames) {
        this.program = program;
        collectStructs();
        collectEnums();
        collectVariables();
        directAccessNames.forEach((accessName, baseName) -> declareDirectAccess(accessName, baseName));
    }

    // ------------------------------------------------------------------ queries

    /** Declared type of a variable, or null if the name is not declared. */
    public IrType variableType(String name) {
        return variableTypes.get(name);
    }

    public boolean isDeclared(String name) {
        return variableTypes.containsKey(name);
    }

    /** Field types of a struct, or null when no such struct is declared. */
    public Map<String, IrType> fieldsOf(String structName) {
        return structFields.get(structName);
    }

    /**
     * Type reached by walking {@code accesses} from a variable, or null if the path does
     * not fit the type - indexing a struct, naming a field of an array, or a field the
     * struct does not declare. This is {@code getVariableType} of the spec.
     */
    public IrType accessType(String name, List<IrExpr.Access> accesses) {
        IrType base = variableTypes.get(name);
        return base == null ? null : accessType(base, accesses, 0);
    }

    private IrType accessType(IrType type, List<IrExpr.Access> accesses, int from) {
        if (from >= accesses.size()) {
            // An enum value is an int32 once every access has been consumed.
            return type instanceof IrType.Enum ? IrType.INT32 : type;
        }
        IrExpr.Access access = accesses.get(from);
        if (type instanceof IrType.Array array) {
            return access instanceof IrExpr.IndexAccess
                    ? accessType(array.element(), accesses, from + 1)
                    : null;
        }
        if (type instanceof IrType.Struct struct) {
            if (!(access instanceof IrExpr.FieldAccess field)) {
                return null;
            }
            Map<String, IrType> fields = structFields.get(struct.name());
            if (fields == null) {
                return null;
            }
            IrType fieldType = fields.get(field.getField());
            return fieldType == null ? null : accessType(fieldType, accesses, from + 1);
        }
        // A builtin or enum has no interior to walk into.
        return null;
    }

    /**
     * The type of a struct's field, or null when the owner is not a struct declaring it.
     * Exposed so annotations can follow an access path without duplicating the lookup.
     */
    public IrType fieldType(IrType owner, String field) {
        if (!(owner instanceof IrType.Struct struct)) {
            return null;
        }
        Map<String, IrType> fields = structFields.get(struct.name());
        return fields == null ? null : fields.get(field);
    }

    /** The element type of an array, or null when the owner is not one. */
    public IrType elementType(IrType owner) {
        return owner instanceof IrType.Array array ? array.element() : null;
    }

    /** Replaces a Named type with the struct or enum it refers to. */
    public IrType resolve(IrType type) {
        if (type instanceof IrType.Named named) {
            if (program.findStruct(named.name()) != null) {
                return new IrType.Struct(named.name());
            }
            if (program.findEnum(named.name()) != null) {
                return new IrType.Enum(named.name());
            }
            throw new IllegalStateException("unknown type: " + named.name());
        }
        if (type instanceof IrType.Array array) {
            IrType element = resolve(array.element());
            return element == array.element() ? array : new IrType.Array(element, array.size());
        }
        return type;
    }

    // ------------------------------------------------------------------ collection

    private void collectStructs() {
        // Field types are resolved after every struct name is known, so structs may
        // refer to one another regardless of declaration order.
        for (IrDecl.Struct struct : program.getStructs()) {
            structFields.put(struct.getName(), new LinkedHashMap<>());
        }
        for (IrDecl.Struct struct : program.getStructs()) {
            Map<String, IrType> fields = structFields.get(struct.getName());
            for (IrDecl.Variable field : struct.getFields()) {
                field.setType(resolve(field.getType()));
                fields.put(field.getName(), field.getType());
            }
        }
    }

    private void collectEnums() {
        for (IrDecl.Enum enumDecl : program.getEnums()) {
            for (IrDecl.EnumMember member : enumDecl.getMembers()) {
                // Enum members denote integers wherever they are used.
                enumMemberTypes.put(member.getName(), IrType.INT32);
                variableTypes.put(member.getName(), IrType.INT32);
            }
        }
    }

    private void collectVariables() {
        for (IrDecl.Constant constant : program.getConstants()) {
            declare(constant);
        }
        for (IrDecl declaration : program.getGlobalVariables()) {
            declare(declaration);
        }
        for (IrDecl.Node node : program.getNodes()) {
            node.getConstants().forEach(this::declare);
            node.getVariables().forEach(this::declare);
        }
        for (IrProcess process : program.getProcesses()) {
            process.getVariables().forEach(this::declare);
            for (IrState state : process.getStates()) {
                state.getStatements().forEach(this::declareLocals);
                if (state.getTimeout() != null) {
                    declareLocals(state.getTimeout().getBody());
                }
            }
        }
    }

    private void declare(IrDecl declaration) {
        if (declaration instanceof IrDecl.Variable variable) {
            variable.setType(resolve(variable.getType()));
            variableTypes.put(variable.getName(), variable.getType());
        } else if (declaration instanceof IrDecl.PhysicalVariable physical) {
            physical.setType(resolve(physical.getType()));
            variableTypes.put(physical.getName(), physical.getType());
        } else if (declaration instanceof IrDecl.Constant constant) {
            constant.setType(resolve(constant.getType()));
            variableTypes.put(constant.getName(), constant.getType());
        }
    }

    /** Local declarations are nested inside statements, so the bodies are walked too. */
    private void declareLocals(IrStmt statement) {
        if (statement == null) {
            return;
        }
        if (statement instanceof IrStmt.LocalVar local) {
            declare(local.getDeclaration());
        } else if (statement instanceof IrStmt.Block block) {
            block.getStatements().forEach(this::declareLocals);
        } else if (statement instanceof IrStmt.If ifStmt) {
            declareLocals(ifStmt.getThenBranch());
            declareLocals(ifStmt.getElseBranch());
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            switchStmt.getCases().forEach(c -> c.getStatements().forEach(this::declareLocals));
        } else if (statement instanceof IrStmt.Wait wait) {
            declareLocals(wait.getTimeoutBody());
        } else if (statement instanceof IrStmt.For forStmt) {
            forStmt.getInitDeclarations().forEach(this::declare);
            declareLocals(forStmt.getBody());
        }
    }

    /**
     * Registers a name introduced after construction, used when normalisation moves
     * declarations into states it synthesises.
     */
    public void declareVariable(String name, IrType type) {
        variableTypes.put(name, type);
    }

    /**
     * Direct bindings are read under per-access names like {@code inp_1.0}; they share
     * the type of the binding they came from.
     */
    public void declareDirectAccess(String accessName, String baseName) {
        IrType type = variableTypes.get(baseName);
        if (type != null) {
            variableTypes.put(accessName, type);
        }
    }
}
