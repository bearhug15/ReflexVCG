package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.Token;

import org.apache.commons.lang3.tuple.ImmutablePair;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.*;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.IntType;
import su.nsk.iae.reflex.expression.types.TimeType;
import su.nsk.iae.reflex.formulas.Formula;
import su.nsk.iae.reflex.formulas.TrueFormula;

import java.util.*;
import java.util.stream.Collectors;


public class GraphBuilder extends ReflexBaseVisitor<ProgramGraph> {

    String currentProcess;
    String currentState;

    ProgramMetaData metaData;
    VariableMapper mapper;
    IStatementCreator creator;
    ASTGraphProjection projection;
    boolean isSimpleExprVisitor;
    ExpressionVisitor visitor;

    public GraphBuilder(ProgramMetaData metaData, VariableMapper mapper, IStatementCreator creator, boolean isSimpleExprVisitor){
        this.metaData = metaData;
        this.mapper = mapper;
        this.creator = creator;
        this.projection = new ASTGraphProjection();
        this.isSimpleExprVisitor = isSimpleExprVisitor;
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
            varInits.add(creator.Setter(variable.getValue(),creator.PlaceHolder(),variable.getKey(),variable.getKey()));
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
                subExp.actuate(creator.PlaceHolder(),creator);
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

        IReflexNode exceedCondition = new ConditionNode(ctx, creator.TimeoutExceed(creator.PlaceHolder(),condition,currentProcess),isVariable);
        ProgramGraph exceedGraph = new ProgramGraph(exceedCondition,exceedCondition);
        ProgramGraph exceed;
        if(ctx.statement()!=null){
            exceed = visit(ctx.statement());
        }else{
            exceed = new ProgramGraph(null,null);
        }
        exceedGraph.extendGraph(exceed);
        graph.insertGraph(exceedGraph);

        IReflexNode loseCondition = new ConditionNode(ctx, creator.TimeoutLose(creator.PlaceHolder(),condition,currentProcess),isVariable);
        ProgramGraph loseGraph = new ProgramGraph(loseCondition,loseCondition);
        graph.insertGraph(loseGraph);



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

    private ProgramGraph visitIfElseResult(ReflexParser.IfElseStatContext ctx, ExprRes res,String resState, boolean trueRes){
        Optional<String> condition = res.getFullCondition(currentProcess);
        res.getExpr().actuate(res.getState(), creator);
        String expr;
        ConditionNode node;
        if(trueRes){
            if(!res.getExpr().toString(creator).equals(creator.True())) {
                expr = res.getExpr().toString(creator);
                node = condition
                        .map(s -> new ConditionNode(ctx, creator.Conjunction(List.of(s, expr))))
                        .orElseGet(() -> new ConditionNode(ctx, creator.Conjunction(List.of(expr))));
            }else{
                node = condition
                        .map(s -> new ConditionNode(ctx, creator.Conjunction(List.of(s))))
                        .orElseGet(() -> new ConditionNode(ctx, creator.True()));
            }
        }else{
            if(!res.getExpr().toString(creator).equals(creator.False())){
            expr = (new UnaryExpression(UnaryOp.Neg,res.getExpr(),res.getExpr().exprType())).toString(creator);
            node = condition
                    .map(s -> new ConditionNode(ctx, creator.Conjunction(List.of(s, expr))))
                    .orElseGet(() -> new ConditionNode(ctx, creator.Conjunction(List.of(expr))));
            }else{
                node = condition
                        .map(s -> new ConditionNode(ctx, creator.Conjunction(List.of(s))))
                        .orElseGet(() -> new ConditionNode(ctx, creator.True()));
            }
        }
        ProgramGraph g = new ProgramGraph(node, node);
        if (!resState.equals(creator.PlaceHolder())) {
            ExpressionNode enode = new ExpressionNode(ctx.expression(), resState);
            g.extendGraph(enode);
        }
        res.getDomain().ifPresent(domain -> g.insertDanglingNode(new ConditionNode(ctx, domain)));
        return g;
    }
    @Override
    public ProgramGraph visitIfElseStat(ReflexParser.IfElseStatContext ctx) {
        //TODO Добавить в condition атрибут
        if(ctx.expression()==null){
            throw new RuntimeException("If condition is null");
        }

        Map.Entry<IfElseNode,IfElseNode> nodes = IfElseNode.IfElseNodes(ctx);
        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        projection.put(ctx, nodes.getKey());

        ReflexParser.ExpressionContext e=ctx.expression();

        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess,creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess,creator.PlaceHolder(),creator);
        }
        List<ExprRes> results = vis.parseExpression(e);

        //Grouped by resulting state change
        Map<String,List<ExprRes>> trueCondGroups = results.stream()
                .filter(res -> res.getBooleanValue().orElse(true))
                .collect(Collectors.groupingBy(ExprRes::getState));
        Map<String,List<ExprRes>> falseCondGroups = results.stream()
                .filter(res -> !res.getBooleanValue().orElse(false))
                .collect(Collectors.groupingBy(ExprRes::getState));

