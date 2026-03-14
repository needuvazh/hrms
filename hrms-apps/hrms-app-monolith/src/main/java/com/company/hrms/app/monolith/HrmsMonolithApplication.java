package com.company.hrms.app.monolith;

import com.company.hrms.platform.starter.webflux.dev.LocalPostgresTestcontainerBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsMonolithApplication {

    public static void main(String[] args) {
        LocalPostgresTestcontainerBootstrap.startIfEnabled();
        SpringApplication.run(HrmsMonolithApplication.class, args);
    }
}
