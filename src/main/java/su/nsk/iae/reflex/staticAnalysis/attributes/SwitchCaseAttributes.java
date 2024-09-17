package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class SwitchCaseAttributes implements IAttributed {
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;

    HashMap<ReflexParser.ProcessContext,ChangeType> procChange = new HashMap<>();
    Vector<Pair<ReflexParser.ProcessContext, ProcessActivity>> activity= null;
    boolean reset = false;
    boolean stateChange = false;

    String setState = null;

    public SwitchCaseAttributes(ReflexParser.ProcessContext process, ReflexParser.StateContext state, ReflexParser.SwitchStatContext attributedContext){
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

    public Vector<Pair<ReflexParser.ProcessContext, ProcessActivity>> getActivity() {
        return activity;
    }

    public void setActivity(Vector<Pair<ReflexParser.ProcessContext, ProcessActivity>> activity) {
        this.activity = activity;
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

    @Override
    public ParserRuleContext getAttributedContext() {
        return null;
    }

    @Override
    public AttributedContextType getContextType() {
        return null;
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

    public void addAnotherAttributes(SwitchCaseAttributes other){
        if(other.reset){
            reset = true;
        }
        procChange.putAll(other.getProcChange());
    }

    public HashMap<ReflexParser.ProcessContext,ChangeType> procChangeIntersection(SwitchCaseAttributes other){
        Set<Map.Entry<ReflexParser.ProcessContext,ChangeType>> set = procChange.entrySet();
        set.retainAll(other.procChange.entrySet());
        HashMap<ReflexParser.ProcessContext,ChangeType> mapFromSet = new HashMap<>();
        for(Map.Entry<ReflexParser.ProcessContext,ChangeType> entry : set)
        {
            mapFromSet.put(entry.getKey(), entry.getValue());
        }
        return mapFromSet;
    }

    public HashMap<ReflexParser.ProcessContext,ChangeType> procChangeIntersection(HashMap<ReflexParser.ProcessContext,ChangeType> map){
        Set<Map.Entry<ReflexParser.ProcessContext,ChangeType>> set = procChange.entrySet();
        set.retainAll(map.entrySet());
        HashMap<ReflexParser.ProcessContext,ChangeType> mapFromSet = new HashMap<>();
        for(Map.Entry<ReflexParser.ProcessContext,ChangeType> entry : set)
        {
            mapFromSet.put(entry.getKey(), entry.getValue());
        }
        return mapFromSet;
    }
}
