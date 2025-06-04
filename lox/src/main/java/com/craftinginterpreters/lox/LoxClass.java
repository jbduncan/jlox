package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

final class LoxClass implements LoxCallable {

  final String name;
  final @Nullable LoxClass superclass;
  private final Map<String, LoxFunction> methods;

  public LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
    this.name = requireNonNull(name);
    this.superclass = superclass;
    this.methods = requireNonNull(methods);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    var instance = new LoxInstance(this);
    findMethod("init")
        .ifPresent(initializer -> initializer.bind(instance).call(interpreter, arguments));
    return instance;
  }

  @Override
  public int arity() {
    return findMethod("init").map(LoxFunction::arity).orElse(0);
  }

  @Override
  public String toString() {
    return name;
  }

  public Optional<LoxFunction> findMethod(String name) {
    if (methods.containsKey(name)) {
      return Optional.of(methods.get(name));
    }

    if (superclass != null) {
      return superclass.findMethod(name);
    }

    return Optional.empty();
  }
}
