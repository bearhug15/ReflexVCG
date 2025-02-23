package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.*;

public class ProcessNode implements IReflexNode{
    ReflexParser.ProcessContext context;
    boolean opener;
    ProcessNode nodeBind;
    ArrayList<StateNode> states;
    String processName;
    int branchNum=0;
    int numOfNextNodes=0;

    ProcessNode(ReflexParser.ProcessContext context, boolean opener, String processName) {
        this.context = context;
        this.opener = opener;
        this.processName = processName;
    }


    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        return new ArrayList<>();
        /*if(opener){
            return new ArrayList<>();
        }else{
            return new ArrayList<>(List.of(""));
        }*/

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

    void bindWith(ProcessNode node){
        this.nodeBind = node;
        node.nodeBind = this;
    }

    public static Map.Entry<ProcessNode,ProcessNode> ProcessNodes(ReflexParser.ProcessContext context, String processName){
        AbstractMap.SimpleImmutableEntry<ProcessNode,ProcessNode> nodes =
                new AbstractMap.SimpleImmutableEntry<>(
                        new ProcessNode(context,true,processName),
                        new ProcessNode(context,false,processName));
        nodes.getKey().bindWith(nodes.getValue());
        return nodes;
    }

    public void setStates(ArrayList<StateNode> states) {
        this.states = states;
    }

    @Override
    public String toString() {
        if(opener){
            return "Process start: "+processName;
        }else{
            return "Process end "+processName;
        }
    }

    public String getProcessName(){
        return processName;
    }
}
