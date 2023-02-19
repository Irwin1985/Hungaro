import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    // static enum for parameters and properties validation
    static enum ValidateTypes {
        PARAMETER,
        PROPERTY,
    } 

    private int current = 0;
    private List<Token> tokens;
    private final Stack<String> functionStack = new Stack<String>();
    private final Stack<ValidateTypes> validateStack = new Stack<ValidateTypes>();    
    private final Stack<Token> classStack = new Stack<Token>();

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
                error(identifier, "Invalid name: please check the following naming rules:\nSCOPE:the first letter indicates the variable scope.\n`l` for locals\n`g` for globals\n`p` for parameters\nOTHER RULES:\n*You can omite the scoping suffix (first letter) if you are declaring a constant (all uppercase).\n*All identifiers must follow the camel case naming convention. e.g: lnPersonAge\n*All identifiers must start with a lowercase letter.\n*All identifiers must be at least 3 characters long.\n*All identifiers must be unique within the scope.\n*All identifiers must be unique within the program.\n*All identifiers must be unique within the class.\n");
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
        ValidateTypes validateType = ValidateTypes.PARAMETER;

        if (name.equals("class function") || name.equals("class procedure")) {
            validateType = ValidateTypes.PROPERTY;
            parameters.add(new Expr.Variable(new Token(TokenType.IDENTIFIER, "poThis")));
        }
        validateStack.push(validateType);

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

        functionStack.push(identifier.lexeme); // fName or pName
        if (name.equals("procedure") || name.equals("class procedure")) {
            mustReturnValue = false;
        }
            
        final Stmt.Block body = block();
        functionStack.pop();
        validateStack.pop();

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
        Expr.Variable superClass = null;        
        final List<Stmt> statements = new ArrayList<Stmt>();
        final List<Expr.Set> properties = new ArrayList<Expr.Set>();
        
        
        // check if class is extending another class
        if (match(TokenType.AS)) {
            final Token superClassName = consume(TokenType.IDENTIFIER, "Expect superclass name.");
            classStack.push(superClassName); // push the inherited class token
            superClass = new Expr.Variable(superClassName);
        }
        match(TokenType.SEMICOLON); // optional semicolon

        // parse class properties
        validateStack.push(ValidateTypes.PROPERTY);
        while (check(Category.CLASS_PROPERTY)) {
            Stmt.Expression exp = (Stmt.Expression)expressionStmt();
            if (!(exp.expression instanceof Expr.Set)) {
                error(exp.token, "Invalid class property.");
            }
            properties.add((Expr.Set)exp.expression);
            match(TokenType.SEMICOLON); // optional semicolon
        }
        validateStack.pop();

        // parse class methods
        String name = "";
        while (match(Category.CLASS_FUNCTION, Category.CLASS_PROCEDURE)) {
            if (previous().category == Category.CLASS_FUNCTION)
                name = "class function";
            else if (previous().category == Category.CLASS_PROCEDURE) {
                name = "class procedure";
            }
            statements.add((Stmt.Function) functionDeclaration(keyword, previous(), name));
        }        
        match(TokenType.SEMICOLON); // optional semicolon
        consume(TokenType.END, "Expect `end` after class declaration.");
        consume(TokenType.SEMICOLON, "Expect new line after `end` keyword.");
        
        if (superClass != null) {
            classStack.pop(); // pop the class name
        }

        return new Stmt.Class(keyword, identifier, superClass, properties, new Stmt.Block(statements));
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
        if (functionStack.peek().startsWith("p")) { // procedure
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
            // validate value
            if (value instanceof Expr.Variable) {
                validateTypes((Expr.Variable) value); // validate right side of assignment
            }

            if (target instanceof Expr.Variable) {
                validateTypes((Expr.Variable) target); // validate left side of assignment                
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

    private void validateTypes(Expr.Variable variable) {
        if (variable.name.category == Category.PARAMETER) {
            checkType(variable.name, "parameters", "function or procedure");
        } else if (variable.name.category == Category.CLASS_PROPERTY) {
            checkType(variable.name, "properties", "class");
            // is stack is not empty then we check if we are inside a class
            if (validateStack.peek() != ValidateTypes.PROPERTY) {
                error(variable.name, "Cannot use `property` outside of a class.");
            }
        }
    }    

    private void checkType(Token token, String typeName, String scope) {
        if (validateStack.isEmpty()) {
            error(token, "Cannot use `" + typeName + "` outside of a " + scope + ".");
        }
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
            // add the target as the first argument. eg: foo.bar(1,2,3) => bar(foo, 1, 2, 3)            
            arguments.add(((Expr.Prop)callee).target);
        } else if (callee instanceof Expr.Super) {
            // add 'poThis' pointer as the first argument. eg: super(poThis)
            final Token token = new Token(TokenType.IDENTIFIER, "poThis");
            arguments.add(new Expr.Variable(token));            
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
        else if (match(TokenType.NEW)) {
            return newExpr();
        }
        else if (match(TokenType.SUPER)) {
            return superExpr();
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

    private Expr newExpr() {
        // the 'new' keyword
        Token keyword = previous();

        // clas name
        Token name = consume(TokenType.IDENTIFIER, "Expect class name after 'new'.");
        // name.lexeme must be a valid class name
        if (name.category != Category.GLOBAL_CLASS && name.category != Category.LOCAL_CLASS) {
            error(name, "Invalid class name.");
        }
        Expr.Variable className = new Expr.Variable(name);

        consume(TokenType.LPAREN, "Expect '(' after class name.");
        final List<Expr> arguments = new ArrayList<Expr>();

        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expect ')' after arguments.");

        return new Expr.New(keyword, className, arguments);
    }

    private Expr superExpr() {
        Token keyword = previous();
        if (functionStack.isEmpty() || classStack.isEmpty()) {
            error(keyword, "Cannot use `super` outside of a class.");
        }    

        final String methodName = functionStack.peek();
        final Expr.Variable method = new Expr.Variable(new Token(TokenType.IDENTIFIER, methodName));
        final Token className = classStack.peek();

        return new Expr.Super(keyword, className, method);
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

    private boolean match(Category... categories) {
        for (Category category : categories) {
            if (check(category)) {
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

    private boolean check(Category category) {
        if (isAtEnd()) return false;
        return peek().category == category;
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
