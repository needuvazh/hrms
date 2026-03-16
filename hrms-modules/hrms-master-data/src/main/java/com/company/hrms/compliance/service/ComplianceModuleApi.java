package com.company.hrms.compliance.service;

import com.company.hrms.compliance.model.ComplianceModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComplianceModuleApi {

    Mono<ComplianceModels.MasterViewDto> create(ComplianceModels.Resource resource, ComplianceModels.MasterUpsertRequest request);

    Mono<ComplianceModels.MasterViewDto> update(ComplianceModels.Resource resource, UUID id, ComplianceModels.MasterUpsertRequest request);

    Mono<ComplianceModels.MasterViewDto> get(ComplianceModels.Resource resource, UUID id);

    Flux<ComplianceModels.MasterViewDto> list(ComplianceModels.Resource resource, ComplianceModels.SearchQuery query);

    Mono<ComplianceModels.MasterViewDto> updateStatus(ComplianceModels.Resource resource, UUID id, ComplianceModels.StatusUpdateCommand command);

    Flux<ComplianceModels.OptionViewDto> options(ComplianceModels.Resource resource, String q, int limit);
}
