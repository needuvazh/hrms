package com.company.hrms.document.service.impl;

import com.company.hrms.document.model.*;
import com.company.hrms.document.repository.*;
import com.company.hrms.document.service.*;

import com.company.hrms.contracts.document.AttachDocumentCommandDto;
import com.company.hrms.document.model.DocumentExpiryQueryDto;
import com.company.hrms.document.model.DocumentListQueryDto;
import com.company.hrms.document.service.DocumentModuleApi;
import com.company.hrms.document.model.DocumentRecordViewDto;
import com.company.hrms.document.model.DocumentRecordDto;
import com.company.hrms.document.repository.DocumentRepository;
import com.company.hrms.document.model.DocumentStorageAdapter;
import com.company.hrms.document.model.StorageRegistrationRequestDto;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentApplicationService implements DocumentModuleApi {

    private final DocumentRepository documentRepository;
    private final DocumentStorageAdapter documentStorageAdapter;
    private final TenantContextAccessor tenantContextAccessor;

    public DocumentApplicationService(
            DocumentRepository documentRepository,
            DocumentStorageAdapter documentStorageAdapter,
            TenantContextAccessor tenantContextAccessor
    ) {
        this.documentRepository = documentRepository;
        this.documentStorageAdapter = documentStorageAdapter;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Mono<DocumentRecordViewDto> attachDocument(AttachDocumentCommandDto command) {
        validateCommand(command);

        return requireTenant().flatMap(tenantId -> documentStorageAdapter
                .registerUploadMetadata(new StorageRegistrationRequestDto(
                        tenantId,
                        command.fileName(),
                        command.contentType(),
                        command.sizeBytes(),
                        command.objectKey(),
                        command.checksum()))
                .flatMap(storageReference -> {
                    Instant now = Instant.now();
                    com.company.hrms.contracts.document.VerificationStatus status = command.initialVerificationStatus() == null
                            ? com.company.hrms.contracts.document.VerificationStatus.PENDING
                            : command.initialVerificationStatus();

                    DocumentRecordDto record = new DocumentRecordDto(
                            UUID.randomUUID(),
                            tenantId,
                            toDomainDocumentType(command.documentType()),
                            command.entityType().trim().toUpperCase(),
                            command.entityId().trim(),
                            command.fileName().trim(),
                            storageReference,
                            command.expiryDate() == null ? com.company.hrms.document.model.ExpiryDateDto.empty() : toDomainExpiryDate(command.expiryDate()),
                            toDomainVerificationStatus(status),
                            false,
                            now,
                            defaultActor(command.createdBy()),
                            now,
                            defaultActor(command.createdBy()));

                    return documentRepository.save(record).map(this::toView);
                }));
    }

    @Override
    public Flux<DocumentRecordViewDto> listDocuments(DocumentListQueryDto query) {
        validateListQuery(query);
        return requireTenant().flatMapMany(tenantId -> documentRepository
                .findByEntity(tenantId, query.entityType().trim().toUpperCase(), query.entityId().trim(), query.includeArchived())
                .map(this::toView));
    }

    @Override
    public Flux<DocumentRecordViewDto> findExpiringDocuments(DocumentExpiryQueryDto query) {
        LocalDate fromDate = query.fromDate() == null ? LocalDate.now() : query.fromDate();
        LocalDate toDate = query.toDate() == null ? fromDate.plusDays(30) : query.toDate();
        if (toDate.isBefore(fromDate)) {
            return Flux.error(new HrmsException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EXPIRY_RANGE",
                    "toDate must be greater than or equal to fromDate"));
        }

        return requireTenant().flatMapMany(tenantId -> documentRepository
                .findExpiringWithin(tenantId, fromDate, toDate, query.includeArchived())
                .map(this::toView));
    }

    @Override
    public Mono<DocumentRecordViewDto> archiveDocument(UUID documentId) {
        return requireTenant().flatMap(tenantId -> documentRepository
                .findById(documentId, tenantId)
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.NOT_FOUND,
                        "DOCUMENT_NOT_FOUND",
                        "Document not found")))
                .flatMap(existing -> documentStorageAdapter.archive(existing.storageReference())
                        .then(documentRepository.update(existing.archive("system", Instant.now()))))
                .map(this::toView));
    }

    @Override
    public Mono<DocumentRecordViewDto> getDocument(UUID documentId) {
        return requireTenant().flatMap(tenantId -> documentRepository
                .findById(documentId, tenantId)
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.NOT_FOUND,
                        "DOCUMENT_NOT_FOUND",
                        "Document not found")))
                .flatMap(record -> documentStorageAdapter.lookup(record.storageReference())
                        .defaultIfEmpty(record.storageReference())
                        .map(storageReference -> withStorageReference(record, storageReference)))
                .map(this::toView));
    }

    private void validateCommand(AttachDocumentCommandDto command) {
        if (command.documentType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_TYPE_REQUIRED", "Document type is required");
        }
        if (!StringUtils.hasText(command.entityType())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENTITY_TYPE_REQUIRED", "Entity type is required");
        }
        if (!StringUtils.hasText(command.entityId())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENTITY_ID_REQUIRED", "Entity id is required");
        }
        if (!StringUtils.hasText(command.fileName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FILE_NAME_REQUIRED", "File name is required");
        }
        if (!StringUtils.hasText(command.contentType())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CONTENT_TYPE_REQUIRED", "Content type is required");
        }
        if (!StringUtils.hasText(command.objectKey())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "OBJECT_KEY_REQUIRED", "Object key is required");
        }
        if (command.sizeBytes() <= 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FILE_SIZE_REQUIRED", "File size must be greater than zero");
        }
    }

    private void validateListQuery(DocumentListQueryDto query) {
        if (!StringUtils.hasText(query.entityType())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENTITY_TYPE_REQUIRED", "Entity type is required");
        }
        if (!StringUtils.hasText(query.entityId())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENTITY_ID_REQUIRED", "Entity id is required");
        }
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "TENANT_REQUIRED",
                        "Tenant is required")));
    }

    private String defaultActor(String actor) {
        return StringUtils.hasText(actor) ? actor : "system";
    }

    private DocumentRecordDto withStorageReference(DocumentRecordDto record, com.company.hrms.document.model.StorageReferenceDto storageReference) {
        return new DocumentRecordDto(
                record.id(),
                record.tenantId(),
                record.documentType(),
                record.entityType(),
                record.entityId(),
                record.fileName(),
                storageReference,
                record.expiryDate(),
                record.verificationStatus(),
                record.archived(),
                record.createdAt(),
                record.createdBy(),
                record.updatedAt(),
                record.updatedBy());
    }

    private DocumentRecordViewDto toView(DocumentRecordDto record) {
        return new DocumentRecordViewDto(
                record.id(),
                record.tenantId(),
                toApiDocumentType(record.documentType()),
                record.entityType(),
                record.entityId(),
                record.fileName(),
                record.storageReference(),
                toApiExpiryDate(record.expiryDate()),
                toApiVerificationStatus(record.verificationStatus()),
                record.archived(),
                record.createdAt(),
                record.createdBy(),
                record.updatedAt(),
                record.updatedBy());
    }

    private com.company.hrms.document.model.DocumentType toDomainDocumentType(com.company.hrms.contracts.document.DocumentType type) {
        return com.company.hrms.document.model.DocumentType.valueOf(type.name());
    }

    private com.company.hrms.document.model.ExpiryDateDto toDomainExpiryDate(com.company.hrms.contracts.document.ExpiryDateDto expiryDate) {
        return new com.company.hrms.document.model.ExpiryDateDto(expiryDate.value());
    }

    private com.company.hrms.document.model.VerificationStatus toDomainVerificationStatus(com.company.hrms.contracts.document.VerificationStatus status) {
        return com.company.hrms.document.model.VerificationStatus.valueOf(status.name());
    }

    private com.company.hrms.document.model.DocumentType toApiDocumentType(com.company.hrms.document.model.DocumentType type) {
        return com.company.hrms.document.model.DocumentType.valueOf(type.name());
    }

    private com.company.hrms.document.model.ExpiryDateDto toApiExpiryDate(com.company.hrms.document.model.ExpiryDateDto expiryDate) {
        return new com.company.hrms.document.model.ExpiryDateDto(expiryDate.value());
    }

    private com.company.hrms.document.model.VerificationStatus toApiVerificationStatus(com.company.hrms.document.model.VerificationStatus status) {
        return com.company.hrms.document.model.VerificationStatus.valueOf(status.name());
    }
}
