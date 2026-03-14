package com.company.hrms.document.domain;

public record StorageRegistrationRequest(
        String tenantId,
        String fileName,
        String contentType,
        long sizeBytes,
        String objectKey,
        String checksum
) {
}
