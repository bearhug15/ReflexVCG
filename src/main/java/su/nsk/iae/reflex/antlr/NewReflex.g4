grammar NewReflex;
program:
    (comments+=comment)*
    'program' name=ID '{'
     clock=clockDefinition
     (consts+=const
     | enums +=enum
     | functions+=functionDecl
     | globalVars+=globalVariable
     | ports+=port
     | processes+=process
     | structures+=structDeclaration
     | imports+=importBlock
     | nodes+=nodeDecl)*

     '}' EOF;
clockDefinition: (comments+=comment)* 'clock' (intValue=UNSIGNED_INTEGER | timeValue=TIME) ';';
process:
    (comments+=comment)*
    'process' name=ID '::' 'node' node=ID '{'
    ((imports+=importedVariableList | variables+=processVariable) ';')*
    states+=state*
    '}';
state:
    (comments+=comment)*
    'state' name=ID looped='looped'? '{'
    stateFunction=statementSeq
    (func=timeoutFunction)?
    '}';

importBlock: (comments+=comment)* 'import' name=ID '{' (importElements+=importElement)* '}';
importElement: (comments+=comment)* (iVector | iRegister | iBit);
iVector: 'vector' name=ID;
iRegister: 'register' name=ID;
iBit: 'bit' name=ID;

nodeDecl: (comments+=comment)* 'node' name=ID '{'
 clock=clockDefinition
 (consts+=const
 | nodeVars+=globalVariable)*
 '}';

importedVariableList: (comments+=comment)* 'shared' (variables+=ID (',' variables+=ID)*) 'from' 'process' processID=ID;
processVariable: (physicalVariable | programVariable) shared='shared'?;
globalVariable: (comments+=comment)* (physicalVariable | programVariable) ';';
physicalVariable: (comments+=comment)* (isDirect=direction)? varType=type name=ID 'as''(' 'read' '=' readId=ID (',''write''='writeId=ID)? (',''config''='configId=ID)? (',''bit''='bitId=ID)?')';
direction: 'direct' | 'indirect';
programVariable: (comments+=comment)* varType=type name=ID ('=' expression)?;
structDeclaration: (comments+=comment)* 'struct' name=ID '{'(variables+=programVariable)+ '}';
timeoutFunction:'timeout' (timeAmountOrRef | '(' timeAmountOrRef ')') body=statement;
timeAmountOrRef: time=TIME | intTime=UNSIGNED_INTEGER | ref=ID;
functionDecl: (comments+=comment)* returnType=type '(' argTypes+=type (',' argTypes+=type)*')';
port: (comments+=comment)* varType=PORT_TYPE name=ID addr1=UNSIGNED_INTEGER addr2=UNSIGNED_INTEGER size=UNSIGNED_INTEGER ';';
PORT_TYPE: 'input' | 'output';
const: (comments+=comment)* 'const' varType=type  name=ID '=' value=expression ';';
enum: (comments+=comment)* 'enum' identifier=ID '{' enumMembers+=enumMember (',' enumMembers+=enumMember) '}';
enumMember: (comments+=comment)* name=ID ('=' value=expression)?;

guardingStatement:
    waitHeader ';'                                                        #Wait
    | 'slice' ';'                                                         #Slice
    | waitHeader 'on' 'timeout' time=timeAmountOrRef body=statement ';'   #WaitOnTimeout
    ;
waitHeader: 'wait' '(' cond=expression ')';

statement: (comments+=comment)* statementKind;
statementKind:
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

iterationStat: 'for' '('init=initIter';' cond=expression ';' upd=expression ')' stat=statement;
initIter: initList | expression;
initList: (inits+=programVariable (',' inits+=programVariable)*)?;
ccodeStat: '$' code=CSTRING;
CSTRING
    : ~[$;\r\n]+
    ;
