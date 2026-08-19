/*
 * ANTLR4 grammar for the Reflex-AL annotation language.
 *
 * Direct translation of the EBNF grammar given in Appendix "ГРАММАТИКА ЯЗЫКА"
 * of the Reflex-AL language specification. Rule names are lower-cased to
 * become ANTLR4 parser rules; the lexical productions (Identifier, the
 * literal forms, and their helper rules Letter/Digit/UnsignedInteger/
 * ExponentPart) become ANTLR4 lexer rules, since ANTLR4 requires parser
 * rules to start with a lower-case letter and lexer rules with an
 * upper-case one.
 *
 * Deviations from the literal source EBNF, made because the source text
 * itself appears inconsistent or because a runnable grammar needs a rule
 * the source leaves implicit:
 *   1. InExpr: the source writes `"in" "(" Identifier, Identifier ")"`
 *      with an unquoted comma, inconsistent with every other use of a
 *      literal comma elsewhere in the same grammar (always `","`). Taken
 *      here as a typo for `"in" "(" Identifier "," Identifier ")"`,
 *      matching the semantics "in(rho,xi)" used throughout the rest of
 *      the specification.
 *   2. A whitespace-skipping lexer rule (WS) is added at the end. The
 *      source EBNF does not define lexical layout (as is typical of an
 *      EBNF appendix), but an ANTLR4 grammar needs one to be usable on
 *      real, whitespace-containing annotation text.
 * Everything else -- including apparent quirks of the source, such as
 * TimeLiteral permitting an empty suffix after "T#", or the UnsignedInteger
 * rule being defined but never referenced by any other rule -- is carried
 * over exactly as given, not "fixed".
 */

grammar ReflexAL;

// =====================================================================
// Parser rules
// =====================================================================

// ---- Top level: how an annotation is embedded as a comment ----------

lineAnnotation: '//' annotation;
blockAnnotation: '/*' annotation* '*/';

// ---- Annotation structure --------------------------------------------

annotation: '[' annotationKind languageSpec? ':' annotationBody ']';

annotationKind
    : 'assume'
    | 'assert'
    | 'invariant'
    | 'define'
    ;

languageSpec: '(' Identifier ')';

annotationBody
    : specificationExpr
    | definitionList
    ;

definitionList: definition (',' definition)*;

definition
    : variableDefinition
    | functionDefinition
    ;

variableDefinition: typeName Identifier '=' specificationExpr;

functionDefinition: typeName Identifier '(' parameterList? ')' '=' specificationExpr;

parameterList: typeName Identifier (',' typeName Identifier)*;

// ---- Expressions, by precedence (loosest to tightest) ----------------

specificationExpr: implicationExpr;

implicationExpr: equivalenceExpr ('==>' equivalenceExpr)*;

equivalenceExpr: logicalOrExpr ('<==>' logicalOrExpr)*;

logicalOrExpr: logicalAndExpr ('||' logicalAndExpr)*;

logicalAndExpr: equalityExpr ('&&' equalityExpr)*;

equalityExpr: relationalExpr (('==' | '!=') relationalExpr)*;

relationalExpr: bitwiseOrExpr (('<' | '>' | '<=' | '>=') bitwiseOrExpr)*;

bitwiseOrExpr: bitwiseXorExpr ('|' bitwiseXorExpr)*;

bitwiseXorExpr: bitwiseAndExpr ('^' bitwiseAndExpr)*;

bitwiseAndExpr: shiftExpr ('&' shiftExpr)*;

shiftExpr: additiveExpr (('<<' | '>>') additiveExpr)*;

additiveExpr: multiplicativeExpr (('+' | '-') multiplicativeExpr)*;

multiplicativeExpr: unaryExpr (('*' | '/' | '%') unaryExpr)*;

unaryExpr
    : unaryOperator unaryExpr
    | postfixExpr
    ;

unaryOperator: '+' | '-' | '!' | '~';

postfixExpr: primaryExpr scopeAccess*;

// ---- Variables, member/array access, calls, scope --------------------

variable: (Identifier? '#' Identifier? '#')? Identifier;

identifierPrimaryExpr: variable ( (memberAccess | arraySubscript)* | functionArguments )?;
memberAccess: '.' Identifier;
arraySubscript: '[' specificationExpr ']';

functionArguments: '(' argumentList? ')';

argumentList: specificationExpr (',' specificationExpr)*;

