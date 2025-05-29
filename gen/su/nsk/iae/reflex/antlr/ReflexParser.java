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
import su.nsk.iae.reflex.antlr.ReflexVisitor;

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
		T__59=60, T__60=61, T__61=62, T__62=63, T__63=64, T__64=65, T__65=66, 
		PORT_TYPE=67, BREAK=68, STATE_QUAL=69, INFIX_POSTFIX_OP=70, MUL_OP=71, 
		SHIFT_OP=72, EQ_OP=73, BIT_AND_OP=74, BIT_XOR_OP=75, BIT_OR_OP=76, AND_OP=77, 
		OR_OP=78, COMP_OP=79, ID=80, UNSIGNED_INTEGER=81, FLOAT=82, DEC_FLOAT=83, 
		HEX_FLOAT=84, DEC_SEQ=85, HEX_SEQ=86, BIN_EXPONENT=87, EXPONENT=88, DECIMAL=89, 
		OCTAL=90, HEX=91, HEX_PREFIX=92, LONG=93, FLOAT_SUFFIX=94, UNSIGNED=95, 
		TIME=96, DAY=97, HOUR=98, MINUTE=99, SECOND=100, MILISECOND=101, BOOL_VAL=102, 
		STRING=103, WS=104;
	public static final int
		RULE_program = 0, RULE_clockDefinition = 1, RULE_process = 2, RULE_state = 3, 
		RULE_lightweightState = 4, RULE_annotation = 5, RULE_annotationKey = 6, 
		RULE_importedVariableList = 7, RULE_processVariable = 8, RULE_globalVariable = 9, 
		RULE_physicalVariable = 10, RULE_portMapping = 11, RULE_programVariable = 12, 
		RULE_structDeclaration = 13, RULE_timeoutFunction = 14, RULE_timeAmountOrRef = 15, 
		RULE_functionDecl = 16, RULE_port = 17, RULE_const = 18, RULE_enum = 19, 
		RULE_enumMember = 20, RULE_guardingStatement = 21, RULE_statement = 22, 
		RULE_statementSeq = 23, RULE_compoundStatement = 24, RULE_ifElseStat = 25, 
		RULE_switchStat = 26, RULE_caseStat = 27, RULE_defaultStat = 28, RULE_switchOptionStatSeq = 29, 
		RULE_startProcStat = 30, RULE_stopProcStat = 31, RULE_errorProcStat = 32, 
		RULE_restartStat = 33, RULE_resetStat = 34, RULE_setStateStat = 35, RULE_functionCall = 36, 
		RULE_checkStateExpression = 37, RULE_infixOp = 38, RULE_postfixOp = 39, 
		RULE_primaryExpression = 40, RULE_unaryExpression = 41, RULE_expression = 42, 
		RULE_variable = 43, RULE_unaryOp = 44, RULE_addOp = 45, RULE_assignOp = 46, 
		RULE_type = 47, RULE_integer = 48;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "clockDefinition", "process", "state", "lightweightState", 
			"annotation", "annotationKey", "importedVariableList", "processVariable", 
			"globalVariable", "physicalVariable", "portMapping", "programVariable", 
			"structDeclaration", "timeoutFunction", "timeAmountOrRef", "functionDecl", 
			"port", "const", "enum", "enumMember", "guardingStatement", "statement", 
			"statementSeq", "compoundStatement", "ifElseStat", "switchStat", "caseStat", 
			"defaultStat", "switchOptionStatSeq", "startProcStat", "stopProcStat", 
			"errorProcStat", "restartStat", "resetStat", "setStateStat", "functionCall", 
			"checkStateExpression", "infixOp", "postfixOp", "primaryExpression", 
			"unaryExpression", "expression", "variable", "unaryOp", "addOp", "assignOp", 
			"type", "integer"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "'program'", "'{'", "'}'", "'clock'", "';'", "'process'", 
			"'state'", "'looped'", "':'", "'.'", "'shared'", "','", "'from'", "'='", 
			"'struct'", "'timeout'", "'('", "')'", "'const'", "'enum'", "'wait'", 
			"'slice'", "'transition'", "'if'", "'else'", "'switch'", "'case'", "'default'", 
			"'start'", "'stop'", "'error'", "'restart'", "'reset'", "'timer'", "'set'", 
			"'next'", "'in'", "'+'", "'-'", "'~'", "'!'", "'*='", "'/='", "'%='", 
			"'+='", "'-='", "'<<='", "'>>='", "'&='", "'^='", "'|='", "'void'", "'bool'", 
			"'time'", "'float'", "'double'", "'int8'", "'uint8'", "'int16'", "'uint16'", 
			"'int32'", "'uint32'", "'int64'", "'uint64'", null, null, null, null, 
			null, null, null, "'&'", "'^'", "'|'", "'&&'", "'||'"
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
			null, null, null, null, null, null, null, "PORT_TYPE", "BREAK", "STATE_QUAL", 
			"INFIX_POSTFIX_OP", "MUL_OP", "SHIFT_OP", "EQ_OP", "BIT_AND_OP", "BIT_XOR_OP", 
			"BIT_OR_OP", "AND_OP", "OR_OP", "COMP_OP", "ID", "UNSIGNED_INTEGER", 
			"FLOAT", "DEC_FLOAT", "HEX_FLOAT", "DEC_SEQ", "HEX_SEQ", "BIN_EXPONENT", 
			"EXPONENT", "DECIMAL", "OCTAL", "HEX", "HEX_PREFIX", "LONG", "FLOAT_SUFFIX", 
			"UNSIGNED", "TIME", "DAY", "HOUR", "MINUTE", "SECOND", "MILISECOND", 
			"BOOL_VAL", "STRING", "WS"
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
		public StructDeclarationContext structDeclaration;
		public List<StructDeclarationContext> structures = new ArrayList<StructDeclarationContext>();
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
		public List<StructDeclarationContext> structDeclaration() {
			return getRuleContexts(StructDeclarationContext.class);
		}
		public StructDeclarationContext structDeclaration(int i) {
			return getRuleContext(StructDeclarationContext.class,i);
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
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(98);
				match(T__0);
				setState(99);
				((ProgramContext)_localctx).annotation = annotation();
				((ProgramContext)_localctx).annotations.add(((ProgramContext)_localctx).annotation);
				setState(100);
				match(T__1);
				}
				}
				setState(106);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(107);
			match(T__2);
			setState(108);
			((ProgramContext)_localctx).name = match(ID);
			setState(109);
			match(T__3);
			setState(110);
			((ProgramContext)_localctx).clock = clockDefinition();
			setState(120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -18014398503059198L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 15L) != 0)) {
				{
				setState(118);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(111);
					((ProgramContext)_localctx).const_ = const_();
					((ProgramContext)_localctx).consts.add(((ProgramContext)_localctx).const_);
					}
					break;
				case 2:
					{
					setState(112);
					((ProgramContext)_localctx).enum_ = enum_();
					((ProgramContext)_localctx).enums.add(((ProgramContext)_localctx).enum_);
					}
					break;
				case 3:
					{
					setState(113);
					((ProgramContext)_localctx).functionDecl = functionDecl();
					((ProgramContext)_localctx).functions.add(((ProgramContext)_localctx).functionDecl);
					}
					break;
				case 4:
					{
					setState(114);
					((ProgramContext)_localctx).globalVariable = globalVariable();
					((ProgramContext)_localctx).globalVars.add(((ProgramContext)_localctx).globalVariable);
					}
					break;
				case 5:
					{
					setState(115);
					((ProgramContext)_localctx).port = port();
					((ProgramContext)_localctx).ports.add(((ProgramContext)_localctx).port);
					}
					break;
				case 6:
					{
					setState(116);
					((ProgramContext)_localctx).process = process();
					((ProgramContext)_localctx).processes.add(((ProgramContext)_localctx).process);
					}
					break;
				case 7:
					{
					setState(117);
					((ProgramContext)_localctx).structDeclaration = structDeclaration();
					((ProgramContext)_localctx).structures.add(((ProgramContext)_localctx).structDeclaration);
					}
					break;
				}
				}
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(123);
			match(T__4);
			setState(124);
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
			setState(126);
			match(T__5);
			setState(129);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UNSIGNED_INTEGER:
				{
				setState(127);
				((ClockDefinitionContext)_localctx).intValue = match(UNSIGNED_INTEGER);
				}
				break;
			case TIME:
				{
				setState(128);
				((ClockDefinitionContext)_localctx).timeValue = match(TIME);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(131);
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
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(133);
				match(T__0);
				setState(134);
				((ProcessContext)_localctx).annotation = annotation();
				((ProcessContext)_localctx).annotations.add(((ProcessContext)_localctx).annotation);
				setState(135);
				match(T__1);
				}
				}
				setState(141);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(142);
			match(T__7);
			setState(143);
			((ProcessContext)_localctx).name = match(ID);
			setState(144);
			match(T__3);
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 13)) & ~0x3f) == 0 && ((1L << (_la - 13)) & 18012199486226433L) != 0)) {
				{
				{
				setState(147);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__12:
					{
					setState(145);
					((ProcessContext)_localctx).importedVariableList = importedVariableList();
					((ProcessContext)_localctx).imports.add(((ProcessContext)_localctx).importedVariableList);
					}
					break;
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
				case T__64:
				case T__65:
					{
					setState(146);
					((ProcessContext)_localctx).processVariable = processVariable();
					((ProcessContext)_localctx).variables.add(((ProcessContext)_localctx).processVariable);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(149);
				match(T__6);
				}
				}
				setState(155);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==T__8) {
				{
				{
				setState(156);
				((ProcessContext)_localctx).state = state();
				((ProcessContext)_localctx).states.add(((ProcessContext)_localctx).state);
				}
				}
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(162);
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
		public LightweightStateContext lightweightState;
		public List<LightweightStateContext> lightweightStates = new ArrayList<LightweightStateContext>();
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
		public List<LightweightStateContext> lightweightState() {
			return getRuleContexts(LightweightStateContext.class);
		}
		public LightweightStateContext lightweightState(int i) {
			return getRuleContext(LightweightStateContext.class,i);
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
			setState(170);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(164);
				match(T__0);
				setState(165);
				((StateContext)_localctx).annotation = annotation();
				((StateContext)_localctx).annotations.add(((StateContext)_localctx).annotation);
				setState(166);
				match(T__1);
				}
				}
				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(173);
			match(T__8);
			setState(174);
			((StateContext)_localctx).name = match(ID);
			setState(176);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(175);
				((StateContext)_localctx).looped = match(T__9);
				}
			}

			setState(178);
			match(T__3);
			setState(179);
			((StateContext)_localctx).stateFunction = statementSeq();
			setState(183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 58720256L) != 0)) {
				{
				{
				setState(180);
				((StateContext)_localctx).lightweightState = lightweightState();
				((StateContext)_localctx).lightweightStates.add(((StateContext)_localctx).lightweightState);
				}
				}
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__17) {
				{
				setState(186);
				((StateContext)_localctx).func = timeoutFunction();
				}
			}

			setState(189);
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
	public static class LightweightStateContext extends ParserRuleContext {
		public GuardingStatementContext guardingStatement() {
			return getRuleContext(GuardingStatementContext.class,0);
		}
		public StatementSeqContext statementSeq() {
			return getRuleContext(StatementSeqContext.class,0);
		}
		public LightweightStateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lightweightState; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitLightweightState(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LightweightStateContext lightweightState() throws RecognitionException {
		LightweightStateContext _localctx = new LightweightStateContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_lightweightState);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			guardingStatement();
			setState(192);
			statementSeq();
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
		enterRule(_localctx, 10, RULE_annotation);
		try {
			setState(199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(194);
				((AnnotationContext)_localctx).key = annotationKey();
				setState(195);
				match(T__10);
				setState(196);
				((AnnotationContext)_localctx).value = match(STRING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(198);
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
		enterRule(_localctx, 12, RULE_annotationKey);
		try {
			setState(205);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(201);
				match(ID);
				setState(202);
				match(T__11);
				setState(203);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(204);
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
		enterRule(_localctx, 14, RULE_importedVariableList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			match(T__12);
			{
			setState(208);
			((ImportedVariableListContext)_localctx).ID = match(ID);
			((ImportedVariableListContext)_localctx).variables.add(((ImportedVariableListContext)_localctx).ID);
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(209);
				match(T__13);
				setState(210);
				((ImportedVariableListContext)_localctx).ID = match(ID);
				((ImportedVariableListContext)_localctx).variables.add(((ImportedVariableListContext)_localctx).ID);
				}
				}
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			setState(216);
			match(T__14);
			setState(217);
			match(T__7);
			setState(218);
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
		enterRule(_localctx, 16, RULE_processVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(222);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(220);
				physicalVariable();
				}
				break;
			case 2:
				{
				setState(221);
				programVariable();
				}
				break;
			}
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__12) {
				{
				setState(224);
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
		enterRule(_localctx, 18, RULE_globalVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(227);
				physicalVariable();
				}
				break;
			case 2:
				{
				setState(228);
				programVariable();
				}
				break;
			}
			setState(231);
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
		enterRule(_localctx, 20, RULE_physicalVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			((PhysicalVariableContext)_localctx).varType = type();
			setState(234);
			((PhysicalVariableContext)_localctx).name = match(ID);
			setState(235);
			match(T__15);
			setState(236);
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
		enterRule(_localctx, 22, RULE_portMapping);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			((PortMappingContext)_localctx).portId = match(ID);
			setState(239);
			match(T__0);
			setState(241);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNSIGNED_INTEGER) {
				{
				setState(240);
				((PortMappingContext)_localctx).bit = match(UNSIGNED_INTEGER);
				}
			}

			setState(243);
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
		enterRule(_localctx, 24, RULE_programVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			((ProgramVariableContext)_localctx).varType = type();
			setState(246);
			((ProgramVariableContext)_localctx).name = match(ID);
			setState(249);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(247);
				match(T__15);
				setState(248);
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
	public static class StructDeclarationContext extends ParserRuleContext {
		public Token name;
		public ProgramVariableContext programVariable;
		public List<ProgramVariableContext> variables = new ArrayList<ProgramVariableContext>();
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public List<ProgramVariableContext> programVariable() {
			return getRuleContexts(ProgramVariableContext.class);
		}
		public ProgramVariableContext programVariable(int i) {
			return getRuleContext(ProgramVariableContext.class,i);
		}
		public StructDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitStructDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructDeclarationContext structDeclaration() throws RecognitionException {
		StructDeclarationContext _localctx = new StructDeclarationContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_structDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			match(T__16);
			setState(252);
			((StructDeclarationContext)_localctx).name = match(ID);
			setState(253);
			match(T__3);
			setState(255); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(254);
				((StructDeclarationContext)_localctx).programVariable = programVariable();
				((StructDeclarationContext)_localctx).variables.add(((StructDeclarationContext)_localctx).programVariable);
				}
				}
				setState(257); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 8191L) != 0) );
			setState(259);
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
		enterRule(_localctx, 28, RULE_timeoutFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
			match(T__17);
			setState(267);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
			case UNSIGNED_INTEGER:
			case TIME:
				{
				setState(262);
				timeAmountOrRef();
				}
				break;
			case T__18:
				{
				setState(263);
				match(T__18);
				setState(264);
				timeAmountOrRef();
				setState(265);
				match(T__19);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(269);
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
		enterRule(_localctx, 30, RULE_timeAmountOrRef);
		try {
			setState(274);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TIME:
				enterOuterAlt(_localctx, 1);
				{
				setState(271);
				((TimeAmountOrRefContext)_localctx).time = match(TIME);
				}
				break;
			case UNSIGNED_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(272);
				((TimeAmountOrRefContext)_localctx).intTime = match(UNSIGNED_INTEGER);
				}
				break;
			case ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(273);
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
		enterRule(_localctx, 32, RULE_functionDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			((FunctionDeclContext)_localctx).returnType = type();
			setState(277);
			match(T__18);
			setState(278);
			((FunctionDeclContext)_localctx).type = type();
			((FunctionDeclContext)_localctx).argTypes.add(((FunctionDeclContext)_localctx).type);
			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(279);
				match(T__13);
				setState(280);
				((FunctionDeclContext)_localctx).type = type();
				((FunctionDeclContext)_localctx).argTypes.add(((FunctionDeclContext)_localctx).type);
				}
				}
				setState(285);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(286);
			match(T__19);
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
		enterRule(_localctx, 34, RULE_port);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			((PortContext)_localctx).varType = match(PORT_TYPE);
			setState(289);
			((PortContext)_localctx).name = match(ID);
			setState(290);
			((PortContext)_localctx).addr1 = match(UNSIGNED_INTEGER);
			setState(291);
			((PortContext)_localctx).addr2 = match(UNSIGNED_INTEGER);
			setState(292);
			((PortContext)_localctx).size = match(UNSIGNED_INTEGER);
			setState(293);
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
		enterRule(_localctx, 36, RULE_const);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			match(T__20);
			setState(296);
			((ConstContext)_localctx).varType = type();
			setState(297);
			((ConstContext)_localctx).name = match(ID);
			setState(298);
			match(T__15);
			setState(299);
			((ConstContext)_localctx).value = expression(0);
			setState(300);
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
		enterRule(_localctx, 38, RULE_enum);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(302);
			match(T__21);
			setState(303);
			((EnumContext)_localctx).identifier = match(ID);
			setState(304);
			match(T__3);
			setState(305);
			((EnumContext)_localctx).enumMember = enumMember();
			((EnumContext)_localctx).enumMembers.add(((EnumContext)_localctx).enumMember);
			{
			setState(306);
			match(T__13);
			setState(307);
			((EnumContext)_localctx).enumMember = enumMember();
			((EnumContext)_localctx).enumMembers.add(((EnumContext)_localctx).enumMember);
			}
			setState(309);
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
		enterRule(_localctx, 40, RULE_enumMember);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			((EnumMemberContext)_localctx).name = match(ID);
			setState(314);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__15) {
				{
				setState(312);
				match(T__15);
				setState(313);
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
	public static class GuardingStatementContext extends ParserRuleContext {
		public GuardingStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_guardingStatement; }
	 
		public GuardingStatementContext() { }
		public void copyFrom(GuardingStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SliceContext extends GuardingStatementContext {
		public SliceContext(GuardingStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitSlice(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TransitionContext extends GuardingStatementContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TransitionContext(GuardingStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitTransition(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WaitContext extends GuardingStatementContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public WaitContext(GuardingStatementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitWait(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GuardingStatementContext guardingStatement() throws RecognitionException {
		GuardingStatementContext _localctx = new GuardingStatementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_guardingStatement);
		try {
			setState(330);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__22:
				_localctx = new WaitContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(316);
				match(T__22);
				setState(317);
				match(T__18);
				setState(318);
				expression(0);
				setState(319);
				match(T__19);
				setState(320);
				match(T__6);
				}
				break;
			case T__23:
				_localctx = new SliceContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(322);
				match(T__23);
				setState(323);
				match(T__6);
				}
				break;
			case T__24:
				_localctx = new TransitionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(324);
				match(T__24);
				setState(325);
				match(T__18);
				setState(326);
				expression(0);
				setState(327);
				match(T__19);
				setState(328);
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
		enterRule(_localctx, 44, RULE_statement);
		try {
			setState(345);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__6:
				_localctx = new EmptyStContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(332);
				match(T__6);
				}
				break;
			case T__3:
				_localctx = new CompoundStContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(333);
				compoundStatement();
				}
				break;
			case T__30:
				_localctx = new StartProcessStContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(334);
				startProcStat();
				}
				break;
			case T__31:
				_localctx = new StopProcessStContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(335);
				stopProcStat();
				}
				break;
			case T__32:
				_localctx = new ErrorProcessStContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(336);
				errorProcStat();
				}
				break;
			case T__33:
				_localctx = new RestartStContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(337);
				restartStat();
				}
				break;
			case T__34:
				_localctx = new ResetStContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(338);
				resetStat();
				}
				break;
			case T__36:
				_localctx = new SetStateStContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(339);
				setStateStat();
				}
				break;
			case T__25:
				_localctx = new IfElseStContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(340);
				ifElseStat();
				}
				break;
			case T__27:
				_localctx = new SwitchStContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(341);
				switchStat();
				}
				break;
			case T__7:
			case T__18:
			case T__39:
			case T__40:
			case T__41:
			case T__42:
			case INFIX_POSTFIX_OP:
			case ID:
			case UNSIGNED_INTEGER:
			case FLOAT:
			case TIME:
			case BOOL_VAL:
				_localctx = new ExprStContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(342);
				expression(0);
				setState(343);
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
		enterRule(_localctx, 46, RULE_statementSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(350);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16697021432208L) != 0) || ((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 4362083329L) != 0)) {
				{
				{
				setState(347);
				((StatementSeqContext)_localctx).statement = statement();
				((StatementSeqContext)_localctx).statements.add(((StatementSeqContext)_localctx).statement);
				}
				}
				setState(352);
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
		enterRule(_localctx, 48, RULE_compoundStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			match(T__3);
			setState(357);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16697021432208L) != 0) || ((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 4362083329L) != 0)) {
				{
				{
				setState(354);
				((CompoundStatementContext)_localctx).statement = statement();
				((CompoundStatementContext)_localctx).statements.add(((CompoundStatementContext)_localctx).statement);
				}
				}
				setState(359);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(360);
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
		enterRule(_localctx, 50, RULE_ifElseStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(362);
			match(T__25);
			setState(363);
			match(T__18);
			setState(364);
			((IfElseStatContext)_localctx).cond = expression(0);
			setState(365);
			match(T__19);
			setState(366);
			((IfElseStatContext)_localctx).then = statement();
			setState(369);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(367);
				match(T__26);
				setState(368);
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
		enterRule(_localctx, 52, RULE_switchStat);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			match(T__27);
			setState(372);
			match(T__18);
			setState(373);
			((SwitchStatContext)_localctx).expr = expression(0);
			setState(374);
			match(T__19);
			setState(375);
			match(T__3);
			setState(379);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__28) {
				{
				{
				setState(376);
				((SwitchStatContext)_localctx).caseStat = caseStat();
				((SwitchStatContext)_localctx).options.add(((SwitchStatContext)_localctx).caseStat);
				}
				}
				setState(381);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(383);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__29) {
				{
				setState(382);
				((SwitchStatContext)_localctx).defaultOption = defaultStat();
				}
			}

			setState(385);
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
		enterRule(_localctx, 54, RULE_caseStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(387);
			match(T__28);
			setState(388);
			((CaseStatContext)_localctx).option = expression(0);
			setState(389);
			match(T__10);
			setState(395);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(390);
				match(T__3);
				setState(391);
				switchOptionStatSeq();
				setState(392);
				match(T__4);
				}
				break;
			case 2:
				{
				setState(394);
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
		enterRule(_localctx, 56, RULE_defaultStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397);
			match(T__29);
			setState(398);
			match(T__10);
			setState(399);
			match(T__3);
			setState(400);
			switchOptionStatSeq();
			setState(401);
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
		enterRule(_localctx, 58, RULE_switchOptionStatSeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16697021432208L) != 0) || ((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 4362083329L) != 0)) {
				{
				{
				setState(403);
				((SwitchOptionStatSeqContext)_localctx).statement = statement();
				((SwitchOptionStatSeqContext)_localctx).statements.add(((SwitchOptionStatSeqContext)_localctx).statement);
				}
				}
				setState(408);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(410);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BREAK) {
				{
				setState(409);
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
		enterRule(_localctx, 60, RULE_startProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412);
			match(T__30);
			setState(413);
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
		enterRule(_localctx, 62, RULE_stopProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
			match(T__31);
			setState(417);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(416);
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
		enterRule(_localctx, 64, RULE_errorProcStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			match(T__32);
			setState(421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(420);
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
		enterRule(_localctx, 66, RULE_restartStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
			match(T__33);
			setState(424);
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
		enterRule(_localctx, 68, RULE_resetStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(426);
			match(T__34);
			setState(427);
			match(T__35);
			setState(428);
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
		enterRule(_localctx, 70, RULE_setStateStat);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			match(T__36);
			setState(435);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__37:
				{
				setState(431);
				match(T__37);
				setState(432);
				match(T__8);
				}
				break;
			case T__8:
				{
				setState(433);
				match(T__8);
				setState(434);
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
		enterRule(_localctx, 72, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
			((FunctionCallContext)_localctx).functionID = match(ID);
			setState(438);
			match(T__18);
			{
			setState(439);
			((FunctionCallContext)_localctx).expression = expression(0);
			((FunctionCallContext)_localctx).args.add(((FunctionCallContext)_localctx).expression);
			setState(444);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13) {
				{
				{
				setState(440);
				match(T__13);
				setState(441);
				((FunctionCallContext)_localctx).expression = expression(0);
				((FunctionCallContext)_localctx).args.add(((FunctionCallContext)_localctx).expression);
				}
				}
				setState(446);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			setState(447);
			match(T__19);
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
		public Token stateId;
		public TerminalNode ID() { return getToken(ReflexParser.ID, 0); }
		public TerminalNode STATE_QUAL() { return getToken(ReflexParser.STATE_QUAL, 0); }
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
		enterRule(_localctx, 74, RULE_checkStateExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			match(T__7);
			setState(450);
			((CheckStateExpressionContext)_localctx).processId = match(ID);
			setState(451);
			match(T__38);
			setState(452);
			match(T__8);
			setState(453);
			((CheckStateExpressionContext)_localctx).stateId = match(STATE_QUAL);
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
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode INFIX_POSTFIX_OP() { return getToken(ReflexParser.INFIX_POSTFIX_OP, 0); }
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
		enterRule(_localctx, 76, RULE_infixOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			((InfixOpContext)_localctx).op = match(INFIX_POSTFIX_OP);
			setState(456);
			variable();
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
		public Token op;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
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
		enterRule(_localctx, 78, RULE_postfixOp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(458);
			variable();
			setState(459);
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
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public IdContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntegerValContext extends PrimaryExpressionContext {
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public IntegerValContext(PrimaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitIntegerVal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_primaryExpression);
		try {
			setState(470);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new IdContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(461);
				variable();
				}
				break;
			case T__39:
			case T__40:
			case UNSIGNED_INTEGER:
				_localctx = new IntegerValContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(462);
				integer();
				}
				break;
			case FLOAT:
				_localctx = new FloatContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(463);
				match(FLOAT);
				}
				break;
			case BOOL_VAL:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(464);
				match(BOOL_VAL);
				}
				break;
			case TIME:
				_localctx = new TimeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(465);
				match(TIME);
				}
				break;
			case T__18:
				_localctx = new ClosedExpressionContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(466);
				match(T__18);
				setState(467);
				expression(0);
				setState(468);
				match(T__19);
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
		enterRule(_localctx, 82, RULE_unaryExpression);
		try {
			setState(479);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				_localctx = new PrimaryExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(472);
				primaryExpression();
				}
				break;
			case 2:
				_localctx = new FuncCallExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(473);
				functionCall();
				}
				break;
			case 3:
				_localctx = new PostfixOpExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(474);
				postfixOp();
				}
				break;
			case 4:
				_localctx = new InfixOpExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(475);
				infixOp();
				}
				break;
			case 5:
				_localctx = new UnaryOpExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(476);
				((UnaryOpExprContext)_localctx).op = unaryOp();
				setState(477);
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
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
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
		int _startState = 84;
		enterRecursionRule(_localctx, 84, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(493);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				_localctx = new UnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(482);
				unaryExpression();
				}
				break;
			case 2:
				{
				_localctx = new CheckStateContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(483);
				checkStateExpression();
				}
				break;
			case 3:
				{
				_localctx = new CastContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(484);
				match(T__18);
				setState(485);
				((CastContext)_localctx).varType = type();
				setState(486);
				match(T__19);
				setState(487);
				expression(12);
				}
				break;
			case 4:
				{
				_localctx = new AssignContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(489);
				variable();
				setState(490);
				assignOp();
				setState(491);
				expression(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(528);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(526);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
					case 1:
						{
						_localctx = new MulContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(495);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(496);
						((MulContext)_localctx).op = match(MUL_OP);
						setState(497);
						expression(12);
						}
						break;
					case 2:
						{
						_localctx = new AddContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(498);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(499);
						((AddContext)_localctx).op = addOp();
						setState(500);
						expression(11);
						}
						break;
					case 3:
						{
						_localctx = new ShiftContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(502);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(503);
						((ShiftContext)_localctx).op = match(SHIFT_OP);
						setState(504);
						expression(10);
						}
						break;
					case 4:
						{
						_localctx = new CompareContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(505);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(506);
						((CompareContext)_localctx).op = match(COMP_OP);
						setState(507);
						expression(9);
						}
						break;
					case 5:
						{
						_localctx = new EqualContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(508);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(509);
						((EqualContext)_localctx).op = match(EQ_OP);
						setState(510);
						expression(8);
						}
						break;
					case 6:
						{
						_localctx = new BitAndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(511);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(512);
						match(BIT_AND_OP);
						setState(513);
						expression(7);
						}
						break;
					case 7:
						{
						_localctx = new BitXorContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(514);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(515);
						match(BIT_XOR_OP);
						setState(516);
						expression(6);
						}
						break;
					case 8:
						{
						_localctx = new BitOrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(517);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(518);
						match(BIT_OR_OP);
						setState(519);
						expression(5);
						}
						break;
					case 9:
						{
						_localctx = new AndContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(520);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(521);
						match(AND_OP);
						setState(522);
						expression(4);
						}
						break;
					case 10:
						{
						_localctx = new OrContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(523);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(524);
						match(OR_OP);
						setState(525);
						expression(3);
						}
						break;
					}
					} 
				}
				setState(530);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
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
	public static class VariableContext extends ParserRuleContext {
		public Token varId;
		public ExpressionContext idx;
		public Token fieldId;
		public List<TerminalNode> ID() { return getTokens(ReflexParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ReflexParser.ID, i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_variable);
		try {
			setState(541);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(531);
				((VariableContext)_localctx).varId = match(ID);
				setState(536);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(532);
					match(T__0);
					setState(533);
					((VariableContext)_localctx).idx = expression(0);
					setState(534);
					match(T__1);
					}
					break;
				}
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(538);
				((VariableContext)_localctx).varId = match(ID);
				setState(539);
				match(T__11);
				setState(540);
				((VariableContext)_localctx).fieldId = match(ID);
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
		enterRule(_localctx, 88, RULE_unaryOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 16492674416640L) != 0)) ) {
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
		enterRule(_localctx, 90, RULE_addOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(545);
			_la = _input.LA(1);
			if ( !(_la==T__39 || _la==T__40) ) {
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
		enterRule(_localctx, 92, RULE_assignOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17996806323503104L) != 0)) ) {
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
		enterRule(_localctx, 94, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549);
			_la = _input.LA(1);
			if ( !(((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 8191L) != 0)) ) {
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
	public static class IntegerContext extends ParserRuleContext {
		public Token sign;
		public TerminalNode UNSIGNED_INTEGER() { return getToken(ReflexParser.UNSIGNED_INTEGER, 0); }
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ReflexVisitor ) return ((ReflexVisitor<? extends T>)visitor).visitInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_integer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(553);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__39:
				{
				setState(551);
				((IntegerContext)_localctx).sign = match(T__39);
				}
				break;
			case T__40:
				{
				setState(552);
				((IntegerContext)_localctx).sign = match(T__40);
				}
				break;
			case UNSIGNED_INTEGER:
				break;
			default:
				break;
			}
			setState(555);
			match(UNSIGNED_INTEGER);
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
		case 42:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 11);
		case 1:
			return precpred(_ctx, 10);
		case 2:
			return precpred(_ctx, 9);
		case 3:
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 6);
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
		case 8:
			return precpred(_ctx, 3);
		case 9:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001h\u022e\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0005\u0000g\b\u0000\n\u0000\f\u0000j\t\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000"+
		"w\b\u0000\n\u0000\f\u0000z\t\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u0082\b\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005\u0002"+
		"\u008a\b\u0002\n\u0002\f\u0002\u008d\t\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u0094\b\u0002\u0001\u0002\u0001"+
		"\u0002\u0005\u0002\u0098\b\u0002\n\u0002\f\u0002\u009b\t\u0002\u0001\u0002"+
		"\u0005\u0002\u009e\b\u0002\n\u0002\f\u0002\u00a1\t\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u00a9"+
		"\b\u0003\n\u0003\f\u0003\u00ac\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003\u00b1\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003"+
		"\u00b6\b\u0003\n\u0003\f\u0003\u00b9\t\u0003\u0001\u0003\u0003\u0003\u00bc"+
		"\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u00c8"+
		"\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00ce"+
		"\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u00d4"+
		"\b\u0007\n\u0007\f\u0007\u00d7\t\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0003\b\u00df\b\b\u0001\b\u0003\b\u00e2\b"+
		"\b\u0001\t\u0001\t\u0003\t\u00e6\b\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00f2"+
		"\b\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u00fa\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0004\r\u0100\b\r\u000b\r"+
		"\f\r\u0101\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0003\u000e\u010c\b\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0113\b\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u011a\b\u0010"+
		"\n\u0010\f\u0010\u011d\t\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u013b\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u014b"+
		"\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u015a\b\u0016\u0001\u0017\u0005\u0017\u015d"+
		"\b\u0017\n\u0017\f\u0017\u0160\t\u0017\u0001\u0018\u0001\u0018\u0005\u0018"+
		"\u0164\b\u0018\n\u0018\f\u0018\u0167\t\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0003\u0019\u0172\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u017a\b\u001a\n\u001a\f\u001a"+
		"\u017d\t\u001a\u0001\u001a\u0003\u001a\u0180\b\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u018c\b\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0005"+
		"\u001d\u0195\b\u001d\n\u001d\f\u001d\u0198\t\u001d\u0001\u001d\u0003\u001d"+
		"\u019b\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f"+
		"\u0003\u001f\u01a2\b\u001f\u0001 \u0001 \u0003 \u01a6\b \u0001!\u0001"+
		"!\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0003#\u01b4\b#\u0001$\u0001$\u0001$\u0001$\u0001$\u0005$\u01bb\b$\n"+
		"$\f$\u01be\t$\u0001$\u0001$\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%"+
		"\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u01d7\b(\u0001)\u0001)\u0001"+
		")\u0001)\u0001)\u0001)\u0001)\u0003)\u01e0\b)\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0003*\u01ee"+
		"\b*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001*\u0005*\u020f\b*\n*\f*\u0212\t*\u0001+\u0001+\u0001+\u0001+\u0001"+
		"+\u0003+\u0219\b+\u0001+\u0001+\u0001+\u0003+\u021e\b+\u0001,\u0001,\u0001"+
		"-\u0001-\u0001.\u0001.\u0001/\u0001/\u00010\u00010\u00030\u022a\b0\u0001"+
		"0\u00010\u00010\u0000\u0001T1\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"+
		"TVXZ\\^`\u0000\u0004\u0001\u0000(+\u0001\u0000()\u0002\u0000\u0010\u0010"+
		",5\u0001\u00006B\u024d\u0000h\u0001\u0000\u0000\u0000\u0002~\u0001\u0000"+
		"\u0000\u0000\u0004\u008b\u0001\u0000\u0000\u0000\u0006\u00aa\u0001\u0000"+
		"\u0000\u0000\b\u00bf\u0001\u0000\u0000\u0000\n\u00c7\u0001\u0000\u0000"+
		"\u0000\f\u00cd\u0001\u0000\u0000\u0000\u000e\u00cf\u0001\u0000\u0000\u0000"+
		"\u0010\u00de\u0001\u0000\u0000\u0000\u0012\u00e5\u0001\u0000\u0000\u0000"+
		"\u0014\u00e9\u0001\u0000\u0000\u0000\u0016\u00ee\u0001\u0000\u0000\u0000"+
		"\u0018\u00f5\u0001\u0000\u0000\u0000\u001a\u00fb\u0001\u0000\u0000\u0000"+
		"\u001c\u0105\u0001\u0000\u0000\u0000\u001e\u0112\u0001\u0000\u0000\u0000"+
		" \u0114\u0001\u0000\u0000\u0000\"\u0120\u0001\u0000\u0000\u0000$\u0127"+
		"\u0001\u0000\u0000\u0000&\u012e\u0001\u0000\u0000\u0000(\u0137\u0001\u0000"+
		"\u0000\u0000*\u014a\u0001\u0000\u0000\u0000,\u0159\u0001\u0000\u0000\u0000"+
		".\u015e\u0001\u0000\u0000\u00000\u0161\u0001\u0000\u0000\u00002\u016a"+
		"\u0001\u0000\u0000\u00004\u0173\u0001\u0000\u0000\u00006\u0183\u0001\u0000"+
		"\u0000\u00008\u018d\u0001\u0000\u0000\u0000:\u0196\u0001\u0000\u0000\u0000"+
		"<\u019c\u0001\u0000\u0000\u0000>\u019f\u0001\u0000\u0000\u0000@\u01a3"+
		"\u0001\u0000\u0000\u0000B\u01a7\u0001\u0000\u0000\u0000D\u01aa\u0001\u0000"+
		"\u0000\u0000F\u01ae\u0001\u0000\u0000\u0000H\u01b5\u0001\u0000\u0000\u0000"+
		"J\u01c1\u0001\u0000\u0000\u0000L\u01c7\u0001\u0000\u0000\u0000N\u01ca"+
		"\u0001\u0000\u0000\u0000P\u01d6\u0001\u0000\u0000\u0000R\u01df\u0001\u0000"+
		"\u0000\u0000T\u01ed\u0001\u0000\u0000\u0000V\u021d\u0001\u0000\u0000\u0000"+
		"X\u021f\u0001\u0000\u0000\u0000Z\u0221\u0001\u0000\u0000\u0000\\\u0223"+
		"\u0001\u0000\u0000\u0000^\u0225\u0001\u0000\u0000\u0000`\u0229\u0001\u0000"+
		"\u0000\u0000bc\u0005\u0001\u0000\u0000cd\u0003\n\u0005\u0000de\u0005\u0002"+
		"\u0000\u0000eg\u0001\u0000\u0000\u0000fb\u0001\u0000\u0000\u0000gj\u0001"+
		"\u0000\u0000\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000"+
		"ik\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000kl\u0005\u0003\u0000"+
		"\u0000lm\u0005P\u0000\u0000mn\u0005\u0004\u0000\u0000nx\u0003\u0002\u0001"+
		"\u0000ow\u0003$\u0012\u0000pw\u0003&\u0013\u0000qw\u0003 \u0010\u0000"+
		"rw\u0003\u0012\t\u0000sw\u0003\"\u0011\u0000tw\u0003\u0004\u0002\u0000"+
		"uw\u0003\u001a\r\u0000vo\u0001\u0000\u0000\u0000vp\u0001\u0000\u0000\u0000"+
		"vq\u0001\u0000\u0000\u0000vr\u0001\u0000\u0000\u0000vs\u0001\u0000\u0000"+
		"\u0000vt\u0001\u0000\u0000\u0000vu\u0001\u0000\u0000\u0000wz\u0001\u0000"+
		"\u0000\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y{\u0001"+
		"\u0000\u0000\u0000zx\u0001\u0000\u0000\u0000{|\u0005\u0005\u0000\u0000"+
		"|}\u0005\u0000\u0000\u0001}\u0001\u0001\u0000\u0000\u0000~\u0081\u0005"+
		"\u0006\u0000\u0000\u007f\u0082\u0005Q\u0000\u0000\u0080\u0082\u0005`\u0000"+
		"\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0080\u0001\u0000\u0000"+
		"\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0084\u0005\u0007\u0000"+
		"\u0000\u0084\u0003\u0001\u0000\u0000\u0000\u0085\u0086\u0005\u0001\u0000"+
		"\u0000\u0086\u0087\u0003\n\u0005\u0000\u0087\u0088\u0005\u0002\u0000\u0000"+
		"\u0088\u008a\u0001\u0000\u0000\u0000\u0089\u0085\u0001\u0000\u0000\u0000"+
		"\u008a\u008d\u0001\u0000\u0000\u0000\u008b\u0089\u0001\u0000\u0000\u0000"+
		"\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008e\u0001\u0000\u0000\u0000"+
		"\u008d\u008b\u0001\u0000\u0000\u0000\u008e\u008f\u0005\b\u0000\u0000\u008f"+
		"\u0090\u0005P\u0000\u0000\u0090\u0099\u0005\u0004\u0000\u0000\u0091\u0094"+
		"\u0003\u000e\u0007\u0000\u0092\u0094\u0003\u0010\b\u0000\u0093\u0091\u0001"+
		"\u0000\u0000\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0094\u0095\u0001"+
		"\u0000\u0000\u0000\u0095\u0096\u0005\u0007\u0000\u0000\u0096\u0098\u0001"+
		"\u0000\u0000\u0000\u0097\u0093\u0001\u0000\u0000\u0000\u0098\u009b\u0001"+
		"\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u0099\u009a\u0001"+
		"\u0000\u0000\u0000\u009a\u009f\u0001\u0000\u0000\u0000\u009b\u0099\u0001"+
		"\u0000\u0000\u0000\u009c\u009e\u0003\u0006\u0003\u0000\u009d\u009c\u0001"+
		"\u0000\u0000\u0000\u009e\u00a1\u0001\u0000\u0000\u0000\u009f\u009d\u0001"+
		"\u0000\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a2\u0001"+
		"\u0000\u0000\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005"+
		"\u0005\u0000\u0000\u00a3\u0005\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005"+
		"\u0001\u0000\u0000\u00a5\u00a6\u0003\n\u0005\u0000\u00a6\u00a7\u0005\u0002"+
		"\u0000\u0000\u00a7\u00a9\u0001\u0000\u0000\u0000\u00a8\u00a4\u0001\u0000"+
		"\u0000\u0000\u00a9\u00ac\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000"+
		"\u0000\u0000\u00aa\u00ab\u0001\u0000\u0000\u0000\u00ab\u00ad\u0001\u0000"+
		"\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ad\u00ae\u0005\t\u0000"+
		"\u0000\u00ae\u00b0\u0005P\u0000\u0000\u00af\u00b1\u0005\n\u0000\u0000"+
		"\u00b0\u00af\u0001\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000"+
		"\u00b1\u00b2\u0001\u0000\u0000\u0000\u00b2\u00b3\u0005\u0004\u0000\u0000"+
		"\u00b3\u00b7\u0003.\u0017\u0000\u00b4\u00b6\u0003\b\u0004\u0000\u00b5"+
		"\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b9\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8"+
		"\u00bb\u0001\u0000\u0000\u0000\u00b9\u00b7\u0001\u0000\u0000\u0000\u00ba"+
		"\u00bc\u0003\u001c\u000e\u0000\u00bb\u00ba\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bc\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd"+
		"\u00be\u0005\u0005\u0000\u0000\u00be\u0007\u0001\u0000\u0000\u0000\u00bf"+
		"\u00c0\u0003*\u0015\u0000\u00c0\u00c1\u0003.\u0017\u0000\u00c1\t\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c3\u0003\f\u0006\u0000\u00c3\u00c4\u0005\u000b"+
		"\u0000\u0000\u00c4\u00c5\u0005g\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000"+
		"\u0000\u00c6\u00c8\u0003\f\u0006\u0000\u00c7\u00c2\u0001\u0000\u0000\u0000"+
		"\u00c7\u00c6\u0001\u0000\u0000\u0000\u00c8\u000b\u0001\u0000\u0000\u0000"+
		"\u00c9\u00ca\u0005P\u0000\u0000\u00ca\u00cb\u0005\f\u0000\u0000\u00cb"+
		"\u00ce\u0005P\u0000\u0000\u00cc\u00ce\u0005P\u0000\u0000\u00cd\u00c9\u0001"+
		"\u0000\u0000\u0000\u00cd\u00cc\u0001\u0000\u0000\u0000\u00ce\r\u0001\u0000"+
		"\u0000\u0000\u00cf\u00d0\u0005\r\u0000\u0000\u00d0\u00d5\u0005P\u0000"+
		"\u0000\u00d1\u00d2\u0005\u000e\u0000\u0000\u00d2\u00d4\u0005P\u0000\u0000"+
		"\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000\u0000"+
		"\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000"+
		"\u00d6\u00d8\u0001\u0000\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d8\u00d9\u0005\u000f\u0000\u0000\u00d9\u00da\u0005\b\u0000\u0000\u00da"+
		"\u00db\u0005P\u0000\u0000\u00db\u000f\u0001\u0000\u0000\u0000\u00dc\u00df"+
		"\u0003\u0014\n\u0000\u00dd\u00df\u0003\u0018\f\u0000\u00de\u00dc\u0001"+
		"\u0000\u0000\u0000\u00de\u00dd\u0001\u0000\u0000\u0000\u00df\u00e1\u0001"+
		"\u0000\u0000\u0000\u00e0\u00e2\u0005\r\u0000\u0000\u00e1\u00e0\u0001\u0000"+
		"\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u0011\u0001\u0000"+
		"\u0000\u0000\u00e3\u00e6\u0003\u0014\n\u0000\u00e4\u00e6\u0003\u0018\f"+
		"\u0000\u00e5\u00e3\u0001\u0000\u0000\u0000\u00e5\u00e4\u0001\u0000\u0000"+
		"\u0000\u00e6\u00e7\u0001\u0000\u0000\u0000\u00e7\u00e8\u0005\u0007\u0000"+
		"\u0000\u00e8\u0013\u0001\u0000\u0000\u0000\u00e9\u00ea\u0003^/\u0000\u00ea"+
		"\u00eb\u0005P\u0000\u0000\u00eb\u00ec\u0005\u0010\u0000\u0000\u00ec\u00ed"+
		"\u0003\u0016\u000b\u0000\u00ed\u0015\u0001\u0000\u0000\u0000\u00ee\u00ef"+
		"\u0005P\u0000\u0000\u00ef\u00f1\u0005\u0001\u0000\u0000\u00f0\u00f2\u0005"+
		"Q\u0000\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000"+
		"\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f4\u0005\u0002"+
		"\u0000\u0000\u00f4\u0017\u0001\u0000\u0000\u0000\u00f5\u00f6\u0003^/\u0000"+
		"\u00f6\u00f9\u0005P\u0000\u0000\u00f7\u00f8\u0005\u0010\u0000\u0000\u00f8"+
		"\u00fa\u0003T*\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001"+
		"\u0000\u0000\u0000\u00fa\u0019\u0001\u0000\u0000\u0000\u00fb\u00fc\u0005"+
		"\u0011\u0000\u0000\u00fc\u00fd\u0005P\u0000\u0000\u00fd\u00ff\u0005\u0004"+
		"\u0000\u0000\u00fe\u0100\u0003\u0018\f\u0000\u00ff\u00fe\u0001\u0000\u0000"+
		"\u0000\u0100\u0101\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000"+
		"\u0000\u0101\u0102\u0001\u0000\u0000\u0000\u0102\u0103\u0001\u0000\u0000"+
		"\u0000\u0103\u0104\u0005\u0005\u0000\u0000\u0104\u001b\u0001\u0000\u0000"+
		"\u0000\u0105\u010b\u0005\u0012\u0000\u0000\u0106\u010c\u0003\u001e\u000f"+
		"\u0000\u0107\u0108\u0005\u0013\u0000\u0000\u0108\u0109\u0003\u001e\u000f"+
		"\u0000\u0109\u010a\u0005\u0014\u0000\u0000\u010a\u010c\u0001\u0000\u0000"+
		"\u0000\u010b\u0106\u0001\u0000\u0000\u0000\u010b\u0107\u0001\u0000\u0000"+
		"\u0000\u010c\u010d\u0001\u0000\u0000\u0000\u010d\u010e\u0003,\u0016\u0000"+
		"\u010e\u001d\u0001\u0000\u0000\u0000\u010f\u0113\u0005`\u0000\u0000\u0110"+
		"\u0113\u0005Q\u0000\u0000\u0111\u0113\u0005P\u0000\u0000\u0112\u010f\u0001"+
		"\u0000\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0112\u0111\u0001"+
		"\u0000\u0000\u0000\u0113\u001f\u0001\u0000\u0000\u0000\u0114\u0115\u0003"+
		"^/\u0000\u0115\u0116\u0005\u0013\u0000\u0000\u0116\u011b\u0003^/\u0000"+
		"\u0117\u0118\u0005\u000e\u0000\u0000\u0118\u011a\u0003^/\u0000\u0119\u0117"+
		"\u0001\u0000\u0000\u0000\u011a\u011d\u0001\u0000\u0000\u0000\u011b\u0119"+
		"\u0001\u0000\u0000\u0000\u011b\u011c\u0001\u0000\u0000\u0000\u011c\u011e"+
		"\u0001\u0000\u0000\u0000\u011d\u011b\u0001\u0000\u0000\u0000\u011e\u011f"+
		"\u0005\u0014\u0000\u0000\u011f!\u0001\u0000\u0000\u0000\u0120\u0121\u0005"+
		"C\u0000\u0000\u0121\u0122\u0005P\u0000\u0000\u0122\u0123\u0005Q\u0000"+
		"\u0000\u0123\u0124\u0005Q\u0000\u0000\u0124\u0125\u0005Q\u0000\u0000\u0125"+
		"\u0126\u0005\u0007\u0000\u0000\u0126#\u0001\u0000\u0000\u0000\u0127\u0128"+
		"\u0005\u0015\u0000\u0000\u0128\u0129\u0003^/\u0000\u0129\u012a\u0005P"+
		"\u0000\u0000\u012a\u012b\u0005\u0010\u0000\u0000\u012b\u012c\u0003T*\u0000"+
		"\u012c\u012d\u0005\u0007\u0000\u0000\u012d%\u0001\u0000\u0000\u0000\u012e"+
		"\u012f\u0005\u0016\u0000\u0000\u012f\u0130\u0005P\u0000\u0000\u0130\u0131"+
		"\u0005\u0004\u0000\u0000\u0131\u0132\u0003(\u0014\u0000\u0132\u0133\u0005"+
		"\u000e\u0000\u0000\u0133\u0134\u0003(\u0014\u0000\u0134\u0135\u0001\u0000"+
		"\u0000\u0000\u0135\u0136\u0005\u0005\u0000\u0000\u0136\'\u0001\u0000\u0000"+
		"\u0000\u0137\u013a\u0005P\u0000\u0000\u0138\u0139\u0005\u0010\u0000\u0000"+
		"\u0139\u013b\u0003T*\u0000\u013a\u0138\u0001\u0000\u0000\u0000\u013a\u013b"+
		"\u0001\u0000\u0000\u0000\u013b)\u0001\u0000\u0000\u0000\u013c\u013d\u0005"+
		"\u0017\u0000\u0000\u013d\u013e\u0005\u0013\u0000\u0000\u013e\u013f\u0003"+
		"T*\u0000\u013f\u0140\u0005\u0014\u0000\u0000\u0140\u0141\u0005\u0007\u0000"+
		"\u0000\u0141\u014b\u0001\u0000\u0000\u0000\u0142\u0143\u0005\u0018\u0000"+
		"\u0000\u0143\u014b\u0005\u0007\u0000\u0000\u0144\u0145\u0005\u0019\u0000"+
		"\u0000\u0145\u0146\u0005\u0013\u0000\u0000\u0146\u0147\u0003T*\u0000\u0147"+
		"\u0148\u0005\u0014\u0000\u0000\u0148\u0149\u0005\u0007\u0000\u0000\u0149"+
		"\u014b\u0001\u0000\u0000\u0000\u014a\u013c\u0001\u0000\u0000\u0000\u014a"+
		"\u0142\u0001\u0000\u0000\u0000\u014a\u0144\u0001\u0000\u0000\u0000\u014b"+
		"+\u0001\u0000\u0000\u0000\u014c\u015a\u0005\u0007\u0000\u0000\u014d\u015a"+
		"\u00030\u0018\u0000\u014e\u015a\u0003<\u001e\u0000\u014f\u015a\u0003>"+
		"\u001f\u0000\u0150\u015a\u0003@ \u0000\u0151\u015a\u0003B!\u0000\u0152"+
		"\u015a\u0003D\"\u0000\u0153\u015a\u0003F#\u0000\u0154\u015a\u00032\u0019"+
		"\u0000\u0155\u015a\u00034\u001a\u0000\u0156\u0157\u0003T*\u0000\u0157"+
		"\u0158\u0005\u0007\u0000\u0000\u0158\u015a\u0001\u0000\u0000\u0000\u0159"+
		"\u014c\u0001\u0000\u0000\u0000\u0159\u014d\u0001\u0000\u0000\u0000\u0159"+
		"\u014e\u0001\u0000\u0000\u0000\u0159\u014f\u0001\u0000\u0000\u0000\u0159"+
		"\u0150\u0001\u0000\u0000\u0000\u0159\u0151\u0001\u0000\u0000\u0000\u0159"+
		"\u0152\u0001\u0000\u0000\u0000\u0159\u0153\u0001\u0000\u0000\u0000\u0159"+
		"\u0154\u0001\u0000\u0000\u0000\u0159\u0155\u0001\u0000\u0000\u0000\u0159"+
		"\u0156\u0001\u0000\u0000\u0000\u015a-\u0001\u0000\u0000\u0000\u015b\u015d"+
		"\u0003,\u0016\u0000\u015c\u015b\u0001\u0000\u0000\u0000\u015d\u0160\u0001"+
		"\u0000\u0000\u0000\u015e\u015c\u0001\u0000\u0000\u0000\u015e\u015f\u0001"+
		"\u0000\u0000\u0000\u015f/\u0001\u0000\u0000\u0000\u0160\u015e\u0001\u0000"+
		"\u0000\u0000\u0161\u0165\u0005\u0004\u0000\u0000\u0162\u0164\u0003,\u0016"+
		"\u0000\u0163\u0162\u0001\u0000\u0000\u0000\u0164\u0167\u0001\u0000\u0000"+
		"\u0000\u0165\u0163\u0001\u0000\u0000\u0000\u0165\u0166\u0001\u0000\u0000"+
		"\u0000\u0166\u0168\u0001\u0000\u0000\u0000\u0167\u0165\u0001\u0000\u0000"+
		"\u0000\u0168\u0169\u0005\u0005\u0000\u0000\u01691\u0001\u0000\u0000\u0000"+
		"\u016a\u016b\u0005\u001a\u0000\u0000\u016b\u016c\u0005\u0013\u0000\u0000"+
		"\u016c\u016d\u0003T*\u0000\u016d\u016e\u0005\u0014\u0000\u0000\u016e\u0171"+
		"\u0003,\u0016\u0000\u016f\u0170\u0005\u001b\u0000\u0000\u0170\u0172\u0003"+
		",\u0016\u0000\u0171\u016f\u0001\u0000\u0000\u0000\u0171\u0172\u0001\u0000"+
		"\u0000\u0000\u01723\u0001\u0000\u0000\u0000\u0173\u0174\u0005\u001c\u0000"+
		"\u0000\u0174\u0175\u0005\u0013\u0000\u0000\u0175\u0176\u0003T*\u0000\u0176"+
		"\u0177\u0005\u0014\u0000\u0000\u0177\u017b\u0005\u0004\u0000\u0000\u0178"+
		"\u017a\u00036\u001b\u0000\u0179\u0178\u0001\u0000\u0000\u0000\u017a\u017d"+
		"\u0001\u0000\u0000\u0000\u017b\u0179\u0001\u0000\u0000\u0000\u017b\u017c"+
		"\u0001\u0000\u0000\u0000\u017c\u017f\u0001\u0000\u0000\u0000\u017d\u017b"+
		"\u0001\u0000\u0000\u0000\u017e\u0180\u00038\u001c\u0000\u017f\u017e\u0001"+
		"\u0000\u0000\u0000\u017f\u0180\u0001\u0000\u0000\u0000\u0180\u0181\u0001"+
		"\u0000\u0000\u0000\u0181\u0182\u0005\u0005\u0000\u0000\u01825\u0001\u0000"+
		"\u0000\u0000\u0183\u0184\u0005\u001d\u0000\u0000\u0184\u0185\u0003T*\u0000"+
		"\u0185\u018b\u0005\u000b\u0000\u0000\u0186\u0187\u0005\u0004\u0000\u0000"+
		"\u0187\u0188\u0003:\u001d\u0000\u0188\u0189\u0005\u0005\u0000\u0000\u0189"+
		"\u018c\u0001\u0000\u0000\u0000\u018a\u018c\u0003:\u001d\u0000\u018b\u0186"+
		"\u0001\u0000\u0000\u0000\u018b\u018a\u0001\u0000\u0000\u0000\u018c7\u0001"+
		"\u0000\u0000\u0000\u018d\u018e\u0005\u001e\u0000\u0000\u018e\u018f\u0005"+
		"\u000b\u0000\u0000\u018f\u0190\u0005\u0004\u0000\u0000\u0190\u0191\u0003"+
		":\u001d\u0000\u0191\u0192\u0005\u0005\u0000\u0000\u01929\u0001\u0000\u0000"+
		"\u0000\u0193\u0195\u0003,\u0016\u0000\u0194\u0193\u0001\u0000\u0000\u0000"+
		"\u0195\u0198\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000\u0000\u0000"+
		"\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u019a\u0001\u0000\u0000\u0000"+
		"\u0198\u0196\u0001\u0000\u0000\u0000\u0199\u019b\u0005D\u0000\u0000\u019a"+
		"\u0199\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000\u0000\u0000\u019b"+
		";\u0001\u0000\u0000\u0000\u019c\u019d\u0005\u001f\u0000\u0000\u019d\u019e"+
		"\u0005P\u0000\u0000\u019e=\u0001\u0000\u0000\u0000\u019f\u01a1\u0005 "+
		"\u0000\u0000\u01a0\u01a2\u0005P\u0000\u0000\u01a1\u01a0\u0001\u0000\u0000"+
		"\u0000\u01a1\u01a2\u0001\u0000\u0000\u0000\u01a2?\u0001\u0000\u0000\u0000"+
		"\u01a3\u01a5\u0005!\u0000\u0000\u01a4\u01a6\u0005P\u0000\u0000\u01a5\u01a4"+
		"\u0001\u0000\u0000\u0000\u01a5\u01a6\u0001\u0000\u0000\u0000\u01a6A\u0001"+
		"\u0000\u0000\u0000\u01a7\u01a8\u0005\"\u0000\u0000\u01a8\u01a9\u0005\u0007"+
		"\u0000\u0000\u01a9C\u0001\u0000\u0000\u0000\u01aa\u01ab\u0005#\u0000\u0000"+
		"\u01ab\u01ac\u0005$\u0000\u0000\u01ac\u01ad\u0005\u0007\u0000\u0000\u01ad"+
		"E\u0001\u0000\u0000\u0000\u01ae\u01b3\u0005%\u0000\u0000\u01af\u01b0\u0005"+
		"&\u0000\u0000\u01b0\u01b4\u0005\t\u0000\u0000\u01b1\u01b2\u0005\t\u0000"+
		"\u0000\u01b2\u01b4\u0005P\u0000\u0000\u01b3\u01af\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b1\u0001\u0000\u0000\u0000\u01b4G\u0001\u0000\u0000\u0000\u01b5"+
		"\u01b6\u0005P\u0000\u0000\u01b6\u01b7\u0005\u0013\u0000\u0000\u01b7\u01bc"+
		"\u0003T*\u0000\u01b8\u01b9\u0005\u000e\u0000\u0000\u01b9\u01bb\u0003T"+
		"*\u0000\u01ba\u01b8\u0001\u0000\u0000\u0000\u01bb\u01be\u0001\u0000\u0000"+
		"\u0000\u01bc\u01ba\u0001\u0000\u0000\u0000\u01bc\u01bd\u0001\u0000\u0000"+
		"\u0000\u01bd\u01bf\u0001\u0000\u0000\u0000\u01be\u01bc\u0001\u0000\u0000"+
		"\u0000\u01bf\u01c0\u0005\u0014\u0000\u0000\u01c0I\u0001\u0000\u0000\u0000"+
		"\u01c1\u01c2\u0005\b\u0000\u0000\u01c2\u01c3\u0005P\u0000\u0000\u01c3"+
		"\u01c4\u0005\'\u0000\u0000\u01c4\u01c5\u0005\t\u0000\u0000\u01c5\u01c6"+
		"\u0005E\u0000\u0000\u01c6K\u0001\u0000\u0000\u0000\u01c7\u01c8\u0005F"+
		"\u0000\u0000\u01c8\u01c9\u0003V+\u0000\u01c9M\u0001\u0000\u0000\u0000"+
		"\u01ca\u01cb\u0003V+\u0000\u01cb\u01cc\u0005F\u0000\u0000\u01ccO\u0001"+
		"\u0000\u0000\u0000\u01cd\u01d7\u0003V+\u0000\u01ce\u01d7\u0003`0\u0000"+
		"\u01cf\u01d7\u0005R\u0000\u0000\u01d0\u01d7\u0005f\u0000\u0000\u01d1\u01d7"+
		"\u0005`\u0000\u0000\u01d2\u01d3\u0005\u0013\u0000\u0000\u01d3\u01d4\u0003"+
		"T*\u0000\u01d4\u01d5\u0005\u0014\u0000\u0000\u01d5\u01d7\u0001\u0000\u0000"+
		"\u0000\u01d6\u01cd\u0001\u0000\u0000\u0000\u01d6\u01ce\u0001\u0000\u0000"+
		"\u0000\u01d6\u01cf\u0001\u0000\u0000\u0000\u01d6\u01d0\u0001\u0000\u0000"+
		"\u0000\u01d6\u01d1\u0001\u0000\u0000\u0000\u01d6\u01d2\u0001\u0000\u0000"+
		"\u0000\u01d7Q\u0001\u0000\u0000\u0000\u01d8\u01e0\u0003P(\u0000\u01d9"+
		"\u01e0\u0003H$\u0000\u01da\u01e0\u0003N\'\u0000\u01db\u01e0\u0003L&\u0000"+
		"\u01dc\u01dd\u0003X,\u0000\u01dd\u01de\u0003T*\u0000\u01de\u01e0\u0001"+
		"\u0000\u0000\u0000\u01df\u01d8\u0001\u0000\u0000\u0000\u01df\u01d9\u0001"+
		"\u0000\u0000\u0000\u01df\u01da\u0001\u0000\u0000\u0000\u01df\u01db\u0001"+
		"\u0000\u0000\u0000\u01df\u01dc\u0001\u0000\u0000\u0000\u01e0S\u0001\u0000"+
		"\u0000\u0000\u01e1\u01e2\u0006*\uffff\uffff\u0000\u01e2\u01ee\u0003R)"+
		"\u0000\u01e3\u01ee\u0003J%\u0000\u01e4\u01e5\u0005\u0013\u0000\u0000\u01e5"+
		"\u01e6\u0003^/\u0000\u01e6\u01e7\u0005\u0014\u0000\u0000\u01e7\u01e8\u0003"+
		"T*\f\u01e8\u01ee\u0001\u0000\u0000\u0000\u01e9\u01ea\u0003V+\u0000\u01ea"+
		"\u01eb\u0003\\.\u0000\u01eb\u01ec\u0003T*\u0001\u01ec\u01ee\u0001\u0000"+
		"\u0000\u0000\u01ed\u01e1\u0001\u0000\u0000\u0000\u01ed\u01e3\u0001\u0000"+
		"\u0000\u0000\u01ed\u01e4\u0001\u0000\u0000\u0000\u01ed\u01e9\u0001\u0000"+
		"\u0000\u0000\u01ee\u0210\u0001\u0000\u0000\u0000\u01ef\u01f0\n\u000b\u0000"+
		"\u0000\u01f0\u01f1\u0005G\u0000\u0000\u01f1\u020f\u0003T*\f\u01f2\u01f3"+
		"\n\n\u0000\u0000\u01f3\u01f4\u0003Z-\u0000\u01f4\u01f5\u0003T*\u000b\u01f5"+
		"\u020f\u0001\u0000\u0000\u0000\u01f6\u01f7\n\t\u0000\u0000\u01f7\u01f8"+
		"\u0005H\u0000\u0000\u01f8\u020f\u0003T*\n\u01f9\u01fa\n\b\u0000\u0000"+
		"\u01fa\u01fb\u0005O\u0000\u0000\u01fb\u020f\u0003T*\t\u01fc\u01fd\n\u0007"+
		"\u0000\u0000\u01fd\u01fe\u0005I\u0000\u0000\u01fe\u020f\u0003T*\b\u01ff"+
		"\u0200\n\u0006\u0000\u0000\u0200\u0201\u0005J\u0000\u0000\u0201\u020f"+
		"\u0003T*\u0007\u0202\u0203\n\u0005\u0000\u0000\u0203\u0204\u0005K\u0000"+
		"\u0000\u0204\u020f\u0003T*\u0006\u0205\u0206\n\u0004\u0000\u0000\u0206"+
		"\u0207\u0005L\u0000\u0000\u0207\u020f\u0003T*\u0005\u0208\u0209\n\u0003"+
		"\u0000\u0000\u0209\u020a\u0005M\u0000\u0000\u020a\u020f\u0003T*\u0004"+
		"\u020b\u020c\n\u0002\u0000\u0000\u020c\u020d\u0005N\u0000\u0000\u020d"+
		"\u020f\u0003T*\u0003\u020e\u01ef\u0001\u0000\u0000\u0000\u020e\u01f2\u0001"+
		"\u0000\u0000\u0000\u020e\u01f6\u0001\u0000\u0000\u0000\u020e\u01f9\u0001"+
		"\u0000\u0000\u0000\u020e\u01fc\u0001\u0000\u0000\u0000\u020e\u01ff\u0001"+
		"\u0000\u0000\u0000\u020e\u0202\u0001\u0000\u0000\u0000\u020e\u0205\u0001"+
		"\u0000\u0000\u0000\u020e\u0208\u0001\u0000\u0000\u0000\u020e\u020b\u0001"+
		"\u0000\u0000\u0000\u020f\u0212\u0001\u0000\u0000\u0000\u0210\u020e\u0001"+
		"\u0000\u0000\u0000\u0210\u0211\u0001\u0000\u0000\u0000\u0211U\u0001\u0000"+
		"\u0000\u0000\u0212\u0210\u0001\u0000\u0000\u0000\u0213\u0218\u0005P\u0000"+
		"\u0000\u0214\u0215\u0005\u0001\u0000\u0000\u0215\u0216\u0003T*\u0000\u0216"+
		"\u0217\u0005\u0002\u0000\u0000\u0217\u0219\u0001\u0000\u0000\u0000\u0218"+
		"\u0214\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000\u0000\u0000\u0219"+
		"\u021e\u0001\u0000\u0000\u0000\u021a\u021b\u0005P\u0000\u0000\u021b\u021c"+
		"\u0005\f\u0000\u0000\u021c\u021e\u0005P\u0000\u0000\u021d\u0213\u0001"+
		"\u0000\u0000\u0000\u021d\u021a\u0001\u0000\u0000\u0000\u021eW\u0001\u0000"+
		"\u0000\u0000\u021f\u0220\u0007\u0000\u0000\u0000\u0220Y\u0001\u0000\u0000"+
		"\u0000\u0221\u0222\u0007\u0001\u0000\u0000\u0222[\u0001\u0000\u0000\u0000"+
		"\u0223\u0224\u0007\u0002\u0000\u0000\u0224]\u0001\u0000\u0000\u0000\u0225"+
		"\u0226\u0007\u0003\u0000\u0000\u0226_\u0001\u0000\u0000\u0000\u0227\u022a"+
		"\u0005(\u0000\u0000\u0228\u022a\u0005)\u0000\u0000\u0229\u0227\u0001\u0000"+
		"\u0000\u0000\u0229\u0228\u0001\u0000\u0000\u0000\u0229\u022a\u0001\u0000"+
		"\u0000\u0000\u022a\u022b\u0001\u0000\u0000\u0000\u022b\u022c\u0005Q\u0000"+
		"\u0000\u022ca\u0001\u0000\u0000\u0000/hvx\u0081\u008b\u0093\u0099\u009f"+
		"\u00aa\u00b0\u00b7\u00bb\u00c7\u00cd\u00d5\u00de\u00e1\u00e5\u00f1\u00f9"+
		"\u0101\u010b\u0112\u011b\u013a\u014a\u0159\u015e\u0165\u0171\u017b\u017f"+
		"\u018b\u0196\u019a\u01a1\u01a5\u01b3\u01bc\u01d6\u01df\u01ed\u020e\u0210"+
		"\u0218\u021d\u0229";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}