package com.company.hrms.app.integration;

import com.company.hrms.platform.starter.webflux.dev.LocalPostgresTestcontainerBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsIntegrationApplication {

    public static void main(String[] args) {
        LocalPostgresTestcontainerBootstrap.startIfEnabled();
        SpringApplication.run(HrmsIntegrationApplication.class, args);
    }
}
