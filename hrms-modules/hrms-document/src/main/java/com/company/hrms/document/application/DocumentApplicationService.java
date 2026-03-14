package com.company.hrms.document.application;

import com.company.hrms.document.api.AttachDocumentCommand;
import com.company.hrms.document.api.DocumentExpiryQuery;
import com.company.hrms.document.api.DocumentListQuery;
import com.company.hrms.document.api.DocumentModuleApi;
import com.company.hrms.document.api.DocumentRecordView;
import com.company.hrms.document.api.ExpiryDate;
import com.company.hrms.document.api.VerificationStatus;
import com.company.hrms.document.domain.DocumentRecord;
import com.company.hrms.document.domain.DocumentRepository;
import com.company.hrms.document.domain.DocumentStorageAdapter;
import com.company.hrms.document.domain.StorageRegistrationRequest;
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
    public Mono<DocumentRecordView> attachDocument(AttachDocumentCommand command) {
        validateCommand(command);

        return requireTenant().flatMap(tenantId -> documentStorageAdapter
                .registerUploadMetadata(new StorageRegistrationRequest(
                        tenantId,
                        command.fileName(),
                        command.contentType(),
                        command.sizeBytes(),
                        command.objectKey(),
                        command.checksum()))
                .flatMap(storageReference -> {
                    Instant now = Instant.now();
                    VerificationStatus status = command.initialVerificationStatus() == null
                            ? VerificationStatus.PENDING
                            : command.initialVerificationStatus();

                    DocumentRecord record = new DocumentRecord(
                            UUID.randomUUID(),
                            tenantId,
                            toDomainDocumentType(command.documentType()),
                            command.entityType().trim().toUpperCase(),
                            command.entityId().trim(),
                            command.fileName().trim(),
                            storageReference,
                            command.expiryDate() == null ? com.company.hrms.document.domain.ExpiryDate.empty() : toDomainExpiryDate(command.expiryDate()),
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
    public Flux<DocumentRecordView> listDocuments(DocumentListQuery query) {
        validateListQuery(query);
        return requireTenant().flatMapMany(tenantId -> documentRepository
                .findByEntity(tenantId, query.entityType().trim().toUpperCase(), query.entityId().trim(), query.includeArchived())
                .map(this::toView));
    }

    @Override
    public Flux<DocumentRecordView> findExpiringDocuments(DocumentExpiryQuery query) {
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
    public Mono<DocumentRecordView> archiveDocument(UUID documentId) {
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
    public Mono<DocumentRecordView> getDocument(UUID documentId) {
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

    private void validateCommand(AttachDocumentCommand command) {
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

    private void validateListQuery(DocumentListQuery query) {
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

    private DocumentRecord withStorageReference(DocumentRecord record, com.company.hrms.document.domain.StorageReference storageReference) {
        return new DocumentRecord(
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

    private DocumentRecordView toView(DocumentRecord record) {
        return new DocumentRecordView(
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

    private com.company.hrms.document.domain.DocumentType toDomainDocumentType(com.company.hrms.document.api.DocumentType type) {
        return com.company.hrms.document.domain.DocumentType.valueOf(type.name());
    }

    private com.company.hrms.document.domain.ExpiryDate toDomainExpiryDate(ExpiryDate expiryDate) {
        return new com.company.hrms.document.domain.ExpiryDate(expiryDate.value());
    }

    private com.company.hrms.document.domain.VerificationStatus toDomainVerificationStatus(VerificationStatus status) {
        return com.company.hrms.document.domain.VerificationStatus.valueOf(status.name());
    }

    private com.company.hrms.document.api.DocumentType toApiDocumentType(com.company.hrms.document.domain.DocumentType type) {
        return com.company.hrms.document.api.DocumentType.valueOf(type.name());
    }

    private ExpiryDate toApiExpiryDate(com.company.hrms.document.domain.ExpiryDate expiryDate) {
        return new ExpiryDate(expiryDate.value());
    }

    private VerificationStatus toApiVerificationStatus(com.company.hrms.document.domain.VerificationStatus status) {
        return VerificationStatus.valueOf(status.name());
    }
}
