package com.company.hrms.document.domain;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {

    Mono<DocumentRecord> save(DocumentRecord record);

    Mono<DocumentRecord> update(DocumentRecord record);

    Mono<DocumentRecord> findById(UUID documentId, String tenantId);

    Flux<DocumentRecord> findByEntity(String tenantId, String entityType, String entityId, boolean includeArchived);

    Flux<DocumentRecord> findExpiringWithin(String tenantId, LocalDate fromDate, LocalDate toDate, boolean includeArchived);
}
