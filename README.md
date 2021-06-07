# jlox

My Java 11 implementation of Lox - a small programming language for scripting - from the
book [Crafting Interpreters](https://www.craftinginterpreters.com/).

One big difference between the
[reference implementation](https://github.com/munificent/craftinginterpreters) and this
one is, whereas the reference implementation generates the `Expr` and `Stmt` classes
by concatenating strings together, this implementation uses
[JavaPoet](https://github.com/square/javapoet), a Java library for generating Java
Java source files in a more type-safe way.

# Build

`./gradlew build`

# Run as a REPL

`java -jar lox/build/libs/lox-0.1.0-SNAPSHOT.jar`

Alternatively, run Java file `com.craftinginterpreters.lox.Lox` in your IDE of choice.

# Run with a Lox program

`java -jar lox/build/libs/lox-0.1.0-SNAPSHOT.jar <path-to-lox-program>`
