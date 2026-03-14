package com.company.hrms.document.api;

public record AttachDocumentCommand(
        DocumentType documentType,
        String entityType,
        String entityId,
        String fileName,
        String contentType,
        long sizeBytes,
        String objectKey,
        String checksum,
        ExpiryDate expiryDate,
        VerificationStatus initialVerificationStatus,
        String createdBy
) {
}
