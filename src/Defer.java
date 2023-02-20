public class Defer extends RuntimeException {
    final Stmt.Block body;

    public Defer(Stmt.Block body) {
        super(null, null, false, false);
        this.body = body;
    }
}
