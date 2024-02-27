package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.*;
import su.nsk.iae.reflex.expression.types.*;
import su.nsk.iae.reflex.formulas.ConjuctionFormula;
import su.nsk.iae.reflex.formulas.EqualityFormula;
import su.nsk.iae.reflex.formulas.Formula;
import su.nsk.iae.reflex.formulas.TrueFormula;

public class ExpressionVisitor extends ReflexBaseVisitor<ExprGenRes> {
    final VariableMapper mapper;
    final String process;
    final String state;

    ExpressionVisitor(VariableMapper mapper, String processName, String state){
        this.mapper = mapper;
        this.process = processName;
        this.state = state;
    }

    @Override
    public ExprGenRes visitCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx) {
        String proc = ctx.processId.getText();
        String procState = ctx.stateId.getText();
        CheckStateExpression exp = new CheckStateExpression(proc,procState,state);
        return new ExprGenRes(exp,state, new TrueFormula());
    }

    @Override
    public ExprGenRes visitId(ReflexParser.IdContext ctx){
        String id = ctx.ID().toString();
        if (mapper.is_const(id)){
            SymbolicExpression expr = new ConstantExpression(mapper.constantValue(id),mapper.constantType(id));
            return new ExprGenRes(expr,state, new TrueFormula());
        }
        if (mapper.is_variable(process,id)){
            String variable = mapper.mapVariable(process,id);
            SymbolicExpression expr = new VariableExpression(variable,mapper.variableType(process,id),true);
            return new ExprGenRes(expr,state, new TrueFormula());
        }
        //TODO enums
        throw new RuntimeException("Unknown variable ID");
    }
    @Override
    public ExprGenRes visitInteger(ReflexParser.IntegerContext ctx) {
        String val = ctx.INTEGER().getText();
        String value = StringUtils.parseInteger(val);
        SymbolicExpression expr;
        if (val.contains("u") || val.contains("U")){
            expr = new ConstantExpression(value, new NatType());
        }else{
            expr = new ConstantExpression(value, new IntType());
        }
        return new ExprGenRes(expr,state,new TrueFormula());
    }

    @Override
    public ExprGenRes visitFloat(ReflexParser.FloatContext ctx) {
        String val =  ctx.FLOAT().getText();
        String value = StringUtils.parseFloat(val);
        SymbolicExpression expr = new ConstantExpression(value,new FloatType());
        return new ExprGenRes(expr,state,new TrueFormula());
    }

    @Override
    public ExprGenRes visitBool(ReflexParser.BoolContext ctx) {
        String val = ctx.BOOL_VAL().getText();
        val = val.substring(0, 1).toUpperCase() + val.substring(1);
        SymbolicExpression expr = new ConstantExpression(val,new BoolType());
        return new ExprGenRes(expr,state,new TrueFormula());
    }

    @Override
    public ExprGenRes visitTime(ReflexParser.TimeContext ctx) {
        String val =  ctx.TIME().getText();
        String value = StringUtils.parseTime(val);
        SymbolicExpression expr = new ConstantExpression(value,new TimeType());
        return new ExprGenRes(expr,state,new TrueFormula());
    }

    @Override
    public ExprGenRes visitClosedExpression(ReflexParser.ClosedExpressionContext ctx) {
        return super.visitClosedExpression(ctx);
    }

    @Override
    public ExprGenRes visitPrimaryExpr(ReflexParser.PrimaryExprContext ctx) {
        return visitPrimaryExpression(ctx.primaryExpression());
    }

    @Override
    public ExprGenRes visitFuncCallExpr(ReflexParser.FuncCallExprContext ctx) {
        //TODO not implemenated
        throw  new RuntimeException("Function call not implemented");
        //return super.visitFuncCallExpr(ctx);
    }

    @Override
    public ExprGenRes visitPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx) {
        String op = ctx.postfixOp().op.getText();
        String id = ctx.postfixOp().varId.getText();
        String variable = mapper.mapVariable(process,id);

        ExprType type = mapper.variableType(process,id);
        String get;
        if (op.equals("++")){
            get = StringUtils.constructGetter(type,state,variable)+"+("+type.toString()+"1)";
        } else{
            get =StringUtils.constructGetter(type,state,variable)+"-("+type.toString()+"1)";
        }
        String newState = StringUtils.constructSetter(type,state,variable,get);
        SymbolicExpression expr = new VariableExpression(variable,type,true);
        expr.actuate(state);
        return new ExprGenRes(expr,newState,new TrueFormula());
    }

    @Override
    public ExprGenRes visitInfixOpExpr(ReflexParser.InfixOpExprContext ctx) {
        String op = ctx.infixOp().op.getText();
        String id = ctx.infixOp().varId.getText();
        String variable = mapper.mapVariable(process,id);

        ExprType type = mapper.variableType(process,id);
        String get;
        if (op.equals("++")){
            get = StringUtils.constructGetter(type,state,variable)+"+("+type.toString()+"1)";
        } else{
            get =StringUtils.constructGetter(type,state,variable)+"-("+type.toString()+"1)";
        }
        String newState = StringUtils.constructSetter(type,state,variable,get);
        SymbolicExpression expr = new VariableExpression(variable,type,true);
        return new ExprGenRes(expr,newState,new TrueFormula());
    }

    @Override
    public ExprGenRes visitUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx) {
        String op = ctx.unaryOp().getText();
        ExprGenRes res = visitExpression(ctx.expression());
        SymbolicExpression expr = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        UnaryOp unOp;
        switch (op) {
            case "+": unOp= new UnPlus();break;
            case "-": unOp= new UnMinus();break;
            case "!": unOp= new UnNeg();break;
            case "~": unOp= new UnInvert(expr.exprType());break;
            default: throw new RuntimeException("Unknown unary op");
        }
        SymbolicExpression newExpr = new UnaryExpression(unOp,expr,expr.exprType());
        return new ExprGenRes(newExpr,newState,domain);
    }

    @Override
    public ExprGenRes visitCast(ReflexParser.CastContext ctx) {
        ExprType castType = TypeUtils.defineType(ctx.varType.getText());
        ExprGenRes res = visitExpression(ctx.expression());
        SymbolicExpression expr = res.getExpr();
        String newState = res.getState();
        Formula domain = res.getDomain();
        expr.actuate(newState);
        SymbolicExpression newExpr = new CastExpression(castType,expr);
        return new ExprGenRes(newExpr,newState,domain);
    }

    @Override
    public ExprGenRes visitAdd(ReflexParser.AddContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.addOp().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitShift(ReflexParser.ShiftContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.SHIFT_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitBitOr(ReflexParser.BitOrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_OR_OP().getText();
        BinaryOp binOp= BinaryOp.BitOr;

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitOr(ReflexParser.OrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.OR_OP().getText();
        BinaryOp binOp= BinaryOp.Or;

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitMul(ReflexParser.MulContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.MUL_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);
        if (binOp.equals(BinaryOp.Div) || binOp.equals(BinaryOp.Mod)) {
            Formula rule = new EqualityFormula("", false, expr2, new ConstantExpression("0", cast));
            newDomain.addConjunct(rule);
        }

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitCheckState(ReflexParser.CheckStateContext ctx) {
        return visitCheckStateExpression(ctx.checkStateExpression());
    }

    @Override
    public ExprGenRes visitUnary(ReflexParser.UnaryContext ctx) {
        return visitUnaryExpression(ctx.unaryExpression());
    }

    @Override
    public ExprGenRes visitBitXor(ReflexParser.BitXorContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_XOR_OP().getText();
        BinaryOp binOp= BinaryOp.BitXor;

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitEqual(ReflexParser.EqualContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.EQ_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitAnd(ReflexParser.AndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.AND_OP().getText();
        BinaryOp binOp= BinaryOp.And;

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    @Override
    public ExprGenRes visitBitAnd(ReflexParser.BitAndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_AND_OP().getText();
        BinaryOp binOp= BinaryOp.BitAnd;

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = TypeUtils.resultType(expr1.exprType(),expr2.exprType(),binOp);

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    public ExprGenRes visitSimpleAssign(ReflexParser.AssignContext ctx){
        BinaryOp binOp = BinaryOp.Eq;
        ReflexParser.ExpressionContext ctx1 = ctx.expression();
        String id = ctx.ID().getText();

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        expr1.actuate(newState1);

        String variable = mapper.mapVariable(process,id);
        ExprType type = mapper.variableType(process,id);

        CastExpression expr = new CastExpression(type,expr1);
        String newState = StringUtils.constructSetter(type,newState1,variable,expr.toString());

        SymbolicExpression newExpr = new VariableExpression(variable,type,true);
        return new ExprGenRes(newExpr,newState,domain1);
    }

    @Override
    public ExprGenRes visitAssign(ReflexParser.AssignContext ctx) {
        String op = ctx.assignOp().getText();
        if (op.equals("=")){
            return visitSimpleAssign(ctx);
        }
        BinaryOp binOp = BinaryOp.getAssignSubOp(op);
        String id = ctx.ID().getText();
        String variable = mapper.mapVariable(process,id);

        ExprGenRes res1 = visitExpression(ctx.expression());
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprType lastCast = mapper.variableType(process,id);
        ExprType cast = TypeUtils.castType(expr1.exprType(),lastCast,binOp);
        if (!TypeUtils.isPossible(expr1.exprType(),cast,binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),cast));
        }
        ExprType resType = lastCast;

        String getter = StringUtils.constructGetter(resType,newState1,variable);
        expr1.actuate(newState1);
        SymbolicExpression assignedExpr = new BinaryExpression(binOp,
                new CastExpression(cast,new ConstantExpression(getter,resType)),
                new CastExpression(cast,expr1),
                resType);
        String newState = StringUtils.constructGetter(resType,newState1,(new CastExpression(lastCast,assignedExpr)).toString());

        if (binOp.equals(BinaryOp.Div) || binOp.equals(BinaryOp.Mod)) {
            ConjuctionFormula newDomain = new ConjuctionFormula();
            newDomain.addConjunct(domain1);
            Formula rule = new EqualityFormula("", false, expr1, new ConstantExpression("0", cast));
            newDomain.addConjunct(rule);
            domain1 = newDomain;
        }

        SymbolicExpression newExpr = new VariableExpression(variable,resType,true);
        return new ExprGenRes(newExpr,newState,domain1);
    }

    @Override
    public ExprGenRes visitCompare(ReflexParser.CompareContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.COMP_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ExprGenRes res1 = visitExpression(ctx1);
        SymbolicExpression expr1 = res1.getExpr();
        String newState1 = res1.getState();
        Formula domain1 = res1.getDomain();

        ExprGenRes res2 = (new ExpressionVisitor(mapper,process,newState1)).visitExpression(ctx2);
        SymbolicExpression expr2 = res2.getExpr();
        String newState2 = res2.getState();
        Formula domain2 = res2.getDomain();

        if (!TypeUtils.isPossible(expr1.exprType(),expr2.exprType(),binOp)){
            throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,expr1.exprType(),expr2.exprType()));
        }
        ExprType cast = TypeUtils.castType(expr1.exprType(),expr2.exprType(),binOp);
        ExprType resType = new BoolType();

        expr1.actuate(newState2);
        if (!cast.equals(expr1.exprType())){
            expr1 = new CastExpression(cast,expr1);
        }
        expr2.actuate(newState2);
        if (!cast.equals(expr2.exprType())){
            expr2 = new CastExpression(cast,expr2);
        }
        SymbolicExpression newExpr = new BinaryExpression(binOp,expr1,expr2,resType);

        ConjuctionFormula newDomain = new ConjuctionFormula();
        newDomain.addConjunct(domain1);
        newDomain.addConjunct(domain2);

        return new ExprGenRes(newExpr,newState2,newDomain);
    }

    public ExprGenRes visitPrimaryExpression(ReflexParser.PrimaryExpressionContext ctx){
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
    public ExprGenRes visitUnaryExpression(ReflexParser.UnaryExpressionContext ctx){
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
    public ExprGenRes visitExpression(ReflexParser.ExpressionContext ctx){
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
}
