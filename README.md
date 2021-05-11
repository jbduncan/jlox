# jlox

My Java 11 implementation of Lox - a small programming language for scripting - from the
book [Crafting Interpreters](https://www.craftinginterpreters.com/).

# Build

`./gradlew build`

# Run as a REPL

`java -jar lox/build/libs/lox-0.1.0-SNAPSHOT.jar`

Alternatively, run Java file `com.craftinginterpreters.lox.Lox` in your IDE of choice.

# Run with a Lox program

`java -jar lox/build/libs/lox-0.1.0-SNAPSHOT.jar <path-to-lox-program>`
