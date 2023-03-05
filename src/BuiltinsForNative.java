import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

public final class BuiltinsForNative {
    public static void create(Interpreter interpreter) {
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

        // httpget() builtin function: perform an HTTP GET request
        interpreter.globals.define("httpget", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the URL
                try {
                    URL url = new URL((String)arguments.get(1));
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        return response.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        /*
        * Database functions
        */
        // connect() builtin function: connect to a database. takes a mapEnv as argument
        interpreter.globals.define("connect", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }
           
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(1) instanceof Environment) {
                    Environment env = (Environment)arguments.get(1);
                    if (env.name != "Map") {
                        return null;
                    }
                    HashMap<Object, Object> map = (HashMap<Object, Object>)env.lookup("value");
                    final String driver = (String)map.get("driver");
                    if (driver == null) {
                        throw new Runtime.Error(null, "The database driver is not specified.");
                    }
                    final String server = (String)map.get("server");
                    if (server == null) {
                        throw new Runtime.Error(null, "The database server is not specified.");
                    }
                    String database = (String)map.get("database");
                    if (database == null) database = "";

                    Object port = map.get("port");                    

                    final String user = (String)map.get("user");
                    if (user == null) {
                        throw new Runtime.Error(null, "The database user is not specified.");
                    }
                    String password = (String)map.get("password");
                    if (password == null) password = "";
                    
                    EngineManager engine = Hungaro.dbEngines.get(driver);
                    if (engine == null) {
                        throw new Runtime.Error(null, "The database driver '" + driver + "' is not supported.");
                    }                    
                    final String connectionString = engine.getConnectionString(server, port, database);
                    try {
                        Class.forName(engine.getDriverClass());
                        Connection connection = DriverManager.getConnection(connectionString, user, password);
                        Environment connectionEnv = interpreter.makeObject(connection, interpreter.connectionEnv, "Connection");                        
                        // add the driver name to the connection environment
                        connectionEnv.define("driver", driver);
                        return connectionEnv;
                    } catch (SQLException | ClassNotFoundException e) {
                        throw new Runtime.Error(null, "Could not connect to the database: " + e.getMessage());
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // disconnect() builtin function: disconnect from a database
        interpreter.globals.define("disconnect", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(1) instanceof Environment) {
                    // extract the connection object from the environment
                    try {
                        Environment env = (Environment)arguments.get(1);
                        if (env.name != "Connection") {
                            return null;
                        }
                        Connection connection = (Connection)env.lookup("value");
                        connection.close();
                    } catch (SQLException e) {
                        throw new Runtime.Error(null, "Could not disconnect from the database: " + e.getMessage());
                    }
                }
                return null;
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
}
