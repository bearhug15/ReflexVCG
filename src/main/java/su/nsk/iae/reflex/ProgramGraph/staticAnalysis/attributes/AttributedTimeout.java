package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.TimeoutNode;

public class AttributedTimeout extends LinearFragment{
    TimeoutNode node;

    public boolean isVariable() {
        return isVariable;
    }

    public void setVariable(boolean variable) {
        isVariable = variable;
    }

    boolean isVariable;

    public AttributedTimeout(TimeoutNode node,boolean isVariable){
        this.node = node;
        this.isVariable = isVariable;
    }

    @Override
    public IReflexNode getAttributedContext() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.Timeout;
    }
}
