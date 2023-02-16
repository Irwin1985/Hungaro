import java.util.List;

public abstract class Expr {
    final Token token;

    public Expr(Token token) {
        this.token = token;
    }

    interface Visitor<R> {
        R visitArrayExpr(Array expr);
        R visitLiteralExpr(Literal expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitLambdaExpr(Lambda expr);
        R visitLogicalExpr(Logical expr);
        R visitMapExpr(Map expr);
        R visitNewExpr(New expr);
        R visitPropExpr(Prop expr);
        R visitSetExpr(Set expr);
        R visitSuperExpr(Super expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    /*
     * Array expression: [1, 2, 3]
     */
    public static class Array extends Expr {
        final List<Expr> elements;

        public Array(Token token, List<Expr> elements) {
            super(token);
            this.elements = elements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayExpr(this);
        }
    }

    /*
     * Literal expression: 123, "hello", true, false, null 
     */
    public static class Literal extends Expr {        

        public Literal(Token token) {
            super(token);
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    /*
     * Binary expression: 1 + 2
     */
    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            super(operator);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    /*
     * Call expression: foo(1, 2)
     */
    public static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            super(paren);
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    /*
     * Lambda expression: (a, b) => a + b
     */
    public static class Lambda extends Expr {
        final List<Token> params;
        final Stmt body;

        public Lambda(Token token, List<Token> params, Stmt body) {
            super(token);
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLambdaExpr(this);
        }
    }

    /*
     * Logical expression: 1 or 2
     */
    public static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            super(operator);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    /*
     * Map expression: {a: 1, b: 2}
     */
    public static class Map extends Expr {
        final List<Expr> keys;
        final List<Expr> values;

        public Map(Token token, List<Expr> keys, List<Expr> values) {
            super(token);
            this.keys = keys;
            this.values = values;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitMapExpr(this);
        }
    }

    /*
     * New expression: new Foo()
     */
    public static class New extends Expr {
        final Token name;
        final List<Expr> arguments;

        public New(Token keyword, Token name, List<Expr> arguments) {
            super(keyword);
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }
    }

    /*
     * Property expression: foo.bar
     */
    public static class Prop extends Expr {
        final Expr target;       
        final Expr property; 
        final boolean computable;

        public Prop(Token name, Expr target, Expr property, boolean computable) {
            super(name);
            this.target = target;
            this.property = property;
            this.computable = computable;            
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPropExpr(this);
        }
    }

    /*
    * Set expression: foo.bar = 1
    */
    public static class Set extends Expr {
        final Expr target;
        final Expr value;
        final Token operator;
        final boolean computable;

        public Set(Token operator, Expr target, Expr value, boolean computable) {
            super(operator);
            this.operator = operator;
            this.target = target;
            this.value = value;
            this.computable = computable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    /*
     * Super expression: super.foo
     */
    public static class Super extends Expr {
        final Token keyword;
        final Variable method;

        public Super(Token keyword, Variable method) {
            super(keyword);
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }
    }

    /*
     * Unary expression: -1
     */
    public static class Unary extends Expr {
        final Token operator;
        final Expr right;

        public Unary(Token operator, Expr right) {
            super(operator);
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    /*
     * Variable expression: foo
     */
    public static class Variable extends Expr {
        final Token name;

        public Variable(Token name) {
            super(name);
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }


}
