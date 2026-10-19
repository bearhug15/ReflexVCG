package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ConditionNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;

public class TimeoutBranch extends LinearFragment{
    ConditionNode node;
    boolean isVariable;
    boolean isExceed;
    // The duration is a known constant greater than zero; a zero timeout elapses at local
    // time zero, so rule 4.2 applies only to positive ones.
    boolean isPositive = false;

    public TimeoutBranch(ConditionNode node,boolean isExceed){
        this.node= node;
        isVariable =node.isTimeoutVariable();
        this.isExceed = isExceed;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    @Override
    public IReflexNode getAttributedNode() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.TimeoutBranch;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public boolean isExceed() {
        return isExceed;
    }
}
