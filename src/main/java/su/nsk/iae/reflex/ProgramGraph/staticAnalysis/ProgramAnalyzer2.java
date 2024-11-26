package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import su.nsk.iae.reflex.ProgramGraph.ASTGraphProjection;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.*;
import su.nsk.iae.reflex.ProgramGraph.ProgramGraph;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.*;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.ChangeType;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;

public class ProgramAnalyzer2 extends ReflexBaseVisitor<Void> {

    Stack<IAttributed> attributesContainers = new Stack<>();
    AttributeCollector collector = new AttributeCollector();

    ProgramMetaData metaData;
    ASTGraphProjection projection;
    ProgramGraph graph;

    ProcessNode currentProcess;
    StateNode currentState;

    public ProgramAnalyzer2(ProgramMetaData metaData, ASTGraphProjection projection, ProgramGraph graph){
        this.metaData = metaData;
        this.projection = projection;
        this.graph = graph;
    }

    public AttributeCollector generateAttributes(ReflexParser.ProgramContext ctx){
        initialAttributeArrangement(ctx);
        additionalAttributeArrangement(ctx);
        return collector;
    }

    Void initialAttributeArrangement(ReflexParser.ProgramContext ctx){
        for(ReflexParser.ProcessContext pctx: ctx.processes){
            IReflexNode proj = projection.get(pctx);
            ProcessAttributes attr = new ProcessAttributes((ProcessNode) proj);
            collector.addAttributes(proj,attr);
            for(ReflexParser.StateContext sctx:pctx.states){
                IReflexNode sproj= projection.get(sctx);
                StateAttributes sattr = new StateAttributes((ProcessNode) proj,(StateNode)sproj);
                collector.addAttributes(sproj,sattr);
            }
        }
        visitProgram(ctx);
        ReflexParser.ProcessContext firstProcess = ctx.processes.get(0);
        IReflexNode proj = projection.get(firstProcess);
        ProcessAttributes attr = (ProcessAttributes)collector.getAttributes(proj);
        attr.setStartS(false);
        return null;
    }

