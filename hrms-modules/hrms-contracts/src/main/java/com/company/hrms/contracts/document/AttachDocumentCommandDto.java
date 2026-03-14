package com.company.hrms.contracts.document;

public record AttachDocumentCommandDto(
        DocumentType documentType,
        String entityType,
        String entityId,
        String fileName,
        String contentType,
        long sizeBytes,
        String objectKey,
        String checksum,
        ExpiryDateDto expiryDate,
        VerificationStatus initialVerificationStatus,
        String createdBy
) {
}
