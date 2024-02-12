package su.nsk.iae.reflex.vcg;



import org.antlr.v4.runtime.misc.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProgramMetaData {

    List<Pair<String,List<String>>> processes;
    Map<String,Pair<String,String>> initializer;
    String clockValue;
    public String getClockValue() {
        return clockValue;
    }

    public void setClockValue(String clockValue) {
        this.clockValue = clockValue;
    }


    public ProgramMetaData(){
        processes = new ArrayList<>();
        initializer = new HashMap<>();
    }

    public String nextState(String processName, String stateName){
        Pair<String,List<String>> process = processes.stream()
                .filter(proc -> proc.a.equals(processName))
                .findFirst()
                .orElse(null);
        if (process==null){
            throw new RuntimeException("Not found process in metadata");
        }
        int idx = IntStream.range(0,process.b.size())
                .filter(i -> process.b.get(i).equals(stateName))
                .toArray()[0];
        if (process.b.size()<=idx){
            throw new RuntimeException("Trying to gen next state for last state");
        }
        return process.b.get(idx+1);
    }

    public void addProcess(String processName, List<String> stateNames){
        processes.add(new Pair<>(processName,stateNames));
    }
    public void addInitializer(String state,String processName, String initializerString){
        initializer.put(processName,new Pair<>(state,initializerString));
    }
    public String initializeProcess(String state,String processName){
        Pair<String,String>initialize= initializer.get(processName);
        if (initialize ==null){
            throw new RuntimeException("Trying initialize non existing process");
        }
        String res = initialize.b.replaceAll(initialize.a,state);
        return res;
    }
    public String startState(String processName){
        Pair<String,List<String>> process = processes.stream()
                .filter(proc -> proc.a.equals(processName))
                .findFirst()
                .orElse(null);
        if (process==null){
            throw new RuntimeException("Not found process in metadata");
        }
        return process.b.get(0);
    }
    public String startProcess(){
        return processes.get(0).a;}
    public List<String> processNames(){
        return processes.stream().map(pair->pair.a).collect(Collectors.toList());
    }
    public String stateByIdx(String processName, int idx){
        Pair<String,List<String>> process = processes.stream()
                .filter(proc -> proc.a.equals(processName))
                .findFirst()
                .orElse(null);
        if (process==null){
            throw new RuntimeException("Not found process in metadata");
        }
        if (process.b.size()<=idx){
            throw new RuntimeException("Not fount state with provided index");
        }
        return process.b.get(idx);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProgramMetaData other))return false;
        if (!this.clockValue.equals(other.clockValue)){
            return false;
        }
        if (!this.initializer.equals(other.initializer)){
            return false;
        }
        if (!(this.processes.containsAll(other.processes) && other.processes.containsAll(this.processes))){
            return false;
        }
        return true;
    }
}
