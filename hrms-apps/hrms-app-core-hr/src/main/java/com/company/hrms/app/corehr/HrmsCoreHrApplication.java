package com.company.hrms.app.corehr;

import com.company.hrms.platform.starter.webflux.dev.LocalPostgresTestcontainerBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsCoreHrApplication {

    public static void main(String[] args) {
        LocalPostgresTestcontainerBootstrap.startIfEnabled();
        SpringApplication.run(HrmsCoreHrApplication.class, args);
    }
}
