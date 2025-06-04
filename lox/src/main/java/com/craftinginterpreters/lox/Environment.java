package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class Environment {
  final @Nullable Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();

  Environment() {
    this.enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = requireNonNull(enclosing);
  }

  void define(String name, Object value) {
    values.put(requireNonNull(name), value);
  }

  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme(), value);
  }

  Environment ancestor(int distance) {
    var environment = this;
    for (int i = 0; i < distance; i++) {
      // We're trusting that Resolver gave us the correct distance to get to the Environment that
      // has the variable we're interested in.
      environment = requireNonNull(environment).enclosing;
    }

    return environment;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme())) {
      return values.get(name.lexeme());
    }

    if (enclosing != null) {
      return enclosing.get(name);
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
  }

  void assign(Token name, Object value) {
    requireNonNull(name);

    if (values.containsKey(name.lexeme())) {
      values.put(name.lexeme(), value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
  }
}
