package su.nsk.iae.reflex.staticAnalysis;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.antlr.ReflexParser;

public class SwitchCaseAttributes implements Attributed{
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;
}
