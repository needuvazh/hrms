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
        return moduleGroup("tenant", "com.company.hrms.tenant.controller");
    }

    @Bean
    GroupedOpenApi authModuleOpenApi() {
        return moduleGroup("auth", "com.company.hrms.auth.controller");
    }

    @Bean
    GroupedOpenApi masterDataModuleOpenApi() {
        return moduleGroup("master-data", "com.company.hrms.masterdata.controller");
    }

    @Bean
    GroupedOpenApi personModuleOpenApi() {
        return moduleGroup("person", "com.company.hrms.person.controller");
    }

    @Bean
    GroupedOpenApi employeeModuleOpenApi() {
        return moduleGroup("employee", "com.company.hrms.employee.controller");
    }

    @Bean
    GroupedOpenApi recruitmentModuleOpenApi() {
        return moduleGroup("recruitment", "com.company.hrms.recruitment.controller");
    }

    @Bean
    GroupedOpenApi attendanceModuleOpenApi() {
        return moduleGroup("attendance", "com.company.hrms.attendance.controller");
    }

    @Bean
    GroupedOpenApi workflowModuleOpenApi() {
        return moduleGroup("workflow", "com.company.hrms.workflow.controller");
    }

    @Bean
    GroupedOpenApi leaveModuleOpenApi() {
        return moduleGroup("leave", "com.company.hrms.leave.controller");
    }

    @Bean
    GroupedOpenApi payrollModuleOpenApi() {
        return moduleGroup("payroll", "com.company.hrms.payroll.controller");
    }

    @Bean
    GroupedOpenApi wpsModuleOpenApi() {
        return moduleGroup("wps", "com.company.hrms.wps.controller");
    }

    @Bean
    GroupedOpenApi pasiModuleOpenApi() {
        return moduleGroup("pasi", "com.company.hrms.pasi.controller");
    }

    @Bean
    GroupedOpenApi reportingModuleOpenApi() {
        return moduleGroup("reporting", "com.company.hrms.reporting.controller");
    }

    @Bean
    GroupedOpenApi integrationHubModuleOpenApi() {
        return moduleGroup("integration-hub", "com.company.hrms.integrationhub.controller");
    }

    @Bean
    GroupedOpenApi notificationModuleOpenApi() {
        return moduleGroup("notification", "com.company.hrms.notification.controller");
    }

    @Bean
    GroupedOpenApi documentModuleOpenApi() {
        return moduleGroup("document", "com.company.hrms.document.controller");
    }

    private GroupedOpenApi moduleGroup(String groupName, String packageToScan) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .packagesToScan(packageToScan)
                .build();
    }
}
