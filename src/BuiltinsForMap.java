import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public final class BuiltinsForMap {
    public static void create(Interpreter interpreter) {
        // map put builtin function
        interpreter.mapEnv.define("put", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
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
            
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map get builtin function
        interpreter.mapEnv.define("get", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
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

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map set builtin function
        interpreter.mapEnv.define("set", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
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

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map len builtin function
        interpreter.mapEnv.define("len", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return (double)map.size();
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map keys builtin function
        interpreter.mapEnv.define("keys", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new ArrayList<Object>(map.keySet()), interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map values builtin function
        interpreter.mapEnv.define("values", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new ArrayList<Object>(map.values()), interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map contains builtin function
        interpreter.mapEnv.define("contains", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
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

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map remove builtin function
        interpreter.mapEnv.define("remove", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
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

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map clear builtin function
        interpreter.mapEnv.define("clear", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
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

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map isEmpty builtin function
        interpreter.mapEnv.define("isEmpty", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return map.isEmpty();
                }
                return true;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map clone builtin function
        interpreter.mapEnv.define("clone", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new HashMap<Object, Object>(map), interpreter.mapEnv, "Map");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }
}
