package com.company.hrms.masterdata.saas.interfaces.rest;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.saas.api.FeatureFlagOptionViewDto;
import com.company.hrms.masterdata.saas.api.FeatureFlagStatusUpdateRequest;
import com.company.hrms.masterdata.saas.api.FeatureFlagUpsertRequest;
import com.company.hrms.masterdata.saas.api.FeatureFlagViewDto;
import com.company.hrms.masterdata.saas.api.TenantSearchQuery;
import com.company.hrms.masterdata.saas.application.SaasMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/saas/feature-flags")
@Tag(name = "SaaS Feature Flags", description = "Feature flag catalog APIs")
public class FeatureFlagController {

    private final SaasMasterService service;

    public FeatureFlagController(SaasMasterService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create feature flag")
    public Mono<FeatureFlagViewDto> create(@Valid @RequestBody FeatureFlagUpsertRequest request) {
        return service.createFeatureFlag(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update feature flag")
    public Mono<FeatureFlagViewDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody FeatureFlagUpsertRequest request
    ) {
        return service.updateFeatureFlag(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature flag")
    public Mono<FeatureFlagViewDto> getById(@PathVariable("id") UUID id) {
        return service.getFeatureFlag(id);
    }

    @GetMapping
    @Operation(summary = "List feature flags")
    public Mono<PagedResult<FeatureFlagViewDto>> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all
    ) {
        return service.listFeatureFlags(new TenantSearchQuery(q, active, page, size, sort, all));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate feature flag")
    public Mono<Void> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody FeatureFlagStatusUpdateRequest request
    ) {
        return service.updateFeatureFlagStatus(id, request.active());
    }

    @GetMapping("/options")
    @Operation(summary = "List feature flag options")
    public Flux<FeatureFlagOptionViewDto> options(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "true") boolean activeOnly
    ) {
        return service.featureFlagOptions(activeOnly);
    }
}
