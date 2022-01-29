package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.Nullable;

record Token(TokenType type, String lexeme, @Nullable Object literal, int line) {

  Token {
    requireNonNull(type);
    requireNonNull(lexeme);
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
