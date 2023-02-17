import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment("Global");
    
    // Objects inheritance chain
    final Environment objectEnv = new Environment("Object");
    final Environment arrayEnv = new Environment(objectEnv, "Array");
    final Environment mapEnv = new Environment(objectEnv, "Map");
    final Environment stringEnv = new Environment(objectEnv, "String");
    final Environment numberEnv = new Environment(objectEnv, "Number");
    final Environment booleanEnv = new Environment(objectEnv, "Boolean");

    final Stack<Boolean> variableStack = new Stack<Boolean>();

    private Environment environment = globals;

    public Interpreter() {
        globals.define("_VERSION", "0.1.1");
        // global _STRING builtin function
        globals.define("_STRING", new Environment(stringEnv, "String"));
        // global _ARRAY builtin function
        globals.define("_ARRAY", new Environment(arrayEnv, "Array"));
        // global _MAP builtin function
        globals.define("_MAP", new Environment(mapEnv, "Map"));
        // global _OBJECT builtin function
        globals.define("_OBJECT", new Environment(objectEnv, "Object"));

        /***********************************************************************
         * Object
         ***********************************************************************/
        // object toString builtin function
        objectEnv.define("toString", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return stringify(arguments.get(0));
            }
        });
        /***********************************************************************
         * Array
         ***********************************************************************/
        // array push builtin function: eg. array.push(1)
        arrayEnv.define("push", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    array.add(arguments.get(1));
                }
                return null;
            }
        });

        // array pop builtin function
        arrayEnv.define("pop", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.remove(array.size() - 1);
                }
                return null;
            }
        });

        // array len builtin function
        arrayEnv.define("len", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.size();
                }
                return 0;
            }
        });

        // array get builtin function
        arrayEnv.define("get", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double index = (Double)arguments.get(1);
                        int i = index.intValue();                
                        return array.get(i);
                    } catch(Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // array set builtin function
        arrayEnv.define("set", new CallableObject() {
            @Override
            public int arity() {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double index = (Double)arguments.get(1);
                        int i = index.intValue();                
                        array.set(i, arguments.get(2));
                    } catch(Exception e) {
                        return null;
                    }                    
                }
                return null;
            }
        });

        // array slice builtin function
        arrayEnv.define("slice", new CallableObject() {
            @Override
            public int arity() {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double start = (Double)arguments.get(1);
                        Double end = (Double)arguments.get(2);
                        int s = start.intValue();
                        int e = end.intValue();
                        return makeArray(array.subList(s, e));
                    } catch(Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // array join builtin function
        arrayEnv.define("join", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    String separator = (String)arguments.get(1);
                    String result = String.join(separator, array.stream().map(Object::toString).toArray(String[]::new));
                    return result;
                }
                return null;
            }
        });

        /***********************************************************************
         * Map
         ***********************************************************************/
        // map get builtin function
        mapEnv.define("get", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return map.get(arguments.get(1));
                }
                return null;
            }
        });

        // map set builtin function
        mapEnv.define("set", new CallableObject() {
            @Override
            public int arity() {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    map.put(arguments.get(1), arguments.get(2));
                }
                return null;
            }
        });

        // map len builtin function
        mapEnv.define("len", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return map.size();
                }
                return 0;
            }
        });

        // map keys builtin function
        mapEnv.define("keys", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return makeArray(new ArrayList<Object>(map.keySet()));                    
                }
                return null;
            }
        });

        // map values builtin function
        mapEnv.define("values", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return makeArray(new ArrayList<Object>(map.values()));                    
                }
                return null;
            }
        });

        // map contains builtin function
        mapEnv.define("contains", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return map.containsKey(arguments.get(1));
                }
                return false;
            }
        });

        // map remove builtin function
        mapEnv.define("remove", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    return map.remove(arguments.get(1));
                }
                return null;
            }
        });

        // map clear builtin function
        mapEnv.define("clear", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("map");
                    map.clear();
                }
                return null;
            }
        });

        /***********************************************************************
         * String
         ***********************************************************************/
        // string len builtin function
        stringEnv.define("len", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return ((String)arguments.get(1)).length();
            }
        });
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Hungaro.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
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
                if (left instanceof Double && right instanceof Double) {
                    return (Double)left + (Double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case DIV:
                checkNumberOperands(expr.operator, left, right);
                // check for division by zero
                if ((Double)right == 0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return (Double)left / (Double)right;
            case MUL:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left * (Double)right;
            case MOD:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left % (Double)right;
            default:
                throw new RuntimeError(expr.operator, "Unknown operator.");
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (Boolean)object;
        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<Object>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof CallableObject)) {
            throw new RuntimeError(expr.paren, "Can only call functions, classes and objects.");
        }

        CallableObject function = (CallableObject)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {        
        switch (expr.token.type) {
            case STRING:
                return makeString((String)expr.token.literal);
            case NUMBER:
                return makeNumber((Double)expr.token.literal);
            case TRUE:
            case FALSE:
                return makeBoolean((Boolean)expr.token.literal);
            default:
                return expr.token.literal;
        }        
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
        return null;
    }

    @Override
    public Object visitPropExpr(Expr.Prop expr) {
        Environment object = null;
        if (expr.target instanceof Expr.Literal) {
            switch (expr.target.token.type) {
                case STRING:
                    object = (Environment)globals.lookup("_STRING");
                    break;
                case NUMBER:
                    object = (Environment)globals.lookup("_NUMBER");
                    break;
                case TRUE:
                case FALSE:
                    object = (Environment)globals.lookup("_BOOLEAN");
                    break;
                case NULL:
                    throw new RuntimeError(expr.target.token, "Cannot access properties of null.");
                default:
                    break;
            }
        } else {
            // retrieve the object
            variableStack.push(true);
            object = (Environment)evaluate(expr.target);
            variableStack.pop();
        }        
        if (expr.computable) {
            Object property = evaluate(expr.property);
            return object.lookup(stringify(property), expr.property.token);
        }
        return object.lookup(expr.property.token);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        if (expr.computable) {            
            return null;
        }
        return environment.assign(((Expr.Variable)expr.target).name, evaluate(expr.value));
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        return null;
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
                throw new RuntimeError(expr.operator, "Unknown operator.");
        }
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        if (variableStack.isEmpty()) { // retrieve the internal "value" of the object
            Object obj = lookup(expr.token);
            if (obj instanceof Environment) {
                return ((Environment)obj).lookup("value");
            }
            throw new RuntimeError(expr.token, "Cannot access properties of " + obj + ".");
        } else { // retrieve the object itself
            return lookup(expr.token);
        }
    }

    private Object lookup(Token token) {  
        if (Hungaro.isConstant(token.lexeme)) {
            return getConstant(token.lexeme);
        }
        return environment.lookup(token);
    }    

    private Object getConstant(String name) {
        if (name.charAt(0) == '_') {
            return globals.lookup(name);
        }
        return environment.lookup(name);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(null, environment, "block"));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        for (Expr expression : stmt.expressions) {
            System.out.println(stringify(evaluate(expression)));
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            validateVariableType(stmt.name, value);
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

    private void validateVariableType(Token name, Object value) {
        if (value == null) return;
        
        char type = name.lexeme.substring(1, 2).charAt(0);
        switch (type) {
            case 'n':
                if (!(value instanceof Double)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Number.");
                }
                break;
            case 's':
                if (!(value instanceof Environment) || !((Environment)value).name.equals("String")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected String.");
                }
                break;
            case 'b':
                if (!(value instanceof Environment) || !((Environment)value).name.equals("Boolean")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Boolean.");
                }
                break;
            case 'a':
                if (!(value instanceof Environment) || !((Environment)value).name.equals("value")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Array.");
                }
                break;
            case 'm':
                if (!(value instanceof Environment) || !((Environment)value).name.equals("Map")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Map.");
                }
                break;
            case 'o':
                if (!(value instanceof Environment)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Object.");
                }
                if (!((Environment)value).name.equals("Object")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Object.");
                }
                break;
            default:
                throw new RuntimeError(name, "Invalid variable type.");
        }        
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    private String stringify(Object object) {
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
            switch (((Environment)object).name) {
                case "Array":
                    return arrayToString((List<Object>)((Environment)object).lookup("value"));
                case "Map":
                    return mapToString((Map<String, Object>)((Environment)object).lookup("value"));
                default:
                    return "Object";
            }
        }
        else if (object instanceof String) {
            return String.format("\"%s\"", object);            
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
        // create an array
        List<Object> array = new ArrayList<Object>();
        // evaluate each element
        for (int i = 0; i < expr.elements.size(); i++) {
            array.add(evaluate(expr.elements.get(i)));
        }
        // return the array
        return makeArray(array);        
    }

    private Environment makeArray(List<Object> array) {
        Environment arrayPrototype = new Environment(arrayEnv, "Array");
        arrayPrototype.define("value", array);
        return arrayPrototype;
    }

    private Environment makeString(String string) {
        Environment stringPrototype = new Environment(stringEnv, "String");
        stringPrototype.define("value", string);
        return stringPrototype;
    }

    private Environment makeNumber(Double number) {
        Environment numberPrototype = new Environment(numberEnv, "Number");
        numberPrototype.define("value", number);
        return numberPrototype;
    }

    private Environment makeBoolean(Boolean bool) {
        Environment booleanPrototype = new Environment(booleanEnv, "Boolean");
        booleanPrototype.define("value", bool);
        return booleanPrototype;
    }

    private Environment makeMap(Map<String, Object> map) {
        Environment mapPrototype = new Environment(mapEnv, "Map");
        mapPrototype.define("map", map);
        return mapPrototype;
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
        // create a map
        Map<String, Object> map = new HashMap<String, Object>();
        // evaluate each element
        for (int i = 0; i < expr.keys.size(); i++) {
            map.put(expr.keys.get(i).token.lexeme, evaluate(expr.values.get(i)));
        }
        // return the map
        return makeMap(map);

        // Copilot you are a genius
        // I'm not sure if I should be proud or ashamed :)
    }
}