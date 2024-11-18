package su.nsk.iae.reflex.ProgramGraph;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;

import java.util.Comparator;

class ReflexNodesComparator implements Comparator<IReflexNode> {
    @Override
    public int compare(IReflexNode o1, IReflexNode o2) {
        return o1.getBranchNum().compareTo(o2.getBranchNum());
    }
}
