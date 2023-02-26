public enum TokenType {
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    LBRACE,
    RBRACE,
    COMMA,
    SEMICOLON,
    DOT,
    COLON,    

    // keywords
    AS,
    DECLARE,
    IF,
    ELSE,
    ECHO,
    TRUE,
    FALSE,
    GUARD,
    NULL,
    RETURN,
    MATCH,
    CASE,
    OTHERWISE,
    NEW,
    // PRINT,
    RELEASE,
    DEFER,
    END,
    TRY,
    CATCH,
    FINALLY,

    // Iterator keywords
    WHILE,
    REPEAT,
    UNTIL,
    FOR,
    EACH,
    IN,
    TO,
    SUPER,
    STEP,
    IMPORT,
    MODULE,
    EXIT,
    LOOP,

    // Literals
    NUMBER,
    STRING,
    IDENTIFIER,

    // Operators
    SIMPLE_ASSIGN,
    COMPLEX_ASSIGN,
    REL_OPE,
    EQU_OPE,
    TERM,
    FACTOR,
    EXPONENT,
    LOGICAL_OR,
    LOGICAL_AND,
    LOGICAL_NOT,
    IGNORE,
    PARAMETER,    
    EOF,    
}
