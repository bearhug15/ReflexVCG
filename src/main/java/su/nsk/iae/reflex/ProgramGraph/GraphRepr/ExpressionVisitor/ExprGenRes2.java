package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProcessStatus;
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
    Optional<Map<String,ProcessStatus>> processesStatuses = Optional.empty();
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
    private ExprGenRes2(IStatementCreator creator,SymbolicExpression expression, String state, Optional<Boolean> booleanValue,Optional<String> condition,Map<String,ProcessStatus> processesStatuses,Optional<String> domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.booleanValue = booleanValue;
        this.creator = creator;
        this.condition = condition;
        this.processesStatuses =Optional.of(processesStatuses);
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

    public Optional<Map<String, ProcessStatus>> getProcessesStatuses() {
        return processesStatuses;
    }

    public Optional<Boolean> getBooleanValue() {
        return booleanValue;
    }
/*
    @Override
    public Optional<ProcessStatusAttributes> getAttributes() {
        if(processesStatuses.isEmpty()){
            return Optional.empty();
        }else{
            UniversalAttributes attr = new UniversalAttributes(null);
            attr.setProcStatuses(processesStatuses);
            return Optional.of(attr);
        }

    }
*/
    public void setBooleanValue(Optional<Boolean> booleanValue) {
        this.booleanValue = booleanValue;
    }
    public Optional<String> getCondition(){return condition;}

    @Override
    public Optional<String> getFullCondition() {
        Optional<String> processesCondition = getProcessesStatusesCondition();
        if(condition.isPresent() && processesCondition.isPresent()){
            return Optional.of(creator.Conjunction(List.of(condition.get(),processesCondition.get())));
        }else if (condition.isPresent()){
            return condition;
        } else {
            return processesCondition;
        }
    }

    @Override
    public Optional<String> getProcessesStatusesCondition() {
        ArrayList<String> fullCondition = new ArrayList<>();
        processesStatuses.ifPresent(map->map.entrySet().forEach(entry->{
            switch(entry.getValue()){
                case Active:{
                    fullCondition.add(
                            creator.BinaryExpression(
                                    creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"stop",BinaryOp.Eq),new StateType(""), UnaryOp.Neg),
                                    creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"error",BinaryOp.Eq),new StateType("") , UnaryOp.Neg),
                                    BinaryOp.And
                            ));
                    break;
                }
                case Inactive:{
                    fullCondition.add(
                            creator.BinaryExpression(
                                    creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"stop",BinaryOp.Eq),
                                    creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"error",BinaryOp.Eq),
                                    BinaryOp.Or
                            ));
                    break;
                }
                case Stop:{
                    fullCondition.add(
                            creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"stop",BinaryOp.Eq)
                    );
                    break;
                }
                case NonStop:{
                    fullCondition.add(
                            creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"stop",BinaryOp.Eq),new StateType(""), UnaryOp.Neg)
                    );
                    break;
                }
                case Error:{
                    fullCondition.add(
                            creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"error",BinaryOp.Eq)
                    );
                    break;
                }
                case NonError:{
                    fullCondition.add(
                            creator.UnaryExpression(creator.BinaryExpression(creator.PstateGetter(state,entry.getKey()),"error",BinaryOp.Eq),new StateType(""), UnaryOp.Neg)
                    );
                    break;
                }
            }
        }));
        if(fullCondition.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(creator.Conjunction(fullCondition));
        }
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
        if(processesStatuses.isEmpty()) processesStatuses =Optional.of(new HashMap<>());
        if(isTrue){
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Inactive , Stop, Error -> false;
                    case NonStop, NonError -> {
                        processesStatuses.get().put(processName, ProcessStatus.Active);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.Active);
                return true;
            }
        }else{
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Active -> false;
                    case Stop , Error -> true;
                    case NonStop -> {
                        processesStatuses.get().put(processName, ProcessStatus.Error);
                        yield true;
                    }
                    case NonError -> {
                        processesStatuses.get().put(processName, ProcessStatus.Stop);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.Inactive);
                return true;
            }
        }
    }
    public boolean addStoppedProcess(String processName, boolean isTrue){
        if(processesStatuses.isEmpty()) processesStatuses =Optional.of(new HashMap<>());
        if(isTrue){
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Active, NonStop, Error -> false;
                    case Inactive ,NonError -> {
                        processesStatuses.get().put(processName, ProcessStatus.Stop);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.Stop);
                return true;
            }
        }else{
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Stop -> false;
                    case Active, Error -> true;
                    case Inactive -> {
                        processesStatuses.get().put(processName, ProcessStatus.Error);
                        yield true;
                    }
                    case NonError -> {
                        processesStatuses.get().put(processName, ProcessStatus.Active);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.NonStop);
                return true;
            }
        }
    }
    public boolean addErroredProcess(String processName, boolean isTrue){
        if(processesStatuses.isEmpty()) processesStatuses =Optional.of(new HashMap<>());
        if(isTrue){
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Active, NonError, Stop -> false;
                    case Inactive,NonStop -> {
                        processesStatuses.get().put(processName, ProcessStatus.Error);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.Error);
                return true;
            }
        }else{
            ProcessStatus status = processesStatuses.get().get(processName);
            if (status!=null){
                return switch (status) {
                    case Error -> false;
                    case Active, Stop -> true;
                    case Inactive -> {
                        processesStatuses.get().put(processName, ProcessStatus.Stop);
                        yield true;
                    }
                    case NonStop -> {
                        processesStatuses.get().put(processName, ProcessStatus.Active);
                        yield true;
                    }
                    default -> throw new RuntimeException("Unknown process status");
                };
            }else{
                processesStatuses.get().put(processName,ProcessStatus.NonError);
                return true;
            }
        }

    }

    protected Optional<Map<String,ProcessStatus>> mergeProcessStatuses(Map<String,ProcessStatus> anotherStatuses){
        if(processesStatuses.isEmpty()) processesStatuses =Optional.of(new HashMap<>());
        HashSet<String> processIntersection = new HashSet<>(this.processesStatuses.get().keySet());
        processIntersection.retainAll(anotherStatuses.keySet());
        HashMap<String,ProcessStatus> newProcessStatuses = new HashMap<>(this.processesStatuses.get());
        newProcessStatuses.putAll(anotherStatuses);
        for(String inter: processIntersection){
            int thisStatus = switch(this.processesStatuses.get().get(inter)){
                case Active -> 1;
                case Inactive -> 3;
                case Stop -> 5;
                case NonStop -> 7;
                case Error -> 11;
                case NonError -> 13;
                default -> throw new IllegalStateException("Unexpected value: " + this.processesStatuses.get().get(inter));
            };
            int anotherStatus = switch(anotherStatuses.get(inter)){
                case Active -> 1;
                case Inactive -> 3;
                case Stop -> 5;
                case NonStop -> 7;
                case Error -> 11;
                case NonError -> 13;
                default -> throw new IllegalStateException("Unexpected value: " + anotherStatuses.get(inter));
            };
            switch (thisStatus*anotherStatus){
                case 1:newProcessStatuses.put(inter,ProcessStatus.Active);break;
                case 3:return Optional.empty();
                case 5:return Optional.empty();
                case 7:newProcessStatuses.put(inter,ProcessStatus.Active);break;
                case 11:return Optional.empty();
                case 13:newProcessStatuses.put(inter,ProcessStatus.Active);break;

                case 9:newProcessStatuses.put(inter,ProcessStatus.Inactive);break;
                case 15:newProcessStatuses.put(inter,ProcessStatus.Stop);break;
                case 21:newProcessStatuses.put(inter,ProcessStatus.Error);break;
                case 33:newProcessStatuses.put(inter,ProcessStatus.Error);break;
                case 39:newProcessStatuses.put(inter,ProcessStatus.Stop);break;

                case 25:newProcessStatuses.put(inter,ProcessStatus.Stop);break;
                case 35:return Optional.empty();
                case 55:return Optional.empty();
                case 65:newProcessStatuses.put(inter,ProcessStatus.Stop);break;

                case 49:newProcessStatuses.put(inter,ProcessStatus.NonStop);break;
                case 77:newProcessStatuses.put(inter,ProcessStatus.NonError);break;
                case 91:newProcessStatuses.put(inter,ProcessStatus.Active);break;

                case 121:newProcessStatuses.put(inter,ProcessStatus.Error);break;
                case 143:return Optional.empty();

                case 169:newProcessStatuses.put(inter,ProcessStatus.NonError);break;
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

        Optional<Map<String,ProcessStatus>> newProcessStatuses;
        if(processesStatuses.isPresent() && another.processesStatuses.isPresent()){
            newProcessStatuses = mergeProcessStatuses(another.processesStatuses.get());
            if(newProcessStatuses.isEmpty()){
                return Optional.empty();
            }
        }else if (processesStatuses.isPresent()){
            newProcessStatuses = this.processesStatuses;
        }else{
            newProcessStatuses = another.processesStatuses;
        }

        ExprGenRes2 newRes = new ExprGenRes2(creator,newExpression,newState);
        newRes.domain=newDomain;
        newRes.condition = newCondition;
        newRes.processesStatuses = newProcessStatuses;
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
        return Optional.of(new ExprGenRes2(creator,newExpression1,this.state,Optional.of(Boolean.FALSE),newCondition1,this.processesStatuses.get(),this.domain));
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

        Optional<Map<String,ProcessStatus>> newProcessStatuses;
        if(processesStatuses.isPresent() && another.processesStatuses.isPresent()){
            newProcessStatuses = mergeProcessStatuses(another.processesStatuses.get());
            if(newProcessStatuses.isEmpty()){
                return Optional.empty();
            }
        }else if (processesStatuses.isPresent()){
            newProcessStatuses = this.processesStatuses;
        }else{
            newProcessStatuses = another.processesStatuses;
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
        return Optional.of(new ExprGenRes2(creator,newExpression1,this.state,Optional.of(Boolean.TRUE),newCondition1,this.processesStatuses.get(),this.domain));
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

        Optional<Map<String,ProcessStatus>> newProcessStatuses;
        if(processesStatuses.isPresent() && another.processesStatuses.isPresent()){
            newProcessStatuses = mergeProcessStatuses(another.processesStatuses.get());
            if(newProcessStatuses.isEmpty()){
                return Optional.empty();
            }
        }else if (processesStatuses.isPresent()){
            newProcessStatuses = this.processesStatuses;
        }else{
            newProcessStatuses = another.processesStatuses;
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
    HashMap<String,String> processesStatuses.get() = new HashMap<>();
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

    public void setProcessesStatuses(Map<String, ProcessStatus> processesStatuses) {
        this.processesStatuses = Optional.of(processesStatuses);
    }

    public void setState(String state) {
        this.state = state;
    }
}
