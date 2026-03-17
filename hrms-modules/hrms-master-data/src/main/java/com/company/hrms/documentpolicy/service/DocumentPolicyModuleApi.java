package com.company.hrms.documentpolicy.service;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import com.company.hrms.masterdata.reference.api.PagedResult;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentPolicyModuleApi {

    Mono<DocumentPolicyModels.MasterViewDto> create(DocumentPolicyModels.Resource resource, DocumentPolicyModels.MasterUpsertRequest request);

    Mono<DocumentPolicyModels.MasterViewDto> update(DocumentPolicyModels.Resource resource, UUID id, DocumentPolicyModels.MasterUpsertRequest request);

    Mono<DocumentPolicyModels.MasterViewDto> get(DocumentPolicyModels.Resource resource, UUID id);

    Mono<PagedResult<DocumentPolicyModels.MasterViewDto>> list(DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query);

    Mono<DocumentPolicyModels.MasterViewDto> updateStatus(DocumentPolicyModels.Resource resource, UUID id, DocumentPolicyModels.StatusUpdateCommand command);

    Flux<DocumentPolicyModels.OptionViewDto> options(DocumentPolicyModels.Resource resource, String q, int limit, boolean activeOnly);
}
