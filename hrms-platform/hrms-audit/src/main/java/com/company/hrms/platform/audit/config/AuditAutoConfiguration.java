package com.company.hrms.platform.audit.config;

import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.audit.api.NoopAuditEventPublisher;
import com.company.hrms.platform.audit.infrastructure.R2dbcAuditEventRepository;
import com.company.hrms.platform.audit.service.AuditEventRepository;
import com.company.hrms.platform.audit.service.DefaultAuditEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

@AutoConfiguration
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnBean(DatabaseClient.class)
    @ConditionalOnMissingBean
    AuditEventRepository auditEventRepository(DatabaseClient databaseClient, ObjectMapper objectMapper) {
        return new R2dbcAuditEventRepository(databaseClient, objectMapper);
    }

    @Bean
    @ConditionalOnBean(AuditEventRepository.class)
    @ConditionalOnMissingBean
    AuditEventPublisher auditEventPublisher(AuditEventRepository auditEventRepository) {
        return new DefaultAuditEventPublisher(auditEventRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AuditEventPublisher.class)
    AuditEventPublisher noopAuditEventPublisher() {
        return new NoopAuditEventPublisher();
    }
}
