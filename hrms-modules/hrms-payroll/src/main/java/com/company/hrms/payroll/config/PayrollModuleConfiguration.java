package com.company.hrms.payroll.config;

import com.company.hrms.payroll.api.PayrollModuleApi;
import com.company.hrms.payroll.api.PayrollModuleClient;
import com.company.hrms.payroll.infrastructure.client.LocalPayrollModuleClient;
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
