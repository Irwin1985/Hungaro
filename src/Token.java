public class Token {
    final TokenType type;
    final String lexeme;
    final Category category;
    final Object literal;
    final int line;
    final int col;
    final Scope scope;
    final boolean isConstant;

    public Token(TokenType type, Category category, String lexeme, Object literal, int line, int col, Scope scope, boolean isConstant) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = category;
        this.literal = literal;
        this.line = line;
        this.col = col;
        this.scope = scope;
        this.isConstant = isConstant;
    }

    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = Category.GENERIC;
        this.literal = lexeme;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
        this.isConstant = false;
    }

    public Token(TokenType type) {
        this.type = type;
        this.lexeme = "";
        this.category = Category.GENERIC;
        this.literal = lexeme;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
        this.isConstant = false;
    }

    @Override
    public String toString() {
        return String.format("%s[%d:%d]<lexeme: '%s'>", type, line, col, lexeme);
    }    
}