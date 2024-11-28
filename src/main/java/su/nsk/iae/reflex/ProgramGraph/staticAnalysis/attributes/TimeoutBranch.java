package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ConditionNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;

public class TimeoutBranch extends LinearFragment{
    ConditionNode node;
    boolean isVariable;
    boolean isExceed;

    public TimeoutBranch(ConditionNode node,boolean isExceed){
        this.node= node;
        isVariable =node.isTimeoutVariable();
        this.isExceed = isExceed;
    }

    @Override
    public IReflexNode getAttributedContext() {
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
