import com.craftinginterpreters.tool.GenerateAstTask

plugins {
    java
    idea

    id("com.diffplug.spotless") version "5.12.1"
}

group = "com.craftinginterpreters.lox"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

sourceSets {
    main {
        java {
            srcDirs("$buildDir/generated/sources/ast/java/main")
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(file("$buildDir/generated/sources/ast/java/main"))
    }
}

dependencies {
    implementation("com.google.guava:guava:30.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
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
        googleJavaFormat("1.10.0")
    }
}
