import java.util.List;

public class TypeChecker implements Expr.Visitor<Type>, Stmt.Visitor<Void> {
    final TypeEnvironment globals = new TypeEnvironment("Global");       
    public TypeEnvironment environment = globals;


    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Hungaro.runtimeError(error);
        }
    }

    public void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Type evaluate(Expr expr) {
        return expr.accept(this);
    }

    public void executeBlock(List<Stmt> statements, TypeEnvironment environment) {
        TypeEnvironment previous = this.environment;
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
    public Void visitBlockStmt(Stmt.Block stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitConstantStmt(Stmt.Constant stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDeclareStmt(Stmt.Declare stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDeferStmt(Stmt.Defer stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExitStmt(Stmt.Exit stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {        
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitForeachStmt(Stmt.Foreach stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLoopStmt(Stmt.Loop stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitRepeatStmt(Stmt.Repeat stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // get the variable type based on the first two characters of the variable name
        final Type varType = getVariableType(stmt.name);
        // get the variable value type
        final Type varValueType = stmt.initializer.accept(this);
        // check if the variable type and value type are equal
        if (!varType.equal(varValueType)) {
            // if not, throw an error
            throw new RuntimeError(stmt.name, String.format("Variable `%s` type %s does not match value type %s", stmt.name.lexeme, varType, varValueType));
        }
        // add the variable to the environment
        environment.define(stmt.name.lexeme, varType);        
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitArrayExpr(Expr.Array expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitLiteralExpr(Expr.Literal expr) {
        switch (expr.token.type) {
            case NUMBER:
                return Type.Number;
            case STRING:
                return Type.String;
            case TRUE:
            case FALSE:
                return Type.Boolean;
            case NULL:
                return Type.Null;
            default:
                throw new RuntimeError(expr.token, "Invalid literal type");
        }
    }

    @Override
    public Type visitBinaryExpr(Expr.Binary expr) {
        final Type left = evaluate(expr.left);
        final Type right = evaluate(expr.right);
        // check if the operator is a comparison operator
        if (expr.operator.type == TokenType.LOGICAL_AND || expr.operator.type == TokenType.LOGICAL_OR || expr.operator.type == TokenType.REL_OPE) {
            // if so, check if the left and right types are equal
            if (!left.equal(right)) {
                // if not, throw an error
                throw new RuntimeError(expr.operator, String.format("Cannot compare %s and %s", left, right));
            }
            // return the boolean type
            return Type.Boolean;
        }
        // check if the operator is a arithmetic operator
        if (expr.operator.type == TokenType.TERM || expr.operator.type == TokenType.FACTOR) {
            // if so, check if the left and right types are numbers
            if (left != Type.Number || right != Type.Number) {
                // if not, throw an error
                throw new RuntimeError(expr.operator, String.format("Cannot perform arithmetic operation on %s and %s", left, right));
            }
            // return the number type
            return Type.Number;
        }
        return null;
    }

    @Override
    public Type visitBinaryStringExpr(Expr.BinaryString expr) {        
        return Type.String;
    }

    @Override
    public Type visitCallExpr(Expr.Call expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitLambdaExpr(Expr.Lambda expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitLogicalExpr(Expr.Logical expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitMapExpr(Expr.Map expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitNewExpr(Expr.New expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitPropExpr(Expr.Prop expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitSetExpr(Expr.Set expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitSliceExpr(Expr.Slice expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitSuperExpr(Expr.Super expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitUnaryExpr(Expr.Unary expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type visitVariableExpr(Expr.Variable expr) {        
        return null;
    }   

    private Type getVariableType(Token name) {        
        char linkedType = 0;
        
        if (name.category == Category.CLASS_PROPERTY) {
            linkedType = name.lexeme.substring(0, 1).charAt(0);    
        } else {
            linkedType = name.lexeme.substring(1, 2).charAt(0);
        }        

        // otherwise we check the type
        switch (linkedType) {
            case 'v':
                return Type.Variant;
            case 's':
                return Type.String;
            case 'n':
                return Type.Number;
            case 'b':
                return Type.Boolean;
            case 'a':
                return Type.Array;
            case 'm':
                return Type.Map;
            case 'o':
                return Type.Object;
            default:
                break;
        }
        throw new RuntimeError(name, " Unknown variable type.");
    }

    @Override
    public Void visitGuardStmt(Stmt.Guard stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGuardStmt'");
    }
}