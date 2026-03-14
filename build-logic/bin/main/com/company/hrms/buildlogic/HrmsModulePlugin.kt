package com.company.hrms.buildlogic

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin

class HrmsModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("hrms.java")
        project.pluginManager.apply(JavaLibraryPlugin::class.java)
        project.pluginManager.apply("io.spring.dependency-management")

        val dependencyManagement = project.extensions.getByType(DependencyManagementExtension::class.java)
        dependencyManagement.imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.5")
        }

        project.dependencies.add("compileOnly", "io.swagger.core.v3:swagger-annotations-jakarta:2.2.22")
        project.dependencies.add("compileOnly", "com.fasterxml.jackson.core:jackson-annotations")
    }
}
