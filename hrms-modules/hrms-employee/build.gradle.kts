plugins {
    id("hrms.module")
}

dependencies {
    implementation(project(":hrms-platform:hrms-shared-kernel"))
    api(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-feature-toggle"))
    implementation(project(":hrms-platform:hrms-audit"))
    implementation(project(":hrms-platform:hrms-outbox"))
    implementation(project(":hrms-modules:hrms-auth"))
    implementation(project(":hrms-modules:hrms-leave"))
    implementation(project(":hrms-modules:hrms-attendance"))
    implementation(project(":hrms-modules:hrms-workflow"))
    implementation(project(":hrms-modules:hrms-notification"))
    implementation(project(":hrms-modules:hrms-document"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:r2dbc-postgresql")
}
