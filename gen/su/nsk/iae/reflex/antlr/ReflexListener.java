// Generated from C:/Users/iarte/Projects/ReflexVCG/src/main/java/su/nsk/iae/reflex/antlr/Reflex.g4 by ANTLR 4.13.1
package su.nsk.iae.reflex.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ReflexParser}.
 */
public interface ReflexListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ReflexParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(ReflexParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(ReflexParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#clockDefinition}.
	 * @param ctx the parse tree
	 */
	void enterClockDefinition(ReflexParser.ClockDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#clockDefinition}.
	 * @param ctx the parse tree
	 */
	void exitClockDefinition(ReflexParser.ClockDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#process}.
	 * @param ctx the parse tree
	 */
	void enterProcess(ReflexParser.ProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#process}.
	 * @param ctx the parse tree
	 */
	void exitProcess(ReflexParser.ProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#state}.
	 * @param ctx the parse tree
	 */
	void enterState(ReflexParser.StateContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#state}.
	 * @param ctx the parse tree
	 */
	void exitState(ReflexParser.StateContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(ReflexParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(ReflexParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#annotationKey}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationKey(ReflexParser.AnnotationKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#annotationKey}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationKey(ReflexParser.AnnotationKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#importedVariableList}.
	 * @param ctx the parse tree
	 */
	void enterImportedVariableList(ReflexParser.ImportedVariableListContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#importedVariableList}.
	 * @param ctx the parse tree
	 */
	void exitImportedVariableList(ReflexParser.ImportedVariableListContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#processVariable}.
	 * @param ctx the parse tree
	 */
	void enterProcessVariable(ReflexParser.ProcessVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#processVariable}.
	 * @param ctx the parse tree
	 */
	void exitProcessVariable(ReflexParser.ProcessVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#globalVariable}.
	 * @param ctx the parse tree
	 */
	void enterGlobalVariable(ReflexParser.GlobalVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#globalVariable}.
	 * @param ctx the parse tree
	 */
	void exitGlobalVariable(ReflexParser.GlobalVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#physicalVariable}.
	 * @param ctx the parse tree
	 */
	void enterPhysicalVariable(ReflexParser.PhysicalVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#physicalVariable}.
	 * @param ctx the parse tree
	 */
	void exitPhysicalVariable(ReflexParser.PhysicalVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#portMapping}.
	 * @param ctx the parse tree
	 */
	void enterPortMapping(ReflexParser.PortMappingContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#portMapping}.
	 * @param ctx the parse tree
	 */
	void exitPortMapping(ReflexParser.PortMappingContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#programVariable}.
	 * @param ctx the parse tree
	 */
	void enterProgramVariable(ReflexParser.ProgramVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#programVariable}.
	 * @param ctx the parse tree
	 */
	void exitProgramVariable(ReflexParser.ProgramVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#timeoutFunction}.
	 * @param ctx the parse tree
	 */
	void enterTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#timeoutFunction}.
	 * @param ctx the parse tree
	 */
	void exitTimeoutFunction(ReflexParser.TimeoutFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#timeAmountOrRef}.
	 * @param ctx the parse tree
	 */
	void enterTimeAmountOrRef(ReflexParser.TimeAmountOrRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#timeAmountOrRef}.
	 * @param ctx the parse tree
	 */
	void exitTimeAmountOrRef(ReflexParser.TimeAmountOrRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(ReflexParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(ReflexParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(ReflexParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(ReflexParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#const}.
	 * @param ctx the parse tree
	 */
	void enterConst(ReflexParser.ConstContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#const}.
	 * @param ctx the parse tree
	 */
	void exitConst(ReflexParser.ConstContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#enum}.
	 * @param ctx the parse tree
	 */
	void enterEnum(ReflexParser.EnumContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#enum}.
	 * @param ctx the parse tree
	 */
	void exitEnum(ReflexParser.EnumContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#enumMember}.
	 * @param ctx the parse tree
	 */
	void enterEnumMember(ReflexParser.EnumMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#enumMember}.
	 * @param ctx the parse tree
	 */
	void exitEnumMember(ReflexParser.EnumMemberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptySt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterEmptySt(ReflexParser.EmptyStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptySt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitEmptySt(ReflexParser.EmptyStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CompoundSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundSt(ReflexParser.CompoundStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CompoundSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundSt(ReflexParser.CompoundStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StartProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStartProcessSt(ReflexParser.StartProcessStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StartProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStartProcessSt(ReflexParser.StartProcessStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StopProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStopProcessSt(ReflexParser.StopProcessStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StopProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStopProcessSt(ReflexParser.StopProcessStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ErrorProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterErrorProcessSt(ReflexParser.ErrorProcessStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ErrorProcessSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitErrorProcessSt(ReflexParser.ErrorProcessStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RestartSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterRestartSt(ReflexParser.RestartStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RestartSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitRestartSt(ReflexParser.RestartStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ResetSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterResetSt(ReflexParser.ResetStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ResetSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitResetSt(ReflexParser.ResetStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SetStateSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSetStateSt(ReflexParser.SetStateStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SetStateSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSetStateSt(ReflexParser.SetStateStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfElseSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIfElseSt(ReflexParser.IfElseStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfElseSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIfElseSt(ReflexParser.IfElseStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SwitchSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchSt(ReflexParser.SwitchStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SwitchSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchSt(ReflexParser.SwitchStContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExprSt(ReflexParser.ExprStContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprSt}
	 * labeled alternative in {@link ReflexParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExprSt(ReflexParser.ExprStContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#statementSeq}.
	 * @param ctx the parse tree
	 */
	void enterStatementSeq(ReflexParser.StatementSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#statementSeq}.
	 * @param ctx the parse tree
	 */
	void exitStatementSeq(ReflexParser.StatementSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(ReflexParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(ReflexParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#ifElseStat}.
	 * @param ctx the parse tree
	 */
	void enterIfElseStat(ReflexParser.IfElseStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#ifElseStat}.
	 * @param ctx the parse tree
	 */
	void exitIfElseStat(ReflexParser.IfElseStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#switchStat}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStat(ReflexParser.SwitchStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#switchStat}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStat(ReflexParser.SwitchStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#caseStat}.
	 * @param ctx the parse tree
	 */
	void enterCaseStat(ReflexParser.CaseStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#caseStat}.
	 * @param ctx the parse tree
	 */
	void exitCaseStat(ReflexParser.CaseStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#defaultStat}.
	 * @param ctx the parse tree
	 */
	void enterDefaultStat(ReflexParser.DefaultStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#defaultStat}.
	 * @param ctx the parse tree
	 */
	void exitDefaultStat(ReflexParser.DefaultStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#switchOptionStatSeq}.
	 * @param ctx the parse tree
	 */
	void enterSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#switchOptionStatSeq}.
	 * @param ctx the parse tree
	 */
	void exitSwitchOptionStatSeq(ReflexParser.SwitchOptionStatSeqContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#startProcStat}.
	 * @param ctx the parse tree
	 */
	void enterStartProcStat(ReflexParser.StartProcStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#startProcStat}.
	 * @param ctx the parse tree
	 */
	void exitStartProcStat(ReflexParser.StartProcStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#stopProcStat}.
	 * @param ctx the parse tree
	 */
	void enterStopProcStat(ReflexParser.StopProcStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#stopProcStat}.
	 * @param ctx the parse tree
	 */
	void exitStopProcStat(ReflexParser.StopProcStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#errorProcStat}.
	 * @param ctx the parse tree
	 */
	void enterErrorProcStat(ReflexParser.ErrorProcStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#errorProcStat}.
	 * @param ctx the parse tree
	 */
	void exitErrorProcStat(ReflexParser.ErrorProcStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#restartStat}.
	 * @param ctx the parse tree
	 */
	void enterRestartStat(ReflexParser.RestartStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#restartStat}.
	 * @param ctx the parse tree
	 */
	void exitRestartStat(ReflexParser.RestartStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#resetStat}.
	 * @param ctx the parse tree
	 */
	void enterResetStat(ReflexParser.ResetStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#resetStat}.
	 * @param ctx the parse tree
	 */
	void exitResetStat(ReflexParser.ResetStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#setStateStat}.
	 * @param ctx the parse tree
	 */
	void enterSetStateStat(ReflexParser.SetStateStatContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#setStateStat}.
	 * @param ctx the parse tree
	 */
	void exitSetStateStat(ReflexParser.SetStateStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(ReflexParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(ReflexParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#checkStateExpression}.
	 * @param ctx the parse tree
	 */
	void enterCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#checkStateExpression}.
	 * @param ctx the parse tree
	 */
	void exitCheckStateExpression(ReflexParser.CheckStateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#stateQual}.
	 * @param ctx the parse tree
	 */
	void enterStateQual(ReflexParser.StateQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#stateQual}.
	 * @param ctx the parse tree
	 */
	void exitStateQual(ReflexParser.StateQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#infixOp}.
	 * @param ctx the parse tree
	 */
	void enterInfixOp(ReflexParser.InfixOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#infixOp}.
	 * @param ctx the parse tree
	 */
	void exitInfixOp(ReflexParser.InfixOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#postfixOp}.
	 * @param ctx the parse tree
	 */
	void enterPostfixOp(ReflexParser.PostfixOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#postfixOp}.
	 * @param ctx the parse tree
	 */
	void exitPostfixOp(ReflexParser.PostfixOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Id}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterId(ReflexParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Id}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitId(ReflexParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Integer}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterInteger(ReflexParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Integer}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitInteger(ReflexParser.IntegerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Float}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFloat(ReflexParser.FloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Float}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFloat(ReflexParser.FloatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterBool(ReflexParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitBool(ReflexParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Time}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterTime(ReflexParser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Time}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitTime(ReflexParser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ClosedExpression}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterClosedExpression(ReflexParser.ClosedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ClosedExpression}
	 * labeled alternative in {@link ReflexParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitClosedExpression(ReflexParser.ClosedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PrimaryExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(ReflexParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PrimaryExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(ReflexParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncCallExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterFuncCallExpr(ReflexParser.FuncCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncCallExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitFuncCallExpr(ReflexParser.FuncCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PostfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PostfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixOpExpr(ReflexParser.PostfixOpExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterInfixOpExpr(ReflexParser.InfixOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InfixOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitInfixOpExpr(ReflexParser.InfixOpExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryOpExpr}
	 * labeled alternative in {@link ReflexParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOpExpr(ReflexParser.UnaryOpExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCast(ReflexParser.CastContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCast(ReflexParser.CastContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Add}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAdd(ReflexParser.AddContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Add}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAdd(ReflexParser.AddContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Shift}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterShift(ReflexParser.ShiftContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Shift}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitShift(ReflexParser.ShiftContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BitOr}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitOr(ReflexParser.BitOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BitOr}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitOr(ReflexParser.BitOrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOr(ReflexParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOr(ReflexParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Mul}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMul(ReflexParser.MulContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Mul}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMul(ReflexParser.MulContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CheckState}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCheckState(ReflexParser.CheckStateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CheckState}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCheckState(ReflexParser.CheckStateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnary(ReflexParser.UnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Unary}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnary(ReflexParser.UnaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BitXor}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitXor(ReflexParser.BitXorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BitXor}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitXor(ReflexParser.BitXorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Equal}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqual(ReflexParser.EqualContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Equal}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqual(ReflexParser.EqualContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd(ReflexParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd(ReflexParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BitAnd}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBitAnd(ReflexParser.BitAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BitAnd}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBitAnd(ReflexParser.BitAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssign(ReflexParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Assign}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssign(ReflexParser.AssignContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Compare}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCompare(ReflexParser.CompareContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Compare}
	 * labeled alternative in {@link ReflexParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCompare(ReflexParser.CompareContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#unaryOp}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOp(ReflexParser.UnaryOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#unaryOp}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOp(ReflexParser.UnaryOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#addOp}.
	 * @param ctx the parse tree
	 */
	void enterAddOp(ReflexParser.AddOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#addOp}.
	 * @param ctx the parse tree
	 */
	void exitAddOp(ReflexParser.AddOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#assignOp}.
	 * @param ctx the parse tree
	 */
	void enterAssignOp(ReflexParser.AssignOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#assignOp}.
	 * @param ctx the parse tree
	 */
	void exitAssignOp(ReflexParser.AssignOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link ReflexParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(ReflexParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ReflexParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(ReflexParser.TypeContext ctx);
}