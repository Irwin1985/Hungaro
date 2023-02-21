public class Type {

    public static final Type Null = new Type("null");
    public static final Type Number = new Type("number");
    public static final Type String = new Type("string");
    public static final Type Boolean = new Type("boolean");
    public static final Type Array = new Type("array");
    public static final Type Map = new Type("map");
    public static final Type Function = new Type("function");
    public static final Type Variant = new Type("variant");
    public static final Type Object = new Type("object");

    final String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equal(Type type) {
        return this.name.equals(type.name);
    }    
}
