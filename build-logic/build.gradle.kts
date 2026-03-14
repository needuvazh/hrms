import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.company.hrms.buildlogic"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.3.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.6")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

gradlePlugin {
    plugins {
        register("hrmsJava") {
            id = "hrms.java"
            implementationClass = "com.company.hrms.buildlogic.HrmsJavaPlugin"
        }
        register("hrmsModule") {
            id = "hrms.module"
            implementationClass = "com.company.hrms.buildlogic.HrmsModulePlugin"
        }
        register("hrmsWebfluxApp") {
            id = "hrms.webflux-app"
            implementationClass = "com.company.hrms.buildlogic.HrmsWebfluxAppPlugin"
        }
    }
}
