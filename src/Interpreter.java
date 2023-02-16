import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment("Global");
    
    // Objects inheritance chain
    final Environment objectEnv = new Environment("Object");
    final Environment arrayEnv = new Environment(objectEnv, "Array");
    final Environment mapEnv = new Environment(objectEnv, "Map");

    private Environment environment = globals;

    public Interpreter() {
        globals.define("_VERSION", "0.1.1");

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

        // array push builtin function
        arrayEnv.define("push", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
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
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("array");
                    String separator = (String)arguments.get(1);
                    String result = String.join(separator, array.stream().map(Object::toString).toArray(String[]::new));
                    return result;
                }
                return null;
            }
        });

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
        return null;
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
        return expr.token.literal;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        return null;
    }

    @Override
    public Object visitPropExpr(Expr.Prop expr) {
        Environment object = (Environment)evaluate(expr.target);        
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
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookup(expr.token);
    }

    private Object lookup(Token token) {        
        return environment.lookup(token);
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
                if (!(value instanceof String)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected String.");
                }
                break;
            case 'b':
                if (!(value instanceof Boolean)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Boolean.");
                }
                break;
            case 'a':
                if (!(value instanceof Environment)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Array.");
                }
                if (!((Environment)value).name.equals("Array")) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Array.");
                }
                break;
            case 'm':
                if (!(value instanceof Environment)) {
                    throw new RuntimeError(name, "Variable type mismatch. Expected Map.");
                }
                if (!((Environment)value).name.equals("Map")) {
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
                    return arrayToString((List<Object>)((Environment)object).lookup("array"));
                case "Map":
                    return mapToString((Map<String, Object>)((Environment)object).lookup("map"));
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
        // Environment arrayPrototype = new Environment(arrayEnv, "Array");
        List<Object> array = new ArrayList<Object>();
        // arrayPrototype.define("array", array);
        for (int i = 0; i < expr.elements.size(); i++) {
            array.add(evaluate(expr.elements.get(i)));
        }
        return makeArray(array);
        // return arrayPrototype;
    }

    private Environment makeArray(List<Object> array) {
        Environment arrayPrototype = new Environment(arrayEnv, "Array");
        arrayPrototype.define("array", array);
        return arrayPrototype;
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
        Environment objectPrototype = new Environment(mapEnv, "Map");
        Map<String, Object> map = new HashMap<String, Object>();
        // fill map
        for (int i = 0; i < expr.keys.size(); i++) {
            map.put(expr.keys.get(i).token.lexeme, evaluate(expr.values.get(i)));
        }
        objectPrototype.define("map", map);

        return objectPrototype;
    }
}
