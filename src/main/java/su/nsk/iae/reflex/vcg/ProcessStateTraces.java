package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.misc.Pair;

import java.util.HashSet;
import java.util.Set;

public class ProcessStateTraces {
    Set<Pair<String,String>> reachableStates;

    public ProcessStateTraces(){
        reachableStates = new HashSet<>();
    }

    public void addReachable(String process, String state){
        reachableStates.add(new Pair<>(process,state));
    }

    public void removeReachable(String process, String state){
        reachableStates.remove(new Pair<>(process,state));
    }

    public boolean isReachable(String process, String state){
        return reachableStates.contains(new Pair<>(process,state));
    }
}
