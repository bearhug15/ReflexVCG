package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ValueParser;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.VariableMapper;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.*;
import su.nsk.iae.reflex.expression.types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpressionVisitor1 extends ReflexBaseVisitor<ExprGenRes1> implements ExpressionVisitor{
    final VariableMapper mapper;
    final String process;
    final String state;
    final IStatementCreator creator;

    public ExpressionVisitor1(VariableMapper mapper, String processName, String state, IStatementCreator creator){
        this.mapper = mapper;
        this.process = processName;
        this.state = state;
        this.creator = creator;
    }

    @Override
    public ExprGenRes1 visitCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx) {
        String proc = ctx.processId.getText();
        String procState = ctx.qual.getText();
        CheckStateExpression exp = new CheckStateExpression(proc,procState,state);
        return new ExprGenRes1(exp,state);
    }

    @Override
    public ExprGenRes1 visitId(ReflexParser.IdContext ctx){
        String id = ctx.ID().toString();
        if (mapper.is_const(id)){
            SymbolicExpression expr = new ConstantExpression(mapper.constantValue(id),mapper.constantType(id));
            return new ExprGenRes1(expr,state);
        }
        if (mapper.is_variable(process,id)){
            String variable = mapper.mapVariable(process,id);
            SymbolicExpression expr = new VariableExpression(variable,mapper.variableType(process,id),true);
            return new ExprGenRes1(expr,state);
        }
        //TODO enums
        throw new RuntimeException("Unknown variable ID");
    }
    @Override
    public ExprGenRes1 visitInteger(ReflexParser.IntegerContext ctx) {
        String val;
        if (ctx.INTEGER()!=null){
            val = ctx.INTEGER().getText();
        } else {
            val = ctx.UNSIGNED_INTEGER().getText();
        }
        String value = ValueParser.parseInteger(val);
        SymbolicExpression expr;
        if (val.contains("u") || val.contains("U")){
            expr = new ConstantExpression(value, new NatType());
        }else{
            expr = new ConstantExpression(value, new IntType());
        }
        return new ExprGenRes1(expr,state);
    }

    @Override
    public ExprGenRes1 visitFloat(ReflexParser.FloatContext ctx) {
        String val =  ctx.FLOAT().getText();
        String value = ValueParser.parseFloat(val);
        SymbolicExpression expr = new ConstantExpression(value,new FloatType());
        return new ExprGenRes1(expr,state);
    }

    @Override
    public ExprGenRes1 visitBool(ReflexParser.BoolContext ctx) {
        String val = ctx.BOOL_VAL().getText();
        val = val.substring(0, 1).toUpperCase() + val.substring(1);
        SymbolicExpression expr = new ConstantExpression(val,new BoolType());
        return new ExprGenRes1(expr,state);
    }

    @Override
    public ExprGenRes1 visitTime(ReflexParser.TimeContext ctx) {
        String val =  ctx.TIME().getText();
        String value = ValueParser.parseTime(val);
        SymbolicExpression expr = new ConstantExpression(value,new TimeType());
        return new ExprGenRes1(expr,state);
    }

    @Override
    public ExprGenRes1 visitClosedExpression(ReflexParser.ClosedExpressionContext ctx) {
        return super.visitClosedExpression(ctx);
    }

    @Override
    public ExprGenRes1 visitPrimaryExpr(ReflexParser.PrimaryExprContext ctx) {
        return visitPrimaryExpression(ctx.primaryExpression());
    }

    @Override
    public ExprGenRes1 visitFuncCallExpr(ReflexParser.FuncCallExprContext ctx) {
        //TODO not implemenated
        throw  new RuntimeException("Function call not implemented");
        //return super.visitFuncCallExpr(ctx);
    }

    @Override
    public ExprGenRes1 visitPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx) {
        String op = ctx.postfixOp().op.getText();
        String id = ctx.postfixOp().varId.getText();
        String variable = mapper.mapVariable(process,id);

        ExprType type = mapper.variableType(process,id);
        String get;
        if (op.equals("++")){
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1",BinaryOp.Sum);
            //get = StringUtils.constructGetter(type,state,variable)+"+("+type.toString(creator)+"1)";
        } else{
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1",BinaryOp.Sub);
            //get =StringUtils.constructGetter(type,state,variable)+"-("+type.toString(creator)+"1)";
        }
        //String newState = StringUtils.constructSetter(type,state,variable,get);
        String newState = creator.Setter(type,state,variable,get);
        SymbolicExpression expr = new VariableExpression(variable,type,true);
        expr.actuate(state, creator);
        return new ExprGenRes1(expr,newState,creator.True());
    }

    @Override
    public ExprGenRes1 visitInfixOpExpr(ReflexParser.InfixOpExprContext ctx) {
        String op = ctx.infixOp().op.getText();
        String id = ctx.infixOp().varId.getText();
        String variable = mapper.mapVariable(process,id);

        ExprType type = mapper.variableType(process,id);
        String get;
        if (op.equals("++")){
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1",BinaryOp.Sum);
            //get = StringUtils.constructGetter(type,state,variable)+"+("+type.toString(creator)+"1)";
        } else{
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1",BinaryOp.Sub);
            //get =StringUtils.constructGetter(type,state,variable)+"-("+type.toString(creator)+"1)";
        }
        //String newState = StringUtils.constructSetter(type,state,variable,get);
        String newState = creator.Setter(type,state,variable,get);
        SymbolicExpression expr = new VariableExpression(variable,type,true);
        return new ExprGenRes1(expr,newState,creator.True());
    }

    @Override
    public ExprGenRes1 visitUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx) {
        String op = ctx.unaryOp().getText();
        ExprGenRes1 res = visitExpression(ctx.expression());
        SymbolicExpression expr = res.getExpr();
        String newState = res.getState();
        Optional<String> domain = res.getDomain();
        UnaryOp unOp = switch (op) {
            case "+" -> UnaryOp.Plus;
            case "-" -> UnaryOp.Minus;
            case "!" -> UnaryOp.Neg;
            case "~" -> UnaryOp.Invert;
            default -> throw new RuntimeException("Unknown unary op");
        };
        //SymbolicExpression newExpr = new UnaryExpression(unOp,expr,expr.exprType());
        SymbolicExpression newExpr = new UnaryExpression(unOp,expr,expr.exprType());
        return new ExprGenRes1(newExpr,newState,domain);
    }

    @Override
    public ExprGenRes1 visitCast(ReflexParser.CastContext ctx) {
        ExprType castType = TypeUtils.defineType(ctx.varType.getText());
        ExprGenRes1 res = visitExpression(ctx.expression());
        SymbolicExpression expr = res.getExpr();
        String newState = res.getState();
        Optional<String> domain = res.getDomain();
        expr.actuate(newState,creator);
        SymbolicExpression newExpr = new CastExpression(expr, castType);
        return new ExprGenRes1(newExpr,newState,domain);
    }

    @Override
    public ExprGenRes1 visitAdd(ReflexParser.AddContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.addOp().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitShift(ReflexParser.ShiftContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.SHIFT_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitBitOr(ReflexParser.BitOrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_OR_OP().getText();
        BinaryOp binOp= BinaryOp.BitOr;

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitOr(ReflexParser.OrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.OR_OP().getText();
        BinaryOp binOp= BinaryOp.Or;

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitMul(ReflexParser.MulContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.MUL_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if (binOp.equals(BinaryOp.Div) || binOp.equals(BinaryOp.Mod)) {
            domains.add(creator.BinaryExpression(expr2.toString(creator),"0",BinaryOp.NotEq));
        }
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitCheckState(ReflexParser.CheckStateContext ctx) {
        return visitCheckStateExpression(ctx.checkStateExpression());
    }

    @Override
    public ExprGenRes1 visitUnary(ReflexParser.UnaryContext ctx) {
        return visitUnaryExpression(ctx.unaryExpression());
    }

    @Override
    public ExprGenRes1 visitBitXor(ReflexParser.BitXorContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_XOR_OP().getText();
        BinaryOp binOp= BinaryOp.BitXor;

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitEqual(ReflexParser.EqualContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.EQ_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitAnd(ReflexParser.AndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.AND_OP().getText();
        BinaryOp binOp= BinaryOp.And;

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitBitAnd(ReflexParser.BitAndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_AND_OP().getText();
        BinaryOp binOp= BinaryOp.BitAnd;

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    public ExprGenRes1 visitSimpleAssign(ReflexParser.AssignContext ctx){
        BinaryOp binOp = BinaryOp.Eq;
        ReflexParser.ExpressionContext ctx1 = ctx.expression();
        String id = ctx.ID().getText();

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        expr1.actuate(newState1, creator);

        String variable = mapper.mapVariable(process,id);
        ExprType type = mapper.variableType(process,id);

        CastExpression expr = new CastExpression(expr1, type);
        String newState = creator.Setter(type,newState1,variable,expr.toString(creator));
        //String newState = StringUtils.constructSetter();

        SymbolicExpression newExpr = new VariableExpression(variable,type,true);
        return new ExprGenRes1(newExpr,newState,domain1);
    }

    @Override
    public ExprGenRes1 visitAssign(ReflexParser.AssignContext ctx) {
        String op = ctx.assignOp().getText();
        if (op.equals("=")){
            return visitSimpleAssign(ctx);
        }
        BinaryOp binOp = BinaryOp.getAssignSubOp(op);
        String id = ctx.ID().getText();
        String variable = mapper.mapVariable(process,id);

        ExprGenRes1 res1 = visitExpression(ctx.expression());
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprType lastCast = mapper.variableType(process,id);
        ExprType cast = TypeUtils.castType(expr1.exprType(),lastCast,binOp);
        if (!TypeUtils.isPossible(expr1.exprType(),cast,binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),cast));
        }
        ExprType resType = lastCast;

        String getter = creator.Getter(resType,newState1,variable);
        expr1.actuate(newState1, creator);
        SymbolicExpression assignedExpr = new BinaryExpression(binOp,
                new CastExpression(new ConstantExpression(getter,resType), cast),
                new CastExpression(expr1, cast),
                resType);
        String newState = creator.Getter(resType,newState1,(new CastExpression(assignedExpr, lastCast)).toString(creator));

        SymbolicExpression newExpr = new VariableExpression(variable,resType,true);
        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        if (binOp.equals(BinaryOp.Div) || binOp.equals(BinaryOp.Mod)) {
            domains.add(creator.BinaryExpression(expr1.toString(creator),"0",BinaryOp.NotEq));
        }
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState);
        }else{
            return new ExprGenRes1(newExpr,newState,creator.Conjunction(domains));
        }
    }

    @Override
    public ExprGenRes1 visitCompare(ReflexParser.CompareContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.COMP_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes1 res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Optional<String> domain1 = res1.getDomain();

        ExprGenRes1 res2 = (new ExpressionVisitor1(mapper,process,newState1,creator)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Optional<String> domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2, creator);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(expr1, cast);
        }
        expr2.actuate(newState2, creator);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(expr2, cast);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ArrayList<String> domains = new ArrayList<>();
        domain1.ifPresent(domains::add);
        domain2.ifPresent(domains::add);
        if(domains.isEmpty()){
            return new ExprGenRes1(newExpr,newState2);
        }else{
            return new ExprGenRes1(newExpr,newState2,creator.Conjunction(domains));
        }
    }

    public ExprGenRes1 visitPrimaryExpression(ReflexParser.PrimaryExpressionContext ctx){
        if (ctx instanceof ReflexParser.IdContext)
            return visitId((ReflexParser.IdContext)ctx);
        if (ctx instanceof ReflexParser.IntegerContext)
            return visitInteger((ReflexParser.IntegerContext)ctx);
        if (ctx instanceof ReflexParser.FloatContext)
            return visitFloat((ReflexParser.FloatContext)ctx);
        if (ctx instanceof ReflexParser.BoolContext)
            return visitBool((ReflexParser.BoolContext)ctx);
        if (ctx instanceof ReflexParser.TimeContext)
            return visitTime((ReflexParser.TimeContext)ctx);
        if (ctx instanceof ReflexParser.ClosedExpressionContext)
            return visitClosedExpression((ReflexParser.ClosedExpressionContext)ctx);

        return null;
    }
    public ExprGenRes1 visitUnaryExpression(ReflexParser.UnaryExpressionContext ctx){
        if (ctx instanceof ReflexParser.PostfixOpExprContext)
            return visitPostfixOpExpr((ReflexParser.PostfixOpExprContext) ctx);
        if (ctx instanceof ReflexParser.InfixOpExprContext)
            return visitInfixOpExpr((ReflexParser.InfixOpExprContext) ctx);
        if (ctx instanceof ReflexParser.PrimaryExprContext)
            return visitPrimaryExpr((ReflexParser.PrimaryExprContext) ctx);
        if (ctx instanceof ReflexParser.UnaryOpExprContext)
            return visitUnaryOpExpr((ReflexParser.UnaryOpExprContext) ctx);
        if (ctx instanceof ReflexParser.FuncCallExprContext)
            return visitFuncCallExpr((ReflexParser.FuncCallExprContext) ctx);

        return null;
    }
    public ExprGenRes1 visitExpression(ReflexParser.ExpressionContext ctx){
        if (ctx instanceof ReflexParser.CastContext)
            return visitCast((ReflexParser.CastContext) ctx);
        if (ctx instanceof ReflexParser.AddContext)
            return visitAdd((ReflexParser.AddContext) ctx);
        if (ctx instanceof ReflexParser.ShiftContext)
            return visitShift((ReflexParser.ShiftContext) ctx);
        if (ctx instanceof ReflexParser.BitOrContext)
            return visitBitOr((ReflexParser.BitOrContext) ctx);
        if (ctx instanceof ReflexParser.OrContext)
            return visitOr((ReflexParser.OrContext) ctx);
        if (ctx instanceof ReflexParser.MulContext)
            return visitMul((ReflexParser.MulContext) ctx);
        if (ctx instanceof ReflexParser.CheckStateContext)
            return visitCheckState((ReflexParser.CheckStateContext) ctx);
        if (ctx instanceof ReflexParser.UnaryContext)
            return visitUnary((ReflexParser.UnaryContext) ctx);
        if (ctx instanceof ReflexParser.BitXorContext)
            return visitBitXor((ReflexParser.BitXorContext) ctx);
        if (ctx instanceof ReflexParser.EqualContext)
            return visitEqual((ReflexParser.EqualContext) ctx);
        if (ctx instanceof ReflexParser.AndContext)
            return visitAnd((ReflexParser.AndContext) ctx);
        if (ctx instanceof ReflexParser.BitAndContext)
            return visitBitAnd((ReflexParser.BitAndContext) ctx);
        if (ctx instanceof ReflexParser.AssignContext)
            return visitAssign((ReflexParser.AssignContext) ctx);
        if (ctx instanceof ReflexParser.CompareContext)
            return visitCompare((ReflexParser.CompareContext) ctx);

        return null;
    }

    @Override
    public List<ExprRes> parseExpression(ReflexParser.ExpressionContext expr) {
        return List.of(visitExpression(expr));
    }
}
