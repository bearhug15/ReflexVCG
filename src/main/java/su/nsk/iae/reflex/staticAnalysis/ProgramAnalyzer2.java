package su.nsk.iae.reflex.staticAnalysis;

import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.staticAnalysis.attributes.*;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

public class ProgramAnalyzer2 extends ReflexBaseVisitor<Void> {

    Stack<IAttributed> attributesContainers = new Stack<>();
    AttributeCollector collector = new AttributeCollector();

    ProgramMetaData metaData;

    ReflexParser.ProcessContext currentProcess;
    ReflexParser.StateContext currentState;

    public ProgramAnalyzer2(ProgramMetaData metaData){
        this.metaData = metaData;
    }

    public AttributeCollector generateAttributes(ReflexParser.ProgramContext ctx){
        initialAttributeArrangement(ctx);
        additionalAttributeArrangement(ctx);
        return collector;
    }

    Void initialAttributeArrangement(ReflexParser.ProgramContext ctx){
        for(ReflexParser.ProcessContext pctx: ctx.processes){
            ProcessAttributes attr = new ProcessAttributes(pctx);
            collector.addAttributes(pctx,attr);
        }
        visitProgram(ctx);
        ReflexParser.ProcessContext firstProcess = ctx.processes.get(0);
        ProcessAttributes attr = (ProcessAttributes)collector.getAttributes(firstProcess);
        attr.setStartS(false);

        return null;
    }

