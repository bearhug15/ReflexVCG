package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.UniversalAttributes;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.*;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.StateType;
import su.nsk.iae.reflex.expression.types.TypeUtils;

import java.util.*;
import java.util.stream.Stream;

public class ExprGenRes2 implements ExprRes {

    Optional<String> condition = Optional.empty();
    HashMap<String,String> processesStatuses = new HashMap<>();
    SymbolicExpression expr;
    String state;
    Optional<String> domain = Optional.empty();
    Optional<Boolean> booleanValue =Optional.empty();
    IStatementCreator creator;

    ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.creator = creator;
    }
    ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state, Boolean booleanValue){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.booleanValue = Optional.of(booleanValue);
        this.creator = creator;
    }
    private ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state, Optional<Boolean> booleanValue,Optional<String> condition,HashMap<String,String> processesStatuses,Optional<String> domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.booleanValue = booleanValue;
        this.creator = creator;
        this.condition = condition;
        this.processesStatuses = processesStatuses;
        this.domain = domain;
    }
    /*ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state, String domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = Optional.of(domain);
        this.booleanValue = Optional.empty();
        this.creator = creator;
    }

    ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state, String domain, Boolean booleanValue){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = Optional.of(domain);
        this.booleanValue = Optional.of(booleanValue);
        this.creator = creator;
    }*/

    public SymbolicExpression getExpr() {
        return expr;
    }

    public String getState() {
        return state;
    }

    public void setDomain(Optional<String> domain) {
        this.domain = domain;
    }
    public Optional<String> getDomain() {
        return domain;
    }

    public HashMap<String, String> getProcessesStatuses() {
        return processesStatuses;
    }

    public Optional<Boolean> getBooleanValue() {
        return booleanValue;
    }

    @Override
    public Optional<UniversalAttributes> getAttributes() {
        if(processesStatuses.isEmpty()){
            return Optional.empty();
        }else{
            UniversalAttributes attr = new UniversalAttributes(null);
            attr.setProcStatuses(processesStatuses);
            return Optional.of(attr);
        }

    }

    public void setBooleanValue(Optional<Boolean> booleanValue) {
        this.booleanValue = booleanValue;
    }
    public Optional<String> getCondition(){return condition;}

    @Override
    public Optional<String> getFullCondition(String process) {
        if(condition.isEmpty() && processesStatuses.isEmpty()){
            return Optional.empty();
        }
        ArrayList<String> fullCondition = new ArrayList<>();
        condition.ifPresent(fullCondition::add);
        processesStatuses.entrySet().stream().forEach(entry->{
            switch(entry.getValue()){
                case "active":{
                    fullCondition.add(
                            creator.BinaryExpression(
                                    creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,process),"stop",BinaryOp.Eq),new StateType(""), UnaryOp.Neg),
                                    creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,process),"error",BinaryOp.Eq),new StateType("") , UnaryOp.Neg),
                                    BinaryOp.And
                            ));
                }
                case "inactive":{
                    fullCondition.add(
                            creator.BinaryExpression(
                                    creator.BinaryExpression(creator.PstateGetter(state,process),"stop",BinaryOp.Eq),
                                    creator.BinaryExpression(creator.PstateGetter(state,process),"error",BinaryOp.Eq),
                                    BinaryOp.Or
                            ));
                }
                case "stop":{
                    fullCondition.add(
                            creator.BinaryExpression(creator.PstateGetter(state,process),"stop",BinaryOp.Eq)
                    );
                }
                case "nonstop":{
                    fullCondition.add(
                            creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,process),"stop",BinaryOp.Eq),new StateType(""), UnaryOp.Neg)
                    );
                }
                case "error":{
                    fullCondition.add(
                            creator.BinaryExpression(creator.PstateGetter(state,process),"error",BinaryOp.Eq)
                    );
                }
                case "nonerror":{
                    fullCondition.add(
                            creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,process),"error",BinaryOp.Eq),new StateType(""), UnaryOp.Neg)
                    );
                }
            }
        });

        return Optional.of(creator.Conjunction(fullCondition));
    }

    public void setExpr(SymbolicExpression expr) {
        this.expr = expr;
    }

    public void addCondition(String condition){
        if (this.condition.isEmpty()){
            this.condition = Optional.of(condition);
        }else{
            this.condition =Optional.of(creator.Conjunction(List.of(this.condition.get(),condition)));
        }
    }

    public boolean addActiveProcess(String processName, boolean isTrue){
        if(isTrue){
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "inactive", "stop", "error" -> false;
                    case "nonstop", "nonerror" -> {
                        processesStatuses.put(processName, "active");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"active");
                return true;
            }
        }else{
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "active" -> false;
                    case "stop", "error" -> true;
                    case "nonstop" -> {
                        processesStatuses.put(processName, "error");
                        yield true;
                    }
                    case "nonerror" -> {
                        processesStatuses.put(processName, "stop");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"inactive");
                return true;
            }
        }
    }
    public boolean addStoppedProcess(String processName, boolean isTrue){
        if(isTrue){
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "active", "nonstop", "error" -> false;
                    case "inactive","nonerror" -> {
                        processesStatuses.put(processName, "stop");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"stop");
                return true;
            }
        }else{
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "stop" -> false;
                    case "active", "error" -> true;
                    case "inactive" -> {
                        processesStatuses.put(processName, "error");
                        yield true;
                    }
                    case "nonerror" -> {
                        processesStatuses.put(processName, "active");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"nonstop");
                return true;
            }
        }
    }
    public boolean addErroredProcess(String processName, boolean isTrue){
        if(isTrue){
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "active", "nonerror", "stop" -> false;
                    case "inactive","nonstop" -> {
                        processesStatuses.put(processName, "error");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"error");
                return true;
            }
        }else{
            String status = processesStatuses.get(processName);
            if (status!=null){
                return switch (status) {
                    case "error" -> false;
                    case "active", "stop" -> true;
                    case "inactive" -> {
                        processesStatuses.put(processName, "stop");
                        yield true;
                    }
                    case "nonstop" -> {
                        processesStatuses.put(processName, "active");
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.put(processName,"nonerror");
                return true;
            }
        }

    }

    protected Optional<HashMap<String,String>> mergeProcessStatuses(HashMap<String,String> anotherStatuses){
        HashSet<String> processIntersection = new HashSet<>(this.processesStatuses.keySet());
        processIntersection.retainAll(anotherStatuses.keySet());
        HashMap<String,String> newProcessStatuses = new HashMap<>(this.processesStatuses);
        newProcessStatuses.putAll(anotherStatuses);
        for(String inter: processIntersection){
            //Перекодировать через произведение простых чисел
            int thisStatus = switch(this.processesStatuses.get(inter)){
                case "active" -> 1;
                case "inactive" -> 3;
                case "stop" -> 5;
                case "nonstop" -> 7;
                case "error" -> 11;
                case "nonerror" -> 13;
                default -> throw new IllegalStateException("Unexpected value: " + this.processesStatuses.get(inter));
            };
            int anotherStatus = switch(anotherStatuses.get(inter)){
                case "active" -> 1;
                case "inactive" -> 3;
                case "stop" -> 5;
                case "nonstop" -> 7;
                case "error" -> 11;
                case "nonerror" -> 13;
                default -> throw new IllegalStateException("Unexpected value: " + anotherStatuses.get(inter));
            };
            switch (thisStatus*anotherStatus){
                case 1:newProcessStatuses.put(inter,"active");break;
                case 3:return Optional.empty();
                case 5:return Optional.empty();
                case 7:newProcessStatuses.put(inter,"active");break;
                case 11:return Optional.empty();
                case 13:newProcessStatuses.put(inter,"active");break;

                case 9:newProcessStatuses.put(inter,"inactive");break;
                case 15:newProcessStatuses.put(inter,"stop");break;
                case 21:newProcessStatuses.put(inter,"error");break;
                case 33:newProcessStatuses.put(inter,"error");break;
                case 39:newProcessStatuses.put(inter,"stop");break;

                case 25:newProcessStatuses.put(inter,"stop");break;
                case 35:return Optional.empty();
                case 55:return Optional.empty();
                case 65:newProcessStatuses.put(inter,"stop");break;

                case 49:newProcessStatuses.put(inter,"nonstop");break;
                case 77:newProcessStatuses.put(inter,"error");break;
                case 91:newProcessStatuses.put(inter,"active");break;

                case 121:newProcessStatuses.put(inter,"error");break;
                case 143:return Optional.empty();

                case 169:newProcessStatuses.put(inter,"nonerror");break;
            }
        }
        return Optional.of(newProcessStatuses);
    }

    public Optional<ExprGenRes2> merge(ExprGenRes2 another, BinaryOp op){
        Optional<String> newCondition;
        if(this.condition.isEmpty()) {
            newCondition = another.condition;
        }else if(another.condition.isEmpty()) {
            newCondition = this.condition;
        } else {
            newCondition =Optional.of(creator.Conjunction(List.of(this.condition.get(),another.condition.get())));
        }

        SymbolicExpression exprClone = this.expr.clone();
        ExprType cast = TypeUtils.castType(exprClone.exprType(),another.expr.exprType(),op);
        ExprType resType = TypeUtils.resultType(exprClone.exprType(),another.expr.exprType(),op);
        exprClone.actuate(another.state, creator);
        if (!cast.equals(exprClone.exprType())){
            exprClone = new CastExpression(exprClone, cast);
        }
        another.expr.actuate(another.state, creator);
        if (!cast.equals(another.expr.exprType())){
            another.expr = new CastExpression(another.expr, cast);
        }

        SymbolicExpression newExpression = new BinaryExpression(op,exprClone,another.expr, resType);
        Optional<String> addDomain;
        if(op.equals(BinaryOp.Div) || op.equals(BinaryOp.Mod)){
             addDomain =Optional.of( creator.BinaryExpression(another.expr.toString(creator),(new ConstantExpression("0", cast)).toString(creator),BinaryOp.NotEq));
        }else{
            addDomain = Optional.empty();
        }

        String newState = another.state;
        Optional<String> newDomain = newDomain(this.domain,another.domain,addDomain);
        Optional<Boolean> newBooleanValue;
        if (this.booleanValue.isPresent() && another.booleanValue.isPresent()){
            boolean left = this.booleanValue.get();
            boolean right = another.booleanValue.get();
            newBooleanValue = Optional.of(op.applyToBoolean(left,right));
        }else{
            newBooleanValue = Optional.empty();
        }

        Optional<HashMap<String,String>> newProcessStatuses = mergeProcessStatuses(another.processesStatuses);
        if(newProcessStatuses.isEmpty()){
            return Optional.empty();
        }
        ExprGenRes2 newRes = new ExprGenRes2(creator,newExpression,newState);
        newRes.domain=newDomain;
        newRes.condition = newCondition;
        newRes.processesStatuses = newProcessStatuses.get();
        newRes.booleanValue = newBooleanValue;
        return Optional.of(new ExprGenRes2(creator,newExpression,newState,newBooleanValue,newCondition,newProcessStatuses.get(),newDomain));
    }

    private Optional<ExprGenRes2> mergeLogicalAndLeftFalse(ExprGenRes2 another){
        Optional<String> newCondition;
        if(this.condition.isEmpty()) {
            newCondition = another.condition;
        }else if(another.condition.isEmpty()) {
            newCondition = this.condition;
        } else {
            newCondition =Optional.of(creator.Conjunction(List.of(this.condition.get(),another.condition.get())));
        }
        Optional<String> newCondition1;
        SymbolicExpression newExpression1;
        SymbolicExpression exprClone = this.expr.clone();
        exprClone.actuate(this.state,creator);

        newCondition1 = Optional.of(creator.Conjunction(List.of(newCondition.orElse(""),
                (new BinaryExpression(BinaryOp.Eq,exprClone,new ConstantExpression(creator.False(), new BoolType()),new BoolType())).toString(creator))));
        newExpression1 = new ConstantExpression(creator.False(), new BoolType());
        return Optional.of(new ExprGenRes2(creator,newExpression1,this.state,Optional.of(Boolean.FALSE),newCondition1,this.processesStatuses,this.domain));
    }
    private Optional<ExprGenRes2> mergeLogicalAndLeftTrue(ExprGenRes2 another){
        Optional<String> newCondition;
        if(this.condition.isEmpty()) {
            newCondition = another.condition;
        }else if(another.condition.isEmpty()) {
            newCondition = this.condition;
        } else {
            newCondition =Optional.of(creator.Conjunction(List.of(this.condition.get(),another.condition.get())));
        }
        Optional<String> newCondition2;
        SymbolicExpression exprClone = this.expr.clone();
        ExprType cast = TypeUtils.castType(exprClone.exprType(),another.expr.exprType(),BinaryOp.And);
        ExprType resType = TypeUtils.resultType(exprClone.exprType(),another.expr.exprType(),BinaryOp.And);
        exprClone.actuate(another.state, creator);
        if (!cast.equals(exprClone.exprType())){
            exprClone = new CastExpression(exprClone, cast);
        }
        another.expr.actuate(another.state, creator);
        if (!cast.equals(another.expr.exprType())){
            another.expr = new CastExpression(another.expr, cast);
        }
        newCondition2 = Optional.of(creator.Conjunction(List.of(newCondition.orElse(""),
                (new BinaryExpression(BinaryOp.Eq,exprClone,new ConstantExpression(creator.True(), new BoolType()),new BoolType())).toString(creator))));
        SymbolicExpression newExpression2 = another.expr;
        Optional<HashMap<String,String>> newProcessStatuses = mergeProcessStatuses(another.processesStatuses);
        if(newProcessStatuses.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(new ExprGenRes2(
                creator,
                newExpression2,
                another.state,
                another.booleanValue,
                newCondition2,
                newProcessStatuses.get(),
                newDomain(this.domain,another.domain,Optional.empty())));
    }
    private Optional<ExprGenRes2> mergeLogicalOrLeftTrue(ExprGenRes2 another){
        Optional<String> newCondition;
        if(this.condition.isEmpty()) {
            newCondition = another.condition;
        }else if(another.condition.isEmpty()) {
            newCondition = this.condition;
        } else {
            newCondition =Optional.of(creator.Conjunction(List.of(this.condition.get(),another.condition.get())));
        }
        Optional<String> newCondition1;
        SymbolicExpression newExpression1;
        SymbolicExpression exprClone = this.expr.clone();
        exprClone.actuate(this.state,creator);

        newCondition1 = Optional.of(creator.Conjunction(List.of(newCondition.orElse(""),
                (new BinaryExpression(BinaryOp.Eq,exprClone,new ConstantExpression(creator.True(), new BoolType()),new BoolType())).toString(creator))));
        newExpression1 = new ConstantExpression(creator.True(), new BoolType());
        return Optional.of(new ExprGenRes2(creator,newExpression1,this.state,Optional.of(Boolean.TRUE),newCondition1,this.processesStatuses,this.domain));
    }
    private Optional<ExprGenRes2> mergeLogicalOrLeftFalse(ExprGenRes2 another){
        Optional<String> newCondition;
        if(this.condition.isEmpty()) {
            newCondition = another.condition;
        }else if(another.condition.isEmpty()) {
            newCondition = this.condition;
        } else {
            newCondition =Optional.of(creator.Conjunction(List.of(this.condition.get(),another.condition.get())));
        }
        Optional<String> newCondition2;
        SymbolicExpression exprClone = this.expr.clone();
        ExprType cast = TypeUtils.castType(exprClone.exprType(),another.expr.exprType(),BinaryOp.Or);
        ExprType resType = TypeUtils.resultType(exprClone.exprType(),another.expr.exprType(),BinaryOp.Or);
        exprClone.actuate(another.state, creator);
        if (!cast.equals(exprClone.exprType())){
            exprClone = new CastExpression(exprClone, cast);
        }
        another.expr.actuate(another.state, creator);
        if (!cast.equals(another.expr.exprType())){
            another.expr = new CastExpression(another.expr, cast);
        }
        newCondition2 = Optional.of(creator.Conjunction(List.of(newCondition.orElse(""),
                (new BinaryExpression(BinaryOp.Eq,exprClone,new ConstantExpression(creator.False(), new BoolType()),new BoolType())).toString(creator))));
        SymbolicExpression newExpression2 = another.expr;
        Optional<HashMap<String,String>> newProcessStatuses = mergeProcessStatuses(another.processesStatuses);
        if(newProcessStatuses.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(new ExprGenRes2(
                creator,
                newExpression2,
                another.state,
                another.booleanValue,
                newCondition2,
                newProcessStatuses.get(),
                newDomain(this.domain,another.domain,Optional.empty())));
    }
    public List<ExprGenRes2> mergeLogical(ExprGenRes2 another, BinaryOp op){
        Optional<String> newCondition;
        ArrayList<ExprGenRes2> results = new ArrayList<>();
        switch (op){
            case And:{

                if(this.booleanValue.isEmpty()){
                    Optional<ExprGenRes2> res1 = mergeLogicalAndLeftFalse(another);
                    Optional<ExprGenRes2> res2 = mergeLogicalAndLeftTrue(another);
                    res1.ifPresent(results::add);
                    res2.ifPresent(results::add);
                }else if (this.booleanValue.get()){
                    Optional<ExprGenRes2> res2 = mergeLogicalAndLeftTrue(another);
                    res2.ifPresent(results::add);
                } else{
                    Optional<ExprGenRes2> res1 = mergeLogicalAndLeftFalse(another);
                    res1.ifPresent(results::add);
                }
                if(results.isEmpty()){
                    return new ArrayList<>();
                }else{
                    return results;
                }
            }
            case Or:{
                if(this.booleanValue.isEmpty()){
                    Optional<ExprGenRes2> res1 = mergeLogicalOrLeftFalse(another);
                    Optional<ExprGenRes2> res2 = mergeLogicalOrLeftTrue(another);
                    res1.ifPresent(results::add);
                    res2.ifPresent(results::add);
                }else if (this.booleanValue.get()){
                    Optional<ExprGenRes2> res2 = mergeLogicalOrLeftTrue(another);
                    res2.ifPresent(results::add);
                } else{
                    Optional<ExprGenRes2> res1 = mergeLogicalOrLeftFalse(another);
                    res1.ifPresent(results::add);
                }
                if(results.isEmpty()){
                    return new ArrayList<>();
                }else{
                    return results;
                }
            }
            default:throw new RuntimeException("Expression result merge error: trying to logically merge unsupported for unsupported binary expression");
        }
    }

    private Optional<String> newDomain(Optional<String> firstDomain, Optional<String> secondDomain, Optional<String> additionalDomain){
        Optional<String> newDomain;
        List<String> res = Stream.of(firstDomain, secondDomain,additionalDomain).filter(Optional::isPresent).map(Optional::get).toList();
        if (res.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(creator.Conjunction(res));
        }
    }
    /*
    Optional<String> condition = Optional.empty();
    HashMap<String,String> processesStatuses = new HashMap<>();
    SymbolicExpression expr;
    String state;
    Optional<String> domain = Optional.empty();
    Optional<Boolean> booleanValue =Optional.empty();
    IStatementCreator creator;
    */
    public ExprGenRes2 unaryUpdate(UnaryOp op){

        SymbolicExpression newExpr = new UnaryExpression(op,this.expr,this.expr.exprType());
        Optional<Boolean> newBooleanValue = switch (op){
            case Neg -> {if (this.booleanValue.isPresent()){
                if (this.booleanValue.get()){
                    yield Optional.of(Boolean.FALSE);
                }else{
                    yield Optional.of(Boolean.TRUE);
                }
            }else{
                yield Optional.empty();
            }}
            default -> Optional.empty();
        };

        ExprGenRes2 newRes = new ExprGenRes2(creator,newExpr,this.state);
        newRes.condition = this.condition;
        newRes.processesStatuses = this.processesStatuses;
        newRes.domain = this.domain;
        newRes.booleanValue = newBooleanValue;
        return newRes;
    }

    public void setCondition(Optional<String> condition) {
        this.condition = condition;
    }

    public void setProcessesStatuses(HashMap<String, String> processesStatuses) {
        this.processesStatuses = processesStatuses;
    }

    public void setState(String state) {
        this.state = state;
    }
}
