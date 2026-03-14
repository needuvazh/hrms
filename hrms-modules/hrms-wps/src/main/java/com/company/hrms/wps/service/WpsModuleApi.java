package com.company.hrms.wps.service;

import com.company.hrms.wps.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WpsModuleApi {

    Mono<WpsBatchViewDto> createBatchFromPayrollRun(CreateWpsBatchCommandDto command);

    Mono<WpsExportFileViewDto> generateExport(GenerateWpsExportCommandDto command);

    Mono<WpsBatchViewDto> markExported(MarkWpsExportedCommandDto command);

    Mono<WpsBatchViewDto> getBatch(UUID batchId);

    Flux<WpsEmployeeEntryViewDto> entries(UUID batchId);
}
