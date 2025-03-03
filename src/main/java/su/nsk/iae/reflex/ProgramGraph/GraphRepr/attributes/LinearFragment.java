package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import  su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import java.util.*;
import java.util.stream.Collectors;

public abstract class LinearFragment implements IAttributed{

    Map<ProcessNode,ChangeType> procChange = new HashMap<>();
    Map<Map.Entry<ProcessNode,ChangeType>,Boolean> potProcChange = new HashMap<>();
    boolean reset = false;
    boolean stateChanging = false;
    ArrayList<IAttributed> attributes = new ArrayList<>();
    Map<String,String> procStatuses = new HashMap<>();

    @Override
    public boolean isReset() {
        return reset;
    }
    /*@Override
    public void setReset(boolean reset) {
        this.reset = reset;
    }*/
    @Override
    public boolean isStateChanging() {
        return stateChanging;
    }
    /*@Override
    public void setStateChanging(boolean stateChanging) {
        this.stateChanging = stateChanging;
    }*/
    @Override
    public Map<ProcessNode, ChangeType> getProcChange() {
        return procChange;
    }

    public void setProcChange(Map<ProcessNode, ChangeType> procChange) {
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

    public void addPotProcChange(Map<Map.Entry<ProcessNode,ChangeType>,Boolean> potProcChange) {
        this.potProcChange.putAll(potProcChange);
    }

    public void addProcessChange(Map<ProcessNode, ChangeType> other){
        this.procChange.putAll(other);
    }

    public void addReset(boolean reset){
        this.reset = this.reset || reset;
    }
    public void setReset(boolean reset){
        this.reset = reset;
    }

    public void addStateChanging(boolean stateChanging){
        this.stateChanging = this.stateChanging || stateChanging;
    }
    public void setStateChanging(boolean stateChanging){
        this.stateChanging = stateChanging;
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
    public void addAttributes(IAttributed attr) {
        this.attributes.add(attr);
    }

    @Override
    public void liftAttributes() {
        for (IAttributed attr: attributes){
            attr.liftAttributes();
        }
        if (!attributes.isEmpty()) {
            Map<ProcessNode,ChangeType> newProcChange = new HashMap<>();
            Map<Map.Entry<ProcessNode,ChangeType>,Boolean> newPotProcChange = new HashMap<>();
            for(IAttributed attr: attributes){
                if(attr.isReset()) this.reset = true;
                if(attr.isStateChanging()) this.stateChanging = true;
                Set<ProcessNode> aimedProcesses = newProcChange.keySet();
                aimedProcesses.retainAll(getDefinedProcesses(attr.getPotProcChange()));
                aimedProcesses.removeAll(attr.getProcChange().keySet());
                for(ProcessNode proc: aimedProcesses){
                    Boolean potStart = attr.getPotProcChange().getOrDefault(new AbstractMap.SimpleImmutableEntry<>(proc,ChangeType.Start),false);
                    Boolean potStop = attr.getPotProcChange().getOrDefault(new AbstractMap.SimpleImmutableEntry<>(proc,ChangeType.Stop),false);
                    Boolean potError = attr.getPotProcChange().getOrDefault(new AbstractMap.SimpleImmutableEntry<>(proc,ChangeType.Error),false);
                    if(newProcChange.getOrDefault(proc,ChangeType.Stop).equals(ChangeType.Start) && (potStop || potError))
                        newProcChange.remove(proc);
                    if(newProcChange.getOrDefault(proc,ChangeType.Error).equals(ChangeType.Stop) && (potStart || potError))
                        newProcChange.remove(proc);
                    if(newProcChange.getOrDefault(proc,ChangeType.Start).equals(ChangeType.Error) && (potStop || potStart))
                        newProcChange.remove(proc);
                }
                newProcChange.putAll(attr.getProcChange());
                newPotProcChange = newPotProcChange.entrySet().stream()
                        .filter(entry->!attr.getProcChange().containsKey(entry.getKey().getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, HashMap::new));
                newPotProcChange.putAll(attr.getPotProcChange());
            }
            this.procChange = newProcChange;
            this.potProcChange = newPotProcChange;
        }
    }

    Set<ProcessNode> getDefinedProcesses(Map<Map.Entry<ProcessNode,ChangeType>,Boolean> set){
        return set.keySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
