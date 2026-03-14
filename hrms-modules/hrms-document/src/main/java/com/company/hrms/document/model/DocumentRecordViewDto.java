package com.company.hrms.document.model;

import com.company.hrms.document.model.StorageReferenceDto;
import java.time.Instant;
import java.util.UUID;
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
public class DocumentRecordViewDto {
    private final UUID id;
    private final String tenantId;
    private final DocumentType documentType;
    private final String entityType;
    private final String entityId;
    private final String fileName;
    private final StorageReferenceDto storageReference;
    private final ExpiryDateDto expiryDate;
    private final VerificationStatus verificationStatus;
    private final boolean archived;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
}
