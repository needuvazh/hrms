package com.company.hrms.employee.service;

import com.company.hrms.employee.model.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "hrms.module.employee.client")
@Getter
@Setter
public class EmployeeClientProperties {
    private String mode = "local";
    private String baseUrl = "http://localhost:8080";
}
