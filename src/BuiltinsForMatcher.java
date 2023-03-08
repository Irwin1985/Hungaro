import java.util.List;
import java.util.regex.Matcher;

public final class BuiltinsForMatcher {
    public static void create(Interpreter interpreter) {
        // fMatches: this is a property that extract the "value" from the Environment
        // and examine the 'matches' property of the Matcher object.
        interpreter.matcherEnv.define("fMatches", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");
                    // return the 'matches' property of the Matcher object
                    return matcher.matches();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fFind: this is a property that extract the "value" from the Environment
        // and examine the 'find' property of the Matcher object.
        interpreter.matcherEnv.define("fFind", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");
                    // return the 'find' property of the Matcher object
                    return matcher.find();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGroup: returns the matched subsequences of the input string
        interpreter.matcherEnv.define("fGroup", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");                    
                    // return the matched subsequences of the input string
                    return matcher.group();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGroupCount: returns the number of matched subsequences of the input string
        interpreter.matcherEnv.define("fGroupCount", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");                    
                    // return the number of matched subsequences of the input string
                    return matcher.groupCount();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fStart: returns the start index of the matched subsequences of the input string
        interpreter.matcherEnv.define("fStart", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");                    
                    // return the start index of the matched subsequences of the input string
                    return matcher.start();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fEnd: returns the end index of the matched subsequences of the input string
        interpreter.matcherEnv.define("fEnd", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // extract the Matcher object from the environment
                    final Matcher matcher = (Matcher)env.lookup("value");                    
                    // return the end index of the matched subsequences of the input string
                    return matcher.end();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }
}
