plugins {
    id("hrms.module")
}

dependencies {
    api(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-platform:hrms-feature-toggle"))
    implementation(project(":hrms-modules:hrms-payroll"))
    implementation(project(":hrms-modules:hrms-employee"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework:spring-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
