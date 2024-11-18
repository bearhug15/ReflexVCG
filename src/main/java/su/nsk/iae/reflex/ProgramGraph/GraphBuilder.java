package su.nsk.iae.reflex.ProgramGraph;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.*;
import su.nsk.iae.reflex.ProgramGraph.ProgramGraph;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.ConstantExpression;
import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.VariableExpression;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.IntType;
import su.nsk.iae.reflex.expression.types.TimeType;
import su.nsk.iae.reflex.formulas.Formula;
import su.nsk.iae.reflex.formulas.TrueFormula;
import su.nsk.iae.reflex.vcg.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GraphBuilder extends ReflexBaseVisitor<ProgramGraph> {

    String currentProcess;
    String currentState;

    ProgramMetaData metaData;
    VariableMapper mapper;
    IStatementCreator creator;
    ASTGraphProjection projection;

    public GraphBuilder(ProgramMetaData metaData, VariableMapper mapper, IStatementCreator creator){
        this.metaData = metaData;
        this.mapper = mapper;
        this.creator = creator;
        this.projection = new ASTGraphProjection();
    }
    public ProgramGraph buildProgramGraph(ReflexParser.ProgramContext context){
        return visitProgram(context);
    }

    public ASTGraphProjection getASTtoGraphProjection(){
        return projection;
    }

    @Override
    public ProgramGraph visitProgram(ReflexParser.ProgramContext ctx) {
        ArrayList<String> varInits = new ArrayList<>();
        for (Map.Entry<String, ExprType> variable: metaData.getInputVariablesNames().entrySet()){
            varInits.add(creator.createSetter(variable.getValue(),creator.createPlaceHolder(),variable.getKey(),variable.getKey()));
        }
        Map.Entry<ProgramNode,ProgramNode> nodes = ProgramNode.ProgramNodes(ctx,ctx.name.getText(),varInits);
        projection.put(ctx, nodes.getKey());

        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        ProgramGraph g = new ProgramGraph(null,null);
        for(ReflexParser.ProcessContext newCtx: ctx.processes){
            ProgramGraph buff= visitProcess(newCtx);
            g.extendGraph(buff);
        }
        graph.insertGraph(g);
        return graph;
    }

    @Override
    public ProgramGraph visitProcess(ReflexParser.ProcessContext ctx) {
        currentProcess = ctx.name.getText();
        Map.Entry<ProcessNode,ProcessNode> nodes = ProcessNode.ProcessNodes(ctx,ctx.name.getText());
        projection.put(ctx, nodes.getKey());
        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        for(ReflexParser.StateContext newCtx: ctx.state()){
            ProgramGraph g = visitState(newCtx);
            graph.insertGraph(g);
        }
        Map.Entry<StateNode,StateNode> stopNodes = StateNode.StateNodes(null,"stop",currentProcess);
        ProgramGraph stopGraph = new ProgramGraph(stopNodes.getKey(),stopNodes.getValue());
        stopGraph.connectStartEnd();
        graph.insertGraph(stopGraph);

        Map.Entry<StateNode,StateNode> errorNodes = StateNode.StateNodes(null,"error",currentProcess);
        ProgramGraph errorGraph = new ProgramGraph(errorNodes.getKey(),errorNodes.getValue());
        errorGraph.connectStartEnd();
        graph.insertGraph(errorGraph);
        return graph;
    }

    @Override
    public ProgramGraph visitState(ReflexParser.StateContext ctx) {
        currentState = ctx.name.getText();
        Map.Entry<StateNode,StateNode> nodes = StateNode.StateNodes(ctx,ctx.name.getText(),currentProcess);
        projection.put(ctx, nodes.getKey());
        ProgramGraph graph = new ProgramGraph(nodes.getKey(), nodes.getValue());
        ProgramGraph innerGraph = new ProgramGraph(null,null);
        for(ReflexParser.StatementContext newCtx: ctx.statementSeq().statements){
            ProgramGraph g = visit(newCtx);
            innerGraph.extendGraph(g);
        }
        if(ctx.timeoutFunction()!=null){
            ProgramGraph g = visitTimeoutFunction(ctx.timeoutFunction());
            innerGraph.extendGraph(g);
        }
        graph.insertGraph(innerGraph);
        return graph;
    }

    @Override
    public ProgramGraph visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx) {
        if (ctx.timeAmountOrRef()==null)
            throw new RuntimeException("Empty timeout reference");
        boolean isVariable = false;
        SymbolicExpression exp;
        Token time = ctx.timeAmountOrRef().time;
        Token intTime = ctx.timeAmountOrRef().intTime;
        Token var = ctx.timeAmountOrRef().ref;
        if(time!=null){
            exp = new ConstantExpression(time.getText(),new TimeType());
        } else if (intTime!=null){
            exp = new ConstantExpression(intTime.getText(),new IntType());
        } else if (var!=null){
            if(mapper.is_variable(currentProcess,var.getText())){
                ExprType t= mapper.variableType(currentProcess,var.getText());
                VariableExpression subExp = new VariableExpression(ctx.timeAmountOrRef().ref.getText(),t,true);
                subExp.actuate(creator.createPlaceHolder(),creator);
                exp = subExp;
                isVariable = true;
            } else if (mapper.is_const(var.getText())){
                ExprType t= mapper.constantType(var.getText());
                exp = new ConstantExpression(mapper.constantValue(var.getText()),t);
            } else{
                throw new RuntimeException("In timeout appeared unknown variable or constant: " + var.getText());
            }
        }else{
            throw new RuntimeException("Empty timeout reference");
        }
        String condition = exp.toString(creator);
        Map.Entry<TimeoutNode,TimeoutNode> nodes = TimeoutNode.TimeoutNodes(ctx,isVariable);
        projection.put(ctx, nodes.getKey());
        ProgramGraph graph = new ProgramGraph(nodes.getKey(), nodes.getValue());

        IReflexNode loseCondition = new ConditionNode(ctx, creator.createTimeoutLose(creator.createPlaceHolder(),condition,currentProcess),isVariable);
        ProgramGraph loseGraph = new ProgramGraph(loseCondition,loseCondition);
        graph.insertGraph(loseGraph);

        IReflexNode exceedCondition = new ConditionNode(ctx, creator.createTimeoutExceed(creator.createPlaceHolder(),condition,currentProcess),isVariable);
        ProgramGraph exceedGraph = new ProgramGraph(exceedCondition,exceedCondition);
        ProgramGraph exceed;
        if(ctx.statement()!=null){
            exceed = visit(ctx.statement());
        }else{
            exceed = new ProgramGraph(null,null);
        }
        exceedGraph.extendGraph(exceed);
        graph.insertGraph(exceedGraph);

        return graph;
    }

    @Override
    public ProgramGraph visitStatementSeq(ReflexParser.StatementSeqContext ctx) {
        ProgramGraph graph = new ProgramGraph(null,null);
        for(ReflexParser.StatementContext newCtx: ctx.statement()){
            graph.extendGraph(visit(newCtx));
        }
        return graph;
    }

    @Override
    public ProgramGraph visitCompoundStatement(ReflexParser.CompoundStatementContext ctx) {
        ProgramGraph graph = new ProgramGraph(null,null);
        for(ReflexParser.StatementContext newCtx: ctx.statement()){
            graph.extendGraph(visit(newCtx));
        }
        return graph;
    }

    @Override
    public ProgramGraph visitIfElseStat(ReflexParser.IfElseStatContext ctx) {
        if(ctx.expression()==null){
            throw new RuntimeException("If condition is null");
        }

        Map.Entry<IfElseNode,IfElseNode> nodes = IfElseNode.IfElseNodes(ctx);
        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        projection.put(ctx, nodes.getKey());

        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,creator.createPlaceHolder(),creator);
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        exp.actuate(newState, creator);
        domain = domain.trim();
        if (!(domain instanceof TrueFormula)){
            ConditionNode cnode = new ConditionNode(ctx,domain.toString(creator));
            graph.insertDanglingGraph(new ProgramGraph(cnode,cnode));
        }
        String condition = exp.toString(creator);


        ConditionNode trueCond = new ConditionNode(ctx,creator.createBinaryExpression(condition,creator.createTrue(), BinaryOp.Eq));
        ProgramGraph trueGraph = new ProgramGraph(trueCond,trueCond);
        if (!newState.equals(creator.createPlaceHolder())){
            ExpressionNode expNode = new ExpressionNode(ctx.expression(),newState);
            trueGraph.extendGraph(new ProgramGraph(expNode,expNode));
        }
        if(ctx.then!=null){
            trueGraph.extendGraph(visit(ctx.then));
        }
        graph.insertGraph(trueGraph);

        ConditionNode falseCond = new ConditionNode(ctx,creator.createBinaryExpression(condition,creator.createFalse(), BinaryOp.Eq));
        ProgramGraph falseGraph = new ProgramGraph(falseCond,falseCond);
        if (!newState.equals(creator.createPlaceHolder())){
            ExpressionNode expNode = new ExpressionNode(ctx.expression(),newState);
            falseGraph.extendGraph(new ProgramGraph(expNode,expNode));
        }
        if(ctx.else_!=null){
            falseGraph.extendGraph(visit(ctx.else_));
        }
        graph.insertGraph(falseGraph);

        return graph;
    }

    @Override
    public ProgramGraph visitSwitchStat(ReflexParser.SwitchStatContext ctx) {
        if(ctx.expression()==null){
            throw new RuntimeException("If condition is null");
        }

        Map.Entry<SwitchNode,SwitchNode> nodes = SwitchNode.SwitchNodes(ctx);
        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        projection.put(ctx, nodes.getKey());

        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,creator.createPlaceHolder(),creator);
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        exp.actuate(newState, creator);
        domain = domain.trim();
        if (!(domain instanceof TrueFormula)){
            ConditionNode cnode = new ConditionNode(ctx,domain.toString(creator));
            graph.insertDanglingGraph(new ProgramGraph(cnode,cnode));
        }
        String condition = exp.toString(creator);


        ArrayList<ProgramGraph> pathGraphs = new ArrayList<>();
        String passedConditions = null;
        for(ReflexParser.CaseStatContext newCtx:ctx.options){
            ReflexParser.ExpressionContext e0 = newCtx.expression();
            ExpressionVisitor vis0 = new ExpressionVisitor(mapper,currentProcess,creator.createPlaceHolder(),creator);
            ExprGenRes res0 = vis0.visitExpression(e0);
            String exp0 = res0.getExpr().toString(creator);

            if (passedConditions !=null){
                exp0 = creator.createBinaryExpression(passedConditions,creator.createBinaryExpression(condition,exp0,BinaryOp.Eq),BinaryOp.And);
                passedConditions = creator.createBinaryExpression(passedConditions,creator.createBinaryExpression(condition,exp0,BinaryOp.NotEq),BinaryOp.And);
            }else{
                exp0 = creator.createBinaryExpression(condition,exp0,BinaryOp.Eq);
                passedConditions = creator.createBinaryExpression(condition,exp0,BinaryOp.NotEq);
            }
            ConditionNode cnode = new ConditionNode(ctx,exp0);
            ProgramGraph cgraph = new ProgramGraph(cnode,cnode);

            if (!newState.equals(creator.createPlaceHolder())){
                ExpressionNode expNode = new ExpressionNode(ctx.expression(),newState);
                cgraph.extendGraph(new ProgramGraph(expNode,expNode));
            }

            ProgramGraph pathGraph = new ProgramGraph(null,null);
            for(ReflexParser.StatementContext sctx: newCtx.switchOptionStatSeq().statement()){
                pathGraph.extendGraph(visit(sctx));
            }

            for(ProgramGraph path: pathGraphs) {
                path.extendGraph(pathGraph);
            }

            boolean breakOp = newCtx.switchOptionStatSeq().break_!=null;
            if(breakOp){
                pathGraphs = new ArrayList<>();
                cgraph.extendGraph(pathGraph);
                graph.insertGraph(cgraph);
            }else{
                cgraph.extendGraph(pathGraph);
                pathGraphs.add(cgraph);
                graph.insertDanglingGraph(cgraph);
            }
        }
        if(ctx.defaultStat()!=null){
            if(ctx.defaultStat().switchOptionStatSeq()!=null){
                ConditionNode cnode = new ConditionNode(ctx,passedConditions);
                ProgramGraph cgraph = new ProgramGraph(cnode,cnode);
                ProgramGraph defGraph = visit(ctx.defaultStat().switchOptionStatSeq());
                cgraph.extendGraph(defGraph);
                for(ProgramGraph path: pathGraphs) {
                    path.extendGraph(defGraph);
                }
                graph.insertGraph(cgraph);
            }else{
                ConditionNode cnode = new ConditionNode(ctx,passedConditions);
                ProgramGraph cgraph = new ProgramGraph(cnode,cnode);
                graph.insertGraph(cgraph);
                if(!pathGraphs.isEmpty()){
                    graph.connectEnds(pathGraphs.get(pathGraphs.size()-1));
                }
            }
        } else{
            ConditionNode cnode = new ConditionNode(ctx,passedConditions);
            ProgramGraph cgraph = new ProgramGraph(cnode,cnode);
            graph.insertGraph(cgraph);
            if(!pathGraphs.isEmpty()){
                graph.connectEnds(pathGraphs.get(pathGraphs.size()-1));
            }
        }

        return graph;
    }

    @Override
    public ProgramGraph visitStartProcStat(ReflexParser.StartProcStatContext ctx) {
        String id = ctx.ID().toString();
        ProgramGraph graph;
        if(id.equals(currentProcess)){
            SetStateNode node = new SetStateNode(ctx,currentProcess, metaData.startState(currentProcess));
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Start,metaData);
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        ProgramGraph graph;
        if(ctx.ID()==null || ctx.ID().toString().equals(currentProcess)){
            SetStateNode node = new SetStateNode(ctx,currentProcess, "stop");
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            String id = ctx.ID().toString();
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Stop,metaData);
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        ProgramGraph graph;
        if(ctx.ID()==null || ctx.ID().toString().equals(currentProcess)){
            SetStateNode node = new SetStateNode(ctx,currentProcess, "error");
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            String id = ctx.ID().toString();
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Error,metaData);
            projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitRestartStat(ReflexParser.RestartStatContext ctx) {
        SetStateNode node = new SetStateNode(ctx,currentProcess, metaData.startState(currentProcess));
        projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitResetStat(ReflexParser.ResetStatContext ctx) {
        ResetNode node= new ResetNode(ctx,currentProcess);
        projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
        SetStateNode node;
        if (ctx.stateId!=null){
            String id = ctx.stateId.getText();
            node = new SetStateNode(ctx,currentProcess,id);
        }else{
            node = new SetStateNode(ctx,currentProcess,metaData.nextState(currentProcess,currentState));
        }
        projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitExprSt(ReflexParser.ExprStContext ctx) {
        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,creator.createPlaceHolder(),creator);
        ExprGenRes res = vis.visitExpression(e);
        String newState = res.getState();
        Formula domain = res.getDomain();
        domain = domain.trim();


        ProgramGraph graph;

        if (!(domain instanceof TrueFormula)){
            BlankNode bnode = new BlankNode();
            graph = new ProgramGraph(bnode,bnode);
            ConditionNode cnode = new ConditionNode(ctx.expression(),domain.toString(creator),true);
            projection.put(ctx, cnode);
            graph.insertDanglingGraph(new ProgramGraph(cnode,cnode));
            if (!newState.equals(creator.createPlaceHolder())){
                ExpressionNode expNode = new ExpressionNode(ctx.expression(),newState);
                graph.extendGraph(new ProgramGraph(expNode,expNode));
            }
            return graph;
        }else{
            if (!newState.equals(creator.createPlaceHolder())){
                ExpressionNode expNode = new ExpressionNode(ctx.expression(),newState);
                projection.put(ctx, expNode);
                graph = new ProgramGraph(expNode,expNode);
            }else{
                BlankNode bnode = new BlankNode();
                graph = new ProgramGraph(bnode,bnode);
            }
        }

        return graph;
    }

    @Override
    public ProgramGraph visitEmptySt(ReflexParser.EmptyStContext ctx) {
        return new ProgramGraph(null,null);
    }
}
