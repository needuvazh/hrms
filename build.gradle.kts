allprojects {
    group = "com.company.hrms"
    version = "0.1.0-SNAPSHOT"
}

val integrationTestProjects = setOf(
    ":hrms-modules:hrms-leave",
    ":hrms-modules:hrms-employee",
    ":hrms-platform:hrms-outbox",
    ":hrms-platform:hrms-audit",
    ":hrms-modules:hrms-person",
    ":hrms-modules:hrms-recruitment"
)

subprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    if (path in integrationTestProjects) {
        tasks.register<Test>("integrationTest") {
            description = "Runs Testcontainers-based integration tests"
            group = "verification"
            useJUnitPlatform()
            filter {
                includeTestsMatching("*IntegrationTest")
                isFailOnNoMatchingTests = true
            }
            shouldRunAfter(tasks.named("test"))
        }
    }
}

tasks.register("integrationTestAll") {
    description = "Runs integration tests across selected HRMS modules"
    group = "verification"
    dependsOn(integrationTestProjects.map { "$it:integrationTest" })
}
