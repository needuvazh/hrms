package com.company.hrms.platform.outbox.config;

import com.company.hrms.platform.outbox.api.LoggingOutboxDispatcher;
import com.company.hrms.platform.outbox.api.NoopOutboxPublisher;
import com.company.hrms.platform.outbox.api.OutboxDispatcher;
import com.company.hrms.platform.outbox.api.OutboxEventHandler;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.outbox.domain.OutboxEventStore;
import com.company.hrms.platform.outbox.infrastructure.R2dbcOutboxEventStore;
import com.company.hrms.platform.outbox.service.DefaultOutboxDispatcher;
import com.company.hrms.platform.outbox.service.LoggingOutboxEventHandler;
import com.company.hrms.platform.outbox.service.PersistentOutboxPublisher;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

@AutoConfiguration
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    OutboxEventStore outboxEventStore(DatabaseClient databaseClient) {
        return new R2dbcOutboxEventStore(databaseClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    OutboxPublisher outboxPublisher(OutboxEventStore outboxEventStore) {
        return new PersistentOutboxPublisher(outboxEventStore);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    OutboxEventHandler loggingOutboxEventHandler() {
        return new LoggingOutboxEventHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    OutboxDispatcher outboxDispatcher(OutboxEventStore outboxEventStore, List<OutboxEventHandler> handlers) {
        return new DefaultOutboxDispatcher(outboxEventStore, handlers);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    LoggingOutboxDispatcher loggingOutboxDispatcher(OutboxDispatcher outboxDispatcher) {
        return new LoggingOutboxDispatcher(outboxDispatcher);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hrms.outbox", name = "enabled", havingValue = "false")
    @ConditionalOnMissingBean
    OutboxPublisher noopOutboxPublisher() {
        return new NoopOutboxPublisher();
    }
}
