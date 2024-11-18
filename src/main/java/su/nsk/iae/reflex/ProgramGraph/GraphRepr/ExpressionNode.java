package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ExpressionNode implements IReflexNode {
    ReflexParser.ExpressionContext context;

    String value;
    int branchNum=0;
    int numOfNextNodes;
    public ExpressionNode(ReflexParser.ExpressionContext context, String value){
        this.context = context;
        this.value = value;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        return new ArrayList<>(Arrays.asList(creator.createExpressionStatement(stateNumber,value)));
    }

    @Override
    public Integer getStateShift() {
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
        return "Expression: "+value;
    }
}
