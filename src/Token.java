public class Token {
    final TokenType type;
    final String lexeme;
    final Category category;
    final Object literal;
    final int line;
    final int col;
    final Scope scope;

    public Token(TokenType type, Category category, String lexeme, Object literal, int line, int col, Scope scope) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = category;
        this.literal = literal;
        this.line = line;
        this.col = col;
        this.scope = scope;
    }

    public Token(TokenType type, Object literal) {
        this.type = type;
        this.lexeme = literal.toString();
        this.category = Category.GENERIC;
        this.literal = literal;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
    }

    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = Category.GENERIC;
        this.literal = lexeme;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
    }

    public Token(TokenType type) {
        this.type = type;
        this.lexeme = "";
        this.category = Category.GENERIC;
        this.literal = lexeme;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
    }

    @Override
    public String toString() {
        return String.format("%s[%d:%d]<lexeme: '%s'>", type, line, col, lexeme);
    }    
}