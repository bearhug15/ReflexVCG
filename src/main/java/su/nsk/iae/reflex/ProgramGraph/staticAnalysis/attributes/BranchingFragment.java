package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BranchingFragment implements IAttributed{

    HashMap<ProcessNode,ChangeType> procChange = new HashMap<>();
    HashMap<ProcessNode,PotChange> potProcChange = new HashMap<>();
    boolean reset = false;
    boolean stateChanging = false;

    ArrayList<IAttributed> attributes = new ArrayList<>();
    HashMap<String, String> procStatuses = new HashMap<>();

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
    public void addAttributes(IAttributed attr){
        attributes.add(attr);
    }
    @Override
    public void liftAttributes(){
        Set<Map.Entry<ProcessNode,ChangeType>> procChangeSet = attributes.get(0).getProcChange().entrySet();
        HashMap<ProcessNode,PotChange> newPotProcChange = new HashMap<>();
        boolean newReset = true;
        boolean newStateChanging = true;

        for(IAttributed attr: attributes){
            newReset = newReset && attr.isReset();
            newStateChanging = newStateChanging && attr.isStateChanging();
            procChangeSet.retainAll(attr.getProcChange().entrySet());
            for (Map.Entry<ProcessNode, PotChange> e: attr.getPotProcChange().entrySet()){
                PotChange res = newPotProcChange.get(e.getKey());
                if(res!=null){
                    newPotProcChange.put(e.getKey(),res.add(e.getValue()));
                }else{
                    newPotProcChange.put(e.getKey(),e.getValue());
                }
            }
        }
        this.reset = newReset;
        this.stateChanging = newStateChanging;
        this.procChange = procChangeSet.stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (prev, next)->next,
                HashMap::new));
        this.potProcChange = newPotProcChange;
    }

}
