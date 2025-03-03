package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProgramNode;

public class ProgramAttributes extends LinearFragment{
    ProgramNode node;

    public ProgramAttributes(ProgramNode node){
        this.node = node;
    }
    @Override
    public IReflexNode getAttributedNode() {
        return node;
    }

    @Override
    public AttributedNodeType getContextType() {
        return AttributedNodeType.Program;
    }
}
