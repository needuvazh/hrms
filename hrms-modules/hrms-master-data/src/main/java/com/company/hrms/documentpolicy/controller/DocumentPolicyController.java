package com.company.hrms.documentpolicy.controller;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import com.company.hrms.documentpolicy.service.DocumentPolicyModuleApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/document-policy")
public class DocumentPolicyController {

    private final DocumentPolicyModuleApi moduleApi;

    public DocumentPolicyController(DocumentPolicyModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping("/{resource}")
    public Mono<DocumentPolicyModels.MasterViewDto> create(
            @PathVariable("resource") String resource,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.create(resolve(resource), request.toModel());
    }

    @PutMapping("/{resource}/{id}")
    public Mono<DocumentPolicyModels.MasterViewDto> update(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.update(resolve(resource), id, request.toModel());
    }

    @GetMapping("/{resource}/{id}")
    public Mono<DocumentPolicyModels.MasterViewDto> get(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id
    ) {
        return moduleApi.get(resolve(resource), id);
    }

    @GetMapping("/{resource}")
    public Flux<DocumentPolicyModels.MasterViewDto> list(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "documentCategoryId", required = false) UUID documentCategoryId,
            @RequestParam(name = "documentFor", required = false) String documentFor,
            @RequestParam(name = "documentTypeId", required = false) UUID documentTypeId,
            @RequestParam(name = "workerTypeId", required = false) UUID workerTypeId,
            @RequestParam(name = "employeeCategoryId", required = false) UUID employeeCategoryId,
            @RequestParam(name = "nationalisationCategoryId", required = false) UUID nationalisationCategoryId,
            @RequestParam(name = "legalEntityId", required = false) UUID legalEntityId,
            @RequestParam(name = "jobFamilyId", required = false) UUID jobFamilyId,
            @RequestParam(name = "designationId", required = false) UUID designationId,
            @RequestParam(name = "dependentTypeId", required = false) UUID dependentTypeId,
            @RequestParam(name = "mandatoryFlag", required = false) Boolean mandatoryFlag,
            @RequestParam(name = "onboardingRequiredFlag", required = false) Boolean onboardingRequiredFlag,
            @RequestParam(name = "expiryTrackingRequired", required = false) Boolean expiryTrackingRequired,
            @RequestParam(name = "renewalRequired", required = false) Boolean renewalRequired,
            @RequestParam(name = "blockTransactionOnExpiryFlag", required = false) Boolean blockTransactionOnExpiryFlag,
            @RequestParam(name = "mimeGroup", required = false) String mimeGroup
    ) {
        return moduleApi.list(
                resolve(resource),
                new DocumentPolicyModels.SearchQuery(
                        q,
                        active,
                        limit,
                        offset,
                        sort,
                        documentCategoryId,
                        documentFor,
                        documentTypeId,
                        workerTypeId,
                        employeeCategoryId,
                        nationalisationCategoryId,
                        legalEntityId,
                        jobFamilyId,
                        designationId,
                        dependentTypeId,
                        mandatoryFlag,
                        onboardingRequiredFlag,
                        expiryTrackingRequired,
                        renewalRequired,
                        blockTransactionOnExpiryFlag,
                        mimeGroup));
    }

    @PatchMapping("/{resource}/{id}/status")
    public Mono<DocumentPolicyModels.MasterViewDto> updateStatus(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(resolve(resource), id, new DocumentPolicyModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/{resource}/options")
    public Flux<DocumentPolicyModels.OptionViewDto> options(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return moduleApi.options(resolve(resource), q, limit, activeOnly);
    }

    private DocumentPolicyModels.Resource resolve(String path) {
        return DocumentPolicyModels.Resource.fromPath(path);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record MasterRequest(
            @NotBlank String code,
            @NotBlank String name,
            String shortDescription,
            String documentFor,
            UUID documentCategoryId,
            Boolean attachmentRequired,
            Boolean issueDateRequired,
            Boolean expiryDateRequired,
            Boolean referenceNoRequired,
            Boolean multipleAllowed,
            Integer displayOrder,
            UUID documentTypeId,
            UUID workerTypeId,
            UUID employeeCategoryId,
            UUID nationalisationCategoryId,
            UUID legalEntityId,
            UUID jobFamilyId,
            UUID designationId,
            UUID dependentTypeId,
            Boolean mandatoryFlag,
            Boolean onboardingRequiredFlag,
            Boolean expiryTrackingRequired,
            Boolean renewalRequired,
            List<Integer> alertDaysBefore,
            Integer gracePeriodDays,
            Boolean blockTransactionOnExpiryFlag,
            Boolean versionRequiredFlag,
            Boolean eSignatureRequiredFlag,
            Boolean reackOnVersionChangeFlag,
            Boolean annualReackFlag,
            String mimeGroup,
            Integer maxFileSizeMb,
            String description,
            Boolean active
    ) {

        DocumentPolicyModels.MasterUpsertRequest toModel() {
            return new DocumentPolicyModels.MasterUpsertRequest(
                    code,
                    name,
                    shortDescription,
                    documentFor,
                    documentCategoryId,
                    attachmentRequired,
                    issueDateRequired,
                    expiryDateRequired,
                    referenceNoRequired,
                    multipleAllowed,
                    displayOrder,
                    documentTypeId,
                    workerTypeId,
                    employeeCategoryId,
                    nationalisationCategoryId,
                    legalEntityId,
                    jobFamilyId,
                    designationId,
                    dependentTypeId,
                    mandatoryFlag,
                    onboardingRequiredFlag,
                    expiryTrackingRequired,
                    renewalRequired,
                    alertDaysBefore,
                    gracePeriodDays,
                    blockTransactionOnExpiryFlag,
                    versionRequiredFlag,
                    eSignatureRequiredFlag,
                    reackOnVersionChangeFlag,
                    annualReackFlag,
                    mimeGroup,
                    maxFileSizeMb,
                    description,
                    active);
        }
    }
}
