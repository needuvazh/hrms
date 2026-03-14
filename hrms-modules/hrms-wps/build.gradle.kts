plugins {
    id("hrms.module")
}

dependencies {
    implementation(project(":hrms-modules:hrms-contracts"))
    api(project(":hrms-platform:hrms-starter-tenancy"))
    implementation(project(":hrms-platform:hrms-starter-error"))
    implementation(project(":hrms-modules:hrms-payroll"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework:spring-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
