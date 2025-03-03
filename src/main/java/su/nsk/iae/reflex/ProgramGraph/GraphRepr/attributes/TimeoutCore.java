package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.TimeoutNode;

public class TimeoutCore extends BranchingFragment{
    TimeoutNode node;

    public boolean isVariable() {
        return isVariable;
    }

    public void setVariable(boolean variable) {
        isVariable = variable;
    }

    boolean isVariable;

    public TimeoutCore(TimeoutNode node, boolean isVariable){
        this.node = node;
        this.isVariable = isVariable;
    }

    @Override
    public IReflexNode getAttributedNode() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.TimeoutCore;
    }
}
