package su.nsk.iae.reflex.ann;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import su.nsk.iae.reflex.antlr.ReflexALParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Lowers a Reflex-AL parse tree into {@link AnnExpr}.
 *
 * <p>A pure translation, like {@link su.nsk.iae.reflex.frontend.AstBuilder} for programs:
 * the precedence chain of the grammar collapses into binary nodes, and nothing is
 * resolved or typed here. Mangling and typing run afterwards, before translation.
 */
public final class AnnLowering {

    /** Lowers the body of one annotation. Returns null for a definition list. */
    public AnnExpr lowerBody(ReflexALParser.AnnotationBodyContext ctx) {
        if (ctx == null || ctx.specificationExpr() == null) {
            // A `define` annotation carries definitions rather than a formula.
            return null;
        }
        return lower(ctx.specificationExpr());
    }

    /** The definitions of a {@code define} annotation, empty for any other kind. */
    public List<AnnDefinition> lowerDefinitions(ReflexALParser.AnnotationBodyContext ctx) {
        List<AnnDefinition> definitions = new ArrayList<>();
        if (ctx == null || ctx.definitionList() == null) {
            return definitions;
        }
        for (ReflexALParser.DefinitionContext d : ctx.definitionList().definition()) {
            if (d.variableDefinition() != null) {
                ReflexALParser.VariableDefinitionContext v = d.variableDefinition();
                definitions.add(new AnnDefinition(v.Identifier().getText(), List.of(),
                        lower(v.specificationExpr())));
            } else {
                ReflexALParser.FunctionDefinitionContext f = d.functionDefinition();
                List<String> parameters = new ArrayList<>();
                if (f.parameterList() != null) {
                    // Parameters alternate typeName Identifier; only the names matter.
                    f.parameterList().Identifier().forEach(id -> parameters.add(id.getText()));
                }
                definitions.add(new AnnDefinition(f.Identifier().getText(), parameters,
                        lower(f.specificationExpr())));
            }
        }
        return definitions;
    }

    // ------------------------------------------------------------------ precedence

    public AnnExpr lower(ReflexALParser.SpecificationExprContext ctx) {
        return lower(ctx.implicationExpr());
    }

    private AnnExpr lower(ReflexALParser.ImplicationExprContext ctx) {
        List<ReflexALParser.EquivalenceExprContext> parts = ctx.equivalenceExpr();
        AnnExpr result = lower(parts.get(parts.size() - 1));
        // Implication associates to the right.
        for (int i = parts.size() - 2; i >= 0; i--) {
            result = new AnnExpr.Implication(lower(parts.get(i)), result);
        }
        return result;
    }

    private AnnExpr lower(ReflexALParser.EquivalenceExprContext ctx) {
        List<ReflexALParser.LogicalOrExprContext> parts = ctx.logicalOrExpr();
        AnnExpr result = lower(parts.get(0));
        for (int i = 1; i < parts.size(); i++) {
            result = new AnnExpr.Equivalence(result, lower(parts.get(i)));
        }
        return result;
    }

    private AnnExpr lower(ReflexALParser.LogicalOrExprContext ctx) {
        return leftAssociative(ctx.logicalAndExpr().stream().map(this::lower).toList(),
                ctx, AnnExpr.BinaryOp.OR);
    }

    private AnnExpr lower(ReflexALParser.LogicalAndExprContext ctx) {
        return leftAssociative(ctx.equalityExpr().stream().map(this::lower).toList(),
                ctx, AnnExpr.BinaryOp.AND);
    }

    private AnnExpr lower(ReflexALParser.EqualityExprContext ctx) {
        return leftAssociativeMixed(ctx.relationalExpr().stream().map(this::lower).toList(), ctx);
    }

    private AnnExpr lower(ReflexALParser.RelationalExprContext ctx) {
        return leftAssociativeMixed(ctx.bitwiseOrExpr().stream().map(this::lower).toList(), ctx);
    }

    private AnnExpr lower(ReflexALParser.BitwiseOrExprContext ctx) {
        return leftAssociative(ctx.bitwiseXorExpr().stream().map(this::lower).toList(),
                ctx, AnnExpr.BinaryOp.BIT_OR);
    }

    private AnnExpr lower(ReflexALParser.BitwiseXorExprContext ctx) {
        return leftAssociative(ctx.bitwiseAndExpr().stream().map(this::lower).toList(),
                ctx, AnnExpr.BinaryOp.BIT_XOR);
    }

    private AnnExpr lower(ReflexALParser.BitwiseAndExprContext ctx) {
        return leftAssociative(ctx.shiftExpr().stream().map(this::lower).toList(),
                ctx, AnnExpr.BinaryOp.BIT_AND);
    }

    private AnnExpr lower(ReflexALParser.ShiftExprContext ctx) {
        return leftAssociativeMixed(ctx.additiveExpr().stream().map(this::lower).toList(), ctx);
    }

    private AnnExpr lower(ReflexALParser.AdditiveExprContext ctx) {
        return leftAssociativeMixed(ctx.multiplicativeExpr().stream().map(this::lower).toList(), ctx);
    }

