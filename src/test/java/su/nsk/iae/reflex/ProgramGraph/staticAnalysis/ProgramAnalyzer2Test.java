package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProgramAnalyzer2Test {
    @Test
    void setsInter() {
        ProcessNode node1 = ProcessNode.ProcessNodes(null, "1").getKey();
        ProcessNode node2 = ProcessNode.ProcessNodes(null, "2").getKey();
        ProcessNode node3 = ProcessNode.ProcessNodes(null, "3").getKey();
        ProcessNode node4 = ProcessNode.ProcessNodes(null, "4").getKey();

        Set<ProcessNode> set0= Set.of();
        Set<ProcessNode> set1= Set.of(node1);
        Set<ProcessNode> set2= Set.of(node2);
        Set<ProcessNode> set3= Set.of(node3);
        Set<ProcessNode> set12= Set.of(node1,node2);
        Set<ProcessNode> set123= Set.of(node1,node2,node3);
        Set<ProcessNode> set34= Set.of(node3,node4);

        Set<Set<ProcessNode>> res = ProgramAnalyzer2.setsInter(Set.of(set0),set0);
        res.remove(Set.of());
        assertEquals(Set.of(),res);

        res = ProgramAnalyzer2.setsInter(Set.of(set1),set0);
        res.remove(Set.of());
        assertEquals(Set.of(set1),res);

        res = ProgramAnalyzer2.setsInter(Set.of(set1),set1);
        res.remove(Set.of());
        assertEquals(Set.of(set1),res);

        res = ProgramAnalyzer2.setsInter(Set.of(set12),set1);
        res.remove(Set.of());
        assertEquals(Set.of(set1,set2),res);

        res = ProgramAnalyzer2.setsInter(Set.of(set123),set34);
        res.remove(Set.of());
        assertEquals(Set.of(set12,set3),res);

        res = ProgramAnalyzer2.setsInter(Set.of(set34),set1);
        res.remove(Set.of());
        assertEquals(Set.of(set34),res);
    }

    @Test
    void setsDiv() {
    }
}