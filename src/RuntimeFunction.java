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

        // load arguments into environment
        for (int i = 0; i < declaration.params.size(); i++) {
            interpreter.checkVariableType(declaration.params.get(i).name, arguments.get(i), "Parameter");
            environment.define(declaration.params.get(i).name.lexeme, arguments.get(i));
        }

        // execute function body
        try {
            interpreter.executeBlock(declaration.body.statements, environment);
        } catch (Return returnValue) {
            // return value
            foundReturn = true;
            return returnValue.value;
        }

        if (!foundReturn && declaration.mustReturnValue) {
            throw new RuntimeError(declaration.name, "Function must return a value.");
        }
        return null;        
    }
    
}
