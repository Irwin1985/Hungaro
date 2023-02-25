import java.util.List;

public interface CallableObject {
    
    // Arity class: this will be used to determine the number of arguments
    public static class Arity {
        final int required;
        final int optional;
        final boolean variadic;

        public Arity(int required, int optional, boolean variadic) {
            this.required = required;
            this.optional = optional;
            this.variadic = variadic;
        }

        public Arity(int required, int optional) {
            this(required, optional, false);
        }

        public Arity(int required) {
            this(required, 0, false);
        }

        public Arity() {
            this(0, 0, false);
        }

        public Arity(boolean variadic) {
            this(0, 0, variadic);
        }
        
    }
    Arity arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
