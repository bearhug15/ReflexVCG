package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProcessStatus;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.UniversalAttributes;
import su.nsk.iae.reflex.expression.SymbolicExpression;
import su.nsk.iae.reflex.expression.types.StateType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExprGenRes1 implements ExprRes{
    SymbolicExpression expr;
    String state;
    Optional<String> domain;

    ExprGenRes1(SymbolicExpression expression, String state, String domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = Optional.of(domain);
    }
    ExprGenRes1(SymbolicExpression expression, String state, Optional<String> domain){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = domain;
    }
    ExprGenRes1(SymbolicExpression expression, String state){
        if (expression.exprType().getClass().equals(StateType.class)){
            throw new RuntimeException("Expression returning state");
        }
        this.expr = expression;
        this.state = state;
        this.domain = Optional.empty();
    }

    @Override
    public Optional<String> getCondition() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getFullCondition() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getProcessesStatusesCondition() {
        return Optional.empty();
    }

    public SymbolicExpression getExpr() {
        return expr;
    }

    public String getState() {
        return state;
    }

    @Override
    public Optional<String> getDomain() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBooleanValue() {
        return Optional.empty();
    }
/*
    @Override
    public Optional<UniversalAttributes> getAttributes() {
        return Optional.empty();
    }
*/
    @Override
    public Optional<Map<String, ProcessStatus>> getProcessesStatuses() {
        return Optional.empty();
    }
}
