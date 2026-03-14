package com.company.hrms.wps.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WpsModuleApi {

    Mono<WpsBatchView> createBatchFromPayrollRun(CreateWpsBatchCommand command);

    Mono<WpsExportFileView> generateExport(GenerateWpsExportCommand command);

    Mono<WpsBatchView> markExported(MarkWpsExportedCommand command);

    Mono<WpsBatchView> getBatch(UUID batchId);

    Flux<WpsEmployeeEntryView> entries(UUID batchId);
}
