package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.SwitchNode;

public class SwitchCaseCore extends BranchingFragment {
    IReflexNode attributedNode;
    ProcessNode rootProcess;
    StateNode rootState;


    public SwitchCaseCore(ProcessNode process, StateNode state, SwitchNode attributedNode){
        rootProcess = process;
        rootState = state;
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
