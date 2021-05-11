package com.craftinginterpreters.lox;

import static java.util.Objects.requireNonNull;

import com.craftinginterpreters.lox.Stmt.Function;
import java.util.List;

final class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean initializer;

  LoxFunction(Function declaration, Environment closure, boolean initializer) {
    this.declaration = requireNonNull(declaration, "declaration");
    this.closure = requireNonNull(closure, "closure");
    this.initializer = initializer;
  }

  LoxFunction bind(LoxInstance instance) {
    requireNonNull(instance);
    var environment = new Environment(closure);
    environment.define("this", instance);
    return new LoxFunction(declaration, environment, initializer);
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    var environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      if (initializer) {
        // The Resolve ensures that Lox code never tries to return a value from an initializer.
        // For example, this is invalid:
        //
        //     class Foo {
        //       init() {
        //         return "foo"; <- invalid
        //       }
        //     }
        //
        // Therefore this point could only have ever been reached with a return that doesn't have a
        // value. For example:
        //
        //     class Foo {
        //       init() {
        //         return; <- only possible kind of return
        //       }
        //     }
        //
        // In this case, the return returns `this`, similar to how calling an instance's `init()`
        // directly returns `this` too.
        return closure.getAt(0, "this");
      }

      return returnValue.value;
    }

    return initializer ? closure.getAt(0, "this") : null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
