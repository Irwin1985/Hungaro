import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public final class BuiltinsForMap {
    public static void create(Interpreter interpreter) {
        // pPut() builtin function
        interpreter.mapEnv.define("pPut", new CallableObject() {
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

        // map fGet builtin function
        interpreter.mapEnv.define("fGet", new CallableObject() {
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

        // map pSet builtin function
        interpreter.mapEnv.define("pSet", new CallableObject() {
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

        // map fLen builtin function
        interpreter.mapEnv.define("fLen", new CallableObject() {
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

        // fKeys builtin function
        interpreter.mapEnv.define("fKeys", new CallableObject() {
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

        // map fValues builtin function
        interpreter.mapEnv.define("fValues", new CallableObject() {
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

        // map fContains builtin function
        interpreter.mapEnv.define("fContains", new CallableObject() {
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

        // map fRemove builtin function
        interpreter.mapEnv.define("fRemove", new CallableObject() {
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

        // map pClear builtin function
        interpreter.mapEnv.define("pClear", new CallableObject() {
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

        // map fIsEmpty builtin function
        interpreter.mapEnv.define("fIsEmpty", new CallableObject() {
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

        // map fClone builtin function
        interpreter.mapEnv.define("fClone", new CallableObject() {
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
