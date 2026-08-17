package su.nsk.iae.reflex.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Statements of the Reflex IR.
 *
 * <p>Statement lists are mutable in place: the normalisation pass of Preprocessing.tex
 * splits a state's body at every {@code slice} / {@code wait} into fresh "light" states,
 * and expands switch fall-through by copying the following case's statements into the
 * preceding one.
 */
public abstract class IrStmt extends IrNode {

    public abstract <R> R accept(Visitor<R> visitor);

    // ------------------------------------------------------------------ statements

    public static final class Empty extends IrStmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitEmpty(this);
        }

        @Override
        public String toString() {
            return ";";
        }
    }

    /** A braced statement sequence. Introduces a scope for name mangling. */
    public static final class Block extends IrStmt {
        private final List<IrStmt> statements;

        public Block() {
            this(new ArrayList<>());
        }

        public Block(List<IrStmt> statements) {
            this.statements = statements;
        }

        public List<IrStmt> getStatements() {
            return statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlock(this);
        }

        @Override
        public String toString() {
            return "{ " + statements.size() + " statements }";
        }
    }

    /** How a process-control statement affects its target. */
    public enum ControlKind { START, STOP, ERROR, RESTART }

    /**
     * {@code start P} / {@code stop} / {@code stop P} / {@code error} / {@code restart}.
     * A null process means the enclosing one; {@code restart} always targets it.
     */
    public static final class ProcessControl extends IrStmt {
        private final ControlKind kind;
        private String process;

        public ProcessControl(ControlKind kind, String process) {
            this.kind = kind;
            this.process = process;
        }

        public ControlKind getKind() {
            return kind;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        /** True when this targets the process that lexically encloses it. */
        public boolean targetsEnclosingProcess() {
            return process == null;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitProcessControl(this);
        }

        @Override
        public String toString() {
            return kind.name().toLowerCase() + (process == null ? "" : " " + process);
        }
    }

    public static final class ResetTimer extends IrStmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitResetTimer(this);
        }

        @Override
        public String toString() {
            return "reset timer";
        }
    }

    /**
     * {@code set state X} or {@code set next state}. Normalisation resolves
     * {@code next} to a concrete state name.
     */
    public static final class SetState extends IrStmt {
        private String state;
        private final boolean next;

        public SetState(String state, boolean next) {
            this.state = state;
            this.next = next;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        /** True if written as {@code set next state}; the target is resolved later. */
        public boolean isNext() {
            return next;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetState(this);
        }

        @Override
        public String toString() {
            return state == null ? "set next state" : "set state " + state;
        }
    }

    public static final class If extends IrStmt {
        private IrExpr condition;
        private IrStmt thenBranch;
        private IrStmt elseBranch;

        public If(IrExpr condition, IrStmt thenBranch, IrStmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public IrExpr getCondition() {
            return condition;
        }

        public void setCondition(IrExpr condition) {
            this.condition = condition;
        }

        public IrStmt getThenBranch() {
            return thenBranch;
        }

        public void setThenBranch(IrStmt thenBranch) {
            this.thenBranch = thenBranch;
        }

        /** Null when the source had no else. */
        public IrStmt getElseBranch() {
            return elseBranch;
        }

        public void setElseBranch(IrStmt elseBranch) {
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIf(this);
        }

        @Override
        public String toString() {
            return "if (" + condition + ")";
        }
    }

    /** One case of a switch. {@code label} is null for the default clause. */
    public static final class SwitchCase extends IrNode {
        private IrExpr label;
        private final List<IrStmt> statements;
        private boolean breaks;

        public SwitchCase(IrExpr label, List<IrStmt> statements, boolean breaks) {
            this.label = label;
            this.statements = statements;
            this.breaks = breaks;
        }

        public IrExpr getLabel() {
            return label;
        }

        public void setLabel(IrExpr label) {
            this.label = label;
        }

        public boolean isDefault() {
            return label == null;
        }

        public List<IrStmt> getStatements() {
            return statements;
        }

        /** Whether the clause ends in break; normalisation uses this for fall-through. */
        public boolean isBreaks() {
            return breaks;
        }

        public void setBreaks(boolean breaks) {
            this.breaks = breaks;
        }

        @Override
        public String toString() {
            return (isDefault() ? "default" : "case " + label) + ": " + statements.size() + " statements";
        }
    }

    public static final class Switch extends IrStmt {
        private IrExpr selector;
        private final List<SwitchCase> cases;

        public Switch(IrExpr selector, List<SwitchCase> cases) {
            this.selector = selector;
            this.cases = cases;
        }

        public IrExpr getSelector() {
            return selector;
        }

        public void setSelector(IrExpr selector) {
            this.selector = selector;
        }

        /** All clauses in source order, the default one included if present. */
        public List<SwitchCase> getCases() {
            return cases;
        }

        public SwitchCase getDefaultCase() {
            return cases.stream().filter(SwitchCase::isDefault).findFirst().orElse(null);
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitch(this);
        }

        @Override
        public String toString() {
            return "switch (" + selector + ") " + cases.size() + " cases";
        }
    }

    public static final class ExprStatement extends IrStmt {
        private IrExpr expression;

        public ExprStatement(IrExpr expression) {
            this.expression = expression;
        }

        public IrExpr getExpression() {
            return expression;
        }

        public void setExpression(IrExpr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }

        @Override
        public String toString() {
            return expression + ";";
        }
    }

    /** A local variable declaration used as a statement. */
    public static final class LocalVar extends IrStmt {
        private IrDecl.Variable declaration;

        public LocalVar(IrDecl.Variable declaration) {
            this.declaration = declaration;
        }

        public IrDecl.Variable getDeclaration() {
            return declaration;
        }

        public void setDeclaration(IrDecl.Variable declaration) {
            this.declaration = declaration;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLocalVar(this);
        }

        @Override
        public String toString() {
            return declaration.toString();
        }
    }

    /**
     * {@code slice}: yield to the environment. Normalisation turns everything after it
     * into a fresh light state.
     */
    public static final class Slice extends IrStmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSlice(this);
        }

        @Override
        public String toString() {
            return "slice;";
        }
    }

    /**
     * {@code wait (cond)}, optionally {@code on timeout t body}. Like slice, this is
     * eliminated by normalisation in favour of explicit light states.
     */
    public static final class Wait extends IrStmt {
        private IrExpr condition;
        private TimeRef timeout;
        private IrStmt timeoutBody;

        public Wait(IrExpr condition, TimeRef timeout, IrStmt timeoutBody) {
            this.condition = condition;
            this.timeout = timeout;
            this.timeoutBody = timeoutBody;
        }

        public IrExpr getCondition() {
            return condition;
        }

        public void setCondition(IrExpr condition) {
            this.condition = condition;
        }

        /** Null for a plain wait with no timeout clause. */
        public TimeRef getTimeout() {
            return timeout;
        }

        public void setTimeout(TimeRef timeout) {
            this.timeout = timeout;
        }

        public IrStmt getTimeoutBody() {
            return timeoutBody;
        }

        public void setTimeoutBody(IrStmt timeoutBody) {
            this.timeoutBody = timeoutBody;
        }

        public boolean hasTimeout() {
            return timeout != null;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWait(this);
        }

        @Override
        public String toString() {
            return "wait (" + condition + ")" + (hasTimeout() ? " on timeout " + timeout : "");
        }
    }

    /** Not supported by VC generation; the graph node reports that when reached. */
    public static final class For extends IrStmt {
        private final List<IrDecl.Variable> initDeclarations;
        private IrExpr initExpression;
        private IrExpr condition;
        private IrExpr update;
        private IrStmt body;

        public For(List<IrDecl.Variable> initDeclarations, IrExpr initExpression,
                   IrExpr condition, IrExpr update, IrStmt body) {
            this.initDeclarations = initDeclarations;
            this.initExpression = initExpression;
            this.condition = condition;
            this.update = update;
            this.body = body;
        }

        public List<IrDecl.Variable> getInitDeclarations() {
            return initDeclarations;
        }

        public IrExpr getInitExpression() {
            return initExpression;
        }

        public void setInitExpression(IrExpr initExpression) {
            this.initExpression = initExpression;
        }

        public IrExpr getCondition() {
            return condition;
        }

        public void setCondition(IrExpr condition) {
            this.condition = condition;
        }

        public IrExpr getUpdate() {
            return update;
        }

        public void setUpdate(IrExpr update) {
            this.update = update;
        }

        public IrStmt getBody() {
            return body;
        }

        public void setBody(IrStmt body) {
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFor(this);
        }

        @Override
        public String toString() {
            return "for (...) ...";
        }
    }

    /** Inline C, opaque to analysis. Not supported by VC generation. */
    public static final class CCode extends IrStmt {
        private final String code;

        public CCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCCode(this);
        }

        @Override
        public String toString() {
            return "$" + code;
        }
    }

    // ------------------------------------------------------------------ visitor

    public interface Visitor<R> {
        R visitEmpty(Empty stmt);

        R visitBlock(Block stmt);

        R visitProcessControl(ProcessControl stmt);

        R visitResetTimer(ResetTimer stmt);

        R visitSetState(SetState stmt);

        R visitIf(If stmt);

        R visitSwitch(Switch stmt);

        R visitExprStatement(ExprStatement stmt);

        R visitLocalVar(LocalVar stmt);

        R visitSlice(Slice stmt);

        R visitWait(Wait stmt);

        R visitFor(For stmt);

        R visitCCode(CCode stmt);
    }
}
