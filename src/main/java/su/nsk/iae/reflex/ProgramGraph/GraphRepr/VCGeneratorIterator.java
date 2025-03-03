package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.AbstractGraphIterator;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.AttributedNodeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.IAttributed;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.StateAttributes;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.RuleChecker.IRuleChecker;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.*;

public class VCGeneratorIterator extends AbstractGraphIterator<IReflexNode, DefaultEdge> {
    //ProgramGraph graph;
    ArrayList<Map.Entry<IReflexNode,Integer>> branchPoints = new ArrayList<>();

    IRuleChecker checker;

    IReflexNode init;
    //Cursor shows last returned element
    IReflexNode cursor;
    AttributeTraversal attributeTraversal;
    VCTraversal vcTraversal;

    public VCGeneratorIterator(ProgramGraph graph,AttributeCollector collector,IRuleChecker checker,IStatementCreator creator){
        super(graph);
        //cursor = graph.getStartNode();
        cursor=null;
        init = graph.getStartNode();
        AttributeTraversal trav1 = new AttributeTraversal(collector);
        this.addTraversalListener(trav1);
        attributeTraversal = trav1;
        VCTraversal trav2 = new VCTraversal(creator);
        this.addTraversalListener(trav2);
        vcTraversal = trav2;
        this.checker = checker;
    }
    public VCGeneratorIterator(ProgramGraph graph,AttributeCollector collector,IStatementCreator creator){
        super(graph);
        //cursor = graph.getStartNode();
        cursor=null;
        init = graph.getStartNode();
        AttributeTraversal trav1 = new AttributeTraversal(collector);
        this.addTraversalListener(trav1);
        attributeTraversal = trav1;
        VCTraversal trav2 = new VCTraversal(creator);
        this.addTraversalListener(trav2);
        vcTraversal = trav2;
        this.checker = new IRuleChecker(){
            @Override
            public boolean checkRules(AttributedPath path, IAttributed attr) {
                return true;
            }
        };
    }
    @Override
    public boolean hasNext() {
        if(init!=null){
            return true;
        }
        List<IReflexNode> neighs = ((ProgramGraph)graph).getOutgoingNeighbours(cursor);
        if(neighs.isEmpty()){
            int i = branchPoints.size()-1;
            while(i>=0){
                Map.Entry<IReflexNode, Integer> point = branchPoints.get(i);
                List<IReflexNode> neighbours = ((ProgramGraph)graph).getOutgoingNeighbours(point.getKey());
                if (point.getValue() >= neighbours.size() - 1) {
                    i--;
                } else {
                    return true;
                }
            }
            return false;
        }else{
            return true;
        }
    }
    @Override
    public IReflexNode next() {
        if(cursor==null){
            if(init==null){
                return null;
            }else{
                cursor = init;
                this.fireVertexTraversed(this.createVertexTraversalEvent(cursor));
                init = null;
                return cursor;
            }
        }
        boolean rollback = false;
        do{
            cursor = findNext(rollback);
            if (cursor == null){
                return null;
            }
            rollback = true;
        }while(!attributeTraversal.isCompatible(checker));
        return cursor;
    }
    public IReflexNode findNext(boolean rollback){
        List<IReflexNode> neighs = ((ProgramGraph)graph).getOutgoingNeighbours(cursor);
        if(neighs.isEmpty() || rollback){
            while(!branchPoints.isEmpty()){
                Map.Entry<IReflexNode, Integer> point = branchPoints.remove(branchPoints.size() - 1);
                List<IReflexNode> neighbours = ((ProgramGraph)graph).getOutgoingNeighbours(point.getKey());
                //List<IReflexNode> neighbours = graph.outgoingEdgesOf(point.getKey()).stream().map(graph::getEdgeTarget).sorted(new ReflexNodesComparator()).toList();
                if (point.getValue() >= neighbours.size() - 1) {
                    fireVertexFinished(this.createVertexTraversalEvent(point.getKey()));
                } else {
                    IReflexNode newCursor = neighbours.get(point.getValue() + 1);
                    branchPoints.add(new AbstractMap.SimpleImmutableEntry<>(point.getKey(), point.getValue() + 1));
                    branchPoints.add(new AbstractMap.SimpleImmutableEntry<>(newCursor, 0));
                    this.fireVertexTraversed(this.createVertexTraversalEvent(newCursor));
                    return newCursor;
                }
            }
            return null;
        }else{
            branchPoints.add(new AbstractMap.SimpleImmutableEntry<>(neighs.get(0), 0));
            this.fireVertexTraversed(this.createVertexTraversalEvent(neighs.get(0)));
            return neighs.get(0);
        }
    }

    public ArrayList<String> getVCStrings(){
        return (ArrayList<String>) vcTraversal.stringBuilder.clone();
    }
    public int getStateCounter(){return vcTraversal.counter;}

}

class AttributeTraversal extends TraversalListenerAdapter<IReflexNode, DefaultEdge> {
    AttributedPath path;
    AttributeCollector collector;


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

    public boolean isCompatible(IRuleChecker checker){
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
        return;
    }

    @Override
    public void vertexFinished(VertexTraversalEvent<IReflexNode> e) {
        IReflexNode node = e.getVertex();
        counter-=node.getStateShift();
        int newEnd = stringBuilder.size()-node.getNumOfStatements();
        for(int i =stringBuilder.size();i>newEnd;i--){
            stringBuilder.remove(i-1);
        }
        return;
    }

}
