package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;

public class UniversalAttributes extends LinearFragment{
    IReflexNode node;
    public UniversalAttributes(IReflexNode node){
        this.node = node;
    }

    @Override
    public IReflexNode getAttributedContext() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.Statement;
    }
}
