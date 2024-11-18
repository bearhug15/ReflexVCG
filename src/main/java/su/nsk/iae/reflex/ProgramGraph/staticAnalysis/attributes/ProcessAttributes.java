package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProcessNode;

public class ProcessAttributes extends BranchingFragment {

    IReflexNode attributedNode;

    boolean reachE = false;
    boolean reachS = false;
    boolean startS = true;

    public ProcessAttributes(ProcessNode attributedNode){
        this.attributedNode = attributedNode;
    }

    @Override
    public IReflexNode getAttributedContext() {
        return attributedNode  ;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.Process;
    }

    public boolean isReachE() {
        return reachE;
    }

    public void setReachE(boolean reachE) {
        this.reachE = reachE;
    }

    public boolean isReachS() {
        return reachS;
    }

    public void setReachS(boolean reachS) {
        this.reachS = reachS;
    }

    public boolean isStartS() {
        return startS;
    }

    public void setStartS(boolean startS) {
        this.startS = startS;
    }
}
