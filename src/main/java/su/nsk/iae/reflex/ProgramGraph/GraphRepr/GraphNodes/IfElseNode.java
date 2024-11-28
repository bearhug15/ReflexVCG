package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class IfElseNode implements IReflexNode{
    ReflexParser.IfElseStatContext context;
    boolean opener;
    IfElseNode nodeBind;
    int branchNum=0;
    int numOfNextNodes=0;
    IfElseNode(ReflexParser.IfElseStatContext context, boolean opener){
        this.context = context;
        this.opener = opener;
    }

    @Override
    public ParserRuleContext getContext() {
        return context;
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

    void bindWith(IfElseNode node){
        this.nodeBind = node;
        node.nodeBind = this;
    }
    public static Map.Entry<IfElseNode,IfElseNode> IfElseNodes(ReflexParser.IfElseStatContext context){
        AbstractMap.SimpleImmutableEntry<IfElseNode,IfElseNode> nodes = new AbstractMap.SimpleImmutableEntry<>(new IfElseNode(context,true),new IfElseNode(context,false));
        nodes.getKey().bindWith(nodes.getValue());
        return nodes;
    }
    @Override
    public String toString() {
        if(opener){
            return "If start";
        }else{
            return "if end";
        }
    }
}
