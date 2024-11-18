package su.nsk.iae.reflex.vcg;


import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.CrossComponentIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class Main {


    static class MyListener extends TraversalListenerAdapter<String, String> {

        SimpleDirectedGraph<String, String> g;
        private boolean newComponent;
        private String reference;
        public MyListener(SimpleDirectedGraph<String, String> g) {
            this.g = g;
        }

        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
            //newComponent = true;
            //System.out.println(newComponent);
        }

        @Override
        public void vertexTraversed(VertexTraversalEvent<String> e) {
            String vertex = e.getVertex();

            //if (newComponent) {
            //    reference = vertex;
            //   newComponent = false;
            //}
            //int l = DijkstraShortestPath.findPathBetween( g, reference, vertex).getLength();
            //String x = "";
            //for (int i=0; i<l; i++) x+= "\t";
            System.out.println("vertex: " + vertex);
        }

        @Override
        public void vertexFinished(VertexTraversalEvent<String> e){
            System.out.println("Finished vertex: " + e.getVertex());
        }
    }


    /*public static void main(String[] args) {
        SimpleDirectedGraph<String,String> g = new SimpleDirectedGraph<String, String>(String.class);



        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addVertex("E");
        g.addVertex("F");
        g.addVertex("G");



        g.addEdge("A", "B", "e1");
        g.addEdge("B", "D", "e3");
        g.addEdge("A", "C", "e2");
        g.addEdge("C", "D", "e4");
        g.addEdge("D", "E", "e5");
        g.addEdge("D", "F", "e6");
        g.addEdge("E", "G", "e7");
        g.addEdge("F", "G", "e8");

        GraphIterator<String, String> iterator = new DepthFirstIterator<>(g);
        iterator.addTraversalListener( new MyListener( g));

        while (iterator.hasNext()) {
            iterator.next();
//			System.out.println( iterator.next() );
        }
        System.out.println(g);
    }*/

}