    Void additionalAttributeArrangement(ReflexParser.ProgramContext ctx){
        for (ReflexParser.ProcessContext pctx1: ctx.processes){
            for (ReflexParser.ProcessContext pctx2: ctx.processes){
                if(pctx1 ==pctx2)break;
                IReflexNode proj1 = projection.get(pctx1);
                IReflexNode proj2 = projection.get(pctx2);
                ProcessAttributes attr1 = (ProcessAttributes)collector.getAttributes(proj1);
                ProcessAttributes attr2 = (ProcessAttributes)collector.getAttributes(proj2);
                if(!attr2.isStartS()){
                    ChangeType changes = ((StateAttributes)collector.getAttributes(projection.get(pctx2.states.get(0)))).getProcChange().get(proj1);
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
        IReflexNode proj = projection.get(ctx);
        currentProcess = (ProcessNode) proj;
        ProcessAttributes attr = (ProcessAttributes)collector.getAttributes(proj);
        attributesContainers.push(attr);

        super.visitProcess(ctx);
        attributesContainers.pop();
        List<IReflexNode> neighs = graph.getOutgoingNeighbours(proj);

        Optional<IReflexNode> sres = neighs.stream().filter(s->((StateNode)s).getStateName().equals("stop")).findFirst();
        if(sres.isEmpty()){
            throw new RuntimeException("Stop state node doesnt exist for process "+((ProcessNode) proj).getProcessName());
        }else{
            StateAttributes sattr = new StateAttributes((ProcessNode)proj,(StateNode)sres.get());
            collector.addAttributes(sres.get(),sattr);
        }

        Optional<IReflexNode> eres = neighs.stream().filter(s->((StateNode)s).getStateName().equals("error")).findFirst();
        if(eres.isEmpty()){
            throw new RuntimeException("Error state node doesnt exist for process "+((ProcessNode) proj).getProcessName());
        }else{
            StateAttributes sattr = new StateAttributes((ProcessNode)proj,(StateNode)eres.get());
            collector.addAttributes(eres.get(),sattr);
        }

        return null;
    }

    @Override
    public Void visitState(ReflexParser.StateContext ctx) {
        IReflexNode proj1 = projection.get(ctx);
        currentState = (StateNode)proj1;
        ProcessNode proj2 = currentProcess;
        StateAttributes attr = (StateAttributes)collector.getAttributes(proj1);
        attributesContainers.push(attr);
        collector.addAttributes(proj1,attr);

        super.visitState(ctx);
        /*if(ctx.timeoutFunction()!=null){
            visitTimeoutFunction(ctx.timeoutFunction());
        }*/
        attr.liftAttributes();
        attributesContainers.pop();
        return null;
    }

    @Override
    public Void visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx){
        TimeoutNode proj = (TimeoutNode)projection.get(ctx);
        TimeoutCore attr = new TimeoutCore(proj,proj.isVariable());
        attributesContainers.add(attr);

        List<IReflexNode> neighbours = graph.getOutgoingNeighbours(proj);
        TimeoutBranch branchAttr0= new TimeoutBranch((ConditionNode) neighbours.get(0),true);
        attributesContainers.add(branchAttr0);
        super.visitTimeoutFunction(ctx);
        attributesContainers.pop();
        collector.addAttributes(neighbours.get(0),branchAttr0);

        TimeoutBranch branchAttr1= new TimeoutBranch((ConditionNode) neighbours.get(1),false);
        collector.addAttributes(neighbours.get(1),branchAttr1);

        attr.addAttributes(branchAttr0);
        attr.addAttributes(branchAttr1);
        attr.liftAttributes();

        attributesContainers.pop();
        collector.addAttributes(proj,attr);
        attributesContainers.peek().addAttributes(attr);
        return null;
    }

    @Override
    public Void visitIfElseStat(ReflexParser.IfElseStatContext ctx) {
        IReflexNode proj = projection.get(ctx);
        IfElseCore attr = new IfElseCore(currentProcess,currentState,(IfElseNode) proj);
        attributesContainers.push(attr);
        collector.addAttributes(proj,attr);

        IfElseBranch trueAttr = new IfElseBranch(currentProcess,currentState,(IfElseNode) proj);
        attributesContainers.push(trueAttr);
        super.visit(ctx.then);
        attributesContainers.pop();
        attr.addAttributes(trueAttr);
        trueAttr.liftAttributes();
        collector.addAttributes(graph.getOutgoingNeighbours(proj).get(0),trueAttr);

        IfElseBranch falseAttr;
        if(ctx.else_!=null){
            falseAttr = new IfElseBranch(currentProcess,currentState,(IfElseNode) proj);
            attributesContainers.push(falseAttr);
            super.visit(ctx.else_);
            attributesContainers.pop();

        }else{
            falseAttr = new IfElseBranch(currentProcess,currentState,(IfElseNode) proj);
        }
        attr.addAttributes(falseAttr);
        falseAttr.liftAttributes();
        collector.addAttributes(graph.getOutgoingNeighbours(proj).get(1),falseAttr);


        attributesContainers.pop();
        attr.liftAttributes();
        //addIfElseIntersection(attr);
        return null;
    }

    @Override
    public Void visitSwitchStat(ReflexParser.SwitchStatContext ctx) {
        IReflexNode proj = projection.get(ctx);
        SwitchCaseCore attr = new SwitchCaseCore(currentProcess,currentState,(SwitchNode) proj);
        attributesContainers.push(attr);
        collector.addAttributes(proj,attr);

        Vector<SwitchCaseBranch> beforeBrake = new Vector<>();

        for(ReflexParser.CaseStatContext cas: ctx.options){
            SwitchCaseBranch caseAttr = new SwitchCaseBranch(currentProcess,currentState,(SwitchNode) proj);
            attributesContainers.push(caseAttr);
            super.visitSwitchOptionStatSeq(cas.switchOptionStatSeq());
            attributesContainers.pop();

            for (SwitchCaseBranch attrs: beforeBrake){
                attrs.addAttributes(caseAttr);

            }
            beforeBrake.add(caseAttr);
            if(cas.switchOptionStatSeq().break_!=null){
                beforeBrake.forEach(b->{
                    b.liftAttributes();
                    attr.addAttributes(b);
                    List<IReflexNode> out = graph.getOutgoingNeighbours(proj);
                    collector.addAttributes(out.get(out.size()-1),b);
                });
                beforeBrake.clear();
            }
        }

        SwitchCaseBranch caseAttr;
        if(ctx.defaultOption!=null){
            caseAttr = new SwitchCaseBranch(currentProcess,currentState,(SwitchNode) proj);
            attributesContainers.push(caseAttr);
            super.visitDefaultStat(ctx.defaultStat());
            attributesContainers.pop();
            for (SwitchCaseBranch attrs: beforeBrake){
                attrs.addAttributes(caseAttr);
            }

        }else{
            caseAttr = new SwitchCaseBranch(currentProcess,currentState,(SwitchNode) proj);
        }
        beforeBrake.forEach(b->{
            b.liftAttributes();
            attr.addAttributes(b);
            List<IReflexNode> out = graph.getOutgoingNeighbours(proj);
            collector.addAttributes(out.get(out.size()-1),b);
        });
        beforeBrake.clear();
        attr.addAttributes(caseAttr);
        attributesContainers.pop();
        attr.liftAttributes();
        //addSwitchCaseIntersection(attr);
        return null;
    }

    @Override
    public Void visitStartProcStat(ReflexParser.StartProcStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        if(ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
            IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
            attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
            attr.setReset(true);
            attr.setStateChanging(true);
        }else{
            IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
            attr.addProcessChange((ProcessNode)proj,ChangeType.Start);
        }

        /*switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if(ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChanging(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode)proj,ChangeType.Start);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if(ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChange(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
                }
            } break;
            case State:{
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if(ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
                    attr.setReset(true);
                    attr.setStateChange(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Start);
                }
            } break;
        }*/
        return null;
    }

    @Override
    public Void visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
            IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
            attr.addProcessChange(currentProcess,ChangeType.Stop);
            attr.setReset(true);
            attr.setStateChanging(true);
            ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

        }else{
            IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
            attr.addProcessChange((ProcessNode) proj,ChangeType.Stop);
            ((ProcessAttributes)collector.getAttributes(proj)).setReachS(true);
        }
        /*switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    IReflexNode proj = projection.get(metaData.getProcessByName(currentProcess.getProcessName()));
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChanging(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachS(true);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachS(true);
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Stop);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachS(true);
                }
            }break;
        }*/
        return null;
    }

