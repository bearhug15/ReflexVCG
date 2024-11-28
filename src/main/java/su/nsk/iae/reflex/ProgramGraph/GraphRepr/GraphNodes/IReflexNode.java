package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.io.Serializable;
import java.util.ArrayList;


public interface IReflexNode extends Serializable {
    ParserRuleContext getContext();
    ArrayList<String> createStatements(IStatementCreator creator, int stateNumber);
    Integer getStateShift();
    Integer getNumOfStatements();

    Integer getBranchNum();
    void setBranchNum(Integer branchNum);

    boolean hasPair();
    boolean isOpener();
    IReflexNode getBind();

    int numOfNextNodes();

    void incNumOfNextNodes(int n);
}