        ProgramGraph trueGraph = new ProgramGraph(new BlankNode(),new BlankNode());
        trueCondGroups.forEach((resState,resList)->{
            ProgramGraph trueSubGraph;
            if(resList.size()>1) {
                trueSubGraph = new ProgramGraph(new BlankNode(),new BlankNode());
                resList.forEach(res -> {
                    trueSubGraph.insertGraph(visitIfElseResult(ctx,res,resState,true));
                });
            }else{
                ExprRes res = resList.get(0);
                trueSubGraph = visitIfElseResult(ctx,res,resState,true);
            }
            trueGraph.insertGraph(trueSubGraph);
        });
        if(ctx.then!=null){
            trueGraph.extendGraph(visit(ctx.then));
            projection.put(ctx.then, trueGraph.startNode);
        }
        graph.insertGraph(trueGraph);

        ProgramGraph falseGraph = new ProgramGraph(new BlankNode(),new BlankNode());
        falseCondGroups.forEach((resState,resList)->{
            ProgramGraph falseSubGraph;
            if(resList.size()>1) {
                falseSubGraph = new ProgramGraph(new BlankNode(),new BlankNode());
                resList.forEach(res->{
                    falseSubGraph.insertGraph(visitIfElseResult(ctx,res,resState,false));
                });
            }else{
                ExprRes res = resList.get(0);
                falseSubGraph = visitIfElseResult(ctx,res,resState,false);
            }
            falseGraph.insertGraph(falseSubGraph);
        });
        if(ctx.else_!=null){
            falseGraph.extendGraph(visit(ctx.else_));
            projection.put(ctx.else_, falseGraph.startNode);
        }
        graph.insertGraph(falseGraph);

