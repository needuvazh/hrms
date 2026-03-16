package com.company.hrms.documentpolicy.repository;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentPolicyRepository {

    Mono<DocumentPolicyModels.MasterViewDto> create(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            DocumentPolicyModels.MasterUpsertRequest request,
            String actor
    );

    Mono<DocumentPolicyModels.MasterViewDto> update(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            UUID id,
            DocumentPolicyModels.MasterUpsertRequest request,
            String actor
    );

    Mono<DocumentPolicyModels.MasterViewDto> get(String tenantId, DocumentPolicyModels.Resource resource, UUID id);

    Flux<DocumentPolicyModels.MasterViewDto> list(String tenantId, DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query);

    Mono<DocumentPolicyModels.MasterViewDto> updateStatus(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    );

    Flux<DocumentPolicyModels.OptionViewDto> options(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            String q,
            int limit,
            boolean activeOnly
    );

    Mono<Boolean> codeExists(String tenantId, DocumentPolicyModels.Resource resource, String code, UUID excludeId);

    Mono<Boolean> existsById(String tenantId, String tableName, UUID id);
}
