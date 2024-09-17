package su.nsk.iae.reflex.staticAnalysis.attributes;

import org.antlr.v4.runtime.ParserRuleContext;

public interface IAttributed {
    ParserRuleContext getAttributedContext();
    AttributedContextType getContextType();
}
