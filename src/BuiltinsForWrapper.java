import java.util.List;

public final class BuiltinsForWrapper {
    public static void create(Interpreter interpreter) {
        // get() builtin function: get a property from the wrapped object
        // the wrapper object is stored in 'value' property of the environment
        interpreter.wrapperEnv.define("message", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object wrappedObj = env.lookup("value");
                    return ((Exception)wrappedObj).getMessage();
                    // String propertyName = (String)arguments.get(1);
                    // return ((Wrapper)wrappedObj).get(propertyName);
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
