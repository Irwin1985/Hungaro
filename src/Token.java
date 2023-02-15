public class Token {
    TokenType type;
    String lexeme;
    Category category;
    Object literal;
    int line;
    int col;

    public Token(TokenType type, Category category, String lexeme, Object literal, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.category = category;
        this.literal = literal;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("%s[%d:%d]<lexeme: '%s'>", type, line, col, lexeme);
    }    
}