    Void additionalAttributeArrangement(ReflexParser.ProgramContext ctx){
        for (ReflexParser.ProcessContext pctx1: ctx.processes){
            for (ReflexParser.ProcessContext pctx2: ctx.processes){
                if(pctx1 ==pctx2)break;
                ProcessAttributes attr1 = (ProcessAttributes)collector.getAttributes(pctx1);
                ProcessAttributes attr2 = (ProcessAttributes)collector.getAttributes(pctx2);
                if(!attr2.isStartS()){
                    ChangeType changes = ((StateAttributes)collector.getAttributes(pctx2.states.get(0))).getProcChange().get(pctx1);
                    if(changes!=null && changes.equals(ChangeType.Start)){
                        attr1.setStartS(false);
                        break;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Void visitProgram(ReflexParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        return null;
    }

    @Override
    public Void visitProcess(ReflexParser.ProcessContext ctx) {
        currentProcess = ctx;
        ProcessAttributes attr = (ProcessAttributes)collector.getAttributes(ctx);
        attributesContainers.push(attr);

        super.visitProcess(ctx);
        attributesContainers.pop();
        return null;
    }

    @Override
    public Void visitState(ReflexParser.StateContext ctx) {
        currentState = ctx;
        StateAttributes attr = new StateAttributes(currentProcess,ctx);
        attributesContainers.push(attr);
        collector.addAttributes(ctx,attr);

        super.visitState(ctx);
        attributesContainers.pop();
        return null;
    }


    @Override
    public Void visitIfElseStat(ReflexParser.IfElseStatContext ctx) {
        IfElseCore attr = new IfElseCore(currentProcess,currentState,ctx);
        attributesContainers.push(attr);
        collector.addAttributes(ctx,attr);

        IfElseAttributes trueAttr = new IfElseAttributes(currentProcess,currentState,ctx);
        attributesContainers.push(trueAttr);
        super.visit(ctx.then);
        attributesContainers.pop();
        attr.setTrueAttributes(trueAttr);

        if(ctx.else_!=null){
            IfElseAttributes falseAttr = new IfElseAttributes(currentProcess,currentState,ctx);
            attributesContainers.push(falseAttr);
            super.visit(ctx.else_);
            attributesContainers.pop();
            attr.setFalseAttributes(falseAttr);
        }

        attributesContainers.pop();
        addIfElseIntersection(attr);
        return null;
    }

    @Override
    public Void visitSwitchStat(ReflexParser.SwitchStatContext ctx) {
        SwitchCaseCore attr = new SwitchCaseCore(currentProcess,currentState,ctx);
        attributesContainers.push(attr);
        collector.addAttributes(ctx,attr);

        Vector<SwitchCaseAttributes> beforeBrake = new Vector<>();

        for(ReflexParser.CaseStatContext cas: ctx.options){
            SwitchCaseAttributes caseAttr = new SwitchCaseAttributes(currentProcess,currentState,ctx);
            attributesContainers.push(caseAttr);
            super.visitSwitchOptionStatSeq(cas.switchOptionStatSeq());
            attributesContainers.pop();

            for (SwitchCaseAttributes attrs: beforeBrake){
                attrs.addAnotherAttributes(caseAttr);
            }
            beforeBrake.add(caseAttr);
            if(cas.switchOptionStatSeq().break_!=null){
                beforeBrake.forEach(attr::addBranchAttributes);
                beforeBrake.clear();
            }
        }

        if(ctx.defaultOption!=null){
            SwitchCaseAttributes caseAttr = new SwitchCaseAttributes(currentProcess,currentState,ctx);
            attributesContainers.push(caseAttr);
            super.visitDefaultStat(ctx.defaultStat());
            attributesContainers.pop();
            for (SwitchCaseAttributes attrs: beforeBrake){
                attrs.addAnotherAttributes(caseAttr);
            }
            attr.setDefaultBranchAttributes(caseAttr);
        }
        beforeBrake.forEach(attr::addBranchAttributes);
        beforeBrake.clear();
        attributesContainers.pop();
        addSwitchCaseIntersection(attr);
        return null;
    }

    @Override
    public Void visitStartProcStat(ReflexParser.StartProcStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if(ctx.processId!=null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChange(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if(ctx.processId!=null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChange(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                }
            } break;
            case State:{
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if(ctx.processId!=null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChange(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Start);
                }
            } break;
        }
        return null;
    }

    @Override
    public Void visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachS(true);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachS(true);
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachS(true);
                }
            }break;
        }
        return null;
    }

    @Override
    public Void visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Error);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachE(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Error);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachE(true);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Error);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachE(true);
                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Error);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachE(true);
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if (ctx.processId.getText()==null || ctx.processId.getText().equals(currentProcess.name.getText())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    attr.addProcessChange(metaData.getProcessByName(ctx.processId.getText()),ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(metaData.getProcessByName(ctx.processId.getText()))).setReachS(true);
                }
            }break;
        }
        return null;
    }

    @Override
    public Void visitRestartStat(ReflexParser.RestartStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                attr.addProcessChange(currentProcess,ChangeType.Start);
                attr.setReset(true);
                attr.setStateChange(true);
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                attr.addProcessChange(currentProcess,ChangeType.Start);
                attr.setReset(true);
                attr.setStateChange(true);
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                attr.addProcessChange(currentProcess,ChangeType.Start);
                attr.setReset(true);
                attr.setStateChange(true);
            }break;
        }
        return null;
    }

    @Override
    public Void visitResetStat(ReflexParser.ResetStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                attr.setReset(true);
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                attr.setReset(true);
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                attr.setReset(true);
            }break;
        }
        return null;
    }

    @Override
    public Void visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
        switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.name.getText(),currentState.name.getText()));
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.name.getText(),currentState.name.getText()));
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.name.getText(),currentState.name.getText()));
                }
            }break;
        }
        return super.visitSetStateStat(ctx);
    }

    Void addIfElseIntersection(IfElseCore core){
        IAttributed attr = attributesContainers.peek();
        switch (attr.getContextType()){
            case IfElse:{
                IfElseAttributes newAttr =(IfElseAttributes)attr;
                newAttr.addReset(core.getTrueAttributes().resetIntersection(core.getFalseAttributes()));
                newAttr.addProcessChange(core.getTrueAttributes().procChangeIntersection(core.getFalseAttributes()));
                break;
            }
            case SwitchCase:{
                SwitchCaseAttributes newAttr =(SwitchCaseAttributes)attr;
                newAttr.addReset(core.getTrueAttributes().resetIntersection(core.getFalseAttributes()));
                newAttr.addProcessChange(core.getTrueAttributes().procChangeIntersection(core.getFalseAttributes()));
                break;
            }
            case State:{
                StateAttributes newAttr =(StateAttributes)attr;
                newAttr.addReset(core.getTrueAttributes().resetIntersection(core.getFalseAttributes()));
                newAttr.addProcessChange(core.getTrueAttributes().procChangeIntersection(core.getFalseAttributes()));
                break;
            }
        }
        return null;
    }
    Void addSwitchCaseIntersection(SwitchCaseCore core){
        IAttributed attr = attributesContainers.peek();
        switch (attr.getContextType()){
            case IfElse:{
                if (core.getDefaultBranchAttributes() == null){break;}

                IfElseAttributes newAttr =(IfElseAttributes)attr;
                boolean reset = true;
                HashMap<ReflexParser.ProcessContext,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
                for (SwitchCaseAttributes at: core.getBranchAttributes()){
                    if(!at.isReset()) reset = false;
                    procChange = at.procChangeIntersection(procChange);
                }
                if(!core.getDefaultBranchAttributes().isReset()) reset = false;
                procChange = core.getDefaultBranchAttributes().procChangeIntersection(procChange);
                newAttr.addReset(reset);
                newAttr.addProcessChange(procChange);
                break;
            }
            case SwitchCase:{
                if (core.getDefaultBranchAttributes() == null){break;}

                SwitchCaseAttributes newAttr =(SwitchCaseAttributes)attr;
                boolean reset = true;
                HashMap<ReflexParser.ProcessContext,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
                for (SwitchCaseAttributes at: core.getBranchAttributes()){
                    if(!at.isReset()) reset = false;
                    procChange = at.procChangeIntersection(procChange);
                }
                if (core.getDefaultBranchAttributes() != null){
                    if(!core.getDefaultBranchAttributes().isReset()) reset = false;
                    procChange = core.getDefaultBranchAttributes().procChangeIntersection(procChange);
                }
                newAttr.addReset(reset);
                newAttr.addProcessChange(procChange);
                break;
            }
            case State:{
                if (core.getDefaultBranchAttributes() == null){break;}

                StateAttributes newAttr =(StateAttributes)attr;
                boolean reset = true;
                HashMap<ReflexParser.ProcessContext,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
                for (SwitchCaseAttributes at: core.getBranchAttributes()){
                    if(!at.isReset()) reset = false;
                    procChange = at.procChangeIntersection(procChange);
                }
                if (core.getDefaultBranchAttributes() != null){
                    if(!core.getDefaultBranchAttributes().isReset()) reset = false;
                    procChange = core.getDefaultBranchAttributes().procChangeIntersection(procChange);
                }
                newAttr.addReset(reset);
                newAttr.addProcessChange(procChange);
                break;
            }
        }
        return null;
    }


}
