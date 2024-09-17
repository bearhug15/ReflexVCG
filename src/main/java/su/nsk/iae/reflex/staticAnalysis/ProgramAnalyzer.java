package su.nsk.iae.reflex.staticAnalysis;

import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.vcg.ProcessStateTraces;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.List;

public class ProgramAnalyzer extends ReflexBaseVisitor<Void> {

    ProcessStateTraces traces;
    ProgramMetaData metaData;
    String currentProcess;
    String currentState;
    public ProgramAnalyzer(ProcessStateTraces traces,ProgramMetaData data){
        this.traces = traces;
        metaData = data;
    }
    @Override
    public Void visitProgram(ReflexParser.ProgramContext ctx) {
        List<ReflexParser.ProcessContext> processes =  ctx.processes;
        ReflexParser.ProcessContext first = processes.get(0);
        if(first==null) {
            return null;
        }

        for (ReflexParser.ProcessContext pctx: processes){
            if (pctx!= first){
                traces.addReachable(pctx.name.getText(),"stop");
            }
            visitProcess(pctx);
        }
        return null;
    }

    @Override
    public Void visitProcess(ReflexParser.ProcessContext ctx) {

        for (ReflexParser.StateContext state: ctx.states){
            currentProcess = ctx.name.getText();
            traces.addReachable(currentProcess,state.name.getText());
            visitState(state);
        }
        return null;
    }

    @Override
    public Void visitState(ReflexParser.StateContext ctx) {
        currentState = ctx.name.getText();
        if (ctx.timeoutFunction()!=null){
            visitTimeoutFunction(ctx.timeoutFunction());
        }
        visitStatementSeq(ctx.stateFunction);
        return null;
    }

    @Override
    public Void visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx) {
        visitStatement(ctx.body);
        return null;
    }

    @Override
    public Void visitCompoundSt(ReflexParser.CompoundStContext ctx) {
        visitCompoundStatement(ctx.compoundStatement());
        return null;
    }

    @Override
    public Void visitStartProcessSt(ReflexParser.StartProcessStContext ctx) {
        return super.visitStartProcessSt(ctx);
    }

    @Override
    public Void visitStopProcessSt(ReflexParser.StopProcessStContext ctx) {
        ReflexParser.StopProcStatContext stopCtx = ctx.stopProcStat();
        if (stopCtx.processId!=null){
            traces.addReachable(stopCtx.processId.getText(),"stop");
        } else {
            traces.addReachable(currentProcess,"stop");
        }
        return null;
    }

    @Override
    public Void visitErrorProcessSt(ReflexParser.ErrorProcessStContext ctx) {
        ReflexParser.ErrorProcStatContext stopCtx = ctx.errorProcStat();
        if (stopCtx.processId!=null){
            traces.addReachable(stopCtx.processId.getText(),"error");
        } else {
            traces.addReachable(currentProcess,"error");
        }
        return null;
    }

    @Override
    public Void visitSetStateSt(ReflexParser.SetStateStContext ctx) {
        ReflexParser.SetStateStatContext stateCtx= ctx.setStateStat();
        if (stateCtx.stateId!=null){
          traces.addReachable(currentProcess,stateCtx.stateId.getText());
        } else {
            traces.addReachable(currentProcess,metaData.nextState(currentProcess,currentState));
        }
        return null;
    }

    @Override
    public Void visitStatementSeq(ReflexParser.StatementSeqContext ctx) {
        ctx.statements.forEach(this::visitStatement);
        return null;
    }

    @Override
    public Void visitCompoundStatement(ReflexParser.CompoundStatementContext ctx) {
        ctx.statements.forEach(this::visitStatement);
        return null;
    }

    public void visitStatement(ReflexParser.StatementContext ctx){
        if (ctx instanceof ReflexParser.EmptyStContext){
            return;
        }
        if (ctx instanceof ReflexParser.StartProcessStContext){
            visitStartProcessSt((ReflexParser.StartProcessStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.StopProcessStContext){
            visitStopProcessSt((ReflexParser.StopProcessStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.ErrorProcessStContext){
            visitErrorProcessSt((ReflexParser.ErrorProcessStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.RestartStContext){
            visitRestartSt((ReflexParser.RestartStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.ResetStContext){
            visitResetSt((ReflexParser.ResetStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.SetStateStContext){
            visitSetStateSt((ReflexParser.SetStateStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.IfElseStContext){
            visitIfElseSt((ReflexParser.IfElseStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.SwitchStContext){
            visitSwitchSt((ReflexParser.SwitchStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.ExprStContext){
            visitExprSt((ReflexParser.ExprStContext) ctx);
            return;
        }
        if (ctx instanceof ReflexParser.CompoundStContext){
            visitCompoundSt((ReflexParser.CompoundStContext) ctx);
            return;
        }
        throw new RuntimeException("Trying to visit unknown statement");
    }
}
