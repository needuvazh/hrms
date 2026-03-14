package com.company.hrms.document.api;

import com.company.hrms.document.domain.StorageReference;
import java.time.Instant;
import java.util.UUID;

public record DocumentRecordView(
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
}
