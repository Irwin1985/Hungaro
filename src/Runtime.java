import java.util.List;

public final class Runtime {

    // Defer: is used to defer execution of statements
    public static class Defer extends RuntimeException {
        final Stmt.Block body;
    
        public Defer(Stmt.Block body) {
            super(null, null, false, false);
            this.body = body;
        }
    }

    // Loop: is used to exit a loop
    public static class Loop extends RuntimeException {    

        public Loop() {
            super(null, null, false, false);     
        }
    }

    // Exit: is used to exit a function or procedure
    public static class Exit extends RuntimeException {    

        public Exit() {
            super(null, null, false, false);
            
        }    
    }

    // Function: is used to represent a function
    public static class Function implements CallableObject {
        public final Stmt.Function declaration;
        public final Environment closure;
    
        public Function(Stmt.Function declaration, Environment closure) {
            this.declaration = declaration;
            this.closure = closure;
        }
    
        @Override
        public String toString() {
            if (declaration.mustReturnValue) {
                return "(function " + declaration.name.lexeme + ")";
            }
            return "(procedure " + declaration.name.lexeme + ")";
        }
    
        @Override
        public Arity arity() {
            final int requiredParams = declaration.params.size() - declaration.optionalParams;
            final int optionalParams = declaration.optionalParams;
            final boolean isVariadic = declaration.isVariadic;
            return new Arity(requiredParams, optionalParams, isVariadic);
        }
    
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            final Arity arity = arity();
            Environment activationEnv = null;
            
            // if we have a variadic function or optional parameters, we need to create a new environment
            if ((!arity.variadic) && (arity.optional == 0)) {
                if (arguments.size() != arity.required) {                
                    throw new Error(declaration.name, "Expected " + (arity.required-1) + " arguments but got " + (arguments.size()-1) + ".");
                }
                activationEnv = new Environment(closure, "function call of " + declaration.name.lexeme);
                // load arguments into environment
                for (int i = 0; i < declaration.params.size(); i++) {
                    interpreter.checkVariableType(declaration.params.get(i).name, arguments.get(i), "Parameter");
                    activationEnv.define(declaration.params.get(i).name.lexeme, arguments.get(i));
                }
            } else {            
                activationEnv = interpreter.createActivationEnvironment(declaration.name, this, arguments);
            }
    
            boolean foundReturn = false;
            Stmt.Block deferredStatements = null;
            Object returnedValue = null;
    
            // execute every statement in the function body and catch return or defer exepctions
            Environment previous = interpreter.environment;
            try {
                interpreter.environment = activationEnv;
                for (Stmt statement : declaration.body.statements) {
                    // execute statement and try to catch return or defer exceptions                
                    try {
                        interpreter.execute(statement);
                    } catch (Return returnValue) {
                        // save return value and exit loop
                        foundReturn = true;
                        returnedValue = returnValue.value;
                        break;
                    } catch (Defer defer) {
                        // save deferred statements and continue execution
                        deferredStatements = defer.body;                    
                    } // we need to catch Exit exception here only for procedures (mustReturnValue = false)
                    catch (Exit exit) {
                        if (!declaration.mustReturnValue) {
                            break;
                        }
                    }
                }
            } finally {
                interpreter.environment = previous;
            }
                        
            if (!foundReturn && declaration.mustReturnValue) {
                throw new Error(declaration.name, "Function must return a value.");
            }
            
            if (deferredStatements != null) {
                interpreter.executeBlock(deferredStatements.statements, activationEnv);
            }
            return returnedValue;
        }
    
        @Override
        public boolean evaluateArguments() {
            return true;
        }        
    }

    // Error: is used to represent a runtime error
    public static class Error extends RuntimeException {
        final Token token;
        Error(Token token, String message) {
            super(message);
            this.token = token;
        }
    }

    // Return: is used to return a value from a function
    public static class Return extends RuntimeException {
        final Object value;
    
        public Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
}
