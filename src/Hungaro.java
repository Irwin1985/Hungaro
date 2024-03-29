import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class Hungaro {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static boolean debugMode = true; // debug mode
    static String hungaroVersion = "alpha 0.0.3"; // Hungaro version

    // foreground colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    // background colors
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    // put all forecolors in a map
    public static final Map<String, String> foreColors = new HashMap<String, String>() {{        
        put("reset", ANSI_RESET);
        put("black", ANSI_BLACK);
        put("red", ANSI_RED);
        put("green", ANSI_GREEN);
        put("yellow", ANSI_YELLOW);
        put("blue", ANSI_BLUE);
        put("purple", ANSI_PURPLE);
        put("cyan", ANSI_CYAN);
        put("white", ANSI_WHITE);
    }};

    // put all backcolors in a map
    public static final Map<String, String> backColors = new HashMap<String, String>() {{
        put("reset", ANSI_RESET);
        put("black", ANSI_BLACK_BACKGROUND);
        put("red", ANSI_RED_BACKGROUND);
        put("green", ANSI_GREEN_BACKGROUND);
        put("yellow", ANSI_YELLOW_BACKGROUND);
        put("blue", ANSI_BLUE_BACKGROUND);
        put("purple", ANSI_PURPLE_BACKGROUND);
        put("cyan", ANSI_CYAN_BACKGROUND);
        put("white", ANSI_WHITE_BACKGROUND);
    }};

    // a map with all database engines supported (key: engine name, value: class name)
    // eg: mysql -> new EngineManager.MySql(), etc.
    public static final Map<String, EngineManager> dbEngines = new HashMap<String, EngineManager>() {{
        put("mysql", new EngineManager.MySql());        
        put("mssql", new EngineManager.MsSql());
    }};
    

    public static void main(String[] args) throws IOException {
        // debug
        if (debugMode) {
            runFile("F:\\Desarrollo\\GitHub\\Hungaro\\sample.hgr");
            return;
        }
        // end debug

        if (args.length > 1) {
            System.out.println("Usage: hungaro [file]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        printPrompt();
        for (;;) {
            System.out.print(">>> ");            
            String line = reader.readLine();
            if (line == null) break;
            
            // Start REPL commands
            if (line.equals("exit")) break;
            if (line.equals("help")) {
                printHelp();
                continue;
            }
            if (line.startsWith("run ")) {
                final String file = line.substring(4).trim();
                try {
                    runFile(file);
                } catch(Exception ex) {
                    System.out.println(ANSI_RED + ex.getMessage() + ANSI_RESET);
                } finally {
                    hadError = false; // ignore errors in REPL mode.
                    hadRuntimeError = false; // ignore errors in REPL mode.
                }
                continue;
            }            
            if (line.equals("cls")) {
                System.out.print("\033[H\033[2J");  
                System.out.flush();
                printPrompt();
                continue;
            }
            if (line.equals("version")) {
                System.out.println("Hungaro v" + hungaroVersion);
                continue;
            }
            if (line.equals("tests")) {
                System.out.println("Running tests...");                
                String testsFile = getMainFolder() + "\\examples\\tests.hgr";
                if (Files.exists(Paths.get(testsFile)))
                    runFile(testsFile);
                else
                    System.out.println("Tests file not found.");
                continue;
            }
            // End REPL commands

            // run line if not a command
            run(line);
            hadError = false; // ignore errors in REPL mode.
        }
    }

    private static void printPrompt() {
        System.out.println("Hungaro v" + hungaroVersion);
        System.out.println("Date: " + new java.util.Date());
        printHelp();
    }

    private static void printHelp() {
        System.out.println("Type 'help' for help.");
        System.out.println("Type 'exit' to exit.");        
        System.out.println("Type 'run <file>' to run a file.");
        System.out.println("Type 'cls' to clear the screen.");
        System.out.println("Type 'version' to show the version.");
    }

    public static void run(String source) {
        List<Token> tokens = null;        
        try {
            final Scanner scanner = new Scanner(source);
            tokens = scanner.scanTokens();
            final boolean printTokens = false;
            // debug
            if (printTokens) {
                System.out.println("=============================");
                for (Token token : tokens) {
                    System.out.println(token);
                }
                System.out.println("=============================");
            }
            // end debug
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        if (hadError) return;

        // continue parsing
        Parser parser = new Parser(tokens);        
        List<Stmt> statements = parser.parse();            
        if (hadError) return;
        try {
            interpreter.interpret(statements);        
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    static void error(int line, int col, String message) {
        report(line, col, "", message);
    }

    static void error(int line, int col, String where, String message) {
        report(line, col, where, message);
    }

    private static void report(int line, int col, String where, String message) {
        System.err.println(ANSI_RED + formatError("Parsing", line, col, where, message));
        System.err.println(ANSI_RESET+"");
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.col, " at end", message);
        } else {
            report(token.line, token.col, token.lexeme, message);
        }
    }

    static void runtimeError(Runtime.Error error) {
        if (error.token == null) {
            System.err.println(ANSI_RED + error.getMessage());
            System.err.println(ANSI_RESET+"");
            return;
        }
        System.err.println(ANSI_RED + formatError("Runtime", error.token.line, error.token.col, error.token.lexeme, error.getMessage()));
        System.err.println(ANSI_RESET+"");
        hadRuntimeError = true;
    }

    static String formatError(String errorStr, int line, int col, String where, String msg) {
        return String.format("[%s:%s] - %s error near of `%s`: %s", line, col, errorStr, where, msg);
    }

    // check if a string is a constant (all uppercase)
    public static boolean isConstant(String name) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[A-Z_]+$");
        java.util.regex.Matcher matcher = pattern.matcher(name);
        return matcher.find();
    }

    public static String getTypeOf(Token token, char type) {
        switch (type) {
            case 'n':
                return "Number";
            case 's':
                return "String";
            case 'b':
                return "Boolean";
            case 'a':
                return "Array";
            case 'm':
                return "Map";
            case 'o':
                return "Object";
            default:
                throw new Runtime.Error(token, "Invalid " + type + " type.");
        }        
    }

    // function that reads a line from the console
    public static String readLine() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    // helper function
    private static String getMainFolder() {
        final String currentFolder = System.getProperty("user.dir");                
        final String parentFolder = Paths.get(currentFolder).getParent().toString();                                
        return parentFolder;
    }
}