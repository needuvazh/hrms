package com.company.hrms.wps.repository;

import com.company.hrms.wps.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WpsRepository {

    Mono<WpsBatchDto> saveBatch(WpsBatchDto batch);

    Mono<WpsBatchDto> updateBatch(WpsBatchDto batch);

    Mono<WpsBatchDto> findBatchById(String tenantId, UUID batchId);

    Mono<WpsEmployeeEntryDto> saveEntry(WpsEmployeeEntryDto entry);

    Flux<WpsEmployeeEntryDto> findEntriesByBatchId(String tenantId, UUID batchId);

    Mono<WpsExportFileDto> saveExportFile(WpsExportFileDto exportFile);

    Flux<WpsExportFileDto> findExportFilesByBatchId(String tenantId, UUID batchId);
}
