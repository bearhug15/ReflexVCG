package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.ArrayList;
import java.util.Arrays;

public class SetStateNode implements IReflexNode{
    //Could be set state s/ set next state/ stop/ error
    ParserRuleContext context;
    String state;
    String processName;
    int branchNum=0;

    int numOfNextNodes=0;
    public SetStateNode(ParserRuleContext context, String processName, String state){
        this.context = context;
        this.processName = processName;
        this.state = state;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        return new ArrayList<>(Arrays.asList(creator.createSetStateStatement(stateNumber,processName,state)));
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
        return "Set state: "+processName+" in "+state;
    }
}
