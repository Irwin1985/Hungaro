public class Token {
    final TokenType type;
    String lexeme;
    final Category category;
    Object literal;
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

    public Token(TokenType type, String lexeme, Category category) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = category;
        this.literal = lexeme;
        this.line = 0;
        this.col = 0;
        this.scope = Scope.NONE;
    }

    @Override
    public String toString() {
        return String.format("(type:%s, cat: %s)[%d:%d]<lexeme: '%s'>", type, category, line, col, lexeme);
    }    
}