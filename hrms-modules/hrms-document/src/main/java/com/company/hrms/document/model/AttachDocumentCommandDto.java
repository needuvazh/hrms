package com.company.hrms.document.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AttachDocumentCommandDto {
    private final DocumentType documentType;
    private final String entityType;
    private final String entityId;
    private final String fileName;
    private final String contentType;
    private final long sizeBytes;
    private final String objectKey;
    private final String checksum;
    private final ExpiryDateDto expiryDate;
    private final VerificationStatus initialVerificationStatus;
    private final String createdBy;
}
