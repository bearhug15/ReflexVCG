package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.ArrayList;

public class BlankNode implements IReflexNode{
    int branchNum =0;
    int numOfNextNodes=0;
    @Override
    public ParserRuleContext getContext() {
        return null;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        return new ArrayList<>();
    }

    @Override
    public Integer getStateShift() {
        return 0;
    }

    @Override
    public Integer getNumOfStatements() {
        return 0;
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
        return "Blank node";
    }
}
