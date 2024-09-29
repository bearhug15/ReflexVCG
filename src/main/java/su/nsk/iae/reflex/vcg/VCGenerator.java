package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.*;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexLexer;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.ConstantExpression;
import su.nsk.iae.reflex.expression.RawExpression;
import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.VariableExpression;
import su.nsk.iae.reflex.expression.types.*;
import su.nsk.iae.reflex.formulas.*;
//import su.nsk.iae.reflex.staticAnalysis.ProgramAnalyzer;
import su.nsk.iae.reflex.staticAnalysis.AttributedPath;
import su.nsk.iae.reflex.staticAnalysis.ProgramAnalyzer2;
import su.nsk.iae.reflex.staticAnalysis.RuleChecker;
import su.nsk.iae.reflex.staticAnalysis.attributes.*;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

public class VCGenerator extends ReflexBaseVisitor<GenReturn> {

    VariableMapper mapper;
    ConjuctionFormula formula;

    AttributedPath path;
    Integer stateCount;
    String currentProcess;
    String currentState;
    ProgramMetaData metaData;
    RuleChecker checker;

    Stack<BranchPoint> branchStack;
    VCPrinter printer;
    //ProcessStateTraces traces;
    HashMap<String,Integer> ifCounter;

    AttributeCollector collector;

    Integer conditionsGenerated;
    public VCGenerator(){
        stateCount = 0;
        formula = new ConjuctionFormula();
        path = new AttributedPath();
        mapper = null;
        branchStack = new Stack<>();
        metaData = null;
        checker = null;
        //traces = new ProcessStateTraces();
        ifCounter = new HashMap<>();
        conditionsGenerated = 0;
    }

    public void generateVC(Path source, Path destination){
        String sourceName = source.getFileName().toString();


        try {
            System.out.println("Starting program parsing.");
            FileInputStream fileInput = new FileInputStream(source.toFile());
            ANTLRInputStream input = new ANTLRInputStream(fileInput);
            ReflexLexer lexer = new ReflexLexer(input);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            mapper = new VariableMapper(context);
            metaData = new ProgramMetaData(context,mapper);
            printer = new VCPrinter(destination,sourceName,metaData);
            checker = new RuleChecker(metaData);
            System.out.println("Completed program parsing. Starting program analysis.");
            ProgramAnalyzer2 analyzer2 = new ProgramAnalyzer2(metaData);
            collector = analyzer2.generateAttributes(context);
            System.out.println("Completed program analysis. Starting verification conditions generation.");
            visitProgram(context);
            visitStack();
            System.out.println("Completed verification conditions generation. Conditions generated: " + conditionsGenerated );
        }catch (Exception e){
            System.out.println("Generation aborted due an error:");
            throw new RuntimeException(e);
        }

    }

    /*private void analyzeProgram(ReflexParser.ProgramContext ctx){
        ProgramAnalyzer analyzer = new ProgramAnalyzer(traces,metaData);
        analyzer.visitProgram(ctx);
    }*/

