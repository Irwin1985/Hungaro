import java.util.List;

public abstract class Stmt {
    final Token token;

    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitConstantStmt(Constant stmt);
        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
        R visitFunctionStmt(Function stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
        R visitDeclareStmt(Declare stmt);
    }

    public Stmt(Token token) {
        this.token = token;
    }

    abstract <R> R accept(Visitor<R> visitor);

    /*
     * Block statement: { ... }
     */
    public static class Block extends Stmt {
        final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            super(statements.get(0).token);                            
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    /*
     * Constant statement: declare CONSTANT = 1 + 2;
     */
    public static class Constant extends Stmt {
        final Token name;
        final Expr value;

        public Constant(Token keyword, Token name, Expr value) {
            super(keyword);
            this.name = name;            
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConstantStmt(this);
        }
    }

    /*
     * Class statement: class Foo { ... }
     */
    public static class Class extends Stmt {
        final Token name;
        final Expr.Variable superclass;
        final List<Expr.Set> properties;
        final Block body;

        public Class(Token keyword, Token name, Expr.Variable superclass, List<Expr.Set> properties, Block body) {
            super(keyword);
            this.name = name;
            this.superclass = superclass;
            this.properties = properties;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    /*
     * Expression statement: 1 + 2;
     */
    public static class Expression extends Stmt {
        final Expr expression;

        public Expression(Expr expression) {
            super(expression.token);
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    /*
     * Function statement: fun foo() { ... }
     */
    public static class Function extends Stmt {
        final Token name;
        final List<Expr.Variable> params;
        final Block body;
        final boolean mustReturnValue;

        public Function(boolean mustReturnValue, Token name, List<Expr.Variable> params, Block body) {
            super(name);
            this.mustReturnValue = mustReturnValue;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    /*
     * If statement: if (a) { ... } else { ... }
     */
    public static class If extends Stmt {
        final Expr condition;
        final Block thenBranch;
        final Block elseBranch;

        public If(Expr condition, Block thenBranch, Block elseBranch) {
            super(condition.token);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    /*
     * Print statement: print 1 + 2;
     */
    public static class Print extends Stmt {
        final List<Expr> expressions;

        public Print(List<Expr> expressions) {
            super(null);
            this.expressions = expressions;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    /*
     * Return statement: return 1 + 2;
     */
    public static class Return extends Stmt {
        final Token keyword;
        final Expr value;

        public Return(Token keyword, Expr value) {
            super(keyword);
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    /*
     * Declaration statement: 
     * 1. declare lsName = 1 + 2
     * 2. declare lnAge = 18
     * 3. declare lbIsAdult = true
     * 4. declare laNames = ["John", "Jane"]
     * 5. declare clPerson
     * 6. declare oPerson = new Person()
     * 7. declare goPerson = new Person()
     * 8. declare loPerson = new Person()
     */
    public static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        public Var(Token keyword, Token name, Expr initializer) {
            super(keyword);
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    /*
     * While statement: while (a) { ... }
     */
    public static class While extends Stmt {
        final Expr condition;
        final Stmt body;

        public While(Expr condition, Stmt body) {
            super(condition.token);
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    /*
     * Declare statement: declare lsName = 1 + 2
     */
    public static class Declare extends Stmt {
        final List<Stmt> statements;
        public Declare(Token keyword, List<Stmt> statements) {
            super(keyword);
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDeclareStmt(this);
        }
    }
}
