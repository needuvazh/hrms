package com.company.hrms.wps.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WpsRepository {

    Mono<WpsBatch> saveBatch(WpsBatch batch);

    Mono<WpsBatch> updateBatch(WpsBatch batch);

    Mono<WpsBatch> findBatchById(String tenantId, UUID batchId);

    Mono<WpsEmployeeEntry> saveEntry(WpsEmployeeEntry entry);

    Flux<WpsEmployeeEntry> findEntriesByBatchId(String tenantId, UUID batchId);

    Mono<WpsExportFile> saveExportFile(WpsExportFile exportFile);

    Flux<WpsExportFile> findExportFilesByBatchId(String tenantId, UUID batchId);
}
