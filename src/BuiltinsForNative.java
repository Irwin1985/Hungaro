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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("unchecked")
public final class BuiltinsForNative {
    public static void create(Interpreter interpreter) {
        // fDate() builtin function
        interpreter.globals.define("fDate", new CallableObject() {
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

        // fReadLn builtin function: read a line from the console
        interpreter.globals.define("fReadLn", new CallableObject() {
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

        // fVal() builtin function: convert a string to a number
        interpreter.globals.define("fVal", new CallableObject() {
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

        // fType() builtin function: return the type of an object eg: 
        interpreter.globals.define("fType", new CallableObject() {
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

        // pPrint builtin function: print a string to the console
        interpreter.globals.define("pPrint", new CallableObject() {
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

        // pPrintLn builtin function: print a string to the console with a newline
        interpreter.globals.define("pPrintLn", new CallableObject() {
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

        // fInt() builtin function: trim the decimal part of a number
        interpreter.globals.define("fInt", new CallableObject() {
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
        
        // fSpace() builtin function: return a string of spaces
        interpreter.globals.define("fSpace", new CallableObject() {
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

        // fChr() builtin function: return the string representation of a number (ASCII code)
        interpreter.globals.define("fChr", new CallableObject() {
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
        

        // fSin() builtin function: return the sine of a number
        interpreter.globals.define("fSin", new CallableObject() {
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
        
        // pSetConsoleForeColor() builtin function: set the console foreground color
        interpreter.globals.define("pSetConsoleForeColor", new CallableObject() {
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
        
        // pSetConsoleBackColor() builtin function: set the console background color
        interpreter.globals.define("pSetConsoleBackColor", new CallableObject() {
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
        
        // pAssert() builtin function: assert that a condition is true
        // otherwise print an error message in red color
        interpreter.globals.define("pAssert", new CallableObject() {
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

        // fRand(from, to) builtin function: return a random number between from and to
        interpreter.globals.define("fRand", new CallableObject() {
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

        // fAbs() builtin function: return the absolute value of a number
        interpreter.globals.define("fAbs", new CallableObject() {
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

        // fSqrt() builtin function: return the square root of a number
        interpreter.globals.define("fSqrt", new CallableObject() {
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

        // fPow() builtin function: return the power of a number
        interpreter.globals.define("fPow", new CallableObject() {
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

        // fRound() builtin function: return the rounded value of a number
        interpreter.globals.define("fRound", new CallableObject() {
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

        // fFloor() builtin function: return the floor value of a number
        interpreter.globals.define("fFloor", new CallableObject() {
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
        
        // fCeil() builtin function: return the ceiling value of a number
        interpreter.globals.define("fCeil", new CallableObject() {
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

        // fMin() builtin function: return the minimum value of two numbers
        interpreter.globals.define("fMin", new CallableObject() {
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

        // fMax() builtin function: return the maximum value of two numbers
        interpreter.globals.define("fMax", new CallableObject() {
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

        // fBetween() builtin function: return true if a number is between two numbers
        interpreter.globals.define("fBetween", new CallableObject() {
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
        
        // fSeconds() builtin function: return the number of seconds since the beginning of the program
        interpreter.globals.define("fSeconds", new CallableObject() {
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

        // fTick() builtin function: return the number of ticks since the beginning of the program
        interpreter.globals.define("fTick", new CallableObject() {
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

        // fTack() builtin function: takes a tick() value and returns the number of seconds since that tick
        interpreter.globals.define("fTack", new CallableObject() {
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

        // fSleep() builtin function: sleep for a number of seconds
        interpreter.globals.define("fSleep", new CallableObject() {
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

        // fFile() builtin function: check if a file exists
        interpreter.globals.define("fFile", new CallableObject() {
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
        
        // fFileToStr() builtin function: read a file and return its content as a string
        interpreter.globals.define("fFileToStr", new CallableObject() {
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

        // fStrToFile() builtin function: write a string to a file
        interpreter.globals.define("fStrToFile", new CallableObject() {
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
        
        // fCurDir() builtin function: return the current directory
        interpreter.globals.define("fCurDir", new CallableObject() {
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

        // fFormat() builtin function: use the java String.format() function to format a string
        // considering all wildcards as strings eg: %s, %d, %f, %c, %b
        // this function arity must return -1 to indicate that it can take any number of arguments
        interpreter.globals.define("fFormat", new CallableObject() {
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

        // fHttpGet() builtin function: perform an HTTP GET request
        interpreter.globals.define("fHttpGet", new CallableObject() {
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
        // fConnect() builtin function: connect to a database. takes a mapEnv as argument
        interpreter.globals.define("fConnect", new CallableObject() {
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

        // fDisconnect() builtin function: disconnect from a database
        interpreter.globals.define("fDisconnect", new CallableObject() {
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
        // pInfo(message, title) builtin function: show an information dialog
        interpreter.globals.define("pInfo", new CallableObject() {
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

        // pWarning(message, title) builtin function: show a warning dialog
        interpreter.globals.define("pWarning", new CallableObject() {
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

        // pError(message, title) builtin function: show an error dialog
        interpreter.globals.define("pError", new CallableObject() {
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

        // fConfirm(message, title) builtin function: show a confirmation dialog
        interpreter.globals.define("fConfirm", new CallableObject() {
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

        // fInput(message, title) builtin function: show an input dialog
        interpreter.globals.define("fInput", new CallableObject() {
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

        // fSelectFile(title, filter) builtin function: show a file selection dialog
        // the filter is the file extension, eg: "txt"
        interpreter.globals.define("fSelectFile", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the title
                // the third argument is the filter
                String title = (String)arguments.get(1);
                String filter = (String)arguments.get(2);
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle(title);
                if (filter != null) {
                    FileNameExtensionFilter fnef = new FileNameExtensionFilter(filter, filter);
                    chooser.setFileFilter(fnef);
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    return chooser.getSelectedFile().getAbsolutePath();
                } else {
                    return "";
                }
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGetFiles(path, filter) builtin function: return a list of files in the given path
        // the filter is a regular expression that is applied to the file names
        // if the filter is null, all files are returned
        // the search is recursive and will find the matched files in all subdirectories
        // eg: fGetFiles("c:\\temp", ".*\\.txt") the example will return all .txt files in the c:\temp directory
        // including the files in the subdirectories        
        interpreter.globals.define("fGetFiles", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the path
                // the third argument is the filter
                String path = (String)arguments.get(1);
                String filter = (String)arguments.get(2);
                if (filter == null) {
                    filter = ".*";
                }
                Pattern pattern = Pattern.compile(filter);
                List<String> files = new ArrayList<String>();
                getFiles(path, pattern, files);
                return interpreter.makeObject(files, interpreter.arrayEnv, "Array");
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fEval(expression) builtin function: evaluate the given expression using the Hungaro interpreter pipeline
        // eg: fEval("1 + 2") will return 3
        // we need to call Hungaro.run() to execute the expression
        interpreter.globals.define("fEval", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the expression
                String expression = (String)arguments.get(1);
                List<Token> tokens = null;        
                try {
                    // tokenize the expression
                    final Scanner scanner = new Scanner(expression);
                    tokens = scanner.scanTokens();
                    if (Hungaro.hadError) return null;
                    
                    // parse the expression
                    Parser parser = new Parser(tokens);
                    Stmt exprStmt = parser.expressionStmt();
                    if (Hungaro.hadError) return null;

                    // execute the expression
                    return interpreter.evaluate(((Stmt.Expression)exprStmt).expression);
                } catch (Exception e) {
                    // print in red color
                    System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
                } finally {
                    Hungaro.hadError = false;
                    Hungaro.hadRuntimeError = false;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
        
        // fExecute(command) builtin function: execute the given command using the Hungaro interpreter pipeline
        // unlike fEval, fExecute will execute the given command as a whole program.
        // eg: fExecute("print 1 + 2") will print 3
        // we need to call Hungaro.run() to execute the command
        interpreter.globals.define("fExecute", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                // the second argument is the command
                String command = (String)arguments.get(1);
                List<Token> tokens = null;        
                try {
                    // tokenize the command
                    final Scanner scanner = new Scanner(command);
                    tokens = scanner.scanTokens();
                    if (Hungaro.hadError) return null;
                    
                    // parse the command
                    Parser parser = new Parser(tokens);
                    List<Stmt> statements = parser.parse();
                    if (Hungaro.hadError) return null;

                    // execute the command
                    interpreter.interpret(statements);
                } catch (Exception e) {
                    // print in red color
                    System.out.println("\u001B[31m" + e.getMessage() + "\u001B[0m");
                } finally {
                    Hungaro.hadError = false;
                    Hungaro.hadRuntimeError = false;
                }
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });
    }

    /*
    * Helper function for fGetFiles
    */
    // getFiles is a recursive function that will find all files in the given path that match the given pattern
    protected static void getFiles(String path, Pattern pattern, List<String> files) {        
        File[] fileArray = new File(path).listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isDirectory()) {
                    // if the file is a directory, call getFiles recursively
                    getFiles(file.getAbsolutePath(), pattern, files);
                } else {
                    // if the file is not a directory, check if it matches the pattern
                    if (pattern.matcher(file.getName()).matches()) {
                        // if it matches, add it to the list                        
                        files.add(file.getAbsolutePath());
                    }
                }
            }
        }        
    }
}
