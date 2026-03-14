package com.company.hrms.app.corehr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsCoreHrApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsCoreHrApplication.class, args);
    }
}
