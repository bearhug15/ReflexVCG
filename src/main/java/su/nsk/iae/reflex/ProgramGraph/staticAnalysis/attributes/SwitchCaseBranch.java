package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.StateNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.SwitchNode;

public class SwitchCaseBranch extends LinearFragment {
    IReflexNode attributedNode;
    ProcessNode rootProcess;
    StateNode rootState;
    String setState = null;

    public SwitchCaseBranch(ProcessNode process, StateNode state, SwitchNode attributedNode){
        rootProcess = process;
        rootState = state;
        this.attributedNode = attributedNode;
    }



    public void setAttributedContext(IReflexNode attributedNode) {
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


    @Override
    public IReflexNode getAttributedContext() {
        return attributedNode;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.SwitchCase;
    }
}
