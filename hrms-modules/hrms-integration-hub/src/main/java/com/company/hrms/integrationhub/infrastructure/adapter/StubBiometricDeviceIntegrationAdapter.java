package com.company.hrms.integrationhub.infrastructure.adapter;

import com.company.hrms.integrationhub.domain.IntegrationAdapter;
import com.company.hrms.integrationhub.domain.IntegrationAdapterResult;
import com.company.hrms.integrationhub.domain.IntegrationInvocation;
import com.company.hrms.integrationhub.domain.IntegrationProviderType;
import java.time.Instant;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StubBiometricDeviceIntegrationAdapter implements IntegrationAdapter {

    @Override
    public IntegrationProviderType providerType() {
        return IntegrationProviderType.BIOMETRIC_DEVICE;
    }

    @Override
    public Mono<IntegrationAdapterResult> execute(IntegrationInvocation invocation) {
        if ("PULL_PUNCH_EVENTS".equalsIgnoreCase(invocation.operation())) {
            return Mono.just(IntegrationAdapterResult.success("biometric-stub-" + Instant.now().toEpochMilli()));
        }
        return Mono.just(IntegrationAdapterResult.failure("Unsupported biometric operation: " + invocation.operation()));
    }
}
