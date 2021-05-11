package com.craftinginterpreters.lox;

final class Return extends RuntimeException {
  final Object value;

  Return(Object value) {
    // Disable some overhead like stack traces, which we don't need for return values.
    super(null, null, false, false);
    this.value = value;
  }
}
