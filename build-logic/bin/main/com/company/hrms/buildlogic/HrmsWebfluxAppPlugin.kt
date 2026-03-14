package com.company.hrms.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

class HrmsWebfluxAppPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("hrms.java")
        project.pluginManager.apply("org.springframework.boot")
        project.pluginManager.apply("io.spring.dependency-management")

        project.dependencies.add("implementation", "org.springframework.boot:spring-boot-starter-webflux")
        project.dependencies.add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
        project.dependencies.add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
        project.dependencies.add("testImplementation", "io.projectreactor:reactor-test")
        project.dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}
