import java.util.List;

public final class BuiltinsForString {
    public static void create(Interpreter interpreter) {
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
}
