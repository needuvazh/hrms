package com.company.hrms.platform.starter.tenancy.config;

import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class TenancyStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TenantContextWebFilter tenantContextWebFilter() {
        return new TenantContextWebFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    TenantContextAccessor tenantContextAccessor() {
        return new DefaultTenantContextAccessor();
    }
}
