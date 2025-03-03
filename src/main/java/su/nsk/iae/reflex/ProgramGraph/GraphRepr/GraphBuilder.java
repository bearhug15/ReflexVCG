package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import org.apache.commons.lang3.tuple.ImmutablePair;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.*;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.IntType;
import su.nsk.iae.reflex.expression.types.TimeType;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;


public class GraphBuilder extends ReflexBaseVisitor<ProgramGraph> {

    ProcessNode currentProcess;
    StateNode currentState;

    ProgramMetaData metaData;
    VariableMapper mapper;
    IStatementCreator creator;
    ASTGraphProjection projection;
    boolean isSimpleExprVisitor;


    HashMap<ParserRuleContext,IReflexNode> ctxNodeProjection = new HashMap<>();
    Stack<IAttributed> attributeContainers = new Stack<>();
    AttributeCollector collector = new AttributeCollector();

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
    public AttributeCollector getAttributeCollector(){
        return collector;
    }

    public void preparePSNodesAttr(ReflexParser.ProgramContext ctx){
        int i=0;
        for(ReflexParser.ProcessContext pctx: ctx.processes){
            ProcessNode processNode = ProcessNode.ProcessNodes(pctx, pctx.name.getText()).getKey();
            ctxNodeProjection.put(pctx,processNode);
            ProcessAttributes pattr = new ProcessAttributes(processNode);
            collector.addAttributes(processNode,pattr);
            if(i==0){
                pattr.setStartS(false);
            }
            for(ReflexParser.StateContext sctx:pctx.states){
                StateNode stateNode = StateNode.StateNodes(sctx,sctx.name.getText(),pctx.name.getText()).getKey();
                ctxNodeProjection.put(sctx,stateNode);
                StateAttributes sattr = new StateAttributes(processNode,stateNode);
                collector.addAttributes(stateNode,sattr);
            }
            i++;
        }

    }

    @Override
    public ProgramGraph visitProgram(ReflexParser.ProgramContext ctx) {
        ArrayList<String> varInits = new ArrayList<>();
        for (Map.Entry<String, ExprType> variable: metaData.getInputVariablesNames().entrySet()){
            varInits.add(creator.Setter(variable.getValue(),creator.PlaceHolder(),variable.getKey(),variable.getKey()));
        }

        Map.Entry<ProgramNode,ProgramNode> nodes = ProgramNode.ProgramNodes(ctx,ctx.name.getText(),varInits);
        //projection.put(ctx, nodes.getKey());
        ProgramAttributes attr = new ProgramAttributes(nodes.getKey());
        collector.addAttributes(nodes.getKey(),attr);
        attributeContainers.push(attr);

        preparePSNodesAttr(ctx);

        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        ProgramGraph g = new ProgramGraph(null,null);
        for(ReflexParser.ProcessContext newCtx: ctx.processes){
            ProgramGraph buff= visitProcess(newCtx);
            g.extendGraph(buff);
        }
        graph.insertGraph(g);
        attributeContainers.pop();

        assert(attributeContainers.isEmpty());
        return graph;
    }

    @Override
    public ProgramGraph visitProcess(ReflexParser.ProcessContext ctx) {
        ProcessNode startNode= (ProcessNode) ctxNodeProjection.get(ctx);
        ProcessNode endNode = (ProcessNode)startNode.getBind();
        currentProcess = startNode;
        ProcessAttributes attr = (ProcessAttributes) collector.getAttributes(startNode);
        attributeContainers.peek().addAttributes(attr);
        attributeContainers.push(attr);
        //Map.Entry<ProcessNode,ProcessNode> nodes = ProcessNode.ProcessNodes(ctx,ctx.name.getText());
        //projection.put(ctx, nodes.getKey());

        ProgramGraph graph = new ProgramGraph(startNode,endNode);
        for(ReflexParser.StateContext newCtx: ctx.state()){
            ProgramGraph g = visitState(newCtx);
            graph.insertGraph(g);
        }

        Map.Entry<StateNode,StateNode> stopNodes = StateNode.StateNodes(null,"stop",currentProcess.getProcessName());
        ProgramGraph stopGraph = new ProgramGraph(stopNodes.getKey(),stopNodes.getValue());
        stopGraph.connectStartEnd();
        graph.insertGraph(stopGraph);
        StateAttributes sattr = new StateAttributes(startNode,stopNodes.getKey());
        collector.addAttributes(stopNodes.getKey(),sattr);

        Map.Entry<StateNode,StateNode> errorNodes = StateNode.StateNodes(null,"error",currentProcess.getProcessName());
        ProgramGraph errorGraph = new ProgramGraph(errorNodes.getKey(),errorNodes.getValue());
        errorGraph.connectStartEnd();
        graph.insertGraph(errorGraph);
        StateAttributes eattr = new StateAttributes(startNode,errorNodes.getKey());
        collector.addAttributes(errorNodes.getKey(),eattr);

        attributeContainers.pop();
        return graph;
    }

