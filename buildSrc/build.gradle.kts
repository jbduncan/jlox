plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api("com.squareup:javapoet:1.13.0")
    implementation("com.google.guava:guava:30.1-jre")
}
