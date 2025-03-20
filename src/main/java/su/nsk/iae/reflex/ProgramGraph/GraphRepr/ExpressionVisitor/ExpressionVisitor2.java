package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ValueParser;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.VariableMapper;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexBaseVisitor;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class ExpressionVisitor2 extends ReflexBaseVisitor<ArrayList<ExprGenRes2>> implements ExpressionVisitor{
    final VariableMapper mapper;
    final String process;
    final String state;
    final IStatementCreator creator;
    boolean isProcessExpr;

    public ExpressionVisitor2(VariableMapper mapper, String processName, String state, IStatementCreator creator, boolean isProcessExpr){
        this.mapper = mapper;
        this.process = processName;
        this.state = state;
        this.creator = creator;
        this.isProcessExpr = isProcessExpr;
    }

    @Override
    public ArrayList<ExprGenRes2> visitCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx) {
        String process = ctx.processId.getText();
        String processState = ctx.qual.getText();
        if(isProcessExpr){
            ExprGenRes2 trueRes = new ExprGenRes2(creator,new ConstantExpression(creator.True(),new BoolType()),state,Boolean.TRUE);
            ExprGenRes2 falseRes = new ExprGenRes2(creator,new ConstantExpression(creator.False(),new BoolType()),state,Boolean.FALSE);
            switch (processState){
                case "active":{
                    trueRes.addActiveProcess(process,true);
                    falseRes.addActiveProcess(process,false);
                    break;
                }
                case "inactive":{
                    trueRes.addActiveProcess(process,false);
                    falseRes.addActiveProcess(process,true);
                    break;
                }
                case "stop":{
                    trueRes.addStoppedProcess(process,true);
                    falseRes.addStoppedProcess(process,false);
                    break;
                }
                case "error":{
                    trueRes.addErroredProcess(process,true);
                    falseRes.addErroredProcess(process,false);
                    break;
                }
                default: throw new RuntimeException("Impermissible state name for check state operator");
            }
            return new ArrayList<>(List.of(trueRes,falseRes));
        }else{
            ExprGenRes2 res = new ExprGenRes2(creator, new CheckStateExpression(process,processState,state),state);
            return new ArrayList<>(List.of(res));
        }

    }

    @Override
    public ArrayList<ExprGenRes2> visitId(ReflexParser.IdContext ctx){
        String id = ctx.ID().toString();
        if (mapper.is_const(id)){
            SymbolicExpression expr = new ConstantExpression(mapper.constantValue(id),mapper.constantType(id));
            return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state)));
        }
        if (mapper.is_variable(process,id)){
            String variable = mapper.mapVariable(process,id);
            SymbolicExpression expr = new VariableExpression(variable,mapper.variableType(process,id),true);
            return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state)));
        }
        //TODO enums
        throw new RuntimeException("Unknown variable ID:" + id);
    }
    @Override
    public ArrayList<ExprGenRes2> visitInteger(ReflexParser.IntegerContext ctx) {
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
        Boolean booleanEq;
        if (Integer.parseInt(value)!=0){
            booleanEq = Boolean.TRUE;
        }else{
            booleanEq = Boolean.FALSE;
        }
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state,booleanEq)));
    }

    @Override
    public ArrayList<ExprGenRes2> visitFloat(ReflexParser.FloatContext ctx) {
        String val =  ctx.FLOAT().getText();
        String value = ValueParser.parseFloat(val);
        SymbolicExpression expr = new ConstantExpression(value,new FloatType());
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state)));
    }

    @Override
    public ArrayList<ExprGenRes2> visitBool(ReflexParser.BoolContext ctx) {
        String val = ctx.BOOL_VAL().getText();
        val = val.substring(0, 1).toUpperCase() + val.substring(1);
        SymbolicExpression expr = new ConstantExpression(val,new BoolType());
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state,Boolean.parseBoolean(val))));
    }

    @Override
    public ArrayList<ExprGenRes2> visitTime(ReflexParser.TimeContext ctx) {
        String val =  ctx.TIME().getText();
        String value = ValueParser.parseTime(val);
        SymbolicExpression expr = new ConstantExpression(value,new TimeType());
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,state)));
    }

    /*@Override
    public ExprGenRes visitClosedExpression(ReflexParser.ClosedExpressionContext ctx) {
        return super.visitClosedExpression(ctx);
    }*/

    /*@Override
    public ExprGenRes visitPrimaryExpr(ReflexParser.PrimaryExprContext ctx) {
        return visitPrimaryExpression(ctx.primaryExpression());
    }*/

    @Override
    public ArrayList<ExprGenRes2> visitFuncCallExpr(ReflexParser.FuncCallExprContext ctx) {
        //TODO not implemenated
        throw  new RuntimeException("Function call not implemented");
        //return super.visitFuncCallExpr(ctx);
    }

    @Override
    public ArrayList<ExprGenRes2> visitPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx) {
        String op = ctx.postfixOp().op.getText();
        String id = ctx.postfixOp().varId.getText();
        String variable = mapper.mapVariable(process,id);

        ExprType type = mapper.variableType(process,id);
        String get;
        if (op.equals("++")){
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1", BinaryOp.Sum);
            //get = StringUtils.constructGetter(type,state,variable)+"+("+type.toString(creator)+"1)";
        } else{
            get = creator.BinaryExpression(creator.Getter(type,state,variable),"1",BinaryOp.Sub);
            //get =StringUtils.constructGetter(type,state,variable)+"-("+type.toString(creator)+"1)";
        }
        //String newState = StringUtils.constructSetter(type,state,variable,get);
        String newState = creator.Setter(type,state,variable,get);
        SymbolicExpression expr = new VariableExpression(variable,type,true);
        expr.actuate(state, creator);
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,newState)));
    }

    @Override
    public ArrayList<ExprGenRes2> visitInfixOpExpr(ReflexParser.InfixOpExprContext ctx) {
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
        return new ArrayList<>(List.of(new ExprGenRes2(creator,expr,newState)));
    }

    @Override
    public ArrayList<ExprGenRes2> visitUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx) {
        String op = ctx.unaryOp().getText();
        UnaryOp unOp = switch (op) {
            case "+" -> UnaryOp.Plus;
            case "-" -> UnaryOp.Minus;
            case "!" -> UnaryOp.Neg;
            case "~" -> UnaryOp.Invert;
            default -> throw new RuntimeException("Unknown unary op");
        };
        ArrayList<ExprGenRes2> fullRes = super.visitUnaryOpExpr(ctx);
        fullRes = fullRes.stream().map(res -> {
            res.getExpr().actuate(res.state,creator);
            return res.unaryUpdate(unOp);}).collect(toCollection(ArrayList::new));

        return fullRes;
    }

    @Override
    public ArrayList<ExprGenRes2> visitCast(ReflexParser.CastContext ctx) {
        ExprType castType = TypeUtils.defineType(ctx.varType.getText());
        ArrayList<ExprGenRes2> fullRes = super.visitCast(ctx);
        fullRes = fullRes.stream().map(res->{
            res.getExpr().actuate(res.state,creator);
            res.setExpr(new CastExpression(res.getExpr(), castType));
            return res;
        }).collect(toCollection(ArrayList::new));
        return fullRes;
    }

    @Override
    public ArrayList<ExprGenRes2> visitAdd(ReflexParser.AddContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.addOp().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitShift(ReflexParser.ShiftContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.SHIFT_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitBitOr(ReflexParser.BitOrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_OR_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitOr(ReflexParser.OrContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.OR_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        return res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator,isProcessExpr)).visitExpression(ctx2);
            return res2Full.stream().flatMap(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.mergeLogical(res2,binOp).stream();
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<ExprGenRes2> visitMul(ReflexParser.MulContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.MUL_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitCheckState(ReflexParser.CheckStateContext ctx) {
        return visitCheckStateExpression(ctx.checkStateExpression());
    }

    @Override
    public ArrayList<ExprGenRes2> visitUnary(ReflexParser.UnaryContext ctx) {
        return visitUnaryExpression(ctx.unaryExpression());
    }

    @Override
    public ArrayList<ExprGenRes2> visitBitXor(ReflexParser.BitXorContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_XOR_OP().getText();
        BinaryOp binOp= BinaryOp.BitXor;

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitEqual(ReflexParser.EqualContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.EQ_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitAnd(ReflexParser.AndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.AND_OP().getText();
        BinaryOp binOp= BinaryOp.And;

        ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        return res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator,isProcessExpr)).visitExpression(ctx2);
            return res2Full.stream().flatMap(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.mergeLogical(res2,binOp).stream();
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));
        //return processBinaryExpression(ctx1,ctx2,binOp);
    }

    @Override
    public ArrayList<ExprGenRes2> visitBitAnd(ReflexParser.BitAndContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.BIT_AND_OP().getText();
        BinaryOp binOp= BinaryOp.BitAnd;

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    public ArrayList<ExprGenRes2> visitSimpleAssign(ReflexParser.AssignContext ctx){
        BinaryOp binOp = BinaryOp.Eq;
        ReflexParser.ExpressionContext ctx1 = ctx.expression();
        String id = ctx.ID().getText();

        String variable = mapper.mapVariable(process,id);
        ExprType type = mapper.variableType(process,id);

        ArrayList<ExprGenRes2> res1 = visitExpression(ctx1);
        res1.forEach(res->{
            SymbolicExpression expr1 = res.getExpr();
            String newState = res.getState();
            expr1.actuate(newState,creator);
            String state = creator.Setter(type,newState,variable,expr1.toString(creator));
            SymbolicExpression newExpr = new VariableExpression(variable,type,true);
            res.setState(state);
            res.setExpr(newExpr);
        });
        return res1;
    }

    @Override
    public ArrayList<ExprGenRes2> visitAssign(ReflexParser.AssignContext ctx) {
        String op = ctx.assignOp().getText();
        if (op.equals("=")){
            return visitSimpleAssign(ctx);
        }
        BinaryOp binOp = BinaryOp.getAssignSubOp(op);
        String id = ctx.ID().getText();
        String variable = mapper.mapVariable(process,id);
        ExprType type = mapper.variableType(process,id);

        ExprGenRes2 left = new ExprGenRes2(creator,new VariableExpression(variable,mapper.variableType(process,id),true),state);

        ArrayList<ExprGenRes2> subRes = Stream.of(left).flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator,isProcessExpr)).visitExpression(ctx.expression());
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));

        subRes.forEach(res->{
            SymbolicExpression expr1 = res.getExpr();
            String newState = res.getState();
            expr1.actuate(newState,creator);
            String state = creator.Setter(type,newState,variable,expr1.toString(creator));
            SymbolicExpression newExpr = new VariableExpression(variable,type,true);
            res.setState(state);
            res.setExpr(newExpr);
        });
        return subRes;
    }

    @Override
    public ArrayList<ExprGenRes2> visitCompare(ReflexParser.CompareContext ctx) {
        ReflexParser.ExpressionContext ctx1 = ctx.expression(0);
        ReflexParser.ExpressionContext ctx2 = ctx.expression(1);
        String op = ctx.COMP_OP().getText();
        BinaryOp binOp= BinaryOp.defineOp(op);

        /*ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        ArrayList<ExprGenRes2> res = res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));*/
        return processBinaryExpression(ctx1,ctx2,binOp);
    }

    private ArrayList<ExprGenRes2> processBinaryExpression(ReflexParser.ExpressionContext ctx1, ReflexParser.ExpressionContext ctx2, BinaryOp binOp){
        ArrayList<ExprGenRes2> res1Full = visitExpression(ctx1);
        return res1Full.stream().flatMap(res1->{
            ArrayList<ExprGenRes2> res2Full = (new ExpressionVisitor2(mapper,process,res1.state,creator,isProcessExpr)).visitExpression(ctx2);
            return res2Full.stream().map(res2->{
                if (!TypeUtils.isPossible(res1.getExpr().exprType(),res2.getExpr().exprType(),binOp)){
                    throw new RuntimeException(String.format("Trying to apply %s operation to types: %s %s",binOp,res1.getExpr().exprType(),res2.getExpr().exprType()));
                }
                return res1.merge(res2,binOp).orElse(null);
            });
        }).filter(Objects::nonNull).collect(toCollection(ArrayList::new));
    }


    public ArrayList<ExprGenRes2> visitUnaryExpression(ReflexParser.UnaryExpressionContext ctx){
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

    public ArrayList<ExprGenRes2> visitExpression(ReflexParser.ExpressionContext ctx){
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
            return visitCheckStateExpression(((ReflexParser.CheckStateContext) ctx).checkStateExpression());
        if (ctx instanceof ReflexParser.UnaryContext)
            return visitUnaryExpression(((ReflexParser.UnaryContext) ctx).unaryExpression());
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
        return new ArrayList<>(this.visitExpression(expr));
    }
}
