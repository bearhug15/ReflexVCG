package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.IAttributed;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.ProcessAttributes;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.StateAttributes;

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
    public ProcessAttributes getProcessAttributeByName(String processName){
        return (ProcessAttributes)attributeMap.entrySet().stream().filter(entry->{
            if(entry.getKey() instanceof ProcessNode){
                return ((ProcessNode) entry.getKey()).getProcessName().equals(processName);
            }else{
                return false;
            }
        }).findFirst().get().getValue();
    }

    public StateAttributes getStateAttributeByName(String processName, String stateName){
        return (StateAttributes)attributeMap.entrySet().stream().filter(entry->{
            if(entry.getKey() instanceof StateNode){
                return ((StateNode) entry.getKey()).getProcessName().equals(processName) && ((StateNode) entry.getKey()).getStateName().equals(stateName);
            }else{
                return false;
            }
        }).findFirst().get().getValue();
    }
}
