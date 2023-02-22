import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment("Global");
    
    // Objects inheritance chain
    final Environment objectEnv = new Environment("Object");
    final Environment arrayEnv = new Environment(objectEnv, "Array");
    final Environment mapEnv = new Environment(objectEnv, "Map");
    final Environment functionEnv = new Environment(objectEnv, "Function");
    final Environment stringEnv = new Environment(objectEnv, "String");
    final Environment numberEnv = new Environment(objectEnv, "Number");
    final Environment booleanEnv = new Environment(objectEnv, "Boolean");
    final Environment dateEnv = new Environment(objectEnv, "Date");

    // final Stack<Boolean> variableStack = new Stack<Boolean>();

    public Environment environment = globals;

    public Interpreter() {
        globals.define("_VERSION", "0.1.1");
        // global _STRING builtin function
        globals.define("_STRING", new Environment(stringEnv, "String"));
        // global _NUMBER builtin function
        globals.define("_NUMBER", new Environment(numberEnv, "Number"));
        // global _ARRAY builtin function
        globals.define("_ARRAY", new Environment(arrayEnv, "Array"));
        // global _MAP builtin function
        globals.define("_MAP", new Environment(mapEnv, "Map"));
        // global _OBJECT builtin function
        globals.define("_OBJECT", new Environment(objectEnv, "Object"));        


        // date() builtin function
        globals.define("date", new CallableObject() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Environment date = new Environment(dateEnv, "Date");
                date.define("value", new java.util.Date());
                return date;
            }
        });

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
                // print the internal value if object is an instance of Environment
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // get the "value" property and print it
                    Object value = env.lookup("value");
                    return stringify(value);
                }
                return stringify(arguments.get(0));
            }
        });

        // object type builtin function: return the name of the Environment
        objectEnv.define("type", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    return env.name;
                }
                return "Object";
            }
        });

        /***********************************************************************
        * Array
        ***********************************************************************/
        // concat builtin function: an array takes another array and call makeObject() to create a new array
        arrayEnv.define("concat", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment && arguments.get(1) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    Environment env2 = (Environment)arguments.get(1);
                    ArrayList<Object> array2 = (ArrayList<Object>)env2.lookup("value");
                    ArrayList<Object> newArray = new ArrayList<Object>();
                    newArray.addAll(array);
                    newArray.addAll(array2);
                    return makeObject(newArray, arrayEnv, "Array");
                }
                return null;
            }
        });

        // array indexOf builtin function
        arrayEnv.define("indexOf", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.indexOf(arguments.get(1));
                }
                return null;
            }
        });

        // array lastIndexOf builtin function
        arrayEnv.define("lastIndexOf", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.lastIndexOf(arguments.get(1));
                }
                return null;
            }
        });

        // array contains builtin function
        arrayEnv.define("contains", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.contains(arguments.get(1));
                }
                return null;
            }
        });

        // array isEmpty builtin function
        arrayEnv.define("isEmpty", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.isEmpty();
                }
                return null;
            }
        });

        // array sort builtin function: sort the array in ascending order
        arrayEnv.define("sort", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // check if the first element is a string
                    if (array.get(0) instanceof String) {
                        // all elements must be strings
                        for (int i = 0; i < array.size(); i++) {
                            if (!(array.get(i) instanceof String)) {
                                throw new RuntimeError(null, "Array elements must be of the same type");
                            }
                        }
                        // convert the array to an ArrayList of strings
                        ArrayList<String> stringArray = new ArrayList<String>();
                        for (int i = 0; i < array.size(); i++) {
                            stringArray.add((String)array.get(i));
                        }
                        // sort the array
                        Collections.sort(stringArray);
                        // define the new array in the "value" of the array environment
                        env.define("value", stringArray);
                    }
                    else if (array.get(0) instanceof Double) {
                        // all elements must be numbers
                        for (int i = 0; i < array.size(); i++) {
                            if (!(array.get(i) instanceof Double)) {
                                throw new RuntimeError(null, "Array elements must be of the same type");
                            }
                        }
                        // convert the array to an ArrayList of numbers
                        ArrayList<Double> numberArray = new ArrayList<Double>();
                        for (int i = 0; i < array.size(); i++) {
                            numberArray.add((Double)array.get(i));
                        }
                        // sort the array
                        Collections.sort(numberArray);
                        // define the new array in the "value" of the array environment
                        env.define("value", numberArray);
                    }
                    else {
                        throw new RuntimeError(null, "Array elements must be of the same type and either numbers or strings");
                    }
                }
                return null;
            }
        });

        // array reverse builtin function
        arrayEnv.define("reverse", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    Collections.reverse(array);
                    // define the new array in the "value" of the array environment
                    env.define("value", array);
                }
                return null;
            }
        });

        // array first() builtin function
        arrayEnv.define("first", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    return array.get(0);
                }
                return null;
            }
        });

        // array last() builtin function
        arrayEnv.define("last", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    return array.get(array.size() - 1);
                }
                return null;
            }
        });

        // array remove() removes the first occurrence of the given element
        arrayEnv.define("remove", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    // if the element is not in the array, return null
                    if (!array.contains(arguments.get(1))) {
                        return null;
                    }
                    array.remove(arguments.get(1));
                    return arguments.get(1);
                }
                return null;
            }
        });

        // array removeAt() builtin function
        arrayEnv.define("removeAt", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    // if the index is out of bounds, return null
                    if ((int)(double)(Double)arguments.get(1) < 0 || (int)(double)(Double)arguments.get(1) >= array.size()) {
                        return null;
                    }
                    return array.remove((int)(double)(Double)arguments.get(1));
                }
                return null;
            }
        });

        // array insertAt() builtin function
        arrayEnv.define("insertAt", new CallableObject() {
            @Override
            public int arity() {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the index is out of bounds, return null
                    if ((int)(double)(Double)arguments.get(1) < 0 || (int)(double)(Double)arguments.get(1) >= array.size()) {
                        return null;
                    }
                    array.add((int)(double)(Double)arguments.get(1), arguments.get(2));
                }
                return null;
            }
        }); 
        
        // array occurs() builtin function: returns the number of times the given element occurs in the array
        arrayEnv.define("occurs", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    int count = 0;
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).equals(arguments.get(1))) {
                            count++;
                        }
                    }
                    return count;
                }
                return null;
            }
        });
        
        // array takes() builtin function: returns a new array containing the first n elements of the given array
        // if the argument is negative then the last n elements are returned
        arrayEnv.define("take", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    ArrayList<Object> newArray = new ArrayList<Object>();
                    if ((int)(double)(Double)arguments.get(1) < 0) {
                        for (int i = array.size() + (int)(double)(Double)arguments.get(1); i < array.size(); i++) {
                            newArray.add(array.get(i));
                        }
                    } else {
                        for (int i = 0; i < (int)(double)(Double)arguments.get(1); i++) {
                            newArray.add(array.get(i));
                        }
                    }
                    return newArray;
                }
                return null;
            }
        });        

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
                    return Double.valueOf(array.size());
                }
                return 0.0;
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
                        return makeObject(array.subList(s, e), arrayEnv, "Array");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return makeObject(new ArrayList<Object>(map.keySet()), arrayEnv, "Array");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return makeObject(new ArrayList<Object>(map.values()), arrayEnv, "Array");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
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
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return ((String)arguments.get(0)).length();
            }
        });

        /***********************************************************************
        * Function prototype
        ***********************************************************************/
        // function arity builtin function: extract the internal "value" which is the 
        // RuntimeFunction object and call its arity() method
        functionEnv.define("arity", new CallableObject() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    RuntimeFunction function = (RuntimeFunction)env.lookup("value");
                    // arity is the number of parameters, not including the "poThis" parameter
                    return function.arity()-1;
                }
                return 0;
            }
        }); 
        /***********************************************************************
        * Built-in functions
        ***********************************************************************/
        // input builtin function: read a line from the console
        globals.define("input", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the prompt
                System.out.print(arguments.get(1));                
                return Hungaro.readLine();
            }
        });

        // val() builtin function: convert a string to a number
        globals.define("val", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                final Object arg = arguments.get(1);
                if (arg instanceof String) {
                    return Double.parseDouble((String)arg);
                }
                return arg;
            }
        });

        // type() builtin function: return the type of an object eg: 
        globals.define("type", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                final Object arg = arguments.get(1);
                if (arg instanceof String) {
                    return "s";
                } else if (arg instanceof Double) {
                    return "n";
                } else if (arg instanceof Boolean) {
                    return "b";
                } else if (arg instanceof Environment) {
                    return ((Environment)arg).name;
                }
                return "u";
            }
        });

        // print builtin function: print a string to the console
        globals.define("print", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the string to print
                System.out.print(stringify(arguments.get(1)));
                return null;
            }
        });

        // println builtin function: print a string to the console with a newline
        globals.define("println", new CallableObject() {
            @Override
            public int arity() {
                return 2;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the string to print
                System.out.println(stringify(arguments.get(1)));
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

    public void execute(Stmt stmt) {
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
                checkNumberOperands(expr.operator, left, right);
                return (Double)left + (Double)right;
                // if (left instanceof Double && right instanceof Double) {
                //     return (Double)left + (Double)right;
                // }
                // if (left instanceof String && right instanceof String) {
                //     return (String)left + (String)right;
                // }
                // throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
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
            case POW:
                checkNumberOperands(expr.operator, left, right);
                return Math.pow((Double)left, (Double)right);
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
        int firstArg = 0;
        for (Expr argument : expr.arguments) {            
            Object evaluatedArgument = evaluate(argument);
            // if the first argument is an instance of RuntimeFunction then 
            // we need to create an object with the functionEnv as the environment            
            if (firstArg == 0 && evaluatedArgument instanceof RuntimeFunction) {
                evaluatedArgument = makeObject(evaluatedArgument, functionEnv, "Function");
            }
            firstArg++;
            arguments.add(evaluatedArgument);
        }
        if (!(callee instanceof CallableObject)) {
            throw new RuntimeError(expr.paren, "Can only call functions, classes and objects.");
        }

        CallableObject function = (CallableObject)callee;
        if (arguments.size() != function.arity()) {
            String message = "Expected " + (function.arity()-1) + " arguments but got " + (arguments.size()-1) + ".";
            // if arity is 0 then throw unexpected arguments error
            if (function.arity()-1 == 0) {
                message = "Expected no arguments but got " + (arguments.size()-1) + ".";
            }
            throw new RuntimeError(expr.paren, message);
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
        // so we need to get the class environment first
        if (expr.name.name.category == Category.GLOBAL_CLASS) {
            classEnv = (Environment)globals.lookup(expr.name.name.lexeme);
        } else { // it's a local class
            classEnv = (Environment)environment.lookup(expr.name.name.lexeme);        
        }

        // now we can create the instance
        Environment instance = new Environment(classEnv, "Instance of " + expr.name.name.lexeme);

        // define the class properties in the instance
        // we need to install all properties found in the class chain (parent classes)
        Environment currentClass = classEnv;
        while (currentClass != null) {
            List<Expr.Set> properties = (List<Expr.Set>)currentClass.lookup("properties");
            if (properties == null) break;

            for (Expr.Set property : properties) {
                Object value = evaluate(property.value);
                checkVariableType(property.target.token, value, "Property");
                instance.define(((Expr.Variable)property.target).name.lexeme, value);
            }
            currentClass = currentClass.parent;
        }

        // next we need to evaluate the arguments
        // and the first argument is the 'this' object
        List<Object> arguments = new ArrayList<Object>();
        // add the 'this' object
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
        }

        // finally we can return the instance
        return instance;                
    }

    @Override
    public Object visitPropExpr(Expr.Prop expr) {
        Environment object = null;
        Object obj = evaluate(expr.target);

        if (obj == null) {
            throw new RuntimeError(expr.target.token, "Cannot access properties of null.");
        } else if (obj instanceof String) {
            object = (Environment)globals.lookup("_STRING");
        } else if (obj instanceof Double) {
            object = (Environment)globals.lookup("_NUMBER");
        } else if (obj instanceof Boolean) {
            object = (Environment)globals.lookup("_BOOLEAN");
        } else if (obj instanceof Environment) {
            object = (Environment)obj;
        } else {
            throw new RuntimeError(expr.target.token, "Cannot access properties of non-object.");
        }

        if (expr.computable) {
            Object property = evaluate(expr.property);
            if (object.name.equals("Array")) {
                if (property instanceof Double) {
                    try {
                        return object.lookup((int)(double)(Double)property, expr.property.token);
                    } catch(Exception e){
                        return null;
                    }
                } else {
                    throw new RuntimeError(expr.property.token, "Array index must be a number.");
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
        final Object value = evaluate(expr.value);
        if (expr.computable) { // eg. a[1] = 2
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
                        // throw new RuntimeError(prop.property.token, "Array index out of bounds.");
                        return null;
                    }
                    return array.set(index, value);
                } else {
                    throw new RuntimeError(prop.property.token, "Array index must be a number.");
                }
            }
            else if (instance.name.equals("Map")) {
                HashMap<Object, Object> map = (HashMap<Object, Object>)instance.lookup("value");
                map.put(property, value);
                return value;
            }
            return null;
        }
        
        // check variable or property value
        Token variableToken = ((Expr.Variable)expr.target).name;

        // check if variable is a class property
        if (variableToken.category == Category.CLASS_PROPERTY) {
            checkVariableType(variableToken, value, "Property");
            // define the property in the instance environment
            Environment instance = (Environment)environment.lookup("poThis");
            // if (instance == null) {
            //     // define the property in current environment
            //     return environment.define(variableToken.lexeme, value);                
            // }
            return instance.define(variableToken.lexeme, value);            
        } else {
            checkVariableType(variableToken, value, name);
            return environment.assign(variableToken, value);
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
            throw new RuntimeError(expr.keyword, "Cannot find superclass.");
        }
        Object method = superClassEnv.lookup(expr.method.name);
        if (method == null) {
            throw new RuntimeError(expr.method.name, "Cannot find superclass method.");
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
                throw new RuntimeError(expr.operator, "Unknown operator.");
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
        else if (token.category == Category.CLASS_PROPERTY) {
            Environment instance = (Environment)environment.lookup("poThis");
            if (instance == null) {
                return environment.lookup(token);
            }
            return instance.lookup(token.lexeme, token);
        }
        // local identifier: variable, constant, function, procedure, parameters, etc.
        return environment.lookup(token);        
    }    

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(null, environment, "block"));
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
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
            superclass = (Environment)result;
        } else {
            superclass = environment;
        }
        // create a new environment for the class and provide the superclass
        final Environment classEnvironment = new Environment(null, superclass, "Class " + stmt.name.lexeme);
        
        // define the class properties
        classEnvironment.define("properties", stmt.properties);
        
        // evaluate the class body in the new environment
        executeBlock(stmt.body.statements, classEnvironment);

        // finally define the class in the global or local environment based on the category
        switch (stmt.name.category) {
            case GLOBAL_CLASS: // eg: gcPerson
                globals.define(stmt.name.lexeme, classEnvironment);
                break;
            case LOCAL_CLASS: // eg: lcPerson
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
        final RuntimeFunction function =  new RuntimeFunction(stmt, environment);
        switch (stmt.name.category) {
            case GLOBAL_FUNCTION:
            case GLOBAL_PROCEDURE:            
                globals.define(stmt.name.lexeme, function);
                break;
            case LOCAL_FUNCTION:
            case LOCAL_PROCEDURE:
            case CLASS_FUNCTION:
            case CLASS_PROCEDURE:
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
        for (Expr expression : stmt.expressions) {
            System.out.print(stringify(evaluate(expression)));
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object returnValue = null;
        if (stmt.value != null) returnValue = evaluate(stmt.value);
        throw new Return(returnValue);
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
        
        // get the type of the variable 
        // the type for variable starts at the second character and for properties at the first        
        if (type.equals("Property")) {
            linkedType = name.lexeme.substring(0, 1).charAt(0);    
        } else {
            linkedType = name.lexeme.substring(1, 2).charAt(0);
        }

        // if value is null then we check if the type is 'o' or 'v'
        if (value == null) {
            // if linkedType is either 'o' or 'v' then we allow null
            if (linkedType == 'o' || linkedType == 'v' || linkedType == 'd') return;
            throw new RuntimeError(name, type + " type mismatch. Expected " + Hungaro.getTypeOf(name, linkedType) + ".");
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
            case 'o':
                if (value instanceof Environment) return;
                break;
            case 'd':
                if (value instanceof Environment) {
                    if (((Environment)value).name.equals("Date")) return;
                }
                break;
            default:
                break;
        }
        throw new RuntimeError(name, type + " type mismatch. Expected " + Hungaro.getTypeOf(name, linkedType) + ".");
    }    

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // execute the while body while the condition is truthy
        // consider the Exit and Loop statements
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (Exit e) {
                break;
            } catch (Loop l) {
                continue;
            }
        }
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
        return makeObject(array, arrayEnv, "Array");
    }

    private Environment makeObject(Object object, Environment parent, String name) {
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
                throw new RuntimeError(expr.target.token, "Cannot slice a non-array.");
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
                throw new RuntimeError(expr.start.token, "Start index must be a number.");
            }
            if (!(end instanceof Double)) {
                throw new RuntimeError(expr.end.token, "End index must be a number.");
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
            throw new RuntimeError(stmt.start.token, "Start index must be a number.");
        }

        Object finalValue = evaluate(stmt.stop);
        if (!(finalValue instanceof Double)) {
            throw new RuntimeError(stmt.stop.token, "Stop index must be a number.");
        }

        Object stepValue = 1.0;

        if (stmt.step != null) {
            stepValue = evaluate(stmt.step);
            if (!(stepValue instanceof Double)) {
                throw new RuntimeError(stmt.step.token, "Step index must be a number.");
            }
        }

        Double start = (Double)initialValue;
        Double end = (Double)finalValue;
        Double inc = (Double)stepValue;

        if ((inc > 0 && start > end) || (inc < 0 && start < end)) {
            return null;
        }
        
        // trick to make the loop work
        if (inc > 0)
            start -= inc;
        else
            start += inc;

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
            } catch(Loop e) {
                continue;
            } catch (Exit e) {
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
            throw new RuntimeError(stmt.iterable.token, "Invalid iterable object.");
        }
        // if it is an array then iterate over the array values
        // if it is a map then create an object with the key and value
        // and iterate over the map entries
        Object value = ((Environment)target).lookup("value");
        if (value instanceof ArrayList) {
            ArrayList<Object> array = (ArrayList<Object>)value;
            for (int i = 0; i < array.size(); i++) {
                // check the type of the value against the type of the variable                
                foreachEnv.define(stmt.variable.name.lexeme, array.get(i));
                
                // execute the block
                try {
                    executeBlock(stmt.body.statements, foreachEnv);
                } catch(Loop e) {
                    continue;
                } catch (Exit e) {
                    break;
                }
            }
        } else if (value instanceof HashMap) {
            // we need to create the entry object with an environment
            Environment entryEnv = new Environment(null, "MapEntry");
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
                } catch(Loop e) {
                    continue;
                } catch (Exit e) {
                    break;
                }
            }
        } else {
            throw new RuntimeError(stmt.iterable.token, "Invalid iterable object.");
        }
        
        return null;
    }

    @Override
    public Void visitLoopStmt(Stmt.Loop stmt) {
        throw new Loop();
    }

    @Override
    public Void visitExitStmt(Stmt.Exit stmt) {
        throw new Exit();
    }

    @Override
    public Object visitBinaryStringExpr(Expr.BinaryString expr) {
        final Object left = evaluate(expr.left);
        // check if left is a string
        if (!(left instanceof String)) {
            throw new RuntimeError(expr.left.token, "Left operand must be a string.");
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
            } catch(Loop e) {
                continue;
            } catch (Exit e) {
                break;
            }
        }    
        return null;    
    }

    @Override
    public Void visitDeferStmt(Stmt.Defer stmt) {
        throw new Defer(stmt.body);
    }

    @Override
    public Void visitGuardStmt(Stmt.Guard stmt) {
        // guard condition must be a boolean 
        // if it is false or generates an exception then execute the block
        try {
            if (!isTruthy(evaluate(stmt.condition))) {
                executeBlock(stmt.body.statements, environment);
            }
        } catch (RuntimeError e) {
            executeBlock(stmt.body.statements, environment);
        }
        return null;
    }
}