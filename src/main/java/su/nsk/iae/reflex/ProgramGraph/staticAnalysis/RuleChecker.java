package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.*;
import su.nsk.iae.reflex.antlr.ReflexParser;

import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.Iterator;
import java.util.Objects;

public class RuleChecker {
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

    public boolean checkRules(AttributedPath path, IAttributed attr){
        if(attr==null){
            return true;
        }
        switch (attr.getContextType()){
            case State: {
                return checkRulesStateAttributes(path,(StateAttributes) attr);
            }
            case Timeout:{
                if(((AttributedTimeout)attr).isVariable()){
                    return checkTimeout(path);
                }else{
                    return true;
                }
            }
            default: return true;
        }
    }

    private boolean checkRulesStateAttributes(AttributedPath path,StateAttributes attr){
        ProcessNode rootProcess = attr.getRootProcess();
        ProcessAttributes pattr = (ProcessAttributes) collector.getAttributes(rootProcess);
        if(!pattr.isReachE() && attr.getStateName().equals("error"))return false;
        if(!pattr.isReachS() && !pattr.isStartS() && attr.getStateName().equals("break")) return false;
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
        }
        return true;
    }

}
