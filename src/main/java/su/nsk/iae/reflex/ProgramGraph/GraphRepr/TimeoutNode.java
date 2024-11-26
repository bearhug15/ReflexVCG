package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class TimeoutNode implements IReflexNode{

    ReflexParser.TimeoutFunctionContext context;
    boolean opener;
    TimeoutNode nodeBind;
    int branchNum=0;
    int numOfNextNodes=0;
    boolean isVariable;
    public boolean isVariable() {
        return isVariable;
    }

    public void setVariable(boolean variable) {
        isVariable = variable;
    }


    TimeoutNode(ReflexParser.TimeoutFunctionContext context, boolean opener,boolean isVariable){
        this.context = context;
        this.opener = opener;
        this.isVariable = isVariable;

    }

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

    void bindWith(TimeoutNode node){
        this.nodeBind = node;
        node.nodeBind = this;
    }
    public static Map.Entry<TimeoutNode,TimeoutNode> TimeoutNodes(ReflexParser.TimeoutFunctionContext context, boolean isVariable){
        AbstractMap.SimpleImmutableEntry<TimeoutNode,TimeoutNode> nodes =
                new AbstractMap.SimpleImmutableEntry<>(
                        new TimeoutNode(context,true,isVariable),
                        new TimeoutNode(context,false,isVariable));
        nodes.getKey().bindWith(nodes.getValue());
        return nodes;
    }
    @Override
    public String toString() {
        if(opener){
            return "Timeout start";
        }else{
            return "Timeout end";
        }
    }
}
