package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.Vector;

public class SwitchCaseCore implements IAttributed {
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;

    Vector<SwitchCaseAttributes> branchAttributes;
    SwitchCaseAttributes defaultBranchAttributes;

    public SwitchCaseCore(ReflexParser.ProcessContext process, ReflexParser.StateContext state, ReflexParser.SwitchStatContext attributedContext){
        rootProcess = process;
        rootState = state;
        this.attributedContext = attributedContext;
    }

    public void setAttributedContext(ParserRuleContext attributedContext) {
        this.attributedContext = attributedContext;
    }

    public ReflexParser.ProcessContext getRootProcess() {
        return rootProcess;
    }

    public void setRootProcess(ReflexParser.ProcessContext rootProcess) {
        this.rootProcess = rootProcess;
    }

    public ReflexParser.StateContext getRootState() {
        return rootState;
    }

    public void setRootState(ReflexParser.StateContext rootState) {
        this.rootState = rootState;
    }

    public Vector<SwitchCaseAttributes> getBranchAttributes() {
        return branchAttributes;
    }

    public void setBranchAttributes(Vector<SwitchCaseAttributes> branchAttributes) {
        this.branchAttributes = branchAttributes;
    }

    public SwitchCaseAttributes getDefaultBranchAttributes() {
        return defaultBranchAttributes;
    }

    public void addBranchAttributes(SwitchCaseAttributes attr){
        this.branchAttributes.add(attr);
    }

    public void setDefaultBranchAttributes(SwitchCaseAttributes defaultBranchAttributes) {
        this.defaultBranchAttributes = defaultBranchAttributes;
    }

    @Override
    public ParserRuleContext getAttributedContext() {
        return attributedContext;
    }

    @Override
    public AttributedContextType getContextType() {
        return AttributedContextType.SwitchCase;
    }
}
