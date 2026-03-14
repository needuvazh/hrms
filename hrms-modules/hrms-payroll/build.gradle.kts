plugins {
    id("hrms.module")
}

dependencies {
    api(project(":hrms-modules:hrms-contracts"))
    api(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-feature-toggle"))
    implementation(project(":hrms-platform:hrms-audit"))
    implementation(project(":hrms-platform:hrms-outbox"))
    implementation("io.micrometer:micrometer-core")
    implementation(project(":hrms-modules:hrms-workflow"))
    implementation(project(":hrms-modules:hrms-document"))
    implementation(project(":hrms-modules:hrms-notification"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework:spring-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
