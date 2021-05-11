import com.google.googlejavaformat.java.Formatter

buildscript {
    dependencies {
        classpath("com.google.googlejavaformat:google-java-format:1.10.0")
    }
}

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    api("com.google.googlejavaformat:google-java-format:1.9")
    api("com.squareup:javapoet:1.13.0")
}

val format = tasks.register("format") {
    val javaFiles = fileTree("$projectDir") {
        include("src/**/*.java")
    }.sorted()

    // Usually, one would consider javaFiles to be both the inputs and outputs of this task.
    // However, we purposefully declare them only as the outputs here so that Gradle doesn't
    // mistakenly think that this task is always UP-TO-DATE.
    outputs.files(javaFiles)

    doLast {
        for (file in javaFiles) {
            file.writeText(Formatter().formatSourceAndFixImports(file.readText()))
        }
    }
}

tasks.withType<AbstractCompile> {
    dependsOn(format)
}
