package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;

public class AttributeCollector {
    HashMap<ParserRuleContext,IAttributed> attributeMap = new HashMap<>();;

    public void addAttributes(ParserRuleContext context,IAttributed attributed){
        attributeMap.put(context,attributed);
    }

    public IAttributed getAttributes(ParserRuleContext context){
        return attributeMap.get(context);
    }
}
