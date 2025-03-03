package su.nsk.iae.reflex.ProgramGraph.GraphRepr.RuleChecker;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.IAttributed;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributedPath;

public interface IRuleChecker {
    public boolean checkRules(AttributedPath path, IAttributed attr);
}
