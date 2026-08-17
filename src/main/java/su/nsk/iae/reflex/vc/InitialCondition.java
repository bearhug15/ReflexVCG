package su.nsk.iae.reflex.vc;

import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the condition for the program's starting state - the base case of the induction.
 *
 * <p>Every other condition has the shape "assume the invariant holds, run one cycle, show
 * it still holds". That is the inductive step, and on its own it proves nothing: something
 * has to establish that the invariant holds to begin with. This condition does, and it is
 * the only one generated without an {@code inv(st0)} assumption.
 *
 * <p>It starts from {@code emptyState}, applies the initialisers the program declares, puts
 * the first process into its first state - every other process is stopped in
 * {@code emptyState} already, since getPstate answers ''stop'' there - and yields to the
 * environment.
 */
public final class InitialCondition {

    private InitialCondition() {
    }

    public static VerificationCondition build(IrProgram program) {
        VerificationCondition condition = new VerificationCondition();
        Counter counter = new Counter();
        String current = counter.next();

        condition.add(new VcStatement.EmptyState(current));

        for (Assignment assignment : initialAssignments(program)) {
            String next = counter.next();
            condition.add(new VcStatement.Assign(next, current, assignment.target(), assignment.value()));
            current = next;
        }

        IrProcess start = program.getProcesses().isEmpty() ? null : program.getProcesses().get(0);
        if (start != null && start.getStartState() != null) {
            String next = counter.next();
            condition.add(new VcStatement.SetProcessState(
                    next, current, start.getName(), start.getStartState().getName()));
            current = next;
        }

        String next = counter.next();
        condition.add(new VcStatement.ToEnv(next, current));
        current = next;

        condition.add(new VcStatement.Final(IsabelleRenderer.FINAL_STATE, current));
        condition.setFinalState(IsabelleRenderer.FINAL_STATE);
        return condition;
    }

    /** One value written into one place. */
    private record Assignment(IrExpr.VarRef target, IrExpr value) {
    }

    private static final class Counter {
        private int index;

        String next() {
            return "st" + index++;
        }
    }

    /**
     * The initialisers the program declares, in declaration order: program-level variables
     * first, then each node's.
     */
    private static List<Assignment> initialAssignments(IrProgram program) {
        List<Assignment> assignments = new ArrayList<>();
        for (IrDecl declaration : program.getGlobalVariables()) {
            collect(declaration, program, assignments);
        }
        for (IrDecl.Node node : program.getNodes()) {
            node.getVariables().forEach(declaration -> collect(declaration, program, assignments));
        }
        return assignments;
    }

    private static void collect(IrDecl declaration, IrProgram program, List<Assignment> assignments) {
        if (!(declaration instanceof IrDecl.Variable variable) || variable.getInitializer() == null) {
            // Physical variables take their value from hardware, and a variable with no
            // initialiser starts at the default its type gives it in emptyState.
            return;
        }
        expand(variable.getName(), variable.getType(), List.of(),
                variable.getInitializer(), program, assignments);
    }

    /**
     * Flattens an initialiser into one assignment per scalar written.
     *
     * <p>Aggregate initialisers are partial, so only the members actually given produce an
     * assignment; the rest keep the default they already have. A designator says which
     * member an element targets, and without one the elements fill in order.
     */
    private static void expand(String name, IrType type, List<IrExpr.Access> path,
                               IrExpr initializer, IrProgram program, List<Assignment> assignments) {
        if (!(initializer instanceof IrExpr.Aggregate aggregate)) {
            IrExpr.VarRef target = new IrExpr.VarRef(name, new ArrayList<>(path));
            target.setResultType(type);
            assignments.add(new Assignment(target, initializer));
            return;
        }

        int position = 0;
        for (IrExpr.Aggregate.Element element : aggregate.getElements()) {
            IrExpr.Access step;
            IrType memberType;

            if (element.getDesignator() != null) {
                step = element.getDesignator();
                memberType = typeOfStep(type, step, program);
            } else {
                step = positionalStep(type, position, program);
                memberType = typeOfStep(type, step, program);
                position++;
            }
            if (step == null || memberType == null) {
                // The initialiser does not fit the declared type; a checking stage should
                // report that rather than this one guessing.
                continue;
            }

            List<IrExpr.Access> extended = new ArrayList<>(path);
            extended.add(step);
            expand(name, memberType, extended, element.getValue(), program, assignments);
        }
    }

    /** The access an element without a designator targets, by its position. */
    private static IrExpr.Access positionalStep(IrType type, int position, IrProgram program) {
        if (type instanceof IrType.Array) {
            IrExpr index = new IrExpr.Literal(IrExpr.Literal.Kind.INTEGER, Integer.toString(position));
            index.setResultType(IrType.INT32);
            return new IrExpr.IndexAccess(index);
        }
        if (type instanceof IrType.Struct struct) {
            IrDecl.Struct declaration = program.findStruct(struct.name());
            if (declaration == null || position >= declaration.getFields().size()) {
                return null;
            }
            return new IrExpr.FieldAccess(declaration.getFields().get(position).getName());
        }
        return null;
    }

    private static IrType typeOfStep(IrType type, IrExpr.Access step, IrProgram program) {
        if (type instanceof IrType.Array array && step instanceof IrExpr.IndexAccess) {
            return array.element();
        }
        if (type instanceof IrType.Struct struct && step instanceof IrExpr.FieldAccess field) {
            IrDecl.Struct declaration = program.findStruct(struct.name());
            if (declaration == null) {
                return null;
            }
            IrDecl.Variable member = declaration.findField(field.getField());
            return member == null ? null : member.getType();
        }
        return null;
    }
}
