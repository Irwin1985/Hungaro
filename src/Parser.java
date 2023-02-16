import java.util.List;
import java.util.ArrayList;

public class Parser {
    private int current = 0;
    private List<Token> tokens;

    private static class ParseError extends RuntimeException {}

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.DECLARE)) return genericDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt genericDeclaration() {
        final Token keyword = previous();
        final List<Stmt> statements = new ArrayList<Stmt>();
        do {
            match(TokenType.SEMICOLON); // consume optional semicolon
            statements.add(parseDeclareStatement(keyword));
        } while (match(TokenType.COMMA) && !isAtEnd());
        
        consume(TokenType.SEMICOLON, "Expect new line after declaration.");

        return new Stmt.Declare(keyword, statements);
    }

    private Stmt parseDeclareStatement(Token keyword) {
        final Token identifier = consume(TokenType.IDENTIFIER, "Expect identifier after `declare`.");
        
        // check for constant declaration
        if (Hungaro.isConstant(identifier.lexeme)) {
            return constantDeclaration(keyword, identifier);
        }

        // check for variable declaration
        String singleType = identifier.lexeme.substring(0, 2);
        switch (singleType) {
            case "ls": // local string
            case "gs": // global string
            case "ps": // parameter string                
            case "ln": // local number            
            case "gn": // global number
            case "pn": // parameter number
            case "lb": // local boolean
            case "gb": // global boolean
            case "pb": // parameter boolean
            case "la": // local array
            case "ga": // global array
            case "pa": // parameter array
            case "lo": // local object
            case "go": // global object
            case "po": // parameter object
            case "lm": // local map
            case "gm": // global map
            case "pm": // parameter map            
                return variableDeclaration(keyword, identifier);
        }
        // check for function, class, or procedure declaration
        String type = identifier.lexeme.substring(0, 3);
        switch (type) {
            case "fnc":
                return functionDeclaration(keyword, identifier);
            case "cls":
                return classDeclaration(keyword, identifier);
            case "prc":
                return procedureDeclaration(keyword, identifier);
            default:
                error(identifier, "Invalid name: please provide one of the following suffixes:\n`l` for locals\n`g` for globals\n`p` for parameters\nNOTE: you can omite the scoping suffix (first letter) if you are declaring a constant (all uppercase) or your are inside of a class body.");
                return null;
        }
    }

    private Stmt variableDeclaration(Token keyword, Token identifier) {
        Expr value = null;
        if (match(TokenType.SIMPLE_ASSIGN)) {            
            value = expression();
        }
        // consume(TokenType.SEMICOLON, "Expect new line after variable declaration.");
        return new Stmt.Var(keyword, identifier, value);
    }

    private Stmt constantDeclaration(Token keyword, Token identifier) {
        consume(TokenType.SIMPLE_ASSIGN, "Expect `=` after constant declaration.");
        final Expr value = expression();
        // consume(TokenType.SEMICOLON, "Expect new line after constant declaration.");
        return new Stmt.Constant(keyword, identifier, value);
    }

    private Stmt functionDeclaration(Token keyword, Token identifier) {
        return null;
    }

    private Stmt classDeclaration(Token keyword, Token identifier) {
        return null;
    }

    private Stmt procedureDeclaration(Token keyword, Token identifier) {
        return null;
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        return expressionStmt();
    }

    private Stmt printStatement() {
        List<Expr> expressions = new ArrayList<Expr>();        

        do {
            expressions.add(expression());
        } while (match(TokenType.COMMA));

        consume(TokenType.SEMICOLON, "Expect new line after expression.");
        return new Stmt.Print(expressions);
    }

    private Stmt expressionStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect new line after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr target = logicalOr();

        if (peek().category == Category.ASSIGNMENT) {
            Token equals = advance();            
            Expr value = assignment();

            if (target instanceof Expr.Variable) {   
                if (Hungaro.isConstant(((Expr.Variable) target).name.lexeme)) {
                    error(equals, "Cannot assign to constant.");
                }
                return new Expr.Set(equals, target, value, false);
            } else if (target instanceof Expr.Prop) {
                return new Expr.Set(equals, target, value, true);
            }

            error(equals, "Invalid assignment target.");
        }

        return target;
    }

    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.LOGICAL_OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.LOGICAL_AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQU_OPE)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.REL_OPE)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.TERM)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.FACTOR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.LOGICAL_NOT, TokenType.TERM)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr left = primary();

        while (true) {
            if (match(TokenType.LPAREN)) {
                left = finishCall(left);
            } 
            else if (match(TokenType.DOT)) {
                Token keyword = previous();
                Token propIdent = consume(TokenType.IDENTIFIER, "Expect property keyword after '.'.");
                Expr property = new Expr.Variable(propIdent);
                left = new Expr.Prop(keyword, left, property, false);                
            }
            else if (match(TokenType.LBRACKET)) {
                Token keyword = previous();
                Expr indexOrkey = expression();
                consume(TokenType.RBRACKET, "Expect ']' after expression.");
                left = new Expr.Prop(keyword, left, indexOrkey, true);
            }
            else {
                break;
            }
        }

        return left;
    }

    private Expr finishCall(Expr callee) {        
        final List<Expr> arguments = new ArrayList<Expr>();
        
        // add the implicit `this` argument
        if (callee instanceof Expr.Prop) {
            if (((Expr.Prop)callee).target instanceof Expr.Super) {
                final Token token = new Token(TokenType.IDENTIFIER, Category.IDENTIFIER, "this", null, 0, 0);
                arguments.add(new Expr.Variable(token));
            } else {
                arguments.add(((Expr.Prop)callee).target);
            }
        }
        
        // parse the arguments
        if (!check(TokenType.RPAREN)) {
            do {                
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RPAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (peek().category == Category.LITERAL) {
            return new Expr.Literal(advance());
        }
        else if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        else if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return expr;
        }
        else if (match(TokenType.LBRACKET)) {
            return array();
        }
        else if (match(TokenType.LBRACE)) {
            return map();
        }

        throw error(peek(), "Expect expression.");
    }

    private Expr array() {
        Token keyword = previous();
        List<Expr> elements = new ArrayList<Expr>();
        match(TokenType.SEMICOLON); // optional semicolon

        if (!check(TokenType.RBRACKET)) {
            do {
                match(TokenType.SEMICOLON); // optional semicolon (before element)
                if (check(TokenType.RBRACKET)) break;
                elements.add(expression());
                match(TokenType.SEMICOLON); // optional semicolon
            } while (match(TokenType.COMMA));
        }
        match(TokenType.SEMICOLON); // optional semicolon (after last element)        
        consume(TokenType.RBRACKET, "Expect ']' after array elements.");
        return new Expr.Array(keyword, elements);
    }

    private Expr map() {
        Token keyword = previous();
        List<Expr> keys = new ArrayList<Expr>();
        List<Expr> values = new ArrayList<Expr>();
        match(TokenType.SEMICOLON); // optional semicolon

        if (!check(TokenType.RBRACE)) {
            do {
                match(TokenType.SEMICOLON); // optional semicolon (before key
                if (check(TokenType.RBRACE)) break;
                keys.add(expression());
                consume(TokenType.COLON, "Expect ':' after map key.");
                values.add(expression());
                match(TokenType.SEMICOLON); // optional semicolon
            } while (match(TokenType.COMMA));
        }
        match(TokenType.SEMICOLON); // optional semicolon (after last element
        consume(TokenType.RBRACE, "Expect '}' after map elements.");
        return new Expr.Map(keyword, keys, values);
    }


    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Hungaro.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
