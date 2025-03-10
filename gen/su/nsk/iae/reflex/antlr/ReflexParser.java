// Generated from C:/Users/iarte/IdeaProjects/ReflexVCG/src/main/java/su/nsk/iae/reflex/antlr/Reflex.g4 by ANTLR 4.13.1
package su.nsk.iae.reflex.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ReflexParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		T__52=53, T__53=54, T__54=55, T__55=56, T__56=57, T__57=58, T__58=59, 
		T__59=60, T__60=61, T__61=62, T__62=63, T__63=64, PORT_TYPE=65, BREAK=66, 
		INFIX_POSTFIX_OP=67, MUL_OP=68, SHIFT_OP=69, EQ_OP=70, BIT_AND_OP=71, 
		BIT_XOR_OP=72, BIT_OR_OP=73, AND_OP=74, OR_OP=75, COMP_OP=76, INTEGER=77, 
		UNSIGNED_INTEGER=78, FLOAT=79, DEC_FLOAT=80, HEX_FLOAT=81, DEC_SEQ=82, 
		HEX_SEQ=83, BIN_EXPONENT=84, EXPONENT=85, DECIMAL=86, OCTAL=87, HEX=88, 
		HEX_PREFIX=89, LONG=90, FLOAT_SUFFIX=91, UNSIGNED=92, TIME=93, DAY=94, 
		HOUR=95, MINUTE=96, SECOND=97, MILISECOND=98, BOOL_VAL=99, STRING=100, 
		WS=101, ID=102;
	public static final int
		RULE_program = 0, RULE_clockDefinition = 1, RULE_process = 2, RULE_state = 3, 
		RULE_annotation = 4, RULE_annotationKey = 5, RULE_importedVariableList = 6, 
		RULE_processVariable = 7, RULE_globalVariable = 8, RULE_physicalVariable = 9, 
		RULE_portMapping = 10, RULE_programVariable = 11, RULE_timeoutFunction = 12, 
		RULE_timeAmountOrRef = 13, RULE_functionDecl = 14, RULE_port = 15, RULE_const = 16, 
		RULE_enum = 17, RULE_enumMember = 18, RULE_statement = 19, RULE_statementSeq = 20, 
		RULE_compoundStatement = 21, RULE_ifElseStat = 22, RULE_switchStat = 23, 
		RULE_caseStat = 24, RULE_defaultStat = 25, RULE_switchOptionStatSeq = 26, 
		RULE_startProcStat = 27, RULE_stopProcStat = 28, RULE_errorProcStat = 29, 
		RULE_restartStat = 30, RULE_resetStat = 31, RULE_setStateStat = 32, RULE_functionCall = 33, 
		RULE_checkStateExpression = 34, RULE_stateQual = 35, RULE_infixOp = 36, 
		RULE_postfixOp = 37, RULE_primaryExpression = 38, RULE_unaryExpression = 39, 
		RULE_expression = 40, RULE_unaryOp = 41, RULE_addOp = 42, RULE_assignOp = 43, 
		RULE_type = 44;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "clockDefinition", "process", "state", "annotation", "annotationKey", 
			"importedVariableList", "processVariable", "globalVariable", "physicalVariable", 
			"portMapping", "programVariable", "timeoutFunction", "timeAmountOrRef", 
			"functionDecl", "port", "const", "enum", "enumMember", "statement", "statementSeq", 
			"compoundStatement", "ifElseStat", "switchStat", "caseStat", "defaultStat", 
			"switchOptionStatSeq", "startProcStat", "stopProcStat", "errorProcStat", 
			"restartStat", "resetStat", "setStateStat", "functionCall", "checkStateExpression", 
			"stateQual", "infixOp", "postfixOp", "primaryExpression", "unaryExpression", 
			"expression", "unaryOp", "addOp", "assignOp", "type"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "'program'", "'{'", "'}'", "'clock'", "';'", "'process'", 
			"'state'", "'looped'", "':'", "'.'", "'shared'", "','", "'from'", "'='", 
			"'timeout'", "'('", "')'", "'const'", "'enum'", "'if'", "'else'", "'switch'", 
			"'case'", "'default'", "'start'", "'stop'", "'error'", "'restart'", "'reset'", 
			"'timer'", "'set'", "'next'", "'in'", "'active'", "'inactive'", "'+'", 
			"'-'", "'~'", "'!'", "'*='", "'/='", "'%='", "'+='", "'-='", "'<<='", 
			"'>>='", "'&='", "'^='", "'|='", "'void'", "'bool'", "'time'", "'float'", 
			"'double'", "'int8'", "'uint8'", "'int16'", "'uint16'", "'int32'", "'uint32'", 
			"'int64'", "'uint64'", null, null, null, null, null, null, "'&'", "'^'", 
			"'|'", "'&&'", "'||'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "PORT_TYPE", "BREAK", "INFIX_POSTFIX_OP", 
			"MUL_OP", "SHIFT_OP", "EQ_OP", "BIT_AND_OP", "BIT_XOR_OP", "BIT_OR_OP", 
			"AND_OP", "OR_OP", "COMP_OP", "INTEGER", "UNSIGNED_INTEGER", "FLOAT", 
			"DEC_FLOAT", "HEX_FLOAT", "DEC_SEQ", "HEX_SEQ", "BIN_EXPONENT", "EXPONENT", 
			"DECIMAL", "OCTAL", "HEX", "HEX_PREFIX", "LONG", "FLOAT_SUFFIX", "UNSIGNED", 
			"TIME", "DAY", "HOUR", "MINUTE", "SECOND", "MILISECOND", "BOOL_VAL", 
			"STRING", "WS", "ID"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Reflex.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ReflexParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public AnnotationContext annotation;
		public List<AnnotationContext> annotations = new ArrayList<AnnotationContext>();
		public Token name;
		public ClockDefinitionContext clock;
		public ConstContext const_;
		public List<ConstContext> consts = new ArrayList<ConstContext>();
		public EnumContext enum_;
		public List<EnumContext> enums = new ArrayList<EnumContext>();
		public FunctionDeclContext functionDecl;
		public List<FunctionDeclContext> functions = new ArrayList<FunctionDeclContext>();
		public GlobalVariableContext globalVariable;
		public List<GlobalVariableContext> globalVars = new ArrayList<GlobalVariableContext>();
		public PortContext port;
		public List<PortContext> ports = new ArrayList<PortContext>();
		public ProcessContext process;
		public List<ProcessContext> processes = new ArrayList<ProcessContext>();
		public TerminalNode EOF() { return getToken(ReflexParser.EOF, 0); }
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public ClockDefinitionContext clockDefinition() {
			return getRuleContext(ClockDefinitionContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<ConstContext> const_() {
			return getRuleContexts(ConstContext.class);
		}
		public ConstContext const_(int i) {
			return getRuleContext(ConstContext.class,i);
		}
		public List<EnumContext> enum_() {
			return getRuleContexts(EnumContext.class);
		}
		public EnumContext enum_(int i) {
			return getRuleContext(EnumContext.class,i);
		}
		public List<FunctionDeclContext> functionDecl() {
			return getRuleContexts(FunctionDeclContext.class);
		}
		public FunctionDeclContext functionDecl(int i) {
			return getRuleContext(FunctionDeclContext.class,i);
		}
		public List<GlobalVariableContext> globalVariable() {
			return getRuleContexts(GlobalVariableContext.class);
		}
		public GlobalVariableContext globalVariable(int i) {
			return getRuleContext(GlobalVariableContext.class,i);
		}
		public List<PortContext> port() {
			return getRuleContexts(PortContext.class);
		}
		public PortContext port(int i) {
			return getRuleContext(PortContext.class,i);
		}
		public List<ProcessContext> process() {
			return getRuleContexts(ProcessContext.class);
		}
		public ProcessContext process(int i) {
			return getRuleContext(ProcessContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(90);
				match(T__0);
				setState(91);
				((ProgramContext)_localctx).annotation = annotation();
				((ProgramContext)_localctx).annotations.add(((ProgramContext)_localctx).annotation);
				setState(92);
				match(T__1);
				}
				}
				setState(98);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(99);
			match(T__2);
			setState(100);
			((ProgramContext)_localctx).name = match(ID);
			setState(101);
			match(T__3);
			setState(102);
			((ProgramContext)_localctx).clock = clockDefinition();
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -4503599624224510L) != 0) || _la==T__63 || _la==PORT_TYPE) {
				{
				setState(109);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(103);
					((ProgramContext)_localctx).const_ = const_();
					((ProgramContext)_localctx).consts.add(((ProgramContext)_localctx).const_);
					}
					break;
				case 2:
					{
					setState(104);
					((ProgramContext)_localctx).enum_ = enum_();
					((ProgramContext)_localctx).enums.add(((ProgramContext)_localctx).enum_);
					}
					break;
				case 3:
					{
					setState(105);
					((ProgramContext)_localctx).functionDecl = functionDecl();
					((ProgramContext)_localctx).functions.add(((ProgramContext)_localctx).functionDecl);
					}
					break;
				case 4:
					{
					setState(106);
					((ProgramContext)_localctx).globalVariable = globalVariable();
					((ProgramContext)_localctx).globalVars.add(((ProgramContext)_localctx).globalVariable);
					}
					break;
				case 5:
					{
					setState(107);
					((ProgramContext)_localctx).port = port();
					((ProgramContext)_localctx).ports.add(((ProgramContext)_localctx).port);
					}
					break;
				case 6:
					{
					setState(108);
					((ProgramContext)_localctx).process = process();
					((ProgramContext)_localctx).processes.add(((ProgramContext)_localctx).process);
					}
					break;
				}
				}
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(114);
			match(T__4);
			setState(115);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClockDefinitionContext extends ParserRuleContext {
		public Token intValue;
		public Token timeValue;
		public TerminalNode UNSIGNED_INTEGER() { return getToken(ReflexParser.UNSIGNED_INTEGER, 0); }
		public TerminalNode TIME() { return getToken(ReflexParser.TIME, 0); }
		public ClockDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clockDefinition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitClockDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClockDefinitionContext clockDefinition() throws RecognitionException {
		ClockDefinitionContext _localctx = new ClockDefinitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_clockDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(T__5);
			setState(120);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNSIGNED_INTEGER:
				{
				setState(118);
				((ClockDefinitionContext)_localctx).intValue = match(UNSIGNED_INTEGER);
				}
				break;
			case TIME:
				{
				setState(119);
				((ClockDefinitionContext)_localctx).timeValue = match(TIME);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(122);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProcessContext extends ParserRuleContext {
		public AnnotationContext annotation;
		public List<AnnotationContext> annotations = new ArrayList<AnnotationContext>();
		public Token name;
		public ImportedVariableListContext importedVariableList;
		public List<ImportedVariableListContext> imports = new ArrayList<ImportedVariableListContext>();
		public ProcessVariableContext processVariable;
		public List<ProcessVariableContext> variables = new ArrayList<ProcessVariableContext>();
		public StateContext state;
		public List<StateContext> states = new ArrayList<StateContext>();
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<StateContext> state() {
			return getRuleContexts(StateContext.class);
		}
		public StateContext state(int i) {
			return getRuleContext(StateContext.class,i);
		}
		public List<ImportedVariableListContext> importedVariableList() {
			return getRuleContexts(ImportedVariableListContext.class);
		}
		public ImportedVariableListContext importedVariableList(int i) {
			return getRuleContext(ImportedVariableListContext.class,i);
		}
		public List<ProcessVariableContext> processVariable() {
			return getRuleContexts(ProcessVariableContext.class);
		}
		public ProcessVariableContext processVariable(int i) {
			return getRuleContext(ProcessVariableContext.class,i);
		}
		public ProcessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_process; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitProcess(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProcessContext process() throws RecognitionException {
		ProcessContext _localctx = new ProcessContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_process);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(124);
				match(T__0);
				setState(125);
				((ProcessContext)_localctx).annotation = annotation();
				((ProcessContext)_localctx).annotations.add(((ProcessContext)_localctx).annotation);
				setState(126);
				match(T__1);
				}
				}
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(133);
			match(T__7);
			setState(134);
			((ProcessContext)_localctx).name = match(ID);
			setState(135);
			match(T__3);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 13)) & ~0x3f) == 0 && ((1L << (_la - 13)) & 4503049871556609L) != 0)) {
				{
				{
				setState(138);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__12:
					{
					setState(136);
					((ProcessContext)_localctx).importedVariableList = importedVariableList();
					((ProcessContext)_localctx).imports.add(((ProcessContext)_localctx).importedVariableList);
					}
					break;
				case T__51:
				case T__52:
				case T__53:
				case T__54:
				case T__55:
				case T__56:
				case T__57:
				case T__58:
				case T__59:
				case T__60:
				case T__61:
				case T__62:
				case T__63:
					{
					setState(137);
					((ProcessContext)_localctx).processVariable = processVariable();
					((ProcessContext)_localctx).variables.add(((ProcessContext)_localctx).processVariable);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(140);
				match(T__6);
				}
				}
				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==T__8) {
				{
				{
				setState(147);
				((ProcessContext)_localctx).state = state();
				((ProcessContext)_localctx).states.add(((ProcessContext)_localctx).state);
				}
				}
				setState(152);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(153);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StateContext extends ParserRuleContext {
		public AnnotationContext annotation;
		public List<AnnotationContext> annotations = new ArrayList<AnnotationContext>();
		public Token name;
		public Token looped;
		public StatementSeqContext stateFunction;
		public TimeoutFunctionContext func;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public StatementSeqContext statementSeq() {
			return getRuleContext(StatementSeqContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TimeoutFunctionContext timeoutFunction() {
			return getRuleContext(TimeoutFunctionContext.class,0);
		}
		public StateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_state; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitState(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StateContext state() throws RecognitionException {
		StateContext _localctx = new StateContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_state);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(155);
				match(T__0);
				setState(156);
				((StateContext)_localctx).annotation = annotation();
				((StateContext)_localctx).annotations.add(((StateContext)_localctx).annotation);
				setState(157);
				match(T__1);
				}
				}
				setState(163);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(164);
			match(T__8);
			setState(165);
			((StateContext)_localctx).name = match(ID);
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(166);
				((StateContext)_localctx).looped = match(T__9);
				}
			}

			setState(169);
			match(T__3);
			setState(170);
			((StateContext)_localctx).stateFunction = statementSeq();
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__16) {
				{
				setState(171);
				((StateContext)_localctx).func = timeoutFunction();
				}
			}

			setState(174);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationContext extends ParserRuleContext {
		public AnnotationKeyContext key;
		public Token value;
		public AnnotationKeyContext annotationKey() {
			return getRuleContext(AnnotationKeyContext.class,0);
		}
		public TerminalNode STRING() { return getToken(ReflexParser.STRING, 0); }
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_annotation);
		try {
			setState(181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(176);
				((AnnotationContext)_localctx).key = annotationKey();
				setState(177);
				match(T__10);
				setState(178);
				((AnnotationContext)_localctx).value = match(STRING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(180);
				((AnnotationContext)_localctx).key = annotationKey();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationKeyContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(ReflexParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ReflexParser.ID, i);
		}
		public AnnotationKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationKey; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAnnotationKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationKeyContext annotationKey() throws RecognitionException {
		AnnotationKeyContext _localctx = new AnnotationKeyContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_annotationKey);
		try {
			setState(187);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				match(ID);
				setState(184);
				match(T__11);
				setState(185);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(186);
				match(ID);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportedVariableListContext extends ParserRuleContext {
		public Token ID;
		public List<Token> variables = new ArrayList<Token>();
		public Token processID;
		public List<TerminalNode> ID() { return getTokens(ReflexParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ReflexParser.ID, i);
		}
		public ImportedVariableListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importedVariableList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitImportedVariableList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportedVariableListContext importedVariableList() throws RecognitionException {
		ImportedVariableListContext _localctx = new ImportedVariableListContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_importedVariableList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(T__12);
			{
			setState(190);
			((ImportedVariableListContext)_localctx).ID = match(ID);
			((ImportedVariableListContext)_localctx).variables.add(((ImportedVariableListContext)_localctx).ID);
			setState(195);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(191);
				match(T__13);
				setState(192);
				((ImportedVariableListContext)_localctx).ID = match(ID);
				((ImportedVariableListContext)_localctx).variables.add(((ImportedVariableListContext)_localctx).ID);
				}
				}
				setState(197);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			setState(198);
			match(T__14);
			setState(199);
			match(T__7);
			setState(200);
			((ImportedVariableListContext)_localctx).processID = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProcessVariableContext extends ParserRuleContext {
		public Token shared;
		public PhysicalVariableContext physicalVariable() {
			return getRuleContext(PhysicalVariableContext.class,0);
		}
		public ProgramVariableContext programVariable() {
			return getRuleContext(ProgramVariableContext.class,0);
		}
		public ProcessVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_processVariable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitProcessVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProcessVariableContext processVariable() throws RecognitionException {
		ProcessVariableContext _localctx = new ProcessVariableContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_processVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(202);
				physicalVariable();
				}
				break;
			case 2:
				{
				setState(203);
				programVariable();
				}
				break;
			}
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__12) {
				{
				setState(206);
				((ProcessVariableContext)_localctx).shared = match(T__12);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GlobalVariableContext extends ParserRuleContext {
		public PhysicalVariableContext physicalVariable() {
			return getRuleContext(PhysicalVariableContext.class,0);
		}
		public ProgramVariableContext programVariable() {
			return getRuleContext(ProgramVariableContext.class,0);
		}
		public GlobalVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globalVariable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitGlobalVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalVariableContext globalVariable() throws RecognitionException {
		GlobalVariableContext _localctx = new GlobalVariableContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_globalVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(209);
				physicalVariable();
				}
				break;
			case 2:
				{
				setState(210);
				programVariable();
				}
				break;
			}
			setState(213);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PhysicalVariableContext extends ParserRuleContext {
		public TypeContext varType;
		public Token name;
		public PortMappingContext mapping;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public PortMappingContext portMapping() {
			return getRuleContext(PortMappingContext.class,0);
		}
		public PhysicalVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_physicalVariable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPhysicalVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PhysicalVariableContext physicalVariable() throws RecognitionException {
		PhysicalVariableContext _localctx = new PhysicalVariableContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_physicalVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			((PhysicalVariableContext)_localctx).varType = type();
			setState(216);
			((PhysicalVariableContext)_localctx).name = match(ID);
			setState(217);
			match(T__15);
			setState(218);
			((PhysicalVariableContext)_localctx).mapping = portMapping();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PortMappingContext extends ParserRuleContext {
		public Token portId;
		public Token bit;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public TerminalNode UNSIGNED_INTEGER() { return getToken(ReflexParser.UNSIGNED_INTEGER, 0); }
		public PortMappingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_portMapping; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPortMapping(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PortMappingContext portMapping() throws RecognitionException {
		PortMappingContext _localctx = new PortMappingContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_portMapping);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
			((PortMappingContext)_localctx).portId = match(ID);
			setState(221);
			match(T__0);
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNSIGNED_INTEGER) {
				{
				setState(222);
				((PortMappingContext)_localctx).bit = match(UNSIGNED_INTEGER);
				}
			}

			setState(225);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramVariableContext extends ParserRuleContext {
		public TypeContext varType;
		public Token name;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ProgramVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_programVariable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitProgramVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramVariableContext programVariable() throws RecognitionException {
		ProgramVariableContext _localctx = new ProgramVariableContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_programVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			((ProgramVariableContext)_localctx).varType = type();
			setState(228);
			((ProgramVariableContext)_localctx).name = match(ID);
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(229);
				match(T__15);
				setState(230);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TimeoutFunctionContext extends ParserRuleContext {
		public StatementContext body;
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TimeAmountOrRefContext timeAmountOrRef() {
			return getRuleContext(TimeAmountOrRefContext.class,0);
		}
		public TimeoutFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeoutFunction; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitTimeoutFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeoutFunctionContext timeoutFunction() throws RecognitionException {
		TimeoutFunctionContext _localctx = new TimeoutFunctionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_timeoutFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			match(T__16);
			setState(239);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNSIGNED_INTEGER:
			case TIME:
			case ID:
				{
				setState(234);
				timeAmountOrRef();
				}
				break;
			case T__17:
				{
				setState(235);
				match(T__17);
				setState(236);
				timeAmountOrRef();
				setState(237);
				match(T__18);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(241);
			((TimeoutFunctionContext)_localctx).body = statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TimeAmountOrRefContext extends ParserRuleContext {
		public Token time;
		public Token intTime;
		public Token ref;
		public TerminalNode TIME() { return getToken(ReflexParser.TIME, 0); }
		public TerminalNode UNSIGNED_INTEGER() { return getToken(ReflexParser.UNSIGNED_INTEGER, 0); }
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public TimeAmountOrRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeAmountOrRef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitTimeAmountOrRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TimeAmountOrRefContext timeAmountOrRef() throws RecognitionException {
		TimeAmountOrRefContext _localctx = new TimeAmountOrRefContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_timeAmountOrRef);
		try {
			setState(246);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TIME:
				enterOuterAlt(_localctx, 1);
				{
				setState(243);
				((TimeAmountOrRefContext)_localctx).time = match(TIME);
				}
				break;
			case UNSIGNED_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(244);
				((TimeAmountOrRefContext)_localctx).intTime = match(UNSIGNED_INTEGER);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(245);
				((TimeAmountOrRefContext)_localctx).ref = match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDeclContext extends ParserRuleContext {
		public TypeContext returnType;
		public TypeContext type;
		public List<TypeContext> argTypes = new ArrayList<TypeContext>();
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public FunctionDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDecl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitFunctionDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDeclContext functionDecl() throws RecognitionException {
		FunctionDeclContext _localctx = new FunctionDeclContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_functionDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			((FunctionDeclContext)_localctx).returnType = type();
			setState(249);
			match(T__17);
			setState(250);
			((FunctionDeclContext)_localctx).type = type();
			((FunctionDeclContext)_localctx).argTypes.add(((FunctionDeclContext)_localctx).type);
			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(251);
				match(T__13);
				setState(252);
				((FunctionDeclContext)_localctx).type = type();
				((FunctionDeclContext)_localctx).argTypes.add(((FunctionDeclContext)_localctx).type);
				}
				}
				setState(257);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(258);
			match(T__18);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PortContext extends ParserRuleContext {
		public Token varType;
		public Token name;
		public Token addr1;
		public Token addr2;
		public Token size;
		public TerminalNode PORT_TYPE() { return getToken(ReflexParser.PORT_TYPE, 0); }
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public List<TerminalNode> UNSIGNED_INTEGER() { return getTokens(ReflexParser.UNSIGNED_INTEGER); }
		public TerminalNode UNSIGNED_INTEGER(int i) {
			return getToken(ReflexParser.UNSIGNED_INTEGER, i);
		}
		public PortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_port; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPort(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PortContext port() throws RecognitionException {
		PortContext _localctx = new PortContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_port);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			((PortContext)_localctx).varType = match(PORT_TYPE);
			setState(261);
			((PortContext)_localctx).name = match(ID);
			setState(262);
			((PortContext)_localctx).addr1 = match(UNSIGNED_INTEGER);
			setState(263);
			((PortContext)_localctx).addr2 = match(UNSIGNED_INTEGER);
			setState(264);
			((PortContext)_localctx).size = match(UNSIGNED_INTEGER);
			setState(265);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstContext extends ParserRuleContext {
		public TypeContext varType;
		public Token name;
		public ExpressionContext value;
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ConstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitConst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstContext const_() throws RecognitionException {
		ConstContext _localctx = new ConstContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_const);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			match(T__19);
			setState(268);
			((ConstContext)_localctx).varType = type();
			setState(269);
			((ConstContext)_localctx).name = match(ID);
			setState(270);
			match(T__15);
			setState(271);
			((ConstContext)_localctx).value = expression(0);
			setState(272);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumContext extends ParserRuleContext {
		public Token identifier;
		public EnumMemberContext enumMember;
		public List<EnumMemberContext> enumMembers = new ArrayList<EnumMemberContext>();
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public List<EnumMemberContext> enumMember() {
			return getRuleContexts(EnumMemberContext.class);
		}
		public EnumMemberContext enumMember(int i) {
			return getRuleContext(EnumMemberContext.class,i);
		}
		public EnumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enum; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitEnum(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumContext enum_() throws RecognitionException {
		EnumContext _localctx = new EnumContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_enum);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(T__20);
			setState(275);
			((EnumContext)_localctx).identifier = match(ID);
			setState(276);
			match(T__3);
			setState(277);
			((EnumContext)_localctx).enumMember = enumMember();
			((EnumContext)_localctx).enumMembers.add(((EnumContext)_localctx).enumMember);
			{
			setState(278);
			match(T__13);
			setState(279);
			((EnumContext)_localctx).enumMember = enumMember();
			((EnumContext)_localctx).enumMembers.add(((EnumContext)_localctx).enumMember);
			}
			setState(281);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumMemberContext extends ParserRuleContext {
		public Token name;
		public ExpressionContext value;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public EnumMemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumMember; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitEnumMember(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumMemberContext enumMember() throws RecognitionException {
		EnumMemberContext _localctx = new EnumMemberContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_enumMember);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(283);
			((EnumMemberContext)_localctx).name = match(ID);
			setState(286);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(284);
				match(T__15);
				setState(285);
				((EnumMemberContext)_localctx).value = expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	 
		public StatementContext() { }
		public void copyFrom(StatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompoundStContext extends StatementContext {
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public CompoundStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCompoundSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IfElseStContext extends StatementContext {
		public IfElseStatContext ifElseStat() {
			return getRuleContext(IfElseStatContext.class,0);
		}
		public IfElseStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitIfElseSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SwitchStContext extends StatementContext {
		public SwitchStatContext switchStat() {
			return getRuleContext(SwitchStatContext.class,0);
		}
		public SwitchStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSwitchSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EmptyStContext extends StatementContext {
		public EmptyStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitEmptySt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ErrorProcessStContext extends StatementContext {
		public ErrorProcStatContext errorProcStat() {
			return getRuleContext(ErrorProcStatContext.class,0);
		}
		public ErrorProcessStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitErrorProcessSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StopProcessStContext extends StatementContext {
		public StopProcStatContext stopProcStat() {
			return getRuleContext(StopProcStatContext.class,0);
		}
		public StopProcessStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStopProcessSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprStContext extends StatementContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExprStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitExprSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StartProcessStContext extends StatementContext {
		public StartProcStatContext startProcStat() {
			return getRuleContext(StartProcStatContext.class,0);
		}
		public StartProcessStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStartProcessSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ResetStContext extends StatementContext {
		public ResetStatContext resetStat() {
			return getRuleContext(ResetStatContext.class,0);
		}
		public ResetStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitResetSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SetStateStContext extends StatementContext {
		public SetStateStatContext setStateStat() {
			return getRuleContext(SetStateStatContext.class,0);
		}
		public SetStateStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSetStateSt(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RestartStContext extends StatementContext {
		public RestartStatContext restartStat() {
			return getRuleContext(RestartStatContext.class,0);
		}
		public RestartStContext(StatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitRestartSt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_statement);
		try {
			setState(301);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
				_localctx = new EmptyStContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(288);
				match(T__6);
				}
				break;
			case T__3:
				_localctx = new CompoundStContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(289);
				compoundStatement();
				}
				break;
			case T__26:
				_localctx = new StartProcessStContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(290);
				startProcStat();
				}
				break;
			case T__27:
				_localctx = new StopProcessStContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(291);
				stopProcStat();
				}
				break;
			case T__28:
				_localctx = new ErrorProcessStContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(292);
				errorProcStat();
				}
				break;
			case T__29:
				_localctx = new RestartStContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(293);
				restartStat();
				}
				break;
			case T__30:
				_localctx = new ResetStContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(294);
				resetStat();
				}
				break;
			case T__32:
				_localctx = new SetStateStContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(295);
				setStateStat();
				}
				break;
			case T__21:
				_localctx = new IfElseStContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(296);
				ifElseStat();
				}
				break;
			case T__23:
				_localctx = new SwitchStContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(297);
				switchStat();
				}
				break;
			case T__7:
			case T__17:
			case T__37:
			case T__38:
			case T__39:
			case T__40:
			case INFIX_POSTFIX_OP:
			case INTEGER:
			case UNSIGNED_INTEGER:
			case FLOAT:
			case TIME:
			case BOOL_VAL:
			case ID:
				_localctx = new ExprStContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(298);
				expression(0);
				setState(299);
				match(T__6);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementSeqContext extends ParserRuleContext {
		public StatementContext statement;
		public List<StatementContext> statements = new ArrayList<StatementContext>();
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatementSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statementSeq; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStatementSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementSeqContext statementSeq() throws RecognitionException {
		StatementSeqContext _localctx = new StatementSeqContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_statementSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4135940522384L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 38721821697L) != 0)) {
				{
				{
				setState(303);
				((StatementSeqContext)_localctx).statement = statement();
				((StatementSeqContext)_localctx).statements.add(((StatementSeqContext)_localctx).statement);
				}
				}
				setState(308);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompoundStatementContext extends ParserRuleContext {
		public StatementContext statement;
		public List<StatementContext> statements = new ArrayList<StatementContext>();
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public CompoundStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCompoundStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompoundStatementContext compoundStatement() throws RecognitionException {
		CompoundStatementContext _localctx = new CompoundStatementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_compoundStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(309);
			match(T__3);
			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4135940522384L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 38721821697L) != 0)) {
				{
				{
				setState(310);
				((CompoundStatementContext)_localctx).statement = statement();
				((CompoundStatementContext)_localctx).statements.add(((CompoundStatementContext)_localctx).statement);
				}
				}
				setState(315);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(316);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfElseStatContext extends ParserRuleContext {
		public ExpressionContext cond;
		public StatementContext then;
		public StatementContext else_;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public IfElseStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifElseStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitIfElseStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfElseStatContext ifElseStat() throws RecognitionException {
		IfElseStatContext _localctx = new IfElseStatContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_ifElseStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			match(T__21);
			setState(319);
			match(T__17);
			setState(320);
			((IfElseStatContext)_localctx).cond = expression(0);
			setState(321);
			match(T__18);
			setState(322);
			((IfElseStatContext)_localctx).then = statement();
			setState(325);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(323);
				match(T__22);
				setState(324);
				((IfElseStatContext)_localctx).else_ = statement();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchStatContext extends ParserRuleContext {
		public ExpressionContext expr;
		public CaseStatContext caseStat;
		public List<CaseStatContext> options = new ArrayList<CaseStatContext>();
		public DefaultStatContext defaultOption;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<CaseStatContext> caseStat() {
			return getRuleContexts(CaseStatContext.class);
		}
		public CaseStatContext caseStat(int i) {
			return getRuleContext(CaseStatContext.class,i);
		}
		public DefaultStatContext defaultStat() {
			return getRuleContext(DefaultStatContext.class,0);
		}
		public SwitchStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSwitchStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchStatContext switchStat() throws RecognitionException {
		SwitchStatContext _localctx = new SwitchStatContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_switchStat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			match(T__23);
			setState(328);
			match(T__17);
			setState(329);
			((SwitchStatContext)_localctx).expr = expression(0);
			setState(330);
			match(T__18);
			setState(331);
			match(T__3);
			setState(335);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__24) {
				{
				{
				setState(332);
				((SwitchStatContext)_localctx).caseStat = caseStat();
				((SwitchStatContext)_localctx).options.add(((SwitchStatContext)_localctx).caseStat);
				}
				}
				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(339);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__25) {
				{
				setState(338);
				((SwitchStatContext)_localctx).defaultOption = defaultStat();
				}
			}

			setState(341);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CaseStatContext extends ParserRuleContext {
		public ExpressionContext option;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public SwitchOptionStatSeqContext switchOptionStatSeq() {
			return getRuleContext(SwitchOptionStatSeqContext.class,0);
		}
		public CaseStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCaseStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseStatContext caseStat() throws RecognitionException {
		CaseStatContext _localctx = new CaseStatContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_caseStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
			match(T__24);
			setState(344);
			((CaseStatContext)_localctx).option = expression(0);
			setState(345);
			match(T__10);
			setState(351);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(346);
				match(T__3);
				setState(347);
				switchOptionStatSeq();
				setState(348);
				match(T__4);
				}
				break;
			case 2:
				{
				setState(350);
				switchOptionStatSeq();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultStatContext extends ParserRuleContext {
		public SwitchOptionStatSeqContext switchOptionStatSeq() {
			return getRuleContext(SwitchOptionStatSeqContext.class,0);
		}
		public DefaultStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitDefaultStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultStatContext defaultStat() throws RecognitionException {
		DefaultStatContext _localctx = new DefaultStatContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_defaultStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			match(T__25);
			setState(354);
			match(T__10);
			setState(355);
			match(T__3);
			setState(356);
			switchOptionStatSeq();
			setState(357);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchOptionStatSeqContext extends ParserRuleContext {
		public StatementContext statement;
		public List<StatementContext> statements = new ArrayList<StatementContext>();
		public Token break_;
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode BREAK() { return getToken(ReflexParser.BREAK, 0); }
		public SwitchOptionStatSeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchOptionStatSeq; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSwitchOptionStatSeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchOptionStatSeqContext switchOptionStatSeq() throws RecognitionException {
		SwitchOptionStatSeqContext _localctx = new SwitchOptionStatSeqContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_switchOptionStatSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(362);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4135940522384L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 38721821697L) != 0)) {
				{
				{
				setState(359);
				((SwitchOptionStatSeqContext)_localctx).statement = statement();
				((SwitchOptionStatSeqContext)_localctx).statements.add(((SwitchOptionStatSeqContext)_localctx).statement);
				}
				}
				setState(364);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(366);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BREAK) {
				{
				setState(365);
				((SwitchOptionStatSeqContext)_localctx).break_ = match(BREAK);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StartProcStatContext extends ParserRuleContext {
		public Token processId;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public StartProcStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_startProcStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStartProcStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartProcStatContext startProcStat() throws RecognitionException {
		StartProcStatContext _localctx = new StartProcStatContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_startProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(368);
			match(T__26);
			setState(369);
			((StartProcStatContext)_localctx).processId = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StopProcStatContext extends ParserRuleContext {
		public Token processId;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public StopProcStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stopProcStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStopProcStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StopProcStatContext stopProcStat() throws RecognitionException {
		StopProcStatContext _localctx = new StopProcStatContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_stopProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			match(T__27);
			setState(373);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(372);
				((StopProcStatContext)_localctx).processId = match(ID);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ErrorProcStatContext extends ParserRuleContext {
		public Token processId;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public ErrorProcStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorProcStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitErrorProcStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorProcStatContext errorProcStat() throws RecognitionException {
		ErrorProcStatContext _localctx = new ErrorProcStatContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_errorProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(375);
			match(T__28);
			setState(377);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				setState(376);
				((ErrorProcStatContext)_localctx).processId = match(ID);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RestartStatContext extends ParserRuleContext {
		public RestartStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_restartStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitRestartStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RestartStatContext restartStat() throws RecognitionException {
		RestartStatContext _localctx = new RestartStatContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_restartStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			match(T__29);
			setState(380);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResetStatContext extends ParserRuleContext {
		public ResetStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resetStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitResetStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResetStatContext resetStat() throws RecognitionException {
		ResetStatContext _localctx = new ResetStatContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_resetStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382);
			match(T__30);
			setState(383);
			match(T__31);
			setState(384);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SetStateStatContext extends ParserRuleContext {
		public Token stateId;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public SetStateStatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setStateStat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSetStateStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetStateStatContext setStateStat() throws RecognitionException {
		SetStateStatContext _localctx = new SetStateStatContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_setStateStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			match(T__32);
			setState(391);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__33:
				{
				setState(387);
				match(T__33);
				setState(388);
				match(T__8);
				}
				break;
			case T__8:
				{
				setState(389);
				match(T__8);
				setState(390);
				((SetStateStatContext)_localctx).stateId = match(ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public Token functionID;
		public ExpressionContext expression;
		public List<ExpressionContext> args = new ArrayList<ExpressionContext>();
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			((FunctionCallContext)_localctx).functionID = match(ID);
			setState(394);
			match(T__17);
			{
			setState(395);
			((FunctionCallContext)_localctx).expression = expression(0);
			((FunctionCallContext)_localctx).args.add(((FunctionCallContext)_localctx).expression);
			setState(400);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(396);
				match(T__13);
				setState(397);
				((FunctionCallContext)_localctx).expression = expression(0);
				((FunctionCallContext)_localctx).args.add(((FunctionCallContext)_localctx).expression);
				}
				}
				setState(402);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			setState(403);
			match(T__18);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CheckStateExpressionContext extends ParserRuleContext {
		public Token processId;
		public StateQualContext qual;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public StateQualContext stateQual() {
			return getRuleContext(StateQualContext.class,0);
		}
		public CheckStateExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_checkStateExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCheckStateExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CheckStateExpressionContext checkStateExpression() throws RecognitionException {
		CheckStateExpressionContext _localctx = new CheckStateExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_checkStateExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(T__7);
			setState(406);
			((CheckStateExpressionContext)_localctx).processId = match(ID);
			setState(407);
			match(T__34);
			setState(408);
			match(T__8);
			setState(409);
			((CheckStateExpressionContext)_localctx).qual = stateQual();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StateQualContext extends ParserRuleContext {
		public StateQualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stateQual; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStateQual(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StateQualContext stateQual() throws RecognitionException {
		StateQualContext _localctx = new StateQualContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_stateQual);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 206963736576L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InfixOpContext extends ParserRuleContext {
		public Token op;
		public Token varId;
		public TerminalNode INFIX_POSTFIX_OP() { return getToken(ReflexParser.INFIX_POSTFIX_OP, 0); }
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public InfixOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_infixOp; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitInfixOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InfixOpContext infixOp() throws RecognitionException {
		InfixOpContext _localctx = new InfixOpContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_infixOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(413);
			((InfixOpContext)_localctx).op = match(INFIX_POSTFIX_OP);
			setState(414);
			((InfixOpContext)_localctx).varId = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixOpContext extends ParserRuleContext {
		public Token varId;
		public Token op;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public TerminalNode INFIX_POSTFIX_OP() { return getToken(ReflexParser.INFIX_POSTFIX_OP, 0); }
		public PostfixOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixOp; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPostfixOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixOpContext postfixOp() throws RecognitionException {
		PostfixOpContext _localctx = new PostfixOpContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_postfixOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			((PostfixOpContext)_localctx).varId = match(ID);
			setState(417);
			((PostfixOpContext)_localctx).op = match(INFIX_POSTFIX_OP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ParserRuleContext {
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
	 
		public PrimaryExpressionContext() { }
		public void copyFrom(PrimaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntegerContext extends PrimaryExpressionContext {
		public TerminalNode INTEGER() { return getToken(ReflexParser.INTEGER, 0); }
		public TerminalNode UNSIGNED_INTEGER() { return getToken(ReflexParser.UNSIGNED_INTEGER, 0); }
		public IntegerContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitInteger(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FloatContext extends PrimaryExpressionContext {
		public TerminalNode FLOAT() { return getToken(ReflexParser.FLOAT, 0); }
		public FloatContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitFloat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BoolContext extends PrimaryExpressionContext {
		public TerminalNode BOOL_VAL() { return getToken(ReflexParser.BOOL_VAL, 0); }
		public BoolContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitBool(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ClosedExpressionContext extends PrimaryExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ClosedExpressionContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitClosedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeContext extends PrimaryExpressionContext {
		public TerminalNode TIME() { return getToken(ReflexParser.TIME, 0); }
		public TimeContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitTime(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdContext extends PrimaryExpressionContext {
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public IdContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_primaryExpression);
		int _la;
		try {
			setState(428);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new IdContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(419);
				match(ID);
				}
				break;
			case INTEGER:
			case UNSIGNED_INTEGER:
				_localctx = new IntegerContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(420);
				_la = _input.LA(1);
				if ( !(_la==INTEGER || _la==UNSIGNED_INTEGER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case FLOAT:
				_localctx = new FloatContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(421);
				match(FLOAT);
				}
				break;
			case BOOL_VAL:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(422);
				match(BOOL_VAL);
				}
				break;
			case TIME:
				_localctx = new TimeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(423);
				match(TIME);
				}
				break;
			case T__17:
				_localctx = new ClosedExpressionContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(424);
				match(T__17);
				setState(425);
				expression(0);
				setState(426);
				match(T__18);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryExpressionContext extends ParserRuleContext {
		public UnaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryExpression; }
	 
		public UnaryExpressionContext() { }
		public void copyFrom(UnaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PostfixOpExprContext extends UnaryExpressionContext {
		public PostfixOpContext postfixOp() {
			return getRuleContext(PostfixOpContext.class,0);
		}
		public PostfixOpExprContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPostfixOpExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExprContext extends UnaryExpressionContext {
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public PrimaryExprContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitPrimaryExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOpExprContext extends UnaryExpressionContext {
		public UnaryOpContext op;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public UnaryOpContext unaryOp() {
			return getRuleContext(UnaryOpContext.class,0);
		}
		public UnaryOpExprContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitUnaryOpExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InfixOpExprContext extends UnaryExpressionContext {
		public InfixOpContext infixOp() {
			return getRuleContext(InfixOpContext.class,0);
		}
		public InfixOpExprContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitInfixOpExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncCallExprContext extends UnaryExpressionContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FuncCallExprContext(UnaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitFuncCallExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryExpressionContext unaryExpression() throws RecognitionException {
		UnaryExpressionContext _localctx = new UnaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_unaryExpression);
		try {
			setState(437);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				_localctx = new PrimaryExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(430);
				primaryExpression();
				}
				break;
			case 2:
				_localctx = new FuncCallExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(431);
				functionCall();
				}
				break;
			case 3:
				_localctx = new PostfixOpExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(432);
				postfixOp();
				}
				break;
			case 4:
				_localctx = new InfixOpExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(433);
				infixOp();
				}
				break;
			case 5:
				_localctx = new UnaryOpExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(434);
				((UnaryOpExprContext)_localctx).op = unaryOp();
				setState(435);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastContext extends ExpressionContext {
		public TypeContext varType;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public CastContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCast(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AddContext extends ExpressionContext {
		public AddOpContext op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public AddOpContext addOp() {
			return getRuleContext(AddOpContext.class,0);
		}
		public AddContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAdd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ShiftContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SHIFT_OP() { return getToken(ReflexParser.SHIFT_OP, 0); }
		public ShiftContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitShift(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BitOrContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BIT_OR_OP() { return getToken(ReflexParser.BIT_OR_OP, 0); }
		public BitOrContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitBitOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OR_OP() { return getToken(ReflexParser.OR_OP, 0); }
		public OrContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MulContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MUL_OP() { return getToken(ReflexParser.MUL_OP, 0); }
		public MulContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitMul(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CheckStateContext extends ExpressionContext {
		public CheckStateExpressionContext checkStateExpression() {
			return getRuleContext(CheckStateExpressionContext.class,0);
		}
		public CheckStateContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCheckState(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryContext extends ExpressionContext {
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public UnaryContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitUnary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BitXorContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BIT_XOR_OP() { return getToken(ReflexParser.BIT_XOR_OP, 0); }
		public BitXorContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitBitXor(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EqualContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EQ_OP() { return getToken(ReflexParser.EQ_OP, 0); }
		public EqualContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitEqual(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AND_OP() { return getToken(ReflexParser.AND_OP, 0); }
		public AndContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BitAndContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BIT_AND_OP() { return getToken(ReflexParser.BIT_AND_OP, 0); }
		public BitAndContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitBitAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignContext extends ExpressionContext {
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public AssignOpContext assignOp() {
			return getRuleContext(AssignOpContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAssign(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompareContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COMP_OP() { return getToken(ReflexParser.COMP_OP, 0); }
		public CompareContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitCompare(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 80;
		enterRecursionRule(_localctx, 80, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				{
				_localctx = new UnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(440);
				unaryExpression();
				}
				break;
			case 2:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(441);
				match(T__17);
				setState(442);
				((CastContext)_localctx).varType = type();
				setState(443);
				match(T__18);
				setState(444);
				expression(13);
				}
				break;
			case 3:
				{
				_localctx = new AssignContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(446);
				match(ID);
				setState(447);
				assignOp();
				setState(448);
				expression(2);
				}
				break;
			case 4:
				{
				_localctx = new CheckStateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(450);
				checkStateExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(486);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(484);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
					case 1:
						{
						_localctx = new MulContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(453);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(454);
						((MulContext)_localctx).op = match(MUL_OP);
						setState(455);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new AddContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(456);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(457);
						((AddContext)_localctx).op = addOp();
						setState(458);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new ShiftContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(460);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(461);
						((ShiftContext)_localctx).op = match(SHIFT_OP);
						setState(462);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new CompareContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(463);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(464);
						((CompareContext)_localctx).op = match(COMP_OP);
						setState(465);
						expression(10);
						}
						break;
					case 5:
						{
						_localctx = new EqualContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(466);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(467);
						((EqualContext)_localctx).op = match(EQ_OP);
						setState(468);
						expression(9);
						}
						break;
					case 6:
						{
						_localctx = new BitAndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(469);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(470);
						match(BIT_AND_OP);
						setState(471);
						expression(8);
						}
						break;
					case 7:
						{
						_localctx = new BitXorContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(472);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(473);
						match(BIT_XOR_OP);
						setState(474);
						expression(7);
						}
						break;
					case 8:
						{
						_localctx = new BitOrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(475);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(476);
						match(BIT_OR_OP);
						setState(477);
						expression(6);
						}
						break;
					case 9:
						{
						_localctx = new AndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(478);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(479);
						match(AND_OP);
						setState(480);
						expression(5);
						}
						break;
					case 10:
						{
						_localctx = new OrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(481);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(482);
						match(OR_OP);
						setState(483);
						expression(4);
						}
						break;
					}
					} 
				}
				setState(488);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOpContext extends ParserRuleContext {
		public UnaryOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryOp; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitUnaryOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryOpContext unaryOp() throws RecognitionException {
		UnaryOpContext _localctx = new UnaryOpContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_unaryOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(489);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4123168604160L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AddOpContext extends ParserRuleContext {
		public AddOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_addOp; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAddOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AddOpContext addOp() throws RecognitionException {
		AddOpContext _localctx = new AddOpContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_addOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(491);
			_la = _input.LA(1);
			if ( !(_la==T__37 || _la==T__38) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignOpContext extends ParserRuleContext {
		public AssignOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignOp; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitAssignOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignOpContext assignOp() throws RecognitionException {
		AssignOpContext _localctx = new AssignOpContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_assignOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(493);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4499201580924928L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			_la = _input.LA(1);
			if ( !(((((_la - 52)) & ~0x3f) == 0 && ((1L << (_la - 52)) & 8191L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 40:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 12);
		case 1:
			return precpred(_ctx, 11);
		case 2:
			return precpred(_ctx, 10);
		case 3:
			return precpred(_ctx, 9);
		case 4:
			return precpred(_ctx, 8);
		case 5:
			return precpred(_ctx, 7);
		case 6:
			return precpred(_ctx, 6);
		case 7:
			return precpred(_ctx, 5);
		case 8:
			return precpred(_ctx, 4);
		case 9:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001f\u01f2\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000_\b\u0000\n\u0000"+
		"\f\u0000b\t\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005"+
		"\u0000n\b\u0000\n\u0000\f\u0000q\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001y\b\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005"+
		"\u0002\u0081\b\u0002\n\u0002\f\u0002\u0084\t\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u008b\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0005\u0002\u008f\b\u0002\n\u0002\f\u0002\u0092\t\u0002\u0001"+
		"\u0002\u0005\u0002\u0095\b\u0002\n\u0002\f\u0002\u0098\t\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003"+
		"\u00a0\b\u0003\n\u0003\f\u0003\u00a3\t\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u00a8\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u00ad\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00b6\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u00bc\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u00c2\b\u0006\n\u0006\f\u0006"+
		"\u00c5\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u00cd\b\u0007\u0001\u0007\u0003\u0007\u00d0\b"+
		"\u0007\u0001\b\u0001\b\u0003\b\u00d4\b\b\u0001\b\u0001\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0003\n\u00e0\b\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u00e8\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f"+
		"\u00f0\b\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0003\r\u00f7\b\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u00fe"+
		"\b\u000e\n\u000e\f\u000e\u0101\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u011f\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u012e\b\u0013"+
		"\u0001\u0014\u0005\u0014\u0131\b\u0014\n\u0014\f\u0014\u0134\t\u0014\u0001"+
		"\u0015\u0001\u0015\u0005\u0015\u0138\b\u0015\n\u0015\f\u0015\u013b\t\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0146\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017"+
		"\u014e\b\u0017\n\u0017\f\u0017\u0151\t\u0017\u0001\u0017\u0003\u0017\u0154"+
		"\b\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u0160"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u001a\u0005\u001a\u0169\b\u001a\n\u001a\f\u001a\u016c\t\u001a"+
		"\u0001\u001a\u0003\u001a\u016f\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001c\u0001\u001c\u0003\u001c\u0176\b\u001c\u0001\u001d\u0001\u001d"+
		"\u0003\u001d\u017a\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001f"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0003 \u0188\b \u0001!\u0001!\u0001!\u0001!\u0001!\u0005!\u018f\b!\n"+
		"!\f!\u0192\t!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001#\u0001#\u0001$\u0001$\u0001$\u0001%\u0001%\u0001%\u0001&\u0001"+
		"&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003&\u01ad\b&\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u01b6\b\'\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0003(\u01c4\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0005(\u01e5\b(\n(\f(\u01e8\t(\u0001)\u0001"+
		")\u0001*\u0001*\u0001+\u0001+\u0001,\u0001,\u0001,\u0000\u0001P-\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFHJLNPRTVX\u0000\u0006\u0002\u0000\u001c\u001d"+
		"$%\u0001\u0000MN\u0001\u0000&)\u0001\u0000&\'\u0002\u0000\u0010\u0010"+
		"*3\u0001\u00004@\u020c\u0000`\u0001\u0000\u0000\u0000\u0002u\u0001\u0000"+
		"\u0000\u0000\u0004\u0082\u0001\u0000\u0000\u0000\u0006\u00a1\u0001\u0000"+
		"\u0000\u0000\b\u00b5\u0001\u0000\u0000\u0000\n\u00bb\u0001\u0000\u0000"+
		"\u0000\f\u00bd\u0001\u0000\u0000\u0000\u000e\u00cc\u0001\u0000\u0000\u0000"+
		"\u0010\u00d3\u0001\u0000\u0000\u0000\u0012\u00d7\u0001\u0000\u0000\u0000"+
		"\u0014\u00dc\u0001\u0000\u0000\u0000\u0016\u00e3\u0001\u0000\u0000\u0000"+
		"\u0018\u00e9\u0001\u0000\u0000\u0000\u001a\u00f6\u0001\u0000\u0000\u0000"+
		"\u001c\u00f8\u0001\u0000\u0000\u0000\u001e\u0104\u0001\u0000\u0000\u0000"+
		" \u010b\u0001\u0000\u0000\u0000\"\u0112\u0001\u0000\u0000\u0000$\u011b"+
		"\u0001\u0000\u0000\u0000&\u012d\u0001\u0000\u0000\u0000(\u0132\u0001\u0000"+
		"\u0000\u0000*\u0135\u0001\u0000\u0000\u0000,\u013e\u0001\u0000\u0000\u0000"+
		".\u0147\u0001\u0000\u0000\u00000\u0157\u0001\u0000\u0000\u00002\u0161"+
		"\u0001\u0000\u0000\u00004\u016a\u0001\u0000\u0000\u00006\u0170\u0001\u0000"+
		"\u0000\u00008\u0173\u0001\u0000\u0000\u0000:\u0177\u0001\u0000\u0000\u0000"+
		"<\u017b\u0001\u0000\u0000\u0000>\u017e\u0001\u0000\u0000\u0000@\u0182"+
		"\u0001\u0000\u0000\u0000B\u0189\u0001\u0000\u0000\u0000D\u0195\u0001\u0000"+
		"\u0000\u0000F\u019b\u0001\u0000\u0000\u0000H\u019d\u0001\u0000\u0000\u0000"+
		"J\u01a0\u0001\u0000\u0000\u0000L\u01ac\u0001\u0000\u0000\u0000N\u01b5"+
		"\u0001\u0000\u0000\u0000P\u01c3\u0001\u0000\u0000\u0000R\u01e9\u0001\u0000"+
		"\u0000\u0000T\u01eb\u0001\u0000\u0000\u0000V\u01ed\u0001\u0000\u0000\u0000"+
		"X\u01ef\u0001\u0000\u0000\u0000Z[\u0005\u0001\u0000\u0000[\\\u0003\b\u0004"+
		"\u0000\\]\u0005\u0002\u0000\u0000]_\u0001\u0000\u0000\u0000^Z\u0001\u0000"+
		"\u0000\u0000_b\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000`a\u0001"+
		"\u0000\u0000\u0000ac\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000"+
		"cd\u0005\u0003\u0000\u0000de\u0005f\u0000\u0000ef\u0005\u0004\u0000\u0000"+
		"fo\u0003\u0002\u0001\u0000gn\u0003 \u0010\u0000hn\u0003\"\u0011\u0000"+
		"in\u0003\u001c\u000e\u0000jn\u0003\u0010\b\u0000kn\u0003\u001e\u000f\u0000"+
		"ln\u0003\u0004\u0002\u0000mg\u0001\u0000\u0000\u0000mh\u0001\u0000\u0000"+
		"\u0000mi\u0001\u0000\u0000\u0000mj\u0001\u0000\u0000\u0000mk\u0001\u0000"+
		"\u0000\u0000ml\u0001\u0000\u0000\u0000nq\u0001\u0000\u0000\u0000om\u0001"+
		"\u0000\u0000\u0000op\u0001\u0000\u0000\u0000pr\u0001\u0000\u0000\u0000"+
		"qo\u0001\u0000\u0000\u0000rs\u0005\u0005\u0000\u0000st\u0005\u0000\u0000"+
		"\u0001t\u0001\u0001\u0000\u0000\u0000ux\u0005\u0006\u0000\u0000vy\u0005"+
		"N\u0000\u0000wy\u0005]\u0000\u0000xv\u0001\u0000\u0000\u0000xw\u0001\u0000"+
		"\u0000\u0000yz\u0001\u0000\u0000\u0000z{\u0005\u0007\u0000\u0000{\u0003"+
		"\u0001\u0000\u0000\u0000|}\u0005\u0001\u0000\u0000}~\u0003\b\u0004\u0000"+
		"~\u007f\u0005\u0002\u0000\u0000\u007f\u0081\u0001\u0000\u0000\u0000\u0080"+
		"|\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000\u0000\u0082\u0080"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0085"+
		"\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085\u0086"+
		"\u0005\b\u0000\u0000\u0086\u0087\u0005f\u0000\u0000\u0087\u0090\u0005"+
		"\u0004\u0000\u0000\u0088\u008b\u0003\f\u0006\u0000\u0089\u008b\u0003\u000e"+
		"\u0007\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a\u0089\u0001\u0000"+
		"\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008d\u0005\u0007"+
		"\u0000\u0000\u008d\u008f\u0001\u0000\u0000\u0000\u008e\u008a\u0001\u0000"+
		"\u0000\u0000\u008f\u0092\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000"+
		"\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0096\u0001\u0000"+
		"\u0000\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0093\u0095\u0003\u0006"+
		"\u0003\u0000\u0094\u0093\u0001\u0000\u0000\u0000\u0095\u0098\u0001\u0000"+
		"\u0000\u0000\u0096\u0094\u0001\u0000\u0000\u0000\u0096\u0097\u0001\u0000"+
		"\u0000\u0000\u0097\u0099\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000"+
		"\u0000\u0000\u0099\u009a\u0005\u0005\u0000\u0000\u009a\u0005\u0001\u0000"+
		"\u0000\u0000\u009b\u009c\u0005\u0001\u0000\u0000\u009c\u009d\u0003\b\u0004"+
		"\u0000\u009d\u009e\u0005\u0002\u0000\u0000\u009e\u00a0\u0001\u0000\u0000"+
		"\u0000\u009f\u009b\u0001\u0000\u0000\u0000\u00a0\u00a3\u0001\u0000\u0000"+
		"\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000"+
		"\u0000\u00a2\u00a4\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a4\u00a5\u0005\t\u0000\u0000\u00a5\u00a7\u0005f\u0000\u0000"+
		"\u00a6\u00a8\u0005\n\u0000\u0000\u00a7\u00a6\u0001\u0000\u0000\u0000\u00a7"+
		"\u00a8\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9"+
		"\u00aa\u0005\u0004\u0000\u0000\u00aa\u00ac\u0003(\u0014\u0000\u00ab\u00ad"+
		"\u0003\u0018\f\u0000\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00af\u0005"+
		"\u0005\u0000\u0000\u00af\u0007\u0001\u0000\u0000\u0000\u00b0\u00b1\u0003"+
		"\n\u0005\u0000\u00b1\u00b2\u0005\u000b\u0000\u0000\u00b2\u00b3\u0005d"+
		"\u0000\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000\u00b4\u00b6\u0003\n\u0005"+
		"\u0000\u00b5\u00b0\u0001\u0000\u0000\u0000\u00b5\u00b4\u0001\u0000\u0000"+
		"\u0000\u00b6\t\u0001\u0000\u0000\u0000\u00b7\u00b8\u0005f\u0000\u0000"+
		"\u00b8\u00b9\u0005\f\u0000\u0000\u00b9\u00bc\u0005f\u0000\u0000\u00ba"+
		"\u00bc\u0005f\u0000\u0000\u00bb\u00b7\u0001\u0000\u0000\u0000\u00bb\u00ba"+
		"\u0001\u0000\u0000\u0000\u00bc\u000b\u0001\u0000\u0000\u0000\u00bd\u00be"+
		"\u0005\r\u0000\u0000\u00be\u00c3\u0005f\u0000\u0000\u00bf\u00c0\u0005"+
		"\u000e\u0000\u0000\u00c0\u00c2\u0005f\u0000\u0000\u00c1\u00bf\u0001\u0000"+
		"\u0000\u0000\u00c2\u00c5\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c6\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c6\u00c7\u0005\u000f"+
		"\u0000\u0000\u00c7\u00c8\u0005\b\u0000\u0000\u00c8\u00c9\u0005f\u0000"+
		"\u0000\u00c9\r\u0001\u0000\u0000\u0000\u00ca\u00cd\u0003\u0012\t\u0000"+
		"\u00cb\u00cd\u0003\u0016\u000b\u0000\u00cc\u00ca\u0001\u0000\u0000\u0000"+
		"\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u00cf\u0001\u0000\u0000\u0000"+
		"\u00ce\u00d0\u0005\r\u0000\u0000\u00cf\u00ce\u0001\u0000\u0000\u0000\u00cf"+
		"\u00d0\u0001\u0000\u0000\u0000\u00d0\u000f\u0001\u0000\u0000\u0000\u00d1"+
		"\u00d4\u0003\u0012\t\u0000\u00d2\u00d4\u0003\u0016\u000b\u0000\u00d3\u00d1"+
		"\u0001\u0000\u0000\u0000\u00d3\u00d2\u0001\u0000\u0000\u0000\u00d4\u00d5"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005\u0007\u0000\u0000\u00d6\u0011"+
		"\u0001\u0000\u0000\u0000\u00d7\u00d8\u0003X,\u0000\u00d8\u00d9\u0005f"+
		"\u0000\u0000\u00d9\u00da\u0005\u0010\u0000\u0000\u00da\u00db\u0003\u0014"+
		"\n\u0000\u00db\u0013\u0001\u0000\u0000\u0000\u00dc\u00dd\u0005f\u0000"+
		"\u0000\u00dd\u00df\u0005\u0001\u0000\u0000\u00de\u00e0\u0005N\u0000\u0000"+
		"\u00df\u00de\u0001\u0000\u0000\u0000\u00df\u00e0\u0001\u0000\u0000\u0000"+
		"\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1\u00e2\u0005\u0002\u0000\u0000"+
		"\u00e2\u0015\u0001\u0000\u0000\u0000\u00e3\u00e4\u0003X,\u0000\u00e4\u00e7"+
		"\u0005f\u0000\u0000\u00e5\u00e6\u0005\u0010\u0000\u0000\u00e6\u00e8\u0003"+
		"P(\u0000\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000"+
		"\u0000\u00e8\u0017\u0001\u0000\u0000\u0000\u00e9\u00ef\u0005\u0011\u0000"+
		"\u0000\u00ea\u00f0\u0003\u001a\r\u0000\u00eb\u00ec\u0005\u0012\u0000\u0000"+
		"\u00ec\u00ed\u0003\u001a\r\u0000\u00ed\u00ee\u0005\u0013\u0000\u0000\u00ee"+
		"\u00f0\u0001\u0000\u0000\u0000\u00ef\u00ea\u0001\u0000\u0000\u0000\u00ef"+
		"\u00eb\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1"+
		"\u00f2\u0003&\u0013\u0000\u00f2\u0019\u0001\u0000\u0000\u0000\u00f3\u00f7"+
		"\u0005]\u0000\u0000\u00f4\u00f7\u0005N\u0000\u0000\u00f5\u00f7\u0005f"+
		"\u0000\u0000\u00f6\u00f3\u0001\u0000\u0000\u0000\u00f6\u00f4\u0001\u0000"+
		"\u0000\u0000\u00f6\u00f5\u0001\u0000\u0000\u0000\u00f7\u001b\u0001\u0000"+
		"\u0000\u0000\u00f8\u00f9\u0003X,\u0000\u00f9\u00fa\u0005\u0012\u0000\u0000"+
		"\u00fa\u00ff\u0003X,\u0000\u00fb\u00fc\u0005\u000e\u0000\u0000\u00fc\u00fe"+
		"\u0003X,\u0000\u00fd\u00fb\u0001\u0000\u0000\u0000\u00fe\u0101\u0001\u0000"+
		"\u0000\u0000\u00ff\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000"+
		"\u0000\u0000\u0100\u0102\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000"+
		"\u0000\u0000\u0102\u0103\u0005\u0013\u0000\u0000\u0103\u001d\u0001\u0000"+
		"\u0000\u0000\u0104\u0105\u0005A\u0000\u0000\u0105\u0106\u0005f\u0000\u0000"+
		"\u0106\u0107\u0005N\u0000\u0000\u0107\u0108\u0005N\u0000\u0000\u0108\u0109"+
		"\u0005N\u0000\u0000\u0109\u010a\u0005\u0007\u0000\u0000\u010a\u001f\u0001"+
		"\u0000\u0000\u0000\u010b\u010c\u0005\u0014\u0000\u0000\u010c\u010d\u0003"+
		"X,\u0000\u010d\u010e\u0005f\u0000\u0000\u010e\u010f\u0005\u0010\u0000"+
		"\u0000\u010f\u0110\u0003P(\u0000\u0110\u0111\u0005\u0007\u0000\u0000\u0111"+
		"!\u0001\u0000\u0000\u0000\u0112\u0113\u0005\u0015\u0000\u0000\u0113\u0114"+
		"\u0005f\u0000\u0000\u0114\u0115\u0005\u0004\u0000\u0000\u0115\u0116\u0003"+
		"$\u0012\u0000\u0116\u0117\u0005\u000e\u0000\u0000\u0117\u0118\u0003$\u0012"+
		"\u0000\u0118\u0119\u0001\u0000\u0000\u0000\u0119\u011a\u0005\u0005\u0000"+
		"\u0000\u011a#\u0001\u0000\u0000\u0000\u011b\u011e\u0005f\u0000\u0000\u011c"+
		"\u011d\u0005\u0010\u0000\u0000\u011d\u011f\u0003P(\u0000\u011e\u011c\u0001"+
		"\u0000\u0000\u0000\u011e\u011f\u0001\u0000\u0000\u0000\u011f%\u0001\u0000"+
		"\u0000\u0000\u0120\u012e\u0005\u0007\u0000\u0000\u0121\u012e\u0003*\u0015"+
		"\u0000\u0122\u012e\u00036\u001b\u0000\u0123\u012e\u00038\u001c\u0000\u0124"+
		"\u012e\u0003:\u001d\u0000\u0125\u012e\u0003<\u001e\u0000\u0126\u012e\u0003"+
		">\u001f\u0000\u0127\u012e\u0003@ \u0000\u0128\u012e\u0003,\u0016\u0000"+
		"\u0129\u012e\u0003.\u0017\u0000\u012a\u012b\u0003P(\u0000\u012b\u012c"+
		"\u0005\u0007\u0000\u0000\u012c\u012e\u0001\u0000\u0000\u0000\u012d\u0120"+
		"\u0001\u0000\u0000\u0000\u012d\u0121\u0001\u0000\u0000\u0000\u012d\u0122"+
		"\u0001\u0000\u0000\u0000\u012d\u0123\u0001\u0000\u0000\u0000\u012d\u0124"+
		"\u0001\u0000\u0000\u0000\u012d\u0125\u0001\u0000\u0000\u0000\u012d\u0126"+
		"\u0001\u0000\u0000\u0000\u012d\u0127\u0001\u0000\u0000\u0000\u012d\u0128"+
		"\u0001\u0000\u0000\u0000\u012d\u0129\u0001\u0000\u0000\u0000\u012d\u012a"+
		"\u0001\u0000\u0000\u0000\u012e\'\u0001\u0000\u0000\u0000\u012f\u0131\u0003"+
		"&\u0013\u0000\u0130\u012f\u0001\u0000\u0000\u0000\u0131\u0134\u0001\u0000"+
		"\u0000\u0000\u0132\u0130\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000"+
		"\u0000\u0000\u0133)\u0001\u0000\u0000\u0000\u0134\u0132\u0001\u0000\u0000"+
		"\u0000\u0135\u0139\u0005\u0004\u0000\u0000\u0136\u0138\u0003&\u0013\u0000"+
		"\u0137\u0136\u0001\u0000\u0000\u0000\u0138\u013b\u0001\u0000\u0000\u0000"+
		"\u0139\u0137\u0001\u0000\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000"+
		"\u013a\u013c\u0001\u0000\u0000\u0000\u013b\u0139\u0001\u0000\u0000\u0000"+
		"\u013c\u013d\u0005\u0005\u0000\u0000\u013d+\u0001\u0000\u0000\u0000\u013e"+
		"\u013f\u0005\u0016\u0000\u0000\u013f\u0140\u0005\u0012\u0000\u0000\u0140"+
		"\u0141\u0003P(\u0000\u0141\u0142\u0005\u0013\u0000\u0000\u0142\u0145\u0003"+
		"&\u0013\u0000\u0143\u0144\u0005\u0017\u0000\u0000\u0144\u0146\u0003&\u0013"+
		"\u0000\u0145\u0143\u0001\u0000\u0000\u0000\u0145\u0146\u0001\u0000\u0000"+
		"\u0000\u0146-\u0001\u0000\u0000\u0000\u0147\u0148\u0005\u0018\u0000\u0000"+
		"\u0148\u0149\u0005\u0012\u0000\u0000\u0149\u014a\u0003P(\u0000\u014a\u014b"+
		"\u0005\u0013\u0000\u0000\u014b\u014f\u0005\u0004\u0000\u0000\u014c\u014e"+
		"\u00030\u0018\u0000\u014d\u014c\u0001\u0000\u0000\u0000\u014e\u0151\u0001"+
		"\u0000\u0000\u0000\u014f\u014d\u0001\u0000\u0000\u0000\u014f\u0150\u0001"+
		"\u0000\u0000\u0000\u0150\u0153\u0001\u0000\u0000\u0000\u0151\u014f\u0001"+
		"\u0000\u0000\u0000\u0152\u0154\u00032\u0019\u0000\u0153\u0152\u0001\u0000"+
		"\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154\u0155\u0001\u0000"+
		"\u0000\u0000\u0155\u0156\u0005\u0005\u0000\u0000\u0156/\u0001\u0000\u0000"+
		"\u0000\u0157\u0158\u0005\u0019\u0000\u0000\u0158\u0159\u0003P(\u0000\u0159"+
		"\u015f\u0005\u000b\u0000\u0000\u015a\u015b\u0005\u0004\u0000\u0000\u015b"+
		"\u015c\u00034\u001a\u0000\u015c\u015d\u0005\u0005\u0000\u0000\u015d\u0160"+
		"\u0001\u0000\u0000\u0000\u015e\u0160\u00034\u001a\u0000\u015f\u015a\u0001"+
		"\u0000\u0000\u0000\u015f\u015e\u0001\u0000\u0000\u0000\u01601\u0001\u0000"+
		"\u0000\u0000\u0161\u0162\u0005\u001a\u0000\u0000\u0162\u0163\u0005\u000b"+
		"\u0000\u0000\u0163\u0164\u0005\u0004\u0000\u0000\u0164\u0165\u00034\u001a"+
		"\u0000\u0165\u0166\u0005\u0005\u0000\u0000\u01663\u0001\u0000\u0000\u0000"+
		"\u0167\u0169\u0003&\u0013\u0000\u0168\u0167\u0001\u0000\u0000\u0000\u0169"+
		"\u016c\u0001\u0000\u0000\u0000\u016a\u0168\u0001\u0000\u0000\u0000\u016a"+
		"\u016b\u0001\u0000\u0000\u0000\u016b\u016e\u0001\u0000\u0000\u0000\u016c"+
		"\u016a\u0001\u0000\u0000\u0000\u016d\u016f\u0005B\u0000\u0000\u016e\u016d"+
		"\u0001\u0000\u0000\u0000\u016e\u016f\u0001\u0000\u0000\u0000\u016f5\u0001"+
		"\u0000\u0000\u0000\u0170\u0171\u0005\u001b\u0000\u0000\u0171\u0172\u0005"+
		"f\u0000\u0000\u01727\u0001\u0000\u0000\u0000\u0173\u0175\u0005\u001c\u0000"+
		"\u0000\u0174\u0176\u0005f\u0000\u0000\u0175\u0174\u0001\u0000\u0000\u0000"+
		"\u0175\u0176\u0001\u0000\u0000\u0000\u01769\u0001\u0000\u0000\u0000\u0177"+
		"\u0179\u0005\u001d\u0000\u0000\u0178\u017a\u0005f\u0000\u0000\u0179\u0178"+
		"\u0001\u0000\u0000\u0000\u0179\u017a\u0001\u0000\u0000\u0000\u017a;\u0001"+
		"\u0000\u0000\u0000\u017b\u017c\u0005\u001e\u0000\u0000\u017c\u017d\u0005"+
		"\u0007\u0000\u0000\u017d=\u0001\u0000\u0000\u0000\u017e\u017f\u0005\u001f"+
		"\u0000\u0000\u017f\u0180\u0005 \u0000\u0000\u0180\u0181\u0005\u0007\u0000"+
		"\u0000\u0181?\u0001\u0000\u0000\u0000\u0182\u0187\u0005!\u0000\u0000\u0183"+
		"\u0184\u0005\"\u0000\u0000\u0184\u0188\u0005\t\u0000\u0000\u0185\u0186"+
		"\u0005\t\u0000\u0000\u0186\u0188\u0005f\u0000\u0000\u0187\u0183\u0001"+
		"\u0000\u0000\u0000\u0187\u0185\u0001\u0000\u0000\u0000\u0188A\u0001\u0000"+
		"\u0000\u0000\u0189\u018a\u0005f\u0000\u0000\u018a\u018b\u0005\u0012\u0000"+
		"\u0000\u018b\u0190\u0003P(\u0000\u018c\u018d\u0005\u000e\u0000\u0000\u018d"+
		"\u018f\u0003P(\u0000\u018e\u018c\u0001\u0000\u0000\u0000\u018f\u0192\u0001"+
		"\u0000\u0000\u0000\u0190\u018e\u0001\u0000\u0000\u0000\u0190\u0191\u0001"+
		"\u0000\u0000\u0000\u0191\u0193\u0001\u0000\u0000\u0000\u0192\u0190\u0001"+
		"\u0000\u0000\u0000\u0193\u0194\u0005\u0013\u0000\u0000\u0194C\u0001\u0000"+
		"\u0000\u0000\u0195\u0196\u0005\b\u0000\u0000\u0196\u0197\u0005f\u0000"+
		"\u0000\u0197\u0198\u0005#\u0000\u0000\u0198\u0199\u0005\t\u0000\u0000"+
		"\u0199\u019a\u0003F#\u0000\u019aE\u0001\u0000\u0000\u0000\u019b\u019c"+
		"\u0007\u0000\u0000\u0000\u019cG\u0001\u0000\u0000\u0000\u019d\u019e\u0005"+
		"C\u0000\u0000\u019e\u019f\u0005f\u0000\u0000\u019fI\u0001\u0000\u0000"+
		"\u0000\u01a0\u01a1\u0005f\u0000\u0000\u01a1\u01a2\u0005C\u0000\u0000\u01a2"+
		"K\u0001\u0000\u0000\u0000\u01a3\u01ad\u0005f\u0000\u0000\u01a4\u01ad\u0007"+
		"\u0001\u0000\u0000\u01a5\u01ad\u0005O\u0000\u0000\u01a6\u01ad\u0005c\u0000"+
		"\u0000\u01a7\u01ad\u0005]\u0000\u0000\u01a8\u01a9\u0005\u0012\u0000\u0000"+
		"\u01a9\u01aa\u0003P(\u0000\u01aa\u01ab\u0005\u0013\u0000\u0000\u01ab\u01ad"+
		"\u0001\u0000\u0000\u0000\u01ac\u01a3\u0001\u0000\u0000\u0000\u01ac\u01a4"+
		"\u0001\u0000\u0000\u0000\u01ac\u01a5\u0001\u0000\u0000\u0000\u01ac\u01a6"+
		"\u0001\u0000\u0000\u0000\u01ac\u01a7\u0001\u0000\u0000\u0000\u01ac\u01a8"+
		"\u0001\u0000\u0000\u0000\u01adM\u0001\u0000\u0000\u0000\u01ae\u01b6\u0003"+
		"L&\u0000\u01af\u01b6\u0003B!\u0000\u01b0\u01b6\u0003J%\u0000\u01b1\u01b6"+
		"\u0003H$\u0000\u01b2\u01b3\u0003R)\u0000\u01b3\u01b4\u0003P(\u0000\u01b4"+
		"\u01b6\u0001\u0000\u0000\u0000\u01b5\u01ae\u0001\u0000\u0000\u0000\u01b5"+
		"\u01af\u0001\u0000\u0000\u0000\u01b5\u01b0\u0001\u0000\u0000\u0000\u01b5"+
		"\u01b1\u0001\u0000\u0000\u0000\u01b5\u01b2\u0001\u0000\u0000\u0000\u01b6"+
		"O\u0001\u0000\u0000\u0000\u01b7\u01b8\u0006(\uffff\uffff\u0000\u01b8\u01c4"+
		"\u0003N\'\u0000\u01b9\u01ba\u0005\u0012\u0000\u0000\u01ba\u01bb\u0003"+
		"X,\u0000\u01bb\u01bc\u0005\u0013\u0000\u0000\u01bc\u01bd\u0003P(\r\u01bd"+
		"\u01c4\u0001\u0000\u0000\u0000\u01be\u01bf\u0005f\u0000\u0000\u01bf\u01c0"+
		"\u0003V+\u0000\u01c0\u01c1\u0003P(\u0002\u01c1\u01c4\u0001\u0000\u0000"+
		"\u0000\u01c2\u01c4\u0003D\"\u0000\u01c3\u01b7\u0001\u0000\u0000\u0000"+
		"\u01c3\u01b9\u0001\u0000\u0000\u0000\u01c3\u01be\u0001\u0000\u0000\u0000"+
		"\u01c3\u01c2\u0001\u0000\u0000\u0000\u01c4\u01e6\u0001\u0000\u0000\u0000"+
		"\u01c5\u01c6\n\f\u0000\u0000\u01c6\u01c7\u0005D\u0000\u0000\u01c7\u01e5"+
		"\u0003P(\r\u01c8\u01c9\n\u000b\u0000\u0000\u01c9\u01ca\u0003T*\u0000\u01ca"+
		"\u01cb\u0003P(\f\u01cb\u01e5\u0001\u0000\u0000\u0000\u01cc\u01cd\n\n\u0000"+
		"\u0000\u01cd\u01ce\u0005E\u0000\u0000\u01ce\u01e5\u0003P(\u000b\u01cf"+
		"\u01d0\n\t\u0000\u0000\u01d0\u01d1\u0005L\u0000\u0000\u01d1\u01e5\u0003"+
		"P(\n\u01d2\u01d3\n\b\u0000\u0000\u01d3\u01d4\u0005F\u0000\u0000\u01d4"+
		"\u01e5\u0003P(\t\u01d5\u01d6\n\u0007\u0000\u0000\u01d6\u01d7\u0005G\u0000"+
		"\u0000\u01d7\u01e5\u0003P(\b\u01d8\u01d9\n\u0006\u0000\u0000\u01d9\u01da"+
		"\u0005H\u0000\u0000\u01da\u01e5\u0003P(\u0007\u01db\u01dc\n\u0005\u0000"+
		"\u0000\u01dc\u01dd\u0005I\u0000\u0000\u01dd\u01e5\u0003P(\u0006\u01de"+
		"\u01df\n\u0004\u0000\u0000\u01df\u01e0\u0005J\u0000\u0000\u01e0\u01e5"+
		"\u0003P(\u0005\u01e1\u01e2\n\u0003\u0000\u0000\u01e2\u01e3\u0005K\u0000"+
		"\u0000\u01e3\u01e5\u0003P(\u0004\u01e4\u01c5\u0001\u0000\u0000\u0000\u01e4"+
		"\u01c8\u0001\u0000\u0000\u0000\u01e4\u01cc\u0001\u0000\u0000\u0000\u01e4"+
		"\u01cf\u0001\u0000\u0000\u0000\u01e4\u01d2\u0001\u0000\u0000\u0000\u01e4"+
		"\u01d5\u0001\u0000\u0000\u0000\u01e4\u01d8\u0001\u0000\u0000\u0000\u01e4"+
		"\u01db\u0001\u0000\u0000\u0000\u01e4\u01de\u0001\u0000\u0000\u0000\u01e4"+
		"\u01e1\u0001\u0000\u0000\u0000\u01e5\u01e8\u0001\u0000\u0000\u0000\u01e6"+
		"\u01e4\u0001\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000\u0000\u0000\u01e7"+
		"Q\u0001\u0000\u0000\u0000\u01e8\u01e6\u0001\u0000\u0000\u0000\u01e9\u01ea"+
		"\u0007\u0002\u0000\u0000\u01eaS\u0001\u0000\u0000\u0000\u01eb\u01ec\u0007"+
		"\u0003\u0000\u0000\u01ecU\u0001\u0000\u0000\u0000\u01ed\u01ee\u0007\u0004"+
		"\u0000\u0000\u01eeW\u0001\u0000\u0000\u0000\u01ef\u01f0\u0007\u0005\u0000"+
		"\u0000\u01f0Y\u0001\u0000\u0000\u0000)`mox\u0082\u008a\u0090\u0096\u00a1"+
		"\u00a7\u00ac\u00b5\u00bb\u00c3\u00cc\u00cf\u00d3\u00df\u00e7\u00ef\u00f6"+
		"\u00ff\u011e\u012d\u0132\u0139\u0145\u014f\u0153\u015f\u016a\u016e\u0175"+
		"\u0179\u0187\u0190\u01ac\u01b5\u01c3\u01e4\u01e6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}