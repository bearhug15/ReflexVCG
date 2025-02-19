package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.StateNode;

import java.util.List;

public class ProgramGraph extends DefaultDirectedGraph<IReflexNode, DefaultEdge> {
    public IReflexNode getStartNode() {
        return startNode;
    }

    public IReflexNode getEndNode() {
        return endNode;
    }

    public int getBranchNum() {
        return branchNum;
    }

    IReflexNode startNode;
    IReflexNode endNode;

    int branchNum=0;
    public ProgramGraph(IReflexNode startNode, IReflexNode endNode) {
        super(DefaultEdge.class);
        if(startNode==null && endNode!=null || startNode!=null && endNode==null){
            throw new RuntimeException("One of graph nodes is null");
        }
        this.startNode = startNode;
        this.endNode = endNode;
        if(startNode!=null) {
            this.addVertex(startNode);
            if (!startNode.equals(endNode)) {
                this.addVertex(endNode);
            }
        }
    }

    public void insertGraph(ProgramGraph graph){
        if(graph.startNode==null){
            return;
        }
        Graphs.addGraph(this,graph);
        if(this.startNode!=null) {
            this.addEdge(startNode, graph.startNode);
            this.addEdge(graph.endNode, endNode);
            graph.startNode.setBranchNum(branchNum);
            branchNum++;
            this.startNode.incNumOfNextNodes(1);
        } else{
            this.startNode = graph.startNode;
            this.endNode = graph.endNode;
            this.branchNum=graph.branchNum;
        }
    }

    public void extendGraph(ProgramGraph graph){
        if(graph.startNode==null){
            return;
        }
        Graphs.addGraph(this,graph);
        if(this.startNode!=null) {
            this.addEdge(endNode, graph.startNode);
            this.endNode.incNumOfNextNodes(1);
            this.endNode = graph.endNode;
        } else{
            this.startNode = graph.startNode;
            this.endNode = graph.endNode;
        }
    }
    public void extendGraph(IReflexNode node){
        if(node==null){
            return;
        }
        this.addVertex(node);
        if(this.startNode!=null) {
            this.addEdge(endNode, node);
            this.endNode.incNumOfNextNodes(1);
            this.endNode = node;
        } else{
            this.startNode = node;
            this.endNode = node;
        }
    }
    public void insertDanglingGraph(ProgramGraph graph){
        if(graph.startNode==null){
            return;
        }
        if(this.startNode!=null) {
            Graphs.addGraph(this,graph);
            this.addEdge(startNode, graph.startNode);
            this.startNode.incNumOfNextNodes(1);
            graph.startNode.setBranchNum(branchNum);
            branchNum++;
        }
    }
    public void insertDanglingNode(IReflexNode node){
        if(node==null){
            return;
        }
        //this.addVertex(node)
        //Graphs.addGraph(this,graph);
        if(this.startNode!=null) {
            this.addVertex(node);
            this.addEdge(startNode, node);
            this.startNode.incNumOfNextNodes(1);
            node.setBranchNum(branchNum);
            branchNum++;
        }
    }

    public void connectEnds(ProgramGraph graph){
        if(graph.endNode==null){
            return;
        }
        if(this.endNode!=null){
            graph.endNode.incNumOfNextNodes(1);
            this.addEdge(graph.endNode,this.endNode);
        }
    }

    public void connectStartEnd(){
        if(startNode!=null&&endNode!=null){
            this.startNode.incNumOfNextNodes(1);
            this.addEdge(startNode,endNode);
            endNode.setBranchNum(branchNum);
            branchNum++;
        }else{
            throw new RuntimeException("Can't connect null nodes in graph");
        }
    }


    public ProcessNode findNodeForProcess(String processName){
        BreadthFirstIterator<IReflexNode,DefaultEdge> iter = new BreadthFirstIterator<>(this);
        while(iter.hasNext()){
            IReflexNode node = iter.next();
            if(node instanceof ProcessNode && ((ProcessNode)node).getProcessName().equals(processName)) {
                return (ProcessNode)node;
            }
        }
        return null;
    }

    public StateNode findNodeForProcess(String processName, String stateName){
        BreadthFirstIterator<IReflexNode,DefaultEdge> iter = new BreadthFirstIterator<>(this);
        while(iter.hasNext()){
            IReflexNode node = iter.next();
            if(node instanceof StateNode n) {
                if (n.getProcessName().equals(processName) && n.getStateName().equals(stateName)){
                    return n;
                }
            }
        }
        return null;
    }

    public List<IReflexNode> getOutgoingNeighbours(IReflexNode node){
        return this.outgoingEdgesOf(node).stream().map(this::getEdgeTarget).sorted(new ReflexNodesComparator()).toList();
    }
}
