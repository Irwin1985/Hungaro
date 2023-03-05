import java.util.List;

public final class BuiltinsForClass {
    public static void create(Interpreter interpreter) {
        // toString() builtin function: show the name of the class
        interpreter.classEnv.define("toString", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    return ((Environment)arguments.get(0)).name;                    
                }
                return "";
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // className(): return the name of the class. The name is the last word in the environment name.
        interpreter.classEnv.define("className", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    String name = ((Environment)arguments.get(0)).name;
                    return lastWord(name);
                }
                return "";
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // instanceOf(): return true if the object is an instance of the class
        // we need to lookup in the environment to find the class
        interpreter.classEnv.define("instanceOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    String className = (String)arguments.get(1);
                    String name = "";
                    do {
                        name = lastWord(env.name);
                        if (className.equals(name)) {
                            return true;
                        }
                        env = env.parent;
                    } while ((env = env.parent) != null);                    
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }
    /***********************************************************************
     * Helpers functions
     * *********************************************************************/
    private static String lastWord(String word) {
        // return the last word of a string
        // ex: lastWord("hello world") returns "world"
        String[] words = word.split(" ");
        return words[words.length - 1];        
    }    
}
