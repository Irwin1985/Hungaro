public enum Category {
    // Literals
    LITERAL,
    IGNORABLE,
    KEYWORD,
    IDENTIFIER,
    ASSIGNMENT,
    GENERIC,
    UNARY,
    ASSIGN,
    LESS,
    LESS_EQ,
    GREATER,
    GREATER_EQ,
    EQUAL,
    BANG,
    NOT_EQ,
    PLUS,
    MINUS,
    MUL,
    DIV,
    MOD, 
    POW, 

    // Variables
    LOCAL_VARIABLE,      
    GLOBAL_VARIABLE,
    
    // Functions
    LOCAL_FUNCTION,
    GLOBAL_FUNCTION,

    // Procedures
    LOCAL_PROCEDURE,
    GLOBAL_PROCEDURE,

    // Classes
    LOCAL_CLASS,
    GLOBAL_CLASS,

    // Modules
    LOCAL_MODULE,
    GLOBAL_MODULE,

    // Constants
    GLOBAL_CONSTANT,
    LOCAL_CONSTANT,

    // Parameters
    PARAMETER,
    VARIADIC,
}
