package com.company.hrms.employee.config;

import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeModuleClient;
import com.company.hrms.employee.infrastructure.client.LocalEmployeeModuleClient;
import com.company.hrms.employee.infrastructure.client.WebClientEmployeeModuleClient;
import com.company.hrms.platform.sharedkernel.remote.ModuleClientDescriptor;
import com.company.hrms.platform.sharedkernel.remote.ModuleClientMode;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(EmployeeClientProperties.class)
public class EmployeeModuleConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "hrms.module.employee.client", name = "mode", havingValue = "remote")
    EmployeeModuleClient remoteEmployeeModuleClient(
            WebClient.Builder webClientBuilder,
            TenantContextAccessor tenantContextAccessor,
            EmployeeClientProperties clientProperties
    ) {
        if (!StringUtils.hasText(clientProperties.baseUrl())) {
            throw new IllegalStateException("hrms.module.employee.client.base-url must be provided when mode=remote");
        }

        ModuleClientDescriptor descriptor = new ModuleClientDescriptor("employee", ModuleClientMode.REMOTE, clientProperties.baseUrl());
        return new WebClientEmployeeModuleClient(
                webClientBuilder.baseUrl(descriptor.baseUrl()).build(),
                tenantContextAccessor);
    }

    @Bean
    @ConditionalOnMissingBean(EmployeeModuleClient.class)
    EmployeeModuleClient localEmployeeModuleClient(EmployeeModuleApi employeeModuleApi) {
        return new LocalEmployeeModuleClient(employeeModuleApi);
    }
}