    @Override
    public Void visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
            attr.addProcessChange(currentProcess,ChangeType.Error);
            attr.setReset(true);
            attr.setStateChanging(true);
            ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachE(true);
        }else{
            IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
            attr.addProcessChange((ProcessNode) proj,ChangeType.Error);
            ((ProcessAttributes)collector.getAttributes(proj)).setReachE(true);
        }
        /*switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    attr.addProcessChange(currentProcess,ChangeType.Error);
                    attr.setReset(true);
                    attr.setStateChanging(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachE(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Error);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachE(true);
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                if (ctx.processId==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    attr.addProcessChange(currentProcess,ChangeType.Error);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachE(true);
                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Error);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachE(true);
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                if (ctx.processId.getText()==null || ctx.processId.getText().equals(currentProcess.getProcessName())){
                    attr.addProcessChange(currentProcess,ChangeType.Stop);
                    attr.setReset(true);
                    attr.setStateChange(true);
                    ((ProcessAttributes)collector.getAttributes(currentProcess)).setReachS(true);

                }else{
                    IReflexNode proj = projection.get(metaData.getProcessByName(ctx.processId.getText()));
                    attr.addProcessChange((ProcessNode) proj,ChangeType.Error);
                    ((ProcessAttributes)collector.getAttributes(proj)).setReachE(true);
                }
            }break;
        }*/
        return null;
    }

    @Override
    public Void visitRestartStat(ReflexParser.RestartStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        attr.addProcessChange(currentProcess,ChangeType.Start);
        attr.setReset(true);
        attr.setStateChanging(true);
        /*switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                attr.addProcessChange(currentProcess,ChangeType.Start);
                attr.setReset(true);
                attr.setStateChanging(true);
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
        }*/
        return null;
    }

    @Override
    public Void visitResetStat(ReflexParser.ResetStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        attr.setReset(true);
        /*switch (attributesContainers.peek().getContextType()){
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
        }*/
        return null;
    }

    @Override
    public Void visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
        IAttributed attr = attributesContainers.peek();
        attr.setReset(true);
        if (ctx.stateId!=null){
            ((StateAttributes)collector.getAttributes(currentState)).addReachFrom(ctx.stateId.getText());
        } else{
            ((StateAttributes)collector.getAttributes(currentState)).addReachFrom(metaData.nextState(currentProcess.getProcessName(),currentState.getStateName()));
        }
        /*switch (attributesContainers.peek().getContextType()){
            case IfElse:{
                IfElseAttributes attr = (IfElseAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.getProcessName(),currentState.getStateName()));
                }
            } break;
            case SwitchCase:{
                SwitchCaseAttributes attr = (SwitchCaseAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.getProcessName(),currentState.getStateName()));
                }
            } break;
            case State: {
                StateAttributes attr = (StateAttributes)attributesContainers.peek();
                attr.setReset(true);
                if (ctx.stateId!=null){
                    attr.setSetState(ctx.stateId.getText());
                } else{
                    attr.setSetState(metaData.nextState(currentProcess.getProcessName(),currentState.getStateName()));
                }
            }break;
        }*/
        return null;
    }

    /*Void addIfElseIntersection(IfElseCore core){
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
                HashMap<ProcessNode,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
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
                HashMap<ProcessNode,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
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
                HashMap<ProcessNode,ChangeType> procChange = core.getBranchAttributes().firstElement().getProcChange();
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
    }*/


}