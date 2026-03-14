package com.company.hrms.document.model;

import java.time.Instant;
import java.util.UUID;

public record DocumentRecordDto(
        UUID id,
        String tenantId,
        DocumentType documentType,
        String entityType,
        String entityId,
        String fileName,
        StorageReferenceDto storageReference,
        ExpiryDateDto expiryDate,
        VerificationStatus verificationStatus,
        boolean archived,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {

    public DocumentRecordDto archive(String archivedBy, Instant archivedAt) {
        return new DocumentRecordDto(
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
