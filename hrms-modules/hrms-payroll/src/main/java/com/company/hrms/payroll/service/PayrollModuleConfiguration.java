package com.company.hrms.payroll.service;

import com.company.hrms.payroll.model.*;

import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.payroll.service.PayrollModuleClient;
import com.company.hrms.payroll.service.impl.LocalPayrollModuleClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayrollModuleConfiguration {

    @Bean
    @ConditionalOnMissingBean(PayrollModuleClient.class)
    PayrollModuleClient localPayrollModuleClient(PayrollModuleApi payrollModuleApi) {
        return new LocalPayrollModuleClient(payrollModuleApi);
    }
}
