package com.company.hrms.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion

class HrmsJavaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)

        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        javaExtension.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        javaExtension.withSourcesJar()

        project.tasks.withType(Test::class.java).configureEach {
            useJUnitPlatform()
        }

        project.dependencies.add("compileOnly", "org.projectlombok:lombok:1.18.34")
        project.dependencies.add("annotationProcessor", "org.projectlombok:lombok:1.18.34")
        project.dependencies.add("testCompileOnly", "org.projectlombok:lombok:1.18.34")
        project.dependencies.add("testAnnotationProcessor", "org.projectlombok:lombok:1.18.34")
    }
}
