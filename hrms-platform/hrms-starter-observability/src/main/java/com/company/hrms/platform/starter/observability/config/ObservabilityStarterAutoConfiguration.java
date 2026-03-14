package com.company.hrms.platform.starter.observability.config;

import com.company.hrms.platform.starter.observability.actuator.HrmsInfoContributor;
import com.company.hrms.platform.starter.observability.web.ContextPropagatingWebClientCustomizer;
import com.company.hrms.platform.starter.observability.web.CorrelationIdWebFilter;
import com.company.hrms.platform.starter.observability.web.RequestObservabilityWebFilter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ObservabilityStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CorrelationIdWebFilter correlationIdWebFilter() {
        return new CorrelationIdWebFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    RequestObservabilityWebFilter requestObservabilityWebFilter(MeterRegistry meterRegistry) {
        return new RequestObservabilityWebFilter(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    WebClientCustomizer contextPropagatingWebClientCustomizer() {
        return new ContextPropagatingWebClientCustomizer();
    }

    @Bean
    @ConditionalOnMissingBean
    InfoContributor hrmsInfoContributor() {
        return new HrmsInfoContributor();
    }
}
