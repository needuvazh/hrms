package com.company.hrms.app.workforce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsWorkforceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsWorkforceApplication.class, args);
    }
}
