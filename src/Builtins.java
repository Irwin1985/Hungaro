import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

@SuppressWarnings("unchecked")
public final class Builtins {

    public static void defineGlobals(Interpreter interpreter) {
        createNativeObjects(interpreter);
        createArrayBuiltins(interpreter);
        createMapBuiltins(interpreter);
        createClassBuiltins(interpreter);
        createFunctionBuiltins(interpreter);
        createBuiltins(interpreter);
        createStringBuiltins(interpreter);
        defineWrapperBuiltins(interpreter);        
    }

    private static void createNativeObjects(Interpreter interpreter) {
        interpreter.globals.define("_VERSION", "0.1.1");
        // global _STRING builtin function
        interpreter.globals.define("_STRING", new Environment(interpreter.stringEnv, "String"));
        // global _NUMBER builtin function
        interpreter.globals.define("_NUMBER", new Environment(interpreter.numberEnv, "Number"));
        // global _ARRAY builtin function
        interpreter.globals.define("_ARRAY", new Environment(interpreter.arrayEnv, "Array"));
        // global _MAP builtin function
        interpreter.globals.define("_MAP", new Environment(interpreter.mapEnv, "Map"));
        // global _OBJECT builtin function
        interpreter.globals.define("_OBJECT", new Environment(interpreter.objectEnv, "Object"));                        

        // object toString builtin function
        interpreter.objectEnv.define("toString", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // print the internal value if object is an instance of Environment
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    // get the "value" property and print it
                    Object value = env.lookup("value");
                    return interpreter.stringify(value);
                }
                return interpreter.stringify(arguments.get(0));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // object type builtin function: return the name of the Environment
        interpreter.objectEnv.define("type", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    return env.name;
                }
                return "Object";
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }

    /***********************************************************************
    * Array
    ***********************************************************************/
    private static void createArrayBuiltins(Interpreter interpreter) {
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
    }

