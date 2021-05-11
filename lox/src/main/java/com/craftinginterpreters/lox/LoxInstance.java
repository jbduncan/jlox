package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class LoxInstance {

  private final LoxClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  LoxInstance(LoxClass klass) {
    this.klass = requireNonNull(klass);
  }

  Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    Optional<LoxFunction> method = klass.findMethod(name.lexeme);
    if (method.isPresent()) {
      return method.get().bind(this);
    }

    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }
}
