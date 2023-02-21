import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TypeEnvironment {
    private final Map<String, Object> record;
    public final TypeEnvironment parent;
    public final String name;

    public TypeEnvironment(Map<String, Object> record, TypeEnvironment parent, String name) {
        this.record = (record != null) ? record : new HashMap<String, Object>();
        this.parent = (parent != null) ? parent : null;
        this.name = name;
    }

    public TypeEnvironment(String name) {
        this.record = new HashMap<String, Object>();
        this.parent = null;
        this.name = name;
    }

    public TypeEnvironment(TypeEnvironment parent, String name) {
        this.record = new HashMap<String, Object>();
        this.parent = parent;
        this.name = name;
    }

    public Object define(String name, Object value) {
        record.put(name, value);
        return value;
    }

    public Object assign(Token name, Object value) {
        resolve(name).record.put(name.lexeme, value);
        return value;
    }

    public Object lookup(Token name) {        
        return resolve(name).record.get(name.lexeme);
    }
    
    public Object lookup(String name, Token token) {
        return resolve(name, token).record.get(name);
    }
    
    private TypeEnvironment resolve(Token name) {
        if (record.containsKey(name.lexeme)) {
            return this;
        }

        if (parent != null) {
            return parent.resolve(name);
        }        

        throw new RuntimeError(name, variableNotDefinedMessage(name.lexeme));
    }

    private TypeEnvironment resolve(String name, Token token) {
        if (record.containsKey(name)) {
            return this;
        }

        if (parent != null) {
            return parent.resolve(name, token);
        }
        
        throw new RuntimeError(token, variableNotDefinedMessage(name));
    }

    private String variableNotDefinedMessage(String name) {
        String message = "Undefined variable `" + name + "`.";
        if (Hungaro.isConstant(name)) {
            message = "Undefined constant `" + name + "`.";
        }
        return message;
    }

    public Object lookup(String name) {
        // return record.get(name);
        if (record.containsKey(name)) {
            return record.get(name);
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    public Map<String, Object> getRecord() {
        return record;
    }

    public Object lookup(int index, Token token) {
        try {
            return ((ArrayList<Object>)record.get("value")).get(index);
        } catch (Exception e) {
            throw new RuntimeError(token, "Invalid index `" + index + "`.");
        }
    }    
}