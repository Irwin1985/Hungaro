import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

public class Hungaro {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static boolean debugMode = true;

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

        for (;;) {
            System.out.print(">>> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false; // ignore errors in REPL mode.
        }
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
        interpreter.interpret(statements);        
    }

    static void error(int line, int col, String message) {
        report(line, col, "", message);
    }

    static void error(int line, int col, String where, String message) {
        report(line, col, where, message);
    }

    private static void report(int line, int col, String where, String message) {
        System.err.println(formatError("Parsing", line, col, where, message));
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, token.col, " at end", message);
        } else {
            report(token.line, token.col, token.lexeme, message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(formatError("Runtime", error.token.line, error.token.col, error.token.lexeme, error.getMessage()));
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
}

