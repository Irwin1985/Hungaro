import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public final class BuiltinsForArray {
    public static void create(Interpreter interpreter) {
        // fConcat builtin function: an array takes another array and call makeObject() to create a new array
        interpreter.arrayEnv.define("fConcat", new CallableObject() {
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

        // array fIndexOf builtin function
        interpreter.arrayEnv.define("fIndexOf", new CallableObject() {
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

        // array fLastIndexOf builtin function
        interpreter.arrayEnv.define("fLastIndexOf", new CallableObject() {
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

        // array fContains builtin function
        interpreter.arrayEnv.define("fContains", new CallableObject() {
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

        // array fIsEmpty builtin function
        interpreter.arrayEnv.define("fIsEmpty", new CallableObject() {
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

        // array pSort builtin function: sort the array in ascending order
        interpreter.arrayEnv.define("pSort", new CallableObject() {
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
                                throw new Runtime.Error(null, "Array elements must be of the same type");
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
                                throw new Runtime.Error(null, "Array elements must be of the same type");
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
                        throw new Runtime.Error(null, "Array elements must be of the same type and either numbers or strings");
                    }
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array pReverse builtin function
        interpreter.arrayEnv.define("pReverse", new CallableObject() {
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
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array fFirst() builtin function
        interpreter.arrayEnv.define("fFirst", new CallableObject() {
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

        // array fLast() builtin function
        interpreter.arrayEnv.define("fLast", new CallableObject() {
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

        // array fRemove() removes the first occurrence of the given element
        interpreter.arrayEnv.define("fRemove", new CallableObject() {
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

        // array fRemoveAt() builtin function
        interpreter.arrayEnv.define("fRemoveAt", new CallableObject() {
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

        // array fInsertAt() builtin function
        interpreter.arrayEnv.define("fInsertAt", new CallableObject() {
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
        
        // array fOccurs() builtin function: returns the number of times the given element occurs in the array
        interpreter.arrayEnv.define("fOccurs", new CallableObject() {
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
        
        // array fTake() builtin function: returns a new array containing the first n elements of the given array
        // if the argument is negative then the last n elements are returned
        interpreter.arrayEnv.define("fTake", new CallableObject() {
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

        // array pPush builtin function: eg. array.pPush(1)
        interpreter.arrayEnv.define("pPush", new CallableObject() {
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

        // array fPop builtin function
        interpreter.arrayEnv.define("fPop", new CallableObject() {
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

        // array fLen builtin function
        interpreter.arrayEnv.define("fLen", new CallableObject() {
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

        // array fGet builtin function
        interpreter.arrayEnv.define("fGet", new CallableObject() {
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

        // array pSet builtin function
        interpreter.arrayEnv.define("pSet", new CallableObject() {
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

        // array fSlice builtin function
        interpreter.arrayEnv.define("fSlice", new CallableObject() {
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

        // array fJoin builtin function
        interpreter.arrayEnv.define("fJoin", new CallableObject() {
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

        // array pShuffle builtin function
        interpreter.arrayEnv.define("pShuffle", new CallableObject() {
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
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // array fEquals builtin function: the argument must be an array
        interpreter.arrayEnv.define("fEquals", new CallableObject() {
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

        // array fSum() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("fSum", new CallableObject() {
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

        // array fAvg() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("fAvg", new CallableObject() {
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

        // array fMin() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("fMin", new CallableObject() {
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

        // array fMax() builtin function: the argument must be an array of numbers
        interpreter.arrayEnv.define("fMax", new CallableObject() {
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
        
        // array fClone() builtin function: the argument must be an array
        interpreter.arrayEnv.define("fClone", new CallableObject() {
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

        // array pClear() builtin function: the argument must be an array
        interpreter.arrayEnv.define("pClear", new CallableObject() {
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
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fFilter(): takes a function and apply it to each element of the array filtering down
        // just those elements that return true
        interpreter.arrayEnv.define("fFilter", new CallableObject() {
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

        // fEvery(): takes a function and apply it to each element of the array. If all the
        // elements return true, then the function returns true, otherwise it returns false
        interpreter.arrayEnv.define("fEvery", new CallableObject() {
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

        // fSome(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns true, otherwise it returns false
        interpreter.arrayEnv.define("fSome", new CallableObject() {
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

        // fFind(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns the element, otherwise it returns null
        interpreter.arrayEnv.define("fFind", new CallableObject() {
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

        // fFindIndex(): takes a function and apply it to each element of the array. If at least one
        // element returns true, then the function returns the index of the element, otherwise it returns -1
        interpreter.arrayEnv.define("fFindIndex", new CallableObject() {
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

        // fReduce(): takes a function and apply it to each element of the array. The function
        // must take two arguments, the first one is the accumulator and the second one is the
        // current element. The function returns the accumulator. The initial value of the
        // accumulator is the first element of the array.
        interpreter.arrayEnv.define("fReduce", new CallableObject() {
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
