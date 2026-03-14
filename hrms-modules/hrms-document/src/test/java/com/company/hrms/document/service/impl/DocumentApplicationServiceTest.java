package com.company.hrms.document.service.impl;

import com.company.hrms.document.model.*;
import com.company.hrms.document.repository.*;
import com.company.hrms.document.service.*;

import com.company.hrms.document.model.AttachDocumentCommandDto;
import com.company.hrms.document.model.DocumentExpiryQueryDto;
import com.company.hrms.document.model.DocumentListQueryDto;
import com.company.hrms.document.model.DocumentRecordDto;
import com.company.hrms.document.repository.DocumentRepository;
import com.company.hrms.document.model.DocumentStorageAdapter;
import com.company.hrms.document.model.DocumentType;
import com.company.hrms.document.model.ExpiryDateDto;
import com.company.hrms.document.model.StorageReferenceDto;
import com.company.hrms.document.model.StorageRegistrationRequestDto;
import com.company.hrms.document.model.VerificationStatus;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DocumentApplicationServiceTest {

    private final InMemoryDocumentRepository documentRepository = new InMemoryDocumentRepository();
    private final InMemoryStorageAdapter storageAdapter = new InMemoryStorageAdapter();
    private final DocumentApplicationService documentApplicationService = new DocumentApplicationService(
            documentRepository,
            storageAdapter,
            new DefaultTenantContextAccessor());

    @Test
    void registerAndListDocumentsByEntity() {
        AttachDocumentCommandDto command = new AttachDocumentCommandDto(
                DocumentType.EMPLOYMENT_CONTRACT,
                "employee",
                "EMP-101",
                "contract.pdf",
                "application/pdf",
                1024,
                "employee/EMP-101/contract.pdf",
                "checksum-1",
                ExpiryDateDto.of(LocalDate.now().plusYears(2)),
                VerificationStatus.PENDING,
                "hr.admin");

        StepVerifier.create(documentApplicationService.attachDocument(command)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view ->
                        "tenant-a".equals(view.tenantId())
                                && "EMPLOYEE".equals(view.entityType())
                                && "EMP-101".equals(view.entityId())
                                && DocumentType.EMPLOYMENT_CONTRACT == view.documentType())
                .verifyComplete();

        StepVerifier.create(documentApplicationService.listDocuments(new DocumentListQueryDto("employee", "EMP-101", false))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void expiryQueryReturnsOnlyTenantDocumentsInRange() {
        seed("tenant-a", "EMPLOYEE", "EMP-1", LocalDate.now().plusDays(3), false);
        seed("tenant-a", "EMPLOYEE", "EMP-2", LocalDate.now().plusDays(40), false);
        seed("tenant-b", "EMPLOYEE", "EMP-9", LocalDate.now().plusDays(2), false);

        StepVerifier.create(documentApplicationService.findExpiringDocuments(new DocumentExpiryQueryDto(
                                LocalDate.now(),
                                LocalDate.now().plusDays(15),
                                false))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view -> "tenant-a".equals(view.tenantId()) && "EMP-1".equals(view.entityId()))
                .verifyComplete();
    }

    @Test
    void attachFailsWithoutTenantContext() {
        AttachDocumentCommandDto command = new AttachDocumentCommandDto(
                DocumentType.OTHER,
                "tenant",
                "default",
                "misc.txt",
                "text/plain",
                100,
                "tenant/default/misc.txt",
                null,
                ExpiryDateDto.empty(),
                VerificationStatus.PENDING,
                "system");

        StepVerifier.create(documentApplicationService.attachDocument(command))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException hrmsException = (HrmsException) error;
                    assertEquals("TENANT_REQUIRED", hrmsException.getErrorCode());
                })
                .verify();
    }

    private void seed(String tenantId, String entityType, String entityId, LocalDate expiryDate, boolean archived) {
        Instant now = Instant.now();
        DocumentRecordDto record = new DocumentRecordDto(
                UUID.randomUUID(),
                tenantId,
                com.company.hrms.document.model.DocumentType.PASSPORT,
                entityType,
                entityId,
                "passport.pdf",
                new StorageReferenceDto("LOCAL_DEV", tenantId, entityId + "/passport.pdf", null, "application/pdf", 1024),
                new com.company.hrms.document.model.ExpiryDateDto(expiryDate),
                com.company.hrms.document.model.VerificationStatus.VERIFIED,
                archived,
                now,
                "seed",
                now,
                "seed");
        documentRepository.seed(record);
    }

    static class InMemoryDocumentRepository implements DocumentRepository {

        private final Map<UUID, DocumentRecordDto> storage = new ConcurrentHashMap<>();

        @Override
        public Mono<DocumentRecordDto> save(DocumentRecordDto record) {
            storage.put(record.id(), record);
            return Mono.just(record);
        }

        @Override
        public Mono<DocumentRecordDto> update(DocumentRecordDto record) {
            storage.put(record.id(), record);
            return Mono.just(record);
        }

        @Override
        public Mono<DocumentRecordDto> findById(UUID documentId, String tenantId) {
            DocumentRecordDto record = storage.get(documentId);
            if (record == null || !tenantId.equals(record.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(record);
        }

        @Override
        public Flux<DocumentRecordDto> findByEntity(String tenantId, String entityType, String entityId, boolean includeArchived) {
            return Flux.fromIterable(storage.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> entityType.equals(record.entityType()))
                    .filter(record -> entityId.equals(record.entityId()))
                    .filter(record -> includeArchived || !record.archived());
        }

        @Override
        public Flux<DocumentRecordDto> findExpiringWithin(String tenantId, LocalDate fromDate, LocalDate toDate, boolean includeArchived) {
            return Flux.fromIterable(storage.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> includeArchived || !record.archived())
                    .filter(record -> record.expiryDate() != null && record.expiryDate().value() != null)
                    .filter(record -> {
                        LocalDate expiry = record.expiryDate().value();
                        return (expiry.isAfter(fromDate) || expiry.isEqual(fromDate))
                                && (expiry.isBefore(toDate) || expiry.isEqual(toDate));
                    })
                    .sort((a, b) -> a.expiryDate().value().compareTo(b.expiryDate().value()));
        }

        void seed(DocumentRecordDto record) {
            storage.put(record.id(), record);
        }
    }

    static class InMemoryStorageAdapter implements DocumentStorageAdapter {

        private final Map<String, StorageReferenceDto> storage = new ConcurrentHashMap<>();

        @Override
        public Mono<StorageReferenceDto> registerUploadMetadata(StorageRegistrationRequestDto request) {
            StorageReferenceDto reference = new StorageReferenceDto(
                    "LOCAL_DEV",
                    request.tenantId(),
                    request.objectKey(),
                    request.checksum(),
                    request.contentType(),
                    request.sizeBytes());
            storage.put(key(reference), reference);
            return Mono.just(reference);
        }

        @Override
        public Mono<StorageReferenceDto> lookup(StorageReferenceDto reference) {
            return Mono.justOrEmpty(storage.get(key(reference)));
        }

        @Override
        public Mono<Void> archive(StorageReferenceDto reference) {
            storage.remove(key(reference));
            return Mono.empty();
        }

        private String key(StorageReferenceDto reference) {
            return reference.provider() + ":" + reference.bucket() + ":" + reference.objectKey();
        }
    }
}
