package com.company.hrms.compliance.repository;

import com.company.hrms.compliance.model.ComplianceModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComplianceRepository {

    Mono<ComplianceModels.MasterViewDto> create(
            String tenantId,
            ComplianceModels.Resource resource,
            ComplianceModels.MasterUpsertRequest request,
            String actor
    );

    Mono<ComplianceModels.MasterViewDto> update(
            String tenantId,
            ComplianceModels.Resource resource,
            UUID id,
            ComplianceModels.MasterUpsertRequest request,
            String actor
    );

    Mono<ComplianceModels.MasterViewDto> get(String tenantId, ComplianceModels.Resource resource, UUID id);

    Flux<ComplianceModels.MasterViewDto> list(String tenantId, ComplianceModels.Resource resource, ComplianceModels.SearchQuery query);

    Mono<ComplianceModels.MasterViewDto> updateStatus(
            String tenantId,
            ComplianceModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    );

    Flux<ComplianceModels.OptionViewDto> options(String tenantId, ComplianceModels.Resource resource, String q, int limit);

    Mono<Boolean> codeExists(String tenantId, ComplianceModels.Resource resource, String code, UUID excludeId);
}
