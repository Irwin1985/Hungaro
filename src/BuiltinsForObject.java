import java.util.List;

public final class BuiltinsForObject {
    public static void create(Interpreter interpreter) {
        interpreter.globals.define("_VERSION", "0.1.1");
        // global _STRING builtin function
        interpreter.globals.define("_STRING", new Environment(interpreter.stringEnv, "String"));
        // global _NUMBER builtin function
        interpreter.globals.define("_NUMBER", new Environment(interpreter.numberEnv, "Number"));
        // global _ARRAY builtin function
        interpreter.globals.define("_ARRAY", new Environment(interpreter.arrayEnv, "Array"));
        // global _MAP builtin function
        interpreter.globals.define("_MAP", new Environment(interpreter.mapEnv, "Map"));
        // global _OBJECT builtin function
        interpreter.globals.define("_OBJECT", new Environment(interpreter.objectEnv, "Object"));                        

        // object fToString builtin function
        interpreter.objectEnv.define("fToString", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // print the internal value if object is an instance of Environment
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // get the "value" property and print it
                    Object value = env.lookup("value");
                    return interpreter.stringify(value);
                }
                return interpreter.stringify(arguments.get(0));
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // object fType builtin function: return the name of the Environment
        interpreter.objectEnv.define("fType", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    return env.name;
                }
                return "Object";
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }
}