    private Void initializeInputVariables(){
        for (Map.Entry<String,ExprType> variable: metaData.getInputVariablesNames().entrySet()){
            String setter = StringUtils.constructSetter(variable.getValue(),stateName(),variable.getKey(),variable.getKey());
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),setter));
        }
        return null;
    }
    @Override
    public GenReturn visitProgram(ReflexParser.ProgramContext ctx) {
        List<String> processNames = metaData.processNames();
        formula.addConjunct(new StateFormula(stateName(),(new StateType(stateName())).defaultValue()));
        for(String processName: processNames){
            String initialized = metaData.initializeProcess(stateName(),processName);
            if (!initialized.equals(stateName())) {
                stateCount++;
                formula.addConjunct(new StateFormula(stateName(), initialized));
            }
        }
        String startProcess = metaData.startProcess();
        String setP = setPstate(stateName(),startProcess,metaData.startState(startProcess));
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        String toE = toEnv(stateName());
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),toE));

        ImplicationFormula f1 = buildImplication(formula);
        finishVC(f1);

        stateCount=0;
        formula = new ConjuctionFormula();
        formula.addConjunct(new RawFormula("base_inv",inv(stateName())));
        initializeInputVariables();
        for(ReflexParser.ProcessContext cont: ctx.processes){
            GenReturn ret = visitProcess(cont);
            if (ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        toE = toEnv(stateName());
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),toE));
        ImplicationFormula f2 = buildImplication(formula);
        finishVC(f2);

        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitProcess(ReflexParser.ProcessContext ctx) {
        currentProcess = ctx.name.getText();
        IAttributed last;
        last = path.peekLast();
        branchStack.push(new BranchPoint(
                formula.peekLastConjunct(),
                last,
                ctx,
                -2,
                stateCount,
                currentProcess,
                currentState));
        branchStack.push(new BranchPoint(
                formula.peekLastConjunct(),
                last,
                ctx,
                -1,
                stateCount,
                currentProcess,
                currentState));
        for(int i=ctx.states.size()-1;i>0;i--){
            //
                branchStack.push(new BranchPoint(
                        formula.peekLastConjunct(),
                        last,
                        ctx,
                        i,
                        stateCount,
                        currentProcess,
                        currentState));
            //}
        }

        formula.addConjunct(new EqualityFormula(
                stateProcessStateName(currentProcess),
                true,
                new RawExpression(getPstate(stateName(),currentProcess)),
                new RawExpression("''"+metaData.stateByIdx(currentProcess,0)+"''")));
        GenReturn ret = visitState(ctx.states.get(0));
        if (ret.getReturnType().equals(ReturnType.ImpossibleVC))
            return ret;
        if(formula.isMarkedReset()) {
            String setP = setPstate(stateName(), currentProcess, metaData.startState(currentProcess));
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(), setP));
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitState(ReflexParser.StateContext ctx) {
        currentState = ctx.name.getText();

        ProcessAttributes attr1 = (ProcessAttributes)collector.getAttributes(ctx.getParent());
        attr1.setState(currentState);
        path.add (attr1);

        if(!checker.checkRules(path,attr1))return new GenReturn(ReturnType.ImpossibleVC);

        StateAttributes attr2 = (StateAttributes)collector.getAttributes(ctx);
        path.add(attr2);


        GenReturn ret = visitStatementSeq(ctx.stateFunction);
        if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
            return ret;

        if (ctx.func !=null){
            ret = visitTimeoutFunction(ctx.func);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx) {
        branchStack.push(new BranchPoint(
                formula.peekLastConjunct(),
                path.peekLast(),
                ctx,
                1,
                stateCount,
                currentProcess,
                currentState));
        SymbolicExpression exp;
        if(ctx.timeAmountOrRef().time!=null){
            if(!checker.checkTimeout(path))return new GenReturn(ReturnType.ImpossibleVC);
            exp = new ConstantExpression(ctx.timeAmountOrRef().time.getText(),new TimeType());
        } else if (ctx.timeAmountOrRef().intTime!=null){
            if(!checker.checkTimeout(path))return new GenReturn(ReturnType.ImpossibleVC);
            exp = new ConstantExpression(ctx.timeAmountOrRef().intTime.getText(),new IntType());
        } else{
            String id = ctx.timeAmountOrRef().ref.getText();
            ExprType t;
            if (mapper.is_variable(currentProcess,id)){
                t= mapper.variableType(currentProcess,id);
                VariableExpression subExp = new VariableExpression(ctx.timeAmountOrRef().ref.getText(),t,true);
                subExp.actuate(stateName());
                exp = subExp;
            }else if (mapper.is_const(id)){
                if(!checker.checkTimeout(path))return new GenReturn(ReturnType.ImpossibleVC);
                t= mapper.constantType(id);
                exp = new ConstantExpression(mapper.constantValue(id),t);
            } else{
                if(!checker.checkTimeout(path))return new GenReturn(ReturnType.ImpossibleVC);
                t = new IntType();
                VariableExpression subExp = new VariableExpression(ctx.timeAmountOrRef().ref.getText(),t,true);
                subExp.actuate(stateName());
                exp = subExp;
            }
        }
        formula.addConjunct(new GreaterFormula(
                stateTimeoutName(currentState),
                false,
                exp.toString(),
                ltime(stateName(),currentProcess)
        ));
        return visitStatement(ctx.body);
    }

    @Override
    public GenReturn visitExprSt(ReflexParser.ExprStContext ctx) {
        ReflexParser.ExpressionContext e=ctx.expression();
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        String newState = res.getState();
        Formula domain = res.getDomain();
        domain = domain.trim();
        if (!(domain instanceof TrueFormula)){
            ImplicationFormula implFormula = new ImplicationFormula(formula,domain);
            finishVC(implFormula);
            formula.addConjunct(domain);
        }
        stateCount++;
        Formula stateConj = new StateFormula(stateName(),newState);
        formula.addConjunct(stateConj);
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitStatementSeq(ReflexParser.StatementSeqContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            GenReturn ret = visitStatement(cont);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitCompoundStatement(ReflexParser.CompoundStatementContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            GenReturn ret = visitStatement(cont);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitIfElseSt(ReflexParser.IfElseStContext ctx) {
        ReflexParser.ExpressionContext e=ctx.ifElseStat().cond;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        exp.actuate(newState);
        domain = domain.trim();
        if (!(domain instanceof TrueFormula)){
            ImplicationFormula implFormula = new ImplicationFormula(formula,domain);
            finishVC(implFormula);
            formula.addConjunct(domain);
        }
        if (!newState.equals(stateName())){
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),newState));
        }
        branchStack.push(new BranchPoint(
                formula.peekLastConjunct(),
                path.peekLast(),
                ctx,
                1,
                stateCount,
                currentProcess,
                currentState));

        IfElseCore attr1 = (IfElseCore)collector.getAttributes(ctx.ifElseStat());
        path.add(attr1.getTrueAttributes());

        formula.addConjunct(new EqualityFormula(stateIfName(),true, exp,new ConstantExpression("True",new BoolType())));

        return visitStatement(ctx.ifElseStat().then);
    }

    @Override
    public GenReturn visitSwitchSt(ReflexParser.SwitchStContext ctx) {
        ReflexParser.ExpressionContext e=ctx.switchStat().expr;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        exp.actuate(newState);
        domain = domain.trim();
        if (!(domain instanceof TrueFormula)){
            ImplicationFormula implFormula = new ImplicationFormula(formula,domain);
            finishVC(implFormula);
            formula.addConjunct(domain);
        }
        if (!newState.equals(stateName())){
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),newState));
        }
        int numBranches = ctx.switchStat().options.size();
        for(int i=numBranches-1;i>=1;i--){
            branchStack.push(new BranchPoint(
                    formula.peekLastConjunct(),
                    path.peekLast(),
                    ctx,
                    i,
                    stateCount,
                    currentProcess,
                    currentState));
        }
        if (ctx.switchStat().defaultOption!=null) {
            branchStack.push(new BranchPoint(
                    formula.peekLastConjunct(),
                    path.peekLast(),
                    ctx,
                    -1,
                    stateCount,
                    currentProcess,
                    currentState));
        }
        SwitchCaseCore attr1 = (SwitchCaseCore)collector.getAttributes(ctx.switchStat());
        path.add(attr1.getBranchAttributes().firstElement());

        boolean breakDef=false;
        ReflexParser.CaseStatContext baseCase=ctx.switchStat().options.get(0);
        ReflexParser.ExpressionContext e0 = baseCase.option;
        ExpressionVisitor vis0 = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res0 = vis0.visitExpression(e0);
        SymbolicExpression exp0 = res0.getExpr();
        if(!exp0.isActuated()){
            throw new RuntimeException("Using of non constant expression in case");
        }
        formula.addConjunct(new EqualityFormula(stateBranchName(0),true,exp,exp0));
        for (int i=0;i<numBranches;i++){
            ReflexParser.CaseStatContext caseI=ctx.switchStat().options.get(i);
            GenReturn ret = visitSwitchOptionStatSeq(caseI.switchOptionStatSeq());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
            if(!(caseI.switchOptionStatSeq().break_==null)){
                breakDef=true;
                break;
            }
        }
        if(!breakDef && ctx.switchStat().defaultOption!=null){
            GenReturn ret = visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            GenReturn ret = visitStatement(cont);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return new GenReturn(ReturnType.Normal);
    }


    @Override
    public GenReturn visitStartProcStat(ReflexParser.StartProcStatContext ctx) {
        String id = ctx.processId.getText();
        if (id.equals(currentProcess)){
            formula.addConjunct(new MarkRestart());
        }else{
            String setP = setPstate(stateName(),id,metaData.startState(id));
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),setP));
        }
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        Token id = ctx.processId;
        String setP;
        if (id==null){
            setP = setPstate(stateName(),currentProcess,"''stop''");
        }else{
            setP = setPstate(stateName(),id.getText(),"''stop''");
        }
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        Token id = ctx.processId;
        String setP;
        if (id==null){
            setP = setPstate(stateName(),currentProcess,"''error''");
        }else{
            setP = setPstate(stateName(),id.getText(),"''error''");
        }
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitRestartStat(ReflexParser.RestartStatContext ctx) {
        formula.addConjunct(new MarkRestart());
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitResetStat(ReflexParser.ResetStatContext ctx) {
        String res = reset(stateName(),currentProcess);
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),res));
        return new GenReturn(ReturnType.Normal);
    }

    @Override
    public GenReturn visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
        Token id = ctx.stateId;
        String nextProcessStateName;
        if (id == null){
            nextProcessStateName = metaData.nextState(currentProcess,currentState);
        }else{
            nextProcessStateName =id.getText();
        }
        String setP = setPstate(stateName(),currentProcess,nextProcessStateName);
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        return new GenReturn(ReturnType.Normal);
    }

    public GenReturn visitStatement(ReflexParser.StatementContext ctx){
        if (ctx instanceof ReflexParser.EmptyStContext){
            return new GenReturn(ReturnType.Normal);
        }
        if (ctx instanceof ReflexParser.StartProcessStContext){
            return visitStartProcessSt((ReflexParser.StartProcessStContext)ctx);
        }
        if (ctx instanceof ReflexParser.StopProcessStContext){
            return visitStopProcessSt((ReflexParser.StopProcessStContext)ctx);
        }
        if (ctx instanceof ReflexParser.ErrorProcessStContext){
            return visitErrorProcessSt((ReflexParser.ErrorProcessStContext)ctx);
        }
        if (ctx instanceof ReflexParser.RestartStContext){
            return visitRestartSt((ReflexParser.RestartStContext)ctx);
        }
        if (ctx instanceof ReflexParser.ResetStContext){
            return visitResetSt((ReflexParser.ResetStContext)ctx);
        }
        if (ctx instanceof ReflexParser.SetStateStContext){
            return visitSetStateSt((ReflexParser.SetStateStContext)ctx);
        }
        if (ctx instanceof ReflexParser.IfElseStContext){
            return visitIfElseSt((ReflexParser.IfElseStContext)ctx);
        }
        if (ctx instanceof ReflexParser.SwitchStContext){
            return visitSwitchSt((ReflexParser.SwitchStContext)ctx);
        }
        if (ctx instanceof ReflexParser.ExprStContext){
            return visitExprSt((ReflexParser.ExprStContext)ctx);
        }
        if (ctx instanceof ReflexParser.CompoundStContext){
            return visitCompoundSt((ReflexParser.CompoundStContext) ctx);
        }
        throw new RuntimeException("Trying to visit unknown statement");
    }

    private GenReturn visitRest(RuleContext ctx, ParserRuleContext childCtx) {
        if (ctx instanceof ReflexParser.CompoundStContext){
            return visitCompoundStRest((ReflexParser.CompoundStContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.CompoundStatementContext){
            return visitCompoundStatementRest((ReflexParser.CompoundStatementContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.StatementSeqContext){
            return visitStatementSeqRest((ReflexParser.StatementSeqContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.IfElseStContext){
            return visitIfElseStRest((ReflexParser.IfElseStContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.IfElseStatContext){
            return visitIfElseStRest((ReflexParser.IfElseStContext)ctx.parent,childCtx);
        }
        if (ctx instanceof ReflexParser.SwitchOptionStatSeqContext){
            return visitSwitchOptionStatSeqRest((ReflexParser.SwitchOptionStatSeqContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.CaseStatContext){
            return visitSwitchStRest((ReflexParser.SwitchStContext)ctx.parent.parent,childCtx);
        }
        if (ctx instanceof ReflexParser.DefaultStatContext){
            ParserRuleContext newChild = (ReflexParser.SwitchStContext)ctx.parent.parent;
            return visitRest(newChild.parent,newChild);
        }
        if (ctx instanceof ReflexParser.TimeoutFunctionContext){
            return visitRest(ctx.parent,(ReflexParser.TimeoutFunctionContext)ctx);
        }
        if (ctx instanceof ReflexParser.StateContext){
            return visitStateRest((ReflexParser.StateContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.ProcessContext){
            return visitProcessRest((ReflexParser.ProcessContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.ProgramContext){
            return visitProgramRest((ReflexParser.ProgramContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.TimeoutFunctionContext){
            return visitTimeoutRest((ReflexParser.TimeoutFunctionContext)ctx,childCtx);
        }
        throw new RuntimeException("No branch in visitRest");
    }

    private GenReturn visitCompoundStRest(ReflexParser.CompoundStContext ctx, ParserRuleContext childCtx) {
        List<ReflexParser.StatementContext> sts = ctx.compoundStatement().statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            GenReturn ret = visitStatement(sts.get(i));
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitCompoundStatementRest(ReflexParser.CompoundStatementContext ctx, ParserRuleContext childCtx){
        return visitCompoundStRest((ReflexParser.CompoundStContext)ctx.parent,childCtx);
    }

    private GenReturn visitStatementSeqRest(ReflexParser.StatementSeqContext ctx,ParserRuleContext childCtx) {
        List<ReflexParser.StatementContext> sts = ctx.statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            GenReturn ret = visitStatement(sts.get(i));
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }

        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitIfElseStRest(ReflexParser.IfElseStContext ctx, ParserRuleContext childCtx){
        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitIfElseStatRest(ReflexParser.IfElseStatContext ctx, ParserRuleContext childCtx){
        return visitRest(ctx.parent.parent,ctx);
    }

    private GenReturn visitSwitchOptionStatSeqRest(ReflexParser.SwitchOptionStatSeqContext ctx, ParserRuleContext childCtx){
        List<ReflexParser.StatementContext> sts = ctx.statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            GenReturn ret = visitStatement(sts.get(i));
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }

        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitSwitchStRest(ReflexParser.SwitchStContext ctx, ParserRuleContext childCtx) {
        List<ReflexParser.CaseStatContext> cases = ctx.switchStat().options;
        int i=0;
        for(;i<cases.size();i++){
            if (cases.get(i).switchOptionStatSeq()==childCtx){
                break;
            }
        }
        if (cases.get(i).switchOptionStatSeq().break_!=null){
            GenReturn ret = visitRest(ctx.parent,ctx);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        boolean breakDef = false;
        i++;
        for(;i<cases.size();i++){
            GenReturn ret = visitSwitchOptionStatSeq(cases.get(i).switchOptionStatSeq());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
            if (cases.get(i).switchOptionStatSeq().break_!=null){
                breakDef=true;
                break;
            }
        }
        if(!breakDef && ctx.switchStat().defaultOption!=null){
            GenReturn ret = visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }

        return visitRest(ctx.parent.parent,ctx);
    }

    private GenReturn visitTimeoutRest(ReflexParser.TimeoutFunctionContext ctx, ParserRuleContext childCtx) {
        GenReturn ret = visitTimeoutFunction(ctx);
        if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
            return ret;
        return visitRest(ctx.parent.parent,ctx);
    }

    private GenReturn visitStateRest(ReflexParser.StateContext ctx, ParserRuleContext childCtx) {
        if (ctx.timeoutFunction()!=null){
            GenReturn ret = visitTimeoutFunction(ctx.timeoutFunction());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return visitProcessRest((ReflexParser.ProcessContext)ctx.parent,ctx);
    }

    private GenReturn visitProcessRest(ReflexParser.ProcessContext ctx, ParserRuleContext childCtx) {
        formula.addConjunct(new UnmarkReset());
        return visitProgramRest((ReflexParser.ProgramContext)ctx.parent,ctx);
    }

    private GenReturn visitProgramRest(ReflexParser.ProgramContext ctx, ParserRuleContext childCtx) {
        List<ReflexParser.ProcessContext> processes = ctx.processes;
        int i=0;
        for (;i<processes.size();i++){
            if (processes.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<processes.size();i++){
            GenReturn ret =  visitProcess(processes.get(i));
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }

        return new GenReturn(ReturnType.Normal);
    }

    private GenReturn visitMiss(BranchPoint point) {
        this.stateCount = point.stateCount;
        this.currentProcess = point.processName;
        this.currentState = point.stateName;
        this.formula.trimByFormula(point.formula);
        path.trimBy(point.attributed);

        if(point.ifCtx!=null){
                return visitIfElseMiss(point.ifCtx,point.branch);
        }
        else if(point.switchCtx!=null){
                return visitSwitchMiss(point.switchCtx,point.branch);
        }
        else if(point.timeoutCtx!=null){
                return visitTimeoutMiss(point.timeoutCtx,point.branch);
        }
        else if(point.processCtx!=null){
                return visitProcessMiss(point.processCtx,point.branch);
        }
        else{
            throw new RuntimeException("undefined branch point");
        }
    }

    private GenReturn visitIfElseMiss(ReflexParser.IfElseStContext ctx, int i)  {
        ReflexParser.ExpressionContext e=ctx.ifElseStat().cond;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        exp.actuate(stateName());
        if (i==1){
            IfElseCore attr1 = (IfElseCore)collector.getAttributes(ctx.ifElseStat());
            //If no FalseAttributes then no else branch
            if(attr1.getFalseAttributes()!=null){
                path.add(attr1.getFalseAttributes());
                formula.addConjunct(new EqualityFormula(stateIfName(), true, exp, new RawExpression("False")));
                GenReturn ret = visitStatement(ctx.ifElseStat().else_);
                if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                    return ret;
            } else{
                formula.addConjunct(new EqualityFormula(stateIfName(), true, exp, new RawExpression("False")));
            }
        }else{
            IfElseCore attr1 = (IfElseCore)collector.getAttributes(ctx.ifElseStat());
            path.add(attr1.getTrueAttributes());
            formula.addConjunct(new EqualityFormula(stateIfName(),true,exp,new RawExpression("True")));
            GenReturn ret = visitStatement(ctx.ifElseStat().then);
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitSwitchMiss(ReflexParser.SwitchStContext ctx, int i) {
        ReflexParser.ExpressionContext e=ctx.switchStat().expr;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        exp.actuate(stateName());
        boolean breakDef=false;
        if(i!=-1) {
            SwitchCaseCore attr1 = (SwitchCaseCore)collector.getAttributes(ctx.switchStat());
            path.add(attr1.getBranchAttributes().get(i));
            for (int j = 0; j < i; j++) {
                SymbolicExpression subExp = vis.visitExpression(ctx.switchStat().options.get(j).option).expr;
                formula.addConjunct(new EqualityFormula(stateBranchName(j) + "_neg", false, exp, subExp));
            }
            SymbolicExpression subExp = vis.visitExpression(ctx.switchStat().options.get(i).option).expr;
            formula.addConjunct(new EqualityFormula(stateBranchName(i) + "_neg", false, exp, subExp));
            for (; i < ctx.switchStat().options.size(); i++) {
                ReflexParser.CaseStatContext caseI = ctx.switchStat().options.get(i);
                GenReturn ret = visitSwitchOptionStatSeq(caseI.switchOptionStatSeq());
                if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                    return ret;
                if (!(caseI.switchOptionStatSeq().break_ == null)) {
                    breakDef = true;
                    break;
                }
            }
            if (!breakDef && ctx.switchStat().defaultOption != null) {
                GenReturn ret = visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
                if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                    return ret;
            }
        }else{
            SwitchCaseCore attr1 = (SwitchCaseCore)collector.getAttributes(ctx.switchStat());
            path.add(attr1.getDefaultBranchAttributes());
            for (int j = 0; j < ctx.switchStat().options.size(); j++) {
                SymbolicExpression subExp = vis.visitExpression(ctx.switchStat().options.get(j).option).expr;
                formula.addConjunct(new EqualityFormula(stateBranchName(j) + "_neg", false, exp, subExp));
            }
            GenReturn ret = visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }
        return visitRest(ctx.parent,ctx);
    }
    private GenReturn visitTimeoutMiss(ReflexParser.TimeoutFunctionContext ctx, int i)  {
        SymbolicExpression exp;
        if(ctx.timeAmountOrRef().time!=null){
            exp = new ConstantExpression(ctx.timeAmountOrRef().time.getText(),new TimeType());
        } else if (ctx.timeAmountOrRef().intTime!=null){
            exp = new ConstantExpression(ctx.timeAmountOrRef().intTime.getText(),new IntType());
        } else{
            String id = ctx.timeAmountOrRef().ref.getText();
            ExprType t;
            if (mapper.is_variable(currentProcess,id)){
                t= mapper.variableType(currentProcess,id);
                VariableExpression subExp = new VariableExpression(ctx.timeAmountOrRef().ref.getText(),t,true);
                subExp.actuate(stateName());
                exp = subExp;
            }else if (mapper.is_const(id)){
                t= mapper.constantType(id);
                exp = new ConstantExpression(mapper.constantValue(id),t);
            } else{
                t = new IntType();
                VariableExpression subExp = new VariableExpression(ctx.timeAmountOrRef().ref.getText(),t,true);
                subExp.actuate(stateName());
                exp = subExp;
            }
        }

        if (i==0) {
            if (!checker.checkTimeout(path)) return new GenReturn(ReturnType.ImpossibleVC);
            formula.addConjunct(new GreaterFormula(
                    stateTimeoutName(currentState),
                    false,
                    exp.toString(),
                    ltime(stateName(), currentProcess)
            ));
            GenReturn ret = visitStatement(ctx.body);
            if (ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
        }else{
            formula.addConjunct(new GreaterFormula(
                    stateTimeoutName(currentState),
                    true,
                    exp.toString(),
                    ltime(stateName(),currentProcess)
            ));
        }
        return visitRest(ctx.parent.parent,ctx);
    }
    private GenReturn visitProcessMiss(ReflexParser.ProcessContext ctx, int i) {
        String processName = ctx.name.getText();
        if (i==-2){
            ProcessAttributes attr1 = (ProcessAttributes)collector.getAttributes(ctx);
            attr1.setState("error");
            path.add(attr1);
            if(!checker.checkRules(path,attr1))return new GenReturn(ReturnType.ImpossibleVC);

            formula.addConjunct(new EqualityFormula(
                    stateProcessStateName(currentProcess),
                    true,
                    new RawExpression(getPstate(stateName(),currentProcess)),
                    new RawExpression("''error''")));
        }else if (i==-1){
            ProcessAttributes attr1 = (ProcessAttributes)collector.getAttributes(ctx);
            attr1.setState("break");
            path.add(attr1);
            if(!checker.checkRules(path,attr1))return new GenReturn(ReturnType.ImpossibleVC);

            formula.addConjunct(new EqualityFormula(
                    stateProcessStateName(currentProcess),
                    true,
                    new RawExpression(getPstate(stateName(),currentProcess)),
                    new RawExpression("''stop''")));
        }else{
            formula.addConjunct(new EqualityFormula(
                    stateProcessStateName(currentProcess),
                    true,
                    new RawExpression(getPstate(stateName(),currentProcess)),
                    new RawExpression("''"+metaData.stateByIdx(currentProcess,i)+"''")));
            GenReturn ret = visitState(ctx.states.get(i));
            if(ret.getReturnType().equals(ReturnType.ImpossibleVC))
                return ret;
            if(formula.isMarkedReset()) {
                String setP = setPstate(stateName(), processName, "''"+metaData.startState(processName)+"''");
                stateCount++;
                formula.addConjunct(new StateFormula(stateName(), setP));
            }
        }
        return visitRest(ctx.parent,ctx);
    }

    public void visitStack(){
        while (!branchStack.isEmpty()){
            GenReturn ret = visitMiss(branchStack.pop());
            if(ret.getReturnType().equals(ReturnType.Normal)){
                String toE = toEnv(stateName());
                stateCount++;
                formula.addConjunct(new StateFormula(stateName(),toE));
                ImplicationFormula f = buildImplication(formula);
                finishVC(f);
            }
        }
    }

    private String constructProcessVariableName(String processName,String variable){
        return "_p_"+processName+"_v_"+variable;
    }
    private String stateName(){
        return "st"+stateCount;
    }
    private String inv(String stateName){
        return "(inv " +stateName+")";
    }
    private String toEnv(String stateName){
        return "(toEnv "+stateName+")";
    }
    private String stateIfName(){
        String name ="st"+stateCount+"_if";
        Integer current = ifCounter.getOrDefault(name,0);
        ifCounter.put(name,current+1);
        name = name+current;
        return name;
    }
    private String stateBranchName(int branch){
        return "st"+stateCount+"_branch"+branch;
    }
    private String stateTimeoutName(String stateName){
        return "st"+stateCount+"_"+stateName+"_timeout";
    }
    private String stateProcessStateName(String processName){
        return "st"+stateCount+"_"+processName+"_state";
    }
    private String setPstate(String stateName,String processName,String processStateName){
        return "(setPstate "+stateName+" ''"+processName+"'' ''"+processStateName+"'')";
    }
    private String getPstate(String stateName,String processName){
        return "(getPstate "+stateName+" ''"+processName+"'')";
    }
    private String reset(String stateName,String processName){
        return "(reset "+stateName+" ''"+processName+"'')";
    }

    private String ltime(String stateName,String processName){
        return "(ltime "+stateName+" ''"+processName+"'')";
    }

    private ImplicationFormula buildImplication(ConjuctionFormula conjuctionFormula){
        conjuctionFormula.addConjunct(new StateFormula("st_final",stateName()));
        return new ImplicationFormula(conjuctionFormula,new RawFormula(inv("st_final")));
    }

    public void finishVC(ImplicationFormula formula){
        conditionsGenerated++;
        printer.printVC(formula);
    }
}
