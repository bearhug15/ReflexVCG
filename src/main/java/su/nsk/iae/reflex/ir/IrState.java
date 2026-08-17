package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * A state of a Reflex process: the statements run on each cycle while the process is in
 * it, plus an optional timeout clause.
 *
 * <p>Normalisation synthesises additional states from {@code wait} and {@code slice};
 * those are marked {@link #isSynthetic()} so later stages and diagnostics can tell them
 * from states the programmer wrote.
 */
public final class IrState extends IrNode {

    private String name;
    private boolean looped;
    private boolean synthetic;

    private final List<IrStmt> statements;
    private Timeout timeout;

    public IrState(String name, boolean looped) {
        this(name, looped, new ArrayList<>());
    }

    public IrState(String name, boolean looped, List<IrStmt> statements) {
        this.name = name;
        this.looped = looped;
        this.statements = statements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** {@code looped}: the state re-enters itself instead of advancing. */
    public boolean isLooped() {
        return looped;
    }

    public void setLooped(boolean looped) {
        this.looped = looped;
    }

    /** True for a state created by normalisation rather than written in the source. */
    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public List<IrStmt> getStatements() {
        return statements;
    }

    /** The {@code timeout} clause, or null. */
    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    /** A state's {@code timeout <duration> <statement>} clause. */
    public static final class Timeout extends IrNode {
        private TimeRef duration;
        private IrStmt body;

        public Timeout(TimeRef duration, IrStmt body) {
            this.duration = duration;
            this.body = body;
        }

        public TimeRef getDuration() {
            return duration;
        }

        public void setDuration(TimeRef duration) {
            this.duration = duration;
        }

        public IrStmt getBody() {
            return body;
        }

        public void setBody(IrStmt body) {
            this.body = body;
        }

        @Override
        public String toString() {
            return "timeout " + duration;
        }
    }

    @Override
    public String toString() {
        return "state " + name + (looped ? " looped" : "");
    }
}
