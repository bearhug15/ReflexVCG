package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProgramNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.IAttributed;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProcessAttributes;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProgramAttributes;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.StateAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class AttributeCollector {
    // LinkedHashMap, not HashMap: the graph nodes used as keys do not override hashCode,
    // so plain hash order is identity-hash order and varies from one JVM run to the next.
    // ProgramAnalyzer2.grouping() iterates this map to order processes, and that order
    // affects which VCs static analysis prunes - see ProgramAnalyzer2.setsDiv.
    HashMap<IReflexNode, IAttributed> attributeMap = new LinkedHashMap<>();
    ProgramNode programNode;

    public void addAttributes(ProgramNode node,IAttributed attributed){
        this.programNode = node;
        attributeMap.put(node,attributed);
    }
    public void addAttributes(IReflexNode node,IAttributed attributed){
        attributeMap.put(node,attributed);
    }

    public ProgramAttributes getProgramAttributes(){
        assert programNode!=null;
        return (ProgramAttributes) attributeMap.get(programNode);
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
