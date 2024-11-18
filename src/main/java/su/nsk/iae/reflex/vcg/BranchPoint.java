package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.formulas.Formula;
import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.IAttributed;

public class BranchPoint {
    Formula formula;
    IAttributed attributed;
    ReflexParser.IfElseStContext ifCtx=null;
    ReflexParser.SwitchStContext switchCtx=null;
    ReflexParser.ProcessContext processCtx=null;
    ReflexParser.TimeoutFunctionContext timeoutCtx=null;
    Integer branch;
    Integer stateCount;
    String processName;
    String stateName;

    public BranchPoint(Formula formula, IAttributed attributed,ReflexParser.IfElseStContext ifCtx, Integer branch, Integer stateCount, String processName, String stateName) {
        this.formula = formula;
        this.attributed = attributed;
        this.ifCtx = ifCtx;
        this.branch = branch;
        this.stateCount = stateCount;
        this.processName = processName;
        this.stateName = stateName;
    }
    public BranchPoint(Formula formula, IAttributed attributed,ReflexParser.SwitchStContext switchCtx, Integer branch, Integer stateCount, String processName, String stateName) {
        this.formula = formula;
        this.attributed = attributed;
        this.switchCtx = switchCtx;
        this.branch = branch;
        this.stateCount = stateCount;
        this.processName = processName;
        this.stateName = stateName;
    }
    public BranchPoint(Formula formula, IAttributed attributed, ReflexParser.TimeoutFunctionContext timeoutCtx, Integer branch, Integer stateCount, String processName, String stateName) {
        this.formula = formula;
        this.attributed = attributed;
        this.timeoutCtx = timeoutCtx;
        this.branch = branch;
        this.stateCount = stateCount;
        this.processName = processName;
        this.stateName = stateName;
    }
    public BranchPoint(Formula formula, IAttributed attributed, ReflexParser.ProcessContext processCtx, Integer branch, Integer stateCount, String processName, String stateName) {
        this.formula = formula;
        this.attributed = attributed;
        this.processCtx = processCtx;
        this.branch = branch;
        this.stateCount = stateCount;
        this.processName = processName;
        this.stateName = stateName;
    }
}
