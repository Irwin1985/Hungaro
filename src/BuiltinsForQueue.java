import java.util.List;
import java.util.PriorityQueue;

@SuppressWarnings("unchecked")
public final class BuiltinsForQueue {
    public static void create(Interpreter interpreter) {       
        // pEnqueue(queue, value) - Pushes a value onto the queue
        interpreter.queueEnv.define("pEnqueue", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        // we use a PriorityQueue to implement the queue
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        Object element = arguments.get(1);
                        queueObj.add(element);
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fDequeue(queue) - Pops a value off the queue
        interpreter.queueEnv.define("fDequeue", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        return queueObj.poll();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fPeek(queue) - Peeks at the top of the queue
        interpreter.queueEnv.define("fPeek", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        return queueObj.peek();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fIsEmpty(queue) - Returns true if the queue is empty
        interpreter.queueEnv.define("fIsEmpty", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        return queueObj.isEmpty();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fLen(queue) - Returns the number of elements in the queue
        interpreter.queueEnv.define("fLen", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        return queueObj.size();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // pClear(queue) - Removes all elements from the queue
        interpreter.queueEnv.define("pClear", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    if (env.name.equals("Queue")) {
                        PriorityQueue<Object> queueObj = (PriorityQueue<Object>)env.lookup("value");
                        queueObj.clear();
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }
}
