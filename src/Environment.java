import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> record;
    private final Environment parent;
    private final String name;

    public Environment(Map<String, Object> record, Environment parent, String name) {
        this.record = (record != null) ? record : new HashMap<String, Object>();
        this.parent = (parent != null) ? parent : null;
        this.name = name;
    }

    public Environment(String name) {
        this.record = new HashMap<String, Object>();
        this.parent = null;
        this.name = name;
    }

    public void define(String name, Object value) {
        record.put(name, value);
    }

    public void assign(Token name, Object value) {
        resolve(name).record.put(name.lexeme, value);        
    }

    public Object lookup(Token name) {
        return resolve(name).record.get(name.lexeme);
    }    
    
    private Environment resolve(Token name) {
        if (record.containsKey(name.lexeme)) {
            return this;
        }

        if (parent != null) {
            return parent.resolve(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
