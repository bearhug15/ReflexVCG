// Generated from C:/Users/iarte/IdeaProjects/ReflexVCG/src/main/java/su/nsk/iae/reflex/antlr/Reflex.g4 by ANTLR 4.13.1
package su.nsk.iae.reflex.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ReflexParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ReflexVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ReflexParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(ReflexParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#clockDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClockDefinition(ReflexParser.ClockDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#process}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcess(ReflexParser.ProcessContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#state}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitState(ReflexParser.StateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(ReflexParser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#annotationKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationKey(ReflexParser.AnnotationKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#importedVariableList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportedVariableList(ReflexParser.ImportedVariableListContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#processVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcessVariable(ReflexParser.ProcessVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#globalVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalVariable(ReflexParser.GlobalVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#physicalVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhysicalVariable(ReflexParser.PhysicalVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#portMapping}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPortMapping(ReflexParser.PortMappingContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#programVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgramVariable(ReflexParser.ProgramVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#timeoutFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#timeAmountOrRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeAmountOrRef(ReflexParser.TimeAmountOrRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#functionDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDecl(ReflexParser.FunctionDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#port}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPort(ReflexParser.PortContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#const}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst(ReflexParser.ConstContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#enum}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnum(ReflexParser.EnumContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#enumMember}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumMember(ReflexParser.EnumMemberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EmptySt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptySt(ReflexParser.EmptyStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CompoundSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundSt(ReflexParser.CompoundStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StartProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartProcessSt(ReflexParser.StartProcessStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StopProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStopProcessSt(ReflexParser.StopProcessStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ErrorProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorProcessSt(ReflexParser.ErrorProcessStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code RestartSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestartSt(ReflexParser.RestartStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ResetSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetSt(ReflexParser.ResetStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SetStateSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStateSt(ReflexParser.SetStateStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfElseSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElseSt(ReflexParser.IfElseStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SwitchSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchSt(ReflexParser.SwitchStContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprSt(ReflexParser.ExprStContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#statementSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementSeq(ReflexParser.StatementSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(ReflexParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#ifElseStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElseStat(ReflexParser.IfElseStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#switchStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchStat(ReflexParser.SwitchStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#caseStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseStat(ReflexParser.CaseStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#defaultStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultStat(ReflexParser.DefaultStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#switchOptionStatSeq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#startProcStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartProcStat(ReflexParser.StartProcStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#stopProcStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStopProcStat(ReflexParser.StopProcStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#errorProcStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorProcStat(ReflexParser.ErrorProcStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#restartStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestartStat(ReflexParser.RestartStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#resetStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResetStat(ReflexParser.ResetStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#setStateStat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStateStat(ReflexParser.SetStateStatContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(ReflexParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#checkStateExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#stateQual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStateQual(ReflexParser.StateQualContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#infixOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInfixOp(ReflexParser.InfixOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#postfixOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOp(ReflexParser.PostfixOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Id}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(ReflexParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Integer}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger(ReflexParser.IntegerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Float}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloat(ReflexParser.FloatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(ReflexParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Time}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTime(ReflexParser.TimeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ClosedExpression}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClosedExpression(ReflexParser.ClosedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PrimaryExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpr(ReflexParser.PrimaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncCallExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCallExpr(ReflexParser.FuncCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PostfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInfixOpExpr(ReflexParser.InfixOpExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast(ReflexParser.CastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Add}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd(ReflexParser.AddContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Shift}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShift(ReflexParser.ShiftContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BitOr}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitOr(ReflexParser.BitOrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(ReflexParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Mul}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMul(ReflexParser.MulContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CheckState}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckState(ReflexParser.CheckStateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary(ReflexParser.UnaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BitXor}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitXor(ReflexParser.BitXorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Equal}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqual(ReflexParser.EqualContext ctx);
	/**
	 * Visit a parse tree produced by the {@code And}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(ReflexParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BitAnd}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitAnd(ReflexParser.BitAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(ReflexParser.AssignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Compare}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompare(ReflexParser.CompareContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#unaryOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOp(ReflexParser.UnaryOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#addOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddOp(ReflexParser.AddOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#assignOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignOp(ReflexParser.AssignOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link ReflexParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(ReflexParser.TypeContext ctx);
}