scopeAccess: '.scope(' scopeSpecifier ')';

scopeSpecifier
    : 'pre'
    | 'prev'
    | pastScopeSpecifier
    ;
pastScopeSpecifier: 'past' '(' specificationExpr ')';

// ---- Primary expressions -----------------------------------------------

primaryExpr
    : literal
    | identifierPrimaryExpr
    | quantifiedExpr
    | temporalExpr
    | processExpr
    | '(' specificationExpr ')'
    ;

// ---- Quantifiers -------------------------------------------------------

quantifiedExpr
    : universalQuantification
    | existentialQuantification
    ;

universalQuantification: 'forall' '(' quantifiedVariableList ':' specificationExpr ')';

existentialQuantification: 'exists' '(' quantifiedVariableList ':' specificationExpr ')';

quantifiedVariableList: quantifiedVariable (',' quantifiedVariable)*;

quantifiedVariable: Identifier quantifierDomain?;

quantifierDomain: 'in' domainExpr;

domainExpr
    : setDomain
    | rangeDomain
    | typeName
    | identifierPrimaryExpr
    ;

rangeDomain: specificationExpr '..' specificationExpr;

setDomain: '{' specificationExpr (',' specificationExpr)? '}';

// ---- Temporal operators -------------------------------------------------

temporalExpr
    : previousExpr
    | nextExpr
    | onceExpr
    | duringExpr
    | timerExpr
    | withinExpr
    | stableExpr
    | cooldownExpr
    | onExpr
    ;

previousExpr: 'previously' '(' specificationExpr ')';

nextExpr: 'next' '(' specificationExpr ')';

onceExpr: 'once' '(' specificationExpr ')';

duringExpr: 'during' '(' specificationExpr ',' specificationExpr ',' specificationExpr ')';

withinExpr: 'within' '(' specificationExpr ',' specificationExpr ')';

stableExpr: 'stable' '(' specificationExpr ',' specificationExpr ')';

cooldownExpr: 'cooldown' '(' specificationExpr ',' specificationExpr ')';

timerExpr: 'timer' '(' specificationExpr ')';

// on(trigger, property): wherever the trigger holds at a reachable state, so does the
// property. Defined in the translation specification but absent from the source grammar,
// so it is added here rather than being unwritable.
onExpr: 'on' '(' trigger=specificationExpr ',' property=specificationExpr ')';

// ---- Process-oriented functions -----------------------------------------

processExpr
    : inExpr
    | timeExpr
    ;

// NB: source EBNF has an unquoted comma here; see file header, note 1.
inExpr: 'in' '(' Identifier ',' Identifier ')';

timeExpr: 'time' '(' Identifier ')';

// ---- Types ---------------------------------------------------------------

typeName
    : 'int' | 'nat' | 'real' | 'string' | 'char' | 'bool' | 'time'
    | Identifier
    ;

// ---- Literals (parser-level grouping; see lexer rules below) -------------

literal
    : IntegerLiteral
    | RealLiteral
    | BooleanLiteral
    | TimeLiteral
    ;

// =====================================================================
// Lexer rules
// =====================================================================

BooleanLiteral: 'true' | 'false';

RealLiteral: Digit+ '.' Digit+ ExponentPart?;

IntegerLiteral: Digit+;

// ("t"|"T") "#" [Digit+("d"|"D")] [Digit+("h"|"H")] [Digit+("m"|"M")]
//                [Digit+("s"|"S")] [Digit+("ms"|"MS")]
// All five unit groups are optional in the source grammar, so -- as
// given -- a bare "T#" with no digits at all is a syntactically valid
// TimeLiteral; this is carried over unchanged rather than fixed.
TimeLiteral
    : ('t' | 'T') '#'
      (Digit+ ('d' | 'D'))?
      (Digit+ ('h' | 'H'))?
      (Digit+ ('m' | 'M'))?
      (Digit+ ('s' | 'S'))?
      (Digit+ ('ms' | 'MS'))?
    ;

Identifier: Letter (Letter | Digit | '_')*;

fragment ExponentPart: ('e' | 'E') ('+' | '-')? Digit+;

fragment Letter: [a-zA-Z];

// Defined in the source grammar but not referenced by any other rule
// there; kept for fidelity to the original appendix.
fragment UnsignedInteger: Digit+;

fragment Digit: [0-9];

// ---- Layout (not specified in the source EBNF; added for usability) -----

WS: [ \t\r\n]+ -> skip;