    @Override
    public ProgramGraph visitState(ReflexParser.StateContext ctx) {
        StateNode startNode= (StateNode) ctxNodeProjection.get(ctx);
        StateNode endNode = (StateNode)startNode.getBind();
        currentState = startNode;
        StateAttributes attr = (StateAttributes) collector.getAttributes(startNode);
        attributeContainers.peek().addAttributes(attr);
        attributeContainers.push(attr);
        //Map.Entry<StateNode,StateNode> nodes = StateNode.StateNodes(ctx,ctx.name.getText(),currentProcess);
        //projection.put(ctx, nodes.getKey());


        ProgramGraph graph = new ProgramGraph(startNode, endNode);
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

        attributeContainers.pop();
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
            if(mapper.is_variable(currentProcess.getProcessName(),var.getText())){
                ExprType t= mapper.variableType(currentProcess.getProcessName(),var.getText());
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
        TimeoutCore attr = new TimeoutCore(nodes.getKey(),nodes.getKey().isVariable());
        collector.addAttributes(nodes.getKey(),attr);
        attributeContainers.peek().addAttributes(attr);
        attributeContainers.add(attr);

        //projection.put(ctx, nodes.getKey());
        ProgramGraph graph = new ProgramGraph(nodes.getKey(), nodes.getValue());

        ConditionNode exceedCondition = new ConditionNode(ctx, creator.TimeoutExceed(creator.PlaceHolder(),condition,currentProcess.getProcessName()),isVariable);
        ProgramGraph exceedGraph = new ProgramGraph(exceedCondition,exceedCondition);
        ProgramGraph exceed;
        TimeoutBranch attr1 = new TimeoutBranch(exceedCondition,true);
        attr.addAttributes(attr1);
        attributeContainers.push(attr1);
        if(ctx.statement()!=null){
            exceed = visit(ctx.statement());
        }else{
            exceed = new ProgramGraph(null,null);
        }
        attributeContainers.pop();
        exceedGraph.extendGraph(exceed);
        graph.insertGraph(exceedGraph);
        collector.addAttributes(exceedGraph.startNode,attr1);

        ConditionNode loseCondition = new ConditionNode(ctx, creator.TimeoutLose(creator.PlaceHolder(),condition,currentProcess.getProcessName()),isVariable);
        ProgramGraph loseGraph = new ProgramGraph(loseCondition,loseCondition);
        graph.insertGraph(loseGraph);
        TimeoutBranch attr2 = new TimeoutBranch(loseCondition,false);
        attr.addAttributes(attr2);
        collector.addAttributes(loseGraph.startNode,attr2);

        attributeContainers.pop();
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
        Optional<String> condition = res.getFullCondition(currentProcess.getProcessName());
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
        if(ctx.expression()==null){
            throw new RuntimeException("If condition is null");
        }

        Map.Entry<IfElseNode,IfElseNode> nodes = IfElseNode.IfElseNodes(ctx);
        IfElseCore attr = new IfElseCore(currentProcess,currentState, nodes.getKey());
        attributeContainers.peek().addAttributes(attr);
        attributeContainers.push(attr);
        collector.addAttributes(nodes.getKey(),attr);
        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        //projection.put(ctx, nodes.getKey());

        ReflexParser.ExpressionContext e=ctx.expression();

        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
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
                    ProgramGraph sub = visitIfElseResult(ctx,res,resState,true);
                    if(!res.getProcessesStatuses().isEmpty()){
                        UniversalAttributes subAttr = new UniversalAttributes(sub.startNode);
                        subAttr.setProcStatuses(res.getProcessesStatuses());
                        collector.addAttributes(sub.startNode,subAttr);
                    }
                    trueSubGraph.insertGraph(sub);
                });
            }else{
                ExprRes res = resList.get(0);

                trueSubGraph = visitIfElseResult(ctx,res,resState,true);
                if(!res.getProcessesStatuses().isEmpty()){
                    UniversalAttributes subAttr = new UniversalAttributes(trueSubGraph.startNode);
                    subAttr.setProcStatuses(res.getProcessesStatuses());
                    collector.addAttributes(trueSubGraph.startNode,subAttr);
                }
            }
            trueGraph.insertGraph(trueSubGraph);
        });

        IfElseBranch trueAttr = new IfElseBranch(currentProcess,currentState, nodes.getKey());
        attributeContainers.push(trueAttr);
        if(ctx.then!=null){
            trueGraph.extendGraph(visit(ctx.then));
            //projection.put(ctx.then, trueGraph.startNode);
        }
        collector.addAttributes(trueGraph.startNode, trueAttr);
        attributeContainers.pop();
        attr.addAttributes(trueAttr);
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

        IfElseBranch falseAttr = new IfElseBranch(currentProcess,currentState, nodes.getKey());
        attributeContainers.push(falseAttr);
        if(ctx.else_!=null){
            falseGraph.extendGraph(visit(ctx.else_));
            projection.put(ctx.else_, falseGraph.startNode);
        }
        collector.addAttributes(falseGraph.startNode, falseAttr);
        attributeContainers.pop();
        attr.addAttributes(falseAttr);
        graph.insertGraph(falseGraph);

        attributeContainers.pop();
        return graph;
    }

    @Override
    public ProgramGraph visitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx) {
        ProgramGraph pathGraph = new ProgramGraph(null,null);
        for(ReflexParser.StatementContext sctx: ctx.statement()){
            pathGraph.extendGraph(visit(sctx));
        }
        return pathGraph;
    }

    public ArrayList<Map.Entry<String,ProgramGraph>> createCasePaths(ReflexParser.SwitchStatContext ctx, SwitchNode switchNode){
        ArrayList<Map.Entry<String,ProgramGraph>> pathGraphs = new ArrayList<>();
        ArrayList<ProgramGraph> pathGraphsToExtend = new ArrayList<>();
        for(ReflexParser.CaseStatContext newCtx:ctx.options){
            ExpressionVisitor1 vis0 = new ExpressionVisitor1(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
            String cond = vis0.parseExpression(newCtx.expression()).get(0).getExpr().toString(creator);

            SwitchCaseBranch branchAttr = new SwitchCaseBranch(currentProcess,currentState,switchNode);
            attributeContainers.peek().addAttributes(branchAttr);
            attributeContainers.push(branchAttr);
            ProgramGraph pathGraph = visitSwitchOptionStatSeq(newCtx.switchOptionStatSeq());
            attributeContainers.pop();
            collector.addAttributes(pathGraph.startNode,branchAttr);

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

                SwitchCaseBranch branchAttr = new SwitchCaseBranch(currentProcess,currentState,switchNode);
                attributeContainers.peek().addAttributes(branchAttr);
                attributeContainers.push(branchAttr);
                ProgramGraph defGraph = visitSwitchOptionStatSeq(ctx.defaultStat().switchOptionStatSeq());
                attributeContainers.pop();
                collector.addAttributes(defGraph.startNode,branchAttr);

                pathGraphs.add(new ImmutablePair<>(null,defGraph));
                for(ProgramGraph path: pathGraphsToExtend){
                    path.extendGraph(defGraph);
                }
            }else{
                if(!pathGraphs.isEmpty()){
                    BlankNode node = new BlankNode();
                    pathGraphs.add(new ImmutablePair<>(null,new ProgramGraph(node,node)));
                    SwitchCaseBranch branchAttr = new SwitchCaseBranch(currentProcess,currentState,switchNode);
                    attributeContainers.peek().addAttributes(branchAttr);
                    collector.addAttributes(node,branchAttr);
                }
            }
        }else{
            if(!pathGraphs.isEmpty()){
                BlankNode node= new BlankNode();
                pathGraphs.add(new ImmutablePair<>(null,new ProgramGraph(node,node)));
                SwitchCaseBranch branchAttr = new SwitchCaseBranch(currentProcess,currentState,switchNode);
                attributeContainers.peek().addAttributes(branchAttr);
                collector.addAttributes(node,branchAttr);
            }
        }
        return pathGraphs;
    }

    @Override
    public ProgramGraph visitSwitchStat(ReflexParser.SwitchStatContext ctx) {
        if(ctx.expression()==null){
            throw new RuntimeException("If condition is null");
        }
        Map.Entry<SwitchNode,SwitchNode> nodes = SwitchNode.SwitchNodes(ctx);
        SwitchCaseCore attr = new SwitchCaseCore(currentProcess,currentState, nodes.getKey());
        attributeContainers.peek().addAttributes(attr);
        attributeContainers.push(attr);
        collector.addAttributes(nodes.getKey(),attr);

        ProgramGraph graph = new ProgramGraph(nodes.getKey(),nodes.getValue());
        //projection.put(ctx, nodes.getKey());

        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
        }
        List<ExprRes> results = vis.parseExpression(e);
        results.forEach(res->res.getExpr().actuate(res.getState(), creator));

        ArrayList<Map.Entry<String,ProgramGraph>> pathGraphs = createCasePaths(ctx,nodes.getKey());
        if(pathGraphs.isEmpty()){
            graph.connectStartEnd();
        }

        for(Map.Entry<String,ProgramGraph> path: pathGraphs){
            ArrayList<String> conditions = new ArrayList<>();
            ProgramGraph resPath;
            if(results.size()>1){
                resPath = new ProgramGraph(new BlankNode(),new BlankNode());
                for (ExprRes res : results) {
                    ProgramGraph condPath = switchResultProcessing(ctx, path, res, pathGraphs, conditions, e);
                    resPath.insertGraph(condPath);
                    if(!res.getProcessesStatuses().isEmpty()){
                        UniversalAttributes subAttr = new UniversalAttributes(condPath.startNode);
                        subAttr.setProcStatuses(res.getProcessesStatuses());
                        collector.addAttributes(condPath.startNode,subAttr);
                    }
                }
                resPath.extendGraph(path.getValue());
            }else{
                resPath = switchResultProcessing(ctx, path, results.get(0), pathGraphs, conditions, e);
            }
            resPath.extendGraph(path.getValue());
            graph.insertGraph(resPath);
            /*if(path.getKey()!=null) {
                projection.put(ctx.options.get(i), resPath.startNode);
                i++;
            }else if (ctx.defaultStat()!=null){
                projection.put(ctx.defaultStat(), resPath.startNode);
            }*/
        }
        attributeContainers.pop();
        return graph;
    }

    private ProgramGraph switchResultProcessing(ReflexParser.SwitchStatContext ctx, Map.Entry<String, ProgramGraph> path, ExprRes res, ArrayList<Map.Entry<String, ProgramGraph>> pathGraphs, ArrayList<String> conditions, ReflexParser.ExpressionContext e) {
        ProgramGraph condPath;
        Optional<String> fullCond = res.getFullCondition(currentProcess.getProcessName());
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
        if(id.equals(currentProcess.getProcessName())){
            SetStateNode node = new SetStateNode(ctx,currentProcess.getProcessName(), metaData.startState(currentProcess.getProcessName()));
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Start)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Start),Boolean.TRUE)));
            attr.setReset(true);
            if(!metaData.isFirstState(currentProcess.getProcessName(),currentState.getStateName())){
                ((StateAttributes)collector.getAttributes(ctxNodeProjection.get(metaData.firstStateCtx(currentProcess.getProcessName())))).addReachFrom(currentState.getStateName());
                attr.setStateChanging(true);
            }
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Start,metaData);
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Start)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Start),Boolean.TRUE)));
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        ProgramGraph graph;
        if(ctx.ID()==null || ctx.ID().toString().equals(currentProcess.getProcessName())){
            SetStateNode node = new SetStateNode(ctx,currentProcess.getProcessName(), "stop");
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Stop)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Stop),Boolean.TRUE)));
            attr.setReset(true);
            attr.setStateChanging(true);
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            String id = ctx.ID().toString();
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Stop,metaData);
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Stop)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Stop),Boolean.TRUE)));
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        ProgramGraph graph;
        if(ctx.ID()==null || ctx.ID().toString().equals(currentProcess.getProcessName())){
            SetStateNode node = new SetStateNode(ctx,currentProcess.getProcessName(), "error");
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Error)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Error),Boolean.TRUE)));
            attr.setReset(true);
            attr.setStateChanging(true);
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }else{
            String id = ctx.ID().toString();
            ProcessChangeNode node= new ProcessChangeNode(ctx,id,ChangeType.Error,metaData);
            UniversalAttributes attr = new UniversalAttributes(node);
            attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Stop)));
            attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Stop),Boolean.TRUE)));
            attributeContainers.peek().addAttributes(attr);
            //projection.put(ctx, node);
            graph = new ProgramGraph(node,node);
        }
        return graph;
    }

    @Override
    public ProgramGraph visitRestartStat(ReflexParser.RestartStatContext ctx) {
        SetStateNode node = new SetStateNode(ctx,currentProcess.getProcessName(), metaData.startState(currentProcess.getProcessName()));
        UniversalAttributes attr = new UniversalAttributes(node);
        attr.setProcChange(Map.ofEntries(entry(currentProcess,ChangeType.Start)));
        attr.setPotProcChange(Map.ofEntries(entry(new AbstractMap.SimpleImmutableEntry<>(currentProcess,ChangeType.Start),Boolean.TRUE)));
        attr.setReset(true);
        if(!metaData.isFirstState(currentProcess.getProcessName(),currentState.getStateName())){
            ((StateAttributes)collector.getAttributes(ctxNodeProjection.get(metaData.firstStateCtx(currentProcess.getProcessName())))).addReachFrom(currentState.getStateName());
            attr.setStateChanging(true);
        }
        attributeContainers.peek().addAttributes(attr);
        //projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitResetStat(ReflexParser.ResetStatContext ctx) {
        ResetNode node= new ResetNode(ctx,currentProcess.getProcessName());
        UniversalAttributes attr = new UniversalAttributes(node);
        attr.setReset(true);
        attributeContainers.peek().addAttributes(attr);

        //projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
        SetStateNode node;
        UniversalAttributes attr;
        if (ctx.stateId!=null){
            String id = ctx.stateId.getText();
            node = new SetStateNode(ctx,currentProcess.getProcessName(),id);
            attr = new UniversalAttributes(node);
            attr.setReset(true);
            ((StateAttributes)collector.getAttributes(ctxNodeProjection.get(metaData.getState(currentProcess.getProcessName(),currentState.getStateName())))).addReachFrom(currentState.getStateName());
        }else{
            node = new SetStateNode(ctx,currentProcess.getProcessName(),metaData.nextState(currentProcess.getProcessName(),currentState.getStateName()));
            attr = new UniversalAttributes(node);
            attr.setReset(true);
        }
        attributeContainers.peek().addAttributes(attr);
        //projection.put(ctx, node);
        return new ProgramGraph(node,node);
    }

    @Override
    public ProgramGraph visitExprSt(ReflexParser.ExprStContext ctx) {
        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis;
        if(isSimpleExprVisitor){
            vis = new ExpressionVisitor1(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
        }else{
            vis = new ExpressionVisitor2(mapper,currentProcess.getProcessName(),creator.PlaceHolder(),creator);
        }
        List<ExprRes> results = vis.parseExpression(e);
        ProgramGraph graph;
        if(results.size()>1){
            graph = new ProgramGraph(new BlankNode(),new BlankNode());
            results.forEach(res->{
                ProgramGraph sub;
                Optional<String> condition = res.getFullCondition(currentProcess.getProcessName());
                if(condition.isPresent()){
                    ConditionNode node = new ConditionNode(e,condition.get());
                    sub = new ProgramGraph(node,node);
                    if (res.getDomain().isPresent()){
                        sub.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
                    }
                }else if (res.getDomain().isPresent()){
                    BlankNode node =new BlankNode();
                    sub = new ProgramGraph(node,node);
                    sub.insertDanglingNode(new ConditionNode(e,res.getDomain().get()));
                } else{
                    sub= new ProgramGraph(null,null);
                }
                if(!res.getState().equals(creator.PlaceHolder())){
                    sub.extendGraph(new ExpressionNode(e,res.getState()));
                }
                if(!res.getProcessesStatuses().isEmpty()){
                    UniversalAttributes subAttr = new UniversalAttributes(sub.startNode);
                    subAttr.setProcStatuses(res.getProcessesStatuses());
                    collector.addAttributes(sub.startNode,subAttr);
                }
                graph.insertGraph(sub);
            });
        }else{
            ExprRes res= results.get(0);
            Optional<String> condition = res.getFullCondition(currentProcess.getProcessName());
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
            if(!res.getProcessesStatuses().isEmpty()){
                UniversalAttributes subAttr = new UniversalAttributes(graph.startNode);
                subAttr.setProcStatuses(res.getProcessesStatuses());
                collector.addAttributes(graph.startNode,subAttr);
            }
        }
        return graph;
    }

    @Override
    public ProgramGraph visitEmptySt(ReflexParser.EmptyStContext ctx) {
        return new ProgramGraph(null,null);
    }
}
