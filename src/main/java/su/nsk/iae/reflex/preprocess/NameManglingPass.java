package su.nsk.iae.reflex.preprocess;

import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.TimeRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites every declaration and every use to a globally unique name.
 *
 * <p>Implements the "Name mangling" section of Preprocessing.tex. Reflex declares
 * variables at program, node, process and block level with C-like shadowing, and two
 * processes may use the same name for different variables. Once every name is unique the
 * (process, variable) lookup table the old VariableMapper maintained is unnecessary: a
 * name means exactly one thing everywhere.
 *
 * <p>Naming:
 * <ul>
 *   <li>{@code newName(path, name)} joins the enclosing scopes with {@code #}, outermost
 *       first, and separates the variable with a further {@code #}, giving
 *       {@code node#process#state#variable}. A name declared at program level has no
 *       enclosing scope and so begins with the separator, {@code #variable}; every
 *       mangled name therefore contains one, which keeps it distinct from the unmangled
 *       names physical variables take from their ports.</li>
 *   <li>Physical variables take their name from the port they are bound to,
 *       {@code newIndirectName(port, bit)}.</li>
 *   <li>Each <em>use</em> of a {@code direct} variable gets its own name,
 *       {@code newDirectName(base, n)} with n counting accesses, because a direct
 *       binding re-reads the hardware on every access and so may observe a new value.</li>
 * </ul>
 */
public final class NameManglingPass {

    /** Global context: node name -> (original variable name -> mangled name). */
    private final Map<String, Map<String, String>> nodeVariables = new LinkedHashMap<>();

    /** Process name -> (original variable name -> mangled name), for shared imports. */
    private final Map<String, Map<String, String>> processVariables = new LinkedHashMap<>();

    /** Global context: mangled name -> whether the binding is direct. */
    private final Map<String, Boolean> isDirect = new LinkedHashMap<>();

    /** Local context: enclosing scope names, outermost first. */
    private final List<String> path = new ArrayList<>();

    /** Local context: original name -> mangled name, in the current scope. */
    private Map<String, String> variableMap = new LinkedHashMap<>();

    /** Local context: mangled name -> number of accesses made to a direct binding. */
    private final Map<String, Integer> directCount = new HashMap<>();

    /** Per-access name of a direct binding -> the binding it came from. */
    private final Map<String, String> directAccessNames = new LinkedHashMap<>();

    private int blockCounter;

    public void run(IrProgram program) {
        mapProgram(program);
    }

    /** Mangled name -> whether that binding is direct; for later stages. */
    public Map<String, Boolean> getDirectBindings() {
        return isDirect;
    }

    /**
     * Per-access names generated for direct bindings, mapped to the binding they came
     * from. Those names have no declaration of their own, so later stages need this to
     * give them a type.
     */
    public Map<String, String> getDirectAccessNames() {
        return directAccessNames;
    }

    // ------------------------------------------------------------------ naming

    static String newName(List<String> path, String name) {
        return String.join(SEPARATOR, path) + SEPARATOR + name;
    }

    /** Separates the enclosing scopes from each other and from the variable name. */
    static final String SEPARATOR = "#";

    static String newIndirectName(String port, String position) {
        return position == null ? port : port + "_" + position;
    }

    static String newDirectName(String name, int accessNumber) {
        return name + "." + accessNumber;
    }

    private void pushScope(String scope) {
        path.add(scope);
    }

    /**
     * A process is scoped by the node it is bound to, so its variables are named
     * {@code node#process#variable}. Programs that declare no nodes leave it out.
     */
    private void pushProcessScope(IrProcess process) {
        if (process.getNodeName() != null) {
            pushScope(process.getNodeName());
        }
        pushScope(process.getName());
    }

    private void popProcessScope(IrProcess process) {
        popScope();
        if (process.getNodeName() != null) {
            popScope();
        }
    }

    private void popScope() {
        path.remove(path.size() - 1);
    }

    // ------------------------------------------------------------------ program

    private void mapProgram(IrProgram program) {
        for (IrDecl.Constant constant : program.getConstants()) {
            mapConstant(constant);
        }
        for (IrDecl.Enum enumDecl : program.getEnums()) {
            mapEnum(enumDecl);
        }
        for (IrDecl declaration : program.getGlobalVariables()) {
            mapDeclaration(declaration);
        }
        for (IrDecl.Node node : program.getNodes()) {
            mapNode(node);
        }
        // Declarations of every process are mangled before any body, because
        // `shared x from process P` needs P's mangled names to already exist and P may
        // be declared after the process importing from it.
        for (IrProcess process : program.getProcesses()) {
            declareProcessVariables(process);
        }
        for (IrProcess process : program.getProcesses()) {
            mapProcessBody(process);
        }
    }

    /**
     * A node contributes its declarations to the processes bound to it, so its variable
     * map is recorded and then rolled back rather than staying in scope.
     */
    private void mapNode(IrDecl.Node node) {
        Map<String, String> saved = new LinkedHashMap<>(variableMap);
        pushScope(node.getName());

        Map<String, String> introduced = new LinkedHashMap<>();
        for (IrDecl.Constant constant : node.getConstants()) {
            String original = constant.getName();
            mapConstant(constant);
            introduced.put(original, constant.getName());
        }
        for (IrDecl declaration : node.getVariables()) {
            String original = declaration.getName();
            mapDeclaration(declaration);
            introduced.put(original, declaration.getName());
        }

        popScope();
        nodeVariables.put(node.getName(), introduced);
        variableMap = saved;
    }

    /** Phase one: mangle a process's own declarations and record what it exports. */
    private void declareProcessVariables(IrProcess process) {
        Map<String, String> saved = new LinkedHashMap<>(variableMap);
        // The node's declarations come into scope before the process's own.
        Map<String, String> fromNode = nodeVariables.get(process.getNodeName());
        if (fromNode != null) {
            variableMap.putAll(fromNode);
        }

        pushProcessScope(process);
        Map<String, String> own = new LinkedHashMap<>();
        for (IrDecl declaration : process.getVariables()) {
            String original = declaration.getName();
            mapDeclaration(declaration);
            own.put(original, declaration.getName());
        }
        popProcessScope(process);

        processVariables.put(process.getName(), own);
        variableMap = saved;
    }

    /** Phase two: mangle the state bodies, with imported names now resolvable. */
    private void mapProcessBody(IrProcess process) {
        Map<String, String> saved = new LinkedHashMap<>(variableMap);

        Map<String, String> fromNode = nodeVariables.get(process.getNodeName());
        if (fromNode != null) {
            variableMap.putAll(fromNode);
        }
        variableMap.putAll(processVariables.get(process.getName()));

        for (IrDecl.SharedImport shared : process.getSharedImports()) {
            Map<String, String> provider = processVariables.get(shared.getSourceProcess());
            if (provider == null) {
                throw new IllegalStateException("process " + process.getName()
                        + " imports from unknown process " + shared.getSourceProcess());
            }
            for (String variable : shared.getVariables()) {
                String mangled = provider.get(variable);
                if (mangled == null) {
                    throw new IllegalStateException("process " + shared.getSourceProcess()
                            + " has no variable " + variable + " to share with " + process.getName());
                }
                // An imported name refers to the very same variable, so it maps to the
                // provider's mangled name rather than getting one of its own.
                variableMap.put(variable, mangled);
            }
        }

        pushProcessScope(process);
        for (IrState state : process.getStates()) {
            mapState(state);
        }
        popProcessScope(process);

        variableMap = saved;
    }

    private void mapState(IrState state) {
        Map<String, String> saved = new LinkedHashMap<>(variableMap);
        pushScope(state.getName());

        for (IrStmt statement : state.getStatements()) {
            mapStatement(statement);
        }
        if (state.getTimeout() != null) {
            mapTimeRef(state.getTimeout().getDuration());
            mapStatement(state.getTimeout().getBody());
        }

        popScope();
        variableMap = saved;
    }

    // ------------------------------------------------------------------ declarations

    private void mapDeclaration(IrDecl declaration) {
        if (declaration instanceof IrDecl.Variable variable) {
            mapVariable(variable);
        } else if (declaration instanceof IrDecl.PhysicalVariable physical) {
            mapPhysicalVariable(physical);
        } else if (declaration instanceof IrDecl.Constant constant) {
            mapConstant(constant);
        }
    }

    private void mapVariable(IrDecl.Variable variable) {
        // The initialiser is mapped first: it is evaluated in the enclosing scope, so a
        // declaration cannot refer to itself.
        if (variable.getInitializer() != null) {
            mapExpression(variable.getInitializer());
        }
        String original = variable.getName();
        String mangled = newName(path, original);
        variable.setName(mangled);
        variableMap.put(original, mangled);
    }

    private void mapConstant(IrDecl.Constant constant) {
        if (constant.getValue() != null) {
            mapExpression(constant.getValue());
        }
        String original = constant.getName();
        String mangled = newName(path, original);
        constant.setName(mangled);
        variableMap.put(original, mangled);
    }

    private void mapPhysicalVariable(IrDecl.PhysicalVariable variable) {
        String original = variable.getName();
        String mangled = newIndirectName(variable.getReadPort(), variable.getBit());
        variable.setName(mangled);
        variableMap.put(original, mangled);
        isDirect.put(mangled, variable.isDirect());
        if (variable.isDirect()) {
            directCount.put(mangled, 0);
        }
    }

    private void mapEnum(IrDecl.Enum enumDecl) {
        for (IrDecl.EnumMember member : enumDecl.getMembers()) {
            if (member.getValue() != null) {
                mapExpression(member.getValue());
            }
            String original = member.getName();
            String mangled = newName(path, original);
            member.setName(mangled);
            variableMap.put(original, mangled);
        }
    }

    // ------------------------------------------------------------------ statements

    private void mapStatement(IrStmt statement) {
        if (statement == null) {
            return;
        }
        if (statement instanceof IrStmt.Block block) {
            // A block is a scope of its own; the spec names it by a unique id.
            Map<String, String> saved = new LinkedHashMap<>(variableMap);
            pushScope("block" + blockCounter++);
            block.getStatements().forEach(this::mapStatement);
            popScope();
            variableMap = saved;
        } else if (statement instanceof IrStmt.If ifStmt) {
            mapExpression(ifStmt.getCondition());
            Map<String, String> saved = new LinkedHashMap<>(variableMap);
            mapStatement(ifStmt.getThenBranch());
            variableMap = saved;
            saved = new LinkedHashMap<>(variableMap);
            mapStatement(ifStmt.getElseBranch());
            variableMap = saved;
        } else if (statement instanceof IrStmt.Switch switchStmt) {
            Map<String, String> saved = new LinkedHashMap<>(variableMap);
            pushScope("switch" + blockCounter++);
            mapExpression(switchStmt.getSelector());
            for (IrStmt.SwitchCase clause : switchStmt.getCases()) {
                if (clause.getLabel() != null) {
                    mapExpression(clause.getLabel());
                }
                clause.getStatements().forEach(this::mapStatement);
            }
            popScope();
            variableMap = saved;
        } else if (statement instanceof IrStmt.ExprStatement expr) {
            mapExpression(expr.getExpression());
        } else if (statement instanceof IrStmt.LocalVar local) {
            mapVariable(local.getDeclaration());
        } else if (statement instanceof IrStmt.Wait wait) {
            Map<String, String> saved = new LinkedHashMap<>(variableMap);
            mapExpression(wait.getCondition());
            if (wait.getTimeout() != null) {
                mapTimeRef(wait.getTimeout());
            }
            mapStatement(wait.getTimeoutBody());
            variableMap = saved;
        } else if (statement instanceof IrStmt.For forStmt) {
            Map<String, String> saved = new LinkedHashMap<>(variableMap);
            pushScope("for" + blockCounter++);
            forStmt.getInitDeclarations().forEach(this::mapVariable);
            if (forStmt.getInitExpression() != null) {
                mapExpression(forStmt.getInitExpression());
            }
            mapExpression(forStmt.getCondition());
            mapExpression(forStmt.getUpdate());
            mapStatement(forStmt.getBody());
            popScope();
            variableMap = saved;
        }
        // Empty, ProcessControl, ResetTimer, SetState, Slice and CCode name no variables.
        // Process and state names are identifiers in their own namespaces and are left
        // alone, so `start P` and `set state s` keep referring to what they always did.
    }

    private void mapTimeRef(TimeRef ref) {
        if (ref != null && ref.isName()) {
            String mapped = variableMap.get(ref.getText());
            if (mapped != null) {
                ref.setText(mapped);
            }
        }
    }

    // ------------------------------------------------------------------ expressions

    private void mapExpression(IrExpr expr) {
        if (expr == null) {
            return;
        }
        if (expr instanceof IrExpr.VarRef ref) {
            mapVarRef(ref, true);
        } else if (expr instanceof IrExpr.Binary binary) {
            mapExpression(binary.getLeft());
            mapExpression(binary.getRight());
        } else if (expr instanceof IrExpr.Unary unary) {
            mapExpression(unary.getOperand());
        } else if (expr instanceof IrExpr.Cast cast) {
            mapExpression(cast.getOperand());
        } else if (expr instanceof IrExpr.Assign assign) {
            // The target is a write, so a direct binding is not counted as a read here.
            mapVarRef(assign.getTarget(), false);
            mapExpression(assign.getValue());
        } else if (expr instanceof IrExpr.IncDec incDec) {
            mapVarRef(incDec.getTarget(), false);
        } else if (expr instanceof IrExpr.Call call) {
            call.getArguments().forEach(this::mapExpression);
        } else if (expr instanceof IrExpr.Aggregate aggregate) {
            for (IrExpr.Aggregate.Element element : aggregate.getElements()) {
                if (element.getDesignator() instanceof IrExpr.IndexAccess index) {
                    mapExpression(index.getIndex());
                }
                mapExpression(element.getValue());
            }
        }
        // Literals and process-state checks name no variables; CheckState refers to a
        // process, which is not part of the variable namespace.
    }

    /**
     * Rewrites one variable use. {@code counted} is false for assignment targets: a
     * direct binding is re-read on each access, so only reads advance its counter.
     */
    private void mapVarRef(IrExpr.VarRef ref, boolean counted) {
        String mangled = variableMap.get(ref.getName());
        if (mangled == null) {
            // Not a known variable - a name from an unresolved shared import or an
            // undeclared identifier. Left as written for a later stage to diagnose.
            mapAccesses(ref);
            return;
        }
        if (counted && Boolean.TRUE.equals(isDirect.get(mangled))) {
            int accessNumber = directCount.getOrDefault(mangled, 0);
            String accessName = newDirectName(mangled, accessNumber);
            ref.setName(accessName);
            directAccessNames.put(accessName, mangled);
            directCount.put(mangled, accessNumber + 1);
        } else {
            ref.setName(mangled);
        }
        mapAccesses(ref);
    }

    private void mapAccesses(IrExpr.VarRef ref) {
        for (IrExpr.Access access : ref.getAccesses()) {
            if (access instanceof IrExpr.IndexAccess index) {
                mapExpression(index.getIndex());
            }
        }
    }

}
