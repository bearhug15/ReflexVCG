package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface IAttributed {
    IReflexNode getAttributedNode();
    AttributedNodeType getContextType();

    boolean isReset();
    //void setReset(boolean reset);

    boolean isStateChanging();
    //void setStateChanging(boolean stateChanging);

    Map<ProcessNode,ChangeType> getProcChange();

    //void setProcChange(HashMap<ProcessNode, ChangeType> procChange);

    //void addProcessChange(ProcessNode ctx, ChangeType ty);
    Map<Map.Entry<ProcessNode,ChangeType>,Boolean> getPotProcChange();
    //void setPotProcChange(HashMap<ProcessNode,PotChange> potProcChange);
    //void addPotProcChange(ProcessNode ctx, PotChange change);

    Map<String,ProcessStatus> getProcStatuses();

    // reachTo: the states a path through the construct may leave the current process in. It
    // contains the current state itself when some path may leave the process where it was.
    Set<String> getReachTo();

    ArrayList<IAttributed> getAttributes();
    //void setAttributes(ArrayList<IAttributed> attributes);
    void addAttributes(IAttributed attr);

    void liftAttributes();
}
