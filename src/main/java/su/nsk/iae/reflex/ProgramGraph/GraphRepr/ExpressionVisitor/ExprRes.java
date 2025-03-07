package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProcessStatus;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.UniversalAttributes;
import su.nsk.iae.reflex.expression.SymbolicExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ExprRes {

    Optional<String> getCondition();
    Optional<String> getFullCondition();
    Optional<String> getProcessesStatusesCondition();
    SymbolicExpression getExpr();
    String getState();
    Optional<String> getDomain();
    Optional<Boolean> getBooleanValue();

    Optional<Map<String, ProcessStatus>> getProcessesStatuses();

}
