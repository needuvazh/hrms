plugins {
    id("hrms.module")
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    implementation(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-audit"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework:spring-web")
    implementation("io.projectreactor:reactor-core")
    implementation(project(":hrms-platform:hrms-shared-kernel"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
