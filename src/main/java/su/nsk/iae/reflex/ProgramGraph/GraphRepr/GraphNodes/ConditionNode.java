package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.ArrayList;

public class ConditionNode implements IReflexNode{
    ReflexParser.IfElseStatContext ifContext;
    ReflexParser.SwitchStatContext switchContext;
    ReflexParser.TimeoutFunctionContext timeoutContext;
    ReflexParser.ExpressionContext exprContext;

    String condition;



    boolean isTimeoutVariable;
    int branchNum=0;
    int numOfNextNodes=0;
    public ConditionNode(ReflexParser.IfElseStatContext context,String condition){
        this.ifContext = context;
        this.switchContext = null;
        this.timeoutContext = null;
        this.exprContext = null;
        this.condition = condition;
        this.isTimeoutVariable = false;
    }
    public ConditionNode(ReflexParser.SwitchStatContext context,String condition){
        this.ifContext = null;
        this.switchContext = context;
        this.timeoutContext = null;
        this.exprContext = null;
        this.condition = condition;
        this.isTimeoutVariable = false;
    }
    public ConditionNode(ReflexParser.TimeoutFunctionContext context,String condition,boolean isTimeoutVariable){
        this.ifContext = null;
        this.switchContext = null;
        this.timeoutContext = context;
        this.exprContext = null;
        this.condition = condition;
        this.isTimeoutVariable = isTimeoutVariable;
    }
    public ConditionNode(ReflexParser.ExpressionContext context,String condition,boolean isDomain){
        this.ifContext = null;
        this.switchContext = null;
        this.timeoutContext = null;
        this.exprContext = context;
        this.condition = condition;
        this.isTimeoutVariable = false;
    }

    @Override
    public ParserRuleContext getContext() {
        if(ifContext!=null){
            return ifContext;
        } else if(switchContext!=null){
            return switchContext;
        } else {
            return timeoutContext;
        }
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        String cond = creator.ConditionStatement(stateNumber,condition);
        ArrayList<String> stmts = new ArrayList<>();
        stmts.add(cond);
        return stmts;
    }

    @Override
    public Integer getStateShift() {
        return 0;
    }

    @Override
    public Integer getNumOfStatements() {
        return 1;
    }

    @Override
    public Integer getBranchNum() {
        return branchNum;
    }

    @Override
    public void setBranchNum(Integer branchNum) {
        this.branchNum = branchNum;
    }

    @Override
    public boolean hasPair() {
        return false;
    }

    @Override
    public boolean isOpener() {
        return false;
    }

    @Override
    public IReflexNode getBind() {
        return null;
    }

    @Override
    public int numOfNextNodes() {
        return numOfNextNodes;
    }

    public void incNumOfNextNodes(int n){
        numOfNextNodes +=n;
    }

    @Override
    public String toString() {
        return "Condition: "+ condition;
    }

    public boolean isTimeoutVariable() {
        return isTimeoutVariable;
    }

    public void setTimeoutVariable(boolean timeoutVariable) {
        isTimeoutVariable = timeoutVariable;
    }
}
