package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.HashMap;
import java.util.Vector;

public class StateAttributes implements IAttributed {
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;

    HashMap<ReflexParser.ProcessContext,ChangeType> procChange = new HashMap<>();
    boolean reset = false;
    boolean stateChange = false;

    ReflexParser.StateContext setState = null;

    public StateAttributes(ReflexParser.ProcessContext process, ReflexParser.StateContext attributedContext){
        rootProcess = process;
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


    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public boolean isStateChange() {
        return stateChange;
    }

    public void setStateChange(boolean stateChange) {
        this.stateChange = stateChange;
    }

    public ReflexParser.StateContext getSetState() {
        return setState;
    }

    public void setSetState(ReflexParser.StateContext setState) {
        this.setState = setState;
    }

    @Override
    public ParserRuleContext getAttributedContext() {
        return attributedContext;
    }

    public HashMap<ReflexParser.ProcessContext, ChangeType> getProcChange() {
        return procChange;
    }

    public void setProcChange(HashMap<ReflexParser.ProcessContext, ChangeType> procChange) {
        this.procChange = procChange;
    }

    public void addReset(boolean reset){
        this.reset = this.reset || reset;
    }

    public void addProcessChange(ReflexParser.ProcessContext ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }
    public void addProcessChange(HashMap<ReflexParser.ProcessContext, ChangeType> other){
        this.procChange.putAll(other);
    }
    @Override
    public AttributedContextType getContextType() {
        return AttributedContextType.State;
    }
}
