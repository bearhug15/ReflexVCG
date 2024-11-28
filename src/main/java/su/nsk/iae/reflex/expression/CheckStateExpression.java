package su.nsk.iae.reflex.expression;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.StateType;

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
    public SymbolicExpression trim() {
        return this;
    }

    @Override
    public SymbolicExpression innerExp() {
        return this;
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
                    creator.createUnaryExpression(creator.createBinaryExpression(creator.createPstateGetter(state,process),"stop",BinaryOp.Eq),new StateType(""), UnaryOp.Neg),
                    creator.createUnaryExpression(creator.createBinaryExpression(creator.createPstateGetter(state,process),"error",BinaryOp.Eq),new StateType("") , UnaryOp.Neg),
                    BinaryOp.And
            );
            //return "(\\<not>(getPstate "+state+" ''"+process+"'' = ''stop'') \\<and> "+"\\<not>(getPstate "+state+" "+process+" = ''error''))" ;
        return creator.createBinaryExpression(creator.createPstateGetter(state,process),processState,BinaryOp.Eq);
        //return "(getPstate "+state+" ''"+process+"'' = ''"+processState+"'')";
    }
}
