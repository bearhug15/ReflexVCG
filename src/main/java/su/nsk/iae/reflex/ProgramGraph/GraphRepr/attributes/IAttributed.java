package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;

import java.util.ArrayList;
import java.util.Map;

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

    ArrayList<IAttributed> getAttributes();
    //void setAttributes(ArrayList<IAttributed> attributes);
    void addAttributes(IAttributed attr);

    void liftAttributes();
}
