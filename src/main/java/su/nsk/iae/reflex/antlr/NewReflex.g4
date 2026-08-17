grammar NewReflex;

/*
 * Reflex, second-generation grammar.
 *
 * Comments are NOT parser rules here: they are emitted on the hidden channel and
 * bound to constructs afterwards by token position (see the annotation front-end).
 * Making them positional syntax, as an earlier draft did, turns any comment in an
 * unanticipated place - mid-expression, between '}' and the next 'state' - into a
 * syntax error, and Reflex-AL annotations must be attachable to any construct.
 */

program:
    'program' name=ID '{'
     clock=clockDefinition
     (consts+=const
     | enums+=enum
     | functions+=functionDecl
     | globalVars+=globalVariable
     | ports+=port
     | processes+=process
     | structures+=structDeclaration
     | imports+=importBlock
     | nodes+=nodeDecl)*
     '}' EOF;

clockDefinition: 'clock' (intValue=UNSIGNED_INTEGER | timeValue=TIME) ';';

process:
    'process' name=ID '::' 'node' node=ID '{'
    ((imports+=importedVariableList | variables+=processVariable) ';')*
    states+=state*
    '}';

state:
    'state' name=ID looped='looped'? '{'
    stateFunction=statementSeq
    (func=timeoutFunction)?
    '}';

importBlock: 'import' name=ID '{' (importElements+=importElement)* '}';
importElement: iVector | iRegister | iBit;
iVector: 'vector' name=ID;
iRegister: 'register' name=ID;
iBit: 'bit' name=ID;

nodeDecl: 'node' name=ID '{'
    clock=clockDefinition
    (consts+=const
    | nodeVars+=globalVariable)*
    '}';

importedVariableList: 'shared' (variables+=ID (',' variables+=ID)*) 'from' 'process' processID=ID;
processVariable: (physicalVariable | programVariable) shared='shared'?;
globalVariable: (physicalVariable | programVariable) ';';
physicalVariable:
    (isDirect=direction)? varType=type name=ID
    'as' '(' 'read' '=' readId=ID
    (',' 'write' '=' writeId=ID)?
    (',' 'config' '=' configId=ID)?
    // Either a named bit from an import block, or a numeric bit position as the
    // legacy grammar and the name-mangling spec (newDirectName(name, pos)) use.
    (',' 'bit' '=' (bitId=ID | bitNum=UNSIGNED_INTEGER))? ')';
direction: 'direct' | 'indirect';
programVariable: varType=type name=ID ('=' expression)?;
structDeclaration: 'struct' name=ID '{' (variables+=programVariable ';')+ '}';
timeoutFunction: 'timeout' (timeAmountOrRef | '(' timeAmountOrRef ')') body=statement;
timeAmountOrRef: time=TIME | intTime=UNSIGNED_INTEGER | ref=ID;
functionDecl: returnType=type '(' (argTypes+=type (',' argTypes+=type)*)? ')';
port: varType=PORT_TYPE name=ID addr1=UNSIGNED_INTEGER addr2=UNSIGNED_INTEGER size=UNSIGNED_INTEGER ';';
const: 'const' varType=type name=ID '=' value=expression ';';
// Trailing '*': the previous version accepted exactly two members.
enum: 'enum' identifier=ID '{' enumMembers+=enumMember (',' enumMembers+=enumMember)* '}';
enumMember: name=ID ('=' value=expression)?;

guardingStatement:
    waitHeader ';'                                                        #Wait
    | 'slice' ';'                                                         #Slice
    | waitHeader 'on' 'timeout' time=timeAmountOrRef body=statement ';'   #WaitOnTimeout
    ;
waitHeader: 'wait' '(' cond=expression ')';

statement:
    ';'                    #EmptySt
    | compoundStatement    #CompoundSt
    | startProcStat        #StartProcessSt
    | stopProcStat         #StopProcessSt
    | errorProcStat        #ErrorProcessSt
    | restartStat          #RestartSt
    | resetStat            #ResetSt
    | setStateStat         #SetStateSt
    | ifElseStat           #IfElseSt
    | switchStat           #SwitchSt
    | expression ';'       #ExprSt
    | guardingStatement    #GuardSt
    | iterationStat        #IterSt
    | ccodeStat            #CCodeSt
    | programVariable ';'  #VariableSt
    ;
statementSeq: statements+=statement*;
compoundStatement: '{' body=statementSeq '}';

iterationStat: 'for' '(' init=initIter ';' cond=expression ';' upd=expression ')' stat=statement;
initIter: initList | expression;
initList: (inits+=programVariable (',' inits+=programVariable)*)?;
ccodeStat: code=CCODE;

ifElseStat: 'if' '(' cond=expression ')' then=statement ('else' else=statement)?;
switchStat: 'switch' '(' expr=expression ')' '{' options+=caseStat* defaultOption=defaultStat? '}';
caseStat: 'case' option=expression ':' switchOptionBody;
defaultStat: 'default' ':' switchOptionBody;
switchOptionBody: '{' switchOptionStatSeq '}' | switchOptionStatSeq;
switchOptionStatSeq: body=statementSeq (break=BREAK ';')?;
startProcStat: 'start' processId=ID;
stopProcStat: 'stop' (processId=ID)?;
errorProcStat: 'error' (processId=ID)?;
restartStat: 'restart' ';';
resetStat: 'reset' 'timer' ';';
setStateStat: 'set' ('next' 'state' | 'state' stateId=ID);
functionCall: functionID=ID '(' (args+=expression (',' args+=expression)*)? ')';

