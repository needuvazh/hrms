package com.company.hrms.platform.featuretoggle.config;

import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.AlwaysEnabledFeatureToggleService;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.featuretoggle.api.ModuleEnablementAdminApi;
import com.company.hrms.platform.featuretoggle.infrastructure.R2dbcFeatureToggleRepository;
import com.company.hrms.platform.featuretoggle.service.DefaultFeatureToggleService;
import com.company.hrms.platform.featuretoggle.service.DefaultModuleEnablementAdminService;
import com.company.hrms.platform.featuretoggle.service.FeatureToggleRepository;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

@AutoConfiguration
public class FeatureToggleAutoConfiguration {

    @Bean
    @ConditionalOnBean(DatabaseClient.class)
    @ConditionalOnMissingBean
    FeatureToggleRepository featureToggleRepository(DatabaseClient databaseClient) {
        return new R2dbcFeatureToggleRepository(databaseClient);
    }

    @Bean
    @ConditionalOnBean(FeatureToggleRepository.class)
    @ConditionalOnMissingBean
    FeatureToggleService featureToggleService(
            FeatureToggleRepository featureToggleRepository,
            TenantContextAccessor tenantContextAccessor
    ) {
        return new DefaultFeatureToggleService(featureToggleRepository, tenantContextAccessor);
    }

    @Bean
    @ConditionalOnMissingBean(FeatureToggleService.class)
    FeatureToggleService alwaysEnabledFeatureToggleService() {
        return new AlwaysEnabledFeatureToggleService();
    }

    @Bean
    @ConditionalOnMissingBean
    EnablementGuard enablementGuard(FeatureToggleService featureToggleService) {
        return new EnablementGuard(featureToggleService);
    }

    @Bean
    @ConditionalOnBean(FeatureToggleRepository.class)
    @ConditionalOnMissingBean
    ModuleEnablementAdminApi moduleEnablementAdminApi(
            FeatureToggleRepository featureToggleRepository,
            AuditEventPublisher auditEventPublisher
    ) {
        return new DefaultModuleEnablementAdminService(featureToggleRepository, auditEventPublisher);
    }
}
