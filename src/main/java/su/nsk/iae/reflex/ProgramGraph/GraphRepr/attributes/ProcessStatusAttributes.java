package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProcessStatusAttributes extends LinearFragment{
    IReflexNode attributedNode;
    public ProcessStatusAttributes(IReflexNode node){
        this.attributedNode = node;
    }
    @Override
    public IReflexNode getAttributedNode() {
        return attributedNode;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.ProcessStatus;
    }

    @Override
    public boolean isReset() {
        return false;
    }

    @Override
    public boolean isStateChanging() {
        return false;
    }

    @Override
    public Map<ProcessNode, ChangeType> getProcChange() {
        return new HashMap<>();
    }

    @Override
    public Map<Map.Entry<ProcessNode, ChangeType>, Boolean> getPotProcChange() {
        return new HashMap<>();
    }

    public void setProcStatuses(Map<String,ProcessStatus> procStatuses){
        this.procStatuses = procStatuses;
    }

    @Override
    public ArrayList<IAttributed> getAttributes() {
        return new ArrayList<>();
    }

    @Override
    public void addAttributes(IAttributed attr) {

    }

    @Override
    public void liftAttributes() {

    }
}
