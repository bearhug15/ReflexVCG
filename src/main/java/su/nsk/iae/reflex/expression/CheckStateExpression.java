package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.ExprType;

public class CheckStateExpression implements SymbolicExpression{
    String process;
    String processState;
    String state;

    public CheckStateExpression(String process, String processState, String state){
        this.process = process;
        this.processState = processState;
        this.state = state;
    }
    @Override
    public ExprType exprType() {
        return new BoolType();
    }

    @Override
    public void actuate(String programState) {}

    @Override
    public boolean isActuated() {
        return true;
    }

    @Override
    public String toString(){
        if (processState.equals("inactive"))
            return "((getPstate "+state+" ''"+process+"'' = ''stop'') \\<or> "+"(getPstate "+state+" "+process+" = ''error''))" ;
        if (processState.equals("active"))
            return "(\\<not>(getPstate "+state+" ''"+process+"'' = ''stop'') \\<and> "+"\\<not>(getPstate "+state+" "+process+" = ''error''))" ;
        return "(getPstate "+state+" ''"+process+"'' = ''"+processState+"'')";
    }
}
