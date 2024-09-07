package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;

public interface Attributed {
    ParserRuleContext getAttributedContext();
    AttributedContextType getContextType();
}
