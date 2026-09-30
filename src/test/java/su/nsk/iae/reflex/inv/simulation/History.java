package su.nsk.iae.reflex.inv.simulation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One run of a program as ReflexBase's {@code state}: a line of {@code setVar},
 * {@code setPstate}, {@code reset} and {@code toEnv} from {@code emptyState}. Every state
 * of the run is a prefix of it, a {@link Node}.
 *
 * <p>The functions of ReflexBase.thy and the program theory - {@code getVarVal},
 * {@code getPstate}, {@code predEnv}, {@code prevProcState}, {@code ltime},
 * {@code toEnvNum} - are answered from indexes rather than by walking back, so a long run
 * stays cheap to question. Each says which function it mirrors.
 */
final class History {

    enum Kind { EMPTY, SET_VAR, TO_ENV, SET_PSTATE, RESET }

    /** A state of the run: everything up to and including position {@code position}. */
    final class Node {
        final int position;
        final Kind kind;
        final String name;
        final Val value;
        final String target;

        private Node(int position, Kind kind, String name, Val value, String target) {
            this.position = position;
            this.kind = kind;
            this.name = name;
            this.value = value;
            this.target = target;
        }

        History history() {
            return History.this;
        }

        /** The state {@code steps} constructors back. */
        Node back(int steps) {
            return nodes.get(Math.max(0, position - steps));
        }

        Node setVar(String variable, Val val) {
            return append(Kind.SET_VAR, variable, val, null);
        }

        /** setVarVal. */
        Node setVarVal(String variable, List<Val.Access> path, Val val) {
            return setVar(variable, Val.updValPath(getVarVal(variable, List.of()), path, val));
        }

        Node setPstate(String process, String state) {
            return append(Kind.SET_PSTATE, process, null, state);
        }

        Node reset(String process) {
            return append(Kind.RESET, process, null, null);
        }

        Node toEnv() {
            return append(Kind.TO_ENV, null, null, null);
        }

        private Node append(Kind kind, String name, Val value, String target) {
            if (position != nodes.size() - 1) {
                throw new IllegalStateException("a run is a line: only its last state can be extended");
            }
            Node node = new Node(nodes.size(), kind, name, value, target);
            nodes.add(node);
            switch (kind) {
                case SET_VAR -> writes.computeIfAbsent(name, k -> new ArrayList<>()).add(node.position);
                case SET_PSTATE -> {
                    moves.computeIfAbsent(name, k -> new ArrayList<>()).add(node.position);
                    restarts.computeIfAbsent(name, k -> new ArrayList<>()).add(node.position);
                }
                case RESET -> restarts.computeIfAbsent(name, k -> new ArrayList<>()).add(node.position);
                case TO_ENV -> boundaries.add(node.position);
                default -> {
                }
            }
            return node;
        }

        /** getVarVal. */
        Val getVarVal(String variable, List<Val.Access> path) {
            int at = lastAtOrBefore(writes.get(variable), position);
            return at < 0 ? Val.Nil.NIL : Val.applyAccess(nodes.get(at).value, path);
        }

        /** getPstate. */
        String getPstate(String process) {
            int at = lastAtOrBefore(moves.get(process), position);
            return at < 0 ? "stop" : nodes.get(at).target;
        }

        /** toEnvP. */
        boolean toEnvP() {
            return kind == Kind.TO_ENV;
        }

        /** predEnv: the last boundary strictly before this state, or emptyState. */
        Node predEnv() {
            int at = lastAtOrBefore(boundaries, position - 1);
            return at < 0 ? nodes.get(0) : nodes.get(at);
        }

        /**
         * prevProcState: walking back, the first setPstate of the process that changed its
         * state, and the state it was applied to.
         */
        Node prevProcState(String process) {
            List<Integer> ofProcess = moves.getOrDefault(process, List.of());
            for (int i = ofProcess.size() - 1; i >= 0; i--) {
                int at = ofProcess.get(i);
                if (at > position) {
                    continue;
                }
                Node before = nodes.get(at - 1);
                if (!before.getPstate(process).equals(nodes.get(at).target)) {
                    return before;
                }
            }
            return nodes.get(0);
        }

        /** ltime: one clock per boundary since the process last moved or was reset. */
        BigInteger ltime(String process, long clock) {
            int since = lastAtOrBefore(restarts.get(process), position);
            int count = countAtOrBefore(boundaries, position) - countAtOrBefore(boundaries, Math.max(since, 0));
            if (since < 0) {
                count = countAtOrBefore(boundaries, position);
            }
            return BigInteger.valueOf(count).multiply(BigInteger.valueOf(clock));
        }

        /** toEnvNum emptyState s: the boundaries up to this state. */
        int boundariesSoFar() {
            return countAtOrBefore(boundaries, position);
        }

        @Override
        public String toString() {
            return "st@" + position;
        }
    }

    private final List<Node> nodes = new ArrayList<>();
    private final Map<String, List<Integer>> writes = new LinkedHashMap<>();
    private final Map<String, List<Integer>> moves = new LinkedHashMap<>();
    private final Map<String, List<Integer>> restarts = new LinkedHashMap<>();
    private final List<Integer> boundaries = new ArrayList<>();

    History() {
        nodes.add(new Node(0, Kind.EMPTY, null, null, null));
    }

    Node empty() {
        return nodes.get(0);
    }

    /** Every state of the run so far, in order. */
    List<Node> nodes() {
        return List.copyOf(nodes);
    }

    Node last() {
        return nodes.get(nodes.size() - 1);
    }

    /** The position of the last entry at or before {@code position}, or -1. */
    private static int lastAtOrBefore(List<Integer> positions, int position) {
        if (positions == null) {
            return -1;
        }
        int low = 0;
        int high = positions.size() - 1;
        int found = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (positions.get(mid) <= position) {
                found = positions.get(mid);
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return found;
    }

    private static int countAtOrBefore(List<Integer> positions, int position) {
        int low = 0;
        int high = positions.size();
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (positions.get(mid) <= position) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }
}
