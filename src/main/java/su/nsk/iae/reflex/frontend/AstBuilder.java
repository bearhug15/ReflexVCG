package su.nsk.iae.reflex.frontend;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import su.nsk.iae.reflex.antlr.NewReflexParser;
import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrDecl;
import su.nsk.iae.reflex.ir.IrExpr;
import su.nsk.iae.reflex.ir.IrNode;
import su.nsk.iae.reflex.ir.IrProcess;
import su.nsk.iae.reflex.ir.IrProgram;
import su.nsk.iae.reflex.ir.IrState;
import su.nsk.iae.reflex.ir.IrStmt;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.ir.TimeRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Lowers a NewReflex parse tree into the Reflex IR.
 *
 * <p>This is a pure translation: it changes representation but not meaning. Everything
 * that rewrites the program - name mangling, cast insertion, normalisation - happens in
 * the preprocessing passes that run on the IR afterwards.
 *
 * <p>If an {@link AnnotationBinder} is supplied, the Reflex-AL annotations carried in
 * comments are attached to the constructs they precede as the tree is walked.
 */
public final class AstBuilder {

    private final AnnotationBinder annotations;

    public AstBuilder() {
        this(null);
    }

    public AstBuilder(AnnotationBinder annotations) {
        this.annotations = annotations;
    }

    // ------------------------------------------------------------------ program

    public IrProgram build(NewReflexParser.ProgramContext ctx) {
        IrProgram program = new IrProgram(ctx.name.getText(), buildClock(ctx.clock));
        claim(program, ctx);

        for (NewReflexParser.ConstContext c : ctx.consts) {
            program.getConstants().add(buildConstant(c));
        }
        for (NewReflexParser.EnumContext e : ctx.enums) {
            program.getEnums().add(buildEnum(e));
        }
        for (NewReflexParser.StructDeclarationContext s : ctx.structures) {
            program.getStructs().add(buildStruct(s));
        }
        for (NewReflexParser.FunctionDeclContext f : ctx.functions) {
            program.getFunctions().add(buildFunction(f));
        }
        for (NewReflexParser.PortContext p : ctx.ports) {
            program.getPorts().add(buildPort(p));
        }
        for (NewReflexParser.GlobalVariableContext v : ctx.globalVars) {
            program.getGlobalVariables().add(buildGlobalVariable(v));
        }
        for (NewReflexParser.ImportBlockContext i : ctx.imports) {
            program.getImports().add(buildImportBlock(i));
        }
        for (NewReflexParser.NodeDeclContext n : ctx.nodes) {
            program.getNodes().add(buildNode(n));
        }
        for (NewReflexParser.ProcessContext p : ctx.processes) {
            program.getProcesses().add(buildProcess(p));
        }
        return program;
    }

    private TimeRef buildClock(NewReflexParser.ClockDefinitionContext ctx) {
        TimeRef clock = ctx.intValue != null
                ? TimeRef.ofInteger(ctx.intValue.getText())
                : TimeRef.ofTimeLiteral(ctx.timeValue.getText());
        return origin(clock, ctx);
    }

    private IrDecl.Node buildNode(NewReflexParser.NodeDeclContext ctx) {
        IrDecl.Node node = new IrDecl.Node(ctx.name.getText(), buildClock(ctx.clock));
        claim(node, ctx);
        for (NewReflexParser.ConstContext c : ctx.consts) {
            node.getConstants().add(buildConstant(c));
        }
        for (NewReflexParser.GlobalVariableContext v : ctx.nodeVars) {
            node.getVariables().add(buildGlobalVariable(v));
        }
        return node;
    }

    private IrProcess buildProcess(NewReflexParser.ProcessContext ctx) {
        IrProcess process = new IrProcess(ctx.name.getText(), ctx.node == null ? null : ctx.node.getText());
        claim(process, ctx);
        for (NewReflexParser.ImportedVariableListContext i : ctx.imports) {
            process.getSharedImports().add(buildSharedImport(i));
        }
        for (NewReflexParser.ProcessVariableContext v : ctx.variables) {
            process.getVariables().add(buildProcessVariable(v));
        }
        for (NewReflexParser.StateContext s : ctx.states) {
            process.getStates().add(buildState(s));
        }
        return process;
    }

