package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.ArrayList;
import java.util.Arrays;

public class ResetNode implements IReflexNode{
    ReflexParser.ResetStatContext context;
    String processName;
    int branchNum=0;

    int numOfNextNodes=0;
    public ResetNode(ReflexParser.ResetStatContext context, String processName){
        this.context = context;
        this.processName = processName;
    }
    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        return new ArrayList<>(Arrays.asList(creator.ResetStatement(stateNumber,processName)));
    }

    @Override
    public Integer getStateShift() {
        return 1;
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
        return "Reset: "+processName;
    }
}
