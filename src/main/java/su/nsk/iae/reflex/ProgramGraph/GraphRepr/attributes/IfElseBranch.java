package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IfElseNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;

public class IfElseBranch extends LinearFragment {
    IReflexNode attributedNode;
    ProcessNode rootProcess;
    StateNode rootState;

    String setState = null;

    public IfElseBranch(ProcessNode process, StateNode state, IfElseNode attributedNode){
        this.attributedNode = attributedNode;
        rootProcess = process;
        rootState = state;
    }

    public void setAttributedContext(IfElseNode attributedNode) {
        this.attributedNode = attributedNode;
    }

    public ProcessNode getRootProcess() {
        return rootProcess;
    }

    public void setRootProcess(ProcessNode rootProcess) {
        this.rootProcess = rootProcess;
    }

    public StateNode getRootState() {
        return rootState;
    }

    public void setRootState(StateNode rootState) {
        this.rootState = rootState;
    }

    /*public boolean resetIntersection(IfElseAttributes other){
        if(other==null) return false;
        return reset&&other.reset;
    }

    public HashMap<ProcessNode,ChangeType> procChangeIntersection(IfElseAttributes other){
        if (other==null) return new HashMap<>();
        Set<Map.Entry<ProcessNode,ChangeType>> set = procChange.entrySet();
        set.retainAll(other.procChange.entrySet());
        HashMap<ProcessNode,ChangeType> mapFromSet = new HashMap<>();
        for(Map.Entry<ProcessNode,ChangeType> entry : set)
        {
            mapFromSet.put(entry.getKey(), entry.getValue());
        }
        return mapFromSet;
    }*/

    @Override
    public IReflexNode getAttributedNode() {
        return attributedNode;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.IfElse;
    }
}
