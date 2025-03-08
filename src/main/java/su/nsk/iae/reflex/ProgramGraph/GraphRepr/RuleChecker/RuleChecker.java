package su.nsk.iae.reflex.ProgramGraph.GraphRepr.RuleChecker;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributedPath;


import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;

import java.util.*;

public class RuleChecker implements IRuleChecker{
    ProgramMetaData metaData;
    AttributeCollector collector;
    public RuleChecker(ProgramMetaData metaData, AttributeCollector collector){
        this.metaData = metaData;
        this.collector = collector;
    }

    public boolean checkTimeout(AttributedPath path){
        Iterator<IAttributed> iter =path.descendingIterator();
        while (iter.hasNext()){
            IAttributed attr = iter.next();
            if (Objects.requireNonNull(attr.getContextType()) == AttributedNodeType.Process) {
                return true;
            } else {
                if (attr.isReset()) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public boolean checkRules(AttributedPath path, IAttributed attr){
        if(attr==null){
            return true;
        }
        switch (attr.getContextType()){
            case State: {
                return checkRulesStateAttributes(path,(StateAttributes) attr);
            }
            case TimeoutBranch:{
                if(((TimeoutBranch)attr).isVariable() || !((TimeoutBranch)attr).isExceed()){
                    return true;
                }else{
                    return checkTimeout(path);
                }
            }
            case ProcessStatus: return checkProcessStatusAttributes(path,(ProcessStatusAttributes)attr);
            default: return true;
        }
    }

    private boolean checkProcessStatusAttributes(AttributedPath path,ProcessStatusAttributes attr){

        HashMap<String,ProcessStatus> newProcessStatuses = new HashMap<>(attr.getProcStatuses());
        for (IAttributed a : path) {
            if (a instanceof ProcessStatusAttributes) {
                for (Map.Entry<String, ProcessStatus> entry : a.getProcStatuses().entrySet()) {
                    if (newProcessStatuses.get(entry.getKey()) == null) {
                        newProcessStatuses.put(entry.getKey(), entry.getValue());
                    }
                    int thisStatus = switch (newProcessStatuses.get(entry.getKey())) {
                        case Active -> 1;
                        case Inactive -> 3;
                        case Stop -> 5;
                        case NonStop -> 7;
                        case Error -> 11;
                        case NonError -> 13;
                    };
                    int anotherStatus = switch (entry.getValue()) {
                        case Active -> 1;
                        case Inactive -> 3;
                        case Stop -> 5;
                        case NonStop -> 7;
                        case Error -> 11;
                        case NonError -> 13;
                    };
                    switch (thisStatus * anotherStatus) {
                        case 1:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Active);
                            break;
                        case 3:
                            return false;
                        case 5:
                            return false;
                        case 7:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Active);
                            break;
                        case 11:
                            return false;
                        case 13:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Active);
                            break;

                        case 9:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Inactive);
                            break;
                        case 15:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Stop);
                            break;
                        case 21:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Error);
                            break;
                        case 33:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Error);
                            break;
                        case 39:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Stop);
                            break;

                        case 25:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Stop);
                            break;
                        case 35:
                            return false;
                        case 55:
                            return false;
                        case 65:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Stop);
                            break;

                        case 49:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.NonStop);
                            break;
                        case 77:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.NonError);
                            break;
                        case 91:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Active);
                            break;

                        case 121:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.Error);
                            break;
                        case 143:
                            return false;

                        case 169:
                            newProcessStatuses.put(entry.getKey(), ProcessStatus.NonError);
                            break;
                    }
                }
            }
        }
        //TODO
        return true;
    }

    private boolean checkRulesStateAttributes(AttributedPath path,StateAttributes attr){
        ProcessNode rootProcess = attr.getRootProcess();
        ProcessAttributes pattr = (ProcessAttributes) collector.getAttributes(rootProcess);
        if(!pattr.isReachE() && attr.getStateName().equals("error"))return false;
        if(!pattr.isReachS() && !pattr.isStartS() && attr.getStateName().equals("stop")) return false;

        //boolean res = true;

        Iterator<IAttributed> iter = path.descendingIterator();
        while(iter.hasNext()){
            IAttributed newAttr = iter.next();
            ChangeType ty = newAttr.getProcChange().get(rootProcess);
            if(ty!=null){
                switch(ty){
                    case Start -> {
                        return metaData.firstState(rootProcess.getProcessName()).equals(attr.getStateName());
                    }
                    case Stop -> {
                        return attr.getStateName().equals("stop");
                    }
                    case Error -> {
                        return attr.getStateName().equals("error");
                    }
                }
            }

            if(newAttr instanceof StateAttributes anotherAttr){
                ProcessAttributes anotherPAttr = (ProcessAttributes) collector.getAttributes(anotherAttr.getRootProcess());
                if (pattr.getGroup()==anotherPAttr.getGroup()){
                    String stateName = attr.getStateName();
                    String anotherStateName = anotherAttr.getStateName();
                    if(stateName.equals("stop")&&!anotherStateName.equals("stop") || !stateName.equals("stop")&&anotherStateName.equals("stop"))
                        return false;
                    if(stateName.equals("error")&&!anotherStateName.equals("error") || !stateName.equals("error")&&anotherStateName.equals("error"))
                        return false;
                    if(stateName.equals(metaData.firstState(rootProcess.getProcessName()))
                            && !anotherStateName.equals(metaData.firstState(anotherAttr.getRootProcess().getProcessName()))
                            && attr.getReachFrom().isEmpty()
                            && attr.isStateChanging())
                        return false;
                    if(!stateName.equals(metaData.firstState(rootProcess.getProcessName()))
                            && anotherStateName.equals(metaData.firstState(anotherAttr.getRootProcess().getProcessName()))
                            && anotherAttr.getReachFrom().isEmpty()
                            && anotherAttr.isStateChanging())
                        return false;
                }
            }
            if(newAttr instanceof ProcessStatusAttributes){
                ProcessStatus status =  newAttr.getProcStatuses().get(attr.getRootProcess().getProcessName());
                if(status==null)continue;
                switch (status){
                    case Active:if(attr.getStateName().equals("stop") || attr.getStateName().equals("error"))return false;break;
                    case Inactive:if(!attr.getStateName().equals("stop") && !attr.getStateName().equals("error"))return false;break;
                    case Stop:if(!attr.getStateName().equals("stop"))return false;break;
                    case NonStop:if(attr.getStateName().equals("stop"))return false;break;
                    case Error:if(!attr.getStateName().equals("error"))return false;break;
                    case NonError:if(attr.getStateName().equals("error"))return false;break;
                }

            }
        }
        return true;
    }

}
