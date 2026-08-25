grammar LabeledExpr;

// ==========================================
// Reglas Sintacticas
// ==========================================

prog
    : stat+
    ;

stat
    : expr NEWLINE                # printExpr
    | ID '=' expr NEWLINE         # assign
    | 'clear' NEWLINE             # clear
    | NEWLINE                     # blank
    ;

expr
    : expr op=('*'|'/') expr      # MulDiv
    | expr op=('+'|'-') expr      # AddSub
    | INT                         # int
    | ID                          # id
    | '(' expr ')'                # parens
    ;

// ==========================================
// Reglas Lexicas (Tokens)
// ==========================================

MUL     : '*' ;
DIV     : '/' ;
ADD     : '+' ;
SUB     : '-' ;

ID      : [a-zA-Z]+ ;
INT     : [0-9]+ ;
NEWLINE : '\r'? '\n' ;
WS      : [ \t]+ -> skip ;
