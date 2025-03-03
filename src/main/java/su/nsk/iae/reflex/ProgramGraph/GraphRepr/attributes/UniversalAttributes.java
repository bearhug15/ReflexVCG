package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;

import java.util.AbstractMap;
import java.util.HashMap;

public class UniversalAttributes extends LinearFragment{
    IReflexNode node;
    public UniversalAttributes(IReflexNode node){
        this.node = node;
    }

    void setAttributesContext(IReflexNode node){
        this.node = node;
    }
    /*public void setReset(boolean reset) {
        this.reset = reset;
    }
    public void setStateChanging(boolean stateChanging) {
        this.stateChanging = stateChanging;
    }
    public void setProcChange(Map<ProcessNode, ChangeType> procChange) {
        this.procChange = procChange;
    }
    public void addProcessChange(ProcessNode ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }*/
    /*public void setPotProcChange(Map<ProcessNode, PotChange> potProcChange) {
        this.potProcChange = potProcChange;
    }*/
    public void addPotProcChange(ProcessNode node, ChangeType change) {
        potProcChange.put(new AbstractMap.SimpleImmutableEntry<>(node,change),Boolean.TRUE);
    }
    @Override
    public IReflexNode getAttributedNode() {
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
