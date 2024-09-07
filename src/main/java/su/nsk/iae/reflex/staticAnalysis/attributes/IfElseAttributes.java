package su.nsk.iae.reflex.staticAnalysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.Vector;

public class IfElseAttributes implements Attributed{
    ParserRuleContext attributedContext;
    ReflexParser.ProcessContext rootProcess;
    ReflexParser.StateContext rootState;
    Vector<ReflexParser.ProcessContext> start = null;
    Vector<ReflexParser.ProcessContext> stop = null;
    Vector<ReflexParser.ProcessContext> error = null;
    Vector<Pair<ReflexParser.ProcessContext,ProcessActivity>> activity= null;
    boolean reset = false;
    boolean stateChange = false;
}
