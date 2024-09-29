package su.nsk.iae.reflex.staticAnalysis;

import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.staticAnalysis.attributes.*;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class RuleChecker {
    ProgramMetaData metaData;
    public RuleChecker(ProgramMetaData metaData){
        this.metaData = metaData;
    }

    public boolean checkTimeout(AttributedPath path){
        Iterator<IAttributed> iter =path.getPath().descendingIterator();
        while (iter.hasNext()){
            IAttributed attr = iter.next();
            switch (attr.getContextType()){
                case Process:{
                    return true;
                }
                case IfElse:{
                    IfElseAttributes newAttr = (IfElseAttributes) attr;
                    if(newAttr.isReset()){
                        return false;
                    }else{
                        break;
                    }
                }
                case SwitchCase:{
                    SwitchCaseAttributes newAttr = (SwitchCaseAttributes) attr;
                    if(newAttr.isReset()){
                        return false;
                    }else{
                        break;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkRules(AttributedPath path, IAttributed attr){
        switch (attr.getContextType()){
            case Process: {
                for (IAttributed pattr: path.getPath()) {
                    if (!checkRulesProcessAttributes(pattr,(ProcessAttributes) attr)) return false;
                }
                return true;
            }
            default: return true;
        }
    }

    private boolean checkRulesProcessAttributes(IAttributed attr1, ProcessAttributes attr2){
        if(!attr2.isReachE() && attr2.getState().equals("error"))return false;
        if(!attr2.isReachS() && !attr2.isStartS() && attr2.getState().equals("break")) return false;
        switch (attr1.getContextType()){
            case IfElse :{
                IfElseAttributes newAttr1 = (IfElseAttributes) attr1;
                ChangeType t = newAttr1.getProcChange().get(attr2.getAttributedContext());
                if (t==null) return true;
                return switch (t) {
                    case Start -> {
                        ReflexParser.ProcessContext p = (ReflexParser.ProcessContext) attr2.getAttributedContext();
                        yield metaData.isFirstState(p.name.getText(), attr2.getState());
                    }
                    case Stop -> attr2.getState().equals("break");
                    case Error -> attr2.getState().equals("error");
                };
            }
            case SwitchCase:{
                SwitchCaseAttributes newAttr1 = (SwitchCaseAttributes) attr1;
                ChangeType t = newAttr1.getProcChange().get(attr2.getAttributedContext());
                if (t==null) return true;
                return switch (t) {
                    case Start -> {
                        ReflexParser.ProcessContext p = (ReflexParser.ProcessContext) attr2.getAttributedContext();
                        yield metaData.isFirstState(p.name.getText(), attr2.getState());
                    }
                    case Stop -> attr2.getState().equals("break");
                    case Error -> attr2.getState().equals("error");
                };
            }
            case State:{
                StateAttributes newAttr1 = (StateAttributes) attr1;
                ChangeType t = newAttr1.getProcChange().get(attr2.getAttributedContext());
                if (t==null) return true;
                return switch (t) {
                    case Start -> {
                        ReflexParser.ProcessContext p = (ReflexParser.ProcessContext) attr2.getAttributedContext();
                        yield metaData.isFirstState(p.name.getText(), attr2.getState());
                    }
                    case Stop -> attr2.getState().equals("break");
                    case Error -> attr2.getState().equals("error");
                };
            }
            case Process:{
                return true;
            }

        }
        return true;
    }

}
