import com.craftinginterpreters.tool.GenerateAstTask

plugins {
    java
    idea

    id("com.diffplug.spotless") version "7.0.4"
}

group = "com.craftinginterpreters.lox"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated/sources/ast/java/main")
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(file("${layout.buildDirectory.get()}/generated/sources/ast/java/main"))
    }
}

dependencies {
    implementation("com.google.guava:guava:33.4.8-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "com.craftinginterpreters.lox.Lox")
    }
}

val generateAstTask = tasks.register<GenerateAstTask>("generateAst") {
    outputDirectory.set(file("$projectDir/build/generated/sources/ast/java/main/com/craftinginterpreters/lox"))
}

tasks.named("compileJava") {
    dependsOn(generateAstTask)
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.27.0")
    }
}
