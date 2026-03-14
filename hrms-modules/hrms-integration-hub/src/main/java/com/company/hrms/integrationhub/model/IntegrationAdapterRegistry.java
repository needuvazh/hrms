package com.company.hrms.integrationhub.model;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IntegrationAdapterRegistry {

    private final List<IntegrationAdapter> adapters;

    public IntegrationAdapterRegistry(List<IntegrationAdapter> adapters) {
        this.adapters = adapters;
    }

    public IntegrationAdapter adapterFor(IntegrationProviderType providerType) {
        return adapters.stream()
                .filter(adapter -> adapter.providerType() == providerType)
                .findFirst()
                .orElseThrow(() -> new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "INTEGRATION_ADAPTER_NOT_CONFIGURED",
                        "No integration adapter configured for provider type: " + providerType));
    }
}
