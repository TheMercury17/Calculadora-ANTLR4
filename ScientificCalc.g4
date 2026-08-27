grammar ScientificCalc;

// ==========================================
// Reglas Sintacticas Principales
// ==========================================

prog
    : stat+ (EOF)?
    ;

stat
    : expr NEWLINE                                                    # printExpr
    | ID '=' expr NEWLINE                                             # assign
    | 'clear' NEWLINE                                                 # clear
    | 'vars' NEWLINE                                                  # showVars
    | 'plot' '(' expr ',' expr ',' expr ')' NEWLINE                   # plotExpr
    | 'plot' '(' expr ',' expr ',' expr ',' expr ',' expr ')' NEWLINE # plotRangeExpr
    | 'plot' '(' expr ',' expr ',' expr ',' expr ')' NEWLINE          # plotMultiExpr
    | NEWLINE                                                         # blank
    ;

expr
    : <assoc=right> expr '^' expr                                     # power
    | op=('+'|'-') expr                                               # unary
    | expr op=('*'|'/') expr                                          # mulDiv
    | expr op=('+'|'-') expr                                          # addSub
    | function '(' expr ')'                                           # functionCall
    | function2 '(' expr ',' expr ')'                                 # function2Call
    | constant                                                        # constantExpr
    | NUMBER                                                          # number
    | ID                                                              # id
    | '(' expr ')'                                                    # parens
    ;

function
    : 'sin'
    | 'cos'
    | 'tan'
    | 'asin'
    | 'acos'
    | 'atan'
    | 'sqrt'
    | 'log'
    | 'ln'
    | 'abs'
    | 'exp'
    | 'floor'
    | 'ceil'
    ;

function2
    : 'pow'
    | 'max'
    | 'min'
    ;

constant
    : 'pi'
    | 'e'
    ;

// ==========================================
// Reglas Lexicas (Tokens)
// ==========================================

MUL     : '*' ;
DIV     : '/' ;
ADD     : '+' ;
SUB     : '-' ;
POW     : '^' ;

NUMBER  : [0-9]+ ('.' [0-9]+)? ;
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
NEWLINE : '\r'? '\n' ;
WS      : [ \t]+ -> skip ;
