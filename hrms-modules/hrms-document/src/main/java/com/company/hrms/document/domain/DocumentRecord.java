package com.company.hrms.document.domain;

import java.time.Instant;
import java.util.UUID;

public record DocumentRecord(
        UUID id,
        String tenantId,
        DocumentType documentType,
        String entityType,
        String entityId,
        String fileName,
        StorageReference storageReference,
        ExpiryDate expiryDate,
        VerificationStatus verificationStatus,
        boolean archived,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {

    public DocumentRecord archive(String archivedBy, Instant archivedAt) {
        return new DocumentRecord(
                id,
                tenantId,
                documentType,
                entityType,
                entityId,
                fileName,
                storageReference,
                expiryDate,
                verificationStatus,
                true,
                createdAt,
                createdBy,
                archivedAt,
                archivedBy);
    }
}
