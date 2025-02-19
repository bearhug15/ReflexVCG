package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;

import java.util.*;

public abstract class LinearFragment implements IAttributed{

    HashMap<ProcessNode,ChangeType> procChange = new HashMap<>();
    HashMap<ProcessNode,PotChange> potProcChange = new HashMap<>();
    boolean reset = false;
    boolean stateChanging = false;
    ArrayList<IAttributed> attributes = new ArrayList<>();
    HashMap<String,String> procStatuses = new HashMap<>();

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
    public HashMap<ProcessNode, ChangeType> getProcChange() {
        return procChange;
    }
    /*@Override
    public void setProcChange(HashMap<ProcessNode, ChangeType> procChange) {
        this.procChange = procChange;
    }*/
    /*@Override
    public void addProcessChange(ProcessNode ctx, ChangeType type){
        this.procChange.put(ctx,type);
    }*/
    @Override
    public HashMap<ProcessNode, PotChange> getPotProcChange() {
        return potProcChange;
    }
    /*@Override
    public void setPotProcChange(HashMap<ProcessNode, PotChange> potProcChange) {
        this.potProcChange = potProcChange;
    }*/
    /*@Override
    public void addPotProcChange(ProcessNode ctx, PotChange change) {
        PotChange res = potProcChange.get(ctx);
        if(res!=null){
            potProcChange.put(ctx,res.add(change));
        }else{
            potProcChange.put(ctx,change);
        }
    }*/

    public void addPotProcChange(HashMap<ProcessNode, PotChange> potProcChange) {
        for (Map.Entry<ProcessNode, PotChange> e: potProcChange.entrySet()){
            PotChange res = this.potProcChange.get(e.getKey());
            if(res!=null){
                this.potProcChange.put(e.getKey(),res.add(e.getValue()));
            }else{
                this.potProcChange.put(e.getKey(),e.getValue());
            }
        }
    }

    public void addProcessChange(HashMap<ProcessNode, ChangeType> other){
        this.procChange.putAll(other);
    }

    public void addReset(boolean reset){
        this.reset = this.reset || reset;
    }

    public void addStateChanging(boolean stateChanging){
        this.stateChanging = this.stateChanging || stateChanging;
    }

    public HashMap<String,String> getProcStatuses(){
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
        if(attributes.isEmpty()){
            return;
        } else if (attributes.size()==1) {
            IAttributed attr = attributes.get(0);
            this.reset = this.reset || attr.isReset();
            this.stateChanging = this.stateChanging || attr.isStateChanging();
            this.procChange.putAll(attr.getProcChange());
            for (Map.Entry<ProcessNode, PotChange> e: attr.getPotProcChange().entrySet()){
                PotChange res = this.potProcChange.get(e.getKey());
                if(res!=null){
                    this.potProcChange.put(e.getKey(),res.add(e.getValue()));
                }else{
                    this.potProcChange.put(e.getKey(),e.getValue());
                }
            }
        }else{
            HashMap<ProcessNode,ChangeType> newProcChange = this.procChange;
            HashMap<ProcessNode,PotChange> newPotProcChange = this.potProcChange;
            boolean newReset = this.reset;
            boolean newStateChanging = this.stateChanging;
            for(IAttributed attr: attributes){
                newReset = newReset || attr.isReset();
                newStateChanging = newStateChanging || attr.isStateChanging();

                Set<ProcessNode> buff = new HashSet<>(newProcChange.keySet());
                buff.retainAll(attr.getPotProcChange().keySet());
                for (ProcessNode node: buff){
                    PotChange res = newPotProcChange.get(node);
                    if(newProcChange.get(node).equals(ChangeType.Start)&&!(!res.stop && !res.error))
                        newProcChange.remove(node);
                    if(newProcChange.get(node).equals(ChangeType.Stop)&&!(!res.start && !res.error))
                        newProcChange.remove(node);
                    if(newProcChange.get(node).equals(ChangeType.Error)&&!(!res.start && !res.stop))
                        newProcChange.remove(node);
                }
                newProcChange.putAll(attr.getProcChange());
                for(Map.Entry<ProcessNode,PotChange> e : attr.getPotProcChange().entrySet()){
                    PotChange res = newPotProcChange.get(e.getKey());
                    if(res==null){
                        newPotProcChange.put(e.getKey(),e.getValue());
                    }else{
                        newPotProcChange.put(e.getKey(),e.getValue().add(res));
                    }
                }
            }
            this.reset = newReset;
            this.stateChanging = newStateChanging;
            this.procChange = newProcChange;
            this.potProcChange = newPotProcChange;
        }
    }
}
