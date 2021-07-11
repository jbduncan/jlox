package com.craftinginterpreters.lox;

import static java.nio.charset.Charset.defaultCharset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Lox {

  private static final Interpreter interpreter = new Interpreter();

  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Path.of(path));
    run(new String(bytes, StandardCharsets.UTF_8));

    // Indicate an error in an exit code.
    if (hadError) {
      System.exit(65);
    }
    if (hadRuntimeError) {
      System.exit(70);
    }
  }

  private static void runPrompt() throws IOException {
    // These resources are not wrapped in try-with-resources because
    // System.in should never be closed by hand.
    var input = new InputStreamReader(System.in, defaultCharset());
    var reader = new BufferedReader(input);
    while (true) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      run(line);

      hadError = false;
    }
  }

  private static void run(String source) {
    List<Token> tokens = new Scanner(source).scanTokens();
    List<Stmt> statements = new Parser(tokens).parse();

    // Stop if there was a syntax error.
    if (hadError) {
      return;
    }

    new Resolver(interpreter).resolve(statements);

    // Stop if there was a resolution error.
    if (hadError) {
      return;
    }

    interpreter.interpret(statements);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  private Lox() {}
}
