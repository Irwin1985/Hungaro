import java.util.List;

public interface CallableObject {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
