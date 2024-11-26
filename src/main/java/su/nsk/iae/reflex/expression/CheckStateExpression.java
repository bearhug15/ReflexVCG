package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnNeg;
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
    public void actuate(String programState, IStatementCreator creator) {}

    @Override
    public boolean isActuated() {
        return true;
    }

    @Override
    public String toString(IStatementCreator creator){
        if (processState.equals("inactive"))
            return creator.createBinaryExpression(
                    creator.createBinaryExpression(creator.createPstateGetter(state,process),"stop",BinaryOp.Eq),
                    creator.createBinaryExpression(creator.createPstateGetter(state,process),"error",BinaryOp.Eq),
                    BinaryOp.Or
            );
            //return "((getPstate "+state+" ''"+process+"'' = ''stop'') \\<or> "+"(getPstate "+state+" "+process+" = ''error''))" ;
        if (processState.equals("active"))
            return creator.createBinaryExpression(
                    creator.createUnaryExpression(creator.createBinaryExpression(creator.createPstateGetter(state,process),"stop",BinaryOp.Eq), new UnNeg()),
                    creator.createUnaryExpression(creator.createBinaryExpression(creator.createPstateGetter(state,process),"error",BinaryOp.Eq), new UnNeg()),
                    BinaryOp.And
            );
            //return "(\\<not>(getPstate "+state+" ''"+process+"'' = ''stop'') \\<and> "+"\\<not>(getPstate "+state+" "+process+" = ''error''))" ;
        return creator.createBinaryExpression(creator.createPstateGetter(state,process),processState,BinaryOp.Eq);
        //return "(getPstate "+state+" ''"+process+"'' = ''"+processState+"'')";
    }
}
