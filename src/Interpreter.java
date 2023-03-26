import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment("Global");
    // define a variable to hold the current color selected by the user.
    
    // this variable will be used to colorize the output of the interpreter
    public static String currentForeColor = Hungaro.ANSI_RESET;
    public static String currentBackColor = Hungaro.ANSI_RESET;

    // interpreter stack
    // final Stack<Token> classStack = new Stack<Token>();
    
    public Environment environment = globals;

    // Objects inheritance chain
    final Environment objectEnv = new Environment(environment, "Object");
    final Environment arrayEnv = new Environment(objectEnv, "Array");
    final Environment mapEnv = new Environment(objectEnv, "Map");
    final Environment functionEnv = new Environment(objectEnv, "Function");
    final Environment stringEnv = new Environment(objectEnv, "String");
    final Environment numberEnv = new Environment(objectEnv, "Number");
    final Environment booleanEnv = new Environment(objectEnv, "Boolean");
    final Environment dateEnv = new Environment(objectEnv, "Date");
    final Environment wrapperEnv = new Environment(objectEnv, "Wrapper");
    final Environment classEnv = new Environment(objectEnv, "Class");
    final Environment stackEnv = new Environment(objectEnv, "Stack");
    final Environment queueEnv = new Environment(objectEnv, "Queue");
    final Environment regexEnv = new Environment(objectEnv, "RegEx");
    final Environment matcherEnv = new Environment(objectEnv, "Matcher");
    final Environment windowEnv = new Environment(objectEnv, "Window");
    final Environment buttonEnv = new Environment(objectEnv, "Button");

    // connection environment
    final Environment connectionEnv = new Environment(objectEnv, "Connection");

    // final Stack<Boolean> variableStack = new Stack<Boolean>();

    public Interpreter() { 
        globals.define("_VARIANT", "v");
        globals.define("_DATE", "d");
        globals.define("_DATETIME", "t");
        globals.define("_STRING", "s");
        globals.define("_ARRAY", "a");
        globals.define("_NUMBER", "n");
        globals.define("_BOOLEAN", "b");
        globals.define("_OBJECT", "o");
        globals.define("_MAP", "m");

        BuiltinsForObject.create(this);
        BuiltinsForArray.create(this);
        BuiltinsForMap.create(this);
        BuiltinsForClass.create(this);
        BuiltinsForFunction.create(this);
        BuiltinsForNative.create(this);
        BuiltinsForString.create(this);
        BuiltinsForDatabase.create(this);
        BuiltinsForStack.create(this);
        BuiltinsForQueue.create(this);
        BuiltinsForRegEx.create(this);
        BuiltinsForMatcher.create(this);
        BuiltinsForWindow.create(this);
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (Runtime.Error error) {
            Hungaro.runtimeError(error);
        }
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {        
        final Object left = evaluate(expr.left);
        final Object right = evaluate(expr.right);
        switch (expr.operator.category) {
            case NOT_EQ:
                return !isEqual(left, right);
            case EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left > (Double)right;
            case GREATER_EQ:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left >= (Double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left < (Double)right;
            case LESS_EQ:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left <= (Double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left - (Double)right;
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left + (Double)right;
                // if (left instanceof Double && right instanceof Double) {
                //     return (Double)left + (Double)right;
                // }
                // if (left instanceof String && right instanceof String) {
                //     return (String)left + (String)right;
                // }
                // throw new Runtime.RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case DIV:
                checkNumberOperands(expr.operator, left, right);
                // check for division by zero
                if ((Double)right == 0) {
                    throw new Runtime.Error(expr.operator, "Division by zero.");
                }
                return (Double)left / (Double)right;
            case MUL:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left * (Double)right;
            case MOD:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left % (Double)right;
            case POW:
                checkNumberOperands(expr.operator, left, right);
                return Math.pow((Double)left, (Double)right);
            default:
                throw new Runtime.Error(expr.operator, "Unknown operator.");
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        // if a and b are instance of Environment then extract the value and compare them
        if (a instanceof Environment && b instanceof Environment) {
            a = ((Environment)a).lookup("value");
            if (a == null)
                return false;
            b = ((Environment)b).lookup("value");
            if (b == null)
                return false;
        }
        return a.equals(b);
    }

    public boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (Boolean)object;
        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new Runtime.Error(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new Runtime.Error(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<Object>();        
        if (!(callee instanceof CallableObject)) {
            throw new Runtime.Error(expr.paren, "Can only call functions, classes and objects.");
        }        
        CallableObject function = (CallableObject)callee;

        if (function.evaluateArguments()) {
            // first argument is the poThis object
            Object evaluatedArgument = evaluate(expr.arguments.get(0));
            arguments.add(evaluatedArgument);
            // evaluate and add the rest of the arguments
            for (int i = 1; i < expr.arguments.size(); i++) {
                arguments.add(evaluate(expr.arguments.get(i)));
            }      
        } else {
            // we evaluate just the first argument which is the poThis object            
            Object evaluatedArgument = evaluate(expr.arguments.get(0));
            arguments.add(evaluatedArgument);
            // we add the rest of the arguments as is
            for (int i = 1; i < expr.arguments.size(); i++) {
                arguments.add(expr.arguments.get(i));
            }
        }

        return function.call(this, arguments);
    }

    public Environment createActivationEnvironment(Token token, Runtime.Function function, List<Object> arguments) {
        // case 1: validate the function's required parameters
        // case 2: validate the function's optional parameters vs variadic parameters
        final CallableObject.Arity arity = function.arity();        
        
        // we exclude the first argument and parameter which is the poThis object
        int required = arity.required-1;
        int argSize = arguments.size()-1;
        // <- we add them back later

        final int requiredParamsWithoutPoThis = required;

        // validate the function's required parameters
        if (required > 0 && argSize < required) {
            String message = "Expected " + required + " arguments but got " + argSize + ".";
            throw new Runtime.Error(token, message);
        }
        // validate no required parameters but arguments are provided
        if (required == 0 && argSize > 0) {
            String message = "Expected no arguments but got " + argSize + ".";
            throw new Runtime.Error(token, message);
        }

        
        // Create the activation environment
        Environment activationEnv = new Environment(function.closure, "Function call of " + function.declaration.name.lexeme);                        

        // add the poThis object to the required parameters and arguments
        required++;
        argSize++;
        // <- from here we take them into account
        
        int endIndex = required;
        if (arity.variadic)
            endIndex -= 1; // we don't add the variadic parameter to the activation environment
        
        // add the arguments to the activation environment
        for (int i = 0; i < endIndex; i++) {
            if (i < arguments.size()) {
                activationEnv.define(function.declaration.params.get(i).name.lexeme, arguments.get(i));
                checkVariableType(function.declaration.params.get(i).name, arguments.get(i), "Parameter");
                continue;
            }
            // if the argument is not provided then we add null and not check the type
            activationEnv.define(function.declaration.params.get(i).name.lexeme, null);
        }

        // validate the function's optional parameters vs variadic parameters
        if (arity.optional > 0) {
            if (arity.variadic) {
                String message = "Cannot have both optional and variadic parameters.";
                throw new Runtime.Error(token, message);
            }
            // if argSize > required + arity.optional then throw error
            if (argSize > required + arity.optional) {
                String message = "Expected at most " + (requiredParamsWithoutPoThis + arity.optional) + " arguments but got " + argSize + ".";
                throw new Runtime.Error(token, message);
            }

            if (argSize == required + arity.optional) {
                // arguments and optional parameters are equal so we don't need to add the default values
                for (int i = required; i < required + arity.optional; i++) {
                    activationEnv.define(function.declaration.params.get(i).name.lexeme, arguments.get(i));
                }
            } else {
                String parameterName = null;
                Object parameterValue = null;
                // arguments and optional parameters are not equal so we need to add the default values
                for (int i = required; i < required + arity.optional; i++) {
                    parameterName = function.declaration.params.get(i).name.lexeme;
                    parameterValue = null;
                    if (i < arguments.size())            
                        parameterValue = arguments.get(i);                        
                    else {
                        // evaluate the default value
                        parameterValue = evaluate(function.declaration.params.get(i).defaultValue);
                    }
                    checkVariableType(function.declaration.params.get(i).name, parameterValue, "Parameter");
                    activationEnv.define(parameterName, parameterValue);
                }
            }
        }
        // if the function has a variadic parameter then add the rest of the arguments to the activation environment
        if (arity.variadic) {
            // if argSize < required + 1 then throw error
            if (argSize < required + 1) {
                String message = "Expected at least " + (requiredParamsWithoutPoThis + 1) + " arguments but got " + argSize + ".";
                throw new Runtime.Error(token, message);
            }
            // create an array with the rest of the arguments
            List<Object> variadicArguments = new ArrayList<Object>();
            for (int i = required-1; i < argSize; i++) {
                variadicArguments.add(arguments.get(i));
            }
            // make the array object
            Object variadicArgumentsObject = makeObject(variadicArguments, arrayEnv, "Array");
            // add the array to the activation environment
            final String parameterName = function.declaration.params.get(required-1).name.lexeme;
            activationEnv.define(parameterName, variadicArgumentsObject);
        } else {
            // if argSize > required then throw error
            if (argSize > required) {
                String message = "Expected at most " + requiredParamsWithoutPoThis + " arguments but got " + argSize + ".";
                throw new Runtime.Error(token, message);
            }
        }

        return activationEnv;
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        // boolean mustReturnValue, 
        // Token name, 
        // List<Expr.Param> params, 
        // Block body, 
        // boolean isVariadic, 
        // int optionalParams
        final boolean mustReturnValue = true;
        final Token name = expr.token;
        final List<Expr.Param> params = expr.params;
        final List<Stmt> statements = new ArrayList<Stmt>();
        statements.add(new Stmt.Return(new Token(TokenType.RETURN, "return"), expr.body));
        final Stmt.Block body = new Stmt.Block(statements);
        final Stmt.Function declaration = new Stmt.Function(mustReturnValue, name, params, body, false, 0);
        
        return new Runtime.Function(declaration, environment);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.token.literal;              
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        final Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.LOGICAL_OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        // class instance: an instance is just an environment
        // and it's parent is the class environment (which is the class itself)

        Environment classEnv = null;

        // get the class name
        Object result = evaluate(expr.name);
        if (!(result instanceof String)) {
            String message = "Class name must be a string.";
            throw new Runtime.Error(expr.name.token, message);
        }

        // so we need to get the class environment first
        final String className = (String)result;
        if (className.length() < 2) {
            String message = "Invalid class name";
            throw new Runtime.Error(expr.name.token, message);
        }
        final char firtLetter = className.charAt(0);        
        if (firtLetter != 'g' && firtLetter != 'c') {
            String message = "Class name must start with `g` or `c`.";
            throw new Runtime.Error(expr.name.token, message);
        }
        
        if (firtLetter == 'g') {
            classEnv = (Environment)globals.lookup(className);
        } else { // it's a local class
            classEnv = (Environment)environment.lookup(className);        
        }

        // check if class exists
        if (classEnv == null) {
            String message = "Undefined class `" + className + "`.";
            throw new Runtime.Error(expr.name.token, message);
        }

        // now we can create the instance
        Environment instance = new Environment(classEnv, "Instance of " + className);

        // define the class properties in the instance
        // we need to install all properties found in the class chain (parent classes)
        Environment currentClass = classEnv;
        while (currentClass != null) {
            List<Expr.Set> properties = (List<Expr.Set>)currentClass.lookup("properties");
            if (properties == null) break;

            for (Expr.Set property : properties) {
                Object value = evaluate(property.value);
                
                // local class constants no need to check their values type.
                if (property.target.token.category != Category.LOCAL_CONSTANT) {
                    checkVariableType(property.target.token, value, "Property");
                }

                instance.define(((Expr.Variable)property.target).name.lexeme, value);
            }
            currentClass = currentClass.parent;
        }

        // next we need to evaluate the arguments
        // and the first argument is the 'this' object
        List<Object> arguments = new ArrayList<Object>();
        // the instance will 
        arguments.add(instance);
        // add the rest of the arguments
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        // now we can call the constructor (if it exists)
        // class constructor name is always 'pInit'
        CallableObject constructor = (CallableObject)classEnv.lookup("pInit");
        if (constructor != null) {
            constructor.call(this, arguments);
        } else {
            if (arguments.size() > 1) {
                String message = "Expected 0 arguments but got " + (arguments.size() - 1) + ".";
                throw new Runtime.Error(expr.name.token, message);
            }
        }

        // finally we can return the instance
        return instance;                
    }

    @Override
    public Object visitPropExpr(Expr.Prop expr) {
        Environment object = null;
        Object obj = evaluate(expr.target);

        if (obj == null) {
            throw new Runtime.Error(expr.target.token, "Cannot access properties of null.");
        } else if (obj instanceof String) {
            object = (Environment)globals.lookup("_STRING");
        } else if (obj instanceof Double) {
            object = (Environment)globals.lookup("_NUMBER");
        } else if (obj instanceof Boolean) {
            object = (Environment)globals.lookup("_BOOLEAN");
        } else if (obj instanceof Environment) {
            object = (Environment)obj;
        } else {
            throw new Runtime.Error(expr.target.token, "Cannot access properties of non-object.");
        }

        if (expr.computable) { // eg: obj[0]
            Object property = evaluate(expr.property);
            if (object.name.equals("Array")) {
                if (property instanceof Double) {
                    try {
                        return object.lookup((int)(double)(Double)property, expr.property.token);
                    } catch(Exception e){
                        return null;
                    }
                } else {
                    throw new Runtime.Error(expr.property.token, "Array index must be a number.");
                }
            }
            else if (object.name.equals("Map")) {
                return ((Map<String, Object>)object.lookup("value")).get((String)property);
            }
            return object.lookup(stringify(property), expr.property.token);
        }
        if (object.name.equals("Map")) {
            HashMap<Object, Object> map = (HashMap<Object, Object>)object.lookup("value");
            Object value = map.get(expr.property.token.lexeme);
            if (value != null) {
                return value;
            }
        }
        
        return object.lookup(expr.property.token);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        // set the value of a variable or property
        // eg. a = 1, a.b = 2, a[1] = 2
        // no computable set: eg: a.b = 2
        // computable set: eg: a[1] = 2

        String name = "Variable";
        Object value = evaluate(expr.value);
        final boolean isComplexOperator = (expr.operator.type == TokenType.COMPLEX_ASSIGN);

        if (expr.computable) { // eg. a[1] = 2, poThis.nAge = 18
            name = "Property";
            final Expr.Prop prop = (Expr.Prop)expr.target;
            final Environment instance = (Environment)evaluate(prop.target);
            Object property = prop.property.token.lexeme;
            if (prop.computable) {                
                property = evaluate(prop.property);
            }
            if (instance.name.equals("Array")) {
                if (property instanceof Double) {
                    int index = (int)(double)(Double)property;  
                    ArrayList<Object> array = (ArrayList<Object>)instance.lookup("value");
                    if (index < 0 || index >= array.size()) {                        
                        return null;
                    }
                    if (isComplexOperator) {
                        value = resolveComplexAssignment(expr.target.token, array.get(index), value, expr.operator.category);
                    }
                    return array.set(index, value);
                } else {
                    throw new Runtime.Error(prop.property.token, "Array index must be a number.");
                }
            }
            else if (instance.name.equals("Map")) {
                HashMap<Object, Object> map = (HashMap<Object, Object>)instance.lookup("value");
                if (isComplexOperator) {
                    value = resolveComplexAssignment(expr.target.token, map.get(property), value, expr.operator.category);
                }
                map.put(property, value);
                return value;
            }
            else if (instance.name.startsWith("Instance of")) {
                if (isComplexOperator) {
                    value = resolveComplexAssignment(expr.target.token, instance.lookup(property.toString()), value, expr.operator.category);
                }
                return instance.define(property.toString(), value);
            } else if (instance.name.equals("RegEx")) {
                HashMap<String, Object> map = (HashMap<String, Object>)instance.lookup("value");
                if (isComplexOperator) {
                    value = resolveComplexAssignment(expr.target.token, map.get(property.toString()), value, expr.operator.category);
                }
                map.put(property.toString(), value);
                return value;
            }
            else {
                throw new Runtime.Error(expr.target.token, "Cannot set property of non-object. `" + stringify(instance) + "`");
            }
        }

        // variable assignment eg: sName = "John"
        final Token variableToken = ((Expr.Variable)expr.target).name;        
        checkVariableType(variableToken, value, name);            
        if (isComplexOperator) {
            value = resolveComplexAssignment(variableToken, environment.lookup(variableToken.lexeme), value, expr.operator.category);                
        }
        return environment.assign(variableToken, value);
        

        // check if variable is a class property
        // if (classStack.size() > 0) {
        //     checkVariableType(variableToken, value, "Property");
        //     // define the property in the instance environment
        //     Environment instance = (Environment)environment.lookup("poThis");
        //     if (isComplexOperator) {
        //         value = resolveComplexAssignment(variableToken, instance.lookup(variableToken.lexeme), value, expr.operator.category);
        //     }
        //     return instance.define(variableToken.lexeme, value);
        // } else {
        //     checkVariableType(variableToken, value, name);            
        //     if (isComplexOperator) {
        //         value = resolveComplexAssignment(variableToken, environment.lookup(variableToken.lexeme), value, expr.operator.category);                
        //     }
        //     return environment.assign(variableToken, value);
        // }
    }

    private Object resolveComplexAssignment(Token variableToken, Object oldVal, Object newVal, Category operator) {        
        if (oldVal instanceof Double) {
            double current = (double)oldVal;
            double newValue = (double)newVal;
            switch (operator) {
                case PLUS:
                    newValue = current + newValue;
                    break;
                case MINUS:
                    newValue = current - newValue;
                    break;
                case MUL:
                    newValue = current * newValue;
                    break;
                case DIV:
                    newValue = current / newValue;
                    break;
                case MOD:
                    newValue = current % newValue;
                    break;
                default:
                    break;
            }
            return newValue;
        } // add support for String type (& operator)
        else if (oldVal instanceof String) {
            String current = (String)oldVal;
            String newValue = (String)newVal;
            switch (operator) {
                case PLUS:
                    newValue = current + newValue;
                    break;
                default:
                    break;
            }
            return newValue;
        } else {
            throw new Runtime.Error(variableToken, "This operator cannot be used with this type.");
        }
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        // get the current class in the local or global environment based on the superclass scope
        Environment superClassEnv = null;
        if (expr.className.category == Category.GLOBAL_CLASS) {
            superClassEnv = (Environment)globals.lookup(expr.className.lexeme);
        } else {
            superClassEnv = (Environment)environment.lookup(expr.className.lexeme);
        }        
        if (superClassEnv == null) {
            throw new Runtime.Error(expr.keyword, "Cannot find superclass.");
        }
        Object method = superClassEnv.lookup(expr.method.name);
        if (method == null) {
            throw new Runtime.Error(expr.method.name, "Cannot find superclass method.");
        }
        return method;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.category) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(Double)right;
            case PLUS:
                checkNumberOperand(expr.operator, right);
                return +(Double)right;
            case BANG:
                return !isTruthy(right);
            default:
                throw new Runtime.Error(expr.operator, "Unknown operator.");
        }
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookup(expr.token);       
    }

    private Object lookup(Token token) {  
        if (token.category == Category.GLOBAL_CONSTANT || token.category == Category.GLOBAL_VARIABLE) {
            return globals.lookup(token);
        }
        // else if (classStack.size() > 0) {
        //     Environment instance = (Environment)environment.lookup("poThis");
        //     if (instance == null) {
        //         return environment.lookup(token);
        //     }
        //     return instance.lookup(token.lexeme, token);
        // }
        // local identifier: variable, constant, function, procedure, parameters, etc.
        return environment.lookup(token);        
    }    

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment, "Block"));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Environment superclass = null;
        // check if superclass is a class and if not we provide the local environment
        if (stmt.superclass != null) {
            Object result = null;
            // check if superclass is local or global
            if (stmt.superclass.name.category == Category.GLOBAL_CLASS) {
                result = globals.lookup(stmt.superclass.name.lexeme, stmt.superclass.name);
            } else {
                result = environment.lookup(stmt.superclass.name.lexeme, stmt.superclass.name);
            }            
            if (!(result instanceof Environment)) {
                throw new Runtime.Error(stmt.superclass.name, "Superclass must be a class.");
            }
            superclass = (Environment)result;
        } else {
            // superclass = environment;
            superclass = classEnv;
        }
        // create a new environment for the class and provide the superclass
        final Environment classEnvironment = new Environment(superclass, "Class " + stmt.name.lexeme);
        
        // define the class properties
        classEnvironment.define("properties", stmt.properties);
        
        // evaluate the class body in the new environment

        // classStack.push(stmt.name); // interpreting inside a class
        executeBlock(stmt.body.statements, classEnvironment);
        // classStack.pop();

        // finally define the class in the global or local environment based on the category
        switch (stmt.name.category) {
            case GLOBAL_CLASS: // eg: gcPerson
                globals.define(stmt.name.lexeme, classEnvironment);
                break;
            case LOCAL_CLASS: // eg: cPerson
                environment.define(stmt.name.lexeme, classEnvironment);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {        
        final Runtime.Function function =  new Runtime.Function(stmt, environment);
        switch (stmt.name.category) {
            case GLOBAL_FUNCTION:
            case GLOBAL_PROCEDURE:            
                globals.define(stmt.name.lexeme, function);
                break;
            case LOCAL_FUNCTION:
            case LOCAL_PROCEDURE:
                environment.define(stmt.name.lexeme, function);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        final Object condition = evaluate(stmt.condition);
        if (isTruthy(condition)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        String consoleColor = "\u001B[31m;]";
        for (Expr expression : stmt.expressions) {
            System.out.print(consoleColor + stringify(evaluate(expression)));
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object returnValue = null;
        if (stmt.value != null) returnValue = evaluate(stmt.value);
        throw new Runtime.Return(returnValue);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            checkVariableType(stmt.name, value, "Variable");
        }
        if (stmt.name.lexeme.substring(0,1) == "g") {
            // global variable
            globals.define(stmt.name.lexeme, value);
        } else {
            // local variable
            environment.define(stmt.name.lexeme, value);
        }
        return null;
    }

    public void checkVariableType(Token name, Object value, String type) {                        
        char linkedType = 0;

        // constants are not checked for type (they are like variants)
        if (name.category == Category.LOCAL_CONSTANT || name.category == Category.GLOBAL_CONSTANT) {
            return;
        }
        // Local or global variables has their type in the first character
        if (name.category == Category.LOCAL_VARIABLE) {
            linkedType = name.lexeme.substring(0, 1).charAt(0);
        } else { // other variables has their type in the second character            
            linkedType = name.lexeme.substring(1, 2).charAt(0);
        }

        // if value is null then we check if the type is 'o' or 'v'
        if (value == null) {
            // if linkedType is either 'o' or 'v' then we allow null
            if (linkedType == 'o' || linkedType == 'v' || linkedType == 'd') return;
            throw new Runtime.Error(name, type + " type mismatch. Expected " + Hungaro.getTypeOf(name, linkedType) + ".");
        }
        // if linkedType is 'v' then we allow any type
        if (linkedType == 'v') return;

        // otherwise we check the type
        switch (linkedType) {
            case 's':
                if (value instanceof String) return;
                break;
            case 'n':
                if (value instanceof Double) return;
                break;
            case 'b':
                if (value instanceof Boolean) return;
                break;
            case 'a':
                if (value instanceof Environment) {
                    if (((Environment)value).name.equals("Array")) return;
                }
                break;
            case 'm':
                if (value instanceof Environment) {
                    if (((Environment)value).name.equals("Map")) return;
                }
                break;
            case 'o': // allowed types: Environment, RuntimeFunction
                if (value instanceof Environment || value instanceof Runtime.Function) return;
                break;
            case 'd':
                if (value instanceof Environment) {
                    if (((Environment)value).name.equals("Date")) return;
                }
                break;
            default:
                break;
        }
        throw new Runtime.Error(name, type + " type mismatch. Expected " + Hungaro.getTypeOf(name, linkedType) + ".");
    }    

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // execute the while body while the condition is truthy
        // consider the Exit and Loop statements
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (Runtime.Exit e) {
                break;
            } catch (Runtime.Loop l) {
                continue;
            }
        }
        return null;
    }

    public String stringify(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        else if (object instanceof Environment) {
            if (((Environment)object).name.equals("MapEntry")) {
                return stringify(((Environment)object).lookup("key")) + ": " + stringify(((Environment)object).lookup("value"));
            } 
            else if (((Environment)object).name.startsWith("Class")) {
                return ((Environment)object).name;
            }
            else if (((Environment)object).name.startsWith("Instance of")) {
                return ((Environment)object).name;
            }
            return stringify(((Environment)object).lookup("value"));           
        }
        else if (object instanceof List) {
            return arrayToString((List<Object>)object);
        }
        else if (object instanceof Map) {
            return mapToString((Map<String, Object>)object);
        }        
        else if (object instanceof java.util.Date) {
            return ((java.util.Date)object).toString();
        }
        else if (object instanceof java.util.Stack) {
            return ((java.util.Stack<Object>)object).toString();
        }
        else if (object instanceof java.util.PriorityQueue) {
            return ((java.util.PriorityQueue<Object>)object).toString();
        }
        return object.toString();
    }

    private String arrayToString(List<Object> array) {
        String result = "[";
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += stringify(array.get(i));
        }
        result += "]";
        return result;
    }

    private String mapToString(Map<String, Object> map) {
        String result = "{";
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i > 0) {
                result += ", ";
            }
            result += String.format("\"%s\": %s", entry.getKey(), stringify(entry.getValue()));
            i++;
        }
        result += "}";
        return result;
    }

    @Override
    public Void visitConstantStmt(Stmt.Constant stmt) {
        if (stmt.name.lexeme.substring(0,1) == "_") {
            // global constant
            globals.define(stmt.name.lexeme, evaluate(stmt.value));
        } else {
            // local constant
            environment.define(stmt.name.lexeme, evaluate(stmt.value));
        }
        return null;
    }

    @Override
    public Object visitArrayExpr(Expr.Array expr) {
        // fixed size array
        if (expr.fixedSize != null) {
            // evaluate the size
            Object size = evaluate(expr.fixedSize);
            // check if the size is a number
            if (!(size instanceof Double)) {
                throw new Runtime.Error(expr.fixedSize.token, "Array size must be a number.");
            }
            // check if the size is an integer
            if (((Double)size) % 1 != 0) {
                throw new Runtime.Error(expr.fixedSize.token, "Array size must be an integer.");
            }
            // check if the size is positive
            if (((Double)size) < 0) {
                throw new Runtime.Error(expr.fixedSize.token, "Array size must be positive.");
            }
            
            // convert from double to int
            int capacity = ((Double)size).intValue();

            // fill the capacity with zero
            List<Object> array = new ArrayList<Object>(capacity);
            Object initializer = null;
            if (expr.initializer != null) {
                initializer = evaluate(expr.initializer);
            }
            for (int i = 0; i < capacity; i++) {
                array.add(initializer);
            }

            // return the array
            return makeObject(array, arrayEnv, "Array");
        }

        // dynamic size array
        List<Object> array = new ArrayList<Object>();
        // evaluate each element
        for (int i = 0; i < expr.elements.size(); i++) {
            array.add(evaluate(expr.elements.get(i)));
        }
        // return the array
        return makeObject(array, arrayEnv, "Array");
    }

    public Environment makeObject(Object object, Environment parent, String name) {
        Environment objectPrototype = new Environment(parent, name);
        objectPrototype.define("value", object);
        return objectPrototype;
    }

    @Override
    public Void visitDeclareStmt(Stmt.Declare stmt) {
        for (Stmt statement : stmt.statements) {
            execute(statement);
        }
        return null;
    }

    @Override
    public Object visitMapExpr(Expr.Map expr) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        
        // evaluate each element
        for (int i = 0; i < expr.keys.size(); i++) {
            map.put(expr.keys.get(i), evaluate(expr.values.get(i)));
        }
        
        return makeObject(map, mapEnv, "Map");
    }

    @Override
    public Object visitSliceExpr(Expr.Slice expr) {
        try {
            Object target = evaluate(expr.target);
            if (!(target instanceof Environment)) {
                throw new Runtime.Error(expr.target.token, "Cannot slice a non-array.");
            }

            ArrayList<Object> array = (ArrayList<Object>)((Environment)target).lookup("value");

            Object start = evaluate(expr.start);
            // end by default gets the size of the array but convert it to Double
            Object end = Double.valueOf(array.size());
            if (expr.end != null) {
                end = evaluate(expr.end);
            }
            // check if start and end are numbers
            if (!(start instanceof Double)) {
                throw new Runtime.Error(expr.start.token, "Start index must be a number.");
            }
            if (!(end instanceof Double)) {
                throw new Runtime.Error(expr.end.token, "End index must be a number.");
            }

            int s = ((Double)start).intValue();
            int e = ((Double)end).intValue();
            return makeObject(array.subList(s, e), arrayEnv, "Array");
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        // a for statement creates an environment
        Environment forEnv = new Environment(environment, "For");
        
        Object initialValue = evaluate(stmt.start);
        if (!(initialValue instanceof Double)) {
            throw new Runtime.Error(stmt.start.token, "Start index must be a number.");
        }

        Object finalValue = evaluate(stmt.stop);
        if (!(finalValue instanceof Double)) {
            throw new Runtime.Error(stmt.stop.token, "Stop index must be a number.");
        }

        Object stepValue = 1.0;

        if (stmt.step != null) {
            stepValue = evaluate(stmt.step);
            if (!(stepValue instanceof Double)) {
                throw new Runtime.Error(stmt.step.token, "Step index must be a number.");
            }
        }

        Double start = (Double)initialValue;
        Double end = (Double)finalValue;
        Double inc = (Double)stepValue;

        if ((inc > 0 && start > end) || (inc < 0 && start < end)) {
            return null;
        }
        
        // trick to make the loop work
        // if inc is positive, we need to start at start - inc
        // if inc is negative, we need to start at start + inc
        // this is because the loop will increment the counter before checking the condition
        if (inc > 0) {
            start -= inc;
        } else {
            // if inc is negative, we need to start at start + inc so we need inc without the sign
            start += Math.abs(inc);
        }

        // define the counter
        forEnv.define(stmt.counter.lexeme, start);

        // execute the for statement block
        while (true) {
            try {
                // update counter
                start = (Double)forEnv.lookup(stmt.counter) + inc;
                forEnv.assign(stmt.counter, start);
                // check if we need to exit
                if ((inc > 0 && start > end) || (inc < 0 && start < end)) {
                    break;
                }
                // execute the block
                executeBlock(stmt.body.statements, forEnv);
            } catch(Runtime.Loop e) {
                continue;
            } catch (Runtime.Exit e) {
                break;
            }
        }        
        return null;
    }

    @Override
    public Void visitForeachStmt(Stmt.Foreach stmt) {
        // foreach: 
        // if isArray then iterate over the array keys
        // if isMap then iterate over the map keys

        // a foreach statement creates an environment
        Environment foreachEnv = new Environment(environment, "Foreach");
        // evaluate the target
        Object target = evaluate(stmt.iterable);
        if (!(target instanceof Environment)) {
            throw new Runtime.Error(stmt.iterable.token, "Invalid iterable object.");
        }
        // checkVariableType(stmt.variable.name, target, "Variable");

        // if it is an array then iterate over the array values
        // if it is a map then create an object with the key and value
        // and iterate over the map entries
        Object value = ((Environment)target).lookup("value");
        

        if (value instanceof ArrayList) {
            ArrayList<Object> array = (ArrayList<Object>)value;
            final int size = array.size();
            for (int i = 0; i < size; i++) {
                // check the type of the value against the type of the variable                
                foreachEnv.define(stmt.variable.name.lexeme, array.get(i));
                
                // execute the block
                try {
                    executeBlock(stmt.body.statements, foreachEnv);
                } catch(Runtime.Loop e) {
                    continue;
                } catch (Runtime.Exit e) {
                    break;
                }
            }
        } else if (value instanceof HashMap) {
            // we need to create the entry object with an environment
            Environment entryEnv = new Environment(objectEnv, "MapEntry");
            // now we define the key and value
            entryEnv.define("key", null);
            entryEnv.define("value", null);
            // define the value in the foreach environment
            foreachEnv.define(stmt.variable.name.lexeme, entryEnv);

            // create the object
            HashMap<String, Object> map = (HashMap<String, Object>)value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                // update the key and value
                entryEnv.define("key", entry.getKey());
                entryEnv.define("value", entry.getValue());
                // execute the block
                try {
                    executeBlock(stmt.body.statements, foreachEnv);
                } catch(Runtime.Loop e) {
                    continue;
                } catch (Runtime.Exit e) {
                    break;
                }
            }
        } else {
            throw new Runtime.Error(stmt.iterable.token, "Invalid iterable object.");
        }
        
        return null;
    }

    @Override
    public Void visitLoopStmt(Stmt.Loop stmt) {
        throw new Runtime.Loop();
    }

    @Override
    public Void visitExitStmt(Stmt.Exit stmt) {
        throw new Runtime.Exit();
    }

    @Override
    public Object visitBinaryStringExpr(Expr.BinaryString expr) {
        final Object left = evaluate(expr.left);
        // check if left is a string
        if (!(left instanceof String)) {
            throw new Runtime.Error(expr.left.token, "Left operand must be a string.");
        }
        final Object right = evaluate(expr.right);        
        // return the concatenation
        return (String)left + stringify(right);
    }

    @Override
    public Void visitRepeatStmt(Stmt.Repeat stmt) {
        // execute the block until the condition is truthy
        // NOTE: consider the Loop and Exit statements
        while (true) {
            try {
                executeBlock(stmt.body.statements, environment);
                if (isTruthy(evaluate(stmt.condition))) {
                    break;
                }
            } catch(Runtime.Loop e) {
                continue;
            } catch (Runtime.Exit e) {
                break;
            }
        }    
        return null;    
    }

    @Override
    public Void visitDeferStmt(Stmt.Defer stmt) {
        throw new Runtime.Defer(stmt.body);
    }

    @Override
    public Void visitGuardStmt(Stmt.Guard stmt) {
        // guard condition must be a boolean 
        // if it is false or generates an exception then execute the block
        try {
            if (!isTruthy(evaluate(stmt.condition))) {
                executeBlock(stmt.body.statements, environment);
            }
        } catch (Runtime.Error e) {
            executeBlock(stmt.body.statements, environment);
        }
        return null;
    }

    @Override
    public Void visitTryCatchStmt(Stmt.TryCatch stmt) {
        try {            
            Environment tryEnv = new Environment(environment, "Try");
            executeBlock(stmt.tryBlock.statements, tryEnv);
        } catch (Runtime.Error e) {            
            if (stmt.finallyBlock != null) {
                executeFinallyBlock(stmt.finallyBlock.statements);
            }            
            if (stmt.catchBlock != null) {
                // instead of bubbling up the exception we wrap it in a Wrapper object
                executeCatchBlock(stmt.catchBlock.statements, e);
            } else {
                throw e; // we need to bubble up the runtime exception
            }
        } catch (Runtime.Return e) {
            if (stmt.finallyBlock != null) {
                executeFinallyBlock(stmt.finallyBlock.statements);
            }
            throw e; // we need to bubble up the return exception
        } catch (Runtime.Exit e) {
            if (stmt.finallyBlock != null) {
                executeFinallyBlock(stmt.finallyBlock.statements);
            }
            // we dont need to throw the Exit exception            
        }

        return null;
    }

    private void executeFinallyBlock(List<Stmt> statements) {
        Environment finallyEnv = new Environment(environment, "Finally");
        executeBlock(statements, finallyEnv);
    }

    private void executeCatchBlock(List<Stmt> statements, Exception e) {
        // create the catch environment
        Environment catchEnv = new Environment(environment, "Catch");
        // create the exception object and define 'message' property.
        Environment exceptionEnv = makeObject(e, objectEnv, "Exception");
        exceptionEnv.define("message", e.getMessage());        
        // define exceptionEnv under the name 'oEx'
        catchEnv.define("oEx", exceptionEnv);
        // execute the catch block
        executeBlock(statements, catchEnv);
    }

    @Override
    public Void visitEmptyStmt(Stmt.Empty stmt) {        
        return null;
    }

    @Override
    public Void visitModuleStmt(Stmt.Module stmt) {
        // create a new environment for the module
        Environment moduleEnv = new Environment(environment, "Module");
        
        // register the module properties
        if (stmt.properties != null) {
            for (Expr.Set property : stmt.properties) {
                Object value = evaluate(property.value);
                
                // local constants in module no need to check their values type.
                if (property.target.token.category != Category.LOCAL_CONSTANT) {
                    checkVariableType(property.target.token, value, "Property");
                }
    
                moduleEnv.define(((Expr.Variable)property.target).name.lexeme, value);
            }
        }

        // execute the module block
        executeBlock(stmt.body.statements, moduleEnv);
        if (stmt.name.category == Category.GLOBAL_MODULE) {            
            globals.define(stmt.name.lexeme, moduleEnv);
        } else {
            environment.define(stmt.name.lexeme, moduleEnv);
        }

        return null;     
    }
}