import java.util.List;
import java.util.Stack;

@SuppressWarnings("unchecked")
public final class BuiltinsForStack {
    // builtins for Stack object
    public static void create(Interpreter interpreter) {
        // pPush() builtin function: push an element to the stack
        interpreter.stackEnv.define("pPush", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        Object element = arguments.get(1);
                        stackObj.push(element);
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fPop() builtin function: pop an element from the stack
        interpreter.stackEnv.define("fPop", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        return stackObj.pop();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fPeek() builtin function: peek an element from the stack
        interpreter.stackEnv.define("fPeek", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        return stackObj.peek();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fIsEmpty() builtin function: check if the stack is empty
        interpreter.stackEnv.define("fIsEmpty", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        return stackObj.isEmpty();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fLen() builtin function: get the length of the stack
        interpreter.stackEnv.define("fLen", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        return stackObj.size();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // pClear() builtin function: clear the stack
        interpreter.stackEnv.define("pClear", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Stack")) {
                        Stack<Object> stackObj = (Stack<Object>)env.lookup("value");
                        stackObj.clear();
                    }
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
