package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
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
}
