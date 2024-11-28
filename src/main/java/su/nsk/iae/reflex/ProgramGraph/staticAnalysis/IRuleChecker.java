package su.nsk.iae.reflex.ProgramGraph.staticAnalysis;

import su.nsk.iae.reflex.ProgramGraph.staticAnalysis.attributes.IAttributed;

public interface IRuleChecker {
    public boolean checkRules(AttributedPath path, IAttributed attr);
}
