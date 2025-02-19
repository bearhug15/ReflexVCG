package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;

import java.util.HashMap;

public class UniversalAttributes extends LinearFragment{
    IReflexNode node;
    public UniversalAttributes(IReflexNode node){
        this.node = node;
    }

    void setAttributesContext(IReflexNode node){
        this.node = node;
    }
    public void setReset(boolean reset) {
        this.reset = reset;
    }
    public void setStateChanging(boolean stateChanging) {
        this.stateChanging = stateChanging;
    }
    public void setProcChange(HashMap<ProcessNode, ChangeType> procChange) {
        this.procChange = procChange;
    }
    public void addProcessChange(ProcessNode ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }
    public void setPotProcChange(HashMap<ProcessNode, PotChange> potProcChange) {
        this.potProcChange = potProcChange;
    }
    public void addPotProcChange(ProcessNode ctx, PotChange change) {
        PotChange res = potProcChange.get(ctx);
        if(res!=null){
            potProcChange.put(ctx,res.add(change));
        }else{
            potProcChange.put(ctx,change);
        }
    }
    @Override
    public IReflexNode getAttributedContext() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.Statement;
    }

    public void setProcStatuses(HashMap<String, String> procStatuses){
        this.procStatuses = procStatuses;
    }
}
