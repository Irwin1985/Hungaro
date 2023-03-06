import java.util.List;

public final class BuiltinsForFunction {
    public static void create(Interpreter interpreter) {
        // function fArity builtin function: extract the internal "value" which is the 
        // RuntimeFunction object and call its arity() method
        interpreter.functionEnv.define("fArity", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Runtime.Function function = (Runtime.Function)env.lookup("value");
                    // arity is the number of parameters, not including the "poThis" parameter
                    return function.arity();
                }
                return new Arity();
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        }); 
    }
}
