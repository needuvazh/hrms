package com.company.hrms.app.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.company.hrms")
public class HrmsMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsMonolithApplication.class, args);
    }
}
