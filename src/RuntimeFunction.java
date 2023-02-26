import java.util.List;

public class RuntimeFunction implements CallableObject {
    public final Stmt.Function declaration;
    public final Environment closure;

    public RuntimeFunction(Stmt.Function declaration, Environment closure) {
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
                throw new RuntimeError(declaration.name, "Expected " + (arity.required-1) + " arguments but got " + arguments.size() + ".");
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
        
        // all functions must return a value
        if (!foundReturn && declaration.mustReturnValue) {
            throw new RuntimeError(declaration.name, "Function must return a value.");
        }
        // execute deferred statements (if any)
        if (deferredStatements != null) {
            interpreter.executeBlock(deferredStatements.statements, activationEnv);
        }

        // return value
        return returnedValue;

        // execute function body
        // try {
        //     interpreter.executeBlock(declaration.body.statements, environment);
        // } catch (Return returnValue) {
        //     // return value
        //     foundReturn = true;
        //     return returnValue.value;
        // }

    }
    
}
