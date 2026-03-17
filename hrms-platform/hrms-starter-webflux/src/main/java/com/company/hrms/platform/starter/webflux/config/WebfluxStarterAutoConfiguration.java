package com.company.hrms.platform.starter.webflux.config;

import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@AutoConfiguration
@EnableConfigurationProperties(WebfluxCorsProperties.class)
public class WebfluxStarterAutoConfiguration {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    Clock hrmsSystemClock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI hrmsOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .info(new Info()
                        .title("HRMS API")
                        .description("Reactive, multi-tenant HRMS APIs for monolith and component deployments.")
                        .version("v1")
                        .contact(new Contact()
                                .name("HRMS Platform Team")));
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    CorsWebFilter hrmsCorsWebFilter(WebfluxCorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return new CorsWebFilter(source);
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
        return moduleGroup(
                "master-data",
                "com.company.hrms.masterdata.controller",
                "com.company.hrms.masterdata.reference.interfaces.rest");
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

    private GroupedOpenApi moduleGroup(String groupName, String... packagesToScan) {
        return GroupedOpenApi.builder()
                .group(groupName)
                .packagesToScan(packagesToScan)
                .build();
    }
}
