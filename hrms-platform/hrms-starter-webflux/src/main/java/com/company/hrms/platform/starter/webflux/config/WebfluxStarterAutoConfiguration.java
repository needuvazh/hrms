package com.company.hrms.platform.starter.webflux.config;

import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class WebfluxStarterAutoConfiguration {

    @Bean
    Clock hrmsSystemClock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI hrmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS API")
                        .description("Reactive, multi-tenant HRMS APIs for monolith and component deployments.")
                        .version("v1")
                        .contact(new Contact()
                                .name("HRMS Platform Team")));
    }

    @Bean
    GroupedOpenApi tenantModuleOpenApi() {
        return moduleGroup("tenant", "com.company.hrms.tenant.infrastructure.web");
    }

    @Bean
    GroupedOpenApi authModuleOpenApi() {
        return moduleGroup("auth", "com.company.hrms.auth.infrastructure.web");
    }

    @Bean
    GroupedOpenApi masterDataModuleOpenApi() {
        return moduleGroup("master-data", "com.company.hrms.masterdata.infrastructure.web");
    }

    @Bean
    GroupedOpenApi personModuleOpenApi() {
        return moduleGroup("person", "com.company.hrms.person.infrastructure.web");
    }

    @Bean
    GroupedOpenApi employeeModuleOpenApi() {
        return moduleGroup("employee", "com.company.hrms.employee.infrastructure.web");
    }

    @Bean
    GroupedOpenApi recruitmentModuleOpenApi() {
        return moduleGroup("recruitment", "com.company.hrms.recruitment.infrastructure.web");
    }

    @Bean
    GroupedOpenApi attendanceModuleOpenApi() {
        return moduleGroup("attendance", "com.company.hrms.attendance.infrastructure.web");
    }

    @Bean
    GroupedOpenApi workflowModuleOpenApi() {
        return moduleGroup("workflow", "com.company.hrms.workflow.infrastructure.web");
    }

    @Bean
    GroupedOpenApi leaveModuleOpenApi() {
        return moduleGroup("leave", "com.company.hrms.leave.infrastructure.web");
    }

    @Bean
    GroupedOpenApi payrollModuleOpenApi() {
        return moduleGroup("payroll", "com.company.hrms.payroll.infrastructure.web");
    }

    @Bean
    GroupedOpenApi wpsModuleOpenApi() {
        return moduleGroup("wps", "com.company.hrms.wps.infrastructure.web");
    }

    @Bean
    GroupedOpenApi pasiModuleOpenApi() {
        return moduleGroup("pasi", "com.company.hrms.pasi.infrastructure.web");
    }

    @Bean
    GroupedOpenApi reportingModuleOpenApi() {
        return moduleGroup("reporting", "com.company.hrms.reporting.infrastructure.web");
    }

    @Bean
    GroupedOpenApi integrationHubModuleOpenApi() {
        return moduleGroup("integration-hub", "com.company.hrms.integrationhub.infrastructure.web");
    }

    @Bean
    GroupedOpenApi notificationModuleOpenApi() {
        return moduleGroup("notification", "com.company.hrms.notification.infrastructure.web");
    }

    @Bean
    GroupedOpenApi documentModuleOpenApi() {
        return moduleGroup("document", "com.company.hrms.document.infrastructure.web");
    }

    private GroupedOpenApi moduleGroup(String groupName, String packageToScan) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .packagesToScan(packageToScan)
                .build();
    }
}