    private IrState buildState(NewReflexParser.StateContext ctx) {
        IrState state = new IrState(ctx.name.getText(), ctx.looped != null,
                buildStatementSeq(ctx.stateFunction));
        claim(state, ctx);
        if (ctx.func != null) {
            NewReflexParser.TimeoutFunctionContext t = ctx.func;
            IrState.Timeout timeout = new IrState.Timeout(
                    buildTimeRef(t.timeAmountOrRef()), buildStatement(t.body));
            state.setTimeout(origin(timeout, t));
        }
        return state;
    }

    // ------------------------------------------------------------------ declarations

    private IrDecl.Constant buildConstant(NewReflexParser.ConstContext ctx) {
        IrDecl.Constant constant = new IrDecl.Constant(
                ctx.name.getText(), buildType(ctx.varType), buildExpression(ctx.value));
        return claim(constant, ctx);
    }

    private IrDecl.Enum buildEnum(NewReflexParser.EnumContext ctx) {
        List<IrDecl.EnumMember> members = new ArrayList<>();
        for (NewReflexParser.EnumMemberContext m : ctx.enumMembers) {
            IrDecl.EnumMember member = new IrDecl.EnumMember(
                    m.name.getText(), m.value == null ? null : buildExpression(m.value));
            members.add(origin(member, m));
        }
        return claim(new IrDecl.Enum(ctx.identifier.getText(), members), ctx);
    }

    private IrDecl.Struct buildStruct(NewReflexParser.StructDeclarationContext ctx) {
        List<IrDecl.Variable> fields = new ArrayList<>();
        for (NewReflexParser.ProgramVariableContext v : ctx.variables) {
            fields.add(buildProgramVariable(v));
        }
        return claim(new IrDecl.Struct(ctx.name.getText(), fields), ctx);
    }

    /** Reflex declares function signatures without a name; the IR records one anyway. */
    private IrDecl.Function buildFunction(NewReflexParser.FunctionDeclContext ctx) {
        List<IrType> parameters = new ArrayList<>();
        for (NewReflexParser.TypeContext t : ctx.argTypes) {
            parameters.add(buildType(t));
        }
        IrDecl.Function function = new IrDecl.Function(
                "function@" + ctx.getStart().getLine(), buildType(ctx.returnType), parameters);
        return claim(function, ctx);
    }

    private IrDecl.Port buildPort(NewReflexParser.PortContext ctx) {
        IrDecl.Port.Direction direction = ctx.varType.getText().equals("input")
                ? IrDecl.Port.Direction.INPUT
                : IrDecl.Port.Direction.OUTPUT;
        IrDecl.Port port = new IrDecl.Port(ctx.name.getText(), direction,
                ctx.addr1.getText(), ctx.addr2.getText(), ctx.size.getText());
        return claim(port, ctx);
    }

    private IrDecl buildGlobalVariable(NewReflexParser.GlobalVariableContext ctx) {
        return ctx.physicalVariable() != null
                ? buildPhysicalVariable(ctx.physicalVariable())
                : buildProgramVariable(ctx.programVariable());
    }

    private IrDecl buildProcessVariable(NewReflexParser.ProcessVariableContext ctx) {
        IrDecl decl = ctx.physicalVariable() != null
                ? buildPhysicalVariable(ctx.physicalVariable())
                : buildProgramVariable(ctx.programVariable());
        if (ctx.shared != null && decl instanceof IrDecl.Variable variable) {
            variable.setShared(true);
        }
        return decl;
    }

    private IrDecl.Variable buildProgramVariable(NewReflexParser.ProgramVariableContext ctx) {
        IrType type = buildType(ctx.varType);
        // Dimensions are written left to right but nest right to left: in `int32 g[2][3]`
        // the element type of the outer array is `int32[3]`.
        if (ctx.dims != null) {
            List<NewReflexParser.ArrayDimContext> dims = ctx.dims.dim;
            for (int i = dims.size() - 1; i >= 0; i--) {
                type = new IrType.Array(type, constantDimension(dims.get(i)));
            }
        }
        IrExpr initializer = ctx.init == null ? null : buildInitializer(ctx.init);
        IrDecl.Variable variable = new IrDecl.Variable(ctx.name.getText(), type, initializer);
        return claim(variable, ctx);
    }

