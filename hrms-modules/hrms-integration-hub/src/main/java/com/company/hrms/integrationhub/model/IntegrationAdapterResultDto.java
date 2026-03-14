package com.company.hrms.integrationhub.model;

public record IntegrationAdapterResultDto(
        IntegrationStatus status,
        String externalReference,
        String errorMessage
) {

    public static IntegrationAdapterResultDto success(String externalReference) {
        return new IntegrationAdapterResultDto(IntegrationStatus.SUCCESS, externalReference, null);
    }

    public static IntegrationAdapterResultDto failure(String errorMessage) {
        return new IntegrationAdapterResultDto(IntegrationStatus.FAILED, null, errorMessage);
    }
}
