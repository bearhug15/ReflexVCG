package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IfElseNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.StateNode;

public class IfElseCore extends BranchingFragment {
    IReflexNode attributedNode;
    ProcessNode rootProcess;
    StateNode rootState;
    String setState = null;

    public IfElseCore(ProcessNode process, StateNode state, IfElseNode attributedNode){
        this.attributedNode = attributedNode;
        rootProcess = process;
        rootState = state;
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
        return AttributedNodeType.IfElse;
    }
}
