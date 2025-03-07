package su.nsk.iae.reflex.ProgramGraph.GraphRepr.RuleChecker;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributedPath;


import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;

import java.util.Iterator;
import java.util.Objects;

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
        }
        return true;
    }

}
