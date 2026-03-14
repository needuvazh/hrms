package com.company.hrms.app.monolith.config;

import com.company.hrms.platform.starter.security.api.AuthorizationRulesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class MonolithSecurityPolicyConfiguration {

    @Bean
    AuthorizationRulesCustomizer monolithAuthorizationRulesCustomizer() {
        return exchanges -> exchanges
                .pathMatchers(HttpMethod.GET, "/api/v1/employees", "/api/v1/employees/**")
                .hasAnyAuthority("PERM_EMPLOYEE_READ", "PERM_EMPLOYEE_WRITE")
                .pathMatchers(HttpMethod.POST, "/api/v1/employees", "/api/v1/employees/**")
                .hasAuthority("PERM_EMPLOYEE_WRITE")
                .pathMatchers(HttpMethod.PUT, "/api/v1/employees/**")
                .hasAuthority("PERM_EMPLOYEE_WRITE")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/employees/**")
                .hasAuthority("PERM_EMPLOYEE_WRITE")
                .pathMatchers(HttpMethod.GET, "/api/v1/persons", "/api/v1/persons/**")
                .hasAnyAuthority("PERM_PERSON_READ", "PERM_PERSON_WRITE")
                .pathMatchers(HttpMethod.POST, "/api/v1/persons", "/api/v1/persons/**")
                .hasAuthority("PERM_PERSON_WRITE")
                .pathMatchers(HttpMethod.GET, "/api/v1/recruitment/candidates", "/api/v1/recruitment/candidates/**")
                .hasAnyAuthority("PERM_RECRUITMENT_READ", "PERM_RECRUITMENT_WRITE")
                .pathMatchers(HttpMethod.POST, "/api/v1/recruitment/candidates", "/api/v1/recruitment/candidates/**")
                .hasAuthority("PERM_RECRUITMENT_WRITE")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/recruitment/candidates/**")
                .hasAuthority("PERM_RECRUITMENT_WRITE");
    }
}
