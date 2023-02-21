import java.util.List;

public class RuntimeFunction implements CallableObject {
    private final Stmt.Function declaration;
    private final Environment closure;

    public RuntimeFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure, "function call of " + declaration.name.lexeme);
        boolean foundReturn = false;
        Stmt.Block deferredStatements = null;
        Object returnedValue = null;
        // load arguments into environment
        for (int i = 0; i < declaration.params.size(); i++) {
            interpreter.checkVariableType(declaration.params.get(i).name, arguments.get(i), "Parameter");
            environment.define(declaration.params.get(i).name.lexeme, arguments.get(i));
        }

        // execute every statement in the function body and catch return or defer exepctions
        Environment previous = interpreter.environment;
        try {
            interpreter.environment = environment;
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
            interpreter.executeBlock(deferredStatements.statements, environment);
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
