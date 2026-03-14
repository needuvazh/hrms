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
    implementation(project(":hrms-modules:hrms-auth"))
    implementation(project(":hrms-modules:hrms-master-data"))
    implementation(project(":hrms-modules:hrms-person"))
    implementation(project(":hrms-modules:hrms-employee"))
    implementation(project(":hrms-modules:hrms-recruitment"))
    implementation(project(":hrms-modules:hrms-document"))

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("hrms-app-core-hr.jar")
}
