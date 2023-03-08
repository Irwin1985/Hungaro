import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public final class BuiltinsForRegEx {
    public static void create(Interpreter interpreter) {
        // fTest(input): test the input string against the regular expression
        interpreter.regexEnv.define("fTest", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // the input is the second argument of the argument list
                    final String input = (String)arguments.get(1);
                    // extract the map of properties from "value"
                    final HashMap<String, Object> properties = (HashMap<String, Object>)env.lookup("value");
                    // extract the pattern from the properties
                    final String pattern = (String)properties.get("sPattern");
                    // extract the ignoreCase property from the properties
                    final boolean ignoreCase = (boolean)properties.get("bIgnoreCase");
                    // validate the pattern
                    if (pattern == null || pattern.isEmpty()) {
                        throw new RuntimeException("Invalid regular expression pattern");
                    }
                    // compile the regular expression
                    final Pattern regex = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
                    // test the input string against the regular expression
                    return regex.matcher(input).matches();
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // fExecute(input): execute the regular expression against the input string and return the 
        // MatchResult object
        interpreter.regexEnv.define("fExecute", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // the input is the second argument of the argument list
                    final String input = (String)arguments.get(1);
                    // extract the map of properties from "value"
                    final HashMap<String, Object> properties = (HashMap<String, Object>)env.lookup("value");
                    // extract the pattern from the properties
                    final String pattern = (String)properties.get("sPattern");
                    // extract the ignoreCase property from the properties
                    final boolean ignoreCase = (boolean)properties.get("bIgnoreCase");
                    // validate the pattern
                    if (pattern == null || pattern.isEmpty()) {
                        throw new RuntimeException("Invalid regular expression pattern");
                    }
                    // compile the regular expression
                    final Pattern regex = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
                    // execute the regular expression against the input string
                    Matcher matcher = regex.matcher(input);
                    // return the MatchResult object with makeObject() method
                    return interpreter.makeObject(matcher, interpreter.matcherEnv, "Matcher");
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
