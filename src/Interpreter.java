import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment("global");
    private Environment environment = globals;

    public Interpreter() {
        globals.define("_VERSION", "0.1.1");
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Hungaro.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {        
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {        
        return expr.token.literal;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public Object visitNewExpr(Expr.New expr) {
        return null;
    }

    @Override
    public Object visitPropExpr(Expr.Prop expr) {
        return null;
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookup(expr.token);
    }

    private Object lookup(Token token) {        
        return environment.lookup(token);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(null, environment, "block"));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        for (Expr expression : stmt.expressions) {
            System.out.println(stringify(evaluate(expression)));
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        if (stmt.name.lexeme.substring(0,1) == "g") {
            // global variable
            globals.define(stmt.name.lexeme, value);
        } else {
            // local variable
            environment.define(stmt.name.lexeme, value);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        return null;
    }

    private String stringify(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        // TODO: Add support for other types.
        return object.toString();
    }

    @Override
    public Void visitConstantStmt(Stmt.Constant stmt) {
        if (stmt.name.lexeme.substring(0,1) == "_") {
            // global constant
            globals.define(stmt.name.lexeme, evaluate(stmt.value));
        } else {
            // local constant
            environment.define(stmt.name.lexeme, evaluate(stmt.value));
        }
        return null;
    }
}
