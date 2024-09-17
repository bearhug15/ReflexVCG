package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.antlr.ReflexParser;


import java.util.HashMap;

public class ProcessAttributes implements IAttributed {

    ParserRuleContext attributedContext;
    String state;

    boolean reachE = false;
    boolean reachS = false;
    boolean startS = true;

    public ProcessAttributes(ReflexParser.ProcessContext attributedContext){
        this.attributedContext = attributedContext;
    }

    public void setAttributedContext(ParserRuleContext attributedContext) {
        this.attributedContext = attributedContext;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public ParserRuleContext getAttributedContext() {
        return attributedContext;
    }

    @Override
    public AttributedContextType getContextType() {
        return AttributedContextType.Process;
    }

    public boolean isReachE() {
        return reachE;
    }

    public void setReachE(boolean reachE) {
        this.reachE = reachE;
    }

    public boolean isReachS() {
        return reachS;
    }

    public void setReachS(boolean reachS) {
        this.reachS = reachS;
    }

    public boolean isStartS() {
        return startS;
    }

    public void setStartS(boolean startS) {
        this.startS = startS;
    }
}
