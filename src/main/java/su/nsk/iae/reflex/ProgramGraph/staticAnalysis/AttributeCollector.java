package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.IAttributed;

import java.util.HashMap;

public class AttributeCollector {
    HashMap<IReflexNode, IAttributed> attributeMap = new HashMap<>();

    public void addAttributes(IReflexNode node,IAttributed attributed){
        attributeMap.put(node,attributed);
    }

    public IAttributed getAttributes(IReflexNode node){
        return attributeMap.get(node);
    }
    public HashMap<IReflexNode, IAttributed> getAttributeMap(){
        return attributeMap;
    }
}
