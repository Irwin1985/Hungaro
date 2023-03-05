import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    // parser constants
    private static final String INVALID_PARAMETER_NAME = "Invalid parameter name: parameters must follow the following rule: first letter `p` match the parameter and second letter match the type: \n1. `s` for string.\n2. `n` for number.\n3. `b` for boolean.\n4. `a` for array.\n5. `m` for map.\n6. `o` for object.\n";

    private int current = 0;
    private List<Token> tokens;
    
    // stacks for semantic validation
    private final Stack<Token> functionStack = new Stack<Token>();      // for function keyword
    private final Stack<Boolean> finallyStack = new Stack<Boolean>();   // for finally keyword    
    private final Stack<Token> superStack = new Stack<Token>();         // for super keyword
    private final Stack<Token> classStack = new Stack<Token>();         // for class keyword
    private final Stack<Boolean> deferStack = new Stack<Boolean>();     // for defer keyword
    private final Stack<Boolean> loopStack = new Stack<Boolean>();      // for loop keyword

    private static class ParseError extends RuntimeException {}

    /*******************************************************************************************
    * Constructor
    ********************************************************************************************/
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    /*******************************************************************************************
    * Parse the source code and return a list of statements
    ********************************************************************************************/
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }
    /*******************************************************************************************
    * Declaration handler
    ********************************************************************************************/
    private Stmt declaration() {
        try {
            if (match(TokenType.DEF)) return defStatement();
            if (match(TokenType.LET)) return letStatement();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    /*******************************************************************************************
    * Parse def statement
    ********************************************************************************************/
    private Stmt defStatement() {
        final Token keyword = previous();
        final Token identifier = consume(TokenType.IDENTIFIER, "Expect variable name.");

        switch (identifier.category) {
            // Function declaration
            case LOCAL_FUNCTION:
            case GLOBAL_FUNCTION:
                return functionDeclaration(keyword, identifier, "function", true);

            // Procedure declaration
            case LOCAL_PROCEDURE:
            case GLOBAL_PROCEDURE:
                return functionDeclaration(keyword, identifier, "procedure", true);

            // Class declaration
            case LOCAL_CLASS:
            case GLOBAL_CLASS:
                return classDeclaration(keyword, identifier);
            
            // Module declaration
            case LOCAL_MODULE:
            case GLOBAL_MODULE:
                return moduleDeclaration(keyword, identifier);
            
            default:
                error(identifier, "Invalid declarator name: are you trying to define a variable? if so please use `let` instead of `def`.\nAlso please check the following naming rules:\nGLOBAL SCOPE: your identifier must start with a `g` letter followed by it's name.\nOTHER RULES:\n*All identifiers must follow the camel case naming convention. e.g: pSetAge\n*All identifiers must start with a lowercase letter.\n*All identifiers must be at least 2 characters long.\n*All identifiers must be unique within the scope.\n*All identifiers must be unique within the program.\n*All identifiers must be unique within the class.\n");
        }

        return null;
    }
    /*******************************************************************************************
    * letStatement
    ********************************************************************************************/
    private Stmt letStatement() {
        final Token keyword = previous();
        final List<Stmt> statements = new ArrayList<Stmt>();
        Token identifier = null;
        do {
            match(TokenType.SEMICOLON); // optional semi colon
            identifier = consume(TokenType.IDENTIFIER, "Expect variable name.");
            if (check(TokenType.END)) break;

            switch (identifier.category) {
                case GLOBAL_CONSTANT:
                case LOCAL_CONSTANT:
                    statements.add(constantDeclaration(keyword, identifier));
                    break;
                case GLOBAL_VARIABLE:
                case LOCAL_VARIABLE:
                    statements.add(variableDeclaration(keyword, identifier));
                    break;
                default:
                    error(identifier, "Invalid name: please check the following naming rules:\nGLOBAL SCOPE:the first letter must be `g`.\nOTHER RULES:\n*You can omite the scoping suffix (first letter) if you are declaring a constant (all uppercase).\n*All identifiers must follow the camel case naming convention. e.g: pnPersonAge\n*All identifiers must start with a lowercase letter.\n*All identifiers must be at least 2 characters long.\n*All identifiers must be unique within the scope.\n*All identifiers must be unique within the program.\n*All identifiers must be unique within the class.\n");
            }
        } while (match(TokenType.COMMA) && !isAtEnd());
        
        return new Stmt.Declare(keyword, statements);
    }

    private Stmt constantDeclaration(Token keyword, Token identifier) {
        consume(TokenType.SIMPLE_ASSIGN, "Expect `=` after constant declaration.");
        final Expr value = expression();

        // if it is semi colon, consume it
        if (!check(TokenType.COMMA) && !check(TokenType.END)) {
            consume(TokenType.SEMICOLON, "Expect new line after constant declaration.");
        }

        return new Stmt.Constant(keyword, identifier, value);
    }
    /*******************************************************************************************
    * variableDeclaration
    ********************************************************************************************/
    private Stmt variableDeclaration(Token keyword, Token identifier) {
        Expr value = null;
        if (match(TokenType.SIMPLE_ASSIGN)) {            
            value = expression();
        } else {
            // provide default value based on second letter of identifier
            if (identifier.category == Category.GLOBAL_CONSTANT || identifier.category == Category.LOCAL_CONSTANT) {
                error(identifier, "Constants must be initialized.");
            }
            switch (identifier.lexeme.charAt(0)) {
                case 'd': // date
                    value = null;
                    break;
                case 't': // date and time
                    value = null;
                    break;
                case 'v': // variant
                    value = null;
                    break;
                case 's': // string
                    value = new Expr.Literal(new Token(TokenType.STRING, ""));
                    break;
                case 'a': // array
                    value = new Expr.Array(null, null, null, null);
                    break;
                case 'n': // number
                    value = new Expr.Literal(new Token(TokenType.NUMBER, 0.0));
                    break;
                case 'b': // boolean
                    value = new Expr.Literal(new Token(TokenType.FALSE, false));
                    break;
                case 'o': // object         
                    break;
                case 'm': // map
                    value = new Expr.Map(null, null, null);
                    break;
                default:
                    error(identifier, "Invalid variable type: variables must follow the following rule: first letter `l` or `g` match the variable scope and second letter match the type: \n1. `v` for variant\n2.`s` for string.\n2. `a` for array.\n4. `n` for number.\n5. `b` for boolean.\n6. `o` for object.\n7. `m` for map.\n");
            }
        }

        if (!check(TokenType.COMMA) && !check(TokenType.END)) {
            consume(TokenType.SEMICOLON, "Expect new line after variable declaration.");
        }
        
        return new Stmt.Var(keyword, identifier, value);
    }
    /*******************************************************************************************
    * functionDeclaration
    ********************************************************************************************/
    private Stmt functionDeclaration(Token keyword, Token identifier, String name, boolean parseBody) {
        List<Expr.Param> parameters = new ArrayList<Expr.Param>();
        Stmt.Block body = new Stmt.Block(new ArrayList<Stmt>());
        boolean mustReturnValue = identifier.category == Category.GLOBAL_FUNCTION || identifier.category == Category.LOCAL_FUNCTION;

        // we always pass 'poThis' as first parameter
        parameters.add(new Expr.Param(new Token(TokenType.PARAMETER, "poThis", Category.PARAMETER)));
        
        Token param = null;
        Expr paramDefaultValue = null;
        List<Integer> optionalParamIndexes = new ArrayList<Integer>();
        boolean isVariadic = false;

        if (match(TokenType.LPAREN)) {
            if (!check(TokenType.RPAREN)) {
                do {
                    param = consume(TokenType.PARAMETER, INVALID_PARAMETER_NAME);
                    paramDefaultValue = null;
                    if (param.category == Category.VARIADIC) {
                        isVariadic = true;
                        // if variadic parameter is not the last parameter then error
                        if (peek().type != TokenType.RPAREN) {
                            error(param, "Variadic parameter must be the last parameter.");
                        }                        
                        // remove the variadic prefix from the parameter name e.g. `...`
                        param.lexeme = param.lexeme.substring(3);
                        param.literal = param.lexeme;
                    } else {                        
                        if (match(TokenType.SIMPLE_ASSIGN)) {
                            // optional parameters are not allowed when variadic parameter is used
                            if (isVariadic) {
                                error(param, "Optional parameters are not allowed when variadic parameter is used.");
                            }
                            paramDefaultValue = expression();
                        }
                    }
                    if (paramDefaultValue != null) {
                        optionalParamIndexes.add(parameters.size());
                    }
                    parameters.add(new Expr.Param(param, paramDefaultValue));
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RPAREN, "Expect `)` after parameters.");                        
        }

        // if there is optional parameter before variadic parameter then error
        if (optionalParamIndexes.size() > 0 && isVariadic) {
            error(param, "Optional parameters are not allowed when variadic parameter is used.");
        }

        // we need to validate the sequence of optional parameters got in optionalParamIndexes
        // if we find a hole in the sequence then error
        int previousIndex = 0;
        for (int i = 0; i < optionalParamIndexes.size(); i++) {
            int index = optionalParamIndexes.get(i);
            if (i == 0) {
                previousIndex = index;
                continue;
            }
            if (index - previousIndex != 1) {
                error(parameters.get(index-1).name, "Optional parameters must be the last parameters.");
            }
            previousIndex = index;            
        }

        if (parseBody) {            
            functionStack.push(identifier); // fName | gfName | lfName | pName | gpName | lpName
            body = block();
            functionStack.pop();
        } else {
            // the new line is mandatory
            consume(TokenType.SEMICOLON, "Expect new line after abstract method declaration.");
        }

        return new Stmt.Function(mustReturnValue, identifier, parameters, body, isVariadic, optionalParamIndexes.size());
    }
    /*******************************************************************************************
    * Block
    ********************************************************************************************/
    private Stmt.Block block() {
        List<Stmt> statements = new ArrayList<Stmt>();
        consume(TokenType.SEMICOLON, "Expect new line before block declaration.");

        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(TokenType.END, "Expect `end` after block declaration.");

        if (!check(TokenType.COMMA) && !check(TokenType.END)) {
            consume(TokenType.SEMICOLON, "Expect new line after block declaration.");
        }

        return new Stmt.Block(statements);
    }
    /*******************************************************************************************
    * Class
    ********************************************************************************************/
    private Stmt classDeclaration(Token keyword, Token identifier) {        
        Expr.Variable superClass = null;        
        final List<Stmt> statements = new ArrayList<Stmt>();
        final List<Expr.Set> properties = new ArrayList<Expr.Set>();
        
        classStack.push(identifier); // entering in parsing class
        
        // check if class is extending another class
        if (match(TokenType.AS)) {
            final Token superClassName = consume(TokenType.IDENTIFIER, "Expect superclass name.");
            // cannot inherit from itself
            if (superClassName.lexeme.equals(identifier.lexeme)) {
                error(superClassName, "A class cannot inherit from itself.");
            }
            superStack.push(superClassName); // push the inherited class token
            superClass = new Expr.Variable(superClassName);
        }
        match(TokenType.SEMICOLON); // optional semicolon

        // parse class properties (global variables are not allowed. Use the constructor instead)        
        while (check(Category.LOCAL_VARIABLE) || check(Category.LOCAL_CONSTANT)) {
            // only allow set property: eg: nAge = 10
            Stmt.Expression exp = (Stmt.Expression)expressionStmt();
            if (!(exp.expression instanceof Expr.Set)) {
                error(exp.token, "Invalid class property.");
            }
            properties.add((Expr.Set)exp.expression);
            match(TokenType.SEMICOLON); // optional semicolon
        }

        // parse class methods: 'def' keyword is omitted but global functions 
        // or procedures are not allowed. Use the constructor instead
        String name = "";
        boolean parseBody = true;

        while (match(Category.MINUS, Category.LOCAL_FUNCTION, Category.LOCAL_PROCEDURE)) {
            if (previous().category == Category.LOCAL_FUNCTION) {
                name = "class function";
            }
            else if (previous().category == Category.LOCAL_PROCEDURE) {
                name = "class procedure";
            }
            else if (previous().category == Category.MINUS) { // eg: -fName([args]) or -pName([args])
                // abstract methods does not implement the body. just name and parameters
                parseBody = false;
                continue;
            }
            statements.add((Stmt.Function) functionDeclaration(new Token(TokenType.DEF, "def"), previous(), name, parseBody));
            parseBody = true; // reset the flag
        }        
        match(TokenType.SEMICOLON); // optional semicolon
        consume(TokenType.END, "Expect `end` after class declaration.");

        if (!check(TokenType.COMMA) && !check(TokenType.END)) {
            consume(TokenType.SEMICOLON, "Expect new line after `end` keyword.");
        }
        
        if (superClass != null) {
            superStack.pop(); // pop the class name
        }
        
        classStack.pop(); // exiting from parsing class

        return new Stmt.Class(keyword, identifier, superClass, properties, new Stmt.Block(statements));
    }
    /*******************************************************************************************
    * Module
    ********************************************************************************************/
    private Stmt moduleDeclaration(Token keyword, Token identifier) {        
        final List<Stmt> statements = new ArrayList<Stmt>();
        final List<Expr.Set> properties = new ArrayList<Expr.Set>();
                
        match(TokenType.SEMICOLON); // optional semicolon

        // parse module properties (locals and globals are allowed)
        while (check(Category.LOCAL_VARIABLE) || check(Category.GLOBAL_VARIABLE) || check(Category.LOCAL_CONSTANT) || check(Category.GLOBAL_CONSTANT)) {
            // only allow set property: eg: a = 10
            Stmt.Expression exp = (Stmt.Expression)expressionStmt();
            if (!(exp.expression instanceof Expr.Set)) {
                error(exp.token, "Invalid property.");
            }
            properties.add((Expr.Set)exp.expression);
            match(TokenType.SEMICOLON); // optional semicolon
        }

        // parse module methods
        String name = "";

        while (match(Category.LOCAL_FUNCTION, Category.LOCAL_PROCEDURE)) {
            name = "module procedure";
            if (previous().category == Category.LOCAL_FUNCTION) {
                name = "module function";
            }            
            statements.add((Stmt.Function) functionDeclaration(new Token(TokenType.DEF, "def"), previous(), name, true));
        }        
        match(TokenType.SEMICOLON); // optional semicolon
        consume(TokenType.END, "Expect `end` after module declaration.");

        if (!check(TokenType.COMMA) && !check(TokenType.END)) {
            consume(TokenType.SEMICOLON, "Expect new line after `end` keyword.");
        }

        return new Stmt.Module(keyword, identifier, properties, new Stmt.Block(statements));
    }
    /*******************************************************************************************
    * Statement handler
    ********************************************************************************************/
    private Stmt statement() {
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.FOR)) return parseForStatement();
        if (match(TokenType.LOOP)) return loopStatement();
        if (match(TokenType.EXIT)) return exitStatement();
        if (match(TokenType.REPEAT)) return repeatStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.DEFER)) return deferStatement();
        if (match(TokenType.GUARD)) return guardStatement();
        if (match(TokenType.TRY)) return tryCatchStatement();
        if (match(TokenType.SEMICOLON)) return new Stmt.Empty(previous());
        return expressionStmt();
    }
    /*******************************************************************************************
    * Return statement
    ********************************************************************************************/
    private Stmt returnStatement() {
        // cannot return from top-level code
        if (functionStack.isEmpty()) {
            error(previous(), "Cannot return from top-level code.");
        }

        // cannot return from a procedures
        if (functionStack.peek().category == Category.LOCAL_PROCEDURE || functionStack.peek().category == Category.GLOBAL_PROCEDURE) {
            error(previous(), "`return` statement is not allowed in procedures. Please use `exit` statement instead.");
        }

        // cannot return from a finally block
        if (finallyStack.size() > 0) {
            error(previous(), "Cannot return from a finally block.");
        }

        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.END)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expect new line after return statement.");
        return new Stmt.Return(keyword, value);
    }
    /*******************************************************************************************
    * If statement
    ********************************************************************************************/
    private Stmt ifStatement() {
        final Expr condition = expression();
        consume(TokenType.SEMICOLON, "Expect new line before `if` block.");
        List<Stmt> thenStmt = new ArrayList<Stmt>();
        Stmt.Block elseBranch = null;

        // then branch
        while (!check(TokenType.ELSE) && !check(TokenType.END) && !isAtEnd()) {
            thenStmt.add(declaration());
        }
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
    /*******************************************************************************************
    * For statement handler
    ********************************************************************************************/
    private Stmt parseForStatement() {
        loopStack.push(true); // push true to indicate that we are in a for loop
        // for i = 1 to 10 | for each i in array
        Token keyword = previous();
        Stmt forStmt = null;
        if (match(TokenType.EACH)) {
            forStmt = foreachStatement(keyword);
        } else {
            forStmt = forStatement(keyword);
        }
        loopStack.pop(); // pop the for loop
        return forStmt;
    }
    /*******************************************************************************************
    * For each statement
    ********************************************************************************************/
    private Stmt foreachStatement(Token keyword) {
        Expr.Variable variable = null;
        Expr iterable = null;
        Stmt.Block body = null;        
        
        Token token = consume(TokenType.IDENTIFIER, "Expect variable name after `for each`.");
        final char firstLetter = token.lexeme.charAt(0);
        // variable lexeme must start with 'l' and followed by 'v' for variant or 'o' for object
        if (firstLetter != 'v' && firstLetter != 'o') {
            error(token, "Invalid variable name in `for each` statement.\nPlease use either `v` for variant or `o` for object.");
        }
        
        variable = new Expr.Variable(token);
        consume(TokenType.IN, "Expect `in` after `for each` counter.");
        iterable = expression();
        body = block();

        return new Stmt.Foreach(keyword, variable, iterable, body);
    }
    /*******************************************************************************************
    * For statement
    ********************************************************************************************/
    private Stmt forStatement(Token keyword) {        
        Token counter = null;
        Expr start = null;
        Expr stop = null;
        Expr step = null;
        Stmt.Block body = null;        
        
        counter = consume(TokenType.IDENTIFIER, "Expect variable name after `for`.");
        final char firstLetter = counter.lexeme.charAt(0);
        // variable lexeme must start with 'ln' for local number or 'lv' for local variant
        if (firstLetter != 'n' && firstLetter != 'v') {
            error(counter, "Invalid variable name in `for` statement.\nPlease use either `ln` for number or `lv` for variant.");
        }
        consume(TokenType.SIMPLE_ASSIGN, "Expect `=` after `for` counter.");
        start = expression();

        consume(TokenType.TO, "Expect `to` after `for` counter.");
        stop = expression();
        if (match(TokenType.STEP)) {
            step = expression();
        }

        body = block();

        return new Stmt.For(keyword, counter, start, stop, step, body);
    }
    /*******************************************************************************************
    * Loop
    ********************************************************************************************/
    private Stmt loopStatement() {
        // throw error if we are not in a loop
        if (loopStack.isEmpty()) {
            error(previous(), "Cannot use `loop` outside of a loop.");
        }
        // eat new line
        consume(TokenType.SEMICOLON, "Expect new line after `loop` keyword.");
        return new Stmt.Loop(previous());
    }
    /*******************************************************************************************
    * Exit
    ********************************************************************************************/
    private Stmt exitStatement() {
        // only loop and procedures can use exit
        if (loopStack.isEmpty() && functionStack.isEmpty()) {
            error(previous(), "Cannot use `exit` outside of a loop or procedure.");
        }

        // if functionStack is a function, throw error
        if (!functionStack.isEmpty() && (functionStack.peek().category == Category.LOCAL_FUNCTION || functionStack.peek().category == Category.GLOBAL_FUNCTION)) {
            if (loopStack.isEmpty()) {
                error(previous(), "Cannot use `exit` in a function.");
            }            
        }
        
        // eat new line
        consume(TokenType.SEMICOLON, "Expect new line after `exit` keyword.");
        return new Stmt.Exit(previous());
    }
    /*******************************************************************************************
    * Repeat
    ********************************************************************************************/
    private Stmt repeatStatement() {
        loopStack.push(true); // push true to indicate that we are in a repeat loop
        Token keyword = previous();
        List<Stmt> statements = new ArrayList<Stmt>();
        // eat new line
        consume(TokenType.SEMICOLON, "Expect new line after `repeat` keyword.");
        do {
            statements.add(declaration());
        } while (!check(TokenType.UNTIL) && !isAtEnd());        
        consume(TokenType.UNTIL, "Expect `until` after `repeat` block.");
        Stmt.Block body = new Stmt.Block(statements);

        Expr condition = expression();        
        consume(TokenType.SEMICOLON, "Expect new line after `until`.");
        loopStack.pop(); // pop the repeat loop
        return new Stmt.Repeat(keyword, body, condition);
    }
    /*******************************************************************************************
    * While
    ********************************************************************************************/
    private Stmt whileStatement() {
        loopStack.push(true); // push true to indicate that we are in a while loop
        Token keyword = previous();
        final Expr condition = expression();
        // final boolean parseRightParen = match(TokenType.LPAREN);
        // Expr condition = expression();
        // if (parseRightParen) {
        //     consume(TokenType.RPAREN, "Expect `)` after `while` condition.");
        // }
        Stmt.Block body = block();
        loopStack.pop(); // pop the while loop
        return new Stmt.While(keyword, condition, body);
    }
    /*******************************************************************************************
    * Defer
    ********************************************************************************************/
    private Stmt deferStatement() {
        // defer are found in function and procedures
        if (functionStack.isEmpty()) {
            error(previous(), "Cannot use `defer` outside of function or procedure.");
        }
        // if deferStack is not empty, we are already in a defer block
        if (!deferStack.isEmpty()) {
            error(previous(), "Cannot use `defer` inside another `defer` block.");
        }
        
        // notify we are in defer block
        deferStack.push(true);
        Token keyword = previous();
        Stmt.Block body = block();
        // notify we are out of defer block
        deferStack.pop();
        return new Stmt.Defer(keyword, body);
    }
    /*******************************************************************************************
    * Guard
    ********************************************************************************************/
    private Stmt guardStatement() {
        // guard are found in function and procedures
        if (functionStack.isEmpty()) {
            error(previous(), "Cannot use `guard` outside of function or procedure.");
        }
        // if deferStack is not empty, we are already in a defer block
        if (!deferStack.isEmpty()) {
            error(previous(), "Cannot use `guard` inside `defer` block.");
        }
        
        Token keyword = previous();
        final Expr condition = expression();
        consume(TokenType.ELSE, "Expect `else` after `guard` condition.");

        final Stmt.Block body = block();

        return new Stmt.Guard(keyword, condition, body);
    }
    /*******************************************************************************************
    * Try Catch Finally
    ********************************************************************************************/
    private Stmt tryCatchStatement() {
        final Token keyword = previous();
        match(TokenType.SEMICOLON); // eat new line
        final List<Stmt> tryStatements = new ArrayList<Stmt>();
        final List<Stmt> catchStatements = new ArrayList<Stmt>();        
        Stmt.Block finallyBlock = null;

        // parse try statements until we find a catch or finally
        while (!check(TokenType.CATCH) && !check(TokenType.FINALLY) && !isAtEnd()) {
            tryStatements.add(declaration());
        }
        
        // parse catch statements until we find a finally or end
        if (match(TokenType.CATCH)) {
            match(TokenType.SEMICOLON); // eat new line
            while (!check(TokenType.FINALLY) && !check(TokenType.END) && !isAtEnd()) {
                catchStatements.add(declaration());
            }
            if (match(TokenType.END)) {
                consume(TokenType.SEMICOLON, "Expect new line after `end` keyword.");
            }
        }

        // parse finally statements
        if (match(TokenType.FINALLY)) {
            finallyStack.push(true); // push true to indicate that we are in a finally block
            finallyBlock = block();
            finallyStack.pop(); // pop the finally block
        }

        return new Stmt.TryCatch(keyword, new Stmt.Block(tryStatements), new Stmt.Block(catchStatements), finallyBlock);
    }    
    /*******************************************************************************************
    * Expression Statement
    ********************************************************************************************/
    private Stmt expressionStmt() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect new line after expression.");
        return new Stmt.Expression(expr);
    }
    /*******************************************************************************************
    * Expression
    ********************************************************************************************/
    private Expr expression() {
        return assignment();
    }
    /*******************************************************************************************
    * Assignment
    ********************************************************************************************/
    private Expr assignment() {
        Expr target = logicalOr();        
        if (peek().type == TokenType.SIMPLE_ASSIGN || peek().type == TokenType.COMPLEX_ASSIGN) {
            Token equals = advance();
            Expr value = assignment();

            if (target instanceof Expr.Variable) {
                // check if variable is 'poThis' which results in an error
                if (((Expr.Variable) target).name.lexeme.equals("poThis")) {
                    error(target.token, "Cannot assign to `poThis`.");
                }                
                Category targetCategory = ((Expr.Variable) target).name.category;
                if (targetCategory == Category.GLOBAL_CONSTANT || targetCategory == Category.LOCAL_CONSTANT) {
                    // constant assignment is not allowed outside of function/procedure
                    if (functionStack.isEmpty()) {
                        error(target.token, "Cannot assign to constant.");
                    }
                }
                return new Expr.Set(equals, target, value, false);
            } else if (target instanceof Expr.Prop) {
                return new Expr.Set(equals, target, value, true);
            }

            error(target.token, "Invalid assignment target.");
        }

        return target;
    }
    /*******************************************************************************************
    * Logical Or
    ********************************************************************************************/
    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.LOGICAL_OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Logical And
    ********************************************************************************************/
    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.LOGICAL_AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Equality
    ********************************************************************************************/
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.EQU_OPE)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Comparison
    ********************************************************************************************/
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.REL_OPE)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Term
    ********************************************************************************************/
    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.TERM)) {
            Token operator = previous();
            Expr right = factor();
            // if operator is AMPERSAND then we are concatenating strings
            if (operator.lexeme.equals("&")) {
                expr = new Expr.BinaryString(expr, operator, right);
            } else {
                expr = new Expr.Binary(expr, operator, right);
            }
        }

        return expr;
    }
    /*******************************************************************************************
    * Factor
    ********************************************************************************************/
    private Expr factor() {
        Expr expr = exponent();

        while (match(TokenType.FACTOR)) {
            Token operator = previous();
            Expr right = exponent();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Exponent
    ********************************************************************************************/
    private Expr exponent() {
        Expr expr = unary();

        while (match(TokenType.EXPONENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    /*******************************************************************************************
    * Unary
    ********************************************************************************************/
    private Expr unary() {
        if (match(TokenType.LOGICAL_NOT, TokenType.TERM)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }
    /*******************************************************************************************
    * Call
    ********************************************************************************************/
    private Expr call() {
        Expr left = primary();

        while (true) {
            if (match(TokenType.LPAREN)) {
                left = finishCall(left);
            } 
            else if (match(TokenType.DOT)) {
                Token keyword = previous();
                Token propIdent = consume(TokenType.IDENTIFIER, "Invalid property name.");
                Expr property = new Expr.Variable(propIdent);
                left = new Expr.Prop(keyword, left, property, false);
            }
            else if (match(TokenType.LBRACKET)) {
                Token keyword = previous();
                // add support for array slicing eg. foo[1:2], foo[:2], foo[1:]
                if (match(TokenType.COLON)) {
                    // foo[:2]
                    Token start = new Token(TokenType.NUMBER, 0);
                    Expr startExpr = new Expr.Literal(start);
                    Expr endExpr = expression();
                    consume(TokenType.RBRACKET, "Expect ']' after expression.");
                    left = new Expr.Slice(keyword, left, startExpr, endExpr);                    
                } else {
                    Expr indexOrkey = expression();
                    if (match(TokenType.COLON)) {
                        // foo[1:] or foo[1:2]
                        Expr startExpr = indexOrkey;
                        Expr endExpr = null;
                        if (!check(TokenType.RBRACKET)) {
                            endExpr = expression();
                        }
                        consume(TokenType.RBRACKET, "Expect ']' after expression.");
                        left = new Expr.Slice(keyword, left, startExpr, endExpr);
                    } else {
                        // array or map indexing eg. foo[1] or foo["bar"]
                        consume(TokenType.RBRACKET, "Expect ']' after expression.");
                        left = new Expr.Prop(keyword, left, indexOrkey, true);
                    }
                }
            }
            else {
                break;
            }
        }

        return left;
    }
    /*******************************************************************************************
    * Finish Call
    ********************************************************************************************/
    private Expr finishCall(Expr callee) {  
        Token paren = previous();      
        final List<Expr> arguments = new ArrayList<Expr>();
        
        // check if the calle is a Prop expression.
        if (callee instanceof Expr.Prop) {
            // add the target as the first argument. eg: foo.bar(1,2,3) -> bar(foo, 1, 2, 3)            
            arguments.add(((Expr.Prop)callee).target);
        } else if (callee instanceof Expr.Super) {
            // add 'poThis' pointer as the first argument. eg: super(poThis)
            final Token token = new Token(TokenType.PARAMETER, "poThis", Category.PARAMETER);
            arguments.add(new Expr.Variable(token));            
        }
        else if (callee instanceof Expr.Variable) {
            if (classStack.isEmpty()) { // eg: pWalk(1, 2) -> pWalk(pWalk, 1, 2)
                arguments.add(callee);
            } else { // eg: pWalk(1, 2) -> pWalk(poThis, 1, 2)
                final Token poThisToken = new Token(TokenType.PARAMETER, "poThis", Category.PARAMETER);
                final Token name = ((Expr.Variable)callee).name;
                arguments.add(new Expr.Variable(poThisToken));
                // calee must change to a Prop expression. eg: pWalk() => poThis.pWalk()                
                callee = new Expr.Prop(
                        new Token(TokenType.DOT), 
                        new Expr.Variable(poThisToken), 
                        new Expr.Variable(name), 
                        false);
            }
        }
        
        // parse the arguments
        match(TokenType.SEMICOLON); // optional semicolon
        if (!check(TokenType.RPAREN)) {
            do {
                match(TokenType.SEMICOLON); // optional semicolon
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        match(TokenType.SEMICOLON); // optional semicolon
        consume(TokenType.RPAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }
    /*******************************************************************************************
    * Parse a map expression.
    ********************************************************************************************/
    private Expr primary() {
        if (peek().category == Category.LITERAL) {
            return new Expr.Literal(advance());
        }
        else if (match(TokenType.IDENTIFIER, TokenType.PARAMETER)) {
            return new Expr.Variable(previous());
        }
        else if (match(TokenType.THIS)) {
            return thisExpr();
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
        else if (match(TokenType.LAMBDA)) {
            return lambda();
        }

        throw error(peek(), "Expect expression.");
    }
    /*******************************************************************************************
    * This expression.
    ********************************************************************************************/
    private Expr thisExpr() {
        if (classStack.isEmpty()) {
            throw error(previous(), "Cannot refer variable instance outside of a class.");
        }
        final Token dotToken = new Token(TokenType.DOT);
        final Expr.Variable property = new Expr.Variable(previous());
        final Token poThisToken = new Token(TokenType.PARAMETER, "poThis", Category.PARAMETER);
        final Expr.Variable poThis = new Expr.Variable(poThisToken);
        final Expr.Prop prop = new Expr.Prop(dotToken, poThis, property, false);

        return prop;
    }
    /*******************************************************************************************
    * Parse a map expression.
    ********************************************************************************************/
    private Expr array() {
        Token keyword = previous();
        List<Expr> elements = new ArrayList<Expr>();
        // get the first 3 tokens
        Token t1 = peek();
        Token t2 = peek(1);        
        // check if those tokens shapes a: ]*number
        if (t1.type == TokenType.RBRACKET && t2.category == Category.MUL) {
            // consume the first 3 tokens
            advance();
            advance();
            
            // parse the size expression
            final Expr expSize = term();
            Expr initializer = null;
            if (match(TokenType.SIMPLE_ASSIGN)) {
                initializer = expression();
            }
            
            return new Expr.Array(keyword, elements, expSize, initializer);
        }

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
        return new Expr.Array(keyword, elements, null, null);
    }
    /*******************************************************************************************
    * Map literal
    ********************************************************************************************/
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
    /*******************************************************************************************
    * newExpr() parses the following grammar:
    ********************************************************************************************/
    private Expr newExpr() {
        // the 'new' keyword
        Token keyword = previous();
        Expr className = null;
        boolean eatLeftParen = true;
        // class name
        Token name = consume(TokenType.IDENTIFIER, "Expect class name after 'new'.");
        if (name.lexeme.equals("object")) {
            consume(TokenType.LPAREN, "Expect '(' after Object constructor.");
            eatLeftParen = false;
            className = expression();
            match(TokenType.COMMA); 
            // eat ',' if there are more arguments eg: new object("class", arg1, arg2, ...)
        } else {
            // name.lexeme must be a valid class name
            if (name.category != Category.GLOBAL_CLASS && name.category != Category.LOCAL_CLASS) {
                error(name, "Invalid class name.");
            }
            className = new Expr.Literal(name);
        }


        // Expr.Variable className = new Expr.Variable(name);
        if (eatLeftParen)
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
    /*******************************************************************************************
    * Parses the super expression.
    ********************************************************************************************/
    private Expr superExpr() {
        Token keyword = previous();
        if (functionStack.isEmpty() || superStack.isEmpty()) {
            error(keyword, "Cannot use `super` outside of a class.");
        }    

        final String methodName = functionStack.peek().lexeme;
        final Expr.Variable method = new Expr.Variable(new Token(TokenType.IDENTIFIER, methodName));
        final Token className = superStack.peek();

        return new Expr.Super(keyword, className, method);
    }

    /*******************************************************************************************
    * Lambda expression
    ********************************************************************************************/
    private Expr lambda() {
        final Token keyword = previous();
        final List<Expr.Param> parameters = new ArrayList<Expr.Param>();
        match(TokenType.SEMICOLON); // optional semicolon
        consume(TokenType.LPAREN, "Expect '(' after 'lambda'.");
        match(TokenType.SEMICOLON); // optional semicolon
        if (!check(TokenType.RPAREN)) {
            do {                
                match(TokenType.SEMICOLON); // optional semicolon
                if (check(TokenType.RPAREN)) break; // allow trailing comma
                Token param = consume(TokenType.PARAMETER, "Expect parameter name.");                
                parameters.add(new Expr.Param(param));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expect ')' after parameters.");

        consume(TokenType.COLON, "Expect ':' after parameters.");        
        final Expr body = expression();

        return new Expr.Lambda(keyword, parameters, body);        
    }


    /*******************************************************************************************
    * Checks if the current token is of the given type.
    ********************************************************************************************/
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
    /*******************************************************************************************
    * Checks if the current token is of the given type.
    ********************************************************************************************/
    private boolean match(Category... categories) {
        for (Category category : categories) {
            if (check(category)) {
                advance();
                return true;
            }
        }

        return false;
    }
    /*******************************************************************************************
    * Returns the current token.
    ********************************************************************************************/
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }
    /*******************************************************************************************
    * Checks if the current token is of the given type.
    ********************************************************************************************/
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    /*******************************************************************************************
    * Checks if the current token is of the given category.
    ********************************************************************************************/
    private boolean check(Category category) {
        if (isAtEnd()) return false;
        return peek().category == category;
    }
    /*******************************************************************************************
    * Returns the current token and advances the current token.
    ********************************************************************************************/
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    /*******************************************************************************************
    * Checks if the current token is the end of the file.
    ********************************************************************************************/
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    /*******************************************************************************************
    * Returns the current token.
    ********************************************************************************************/
    private Token peek() {
        return tokens.get(current);
    }
    /*******************************************************************************************
    * Returns the token at the given offset from the current token.
    ********************************************************************************************/
    private Token peek(int offset) {
        // check if the offset is out of bounds, if so, return EOF
        if (current + offset >= tokens.size()) {
            return new Token(TokenType.EOF, "");
        }
        return tokens.get(current + offset);
    }
    /*******************************************************************************************
    * Returns the previous token.
    ********************************************************************************************/
    private Token previous() {
        return tokens.get(current - 1);
    }
    /*******************************************************************************************
    * Error handling
    ********************************************************************************************/
    private ParseError error(Token token, String message) {
        Hungaro.error(token, message);
        return new ParseError();
    }
    /*******************************************************************************************
    * Synchronize the parser after an error.
    ********************************************************************************************/    
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case RETURN:
                case IF:
                case FOR:
                case REPEAT:
                case WHILE:
                case DEF:
                case LET:
                    return;
            }

            advance();
        }
    }

}