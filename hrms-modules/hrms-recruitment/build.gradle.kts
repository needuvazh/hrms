plugins {
    id("hrms.module")
}

dependencies {
    implementation(project(":hrms-modules:hrms-contracts"))
    api(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-feature-toggle"))
    implementation(project(":hrms-platform:hrms-audit"))
    implementation(project(":hrms-platform:hrms-outbox"))
    implementation(project(":hrms-modules:hrms-person"))
    implementation(project(":hrms-modules:hrms-employee"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:r2dbc-postgresql")
}
