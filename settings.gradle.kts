pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "hrms-root"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

includeBuild("build-logic")

include(":hrms-platform")
include(":hrms-platform:hrms-starter-webflux")
include(":hrms-platform:hrms-starter-security")
include(":hrms-platform:hrms-starter-tenancy")
include(":hrms-platform:hrms-starter-observability")
include(":hrms-platform:hrms-starter-error")
include(":hrms-platform:hrms-outbox")
include(":hrms-platform:hrms-feature-toggle")
include(":hrms-platform:hrms-shared-kernel")
include(":hrms-platform:hrms-audit")

include(":hrms-modules")
include(":hrms-modules:hrms-contracts")
include(":hrms-modules:hrms-tenant")
include(":hrms-modules:hrms-auth")
include(":hrms-modules:hrms-master-data")
include(":hrms-modules:hrms-person")
include(":hrms-modules:hrms-employee")
include(":hrms-modules:hrms-recruitment")
include(":hrms-modules:hrms-notification")
include(":hrms-modules:hrms-document")
include(":hrms-modules:hrms-attendance")
include(":hrms-modules:hrms-workflow")
include(":hrms-modules:hrms-leave")
include(":hrms-modules:hrms-payroll")
include(":hrms-modules:hrms-wps")
include(":hrms-modules:hrms-pasi")
include(":hrms-modules:hrms-reporting")
include(":hrms-modules:hrms-integration-hub")

include(":hrms-apps")
include(":hrms-apps:hrms-app-monolith")
include(":hrms-apps:hrms-app-core-hr")
include(":hrms-apps:hrms-app-workforce")
include(":hrms-apps:hrms-app-payroll")
include(":hrms-apps:hrms-app-integration")
