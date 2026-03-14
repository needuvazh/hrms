package com.company.hrms.employee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hrms.module.employee.client")
public record EmployeeClientProperties(
        String mode,
        String baseUrl
) {
}