    /***********************************************************************
    * String
    ***********************************************************************/
    private static void createStringBuiltins(Interpreter interpreter) {
        // string len builtin function
        interpreter.stringEnv.define("len", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)((String)arguments.get(0)).length();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string slice builtin function
        interpreter.stringEnv.define("slice", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                // start and end must be converted from Double to Integer
                int start = ((Double)arguments.get(1)).intValue();
                int end = ((Double)arguments.get(2)).intValue();
                return str.substring(start, end);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string find builtin function
        interpreter.stringEnv.define("find", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return (double)str.indexOf(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string contains builtin function
        interpreter.stringEnv.define("contains", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return str.contains(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string replace builtin function
        interpreter.stringEnv.define("replace", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                String newstr = (String)arguments.get(2);
                return str.replace(substr, newstr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string split builtin function
        interpreter.stringEnv.define("split", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return interpreter.makeObject(java.util.Arrays.asList(str.split(substr)), interpreter.arrayEnv, "Array");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string trim builtin function
        interpreter.stringEnv.define("trim", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.trim();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string ltrim builtin function
        interpreter.stringEnv.define("ltrim", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.replaceAll("^\\s+", "");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string rtrim builtin function
        interpreter.stringEnv.define("rtrim", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.replaceAll("\\s+$", "");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string padl builtin function: pad left
        interpreter.stringEnv.define("padl", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                int len = ((Double)arguments.get(1)).intValue();
                String pad = (String)arguments.get(2);
                return String.format("%1$" + len + "s", str).replace(' ', pad.charAt(0));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string padr builtin function: pad right
        interpreter.stringEnv.define("padr", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                int len = ((Double)arguments.get(1)).intValue();
                String pad = (String)arguments.get(2);
                return String.format("%1$-" + len + "s", str).replace(' ', pad.charAt(0));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        

        // string toUpper builtin function
        interpreter.stringEnv.define("toUpper", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.toUpperCase();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string toLower builtin function
        interpreter.stringEnv.define("toLower", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.toLowerCase();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string toNumber builtin function
        interpreter.stringEnv.define("toNumber", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                try {
                    return (double)Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        

        // string reverse builtin function
        interpreter.stringEnv.define("reverse", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return new StringBuilder(str).reverse().toString();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });  
        
        // charAt builtin function
        interpreter.stringEnv.define("charAt", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try {
                    String str = (String)arguments.get(0);
                    int index = ((Double)arguments.get(1)).intValue();
                    char result = str.charAt(index);
                    return String.valueOf(result);
                } catch(Exception e) {
                    return null;
                }
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string startsWith builtin function: return true if the string starts with the given substring
        interpreter.stringEnv.define("startsWith", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return str.startsWith(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string endsWith builtin function: return true if the string ends with the given substring
        interpreter.stringEnv.define("endsWith", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return str.endsWith(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string times builtin function: return a string that is the given string repeated n times
        interpreter.stringEnv.define("times", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                int n = ((Double)arguments.get(1)).intValue();
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<n; i++) {
                    sb.append(str);
                }
                return sb.toString();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string indexOf builtin function: return the index of the first occurrence of the given substring
        interpreter.stringEnv.define("indexOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return (double)str.indexOf(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string lastIndexOf builtin function: return the index of the last occurrence of the given substring
        interpreter.stringEnv.define("lastIndexOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                return (double)str.lastIndexOf(substr);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string occurs builtin function: return the number of occurrences of the given substring
        interpreter.stringEnv.define("occurs", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                String substr = (String)arguments.get(1);
                int count = 0;
                int index = str.indexOf(substr);
                while (index != -1) {
                    count++;
                    index = str.indexOf(substr, index+1);
                }
                return (double)count;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isAlpha() builtin function: return true if the string contains only alphabetic characters
        interpreter.stringEnv.define("isAlpha", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("[a-zA-Z]+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isAlphaNum() builtin function: return true if the string contains only alphabetic and numeric characters
        interpreter.stringEnv.define("isAlphaNum", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("[a-zA-Z0-9]+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isNumeric() builtin function: return true if the string contains only numeric characters
        interpreter.stringEnv.define("isNumeric", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("[0-9]+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isSpace() builtin function: return true if the string contains only whitespace characters
        interpreter.stringEnv.define("isSpace", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("\\s+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isUpper() builtin function: return true if the string contains only uppercase characters
        interpreter.stringEnv.define("isUpper", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("[A-Z]+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // string isLower() builtin function: return true if the string contains only lowercase characters
        interpreter.stringEnv.define("isLower", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                String str = (String)arguments.get(0);
                return str.matches("[a-z]+");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }

    /***********************************************************************
    * Map
    ***********************************************************************/
    private static void createMapBuiltins(Interpreter interpreter) {

        // map put builtin function
        interpreter.mapEnv.define("put", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    map.put(arguments.get(1), arguments.get(2));
                }
                return null;
            }
            
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map get builtin function
        interpreter.mapEnv.define("get", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return map.get(arguments.get(1));
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map set builtin function
        interpreter.mapEnv.define("set", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    map.put(arguments.get(1), arguments.get(2));
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map len builtin function
        interpreter.mapEnv.define("len", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return (double)map.size();
                }
                return 0.0;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map keys builtin function
        interpreter.mapEnv.define("keys", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new ArrayList<Object>(map.keySet()), interpreter.arrayEnv, "Array");
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map values builtin function
        interpreter.mapEnv.define("values", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new ArrayList<Object>(map.values()), interpreter.arrayEnv, "Array");
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map contains builtin function
        interpreter.mapEnv.define("contains", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return map.containsKey(arguments.get(1));
                }
                return false;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map remove builtin function
        interpreter.mapEnv.define("remove", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return map.remove(arguments.get(1));
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map clear builtin function
        interpreter.mapEnv.define("clear", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    map.clear();
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map isEmpty builtin function
        interpreter.mapEnv.define("isEmpty", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return map.isEmpty();
                }
                return true;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // map clone builtin function
        interpreter.mapEnv.define("clone", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    return interpreter.makeObject(new HashMap<Object, Object>(map), interpreter.mapEnv, "Map");
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }

    /***********************************************************************
    * Function prototype
    ***********************************************************************/
    private static void createFunctionBuiltins(Interpreter interpreter) {
        // function arity builtin function: extract the internal "value" which is the 
        // RuntimeFunction object and call its arity() method
        interpreter.functionEnv.define("arity", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    RuntimeFunction function = (RuntimeFunction)env.lookup("value");
                    // arity is the number of parameters, not including the "poThis" parameter
                    return function.arity();
                }
                return new Arity();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        }); 
    }

    /***********************************************************************
    * Class builtin functions
    ***********************************************************************/
    private static void createClassBuiltins(Interpreter interpreter) {
        // toString() builtin function: show the name of the class
        interpreter.classEnv.define("toString", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    return ((Environment)arguments.get(0)).name;                    
                }
                return "";
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // className(): return the name of the class. The name is the last word in the environment name.
        interpreter.classEnv.define("className", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    String name = ((Environment)arguments.get(0)).name;
                    return lastWord(name);
                }
                return "";
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // instanceOf(): return true if the object is an instance of the class
        // we need to lookup in the environment to find the class
        interpreter.classEnv.define("instanceOf", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    String className = (String)arguments.get(1);
                    String name = "";
                    do {
                        name = lastWord(env.name);
                        if (className.equals(name)) {
                            return true;
                        }
                        env = env.parent;
                    } while ((env = env.parent) != null);                    
                }
                return false;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }

    /***********************************************************************
    * Built-in functions
    ***********************************************************************/
    private static void createBuiltins(Interpreter interpreter) {
        // date() builtin function
        interpreter.globals.define("date", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity();
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {                
                return interpreter.makeObject(new Date(), interpreter.dateEnv, "Date");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        // readln builtin function: read a line from the console
        interpreter.globals.define("readln", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the prompt
                System.out.print(arguments.get(1));                
                return Hungaro.readLine();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // val() builtin function: convert a string to a number
        interpreter.globals.define("val", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                final Object arg = arguments.get(1);
                if (arg instanceof String) {
                    try {
                        return Double.parseDouble((String)arg);
                    } catch(NumberFormatException e) {
                        return 0.0;
                    }
                }
                return arg;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // type() builtin function: return the type of an object eg: 
        interpreter.globals.define("type", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                final Object arg = arguments.get(1);
                if (arg instanceof String) {
                    return "s";
                } else if (arg instanceof Double) {
                    return "n";
                } else if (arg instanceof Boolean) {
                    return "b";
                } else if (arg instanceof Environment) {
                    return ((Environment)arg).name;
                }
                return "u";
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // print builtin function: print a string to the console
        interpreter.globals.define("print", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(true);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the string to print
                // we start printing from argument 1 because argument 0 is the "poThis" parameter                

                for (int i = 1; i < arguments.size(); i++) {
                    System.out.print(Interpreter.currentBackColor + Interpreter.currentForeColor + interpreter.stringify(arguments.get(i)));
                }

                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // println builtin function: print a string to the console with a newline
        interpreter.globals.define("println", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(true);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the list to print
                
                for (int i = 1; i < arguments.size(); i++) {
                    System.out.println(Interpreter.currentBackColor + Interpreter.currentForeColor + interpreter.stringify(arguments.get(i)));
                }

                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // int() builtin function: trim the decimal part of a number
        interpreter.globals.define("int", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // check if the argument is a number
                if (arguments.get(1) instanceof Double) {
                    // convert from double to int
                    int result = (int)(double)arguments.get(1);
                    return Double.valueOf(result);
                }
                return Double.valueOf(0);                
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });   
        
        // space() builtin function: return a string of spaces
        interpreter.globals.define("space", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number of spaces
                // but we need to convert it to an integer
                return " ".repeat((int)(double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // chr() builtin function: return the string representation of a number (ASCII code)
        interpreter.globals.define("chr", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                // but we need to convert it to an integer
                // check if the argument is a number
                if (arguments.get(1) instanceof Double) {
                    // convert from double to int
                    int result = (int)(double)arguments.get(1);
                    return String.valueOf((char)result);
                }
                return String.valueOf((char)0);                
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        

        // sin() builtin function: return the sine of a number
        interpreter.globals.define("sin", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.sin((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });  
        
        // setConsoleForeColor() builtin function: set the console foreground color
        interpreter.globals.define("setConsoleForeColor", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the color
                // check if it is a string and if it is a valid color
                // the forecolor is in the Hungaro.foreColors map
                if (arguments.get(1) instanceof String) {
                    String color = (String)arguments.get(1);
                    if (Hungaro.foreColors.containsKey(color)) {
                        Interpreter.currentForeColor = Hungaro.foreColors.get(color);
                    }
                }            
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // setConsoleBackColor() builtin function: set the console background color
        interpreter.globals.define("setConsoleBackColor", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the color
                // check if it is a string and if it is a valid color
                // the backcolor is in the Hungaro.backColors map
                if (arguments.get(1) instanceof String) {
                    String color = (String)arguments.get(1);
                    if (Hungaro.backColors.containsKey(color)) {
                        Interpreter.currentBackColor = Hungaro.backColors.get(color);
                    }
                }            
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        }); 
        
        // assert() builtin function: assert that a condition is true
        // otherwise print an error message in red color
        interpreter.globals.define("assert", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the condition
                // the third argument is the error message
                if (!(boolean)arguments.get(1)) {
                    System.out.println(Hungaro.backColors.get("red") + Hungaro.foreColors.get("white") + "Assertion failed: " + arguments.get(2));
                    System.out.println(Hungaro.backColors.get("reset") + Hungaro.foreColors.get("reset") + " ");
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // rand(from, to) builtin function: return a random number between from and to
        interpreter.globals.define("rand", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the from number
                // the third argument is the to number
                // return the integer part of the random number
                return Math.floor(Math.random() * ((double)arguments.get(2) - (double)arguments.get(1) + 1) + (double)arguments.get(1));                
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // abs() builtin function: return the absolute value of a number
        interpreter.globals.define("abs", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.abs((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // sqrt() builtin function: return the square root of a number
        interpreter.globals.define("sqrt", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.sqrt((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // pow() builtin function: return the power of a number
        interpreter.globals.define("pow", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                // the third argument is the power
                return Math.pow((double)arguments.get(1), (double)arguments.get(2));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // round() builtin function: return the rounded value of a number
        interpreter.globals.define("round", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.round((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // floor() builtin function: return the floor value of a number
        interpreter.globals.define("floor", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.floor((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // ceil() builtin function: return the ceiling value of a number
        interpreter.globals.define("ceil", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                return Math.ceil((double)arguments.get(1));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // min() builtin function: return the minimum value of two numbers
        interpreter.globals.define("min", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the first number
                // the third argument is the second number
                return Math.min((double)arguments.get(1), (double)arguments.get(2));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // max() builtin function: return the maximum value of two numbers
        interpreter.globals.define("max", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the first number
                // the third argument is the second number
                return Math.max((double)arguments.get(1), (double)arguments.get(2));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // between() builtin function: return true if a number is between two numbers
        interpreter.globals.define("between", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(4);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number
                // the third argument is the first number
                // the fourth argument is the second number
                return ((double)arguments.get(1) >= (double)arguments.get(2) && (double)arguments.get(1) <= (double)arguments.get(3));
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // seconds() builtin function: return the number of seconds since the beginning of the program
        interpreter.globals.define("seconds", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // tick() builtin function: return the number of ticks since the beginning of the program
        interpreter.globals.define("tick", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // tack() builtin function: takes a tick() value and returns the number of seconds since that tick
        interpreter.globals.define("tack", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the tick value
                return (double)System.currentTimeMillis() / 1000.0 - (double)arguments.get(1);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // sleep() builtin function: sleep for a number of seconds
        interpreter.globals.define("sleep", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the number of seconds
                try {
                    Thread.sleep((long)((double)arguments.get(1) * 1000.0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // file() builtin function: check if a file exists
        interpreter.globals.define("file", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the file name
                return new File((String)arguments.get(1)).exists();
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // filetostr() builtin function: read a file and return its content as a string
        interpreter.globals.define("filetostr", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the file name
                try {
                    return new String(Files.readAllBytes(Paths.get((String)arguments.get(1))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // strtofile() builtin function: write a string to a file
        interpreter.globals.define("strtofile", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the file name
                // the third argument is the string
                try {
                    String content = interpreter.stringify(arguments.get(1));
                    byte[] bytes = content.getBytes();
                    Files.write(Paths.get((String)arguments.get(2)), bytes);
                } catch (IOException e) {
                    return null;
                }
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // curdir() builtin function: return the current directory
        interpreter.globals.define("curdir", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(1);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.getProperty("user.dir");
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // format() builtin function: use the java String.format() function to format a string
        // considering all wildcards as strings eg: %s, %d, %f, %c, %b
        // this function arity must return -1 to indicate that it can take any number of arguments
        interpreter.globals.define("format", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(true);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the first argument is the format string
                // the second argument is the first wildcard value
                // the third argument is the second wildcard value
                // etc...
                String format = (String)arguments.get(1);
                Object[] values = new Object[arguments.size() - 2];
                for (int i = 0; i < values.length; i++) {
                    values[i] = interpreter.stringify(arguments.get(i + 2));
                }
                return String.format(format, values);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        /*
        * Dialogs
        */
        // info(message, title) builtin function: show an information dialog
        interpreter.globals.define("info", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the message
                // the third argument is the title
                String message = (String)arguments.get(1);
                String title = "Information";
                if (arguments.size() > 2) {
                    title = (String)arguments.get(2);
                }
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // warning(message, title) builtin function: show a warning dialog
        interpreter.globals.define("warning", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the message
                // the third argument is the title
                String message = (String)arguments.get(1);
                String title = "Warning";
                if (arguments.size() > 2) {
                    title = (String)arguments.get(2);
                }
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // error(message, title) builtin function: show an error dialog
        interpreter.globals.define("error", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the message
                // the third argument is the title
                String message = (String)arguments.get(1);
                String title = "Error";
                if (arguments.size() > 2) {
                    title = (String)arguments.get(2);
                }
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // confirm(message, title) builtin function: show a confirmation dialog
        interpreter.globals.define("confirm", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the message
                // the third argument is the title
                String message = (String)arguments.get(1);
                String title = "Confirmation";
                if (arguments.size() > 2) {
                    title = (String)arguments.get(2);
                }
                return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // input(message, title) builtin function: show an input dialog
        interpreter.globals.define("input", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the message
                // the third argument is the title
                String message = (String)arguments.get(1);
                String title = "Input";
                if (arguments.size() > 2) {
                    title = (String)arguments.get(2);
                }
                return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // iif(condition, trueValue, falseValue) builtin function: return trueValue if condition is true, falseValue otherwise
        interpreter.globals.define("iif", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(4);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                final Object condition = interpreter.evaluate((Expr)arguments.get(1));
                if (interpreter.isTruthy(condition)) {
                    return interpreter.evaluate((Expr)arguments.get(2));
                } else {
                    return interpreter.evaluate((Expr)arguments.get(3));
                }                
            }
            
            @Override
            public boolean evaluateArguments() {
                return false;
            }
        });
    }   

    /***********************************************************************
    * Wrapper builtins functions
    ***********************************************************************/
    private static void defineWrapperBuiltins(Interpreter interpreter) {
        // get() builtin function: get a property from the wrapped object
        // the wrapper object is stored in 'value' property of the environment
        interpreter.wrapperEnv.define("message", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object wrappedObj = env.lookup("value");
                    return ((Exception)wrappedObj).getMessage();
                    // String propertyName = (String)arguments.get(1);
                    // return ((Wrapper)wrappedObj).get(propertyName);
                }                
                return null;
            }
            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });        
    }

    /***********************************************************************
     * Helpers functions
     * *********************************************************************/
    private static String lastWord(String word) {
        // return the last word of a string
        // ex: lastWord("hello world") returns "world"
        String[] words = word.split(" ");
        return words[words.length - 1];        
    }
}