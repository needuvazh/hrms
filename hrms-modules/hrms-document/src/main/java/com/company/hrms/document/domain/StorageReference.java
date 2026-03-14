package com.company.hrms.document.domain;

public record StorageReference(
        String provider,
        String bucket,
        String objectKey,
        String checksum,
        String contentType,
        long sizeBytes
) {
}
