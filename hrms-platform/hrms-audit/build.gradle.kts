plugins {
    id("hrms.module")
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    implementation(project(":hrms-platform:hrms-starter-tenancy"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.postgresql:r2dbc-postgresql")
}