    /**
     * A dimension written as a plain integer literal, or null when it is absent (taken
     * from the initialiser) or a non-literal expression that only later stages resolve.
     */
    private Integer constantDimension(NewReflexParser.ArrayDimContext dim) {
        if (dim.size == null) {
            return null;
        }
        try {
            return Integer.valueOf(dim.size.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private IrDecl.PhysicalVariable buildPhysicalVariable(NewReflexParser.PhysicalVariableContext ctx) {
        String bit = null;
        if (ctx.bitId != null) {
            bit = ctx.bitId.getText();
        } else if (ctx.bitNum != null) {
            bit = ctx.bitNum.getText();
        }
        IrDecl.PhysicalVariable variable = new IrDecl.PhysicalVariable(
                ctx.name.getText(),
                buildType(ctx.varType),
                ctx.isDirect != null && ctx.isDirect.getText().equals("direct"),
                ctx.readId == null ? null : ctx.readId.getText(),
                ctx.writeId == null ? null : ctx.writeId.getText(),
                ctx.configId == null ? null : ctx.configId.getText(),
                bit);
        return claim(variable, ctx);
    }

    private IrDecl.SharedImport buildSharedImport(NewReflexParser.ImportedVariableListContext ctx) {
        List<String> names = new ArrayList<>();
        for (Token t : ctx.variables) {
            names.add(t.getText());
        }
        return claim(new IrDecl.SharedImport(names, ctx.processID.getText()), ctx);
    }

    private IrDecl.ImportBlock buildImportBlock(NewReflexParser.ImportBlockContext ctx) {
        List<IrDecl.ImportBlock.Element> elements = new ArrayList<>();
        for (NewReflexParser.ImportElementContext e : ctx.importElements) {
            if (e.iVector() != null) {
                elements.add(new IrDecl.ImportBlock.Element(
                        IrDecl.ImportBlock.ElementKind.VECTOR, e.iVector().name.getText()));
            } else if (e.iRegister() != null) {
                elements.add(new IrDecl.ImportBlock.Element(
                        IrDecl.ImportBlock.ElementKind.REGISTER, e.iRegister().name.getText()));
            } else {
                elements.add(new IrDecl.ImportBlock.Element(
                        IrDecl.ImportBlock.ElementKind.BIT, e.iBit().name.getText()));
            }
        }
        return claim(new IrDecl.ImportBlock(ctx.name.getText(), elements), ctx);
    }

    // ------------------------------------------------------------------ types

    private IrType buildType(NewReflexParser.TypeContext ctx) {
        if (ctx.builtinType() != null) {
            return IrType.of(IrType.BuiltinKind.fromKeyword(ctx.builtinType().getText()));
        }
        // A struct or enum reference; resolution happens once all declarations are known.
        return new IrType.Named(ctx.typeName.getText());
    }

    // ------------------------------------------------------------------ statements

    private List<IrStmt> buildStatementSeq(NewReflexParser.StatementSeqContext ctx) {
        List<IrStmt> statements = new ArrayList<>();
        if (ctx != null) {
            for (NewReflexParser.StatementContext s : ctx.statements) {
                statements.add(buildStatement(s));
            }
        }
        return statements;
    }

    private IrStmt buildStatement(NewReflexParser.StatementContext ctx) {
        // Claimed before the children are built: a statement and the expression or
        // declaration inside it share a start token, and the statement should win.
        List<Annotation> claimed = claimAnnotations(ctx);
        IrStmt stmt = origin(buildStatementKind(ctx), ctx);
        claimed.forEach(stmt::addAnnotation);
        return stmt;
    }

    private IrStmt buildStatementKind(NewReflexParser.StatementContext ctx) {
        if (ctx instanceof NewReflexParser.EmptyStContext) {
            return new IrStmt.Empty();
        }
        if (ctx instanceof NewReflexParser.CompoundStContext c) {
            return new IrStmt.Block(buildStatementSeq(c.compoundStatement().body));
        }
        if (ctx instanceof NewReflexParser.StartProcessStContext c) {
            NewReflexParser.StartProcStatContext s = c.startProcStat();
            return new IrStmt.ProcessControl(IrStmt.ControlKind.START, s.processId.getText());
        }
        if (ctx instanceof NewReflexParser.StopProcessStContext c) {
            NewReflexParser.StopProcStatContext s = c.stopProcStat();
            return new IrStmt.ProcessControl(IrStmt.ControlKind.STOP,
                    s.processId == null ? null : s.processId.getText());
        }
        if (ctx instanceof NewReflexParser.ErrorProcessStContext c) {
            NewReflexParser.ErrorProcStatContext s = c.errorProcStat();
            return new IrStmt.ProcessControl(IrStmt.ControlKind.ERROR,
                    s.processId == null ? null : s.processId.getText());
        }
        if (ctx instanceof NewReflexParser.RestartStContext) {
            return new IrStmt.ProcessControl(IrStmt.ControlKind.RESTART, null);
        }
        if (ctx instanceof NewReflexParser.ResetStContext) {
            return new IrStmt.ResetTimer();
        }
        if (ctx instanceof NewReflexParser.SetStateStContext c) {
            NewReflexParser.SetStateStatContext s = c.setStateStat();
            return s.stateId == null
                    ? new IrStmt.SetState(null, true)
                    : new IrStmt.SetState(s.stateId.getText(), false);
        }
        if (ctx instanceof NewReflexParser.IfElseStContext c) {
            NewReflexParser.IfElseStatContext s = c.ifElseStat();
            return new IrStmt.If(
                    buildExpression(s.cond),
                    buildStatement(s.then),
                    s.else_ == null ? null : buildStatement(s.else_));
        }
        if (ctx instanceof NewReflexParser.SwitchStContext c) {
            return buildSwitch(c.switchStat());
        }
        if (ctx instanceof NewReflexParser.ExprStContext c) {
            return new IrStmt.ExprStatement(buildExpression(c.expression()));
        }
        if (ctx instanceof NewReflexParser.GuardStContext c) {
            return buildGuarding(c.guardingStatement());
        }
        if (ctx instanceof NewReflexParser.IterStContext c) {
            return buildFor(c.iterationStat());
        }
        if (ctx instanceof NewReflexParser.CCodeStContext c) {
            String code = c.ccodeStat().code.getText();
            // Drop the leading '$' that delimits the token.
            return new IrStmt.CCode(code.startsWith("$") ? code.substring(1) : code);
        }
        if (ctx instanceof NewReflexParser.VariableStContext c) {
            return new IrStmt.LocalVar(buildProgramVariable(c.programVariable()));
        }
        throw new IllegalStateException(
                "Unhandled statement form " + ctx.getClass().getSimpleName() + at(ctx));
    }

    private IrStmt buildSwitch(NewReflexParser.SwitchStatContext ctx) {
        List<IrStmt.SwitchCase> cases = new ArrayList<>();
        for (NewReflexParser.CaseStatContext c : ctx.options) {
            NewReflexParser.SwitchOptionStatSeqContext body = c.switchOptionBody().switchOptionStatSeq();
            IrStmt.SwitchCase clause = new IrStmt.SwitchCase(
                    buildExpression(c.option), buildStatementSeq(body.body), body.break_ != null);
            cases.add(claim(clause, c));
        }
        if (ctx.defaultOption != null) {
            NewReflexParser.SwitchOptionStatSeqContext body =
                    ctx.defaultOption.switchOptionBody().switchOptionStatSeq();
            IrStmt.SwitchCase clause = new IrStmt.SwitchCase(
                    null, buildStatementSeq(body.body), body.break_ != null);
            cases.add(claim(clause, ctx.defaultOption));
        }
        return new IrStmt.Switch(buildExpression(ctx.expr), cases);
    }

    private IrStmt buildGuarding(NewReflexParser.GuardingStatementContext ctx) {
        if (ctx instanceof NewReflexParser.SliceContext) {
            return new IrStmt.Slice();
        }
        if (ctx instanceof NewReflexParser.WaitContext c) {
            return new IrStmt.Wait(buildExpression(c.waitHeader().cond), null, null);
        }
        if (ctx instanceof NewReflexParser.WaitOnTimeoutContext c) {
            return new IrStmt.Wait(
                    buildExpression(c.waitHeader().cond),
                    buildTimeRef(c.time),
                    buildStatement(c.body));
        }
        throw new IllegalStateException(
                "Unhandled guarding statement " + ctx.getClass().getSimpleName() + at(ctx));
    }

    private IrStmt buildFor(NewReflexParser.IterationStatContext ctx) {
        List<IrDecl.Variable> initDeclarations = new ArrayList<>();
        IrExpr initExpression = null;
        NewReflexParser.InitIterContext init = ctx.init;
        if (init.initList() != null) {
            for (NewReflexParser.ProgramVariableContext v : init.initList().inits) {
                initDeclarations.add(buildProgramVariable(v));
            }
        } else if (init.expression() != null) {
            initExpression = buildExpression(init.expression());
        }
        return new IrStmt.For(initDeclarations, initExpression,
                buildExpression(ctx.cond), buildExpression(ctx.upd), buildStatement(ctx.stat));
    }

    private TimeRef buildTimeRef(NewReflexParser.TimeAmountOrRefContext ctx) {
        TimeRef ref;
        if (ctx.time != null) {
            ref = TimeRef.ofTimeLiteral(ctx.time.getText());
        } else if (ctx.intTime != null) {
            ref = TimeRef.ofInteger(ctx.intTime.getText());
        } else {
            ref = TimeRef.ofName(ctx.ref.getText());
        }
        return origin(ref, ctx);
    }

    // ------------------------------------------------------------------ expressions

    private IrExpr buildInitializer(NewReflexParser.InitializerContext ctx) {
        if (ctx.aggregateInitializer() != null) {
            return buildAggregate(ctx.aggregateInitializer());
        }
        return buildExpression(ctx.expression());
    }

    private IrExpr buildAggregate(NewReflexParser.AggregateInitializerContext ctx) {
        List<IrExpr.Aggregate.Element> elements = new ArrayList<>();
        for (NewReflexParser.InitializerElementContext e : ctx.elements) {
            IrExpr.Access designator = null;
            if (e.designator() != null) {
                NewReflexParser.DesignatorContext d = e.designator();
                designator = d.field != null
                        ? new IrExpr.FieldAccess(d.field.getText())
                        : new IrExpr.IndexAccess(buildExpression(d.index));
            }
            elements.add(new IrExpr.Aggregate.Element(designator, buildInitializer(e.initializer())));
        }
        return origin(new IrExpr.Aggregate(elements), ctx);
    }

    private IrExpr buildExpression(NewReflexParser.ExpressionContext ctx) {
        IrExpr expr = buildExpressionKind(ctx);
        return origin(expr, ctx);
    }

    private IrExpr buildExpressionKind(NewReflexParser.ExpressionContext ctx) {
        if (ctx instanceof NewReflexParser.UnaryContext c) {
            return buildUnaryExpression(c.unaryExpression());
        }
        if (ctx instanceof NewReflexParser.CheckStateContext c) {
            NewReflexParser.CheckStateExpressionContext s = c.checkStateExpression();
            return new IrExpr.CheckState(s.processId.getText(),
                    processStatus(s.qual.getText()));
        }
        if (ctx instanceof NewReflexParser.CastContext c) {
            IrType target = IrType.of(IrType.BuiltinKind.fromKeyword(c.varType.getText()));
            return new IrExpr.Cast(target, buildExpression(c.expression()), null, false);
        }
        if (ctx instanceof NewReflexParser.MulContext c) {
            return binary(c.op.getText(), c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.AddContext c) {
            return binary(c.op.getText(), c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.ShiftContext c) {
            return binary(c.op.getText(), c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.CompareContext c) {
            return binary(c.op.getText(), c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.EqualContext c) {
            return binary(c.op.getText(), c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.BitAndContext c) {
            return binary("&", c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.BitXorContext c) {
            return binary("^", c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.BitOrContext c) {
            return binary("|", c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.AndContext c) {
            return binary("&&", c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.OrContext c) {
            return binary("||", c.expression(0), c.expression(1));
        }
        if (ctx instanceof NewReflexParser.AssignContext c) {
            return new IrExpr.Assign(
                    IrExpr.AssignOp.fromSymbol(c.assignOp().getText()),
                    buildVariable(c.variable()),
                    buildExpression(c.expression()));
        }
        throw new IllegalStateException(
                "Unhandled expression form " + ctx.getClass().getSimpleName() + at(ctx));
    }

    private IrExpr binary(String symbol, NewReflexParser.ExpressionContext left,
                          NewReflexParser.ExpressionContext right) {
        return new IrExpr.Binary(IrExpr.BinaryOp.fromSymbol(symbol),
                buildExpression(left), buildExpression(right));
    }

    private IrExpr buildUnaryExpression(NewReflexParser.UnaryExpressionContext ctx) {
        if (ctx instanceof NewReflexParser.PrimaryExprContext c) {
            return buildPrimary(c.primaryExpression());
        }
        if (ctx instanceof NewReflexParser.FuncCallExprContext c) {
            NewReflexParser.FunctionCallContext call = c.functionCall();
            List<IrExpr> args = new ArrayList<>();
            for (NewReflexParser.ExpressionContext a : call.args) {
                args.add(buildExpression(a));
            }
            return origin(new IrExpr.Call(call.functionID.getText(), args), call);
        }
        if (ctx instanceof NewReflexParser.PostfixOpExprContext c) {
            NewReflexParser.PostfixOpContext p = c.postfixOp();
            return origin(new IrExpr.IncDec(incDecOp(p.op.getText()), false, buildVariable(p.variable())), p);
        }
        if (ctx instanceof NewReflexParser.InfixOpExprContext c) {
            NewReflexParser.InfixOpContext p = c.infixOp();
            return origin(new IrExpr.IncDec(incDecOp(p.op.getText()), true, buildVariable(p.variable())), p);
        }
        if (ctx instanceof NewReflexParser.UnaryOpExprContext c) {
            return origin(new IrExpr.Unary(
                    IrExpr.UnaryOp.fromSymbol(c.op.getText()), buildExpression(c.expression())), c);
        }
        throw new IllegalStateException(
                "Unhandled unary expression " + ctx.getClass().getSimpleName() + at(ctx));
    }

    private IrExpr buildPrimary(NewReflexParser.PrimaryExpressionContext ctx) {
        if (ctx instanceof NewReflexParser.IdContext c) {
            return buildVariable(c.variable());
        }
        if (ctx instanceof NewReflexParser.IntegerLitContext c) {
            return origin(new IrExpr.Literal(IrExpr.Literal.Kind.INTEGER, c.integer().getText()), c);
        }
        if (ctx instanceof NewReflexParser.FloatLitContext c) {
            return origin(new IrExpr.Literal(IrExpr.Literal.Kind.FLOAT, c.floatVal().getText()), c);
        }
        if (ctx instanceof NewReflexParser.BoolContext c) {
            return origin(new IrExpr.Literal(IrExpr.Literal.Kind.BOOL, c.getText()), c);
        }
        if (ctx instanceof NewReflexParser.TimeContext c) {
            return origin(new IrExpr.Literal(IrExpr.Literal.Kind.TIME, c.getText()), c);
        }
        if (ctx instanceof NewReflexParser.ClosedExpressionContext c) {
            return buildExpression(c.expression());
        }
        throw new IllegalStateException(
                "Unhandled primary expression " + ctx.getClass().getSimpleName() + at(ctx));
    }

    private IrExpr.VarRef buildVariable(NewReflexParser.VariableContext ctx) {
        List<IrExpr.Access> accesses = new ArrayList<>();
        for (NewReflexParser.VariableAccessContext a : ctx.variableAccess()) {
            accesses.add(a.field != null
                    ? new IrExpr.FieldAccess(a.field.getText())
                    : new IrExpr.IndexAccess(buildExpression(a.index)));
        }
        return origin(new IrExpr.VarRef(ctx.varId.getText(), accesses), ctx);
    }

    private IrExpr.IncDecOp incDecOp(String symbol) {
        return symbol.equals("++") ? IrExpr.IncDecOp.INCREMENT : IrExpr.IncDecOp.DECREMENT;
    }

    private IrExpr.ProcessStatus processStatus(String keyword) {
        return switch (keyword) {
            case "active" -> IrExpr.ProcessStatus.ACTIVE;
            case "inactive" -> IrExpr.ProcessStatus.INACTIVE;
            case "stop" -> IrExpr.ProcessStatus.STOP;
            case "error" -> IrExpr.ProcessStatus.ERROR;
            default -> throw new IllegalArgumentException("Unknown process state qualifier: " + keyword);
        };
    }

    // ------------------------------------------------------------------ bookkeeping

    /** Records where a node came from. Does not touch annotations. */
    private <T extends IrNode> T origin(T node, ParserRuleContext ctx) {
        node.setSource(ctx);
        return node;
    }

    /**
     * Records origin and attaches the annotations bound to {@code ctx}.
     *
     * <p>Only construct-level nodes claim annotations - programs, nodes, processes,
     * states, statements and declarations - never subexpressions, so an annotation
     * before a statement is not swallowed by the first expression inside it.
     */
    private <T extends IrNode> T claim(T node, ParserRuleContext ctx) {
        node.setSource(ctx);
        for (Annotation annotation : claimAnnotations(ctx)) {
            node.addAnnotation(annotation);
        }
        return node;
    }

    /**
     * Takes the annotations bound to {@code ctx} before its children are built, so the
     * outermost construct starting at a token wins.
     */
    private List<Annotation> claimAnnotations(ParserRuleContext ctx) {
        return annotations == null ? List.of() : annotations.annotationsFor(ctx);
    }

    private static String at(ParserRuleContext ctx) {
        return " at line " + ctx.getStart().getLine() + ":" + ctx.getStart().getCharPositionInLine();
    }
}
