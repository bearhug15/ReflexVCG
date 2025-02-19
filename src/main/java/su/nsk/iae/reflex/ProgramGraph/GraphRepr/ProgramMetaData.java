package su.nsk.iae.reflex.ProgramGraph.GraphRepr;



import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor.ExprGenRes1;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor.ExpressionVisitor1;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.TypeUtils;
//import su.nsk.iae.reflex.vcg.ValueParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProgramMetaData {

    List<Pair<String,List<String>>> processes;
    Map<String,Pair<String,String>> initializer;
    String clockValue;
    Map<String, ExprType> inputVariablesNames;

    HashMap<String, ReflexParser.ProcessContext> processNameMapper=new HashMap<>();
    IStatementCreator creator;

    public String getClockValue() {
        return clockValue;
    }

    public void setClockValue(String clockValue) {
        this.clockValue = clockValue;
    }
    public ProgramMetaData(ReflexParser.ProgramContext ctx, VariableMapper mapper, IStatementCreator creator){
        processes = new ArrayList<>();
        initializer = new HashMap<>();
        inputVariablesNames = new HashMap<>();
        this.creator = creator;

        for (ReflexParser.GlobalVariableContext variable: ctx.globalVars){
            if (variable.programVariable() == null) {
                ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                ReflexParser.PortMappingContext pmap = physVariable.mapping;
                String portId = pmap.portId.getText();
                String qualName = portId;
                if (pmap.bit!=null){
                    qualName += "_"+pmap.bit.getText();
                }
                for (ReflexParser.PortContext port:ctx.ports){
                    if (port.name.getText().equals(portId) && port.varType.getText().equals("input")){
                        this.addInputVariable(qualName,TypeUtils.defineType(physVariable.varType.getText()));
                    }
                }
            }
        }

        for (ReflexParser.ProcessContext process: ctx.processes){
            for(ReflexParser.ProcessVariableContext variable: process.variables){
                if (variable.programVariable() == null) {
                    ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                    ReflexParser.PortMappingContext pmap = physVariable.mapping;
                    String portId = pmap.portId.getText();
                    String qualName = portId;
                    if (pmap.bit!=null){
                        qualName += "_"+pmap.bit.getText();
                    }
                    for (ReflexParser.PortContext port:ctx.ports){
                        if (port.name.getText().equals(portId) && port.varType.getText().equals("input")){
                            this.addInputVariable(qualName,TypeUtils.defineType(physVariable.varType.getText()));
                        }
                    }
                }
            }
        }

        String replacedState="#";
        String state = replacedState;
        for (ReflexParser.GlobalVariableContext variable: ctx.globalVars){
            if(variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext programVariable = variable.programVariable();
                String variableName = programVariable.name.getText();
                String processName = ctx.processes.get(0).name.getText();
                if(programVariable.expression()!=null){
                    ExpressionVisitor1 vis = new ExpressionVisitor1(mapper,processName,state,creator);
                    ExprGenRes1 res = vis.visitExpression(programVariable.expression());
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = creator.Setter(ty,state,mapper.mapVariable(processName,variableName),res.getExpr().toString(creator));
                    //state = StringUtils.constructSetter();
                }else{
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = creator.Setter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                    //state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                }
            }
        }
        for(ReflexParser.ProcessContext procCtx: ctx.processes){
            List<String> stateNames= procCtx.states.stream()
                    .map(stateCtx->stateCtx.name.getText()).toList();
            this.addProcess(procCtx);
            this.addProcess(procCtx.name.getText(),stateNames);
            this.addInitializer(replacedState,procCtx.name.getText(),initializeProcess(procCtx,replacedState, mapper));
        }

        ReflexParser.ClockDefinitionContext clockCtx = ctx.clock;
        String value;
        if (clockCtx.intValue != null){
            value = ValueParser.parseInteger(clockCtx.intValue.getText());
        } else{
            value = ValueParser.parseTime(clockCtx.timeValue.getText());
        }
        this.setClockValue(value);

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
        if (process.b.size()<=idx+1){
            throw new RuntimeException("Trying to get next state for last state");
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

    public String firstState(String processName){
        Pair<String,List<String>> process = processes.stream()
                .filter(proc -> proc.a.equals(processName))
                .findFirst()
                .orElse(null);
        assert process != null;
        return process.b.get(0);
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

    public void addInputVariable(String name,ExprType type){
        inputVariablesNames.put(name,type);
    }
    public Map<String,ExprType> getInputVariablesNames(){
        return inputVariablesNames;
    }

    public boolean isFirstProcess(String processName){
        return processName.equals(processes.get(0).a);
    }

    public boolean isFirstState(String processName, String stateName){
        return processes.stream()
                .anyMatch(process ->
                        (processName.equals(process.a) && stateName.equals(process.b.get(0))));
    }

    public void addProcess(ReflexParser.ProcessContext process){
        processNameMapper.put(process.name.getText(),process);
    }

    public void addProcessByName(ReflexParser.ProcessContext process, String name){
        processNameMapper.put(name,process);
    }

    public ReflexParser.ProcessContext getProcessByName(String name){
        return processNameMapper.get(name);
    }

    private String initializeProcess(ReflexParser.ProcessContext ctx,String stateReplace, VariableMapper mapper){
        String processName = ctx.name.getText();
        String state = stateReplace;
        for(ReflexParser.ProcessVariableContext variable: ctx.variables){
            if (variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext programVariable = variable.programVariable();
                String variableName = programVariable.name.getText();
                if(programVariable.expression()!=null){
                    ExpressionVisitor1 vis = new ExpressionVisitor1(mapper,processName,state,creator);
                    ExprGenRes1 res = vis.visitExpression(programVariable.expression());
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = creator.Setter(ty,state,mapper.mapVariable(processName,variableName),res.getExpr().toString(creator));
                    //state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),res.getExpr().toString());
                }else{
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = creator.Setter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                    //state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                }
            }
        }
        return state;
    }

    public int getProcessId(String processName){
        return IntStream
                .range(0, processes.size())
                .filter(e->processes.get(e).a.equals(processName))
                .findFirst()
                .getAsInt();
    }
    public List<String> getProcessStates(String processName){
        return processes.stream().filter(p->p.a.equals(processName)).findFirst().get().b;
    }
}
