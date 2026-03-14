package com.company.hrms.document.application;

import com.company.hrms.document.api.AttachDocumentCommand;
import com.company.hrms.document.api.DocumentExpiryQuery;
import com.company.hrms.document.api.DocumentListQuery;
import com.company.hrms.document.domain.DocumentRecord;
import com.company.hrms.document.domain.DocumentRepository;
import com.company.hrms.document.domain.DocumentStorageAdapter;
import com.company.hrms.document.api.DocumentType;
import com.company.hrms.document.api.ExpiryDate;
import com.company.hrms.document.domain.StorageReference;
import com.company.hrms.document.domain.StorageRegistrationRequest;
import com.company.hrms.document.api.VerificationStatus;
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
        AttachDocumentCommand command = new AttachDocumentCommand(
                DocumentType.EMPLOYMENT_CONTRACT,
                "employee",
                "EMP-101",
                "contract.pdf",
                "application/pdf",
                1024,
                "employee/EMP-101/contract.pdf",
                "checksum-1",
                ExpiryDate.of(LocalDate.now().plusYears(2)),
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

        StepVerifier.create(documentApplicationService.listDocuments(new DocumentListQuery("employee", "EMP-101", false))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void expiryQueryReturnsOnlyTenantDocumentsInRange() {
        seed("tenant-a", "EMPLOYEE", "EMP-1", LocalDate.now().plusDays(3), false);
        seed("tenant-a", "EMPLOYEE", "EMP-2", LocalDate.now().plusDays(40), false);
        seed("tenant-b", "EMPLOYEE", "EMP-9", LocalDate.now().plusDays(2), false);

        StepVerifier.create(documentApplicationService.findExpiringDocuments(new DocumentExpiryQuery(
                                LocalDate.now(),
                                LocalDate.now().plusDays(15),
                                false))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view -> "tenant-a".equals(view.tenantId()) && "EMP-1".equals(view.entityId()))
                .verifyComplete();
    }

    @Test
    void attachFailsWithoutTenantContext() {
        AttachDocumentCommand command = new AttachDocumentCommand(
                DocumentType.OTHER,
                "tenant",
                "default",
                "misc.txt",
                "text/plain",
                100,
                "tenant/default/misc.txt",
                null,
                ExpiryDate.empty(),
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
        DocumentRecord record = new DocumentRecord(
                UUID.randomUUID(),
                tenantId,
                com.company.hrms.document.domain.DocumentType.PASSPORT,
                entityType,
                entityId,
                "passport.pdf",
                new StorageReference("LOCAL_DEV", tenantId, entityId + "/passport.pdf", null, "application/pdf", 1024),
                new com.company.hrms.document.domain.ExpiryDate(expiryDate),
                com.company.hrms.document.domain.VerificationStatus.VERIFIED,
                archived,
                now,
                "seed",
                now,
                "seed");
        documentRepository.seed(record);
    }

    static class InMemoryDocumentRepository implements DocumentRepository {

        private final Map<UUID, DocumentRecord> storage = new ConcurrentHashMap<>();

        @Override
        public Mono<DocumentRecord> save(DocumentRecord record) {
            storage.put(record.id(), record);
            return Mono.just(record);
        }

        @Override
        public Mono<DocumentRecord> update(DocumentRecord record) {
            storage.put(record.id(), record);
            return Mono.just(record);
        }

        @Override
        public Mono<DocumentRecord> findById(UUID documentId, String tenantId) {
            DocumentRecord record = storage.get(documentId);
            if (record == null || !tenantId.equals(record.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(record);
        }

        @Override
        public Flux<DocumentRecord> findByEntity(String tenantId, String entityType, String entityId, boolean includeArchived) {
            return Flux.fromIterable(storage.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> entityType.equals(record.entityType()))
                    .filter(record -> entityId.equals(record.entityId()))
                    .filter(record -> includeArchived || !record.archived());
        }

        @Override
        public Flux<DocumentRecord> findExpiringWithin(String tenantId, LocalDate fromDate, LocalDate toDate, boolean includeArchived) {
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

        void seed(DocumentRecord record) {
            storage.put(record.id(), record);
        }
    }

    static class InMemoryStorageAdapter implements DocumentStorageAdapter {

        private final Map<String, StorageReference> storage = new ConcurrentHashMap<>();

        @Override
        public Mono<StorageReference> registerUploadMetadata(StorageRegistrationRequest request) {
            StorageReference reference = new StorageReference(
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
        public Mono<StorageReference> lookup(StorageReference reference) {
            return Mono.justOrEmpty(storage.get(key(reference)));
        }

        @Override
        public Mono<Void> archive(StorageReference reference) {
            storage.remove(key(reference));
            return Mono.empty();
        }

        private String key(StorageReference reference) {
            return reference.provider() + ":" + reference.bucket() + ":" + reference.objectKey();
        }
    }
}