        return graph;
    }
    /*
    private ProgramGraph visitSwitchResult(ReflexParser.SwitchStatContext ctx, ExprRes res, String exp0){
        Optional<String> condition = res.getFullCondition(currentProcess);
        String expr = creator.Conjunction(List.of(
                res.getExpr().toString(creator),
                (new BinaryExpression(BinaryOp.Eq,
                        res.getExpr(),
                        new ConstantExpression(exp0,res.getExpr().exprType()),
                        res.getExpr().exprType())
                ).toString(creator)));
        ConditionNode node = condition
                .map(s -> new ConditionNode(ctx, creator.Conjunction(List.of(s, expr))))
                .orElseGet(() -> new ConditionNode(ctx, creator.Conjunction(List.of(expr))));
        ProgramGraph g = new ProgramGraph(node, node);
        String resState = res.getState();
        if (!resState.equals(creator.PlaceHolder())) {
            ExpressionNode enode = new ExpressionNode(ctx.expression(), resState);
            g.extendGraph(enode);
        }
        res.getDomain().ifPresent(domain -> g.insertDanglingNode(new ConditionNode(ctx, domain)));
        return g;
    }*/

    @Override
    public ProgramGraph visitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx) {
        ProgramGraph pathGraph = new ProgramGraph(null,null);
        for(ReflexParser.StatementContext sctx: ctx.statement()){
            pathGraph.extendGraph(visit(sctx));
        }
        return pathGraph;
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
        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess,creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess,creator.PlaceHolder(),creator);
        }
        List<ExprRes> results = vis.parseExpression(e);
        results.forEach(res->res.getExpr().actuate(res.getState(), creator));

        ArrayList<Map.Entry<String,ProgramGraph>> pathGraphs = new ArrayList<>();
        ArrayList<ProgramGraph> pathGraphsToExtend = new ArrayList<>();
        for(ReflexParser.CaseStatContext newCtx:ctx.options){
            ExpressionVisitor1 vis0 = new ExpressionVisitor1(mapper,currentProcess,creator.PlaceHolder(),creator);
            String cond = vis0.parseExpression(newCtx.expression()).get(0).getExpr().toString(creator);
            ProgramGraph pathGraph = visitSwitchOptionStatSeq(newCtx.switchOptionStatSeq());
            pathGraphs.add(new ImmutablePair<>(cond,pathGraph));
            for(ProgramGraph path: pathGraphsToExtend){
                path.extendGraph(pathGraph);
            }
            pathGraphsToExtend.add(pathGraph);

            if(newCtx.switchOptionStatSeq().break_!=null){
                pathGraphsToExtend.clear();
            }
        }
        if(ctx.defaultStat()!=null){
            if(ctx.defaultStat().switchOptionStatSeq()!=null){
                //ConditionNode cnode = new ConditionNode(ctx,passedConditions);
                //ProgramGraph cgraph = new ProgramGraph(cnode,cnode);
                ProgramGraph defGraph = visitSwitchOptionStatSeq(ctx.defaultStat().switchOptionStatSeq());
                pathGraphs.add(new ImmutablePair<>(null,defGraph));
                for(ProgramGraph path: pathGraphsToExtend){
                    path.extendGraph(defGraph);
                }
            }else{
                if(pathGraphs.isEmpty()){
                    graph.connectStartEnd();
                }else{
                    BlankNode node= new BlankNode();
                    pathGraphs.add(new ImmutablePair<>(null,new ProgramGraph(node,node)));
                }
            }
        }else{
            if(pathGraphs.isEmpty()){
                graph.connectStartEnd();
            }else{
                BlankNode node= new BlankNode();
                pathGraphs.add(new ImmutablePair<>(null,new ProgramGraph(node,node)));
            }
        }

        int i=0;
        for(Map.Entry<String,ProgramGraph> path: pathGraphs){
            ArrayList<String> conditions = new ArrayList<>();
            ProgramGraph resPath;
            if(results.size()>1){
                resPath = new ProgramGraph(new BlankNode(),new BlankNode());
                for (ExprRes res : results) {
                    ProgramGraph condPath = switchResultProcessing(ctx, path, res, pathGraphs, conditions, e);
                    resPath.insertGraph(condPath);
                }
                resPath.extendGraph(path.getValue());
            }else{
                resPath = switchResultProcessing(ctx, path, results.get(0), pathGraphs, conditions, e);
            }
            resPath.extendGraph(path.getValue());
            graph.insertGraph(resPath);
            if(path.getKey()!=null) {
                projection.put(ctx.options.get(i), resPath.startNode);
                i++;
            }else if (ctx.defaultStat()!=null){
                projection.put(ctx.defaultStat(), resPath.startNode);
            }
        }
        return graph;
    }

    private ProgramGraph switchResultProcessing(ReflexParser.SwitchStatContext ctx, Map.Entry<String, ProgramGraph> path, ExprRes res, ArrayList<Map.Entry<String, ProgramGraph>> pathGraphs, ArrayList<String> conditions, ReflexParser.ExpressionContext e) {
        ProgramGraph condPath;
        Optional<String> fullCond = res.getFullCondition(currentProcess);
        if(fullCond.isPresent()){
            ConditionNode cnode = new ConditionNode(ctx, fullCond.get());
            condPath = new ProgramGraph(cnode,cnode);
            if(res.getDomain().isPresent()){
                condPath.insertDanglingNode(new ConditionNode(ctx, res.getDomain().get()));
            }
        }else{
            condPath = null;
        }
        SymbolicExpression expr = res.getExpr();
        for(Map.Entry<String,ProgramGraph> prevPath: pathGraphs){
            if(prevPath.getKey()==null){
                break;
            }else if (prevPath.getKey().equals(path.getKey())){
                conditions.add(new BinaryExpression(
                        BinaryOp.Eq,
                        expr,
                        new ConstantExpression(prevPath.getKey(),expr.exprType()),
                        expr.exprType()
                ).toString(creator));
                break;
            }else{
                conditions.add(new BinaryExpression(
                        BinaryOp.NotEq,
                        expr,
                        new ConstantExpression(prevPath.getKey(),expr.exprType()),
                        expr.exprType()).toString(creator));
            }
        }
        String condition = creator.Conjunction(conditions);
        ConditionNode cnode= new ConditionNode(ctx,condition);
        ProgramGraph  conPath = new ProgramGraph(cnode,cnode);
        if(condPath==null){
            condPath = conPath;
        }else{
            condPath.extendGraph(conPath);
        }
        if(!res.getState().equals(creator.PlaceHolder())){
            ExpressionNode expNode= new ExpressionNode(e, res.getState());
            condPath.extendGraph(expNode);
        }
        return condPath;
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
        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess,creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess,creator.PlaceHolder(),creator);
        }
        List<ExprRes> results = vis.parseExpression(e);
        ProgramGraph graph;
        if(results.size()>1){
            graph = new ProgramGraph(new BlankNode(),new BlankNode());
            results.forEach(res->{
                ProgramGraph g;
                Optional<String> condition = res.getFullCondition(currentProcess);
                if(condition.isPresent()){
                    ConditionNode node = new ConditionNode(e,condition.get());
                    g = new ProgramGraph(node,node);
                    if (res.getDomain().isPresent()){
                        g.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
                    }
                }else if (res.getDomain().isPresent()){
                    BlankNode node =new BlankNode();
                    g = new ProgramGraph(node,node);
                    g.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
                } else{
                    g= new ProgramGraph(null,null);
                }
                if(!res.getState().equals(creator.PlaceHolder())){
                    g.extendGraph(new ExpressionNode(e,res.getState()));
                }
                graph.insertGraph(g);
            });
        }else{
            ExprRes res= results.get(0);
            Optional<String> condition = res.getFullCondition(currentProcess);
            if(condition.isPresent()){
                ConditionNode node = new ConditionNode(e,condition.get());
                graph = new ProgramGraph(node,node);
                if (res.getDomain().isPresent()){
                    graph.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
                }
            }else if (res.getDomain().isPresent()){
                BlankNode node =new BlankNode();
                graph = new ProgramGraph(node,node);
                graph.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
            } else{
                graph= new ProgramGraph(null,null);
            }
            if(!res.getState().equals(creator.PlaceHolder())){
                graph.extendGraph(new ExpressionNode(e,res.getState()));
            }
        }
        return graph;
    }

    @Override
    public ProgramGraph visitEmptySt(ReflexParser.EmptyStContext ctx) {
        return new ProgramGraph(null,null);
    }
}
