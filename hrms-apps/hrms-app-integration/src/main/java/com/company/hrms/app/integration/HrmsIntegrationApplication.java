package com.company.hrms.app.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsIntegrationApplication.class, args);
    }
}
