package su.nsk.iae.reflex.vcg;

import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.AbstractGraphIterator;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramNode;
import su.nsk.iae.reflex.ProgramGraph.ProgramGraph;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.AttributedPath;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.RuleChecker;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.AttributedNodeType;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.IAttributed;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.StateAttributes;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.*;

public class VCGeneratorIterator extends AbstractGraphIterator<IReflexNode, DefaultEdge> {
    //ProgramGraph graph;
    ArrayList<Map.Entry<IReflexNode,Integer>> branchPoints = new ArrayList<>();

    RuleChecker checker;

    IReflexNode next;
    AttributeTraversal attributeTraversal;
    VCTraversal vcTraversal;

    public VCGeneratorIterator(ProgramGraph graph,AttributeCollector collector,RuleChecker checker,IStatementCreator creator){
        super(graph);
        next = graph.getStartNode();
        AttributeTraversal trav1 = new AttributeTraversal(collector);
        this.addTraversalListener(trav1);
        attributeTraversal = trav1;
        VCTraversal trav2 = new VCTraversal(creator);
        this.addTraversalListener(trav2);
        vcTraversal = trav2;
        this.checker = checker;
    }
    @Override
    public boolean hasNext() {
        return next!=null;
    }

    @Override
    public IReflexNode next() {
        this.fireVertexTraversed(this.createVertexTraversalEvent(next));
        IReflexNode toReturn = next;
        while(!attributeTraversal.isCompatible(checker)){
            boolean rest = rollback();
            if(!rest){
                return null;
            }
            next = findNext();
            this.fireVertexTraversed(this.createVertexTraversalEvent(next));
        }
        branchPoints.add(new AbstractMap.SimpleImmutableEntry<>(next,0));
        next = findNext();
        return toReturn;
    }

    protected boolean rollback() {
        while (!branchPoints.isEmpty()) {
            Map.Entry<IReflexNode, Integer> point = branchPoints.remove(branchPoints.size() - 1);
            List<IReflexNode> neighbours = ((ProgramGraph)graph).getOutgoingNeighbours(point.getKey());
            //List<IReflexNode> neighbours = graph.outgoingEdgesOf(point.getKey()).stream().map(graph::getEdgeTarget).sorted(new ReflexNodesComparator()).toList();
            if (point.getValue() >= neighbours.size() - 1) {
                fireVertexFinished(this.createVertexTraversalEvent(point.getKey()));
            } else {
                branchPoints.add(new AbstractMap.SimpleImmutableEntry<>(point.getKey(), point.getValue() + 1));
                return true;
            }
        }
        return false;
    }

    protected IReflexNode findNext(){
        List<IReflexNode> neighbours = ((ProgramGraph)graph).getOutgoingNeighbours(next);
        //List<IReflexNode> neighbours = graph.outgoingEdgesOf(next).stream().map(graph::getEdgeTarget).sorted(new ReflexNodesComparator()).toList();
        IReflexNode newNext = next;
        if(!neighbours.isEmpty()){
            newNext = neighbours.get(0);
            return newNext;
        }else{
            boolean rest = rollback();
            if(!rest){
                return null;
            }
            Map.Entry<IReflexNode,Integer> point = branchPoints.get(branchPoints.size()-1);
            neighbours = ((ProgramGraph)graph).getOutgoingNeighbours(point.getKey());
            //neighbours = graph.outgoingEdgesOf(point.getKey()).stream().map(graph::getEdgeTarget).sorted(new ReflexNodesComparator()).toList();
            newNext = neighbours.get(point.getValue());
            return newNext;
        }
    }

    public ArrayList<String> getVCStrings(){
        return vcTraversal.stringBuilder;
    }
    public int getStateCounter(){return vcTraversal.counter;}

}

class AttributeTraversal extends TraversalListenerAdapter<IReflexNode, DefaultEdge> {
    AttributedPath path;
    AttributeCollector collector;

    String currentProcess;
    String currentState;

    public AttributeTraversal(AttributeCollector collector){
        path = new AttributedPath();
        this.collector = collector;
    }

    @Override
    public void vertexTraversed(VertexTraversalEvent<IReflexNode> e) {
        IAttributed attr= collector.getAttributes(e.getVertex());
        if (attr!=null){
            if(attr.getContextType()== AttributedNodeType.State){
                StateAttributes sattr = (StateAttributes)attr;
                //((ProcessAttributes)collector.getAttributes(sattr.getRootProcess())).setState(sattr.getStateName());
            }
            path.add(attr);
        }
    }

    @Override
    public void vertexFinished(VertexTraversalEvent<IReflexNode> e) {
        IAttributed attr= collector.getAttributes(e.getVertex());
        if (attr!=null){
            path.trimBy(attr);
        }
    }

    public boolean isCompatible(RuleChecker checker){
        if(path.size()<2){
            return true;
        }
        IAttributed last = path.removeLast();
        boolean res = checker.checkRules(path,last);
        path.add(last);
        return res;
    }
}

class VCTraversal extends TraversalListenerAdapter<IReflexNode, DefaultEdge>{
    ArrayList<String> stringBuilder;
    IStatementCreator creator;
    int counter;
    public VCTraversal(IStatementCreator creator){
        stringBuilder = new ArrayList<>();
        this.creator = creator;
        counter =0;
    }
    @Override
    public void vertexTraversed(VertexTraversalEvent<IReflexNode> e) {
        IReflexNode node = e.getVertex();
        stringBuilder.addAll(node.createStatements(creator,counter));
        counter+=node.getStateShift();
    }

    @Override
    public void vertexFinished(VertexTraversalEvent<IReflexNode> e) {
        int rem = e.getVertex().getStateShift();
        counter-=rem;
        stringBuilder.subList(0,stringBuilder.size()-rem);
    }

}
