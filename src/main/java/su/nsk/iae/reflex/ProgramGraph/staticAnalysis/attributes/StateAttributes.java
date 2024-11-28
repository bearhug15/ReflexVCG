package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;

import java.util.HashSet;
import java.util.Set;

public class StateAttributes extends LinearFragment {
    IReflexNode attributedNode;
    ProcessNode rootProcess;

    String stateName;
    String setState = null;
    Set<String> reachFrom = new HashSet<>();
    public StateAttributes(ProcessNode process, StateNode attributedNode){
        this.rootProcess = process;
        this.attributedNode = attributedNode;
        this.stateName = attributedNode.getStateName();
    }

    public ProcessNode getRootProcess() {
        return rootProcess;
    }


    public String getStateName(){
        return ((StateNode)attributedNode).getStateName();
    }

    @Override
    public IReflexNode getAttributedContext() {
        return attributedNode;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.State;
    }


    public Set<String> getReachFrom(){
        return  reachFrom;
    }

    public void setReachFrom(Set<String> reachFrom) {
        this.reachFrom = reachFrom;
    }

    public void addReachFrom(String state){
        this.reachFrom.add(state);
    }

}
