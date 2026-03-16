package com.company.hrms.compliance.controller;

import com.company.hrms.compliance.model.ComplianceModels;
import com.company.hrms.compliance.service.ComplianceModuleApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceModuleApi moduleApi;

    public ComplianceController(ComplianceModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping("/{resource}")
    public Mono<ComplianceModels.MasterViewDto> create(
            @PathVariable("resource") String resource,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.create(resolve(resource), request.toModel());
    }

    @PutMapping("/{resource}/{id}")
    public Mono<ComplianceModels.MasterViewDto> update(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.update(resolve(resource), id, request.toModel());
    }

    @GetMapping("/{resource}/{id}")
    public Mono<ComplianceModels.MasterViewDto> get(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id
    ) {
        return moduleApi.get(resolve(resource), id);
    }

    @GetMapping("/{resource}")
    public Flux<ComplianceModels.MasterViewDto> list(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        return moduleApi.list(resolve(resource), new ComplianceModels.SearchQuery(q, active, limit, offset));
    }

    @PatchMapping("/{resource}/{id}/status")
    public Mono<ComplianceModels.MasterViewDto> updateStatus(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(resolve(resource), id, new ComplianceModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/{resource}/options")
    public Flux<ComplianceModels.OptionViewDto> options(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        return moduleApi.options(resolve(resource), q, limit);
    }

    private ComplianceModels.Resource resolve(String path) {
        return ComplianceModels.Resource.fromPath(path);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record MasterRequest(
            @NotBlank String code,
            @NotBlank String name,
            String visaCategory,
            String appliesTo,
            Boolean renewableFlag,
            Boolean expiryTrackingRequired,
            Boolean omaniFlag,
            Boolean countsForOmanisationFlag,
            Boolean pensionEligibleFlag,
            Boolean occupationalHazardEligibleFlag,
            Boolean govtContributionApplicableFlag,
            Integer priorityOrder,
            Boolean insuranceEligibleFlag,
            Boolean familyVisaEligibleFlag,
            String description,
            Boolean active
    ) {

        ComplianceModels.MasterUpsertRequest toModel() {
            return new ComplianceModels.MasterUpsertRequest(
                    code,
                    name,
                    visaCategory,
                    appliesTo,
                    renewableFlag,
                    expiryTrackingRequired,
                    omaniFlag,
                    countsForOmanisationFlag,
                    pensionEligibleFlag,
                    occupationalHazardEligibleFlag,
                    govtContributionApplicableFlag,
                    priorityOrder,
                    insuranceEligibleFlag,
                    familyVisaEligibleFlag,
                    description,
                    active);
        }
    }
}