// stateQual is a parser rule, not a lexer token: 'stop' and 'error' also appear as
// literals in stopProcStat/errorProcStat, and implicit literal tokens outrank named
// lexer rules, so a STATE_QUAL token could never match them.
checkStateExpression: 'process' processId=ID 'in' 'state' qual=stateQual;
stateQual: 'active' | 'inactive' | 'stop' | 'error';

infixOp: op=INFIX_POSTFIX_OP variable;
postfixOp: variable op=INFIX_POSTFIX_OP;

primaryExpression:
    variable                #Id
    | integer                #IntegerLit
    | floatVal               #FloatLit
    | BOOL_VAL              #Bool
    | TIME                  #Time
    | '(' expression ')'    #ClosedExpression
    ;
unaryExpression:
    primaryExpression           #PrimaryExpr
    | functionCall              #FuncCallExpr
    | postfixOp                 #PostfixOpExpr
    | infixOp                   #InfixOpExpr
    | op=unaryOp expression     #UnaryOpExpr
    ;

expression:
    unaryExpression                             #Unary
    | checkStateExpression                      #CheckState
    | '(' varType=type ')' expression           #Cast
    | expression op=MUL_OP expression           #Mul
    | expression op=addOp expression            #Add
    | expression op=SHIFT_OP expression         #Shift
    | expression op=COMP_OP expression          #Compare
    | expression op=EQ_OP expression            #Equal
    | expression BIT_AND_OP expression          #BitAnd
    | expression BIT_XOR_OP expression          #BitXor
    | expression BIT_OR_OP expression           #BitOr
    | expression AND_OP expression              #And
    | expression OR_OP expression               #Or
    | variable assignOp expression              #Assign
    ;
variable: varId=ID variableAccess*;
variableAccess: ('.' field=ID) | ('[' index=expression ']');

// Signs are applied at parser level. As lexer tokens they swallow the operator in
// `a + 1` / `a + 1.0`, which then cannot parse as an addition.
integer: (sign='+' | sign='-')? UNSIGNED_INTEGER;
floatVal: (sign='+' | sign='-')? FLOAT;

unaryOp: '+' | '-' | '~' | '!';
addOp: '+' | '-';
assignOp:
    '=' | '*=' | '/=' | '%=' | '+=' | '-='
    | '<<=' | '>>=' | '&=' | '^=' | '|=';
type:
    'void' | 'bool' | 'time'
    | 'float' | 'double'
    | 'int8' | 'uint8'
    | 'int16' | 'uint16'
    | 'int32' | 'uint32'
    | 'int64' | 'uint64'
    ;

// ---------------------------------------------------------------- lexer rules

BREAK: 'break';
PORT_TYPE: 'input' | 'output';
BOOL_VAL: 'true' | 'false';

INFIX_POSTFIX_OP: '++' | '--';
MUL_OP: '*' | '/' | '%';
SHIFT_OP: '<<' | '>>';
EQ_OP: '==' | '!=';
BIT_AND_OP: '&';
BIT_XOR_OP: '^';
BIT_OR_OP: '|';
AND_OP: '&&';
OR_OP: '||';
COMP_OP: '<' | '>' | '<=' | '>=';

ID: [a-zA-Z]+ [a-zA-Z0-9_]*;

UNSIGNED_INTEGER: (HEX | OCTAL | DECIMAL) LONG? UNSIGNED?;
FLOAT: DEC_FLOAT | HEX_FLOAT;

// Milliseconds are matched before minutes and seconds: 'ms' would otherwise be
// consumed as MINUTE followed by a stray 's'.
TIME: ('0t' | '0T')
      (DEC_SEQ DAY)?
      (DEC_SEQ HOUR)?
      (DEC_SEQ MILISECOND | DEC_SEQ MINUTE)?
      (DEC_SEQ SECOND)?
      (DEC_SEQ MILISECOND)?;

// Self-delimiting, so no lexer mode is needed. The previous CSTRING rule matched
// any run of characters other than '$', ';' and newline, anywhere in the input,
// which swallowed ordinary program text.
CCODE: '$' ~[;\r\n]*;

STRING: '"' [a-zA-Z0-9 ]* '"';

LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);
WS: [ \t\r\n]+ -> skip;

// All helpers below are fragments: as standalone tokens they compete with the real
// tokens above (HEX_SEQ, for instance, matches any identifier spelled with a-f).
fragment DEC_FLOAT: DEC_SEQ? '.' DEC_SEQ (EXPONENT ('+' | '-')? DEC_SEQ)? (LONG | FLOAT_SUFFIX)?;
fragment HEX_FLOAT: HEX_PREFIX HEX_SEQ? '.' HEX_SEQ (BIN_EXPONENT ('+' | '-')? DEC_SEQ)? (LONG | FLOAT_SUFFIX)?;
fragment DEC_SEQ: [0-9]+;
fragment HEX_SEQ: [0-9a-fA-F]+;
fragment BIN_EXPONENT: 'P' | 'p';
fragment EXPONENT: 'E' | 'e';
fragment DECIMAL: '0' | [1-9] [0-9]*;
fragment OCTAL: '0' [0-7]+;
fragment HEX: HEX_PREFIX HEX_SEQ;
fragment HEX_PREFIX: '0' ('X' | 'x');
fragment LONG: 'L' | 'l';
fragment FLOAT_SUFFIX: 'F' | 'f';
fragment UNSIGNED: 'U' | 'u';
fragment DAY: 'D' | 'd';
fragment HOUR: 'H' | 'h';
fragment MINUTE: 'M' | 'm';
fragment SECOND: 'S' | 's';
fragment MILISECOND: 'MS' | 'ms';
