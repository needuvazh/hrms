package com.company.hrms.integrationhub.domain;

public record IntegrationAdapterResult(
        IntegrationStatus status,
        String externalReference,
        String errorMessage
) {

    public static IntegrationAdapterResult success(String externalReference) {
        return new IntegrationAdapterResult(IntegrationStatus.SUCCESS, externalReference, null);
    }

    public static IntegrationAdapterResult failure(String errorMessage) {
        return new IntegrationAdapterResult(IntegrationStatus.FAILED, null, errorMessage);
    }
}
