package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

final class Token {
  final TokenType type;
  final String lexeme;
  final @Nullable Object literal;
  final int line;

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = requireNonNull(type);
    this.lexeme = requireNonNull(lexeme);
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
