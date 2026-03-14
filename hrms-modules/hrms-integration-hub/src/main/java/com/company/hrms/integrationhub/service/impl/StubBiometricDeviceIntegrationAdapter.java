package com.company.hrms.integrationhub.service.impl;

import com.company.hrms.integrationhub.model.*;
import com.company.hrms.integrationhub.repository.*;
import com.company.hrms.integrationhub.service.*;

import com.company.hrms.integrationhub.model.IntegrationAdapter;
import com.company.hrms.integrationhub.model.IntegrationAdapterResultDto;
import com.company.hrms.integrationhub.model.IntegrationInvocationDto;
import com.company.hrms.integrationhub.model.IntegrationProviderType;
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
    public Mono<IntegrationAdapterResultDto> execute(IntegrationInvocationDto invocation) {
        if ("PULL_PUNCH_EVENTS".equalsIgnoreCase(invocation.operation())) {
            return Mono.just(IntegrationAdapterResultDto.success("biometric-stub-" + Instant.now().toEpochMilli()));
        }
        return Mono.just(IntegrationAdapterResultDto.failure("Unsupported biometric operation: " + invocation.operation()));
    }
}
