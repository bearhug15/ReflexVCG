package su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.IReflexNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProcessNode;

import java.util.ArrayList;
import java.util.HashMap;

public interface IAttributed {
    IReflexNode getAttributedContext();
    AttributedNodeType getContextType();

    boolean isReset();
    void setReset(boolean reset);

    boolean isStateChanging();
    void setStateChanging(boolean stateChanging);

    HashMap<ProcessNode,ChangeType> getProcChange();

    void setProcChange(HashMap<ProcessNode, ChangeType> procChange);

    void addProcessChange(ProcessNode ctx, ChangeType ty);
    HashMap<ProcessNode,PotChange> getPotProcChange();
    void setPotProcChange(HashMap<ProcessNode,PotChange> potProcChange);
    void addPotProcChange(ProcessNode ctx, PotChange change);

    ArrayList<IAttributed> getAttributes();
    void setAttributes(ArrayList<IAttributed> attributes);
    void addAttributes(IAttributed attr);

    void liftAttributes();
}
