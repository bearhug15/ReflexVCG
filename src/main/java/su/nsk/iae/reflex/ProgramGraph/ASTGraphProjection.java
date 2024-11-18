package su.nsk.iae.reflex.ProgramGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;

import java.util.HashMap;

public class ASTGraphProjection {
    HashMap<ParserRuleContext, IReflexNode> projection=new HashMap<>();

    public void put(ParserRuleContext context,IReflexNode node){
        projection.put(context,node);
    }

    public IReflexNode get(ParserRuleContext context){
        IReflexNode node = projection.get(context);
        if(node==null){
            throw new RuntimeException("Projection tried to get node for unknown context:\n"+context.getText());
        }else{
            return node;
        }
    }
}
