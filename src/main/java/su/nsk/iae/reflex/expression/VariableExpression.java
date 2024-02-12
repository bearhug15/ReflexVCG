package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.types.ExprType;

public class VariableExpression implements SymbolicExpression{
    String variable;
    ExprType type;
    boolean isMarked;

    public VariableExpression(String variable, ExprType type, boolean is_marked){
        this.variable = variable;
        this.type = type;
        this.isMarked = is_marked;
    }

    public String toString(){
        if (isMarked) {
            throw new RuntimeException("Trying toString on not actuated variable");
        }
        return variable;
    }

    public void actuate(String programState){
        if (isMarked){
            variable = "("+type.takeGetter()+" "+programState+" ''"+variable+"'')";
            isMarked = false;
        }
    }

    public boolean isActuated(){
        return !isMarked;
    }

    @Override
    public ExprType exprType() {
        return type;
    }
}
