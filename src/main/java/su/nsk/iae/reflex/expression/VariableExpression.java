package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.types.ExprType;

public class VariableExpression implements SymbolicExpression{
    String variable;
    ExprType type;
    boolean isMarked;

    VariableType ty;


    public VariableExpression(String variable, ExprType type, boolean isMarked){
        this.variable = variable;
        this.type = type;
        this.isMarked = isMarked;
        this.ty = VariableType.Simple;
    }

    public String toString(IStatementCreator creator){
        if (isMarked) {
            throw new RuntimeException("Trying toString on not actuated variable");
        }
        return variable;
    }

    public void actuate(String programState, IStatementCreator creator){
        if (isMarked){
            variable = "("+type.takeGetter()+" "+programState+" ''"+variable+"'')";
            isMarked = false;
        }
    }

    public boolean isActuated(){
        return !isMarked;
    }

    @Override
    public SymbolicExpression trim() {
        return this;
    }

    @Override
    public SymbolicExpression innerExp() {
        return this;
    }

    @Override
    public SymbolicExpression clone() {
        return new VariableExpression(variable,type,isMarked);
    }

    @Override
    public ExprType exprType() {
        return type;
    }
}
