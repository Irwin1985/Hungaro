import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    private int current = 0;
    private List<Token> tokens;
    private final Stack<Integer> functionStack = new Stack<Integer>();
    
    private final int PARSING_FUNCTION = 1;
    private final int PARSING_PROCEDURE = 2;

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

        return new Stmt.Declare(keyword, statements);
    }

    private Stmt parseDeclareStatement(Token keyword) {
        final Token identifier = consume(TokenType.IDENTIFIER, "Expect identifier after `declare`.");
        switch (identifier.category) {
            case GLOBAL_CONSTANT:
            case LOCAL_CONSTANT:
                return constantDeclaration(keyword, identifier);
            case GLOBAL_VARIABLE:
            case LOCAL_VARIABLE:
                return variableDeclaration(keyword, identifier);
            case LOCAL_FUNCTION:
            case GLOBAL_FUNCTION:
                return functionDeclaration(keyword, identifier, "function");
            case LOCAL_PROCEDURE:
            case GLOBAL_PROCEDURE:
                return functionDeclaration(keyword, identifier, "procedure");
            case LOCAL_CLASS:
            case GLOBAL_CLASS:
                return classDeclaration(keyword, identifier);
            default:
                error(identifier, "Invalid name: please provide one of the following suffixes:\n`l` for locals\n`g` for globals\n`p` for parameters\nNOTE: you can omite the scoping suffix (first letter) if you are declaring a constant (all uppercase) or your are inside of a class body.");
        }
        return null;
    }

    private Stmt variableDeclaration(Token keyword, Token identifier) {
        Expr value = null;
        if (match(TokenType.SIMPLE_ASSIGN)) {            
            value = expression();
        } else {
            // provide default value based on second letter of identifier
            if (identifier.category == Category.GLOBAL_CONSTANT || identifier.category == Category.LOCAL_CONSTANT) {
                error(identifier, "Constants must be initialized.");
            }
            switch (identifier.lexeme.charAt(1)) {
                case 's':
                    value = new Expr.Literal(new Token(TokenType.STRING, ""));
                    break;
                case 'n':
                    value = new Expr.Literal(new Token(TokenType.NUMBER, 0));
                    break;
                case 'b':
                    value = new Expr.Literal(new Token(TokenType.FALSE, false));
                    break;
                case 'a':
                    value = new Expr.Array(null, null);
                    break;
                case 'm':
                    value = new Expr.Map(null, null, null);
                    break;
                case 'o':                    
                    break;
                default:
                    error(identifier, "Invalid variable type: variables must follow the following rule: first letter `l` or `g` match the variable scope and second letter match the type: \n1. `s` for string.\n2. `n` for number.\n3. `b` for boolean.\n4. `a` for array.\n5. `m` for map.\n6. `o` for object.\n");
            }
        }
        consume(TokenType.SEMICOLON, "Expect new line after variable declaration.");
        return new Stmt.Var(keyword, identifier, value);
    }

    private Stmt constantDeclaration(Token keyword, Token identifier) {
        consume(TokenType.SIMPLE_ASSIGN, "Expect `=` after constant declaration.");
        final Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect new line after constant declaration.");
        return new Stmt.Constant(keyword, identifier, value);
    }

    private Stmt functionDeclaration(Token keyword, Token identifier, String name) {
        List<Expr.Variable> parameters = new ArrayList<Expr.Variable>();
        boolean mustReturnValue = true;
        Token param = null;

        if (name.equals("method")) {
            parameters.add(new Expr.Variable(new Token(TokenType.IDENTIFIER, "this")));
        }
        if (match(TokenType.LPAREN)) {
            if (!check(TokenType.RPAREN)) {
                do {
                    param = consume(TokenType.IDENTIFIER, "Expect parameter name.");
                    if (param.category != Category.PARAMETER) {
                        error(param, "Invalid parameter name: parameters must follow the following rule: first letter `p` match the parameter and second letter match the type: \n1. `s` for string.\n2. `n` for number.\n3. `b` for boolean.\n4. `a` for array.\n5. `m` for map.\n6. `o` for object.\n");
                    }
                    parameters.add(new Expr.Variable(param));
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RPAREN, "Expect `)` after parameters.");
        }

        if (name.equals("procedure")) {
            mustReturnValue = false;
            functionStack.push(PARSING_PROCEDURE);
        } else {
            functionStack.push(PARSING_FUNCTION);
        }
            
        final Stmt.Block body = block();
        functionStack.pop();
        return new Stmt.Function(mustReturnValue, identifier, parameters, body);
    }

    private Stmt.Block block() {
        List<Stmt> statements = new ArrayList<Stmt>();
        consume(TokenType.SEMICOLON, "Expect new line before block declaration.");

        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.END, "Expect `end` after block declaration.");
        consume(TokenType.SEMICOLON, "Expect new line after block declaration.");

        return new Stmt.Block(statements);
    }

    private Stmt classDeclaration(Token keyword, Token identifier) {
        return null;
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.IF)) return ifStatement();
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

    private Stmt returnStatement() {
        if (functionStack.isEmpty()) {
            error(previous(), "Cannot return from top-level code.");
        }
        if (functionStack.peek() == PARSING_PROCEDURE) {
            error(previous(), "Cannot return a value from a procedure.");
        }

        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expect new line after return statement.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt ifStatement() {
        Expr condition = null;
        if (match(TokenType.LPAREN)) {
            condition = expression();
            consume(TokenType.RPAREN, "Expect `)` after `if` condition.");
        } else {
            condition = expression();
        }
        
        consume(TokenType.SEMICOLON, "Expect new line before `if` block.");
        List<Stmt> thenStmt = new ArrayList<Stmt>();
        Stmt.Block elseBranch = null;

        // then branch
        do {
            thenStmt.add(declaration());
        } while (!check(TokenType.ELSE) && !check(TokenType.END) && !isAtEnd());
        Stmt.Block thenBranch = new Stmt.Block(thenStmt);
        
        // else branch
        if (match(TokenType.ELSE)) {
            elseBranch = block();
        } else {
            consume(TokenType.END, "Expect `end` after `if` block.");
            consume(TokenType.SEMICOLON, "Expect new line after `if` block.");
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
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
                Category targetCategory = ((Expr.Variable) target).name.category;
                if (targetCategory == Category.GLOBAL_CONSTANT || targetCategory == Category.LOCAL_CONSTANT) {
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
        
        // check if the calle is a Prop expression.
        if (callee instanceof Expr.Prop) {
            // check if the target is a super expression: super.method()
            if (((Expr.Prop)callee).target instanceof Expr.Super) {
                final Token token = new Token(TokenType.IDENTIFIER, "this");
                arguments.add(new Expr.Variable(token));
            } else { // check if the target is a literal expression. eg: "string".len()
                if (((Expr.Prop)callee).target instanceof Expr.Literal) {
                    Token token;
                    final Expr.Literal literal = (Expr.Literal)((Expr.Prop)callee).target;
                    switch (literal.token.type) {
                        case STRING:
                            token = new Token(TokenType.IDENTIFIER, "_STRING");
                            arguments.add(new Expr.Variable(token));                            
                            break;
                        case NUMBER:
                            token = new Token(TokenType.IDENTIFIER, "_NUMBER");
                            arguments.add(new Expr.Variable(token));
                            break;
                        default:
                            error(((Expr.Prop)callee).target.token, "Invalid target for method call.");                                    
                    }
                } 
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
        List<String> keys = new ArrayList<String>();
        List<Expr> values = new ArrayList<Expr>();
        match(TokenType.SEMICOLON); // optional semicolon
        Expr exp = null;

        if (!check(TokenType.RBRACE)) {
            do {
                match(TokenType.SEMICOLON); // optional semicolon (before key
                if (check(TokenType.RBRACE)) break;
                exp = expression();
                if (exp instanceof Expr.Literal) {
                    if (((Expr.Literal)exp).token.type != TokenType.STRING) {
                        error(exp.token, "Expect string literal as map key.");
                    }                    
                }
                else if (!(exp instanceof Expr.Variable)) {
                    error(exp.token, "Expect string literal as map key.");
                }
                keys.add(exp.token.lexeme);
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
