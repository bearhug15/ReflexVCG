package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.*;

public class StateNode implements IReflexNode{
    ReflexParser.StateContext context;
    boolean opener;
    StateNode nodeBind;
    String stateName;

    String processName;

    int branchNum=0;

    int numOfNextNodes=0;
    StateNode(ReflexParser.StateContext context, boolean opener, String stateName, String processName) {
        this.context = context;
        this.opener = opener;
        this.stateName = stateName;
        this.processName = processName;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        if(isOpener())
            return new ArrayList<>(Arrays.asList(creator.StateStatement(stateNumber,processName,stateName)));
        else
            return new ArrayList<>();
    }

    @Override
    public Integer getStateShift() {
        if(!isOpener())
            return 0;
        return 0;
    }

    @Override
    public Integer getNumOfStatements() {
        if(!isOpener())
            return 0;
        return 1;
    }

    @Override
    public Integer getBranchNum() {
        return branchNum;
    }
    @Override
    public void setBranchNum(Integer branchNum) {
        this.branchNum = branchNum;
        nodeBind.branchNum = branchNum;
    }

    @Override
    public boolean hasPair() {
        return true;
    }

    @Override
    public boolean isOpener() {
        return opener;
    }

    @Override
    public IReflexNode getBind() {
        return nodeBind;
    }

    @Override
    public int numOfNextNodes() {
        return numOfNextNodes;
    }

    public void incNumOfNextNodes(int n){
        numOfNextNodes +=n;
    }

    void bindWith(StateNode node){
        this.nodeBind = node;
        node.nodeBind = this;
    }

    public static Map.Entry<StateNode,StateNode> StateNodes(ReflexParser.StateContext context,String stateName, String processName){
        AbstractMap.SimpleImmutableEntry<StateNode,StateNode> nodes =
                new AbstractMap.SimpleImmutableEntry<>(
                        new StateNode(context,true,stateName,processName),
                        new StateNode(context,false,stateName,processName));
        nodes.getKey().bindWith(nodes.getValue());
        return nodes;
    }

    @Override
    public String toString() {
        if(opener){
            return "State start: "+stateName;
        }else{
            return "State end: "+stateName;
        }
    }

    public String getProcessName(){
        return processName;
    }
    public String getStateName(){
        return stateName;
    }
}
