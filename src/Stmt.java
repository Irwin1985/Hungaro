import java.util.List;

public abstract class Stmt {
    final Token token;

    interface Visitor<R> {
        R visitBlockStmt(Block stmt);        
        R visitConstantStmt(Constant stmt);
        R visitClassStmt(Class stmt);
        R visitDeclareStmt(Declare stmt);
        R visitDeferStmt(Defer stmt);
        R visitExitStmt(Exit stmt);
        R visitExpressionStmt(Expression stmt);
        R visitForeachStmt(Foreach stmt);
        R visitForStmt(For stmt);
        R visitFunctionStmt(Function stmt);
        R visitGuardStmt(Guard stmt);
        R visitIfStmt(If stmt);
        R visitLoopStmt(Loop stmt);
        R visitPrintStmt(Print stmt);
        R visitRepeatStmt(Repeat stmt);
        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
        R visitWhileStmt(While stmt);
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
            super(null);                            
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
     * Loop statement: throws an exception to continue the loop
     */
    public static class Loop extends Stmt {        

        public Loop(Token keyword) {
            super(keyword);            
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLoopStmt(this);
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
     * Exit statement: throws an exception to exit the loop
     */
    public static class Exit extends Stmt {        

        public Exit(Token keyword) {
            super(keyword);            
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExitStmt(this);
        }
    }

    /*
    * For statement: for i = 1 to 10 { ... }
    */
    public static class For extends Stmt {
        final Token counter;
        final Expr start;
        final Expr stop;
        final Expr step;
        final Block body;

        public For(Token keyword, Token counter, Expr start, Expr stop, Expr step, Stmt.Block body) {
            super(keyword);
            this.counter = counter;
            this.start = start;
            this.stop = stop;
            this.step = step;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    /*
     * Foreach statement: foreach i in [1, 2, 3] { ... }
     */
    public static class Foreach extends Stmt {
        final Expr.Variable variable;
        final Expr iterable;
        final Block body;

        public Foreach(Token keyword, Expr.Variable variable, Expr iterable, Stmt.Block body) {
            super(keyword);
            this.variable = variable;
            this.iterable = iterable;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForeachStmt(this);
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
     * Guard statement: keyword, condition, body
     */
    public static class Guard extends Stmt {
        final Expr condition;
        final Block body;

        public Guard(Token keyword, Expr condition, Block body) {
            super(keyword);
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGuardStmt(this);
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
    * Repeat statement: repeat { ... } until (a)
    */
    public static class Repeat extends Stmt {
        final Block body;
        final Expr condition;

        public Repeat(Token keyword, Block body, Expr condition) {
            super(keyword);
            this.body = body;
            this.condition = condition;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitRepeatStmt(this);
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
        final Stmt.Block body;

        public While(Token keyword, Expr condition, Stmt.Block body) {
            super(keyword);
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

    /*
    * Defer: defer { ... }
    */
    public static class Defer extends Stmt {
        final Block body;

        public Defer(Token keyword, Block body) {
            super(keyword);
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDeferStmt(this);
        }
    }     
}
