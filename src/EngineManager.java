
public abstract class EngineManager {
    public abstract String getConnectionString(String server, Object port, String database);
    public abstract String getDriverClass();
    public abstract String getTables(String database);
    public abstract String getColumns(String database, String tableName);
    public abstract String getPrimaryKey(String database, String tableName);
    public abstract String tableExists(String database, String tableName);

    // MySql implementation class
    public static class MySql extends EngineManager {

        @Override
        public String getConnectionString(String server, Object port, String database) {
            if (port == null) {
                port = "3306";
            }
            
            return String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", server, port, database);
        }

        @Override
        public String getDriverClass() {
            return "com.mysql.cj.jdbc.Driver";
        }

        @Override
        public String getTables(String database) {            
            return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";                        
        }

        @Override
        public String getColumns(String database, String tableName) {
            return "SELECT * FROM " + database + "." + tableName + " WHERE 1 = 0";
        }

        @Override
        public String getPrimaryKey(String database, String tableName) {
            final String Query = """
                SELECT COLUMN_NAME AS PKFIELD
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = '@DB_NAME'
                  AND TABLE_NAME = '@TBL_NAME'
                  AND COLUMN_KEY = 'PRI'                                    
                """;
            return Query.replace("@DB_NAME", database).replace("@TBL_NAME", tableName);
        }

        @Override
        public String tableExists(String database, String tableName) {
            final String Query = """
                SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '@DB_NAME' AND TABLE_NAME = '@TBL_NAME';
                """;
            return Query.replace("@DB_NAME", database).replace("@TBL_NAME", tableName);
        }
    }

    // MsSql implementation class
    public static class MsSql extends EngineManager {

        @Override
        public String getConnectionString(String server, Object port, String database) {
            String url = String.format("jdbc:sqlserver://%s", server);
            if (port != null) {
                url += ":" + port;
            }
            if (database != null && !database.isEmpty()) {
                url += ";database=" + database;
            }
            url += ";integratedSecurity=false;encrypt=true;trustServerCertificate=true;loginTimeout=30;";
            return url;
        }

        @Override
        public String getDriverClass() {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }

        @Override
        public String getTables(String database) {
            return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_CATALOG = '" + database + "'";
        }

        @Override
        public String getColumns(String database, String tableName) {
            return "SELECT * FROM " + database + ".." + tableName + " WHERE 1 = 0";
        }        

        @Override
        public String getPrimaryKey(String database, String tableName) {
            final String Query = """
                SELECT K.COLUMN_NAME AS PKFIELD FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE K 
                INNER JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS TC 
                ON K.TABLE_CATALOG = TC.TABLE_CATALOG 
                AND K.TABLE_SCHEMA = TC.TABLE_SCHEMA 
                AND K.CONSTRAINT_NAME = TC.CONSTRAINT_NAME 
                WHERE TC.TABLE_CATALOG = '@DB_NAME' AND TC.CONSTRAINT_TYPE = 'PRIMARY KEY' AND K.TABLE_NAME = '@TBL_NAME';                                    
                """;
            return Query.replace("@DB_NAME", database).replace("@TBL_NAME", tableName);
        }

        @Override
        public String tableExists(String database, String tableName) {
            final String Query = """
                SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = '@DB_NAME' AND TABLE_NAME = '@TBL_NAME';
                """;
            return Query.replace("@DB_NAME", database).replace("@TBL_NAME", tableName);
        }
    }
}