package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.*;
import java.util.stream.Collectors;

public class IfElseAttributes implements IAttributed {
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;

    HashMap<ReflexParser.ProcessContext,ChangeType> procChange = new HashMap<>();
    boolean reset = false;
    boolean stateChange = false;

    String setState = null;

    public IfElseAttributes(ReflexParser.ProcessContext process, ReflexParser.StateContext state, ReflexParser.IfElseStatContext attributedContext){
        this.attributedContext = attributedContext;
        rootProcess = process;
        rootState = state;
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

    public String getSetState() {
        return setState;
    }

    public void setSetState(String setState) {
        this.setState = setState;
    }

    public HashMap<ReflexParser.ProcessContext, ChangeType> getProcChange() {
        return procChange;
    }

    public void setProcChange(HashMap<ReflexParser.ProcessContext, ChangeType> procChange) {
        this.procChange = procChange;
    }

    public void addProcessChange(ReflexParser.ProcessContext ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }

    public void addProcessChange(HashMap<ReflexParser.ProcessContext, ChangeType> other){
        this.procChange.putAll(other);
    }

    public void addReset(boolean reset){
        this.reset = this.reset || reset;
    }

    @Override
    public ParserRuleContext getAttributedContext() {
        return attributedContext;
    }

    @Override
    public AttributedContextType getContextType() {
        return AttributedContextType.IfElse;
    }

    public boolean resetIntersection(IfElseAttributes other){
        if(other==null) return false;
        return reset&&other.reset;
    }

    public HashMap<ReflexParser.ProcessContext,ChangeType> procChangeIntersection(IfElseAttributes other){
        if (other==null) return new HashMap<>();
        Set<Map.Entry<ReflexParser.ProcessContext,ChangeType>> set = procChange.entrySet();
        set.retainAll(other.procChange.entrySet());
        HashMap<ReflexParser.ProcessContext,ChangeType> mapFromSet = new HashMap<>();
        for(Map.Entry<ReflexParser.ProcessContext,ChangeType> entry : set)
        {
            mapFromSet.put(entry.getKey(), entry.getValue());
        }
        return mapFromSet;
    }
}
