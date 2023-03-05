import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BuiltinsForDatabase {
    public static void create(Interpreter interpreter) {
        // fSelect(sQuery) builtin function: execute a query and return the result as a list of maps
        interpreter.connectionEnv.define("fSelect", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    String query = (String)arguments.get(1);
                    // declare Statement and ResultSet
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        resultSet = statement.executeQuery(query);
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        List<Environment> result = new ArrayList<>();
                        while (resultSet.next()) {
                            Map<String, Object> row = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.put(metaData.getColumnName(i), resultSet.getObject(i));
                            }
                            result.add(interpreter.makeObject(row, interpreter.mapEnv, "Map"));
                        }
                        // finally, return the result                        
                        return interpreter.makeObject(result, interpreter.arrayEnv, "Array");
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement or result set: " + e.getMessage());
                        }
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fExecute(sQuery) builtin function: execute a query and return the number of affected rows
        interpreter.connectionEnv.define("fExecute", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    String query = (String)arguments.get(1);
                    // declare Statement
                    Statement statement = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        // the result must be a double
                        double result = statement.executeUpdate(query);
                        if (result < 0) return 0.0;
                        return result;
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement: " + e.getMessage());
                        }
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGetColumns(sTable) builtin function: return the list of columns of a table
        interpreter.connectionEnv.define("fGetColumns", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    // extract the driver
                    String driver = (String)env.lookup("driver");
                    // extract the engine manager from Hungaro map
                    EngineManager engineManager = Hungaro.dbEngines.get(driver);

                    // second argument is the database name
                    String database = (String)arguments.get(1);
                    // third argument is the table name
                    String table = (String)arguments.get(2);

                    // declare Statement and ResultSet
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        resultSet = statement.executeQuery(engineManager.getColumns(database, table));
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        List<Environment> result = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Map<String, Object> column = new HashMap<>();
                            column.put("name", metaData.getColumnName(i));
                            column.put("type", metaData.getColumnTypeName(i));
                            column.put("size", metaData.getColumnDisplaySize(i));
                            column.put("nullable", metaData.isNullable(i) == ResultSetMetaData.columnNullable);
                            result.add(interpreter.makeObject(column, interpreter.mapEnv, "Map"));
                        }
                        // finally, return the result                        
                        return interpreter.makeObject(result, interpreter.arrayEnv, "Array");
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement or result set: " + e.getMessage());
                        }
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGetTables() builtin function: return the list of tables in the provided database.
        interpreter.connectionEnv.define("fGetTables", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(2);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    final String driver = (String)env.lookup("driver");
                    EngineManager engineManager = Hungaro.dbEngines.get(driver);
                    // second argumment is the database name
                    String database = (String)arguments.get(1);

                    // declare Statement and ResultSet
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        resultSet = statement.executeQuery(engineManager.getTables(database));
                        // create an array with the field TABLE_NAME
                        
                        List<String> tables = new ArrayList<>();
                        while (resultSet.next()) {
                            tables.add(resultSet.getString(1));
                        }

                        // finally, return the result                        
                        return interpreter.makeObject(tables, interpreter.arrayEnv, "Array");
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement or result set: " + e.getMessage());
                        }
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fGetPrimaryKey(sTable) builtin function: return the list of primary keys of a table
        interpreter.connectionEnv.define("fGetPrimaryKey", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    final String driver = (String)env.lookup("driver");
                    // extract the engine manager from Hungaro map
                    EngineManager engineManager = Hungaro.dbEngines.get(driver);
                    // second argumment is the database name
                    String database = (String)arguments.get(1);
                    // third argumment is the database name
                    String table = (String)arguments.get(2);

                    // declare Statement and ResultSet
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        resultSet = statement.executeQuery(engineManager.getPrimaryKey(database, table));
                        // create an array with the field TABLE_NAME
                        
                        List<String> primaryKeys = new ArrayList<>();
                        while (resultSet.next()) {
                            primaryKeys.add(resultSet.getString(1));
                        }

                        // finally, return the result                        
                        return interpreter.makeObject(primaryKeys, interpreter.arrayEnv, "Array");
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement or result set: " + e.getMessage());
                        }
                    }
                }                
                return null;
            }

            @Override
            public boolean evaluateArguments() {
                return true;
            }
        });

        // fTableExists(sDatabase, sTable) builtin function: return true if the table exists in the database
        interpreter.connectionEnv.define("fTableExists", new CallableObject() {
            @Override
            public Arity arity() {
                return new Arity(3);
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof Environment) {
                    Environment env = (Environment)arguments.get(0);
                    Object connectionObj = env.lookup("value");
                    final String driver = (String)env.lookup("driver");
                    // extract the engine manager from Hungaro map
                    EngineManager engineManager = Hungaro.dbEngines.get(driver);
                    // second argumment is the database name
                    String database = (String)arguments.get(1);
                    // third argumment is the database name
                    String table = (String)arguments.get(2);

                    // declare Statement and ResultSet
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        statement = ((Connection)connectionObj).createStatement();
                        resultSet = statement.executeQuery(engineManager.tableExists(database, table));
                        // create an array with the field TABLE_NAME
                        
                        return resultSet.next();
                    } catch (SQLException e) {
                        throw new RuntimeError(null, "Could not execute query: " + e.getMessage());
                    } finally {
                        try {
                            if (statement != null) {
                                statement.close();
                            }
                            if (resultSet != null) {
                                resultSet.close();
                            }
                        } catch (SQLException e) {
                            throw new RuntimeError(null, "Could not close statement or result set: " + e.getMessage());
                        }
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
