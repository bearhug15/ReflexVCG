package su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;



import su.nsk.iae.reflex.antlr.ReflexParser;

import java.util.ArrayList;
import java.util.List;

public interface ExpressionVisitor {
    public List<ExprRes> parseExpression(ReflexParser.ExpressionContext expr);
}
