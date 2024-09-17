package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.antlr.ReflexParser;

public class IfElseCore implements IAttributed {
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;
    IfElseAttributes trueAttributes;

    IfElseAttributes falseAttributes;

    public IfElseCore(ReflexParser.ProcessContext process, ReflexParser.StateContext state, ReflexParser.IfElseStatContext attributedContext){
        this.attributedContext = attributedContext;
        rootProcess = process;
        rootState = state;
    }

    public void setAttributedContext(ParserRuleContext attributedContext) {
        this.attributedContext = attributedContext;
    }

    public ReflexParser.ProcessContext getRootProcess() {
        return rootProcess;
    }

    public void setRootProcess(ReflexParser.ProcessContext rootProcess) {
        this.rootProcess = rootProcess;
    }

    public ReflexParser.StateContext getRootState() {
        return rootState;
    }

    public void setRootState(ReflexParser.StateContext rootState) {
        this.rootState = rootState;
    }

    public IfElseAttributes getTrueAttributes() {
        return trueAttributes;
    }

    public void setTrueAttributes(IfElseAttributes trueAttributes) {
        this.trueAttributes = trueAttributes;
    }

    public IfElseAttributes getFalseAttributes() {
        return falseAttributes;
    }

    public void setFalseAttributes(IfElseAttributes falseAttributes) {
        this.falseAttributes = falseAttributes;
    }

    @Override
    public ParserRuleContext getAttributedContext() {
        return attributedContext;
    }

    @Override
    public AttributedContextType getContextType() {
        return AttributedContextType.IfElse;
    }
}
