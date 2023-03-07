import java.util.List;

public final class BuiltinsForString {
    public static void create(Interpreter interpreter) {
        // string fLen builtin function
        interpreter.stringEnv.define("fLen", new CallableObject() {
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

        // string fSlice builtin function
        interpreter.stringEnv.define("fSlice", new CallableObject() {
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

        // string fFind builtin function
        interpreter.stringEnv.define("fFind", new CallableObject() {
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

        // string fContains builtin function
        interpreter.stringEnv.define("fContains", new CallableObject() {
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

        // string fReplace builtin function
        interpreter.stringEnv.define("fReplace", new CallableObject() {
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

        // string fSplit builtin function
        interpreter.stringEnv.define("fSplit", new CallableObject() {
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

        // string fTrim builtin function
        interpreter.stringEnv.define("fTrim", new CallableObject() {
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

        // string fLtrim builtin function
        interpreter.stringEnv.define("fLtrim", new CallableObject() {
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

        // string fRtrim builtin function
        interpreter.stringEnv.define("fRtrim", new CallableObject() {
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

        // string fPadl builtin function: pad left
        interpreter.stringEnv.define("fPadl", new CallableObject() {
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

        // string fPadr builtin function: pad right
        interpreter.stringEnv.define("fPadr", new CallableObject() {
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

        // string fToUpper builtin function
        interpreter.stringEnv.define("fToUpper", new CallableObject() {
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

        // string fToLower builtin function
        interpreter.stringEnv.define("fToLower", new CallableObject() {
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

        // string fToNumber builtin function
        interpreter.stringEnv.define("fToNumber", new CallableObject() {
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

        // string fReverse builtin function
        interpreter.stringEnv.define("fReverse", new CallableObject() {
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
        
        // fCharAt builtin function
        interpreter.stringEnv.define("fCharAt", new CallableObject() {
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

        // string fStartsWith builtin function: return true if the string starts with the given substring
        interpreter.stringEnv.define("fStartsWith", new CallableObject() {
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

        // string fEndsWith builtin function: return true if the string ends with the given substring
        interpreter.stringEnv.define("fEndsWith", new CallableObject() {
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

        // string fTimes builtin function: return a string that is the given string repeated n times
        interpreter.stringEnv.define("fTimes", new CallableObject() {
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

        // string fIndexOf builtin function: return the index of the first occurrence of the given substring
        interpreter.stringEnv.define("fIndexOf", new CallableObject() {
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

        // string fLastIndexOf builtin function: return the index of the last occurrence of the given substring
        interpreter.stringEnv.define("fLastIndexOf", new CallableObject() {
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

        // string fOccurs builtin function: return the number of occurrences of the given substring
        interpreter.stringEnv.define("fOccurs", new CallableObject() {
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

        // string fIsAlpha() builtin function: return true if the string contains only alphabetic characters
        interpreter.stringEnv.define("fIsAlpha", new CallableObject() {
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

        // string fIsAlphaNum() builtin function: return true if the string contains only alphabetic and numeric characters
        interpreter.stringEnv.define("fIsAlphaNum", new CallableObject() {
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

        // string fIsNumeric() builtin function: return true if the string contains only numeric characters
        interpreter.stringEnv.define("fIsNumeric", new CallableObject() {
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

        // string fIsSpace() builtin function: return true if the string contains only whitespace characters
        interpreter.stringEnv.define("fIsSpace", new CallableObject() {
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

        // string fIsUpper() builtin function: return true if the string contains only uppercase characters
        interpreter.stringEnv.define("fIsUpper", new CallableObject() {
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

        // string fIsLower() builtin function: return true if the string contains only lowercase characters
        interpreter.stringEnv.define("fIsLower", new CallableObject() {
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
}
