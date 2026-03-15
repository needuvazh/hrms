package com.company.hrms.jobarchitecture.controller;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import com.company.hrms.jobarchitecture.service.JobArchitectureModuleApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
@RequestMapping("/api/job-architecture")
public class JobArchitectureController {

    private final JobArchitectureModuleApi moduleApi;

    public JobArchitectureController(JobArchitectureModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping("/{resource}")
    public Mono<JobArchitectureModels.MasterViewDto> create(
            @PathVariable("resource") String resource,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.create(toResource(resource), request.toModel());
    }

    @PutMapping("/{resource}/{id}")
    public Mono<JobArchitectureModels.MasterViewDto> update(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.update(toResource(resource), id, request.toModel());
    }

    @GetMapping("/{resource}/{id}")
    public Mono<JobArchitectureModels.MasterViewDto> get(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id
    ) {
        return moduleApi.get(toResource(resource), id);
    }

    @GetMapping("/{resource}")
    public Flux<JobArchitectureModels.MasterViewDto> list(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset,
            @RequestParam(name = "jobFamilyId", required = false) UUID jobFamilyId,
            @RequestParam(name = "jobFunctionId", required = false) UUID jobFunctionId,
            @RequestParam(name = "gradeBandId", required = false) UUID gradeBandId,
            @RequestParam(name = "designationId", required = false) UUID designationId,
            @RequestParam(name = "gradeId", required = false) UUID gradeId,
            @RequestParam(name = "legalEntityId", required = false) UUID legalEntityId,
            @RequestParam(name = "branchId", required = false) UUID branchId,
            @RequestParam(name = "departmentId", required = false) UUID departmentId,
            @RequestParam(name = "costCenterId", required = false) UUID costCenterId,
            @RequestParam(name = "vacancyStatus", required = false) String vacancyStatus,
            @RequestParam(name = "criticalPositionFlag", required = false) Boolean criticalPositionFlag,
            @RequestParam(name = "employeeCategoryId", required = false) UUID employeeCategoryId
    ) {
        return moduleApi.list(
                toResource(resource),
                new JobArchitectureModels.SearchQuery(
                        q,
                        active,
                        limit,
                        offset,
                        jobFamilyId,
                        jobFunctionId,
                        gradeBandId,
                        designationId,
                        gradeId,
                        legalEntityId,
                        branchId,
                        departmentId,
                        costCenterId,
                        vacancyStatus,
                        criticalPositionFlag,
                        employeeCategoryId));
    }

    @PatchMapping("/{resource}/{id}/status")
    public Mono<JobArchitectureModels.MasterViewDto> updateStatus(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(toResource(resource), id, new JobArchitectureModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/{resource}/options")
    public Flux<JobArchitectureModels.OptionViewDto> options(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        return moduleApi.options(toResource(resource), q, limit);
    }

    private JobArchitectureModels.Resource toResource(String pathResource) {
        String normalized = pathResource.replace('-', '_').toUpperCase();
        return JobArchitectureModels.Resource.valueOf(normalized);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record MasterRequest(
            @NotBlank String code,
            @NotBlank String name,
            String shortName,
            UUID jobFamilyId,
            UUID jobFunctionId,
            String description,
            Integer bandOrder,
            UUID gradeBandId,
            Integer rankingOrder,
            BigDecimal salaryScaleMin,
            BigDecimal salaryScaleMax,
            UUID designationId,
            UUID gradeId,
            UUID legalEntityId,
            UUID branchId,
            UUID businessUnitId,
            UUID divisionId,
            UUID departmentId,
            UUID sectionId,
            UUID workLocationId,
            UUID costCenterId,
            UUID reportingUnitId,
            UUID reportsToPositionId,
            Integer approvedHeadcount,
            Integer filledHeadcount,
            String vacancyStatus,
            Boolean criticalPositionFlag,
            Boolean contractRequired,
            UUID employeeCategoryId,
            Boolean fixedTermFlag,
            Integer defaultDurationDays,
            Boolean renewalAllowed,
            Integer durationDays,
            Boolean extensionAllowed,
            Integer maxExtensionDays,
            Boolean confirmationRequired,
            Integer employeeNoticeDays,
            Integer employerNoticeDays,
            Boolean paymentInLieuAllowed,
            Boolean gardenLeaveAllowed,
            String separationCategory,
            Boolean voluntaryFlag,
            Boolean finalSettlementRequired,
            Boolean active
    ) {
        JobArchitectureModels.MasterUpsertRequest toModel() {
            return new JobArchitectureModels.MasterUpsertRequest(
                    code,
                    name,
                    shortName,
                    jobFamilyId,
                    jobFunctionId,
                    description,
                    bandOrder,
                    gradeBandId,
                    rankingOrder,
                    salaryScaleMin,
                    salaryScaleMax,
                    designationId,
                    gradeId,
                    legalEntityId,
                    branchId,
                    businessUnitId,
                    divisionId,
                    departmentId,
                    sectionId,
                    workLocationId,
                    costCenterId,
                    reportingUnitId,
                    reportsToPositionId,
                    approvedHeadcount,
                    filledHeadcount,
                    vacancyStatus,
                    criticalPositionFlag,
                    contractRequired,
                    employeeCategoryId,
                    fixedTermFlag,
                    defaultDurationDays,
                    renewalAllowed,
                    durationDays,
                    extensionAllowed,
                    maxExtensionDays,
                    confirmationRequired,
                    employeeNoticeDays,
                    employerNoticeDays,
                    paymentInLieuAllowed,
                    gardenLeaveAllowed,
                    separationCategory,
                    voluntaryFlag,
                    finalSettlementRequired,
                    active);
        }
    }
}
