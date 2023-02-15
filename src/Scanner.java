import java.util.List;
import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// DEBUG
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
// DEBUG

public class Scanner {
    private final String source;
    private int cursor = 0;
    private int tokenCounter = 0;
    private TokenType lastToken;
    private int line = 1;
    private int col = 1;
    private String lexeme;

    private List<Token> tokens = new ArrayList<Token>();


    static class Spec {
        final Pattern pattern;
        final TokenType type;
        final Category category;

        public Spec(Pattern pattern, TokenType type, Category category) {
            this.pattern = pattern;
            this.type = type;
            this.category = category;
        }
    }

    private Spec[] specs = {
        // Whitespace
        new Spec(Pattern.compile("^[ \\t\\r\\f]+"), TokenType.IGNORE, Category.IGNORABLE),

        // Single-line comment
        new Spec(Pattern.compile("^#.*"), TokenType.IGNORE, Category.IGNORABLE),

        // Multi-line comment
        new Spec(Pattern.compile("^\"\"\"[\\s\\S]*?\"\"\""), TokenType.IGNORE, Category.IGNORABLE),

        // Comma + new line: it's used to concatenate expressions.
        new Spec(Pattern.compile("^;[\\s]*?\\n"), TokenType.IGNORE, Category.IGNORABLE),

        // New line: emmit a ';'
        new Spec(Pattern.compile("^\\n+"), TokenType.SEMICOLON, Category.GENERIC),

        // Numbers
        new Spec(Pattern.compile("^\\d+[_\\.\\d]*"), TokenType.NUMBER, Category.LITERAL),

        // Double quoted string
        new Spec(Pattern.compile("^\"(?:[^\"\\\\^'\\\\]|\\\\.)*\""), TokenType.STRING, Category.LITERAL),

        // Single quoted string:
        new Spec(Pattern.compile("^'(?:[^\"\\\\^'\\\\]|\\\\.)*'"), TokenType.STRING, Category.LITERAL),

        // Backticked string:
        new Spec(Pattern.compile("^`[^`]*`"), TokenType.STRING, Category.LITERAL),

        // Relational Operators
        new Spec(Pattern.compile("^[<>]=?"), TokenType.REL_OPE, Category.GENERIC),
        new Spec(Pattern.compile("^[!=]="), TokenType.EQU_OPE, Category.GENERIC),

        // Logical Operators
        new Spec(Pattern.compile("^\\band\\b"), TokenType.LOGICAL_AND, Category.GENERIC),
        new Spec(Pattern.compile("^\\bor\\b"), TokenType.LOGICAL_OR, Category.GENERIC),
        new Spec(Pattern.compile("^!"), TokenType.LOGICAL_NOT, Category.UNARY),

        // Keywords        
        new Spec(Pattern.compile("^\\bas\\b"), TokenType.AS,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bdeclare\b"), TokenType.DECLARE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bif\\b"), TokenType.IF,Category.KEYWORD),
        new Spec(Pattern.compile("^\\belse\\b"), TokenType.ELSE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\btrue\\b"), TokenType.TRUE,Category.LITERAL),
        new Spec(Pattern.compile("^\\bfalse\\b"), TokenType.FALSE,Category.LITERAL),
        new Spec(Pattern.compile("^\\bnull\\b"), TokenType.NULL,Category.LITERAL),
        new Spec(Pattern.compile("^\\breturn\\b"), TokenType.RETURN,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bmatch\\b"), TokenType.MATCH,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bcase\\b"), TokenType.CASE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\botherwise\\b"), TokenType.OTHERWISE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bnew\\b"), TokenType.NEW,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bprint\\b"), TokenType.PRINT,Category.KEYWORD),
        new Spec(Pattern.compile("^\\brelease\\b"), TokenType.RELEASE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bdefer\\b"), TokenType.DEFER,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bend\\b"), TokenType.END,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bwhile\\b"), TokenType.WHILE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\brepeat\\b"), TokenType.REPEAT,Category.KEYWORD),
        new Spec(Pattern.compile("^\\buntil\\b"), TokenType.UNTIL,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bfor\\b"), TokenType.FOR,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bto\\b"), TokenType.TO,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bstep\\b"), TokenType.STEP,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bimport\\b"), TokenType.IMPORT,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bmodule\\b"), TokenType.MODULE,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bexit\\b"), TokenType.EXIT,Category.KEYWORD),
        new Spec(Pattern.compile("^\\bloop\\b"), TokenType.LOOP,Category.KEYWORD),

        // Assignment operators
        new Spec(Pattern.compile("^="), TokenType.SIMPLE_ASSIGN, Category.ASSIGNMENT),
        new Spec(Pattern.compile("^[\\+\\-\\*/]="), TokenType.COMPLEX_ASSIGN, Category.ASSIGNMENT),
        new Spec(Pattern.compile("^[\\+\\-]"), TokenType.TERM, Category.UNARY),
        new Spec(Pattern.compile("^[\\*/]"), TokenType.TERM, Category.UNARY),

        // Identifier
        new Spec(Pattern.compile("^\\w+"), TokenType.IDENTIFIER, Category.LITERAL),

        // Symbols and delimiters
        new Spec(Pattern.compile("^\\("), TokenType.LPAREN, Category.GENERIC),
        new Spec(Pattern.compile("^\\)"), TokenType.RPAREN, Category.GENERIC),
        new Spec(Pattern.compile("^\\["), TokenType.LBRACKET, Category.GENERIC),
        new Spec(Pattern.compile("^\\]"), TokenType.RBRACKET, Category.GENERIC),
        new Spec(Pattern.compile("^\\{"), TokenType.LBRACE, Category.GENERIC),
        new Spec(Pattern.compile("^\\}"), TokenType.RBRACE, Category.GENERIC),
        new Spec(Pattern.compile("^\\."), TokenType.DOT, Category.GENERIC),
        new Spec(Pattern.compile("^,"), TokenType.COMMA, Category.GENERIC),
        new Spec(Pattern.compile("^\\:"), TokenType.COLON, Category.GENERIC),
        new Spec(Pattern.compile("^\\?"), TokenType.PRINT, Category.KEYWORD),
    };


    public Scanner(String source) {
        if (!source.endsWith("\n")) {
            source += "\n";
        }        
        this.source = source;        
    }

    public List<Token> scanTokens() {
        for (;;) {
            final Token token = getNextToken();
            if (token == null)
                break;
            tokens.add(token);
        }
        tokens.add(new Token(TokenType.EOF, Category.GENERIC, "", "", line, col));

        return tokens;
    }

    private Token getNextToken() {
        if (cursor >= source.length())
            return null;
        
        String input = source.substring(cursor);

        for (Spec spec : specs) {
            Matcher matcher = spec.pattern.matcher(input);
            if (!matcher.find()) {
                continue;
            }
            // increase cursor to the length of matched string.
            cursor += matcher.end();
            lexeme = matcher.group(0);

            // count number of lines
            int ln = lexeme.length() - lexeme.replace("\n", "").length();
            line += ln;
            if (ln > 0) col = 1;

            // check for the IGNORE token type.
            if (spec.type == TokenType.IGNORE) {
                col += lexeme.length(); // update column number.
                return getNextToken();
            }

            // check for new line
            if (spec.type == TokenType.SEMICOLON) {
                if (lastToken == TokenType.SEMICOLON || tokenCounter == 0)
                    return getNextToken();
                
                lastToken = TokenType.SEMICOLON;
                lexeme = "";
            } else {
                lastToken = spec.type;
            }
            tokenCounter++;

            // return the token and value
            Object value = "";
            Category category = spec.category;
            switch (spec.type) {
                case NUMBER:
                    lexeme = lexeme.replaceAll("_", "");
                    value = Double.parseDouble(lexeme);
                    break;
                case TRUE:
                    value = true;
                    break;
                case FALSE:
                    value = false;
                    break;
                case NULL:
                    value = null;
                    break;
                case STRING:
                    if (lexeme.startsWith("`")) { // raw string
                        lexeme = lexeme.substring(1, lexeme.length()-1);
                    } else {
                        lexeme = lexeme.substring(1, lexeme.length()-1);
                        lexeme = lexeme.replaceAll("\\\\r", "\r");
                        lexeme = lexeme.replaceAll("\\\\n", "\r");
                        lexeme = lexeme.replaceAll("\\\\t", "\t");
                        lexeme = lexeme.replaceAll("\\\\\"", "\"");
                        lexeme = lexeme.replaceAll("\\\\\'", "\'");
                    }
                    value = lexeme;
                    break;
                case SEMICOLON:
                    value = "new line";
                    break;
                case COMPLEX_ASSIGN, TERM, FACTOR, REL_OPE, EQU_OPE, LOGICAL_NOT:
                    switch (lexeme) {
                        case "+", "+=": category = Category.PLUS; break;
                        case "-", "-=": category = Category.MINUS; break;
                        case "*", "*=": category = Category.MUL; break;
                        case "/", "/=": category = Category.DIV; break;
                        case "=": category = Category.ASSIGN; break;
                        case "<": category = Category.LESS; break;
                        case "<=": category = Category.LESS_EQ; break;
                        case ">": category = Category.GREATER; break;
                        case ">=": category = Category.GREATER_EQ; break;
                        case "==": category = Category.EQUAL; break;
                        case "!=": category = Category.NOT_EQ; break;
                        case "!": category = Category.BANG; break;
                        default: break;
                    }
                default:
                    value = lexeme;
                    break;
            }
            Token tok = new Token(spec.type, category, lexeme, value, line, col);
            col += lexeme.length();
            return tok;
        }
        Hungaro.error(line, col, "Unknown character: " + input.charAt(0));
        cursor += input.length();
        return null;
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get("F:\\Desarrollo\\GitHub\\Hungaro\\ideas.txt"));
        String source = new String(bytes, Charset.defaultCharset());

        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();

        for (Token tok : tokens) {
            System.out.println(tok);
        }
    }
}