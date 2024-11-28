package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class ProgramNode implements IReflexNode{
    ReflexParser.ProgramContext context;
    boolean opener;
    ProgramNode nodeBind;
    String programName;
    ArrayList<ProcessNode> processes;
    ArrayList<String> inputVars;
    int branchNum = 0;

    int numOfNextNodes=0;

    ProgramNode(ReflexParser.ProgramContext context, boolean opener,String programName, ArrayList<String> inputVars){
        this.context = context;
        this.opener = opener;
        this.programName=programName;
        this.inputVars = inputVars;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
    }

    @Override
    public ArrayList<String> createStatements(IStatementCreator creator, int stateNumber) {
        if (!isOpener())
            return new ArrayList<>();
        ArrayList<String> vec = new ArrayList<>();
        for(String var: inputVars){
            String res = creator.createInputVarInitStatement(stateNumber,var);
            stateNumber++;
            vec.add(res);
        }
        return vec;
    }

    @Override
    public Integer getStateShift() {
        if(!isOpener())
            return 0;
        return inputVars.size();
    }

    @Override
    public Integer getNumOfStatements() {
        if(!isOpener())
            return 0;
        return inputVars.size();
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

    void bindWith(ProgramNode node){
        this.nodeBind = node;
        node.nodeBind = this;
    }
    public static Map.Entry<ProgramNode,ProgramNode> ProgramNodes(ReflexParser.ProgramContext context,String programName, ArrayList<String> inputVars){
        AbstractMap.SimpleImmutableEntry<ProgramNode,ProgramNode> nodes =
                new AbstractMap.SimpleImmutableEntry<>(
                        new ProgramNode(context,true,programName,inputVars),
                        new ProgramNode(context,false,programName,inputVars));
        nodes.getKey().bindWith(nodes.getValue());
        return nodes;
    }

    public void setProcesses(ArrayList<ProcessNode> processes) {
        this.processes = processes;
    }

    @Override
    public String toString() {
        if(opener){
            return "Program start";
        }else{
            return "Program end";
        }
    }
}
