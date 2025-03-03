package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BranchingFragment implements IAttributed{

    Map<ProcessNode,ChangeType> procChange = new HashMap<>();
    Map<Map.Entry<ProcessNode,ChangeType>,Boolean> potProcChange = new HashMap<>();
    boolean reset = false;
    boolean stateChanging = false;

    ArrayList<IAttributed> attributes = new ArrayList<>();
    Map<String, String> procStatuses = new HashMap<>();

    @Override
    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }
    @Override
    public boolean isStateChanging() {
        return stateChanging;
    }

    public void setStateChanging(boolean stateChanging) {
        this.stateChanging = stateChanging;
    }
    @Override
    public Map<ProcessNode, ChangeType> getProcChange() {
        return procChange;
    }

    public void setProcChange(HashMap<ProcessNode, ChangeType> procChange) {
        this.procChange = procChange;
    }

    public void addProcessChange(ProcessNode ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }
    @Override
    public Map<Map.Entry<ProcessNode,ChangeType>,Boolean> getPotProcChange() {
        return potProcChange;
    }

    public void setPotProcChange(Map<Map.Entry<ProcessNode,ChangeType>,Boolean> potProcChange) {
        this.potProcChange = potProcChange;
    }

    public void addPotProcChange(ProcessNode node, ChangeType change) {
        potProcChange.put(new AbstractMap.SimpleImmutableEntry<>(node,change),Boolean.TRUE);
    }

    public Map<String,String> getProcStatuses(){
        return procStatuses;
    }
    @Override
    public ArrayList<IAttributed> getAttributes() {
        return attributes;
    }
    /*@Override
    public void setAttributes(ArrayList<IAttributed> attributes) {
        this.attributes = attributes;
    }*/
    @Override
    public void addAttributes(IAttributed attr){
        attributes.add(attr);
    }
    @Override
    public void liftAttributes(){
        for (IAttributed attr: attributes){
            attr.liftAttributes();
        }
        if(!attributes.isEmpty()){
            Map<ProcessNode,ChangeType> newProcChange = new HashMap<>(attributes.get(0).getProcChange());
            Map<Map.Entry<ProcessNode,ChangeType>,Boolean> newPotProcChange = new HashMap<>();
            for(IAttributed attr: attributes){
                if(attr.isReset()) this.reset = true;
                if(attr.isStateChanging()) this.stateChanging = true;
                for (ProcessNode proc: newProcChange.keySet()){
                    if(!attr.getProcChange().containsKey(proc) || !attr.getProcChange().get(proc).equals(attr.getProcChange().get(proc)))
                        newProcChange.remove(proc);
                }
                newPotProcChange.putAll(attr.getPotProcChange());
            }
            newPotProcChange = newPotProcChange.entrySet().stream()
                    .filter(entry->!newProcChange.containsKey(entry.getKey().getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
            for(ProcessNode proc: newProcChange.keySet()){
                newPotProcChange.put(new AbstractMap.SimpleImmutableEntry<>(proc,newProcChange.get(proc)),true);
            }
            this.procChange = newProcChange;
            this.potProcChange = newPotProcChange;
        }
    }

}
