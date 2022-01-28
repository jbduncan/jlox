package com.craftinginterpreters.tool

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.IOException

open class GenerateAstTask : DefaultTask() {
    @get:OutputDirectory
    val outputDirectory = project.objects.directoryProperty()

    @TaskAction
    fun exec() {
        try {
            GenerateAstWithJavaPoet.main(outputDirectory.asFile.get().path)
        } catch (exception: IOException) {
            throw GradleException("Code generation failed", exception)
        }
    }
}