    private AnnExpr lower(ReflexALParser.MultiplicativeExprContext ctx) {
        return leftAssociativeMixed(ctx.unaryExpr().stream().map(this::lower).toList(), ctx);
    }

    /** Folds a chain whose operator is the same throughout. */
    private AnnExpr leftAssociative(List<AnnExpr> parts, ParserRuleContext ctx, AnnExpr.BinaryOp op) {
        AnnExpr result = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            result = new AnnExpr.Binary(op, result, parts.get(i));
        }
        return result;
    }

    /**
     * Folds a chain whose operators vary, reading them from the tokens between operands.
     * The grammar leaves them unlabelled, so they are recovered positionally.
     */
    private AnnExpr leftAssociativeMixed(List<AnnExpr> parts, ParserRuleContext ctx) {
        if (parts.size() == 1) {
            return parts.get(0);
        }
        List<String> operators = new ArrayList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof TerminalNode terminal) {
                operators.add(terminal.getText());
            }
        }
        AnnExpr result = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            result = new AnnExpr.Binary(
                    AnnExpr.BinaryOp.fromSymbol(operators.get(i - 1)), result, parts.get(i));
        }
        return result;
    }

    // ------------------------------------------------------------------ unary, postfix

    private AnnExpr lower(ReflexALParser.UnaryExprContext ctx) {
        if (ctx.unaryOperator() != null) {
            return new AnnExpr.Unary(
                    AnnExpr.UnaryOp.fromSymbol(ctx.unaryOperator().getText()), lower(ctx.unaryExpr()));
        }
        return lower(ctx.postfixExpr());
    }

    /** {@code primaryExpr scopeAccess*}: each scope wraps what precedes it. */
    private AnnExpr lower(ReflexALParser.PostfixExprContext ctx) {
        AnnExpr result = lower(ctx.primaryExpr());
        for (ReflexALParser.ScopeAccessContext scope : ctx.scopeAccess()) {
            result = wrapInScope(result, scope.scopeSpecifier());
        }
        return result;
    }

    private AnnExpr wrapInScope(AnnExpr base, ReflexALParser.ScopeSpecifierContext spec) {
        if (spec.pastScopeSpecifier() != null) {
            return new AnnExpr.Scope(AnnExpr.Scope.Kind.PAST, base,
                    lower(spec.pastScopeSpecifier().specificationExpr()));
        }
        AnnExpr.Scope.Kind kind = spec.getText().equals("pre")
                ? AnnExpr.Scope.Kind.PRE
                : AnnExpr.Scope.Kind.PREV;
        return new AnnExpr.Scope(kind, base, null);
    }

    // ------------------------------------------------------------------ primaries

    private AnnExpr lower(ReflexALParser.PrimaryExprContext ctx) {
        if (ctx.literal() != null) {
            return lowerLiteral(ctx.literal());
        }
        if (ctx.identifierPrimaryExpr() != null) {
            return lowerIdentifier(ctx.identifierPrimaryExpr());
        }
        if (ctx.quantifiedExpr() != null) {
            return lowerQuantifier(ctx.quantifiedExpr());
        }
        if (ctx.temporalExpr() != null) {
            return lowerTemporal(ctx.temporalExpr());
        }
        if (ctx.processExpr() != null) {
            return lowerProcess(ctx.processExpr());
        }
        // A parenthesised expression.
        return lower(ctx.specificationExpr());
    }

    private AnnExpr lowerLiteral(ReflexALParser.LiteralContext ctx) {
        String text = ctx.getText();
        AnnExpr.Literal.Kind kind;
        if (ctx.BooleanLiteral() != null) {
            kind = AnnExpr.Literal.Kind.BOOL;
        } else if (ctx.RealLiteral() != null) {
            kind = AnnExpr.Literal.Kind.REAL;
        } else if (ctx.TimeLiteral() != null) {
            kind = AnnExpr.Literal.Kind.TIME;
        } else {
            kind = AnnExpr.Literal.Kind.INTEGER;
        }
        return new AnnExpr.Literal(kind, text);
    }

    /** Either a variable with an access path, or the application of a define. */
    private AnnExpr lowerIdentifier(ReflexALParser.IdentifierPrimaryExprContext ctx) {
        String name = variableName(ctx.variable());

        if (ctx.functionArguments() != null) {
            List<AnnExpr> arguments = new ArrayList<>();
            if (ctx.functionArguments().argumentList() != null) {
                ctx.functionArguments().argumentList().specificationExpr()
                        .forEach(a -> arguments.add(lower(a)));
            }
            return new AnnExpr.Call(name, arguments);
        }

        List<AnnExpr.Access> accesses = new ArrayList<>();
        for (ReflexALParser.MemberAccessContext member : ctx.memberAccess()) {
            accesses.add(new AnnExpr.FieldAccess(member.Identifier().getText()));
        }
        for (ReflexALParser.ArraySubscriptContext subscript : ctx.arraySubscript()) {
            accesses.add(new AnnExpr.IndexAccess(lower(subscript.specificationExpr())));
        }
        return new AnnExpr.VarRef(name, accesses);
    }

    /**
     * The annotation grammar writes a qualified name as {@code a#b#c}, the same
     * separator mangling uses, so a name written qualified is already in final form.
     */
    private String variableName(ReflexALParser.VariableContext ctx) {
        return ctx.getText();
    }

    private AnnExpr lowerQuantifier(ReflexALParser.QuantifiedExprContext ctx) {
        boolean universal = ctx.universalQuantification() != null;
        ReflexALParser.QuantifiedVariableListContext variables;
        ReflexALParser.SpecificationExprContext body;
        if (universal) {
            variables = ctx.universalQuantification().quantifiedVariableList();
            body = ctx.universalQuantification().specificationExpr();
        } else {
            variables = ctx.existentialQuantification().quantifiedVariableList();
            body = ctx.existentialQuantification().specificationExpr();
        }

        List<AnnExpr.BoundVar> bound = new ArrayList<>();
        for (ReflexALParser.QuantifiedVariableContext v : variables.quantifiedVariable()) {
            bound.add(new AnnExpr.BoundVar(v.Identifier().getText(),
                    v.quantifierDomain() == null ? null : lowerDomain(v.quantifierDomain().domainExpr())));
        }
        return new AnnExpr.Quantifier(
                universal ? AnnExpr.Quantifier.Kind.FORALL : AnnExpr.Quantifier.Kind.EXISTS,
                bound, lower(body));
    }

    private AnnExpr.Domain lowerDomain(ReflexALParser.DomainExprContext ctx) {
        if (ctx.rangeDomain() != null) {
            List<ReflexALParser.SpecificationExprContext> bounds =
                    ctx.rangeDomain().specificationExpr();
            return new AnnExpr.RangeDomain(lower(bounds.get(0)), lower(bounds.get(1)));
        }
        if (ctx.setDomain() != null) {
            List<ReflexALParser.SpecificationExprContext> members =
                    ctx.setDomain().specificationExpr();
            return new AnnExpr.SetDomain(lower(members.get(0)),
                    members.size() > 1 ? lower(members.get(1)) : null);
        }
        if (ctx.typeName() != null) {
            return new AnnExpr.TypeDomain(ctx.typeName().getText());
        }
        return new AnnExpr.ExprDomain(lowerIdentifier(ctx.identifierPrimaryExpr()));
    }

    private AnnExpr lowerTemporal(ReflexALParser.TemporalExprContext ctx) {
        if (ctx.previousExpr() != null) {
            return temporal(AnnExpr.Temporal.Kind.PREVIOUSLY, ctx.previousExpr().specificationExpr());
        }
        if (ctx.nextExpr() != null) {
            return temporal(AnnExpr.Temporal.Kind.NEXT, ctx.nextExpr().specificationExpr());
        }
        if (ctx.onceExpr() != null) {
            return temporal(AnnExpr.Temporal.Kind.ONCE, ctx.onceExpr().specificationExpr());
        }
        if (ctx.timerExpr() != null) {
            return temporal(AnnExpr.Temporal.Kind.TIMER, ctx.timerExpr().specificationExpr());
        }
        if (ctx.duringExpr() != null) {
            List<ReflexALParser.SpecificationExprContext> args =
                    ctx.duringExpr().specificationExpr();
            return new AnnExpr.Temporal(AnnExpr.Temporal.Kind.DURING,
                    lower(args.get(0)), lower(args.get(1)), lower(args.get(2)));
        }
        if (ctx.withinExpr() != null) {
            return binaryTemporal(AnnExpr.Temporal.Kind.WITHIN, ctx.withinExpr().specificationExpr());
        }
        if (ctx.stableExpr() != null) {
            return binaryTemporal(AnnExpr.Temporal.Kind.STABLE, ctx.stableExpr().specificationExpr());
        }
        if (ctx.cooldownExpr() != null) {
            return binaryTemporal(AnnExpr.Temporal.Kind.COOLDOWN,
                    ctx.cooldownExpr().specificationExpr());
        }
        ReflexALParser.OnExprContext on = ctx.onExpr();
        return new AnnExpr.Temporal(AnnExpr.Temporal.Kind.ON,
                lower(on.trigger), lower(on.property), null);
    }

    private AnnExpr temporal(AnnExpr.Temporal.Kind kind,
                             ReflexALParser.SpecificationExprContext argument) {
        return new AnnExpr.Temporal(kind, lower(argument), null, null);
    }

    private AnnExpr binaryTemporal(AnnExpr.Temporal.Kind kind,
                                   List<ReflexALParser.SpecificationExprContext> args) {
        return new AnnExpr.Temporal(kind, lower(args.get(0)), lower(args.get(1)), null);
    }

    private AnnExpr lowerProcess(ReflexALParser.ProcessExprContext ctx) {
        if (ctx.inExpr() != null) {
            List<TerminalNode> names = ctx.inExpr().Identifier();
            return new AnnExpr.InState(names.get(0).getText(), names.get(1).getText());
        }
        return new AnnExpr.LocalTime(ctx.timeExpr().Identifier().getText());
    }
}