ifElseStat: 'if' '(' cond=expression ')' then=statement ( 'else' else=statement)?;
switchStat: 'switch' '(' expr=expression ')' '{' options+=caseStat* defaultOption=defaultStat? '}';
caseStat: 'case' option=expression ':' switchOptionBody;
defaultStat: 'default' ':' switchOptionBody;
switchOptionBody: '{' switchOptionStatSeq '}' | switchOptionStatSeq;
switchOptionStatSeq: body=statementSeq break=BREAK?;
BREAK: 'break' ';';
startProcStat: 'start' processId=ID;
stopProcStat: 'stop' (processId=ID)?;
errorProcStat: 'error' (processId=ID)?;
restartStat: 'restart' ';';
resetStat: 'reset' 'timer' ';';
setStateStat: 'set' ('next' 'state' | 'state' stateId=ID);
functionCall: functionID=ID '(' (args+=expression (',' args+=expression)*) ')';
checkStateExpression: 'process' processId=ID 'in' 'state' stateId=STATE_QUAL;
STATE_QUAL:'active'|'inactive'|'stop'|'error';
infixOp: op=INFIX_POSTFIX_OP variable;
postfixOp: variable op=INFIX_POSTFIX_OP;
primaryExpression:
    variable                #Id
    | integer #IntegerVal
    | FLOAT                 #Float
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
unaryExpression                            #Unary
    | checkStateExpression                    #CheckState
    | '(' varType=type ')' expression           #Cast
    | expression op=MUL_OP expression        #Mul
    | expression op=addOp expression        #Add
    | expression op=SHIFT_OP expression    #Shift
    | expression op=COMP_OP expression      #Compare
    | expression op=EQ_OP expression          #Equal
    | expression BIT_AND_OP expression          #BitAnd
    | expression BIT_XOR_OP expression          #BitXor
    | expression BIT_OR_OP expression           #BitOr
    | expression AND_OP expression              #And
    | expression OR_OP  expression              #Or
    | variable assignOp expression              #Assign
    ;
variable:
    varId=ID variableAccess*;
variableAccess: ('.' ID) | ('[' expression ']');
INFIX_POSTFIX_OP: '++' | '--';
unaryOp: '+' | '-' | '~' | '!';
MUL_OP: '*' | '/' | '%';
addOp: '+' | '-';
SHIFT_OP: '<<' | '>>';
EQ_OP: '==' | '!=';
BIT_AND_OP: '&';
BIT_XOR_OP: '^';
BIT_OR_OP: '|';
AND_OP: '&&';
OR_OP: '||';
COMP_OP:
     '<' | '>' | '<=' | '>=';

assignOp:
    '=' | '*=' | '/=' | '%=' | '+=' | '-='
    | '<<=' | '>>=' | '&=' | '^=' | '|=';
type:
     'void' | 'bool' |'time'
    | 'float' | 'double'
    | 'int8' | 'uint8'
    | 'int16' | 'uint16'
    | 'int32' | 'uint32'
    | 'int64' | 'uint64'
    ;
comment
    : LINE_COMMENT
    | BLOCK_COMMENT
    ;
ID: [a-zA-Z]+ [a-zA-Z0-9_]*;
integer: (sign='+' | sign='-')? UNSIGNED_INTEGER;
UNSIGNED_INTEGER: (HEX | OCTAL | DECIMAL) LONG? UNSIGNED?;

FLOAT: ('+' | '-')? (DEC_FLOAT | HEX_FLOAT);
DEC_FLOAT: DEC_SEQ? '.' DEC_SEQ (EXPONENT ('+' | '-')? DEC_SEQ)? (LONG | FLOAT_SUFFIX)?;
HEX_FLOAT: HEX_PREFIX HEX_SEQ? '.' HEX_SEQ (BIN_EXPONENT ('+' | '-')? DEC_SEQ)? (LONG | FLOAT_SUFFIX)?;
DEC_SEQ: [0-9]+;
HEX_SEQ: [0-9a-fA-F]+;
BIN_EXPONENT: 'P' | 'p';
EXPONENT: 'E' | 'e';
//SIGN: '+' | '-';
DECIMAL: '0' | [1-9] [0-9]*;
OCTAL: '0' [0-7]+;
HEX: HEX_PREFIX HEX_SEQ;
HEX_PREFIX: '0' ('X' | 'x');
LONG: 'L' | 'l';
FLOAT_SUFFIX: 'F' | 'f' ;
UNSIGNED: 'U' | 'u';
TIME: ('0t' | '0T') (DECIMAL DAY)? (DECIMAL HOUR)? (DECIMAL MINUTE)? (DECIMAL SECOND)? (DECIMAL MILISECOND)?;
DAY: 'D' | 'd';
HOUR: 'H' | 'h';
MINUTE: 'M' | 'm';
SECOND: 'S' | 's';
MILISECOND: 'MS' | 'ms';
BOOL_VAL: 'true' | 'false';
LINE_COMMENT: '//' ~[\r\n]*;
BLOCK_COMMENT: '/*' .*? '*/';
STRING: '"'[a-zA-Z0-9 ]*'"';
WS: [ \t\r\n\u000C]+ -> skip;
