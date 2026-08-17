package su.nsk.iae.reflex.cfg;

import su.nsk.iae.reflex.vc.VcStatement;
import su.nsk.iae.reflex.vc.VerificationCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Enumerates the paths of a {@link Cfg}, turning each into a verification condition.
 *
 * <p>A plain depth-first walk, emitting a condition on reaching the exit. The previous
 * generator extended jgrapht's AbstractGraphIterator while firing its own traversal
 * events and keeping the accumulated state in listener fields, which made the order of
 * effects hard to follow; here the recursion carries its own state and nothing is shared.
 *
 * <p>State variables are numbered along the path: an effect consumes the current one and
 * produces the next, so a condition reads as a chain {@code st0, st1, ... st_final}.
 */
public final class PathEnumerator {

    /** Guards against runaway enumeration if the graph ever contains a cycle. */
    private static final int MAX_PATHS = 500_000;

    private final Cfg cfg;
    private int emitted;

    public PathEnumerator(Cfg cfg) {
        this.cfg = cfg;
    }

    /** Collects every condition. Convenient for tests and small programs. */
    public List<VerificationCondition> enumerate() {
        List<VerificationCondition> conditions = new ArrayList<>();
        forEach(conditions::add);
        return conditions;
    }

    /** Streams conditions to {@code sink}, so large programs need not be held in memory. */
    public void forEach(Consumer<VerificationCondition> sink) {
        emitted = 0;
        walk(cfg.getEntry(), new ArrayList<>(), sink);
    }

    public int getEmitted() {
        return emitted;
    }

    private void walk(CfgNode node, List<CfgNode> path, Consumer<VerificationCondition> sink) {
        if (emitted >= MAX_PATHS) {
            throw new IllegalStateException(
                    "path enumeration exceeded " + MAX_PATHS + " conditions; the graph may contain a cycle");
        }

        path.add(node);
        if (node.isTerminal()) {
            sink.accept(toCondition(path));
            emitted++;
        } else {
            for (CfgNode successor : node.getSuccessors()) {
                walk(successor, path, sink);
            }
        }
        path.remove(path.size() - 1);
    }

    /** Converts one path into the chain of assumptions describing it. */
    private VerificationCondition toCondition(List<CfgNode> path) {
        VerificationCondition condition = new VerificationCondition();
        int stateIndex = 0;
        String current = stateName(stateIndex);

        condition.add(new VcStatement.Invariant(current));

        for (CfgNode node : path) {
            if (node instanceof CfgNode.InState inState) {
                condition.add(new VcStatement.ProcessInState(
                        current, inState.getProcess(), inState.getState()));
            } else if (node instanceof CfgNode.Guard guard) {
                condition.add(new VcStatement.Condition(current, guard.getCondition()));
            } else if (node instanceof CfgNode.TimeoutGuard timeout) {
                condition.add(new VcStatement.TimeoutCheck(current, timeout.getProcess(),
                        timeout.getDuration(), timeout.isExceeded()));
            } else if (node instanceof CfgNode.Assign assign) {
                String next = stateName(++stateIndex);
                condition.add(new VcStatement.Assign(next, current, assign.getTarget(), assign.getValue()));
                current = next;
            } else if (node instanceof CfgNode.SetState setState) {
                String next = stateName(++stateIndex);
                condition.add(new VcStatement.SetProcessState(
                        next, current, setState.getProcess(), setState.getState()));
                current = next;
            } else if (node instanceof CfgNode.ResetTimer reset) {
                String next = stateName(++stateIndex);
                condition.add(new VcStatement.ResetTimer(next, current, reset.getProcess()));
                current = next;
            } else if (node instanceof CfgNode.ToEnv) {
                String next = stateName(++stateIndex);
                condition.add(new VcStatement.ToEnv(next, current));
                current = next;
            } else if (node instanceof CfgNode.Unsupported unsupported) {
                throw new UnsupportedConstructException(unsupported);
            }
            // Entry, Exit and Join carry no assumption.
        }

        condition.add(new VcStatement.Final(su.nsk.iae.reflex.vc.IsabelleRenderer.FINAL_STATE, current));
        condition.setFinalState(su.nsk.iae.reflex.vc.IsabelleRenderer.FINAL_STATE);
        return condition;
    }

    private static String stateName(int index) {
        return "st" + index;
    }

    /** Raised when a path reaches a construct VC generation does not support. */
    public static final class UnsupportedConstructException extends RuntimeException {
        private final transient CfgNode.Unsupported node;

        UnsupportedConstructException(CfgNode.Unsupported node) {
            super("This feature is not supported: " + node.getConstruct() + " - " + node.getDetail());
            this.node = node;
        }

        public CfgNode.Unsupported getNode() {
            return node;
        }
    }
}
