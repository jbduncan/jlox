package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.AND;
import static com.craftinginterpreters.lox.TokenType.BANG;
import static com.craftinginterpreters.lox.TokenType.BANG_EQUAL;
import static com.craftinginterpreters.lox.TokenType.CLASS;
import static com.craftinginterpreters.lox.TokenType.COMMA;
import static com.craftinginterpreters.lox.TokenType.DOT;
import static com.craftinginterpreters.lox.TokenType.ELSE;
import static com.craftinginterpreters.lox.TokenType.EQUAL;
import static com.craftinginterpreters.lox.TokenType.EQUAL_EQUAL;
import static com.craftinginterpreters.lox.TokenType.FALSE;
import static com.craftinginterpreters.lox.TokenType.FOR;
import static com.craftinginterpreters.lox.TokenType.FUN;
import static com.craftinginterpreters.lox.TokenType.GREATER;
import static com.craftinginterpreters.lox.TokenType.GREATER_EQUAL;
import static com.craftinginterpreters.lox.TokenType.IDENTIFIER;
import static com.craftinginterpreters.lox.TokenType.IF;
import static com.craftinginterpreters.lox.TokenType.LEFT_BRACE;
import static com.craftinginterpreters.lox.TokenType.LEFT_PAREN;
import static com.craftinginterpreters.lox.TokenType.LESS;
import static com.craftinginterpreters.lox.TokenType.LESS_EQUAL;
import static com.craftinginterpreters.lox.TokenType.MINUS;
import static com.craftinginterpreters.lox.TokenType.NIL;
import static com.craftinginterpreters.lox.TokenType.NUMBER;
import static com.craftinginterpreters.lox.TokenType.OR;
import static com.craftinginterpreters.lox.TokenType.PLUS;
import static com.craftinginterpreters.lox.TokenType.PRINT;
import static com.craftinginterpreters.lox.TokenType.RETURN;
import static com.craftinginterpreters.lox.TokenType.RIGHT_BRACE;
import static com.craftinginterpreters.lox.TokenType.RIGHT_PAREN;
import static com.craftinginterpreters.lox.TokenType.SEMICOLON;
import static com.craftinginterpreters.lox.TokenType.SLASH;
import static com.craftinginterpreters.lox.TokenType.STAR;
import static com.craftinginterpreters.lox.TokenType.STRING;
import static com.craftinginterpreters.lox.TokenType.SUPER;
import static com.craftinginterpreters.lox.TokenType.THIS;
import static com.craftinginterpreters.lox.TokenType.TRUE;
import static com.craftinginterpreters.lox.TokenType.VAR;
import static com.craftinginterpreters.lox.TokenType.WHILE;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNullElse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Scanner {

  private static final Map<String, TokenType> KEYWORD_TO_TOKEN =
      Map.ofEntries(
          entry("and", AND),
          entry("class", CLASS),
          entry("else", ELSE),
          entry("false", FALSE),
          entry("for", FOR),
          entry("fun", FUN),
          entry("if", IF),
          entry("nil", NIL),
          entry("or", OR),
          entry("print", PRINT),
          entry("return", RETURN),
          entry("super", SUPER),
          entry("this", THIS),
          entry("true", TRUE),
          entry("var", VAR),
          entry("while", WHILE));

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else if (match('*')) {
          blockComment();
        } else {
          addToken(SLASH);
        }

      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;

      case '"':
        string();
        break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String text = source.substring(start, current);
    var typeToken = KEYWORD_TO_TOKEN.get(text);
    addToken(requireNonNullElse(typeToken, IDENTIFIER));
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) {
        advance();
      }
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private void blockComment() {
    while (!(isEndOfBlockComment() || isAtEnd())) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated block comment.");
      return;
    }

    // The closing * and /.
    advance();
    advance();
  }

  private boolean isEndOfBlockComment() {
    return peek() == '*' && peekNext() == '/';
  }

  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != expected) {
      return false;
    }

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) {
      return '\0';
    }
    return source.charAt(current + 1);
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
