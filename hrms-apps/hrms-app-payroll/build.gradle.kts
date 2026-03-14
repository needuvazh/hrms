plugins {
    id("hrms.webflux-app")
}

dependencies {
    implementation(project(":hrms-platform:hrms-starter-webflux"))
    implementation(project(":hrms-platform:hrms-starter-security"))
    implementation(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-observability"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-feature-toggle"))
    implementation(project(":hrms-platform:hrms-outbox"))
    implementation(project(":hrms-platform:hrms-audit"))

    implementation(project(":hrms-modules:hrms-tenant"))
    implementation(project(":hrms-modules:hrms-payroll"))
    implementation(project(":hrms-modules:hrms-wps"))
    implementation(project(":hrms-modules:hrms-pasi"))
    implementation(project(":hrms-modules:hrms-notification"))

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("hrms-app-payroll.jar")
}
