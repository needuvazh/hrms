package com.company.hrms.document.repository;

import com.company.hrms.document.model.*;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {

    Mono<DocumentRecordDto> save(DocumentRecordDto record);

    Mono<DocumentRecordDto> update(DocumentRecordDto record);

    Mono<DocumentRecordDto> findById(UUID documentId, String tenantId);

    Flux<DocumentRecordDto> findByEntity(String tenantId, String entityType, String entityId, boolean includeArchived);

    Flux<DocumentRecordDto> findExpiringWithin(String tenantId, LocalDate fromDate, LocalDate toDate, boolean includeArchived);
}
