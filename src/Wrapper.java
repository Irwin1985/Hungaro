public interface Wrapper {
    public static class ExceptionClass implements Wrapper {
        private final Exception value;
        private final Token token;

        public ExceptionClass(Exception value) {
            this.token = null;
            this.value = value;
        }

        public ExceptionClass(Token token, Exception value) {
            this.token = token;
            this.value = value;
        }

        public Object get(String key) {
            switch (key) {
                case "message":
                    return value.getMessage();                
                default:
                    throw new RuntimeError(token, "No such property '" + key + "' on exception");
            }
        }

        public void set(String key, Object value) {
            throw new RuntimeException("Cannot set property on exception");
        }

        public boolean has(String key) {
            return false;
        }
        
        public Object getValue() {
            return value;
        }
    }
    Object get(String key);
    void set(String key, Object value);
    boolean has(String key);
}
