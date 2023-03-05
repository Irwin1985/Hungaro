import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public final class BuiltinsForArray {
    public static void create(Interpreter interpreter) {
        // concat builtin function: an array takes another array and call makeObject() to create a new array
        interpreter.arrayEnv.define("concat", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment && arguments.get(1) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    Environment env2 = (Environment)arguments.get(1);
                    ArrayList<Object> array2 = (ArrayList<Object>)env2.lookup("value");
                    ArrayList<Object> newArray = new ArrayList<Object>();
                    newArray.addAll(array);
                    newArray.addAll(array2);
                    return interpreter.makeObject(newArray, interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array indexOf builtin function
        interpreter.arrayEnv.define("indexOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return (double)array.indexOf(arguments.get(1));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array lastIndexOf builtin function
        interpreter.arrayEnv.define("lastIndexOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return (double)array.lastIndexOf(arguments.get(1));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array contains builtin function
        interpreter.arrayEnv.define("contains", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.contains(arguments.get(1));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array isEmpty builtin function
        interpreter.arrayEnv.define("isEmpty", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.isEmpty();
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array sort builtin function: sort the array in ascending order
        interpreter.arrayEnv.define("sort", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // check if the first element is a string
                    if (array.get(0) instanceof String) {
                        // all elements must be strings
                        for (int i = 0; i < array.size(); i++) {
                            if (!(array.get(i) instanceof String)) {
                                throw new RuntimeError(null, "Array elements must be of the same type");
                            }
                        }
                        // convert the array to an ArrayList of strings
                        ArrayList<String> stringArray = new ArrayList<String>();
                        for (int i = 0; i < array.size(); i++) {
                            stringArray.add((String)array.get(i));
                        }
                        // sort the array
                        Collections.sort(stringArray);
                        // define the new array in the "value" of the array environment
                        env.define("value", stringArray);
                    }
                    else if (array.get(0) instanceof Double) {
                        // all elements must be numbers
                        for (int i = 0; i < array.size(); i++) {
                            if (!(array.get(i) instanceof Double)) {
                                throw new RuntimeError(null, "Array elements must be of the same type");
                            }
                        }
                        // convert the array to an ArrayList of numbers
                        ArrayList<Double> numberArray = new ArrayList<Double>();
                        for (int i = 0; i < array.size(); i++) {
                            numberArray.add((Double)array.get(i));
                        }
                        // sort the array
                        Collections.sort(numberArray);
                        // define the new array in the "value" of the array environment
                        env.define("value", numberArray);
                    }
                    else {
                        throw new RuntimeError(null, "Array elements must be of the same type and either numbers or strings");
                    }
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array reverse builtin function
        interpreter.arrayEnv.define("reverse", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    Collections.reverse(array);
                    // define the new array in the "value" of the array environment
                    env.define("value", array);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array first() builtin function
        interpreter.arrayEnv.define("first", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    return array.get(0);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array last() builtin function
        interpreter.arrayEnv.define("last", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    return array.get(array.size() - 1);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array remove() removes the first occurrence of the given element
        interpreter.arrayEnv.define("remove", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    // if the element is not in the array, return null
                    if (!array.contains(arguments.get(1))) {
                        return null;
                    }
                    array.remove(arguments.get(1));
                    return arguments.get(1);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array removeAt() builtin function
        interpreter.arrayEnv.define("removeAt", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the array is empty, return null
                    if (array.isEmpty()) {
                        return null;
                    }
                    // if the index is out of bounds, return null
                    if ((int)(double)(Double)arguments.get(1) < 0 || (int)(double)(Double)arguments.get(1) >= array.size()) {
                        return null;
                    }
                    return array.remove((int)(double)(Double)arguments.get(1));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array insertAt() builtin function
        interpreter.arrayEnv.define("insertAt", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // if the index is out of bounds, return null
                    if ((int)(double)(Double)arguments.get(1) < 0 || (int)(double)(Double)arguments.get(1) >= array.size()) {
                        return null;
                    }
                    array.add((int)(double)(Double)arguments.get(1), arguments.get(2));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        }); 
        
        // array occurs() builtin function: returns the number of times the given element occurs in the array
        interpreter.arrayEnv.define("occurs", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    int count = 0;
                    for (int i = 0; i < array.size(); i++) {
                        if (array.get(i).equals(arguments.get(1))) {
                            count++;
                        }
                    }
                    return (double)count;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // array takes() builtin function: returns a new array containing the first n elements of the given array
        // if the argument is negative then the last n elements are returned
        interpreter.arrayEnv.define("take", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    ArrayList<Object> newArray = new ArrayList<Object>();
                    if ((int)(double)(Double)arguments.get(1) < 0) {
                        for (int i = array.size() + (int)(double)(Double)arguments.get(1); i < array.size(); i++) {
                            newArray.add(array.get(i));
                        }
                    } else {
                        for (int i = 0; i < (int)(double)(Double)arguments.get(1); i++) {
                            newArray.add(array.get(i));
                        }
                    }                    
                    return interpreter.makeObject(newArray, interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        

        // array push builtin function: eg. array.push(1)
        interpreter.arrayEnv.define("push", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    array.add(arguments.get(1));
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array pop builtin function
        interpreter.arrayEnv.define("pop", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return array.remove(array.size() - 1);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array len builtin function
        interpreter.arrayEnv.define("len", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    return Double.valueOf(array.size());
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array get builtin function
        interpreter.arrayEnv.define("get", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double index = (Double)arguments.get(1);
                        int i = index.intValue();                
                        return array.get(i);
                    } catch(Exception e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array set builtin function
        interpreter.arrayEnv.define("set", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double index = (Double)arguments.get(1);
                        int i = index.intValue();                
                        array.set(i, arguments.get(2));
                    } catch(Exception e) {
                        return null;
                    }                    
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array slice builtin function
        interpreter.arrayEnv.define("slice", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    try {
                        Double start = (Double)arguments.get(1);
                        Double end = (Double)arguments.get(2);
                        int s = start.intValue();
                        int e = end.intValue();
                        return interpreter.makeObject(array.subList(s, e), interpreter.arrayEnv, "Array");
                    } catch(Exception e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array join builtin function
        interpreter.arrayEnv.define("join", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    // create an array of strings by calling stringify on each element
                    String[] strings = new String[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        strings[i] = interpreter.stringify(array.get(i));
                    }
                    String separator = (String)arguments.get(1);
                    return String.join(separator, strings);                    

                    // String result = String.join(separator, array.stream().map(Object::toString).toArray(String[]::new));
                    // return result;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array shuffle builtin function
        interpreter.arrayEnv.define("shuffle", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    Collections.shuffle(array);
                    // update the array
                    env.define("value", array);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array equals builtin function: the argument must be an array
        interpreter.arrayEnv.define("equals", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment && arguments.get(1) instanceof Environment) {
                    Environment env1 = (Environment)arguments.get(0);
                    Environment env2 = (Environment)arguments.get(1);
                    ArrayList<Object> array1 = (ArrayList<Object>)env1.lookup("value");
                    ArrayList<Object> array2 = (ArrayList<Object>)env2.lookup("value");
                    return array1.equals(array2);
                }
                return false;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array sum() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("sum", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    double sum = 0.0;
                    for (Object o : array) {
                        if (o instanceof Double) {
                            sum += (Double)o;
                        }
                    }
                    return sum;
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array avg() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("avg", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    double sum = 0.0;
                    for (Object o : array) {
                        if (o instanceof Double) {
                            sum += (Double)o;
                        }
                    }
                    return sum / array.size();
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array min() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("min", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    double min = Double.MAX_VALUE;
                    for (Object o : array) {
                        if (o instanceof Double) {
                            double d = (Double)o;
                            if (d < min) {
                                min = d;
                            }
                        }
                    }
                    return min;
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array max() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("max", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    double max = Double.MIN_VALUE;
                    for (Object o : array) {
                        if (o instanceof Double) {
                            double d = (Double)o;
                            if (d > max) {
                                max = d;
                            }
                        }
                    }
                    return max;
                }
                return 0.0;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });  
        
        // array clone() builtin function: the argument must be an array
        interpreter.arrayEnv.define("clone", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    ArrayList<Object> clone = new ArrayList<Object>(array);
                    return interpreter.makeObject(clone, interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array clear() builtin function: the argument must be an array
        interpreter.arrayEnv.define("clear", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    array.clear();
                    env.define("value", array);
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // filter(): takes a function and apply it to each element of the array filtering down
        // just those elements that return true
        interpreter.arrayEnv.define("filter", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    ArrayList<Object> filtered = new ArrayList<Object>();
                    for (Object o : array) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(o);
                        Object result = function.call(interpreter, args);
                        if (result instanceof Boolean) {
                            if ((Boolean)result) {
                                filtered.add(o);
                            }
                        }
                    }
                    return interpreter.makeObject(filtered, interpreter.arrayEnv, "Array");
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // every(): takes a function and apply it to each element of the array. If all the
        // elements return true, then the function returns true, otherwise it returns false
        interpreter.arrayEnv.define("every", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    for (Object o : array) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(o);
                        Object result = function.call(interpreter, args);
                        if (result instanceof Boolean) {
                            if (!(Boolean)result) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // some(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns true, otherwise it returns false
        interpreter.arrayEnv.define("some", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    for (Object o : array) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(o);
                        Object result = function.call(interpreter, args);
                        if (result instanceof Boolean) {
                            if ((Boolean)result) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // find(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns the element, otherwise it returns null
        interpreter.arrayEnv.define("find", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    for (Object o : array) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(o);
                        Object result = function.call(interpreter, args);
                        if (result instanceof Boolean) {
                            if ((Boolean)result) {
                                return o;
                            }
                        }
                    }
                    return null;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // findIndex(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns the index of the element, otherwise it returns -1
        interpreter.arrayEnv.define("findIndex", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    int index = 0;
                    for (Object o : array) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(o);
                        Object result = function.call(interpreter, args);
                        if (result instanceof Boolean) {
                            if ((Boolean)result) {
                                return (double)index;
                            }
                        }
                        index++;
                    }
                    return (double)-1;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // reduce(): takes a function and apply it to each element of the array. The function
        // must take two arguments, the first one is the accumulator and the second one is the
        // current element. The function returns the accumulator. The initial value of the
        // accumulator is the first element of the array.
        interpreter.arrayEnv.define("reduce", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                CallableObject function = (CallableObject)arguments.get(1);
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    ArrayList<Object> array = (ArrayList<Object>)env.lookup("value");
                    if (array.size() == 0) {
                        return null;
                    }
                    Object accumulator = array.get(0);
                    for (int i = 1; i < array.size(); i++) {
                        ArrayList<Object> args = new ArrayList<Object>();
                        args.add(accumulator);
                        args.add(array.get(i));
                        accumulator = function.call(interpreter, args);
                    }
                    return accumulator;
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
