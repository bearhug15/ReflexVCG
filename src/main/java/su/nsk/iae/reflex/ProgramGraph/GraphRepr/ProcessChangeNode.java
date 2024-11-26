package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ProcessChangeNode implements IReflexNode{
    //Could be start process/ stop process/ error process;
    ParserRuleContext context;

    String processName;
    ChangeType ty;
    ProgramMetaData metaData;
    int branchNum=0;
    int numOfNextNodes=0;

    public ProcessChangeNode(ParserRuleContext context, String processName, ChangeType ty, ProgramMetaData metaData){
        this.context = context;
        this.processName = processName;
        this.ty = ty;
        this.metaData = metaData;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        String res = switch (ty){
            case Start -> creator.createStartProcessStatement(stateNumber,processName,metaData.startState(processName));

            case Stop -> creator.createStopProcessStatement(stateNumber,processName);

            case Error -> creator.createErrorProcessStatement(stateNumber,processName);

            default -> throw new RuntimeException("Unexpected ChangeType");
        };
        return new ArrayList<>(Arrays.asList(res));
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
        return "Process change: "+processName+" in "+ ty.toString();

    }
}
