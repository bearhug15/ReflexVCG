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

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

public class VCGenerator extends ReflexBaseVisitor<Void> {

    VariableMapper mapper;
    ConjuctionFormula formula;

    Integer stateCount;
    String currentProcess;
    String currentState;
    ProgramMetaData metaData;

    Stack<BranchPoint> branchStack;
    VCPrinter printer;
    ProcessStateTraces traces;
    HashMap<String,Integer> ifCounter;
    public VCGenerator(){
        stateCount = 0;
        formula = new ConjuctionFormula();
        mapper = new VariableMapper();
        branchStack = new Stack<>();
        metaData= new ProgramMetaData();
        traces = new ProcessStateTraces();
        ifCounter = new HashMap<>();
    }

    public void test(){
        //String sourceName = source.getFileName().toString();
        printer = new VCPrinter(Path.of("./"),"test",metaData);
        CharStream inputStream = CharStreams.fromString("program Dryer {\n" +
                "clock 100;\n" +
                "input inp 0x00 0x00 8;\n" +
                "output out 0x00 0x01 8;\n" +
                "const bool ON = true;\n" +
                "const bool OFF = false;\n" +
                "const time TIMEOUT = 0t2s;\n" +
                "process Dryer {\n" +
                "bool hands_under_dryer = inp[1];\n" +
                "bool dryer_control = out[1];\n" +
                "state Wait {\n" +
                "if (hands_under_dryer) {\n" +
                "dryer_control = ON;\n" +
                "set state Work;\n" +
                "}\n" +
                "}\n" +
                "state Work {\n" +
                "if (hands_under_dryer) reset timer;timeout (TIMEOUT) {\n" +
                "dryer_control = OFF;\n" +
                "set state Wait;\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}");
        try {
            ReflexLexer lexer = new ReflexLexer(inputStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            prepareVariableMapper(context);
            prepareMetaData(context);

            visitProgram(context);
            visitStack();

        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
    public void generateVC(Path source, Path destination){
        String sourceName = source.getFileName().toString();
        printer = new VCPrinter(destination,sourceName,metaData);

        try {
            FileInputStream fileInput = new FileInputStream(source.toFile());
            ANTLRInputStream input = new ANTLRInputStream(fileInput);
            ReflexLexer lexer = new ReflexLexer(input);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            ReflexParser parser = new ReflexParser(tokenStream);
            ReflexParser.ProgramContext context = parser.program();

            prepareVariableMapper(context);
            prepareMetaData(context);

            analyzeProgram(context);

            visitProgram(context);
            visitStack();


        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    private void analyzeProgram(ReflexParser.ProgramContext ctx){
        ProgramAnalyzer analyzer = new ProgramAnalyzer(traces,metaData);
        analyzer.visitProgram(ctx);
    }

    private void prepareVariableMapper(ReflexParser.ProgramContext ctx){
        for (ReflexParser.GlobalVariableContext variable: ctx.globalVars){
            if(variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext progVariable = variable.programVariable();
                String name = progVariable.name.getText();
                mapper.addGlobalVariable(name,"_"+name,TypeUtils.defineType(progVariable.varType.getText()));
            }else{
                ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                String name = physVariable.name.getText();
                ReflexParser.PortMappingContext pmap = physVariable.mapping;
                String portId = pmap.portId.getText();
                String qualName = portId;
                if (pmap.bit!=null){
                    qualName += "_"+pmap.bit.getText();
                }else{
                    qualName+= "_0";
                }
                for (ReflexParser.PortContext port:ctx.ports){
                    if (port.name.getText().equals(portId) && port.varType.getText().equals("input")){
                        metaData.addInputVariable(qualName,TypeUtils.defineType(physVariable.varType.getText()));
                    }
                }
                mapper.addGlobalVariable(
                        name,
                        qualName,
                        TypeUtils.defineType(physVariable.varType.getText()));
            }
        }
        for (ReflexParser.ConstContext con: ctx.consts){
            ExpressionVisitor vis = new ExpressionVisitor(mapper,"_",stateName());
            ExprGenRes res = vis.visitExpression(con.expression());
            mapper.addConstant(
                    con.name.getText(),
                    res.expr.toString(),
                    TypeUtils.defineType(con.varType.getText()));
        }

        for (ReflexParser.ProcessContext process: ctx.processes){
            for(ReflexParser.ProcessVariableContext variable: process.variables){
                String processName = process.name.getText();
                if(variable.programVariable()!=null){
                    ReflexParser.ProgramVariableContext progVariable = variable.programVariable();
                    String name = progVariable.name.getText();
                    mapper.addVariable(
                            processName,
                            name,
                            constructProcessVariableName(processName,name),
                            TypeUtils.defineType(progVariable.varType.getText()));
                }else{
                    ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                    String name = physVariable.name.getText();
                    ReflexParser.PortMappingContext pmap = physVariable.mapping;
                    String portId = pmap.portId.getText();
                    String qualName = portId;
                    if (pmap.bit!=null){
                        qualName += "_"+pmap.bit.getText();
                    }else{
                        qualName+= "_0";
                    }
                    for (ReflexParser.PortContext port:ctx.ports){
                        if (port.name.getText().equals(portId) && port.varType.getText().equals("input")){
                            metaData.addInputVariable(qualName,TypeUtils.defineType(physVariable.varType.getText()));
                        }
                    }
                    mapper.addVariable(
                            processName,
                            name,
                            qualName,
                            TypeUtils.defineType(physVariable.varType.getText()));
                }
            }
        }

        for (ReflexParser.ProcessContext process: ctx.processes){
            for(ReflexParser.ImportedVariableListContext imported: process.imports){
                String providerProcess = imported.processID.getText();
                String acceptorProcess = process.name.getText();
                for (Token id: imported.variables){
                    String name=id.getText();
                    mapper.addVariable(
                            acceptorProcess,
                            name,
                            mapper.mapVariable(providerProcess,name),
                            mapper.variableType(providerProcess,name));
                }
            }
        }
    }
    private void prepareMetaData(ReflexParser.ProgramContext ctx){
        String replacedState="#";
        String state = replacedState;
        for (ReflexParser.GlobalVariableContext variable: ctx.globalVars){
            if(variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext programVariable = variable.programVariable();
                String variableName = programVariable.name.getText();
                String processName = ctx.processes.get(0).name.getText();
                if(programVariable.expression()!=null){
                    ExpressionVisitor vis = new ExpressionVisitor(mapper,processName,state);
                    ExprGenRes res = vis.visitExpression(programVariable.expression());
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),res.getExpr().toString());
                }else{
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                }
            }
        }
        for(ReflexParser.ProcessContext procCtx: ctx.processes){
            List<String> stateNames= procCtx.states.stream()
                    .map(stateCtx->stateCtx.name.getText()).toList();
            metaData.addProcess(procCtx.name.getText(),stateNames);
            metaData.addInitializer(replacedState,procCtx.name.getText(),initializeProcess(procCtx,replacedState));
        }

        ReflexParser.ClockDefinitionContext clockCtx = ctx.clock;
        String value;
        if (clockCtx.intValue != null){
            value = StringUtils.parseInteger(clockCtx.intValue.getText());
        } else{
            value = StringUtils.parseTime(clockCtx.timeValue.getText());
        }
        metaData.setClockValue(value);
    }

    private String initializeProcess(ReflexParser.ProcessContext ctx,String stateReplace){
        String processName = ctx.name.getText();
        String state = stateReplace;
        for(ReflexParser.ProcessVariableContext variable: ctx.variables){
            if (variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext programVariable = variable.programVariable();
                String variableName = programVariable.name.getText();
                if(programVariable.expression()!=null){
                    ExpressionVisitor vis = new ExpressionVisitor(mapper,processName,state);
                    ExprGenRes res = vis.visitExpression(programVariable.expression());
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),res.getExpr().toString());
                }else{
                    ExprType ty = mapper.variableType(processName,variableName);
                    state = StringUtils.constructSetter(ty,state,mapper.mapVariable(processName,variableName),ty.defaultValue());
                }
            }
        }
        return state;
    }

    private Void initializeInputVariables(){
        for (Map.Entry<String,ExprType> variable: metaData.getInputVariablesNames().entrySet()){
            String setter = StringUtils.constructSetter(variable.getValue(),stateName(),variable.getKey(),variable.getKey());
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),setter));
        }
        return null;
    }
    @Override
    public Void visitProgram(ReflexParser.ProgramContext ctx) {
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

        ImplicationFormula f1 = new ImplicationFormula(formula,new RawFormula(inv(stateName())));
        finishVC(f1);

        stateCount=0;
        formula = new ConjuctionFormula();
        formula.addConjunct(new RawFormula("base_inv",inv(stateName())));
        initializeInputVariables();
        for(ReflexParser.ProcessContext cont: ctx.processes){
            visitProcess(cont);
        }
        toE = toEnv(stateName());
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),toE));
        ImplicationFormula f2 = new ImplicationFormula(formula,new RawFormula(inv(stateName())));
        finishVC(f2);

        return null;
    }

    @Override
    public Void visitProcess(ReflexParser.ProcessContext ctx) {
        currentProcess = ctx.name.getText();

        if (traces.isReachable(currentProcess,"error")) {
            branchStack.push(new BranchPoint(
                    formula.peekLastConjunct(),
                    ctx,
                    -2,
                    stateCount,
                    currentProcess,
                    currentState));
        }
        if (traces.isReachable(currentProcess,"stop")) {
            branchStack.push(new BranchPoint(
                    formula.peekLastConjunct(),
                    ctx,
                    -1,
                    stateCount,
                    currentProcess,
                    currentState));
        }
        for(int i=ctx.states.size()-1;i>0;i--){
            if (traces.isReachable(currentProcess,ctx.state(i).name.getText())) {
                branchStack.push(new BranchPoint(
                        formula.peekLastConjunct(),
                        ctx,
                        i,
                        stateCount,
                        currentProcess,
                        currentState));
            }
        }
        formula.addConjunct(new EqualityFormula(
                stateProcessStateName(currentProcess),
                true,
                new RawExpression(getPstate(stateName(),currentProcess)),
                new RawExpression("''"+metaData.stateByIdx(currentProcess,0)+"''")));
        visitState(ctx.states.get(0));
        if(formula.isMarkedReset()) {
            String setP = setPstate(stateName(), currentProcess, metaData.startState(currentProcess));
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(), setP));
        }
        return null;
    }

    @Override
    public Void visitState(ReflexParser.StateContext ctx) {
        currentState = ctx.name.getText();
        visitStatementSeq(ctx.stateFunction);

        if (ctx.func !=null){
            visitTimeoutFunction( ctx.func);
        }
        formula.addConjunct(new UnmarkSetState());
        return null;
    }

    @Override
    public Void visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx) {
        if (formula.isMarkedReset() || formula.isMarkedSetState()){
            return null;
        }
        branchStack.push(new BranchPoint(
                formula.peekLastConjunct(),
                ctx,
                1,
                stateCount,
                currentProcess,
                currentState));
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
        formula.addConjunct(new GreaterFormula(
                stateTimeoutName(currentState),
                false,
                exp.toString(),
                ltime(stateName(),currentProcess)
        ));
        visitStatement(ctx.body);
        return null;
    }

    @Override
    public Void visitExprSt(ReflexParser.ExprStContext ctx) {
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
        return null;
    }

    @Override
    public Void visitStatementSeq(ReflexParser.StatementSeqContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            visitStatement(cont);
        }
        return null;
    }

    @Override
    public Void visitCompoundStatement(ReflexParser.CompoundStatementContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            visitStatement(cont);
        }
        return null;
    }

    @Override
    public Void visitIfElseSt(ReflexParser.IfElseStContext ctx) {
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
                ctx,
                1,
                stateCount,
                currentProcess,
                currentState));


        formula.addConjunct(new EqualityFormula(stateIfName(),true, exp,new ConstantExpression("True",new BoolType())));

        visitStatement(ctx.ifElseStat().then);
        return null;
    }

    @Override
    public Void visitSwitchSt(ReflexParser.SwitchStContext ctx) {
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
        for(int i=numBranches;i>=1;i--){
            branchStack.push(new BranchPoint(
                    formula.peekLastConjunct(),
                    ctx,
                    i,
                    stateCount,
                    currentProcess,
                    currentState));
        }
        boolean breakDef=false;
        ReflexParser.CaseStatContext baseCase=ctx.switchStat().options.get(0);
        ReflexParser.ExpressionContext e0 = baseCase.option;
        ExpressionVisitor vis0 = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res0 = vis0.visitExpression(e);
        SymbolicExpression exp0 = res0.getExpr();
        if(!exp0.isActuated()){
            throw new RuntimeException("Using of non constant expression in case");
        }
        formula.addConjunct(new EqualityFormula(stateBranchName(0),true,exp,exp0));
        for (int i=0;i<numBranches;i++){
            ReflexParser.CaseStatContext caseI=ctx.switchStat().options.get(i);
            visitSwitchOptionStatSeq(caseI.switchOptionStatSeq());
            if(!(caseI.switchOptionStatSeq().break_==null)){
                breakDef=true;
                break;
            }
        }
        if(!breakDef && ctx.switchStat().defaultOption!=null){
            visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
        }
        return null;
    }

    @Override
    public Void visitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx) {
        for(ReflexParser.StatementContext cont:ctx.statements){
            visitStatement(cont);
        }
        return null;
    }

    @Override
    public Void visitDefaultStat(ReflexParser.DefaultStatContext ctx) {
        return super.visitDefaultStat(ctx);
    }

    @Override
    public Void visitStartProcStat(ReflexParser.StartProcStatContext ctx) {
        String id = ctx.processId.getText();
        if (id.equals(currentProcess)){
            formula.addConjunct(new MarkRestart());
        }else{
            String setP = setPstate(stateName(),id,metaData.startState(id));
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),setP));
        }
        return null;
    }

    @Override
    public Void visitStopProcStat(ReflexParser.StopProcStatContext ctx) {
        Token id = ctx.processId;
        String setP;
        if (id==null){
            setP = setPstate(stateName(),currentProcess,"''stop''");
            formula.addConjunct(new MarkSetState());
        }else{
            setP = setPstate(stateName(),id.getText(),"''stop''");
        }
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        return null;
    }

    @Override
    public Void visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx) {
        Token id = ctx.processId;
        String setP;
        if (id==null){
            setP = setPstate(stateName(),currentProcess,"''error''");
            formula.addConjunct(new MarkSetState());
        }else{
            setP = setPstate(stateName(),id.getText(),"''error''");
        }
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),setP));
        return null;
    }

    @Override
    public Void visitRestartStat(ReflexParser.RestartStatContext ctx) {
        formula.addConjunct(new MarkRestart());
        return null;
    }

    @Override
    public Void visitResetStat(ReflexParser.ResetStatContext ctx) {
        String res = reset(stateName(),currentProcess);
        formula.addConjunct(new MarkSetState());
        stateCount++;
        formula.addConjunct(new StateFormula(stateName(),res));
        return null;
    }

    @Override
    public Void visitSetStateStat(ReflexParser.SetStateStatContext ctx) {
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
        formula.addConjunct(new MarkSetState());
        return null;
    }

    public Void visitStatement(ReflexParser.StatementContext ctx){
        if (ctx instanceof ReflexParser.EmptyStContext){
            return null;
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

    private void visitRest(RuleContext ctx, ParserRuleContext childCtx){
        if (ctx instanceof ReflexParser.CompoundStContext){
            visitCompoundStRest((ReflexParser.CompoundStContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.CompoundStatementContext){
            visitCompoundStatementRest((ReflexParser.CompoundStatementContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.StatementSeqContext){
            visitStatementSeqRest((ReflexParser.StatementSeqContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.IfElseStContext){
            visitIfElseStRest((ReflexParser.IfElseStContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.IfElseStatContext){
            visitIfElseStRest((ReflexParser.IfElseStContext)ctx.parent,childCtx);
        }
        if (ctx instanceof ReflexParser.SwitchOptionStatSeqContext){
            visitSwitchOptionStatSeqRest((ReflexParser.SwitchOptionStatSeqContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.CaseStatContext){
            visitSwitchStRest((ReflexParser.SwitchStContext)ctx.parent.parent,childCtx);
        }
        if (ctx instanceof ReflexParser.DefaultStatContext){
            ParserRuleContext newChild = (ReflexParser.SwitchStContext)ctx.parent.parent;
            visitRest(newChild.parent,newChild);
        }
        if (ctx instanceof ReflexParser.TimeoutFunctionContext){
            visitRest(ctx.parent,(ReflexParser.TimeoutFunctionContext)ctx);
        }
        if (ctx instanceof ReflexParser.StateContext){
            visitStateRest((ReflexParser.StateContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.ProcessContext){
            visitProcessRest((ReflexParser.ProcessContext)ctx,childCtx);
        }
        if (ctx instanceof ReflexParser.ProgramContext){
            visitProgramRest((ReflexParser.ProgramContext)ctx,childCtx);
        }
    }

    private void visitCompoundStRest(ReflexParser.CompoundStContext ctx, ParserRuleContext childCtx){
        List<ReflexParser.StatementContext> sts = ctx.compoundStatement().statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            visitStatement(sts.get(i));
        }

        visitRest(ctx.parent,ctx);
    }
    private void visitCompoundStatementRest(ReflexParser.CompoundStatementContext ctx, ParserRuleContext childCtx){
        visitCompoundStRest((ReflexParser.CompoundStContext)ctx.parent,childCtx);
    }

    private void visitStatementSeqRest(ReflexParser.StatementSeqContext ctx,ParserRuleContext childCtx){
        List<ReflexParser.StatementContext> sts = ctx.statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            visitStatement(sts.get(i));
        }

        visitRest(ctx.parent,ctx);
    }
    private void visitIfElseStRest(ReflexParser.IfElseStContext ctx, ParserRuleContext childCtx){
        visitRest(ctx.parent,ctx);
    }
    private void visitIfElseStatRest(ReflexParser.IfElseStatContext ctx, ParserRuleContext childCtx){
        visitRest(ctx.parent.parent,ctx);
    }

    private void visitSwitchOptionStatSeqRest(ReflexParser.SwitchOptionStatSeqContext ctx, ParserRuleContext childCtx){
        List<ReflexParser.StatementContext> sts = ctx.statements;
        int i=0;
        for(;i<sts.size();i++){
            if (sts.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<sts.size();i++){
            visitStatement(sts.get(i));
        }

        visitRest(ctx.parent,ctx);
    }
    private void visitSwitchStRest(ReflexParser.SwitchStContext ctx, ParserRuleContext childCtx){
        List<ReflexParser.CaseStatContext> cases = ctx.switchStat().options;
        int i=0;
        for(;i<cases.size();i++){
            if (cases.get(i).switchOptionStatSeq()==childCtx){
                break;
            }
        }
        if (cases.get(i).switchOptionStatSeq().break_!=null){
            visitRest(ctx.parent,ctx);
        }
        boolean breakDef = false;
        i++;
        for(;i<cases.size();i++){
            visitSwitchOptionStatSeq(cases.get(i).switchOptionStatSeq());
            if (cases.get(i).switchOptionStatSeq().break_!=null){
                breakDef=true;
                break;
            }
        }
        if(!breakDef && ctx.switchStat().defaultOption!=null){
            visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
        }
    }

    private void visitTimeoutRest(ReflexParser.TimeoutFunctionContext ctx, ParserRuleContext childCtx){
        visitTimeoutFunction(ctx);
        visitRest(ctx.parent.parent,ctx);
    }

    private void visitStateRest(ReflexParser.StateContext ctx, ParserRuleContext childCtx){
        if (ctx.timeoutFunction()!=null){
            visitTimeoutRest(ctx.timeoutFunction(),null);
        }
        formula.addConjunct(new UnmarkSetState());
        visitProcessRest((ReflexParser.ProcessContext)ctx.parent,ctx);
    }

    private void visitProcessRest(ReflexParser.ProcessContext ctx, ParserRuleContext childCtx){
        formula.addConjunct(new UnmarkReset());
        visitProgramRest((ReflexParser.ProgramContext)ctx.parent,ctx);
    }

    private void visitProgramRest(ReflexParser.ProgramContext ctx, ParserRuleContext childCtx){
        List<ReflexParser.ProcessContext> processes = ctx.processes;
        int i=0;
        for (;i<processes.size();i++){
            if (processes.get(i)==childCtx){
                break;
            }
        }
        i++;
        for(;i<processes.size();i++){
            visitProcess(processes.get(i));
        }
    }

    private void visitMiss(BranchPoint point){
        this.stateCount = point.stateCount;
        this.currentProcess = point.processName;
        this.currentState = point.stateName;
        this.formula.trimByFormula(point.formula);
        if(point.ifCtx!=null){
            visitIfElseMiss(point.ifCtx,point.branch);
        }
        else if(point.switchCtx!=null){
            visitSwitchMiss(point.switchCtx,point.branch);
        }
        else if(point.timeoutCtx!=null){
            visitTimeoutMiss(point.timeoutCtx,point.branch);
        }
        else if(point.processCtx!=null){
            visitProcessMiss(point.processCtx,point.branch);
        }
        else{
            throw new RuntimeException("undefined branch point");
        }
    }

    private void visitIfElseMiss(ReflexParser.IfElseStContext ctx, int i){
        ReflexParser.ExpressionContext e=ctx.ifElseStat().cond;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        exp.actuate(stateName());
        if (i==1){
            formula.addConjunct(new EqualityFormula(stateIfName(), true, exp, new RawExpression("False")));
            if (ctx.ifElseStat().else_!=null) {
                visitStatement(ctx.ifElseStat().else_);
            }
        }else{
            formula.addConjunct(new EqualityFormula(stateIfName(),true,exp,new RawExpression("True")));
            visitStatement(ctx.ifElseStat().then);
        }
        visitRest(ctx.parent,ctx);
    }
    private void visitSwitchMiss(ReflexParser.SwitchStContext ctx, int i){
        ReflexParser.ExpressionContext e=ctx.switchStat().expr;
        ExpressionVisitor vis = new ExpressionVisitor(mapper,currentProcess,stateName());
        ExprGenRes res = vis.visitExpression(e);
        SymbolicExpression exp = res.getExpr();
        exp.actuate(stateName());
        boolean breakDef=false;
        for(int j=0;j<i;j++){
            SymbolicExpression subExp = vis.visitExpression(ctx.switchStat().options.get(j).option).expr;
            formula.addConjunct(new EqualityFormula(stateBranchName(j)+"_neg",false,exp,subExp));
        }
        SymbolicExpression subExp = vis.visitExpression(ctx.switchStat().options.get(i).option).expr;
        formula.addConjunct(new EqualityFormula(stateBranchName(i)+"_neg",false,exp,subExp));
        for (;i<ctx.switchStat().options.size();i++){
            ReflexParser.CaseStatContext caseI=ctx.switchStat().options.get(i);
            visitSwitchOptionStatSeq(caseI.switchOptionStatSeq());
            if(!(caseI.switchOptionStatSeq().break_==null)){
                breakDef=true;
                break;
            }
        }
        if(!breakDef && ctx.switchStat().defaultOption!=null){
            visitSwitchOptionStatSeq(ctx.switchStat().defaultOption.switchOptionStatSeq());
        }
        visitRest(ctx.parent,ctx);
    }
    private void visitTimeoutMiss(ReflexParser.TimeoutFunctionContext ctx, int i){
        if (formula.isMarkedReset() || formula.isMarkedSetState()){
            visitRest(ctx.parent.parent,ctx);
        }
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

        if (i==0){
            formula.addConjunct(new GreaterFormula(
                    stateTimeoutName(currentState),
                    false,
                    exp.toString(),
                    ltime(stateName(),currentProcess)
            ));
            visitStatement(ctx.body);
        }else{
            formula.addConjunct(new GreaterFormula(
                    stateTimeoutName(currentState),
                    true,
                    exp.toString(),
                    ltime(stateName(),currentProcess)
            ));
        }
        visitRest(ctx.parent.parent,ctx);
    }
    private void visitProcessMiss(ReflexParser.ProcessContext ctx, int i){
        String processName = ctx.name.getText();
        if (i==-2){
            formula.addConjunct(new EqualityFormula(
                    stateProcessStateName(currentProcess),
                    true,
                    new RawExpression(getPstate(stateName(),currentProcess)),
                    new RawExpression("''error''")));
        }else if (i==-1){
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
            visitState(ctx.states.get(i));
            if(formula.isMarkedReset()) {
                String setP = setPstate(stateName(), processName, "''"+metaData.startState(processName)+"''");
                stateCount++;
                formula.addConjunct(new StateFormula(stateName(), setP));
            }
        }
        visitRest(ctx.parent,ctx);
    }

    private void visitStack(){
        while (!branchStack.isEmpty()){
            visitMiss(branchStack.pop());
            String toE = toEnv(stateName());
            stateCount++;
            formula.addConjunct(new StateFormula(stateName(),toE));
            ImplicationFormula f = new ImplicationFormula(formula,new RawFormula(inv(stateName())));
            finishVC(f);
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

    public void finishVC(ImplicationFormula formula){
        printer.printVC(formula);
    }
}
