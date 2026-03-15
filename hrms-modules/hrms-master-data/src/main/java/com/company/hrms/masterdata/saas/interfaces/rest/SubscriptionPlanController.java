package com.company.hrms.masterdata.saas.interfaces.rest;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanStatusUpdateRequest;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanUpsertRequest;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanViewDto;
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
@RequestMapping("/api/saas/subscription-plans")
@Tag(name = "SaaS Subscription Plans", description = "Subscription plan APIs")
public class SubscriptionPlanController {

    private final SaasMasterService service;

    public SubscriptionPlanController(SaasMasterService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create subscription plan")
    public Mono<SubscriptionPlanViewDto> create(@Valid @RequestBody SubscriptionPlanUpsertRequest request) {
        return service.createSubscriptionPlan(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update subscription plan")
    public Mono<SubscriptionPlanViewDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody SubscriptionPlanUpsertRequest request
    ) {
        return service.updateSubscriptionPlan(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription plan")
    public Mono<SubscriptionPlanViewDto> getById(@PathVariable("id") UUID id) {
        return service.getSubscriptionPlan(id);
    }

    @GetMapping
    @Operation(summary = "List subscription plans")
    public Mono<PagedResult<SubscriptionPlanViewDto>> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all
    ) {
        return service.listSubscriptionPlans(new TenantSearchQuery(q, active, page, size, sort, all));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate subscription plan")
    public Mono<Void> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody SubscriptionPlanStatusUpdateRequest request
    ) {
        return service.updateSubscriptionPlanStatus(id, request.active());
    }

    @GetMapping("/options")
    @Operation(summary = "List subscription plan options")
    public Flux<SubscriptionPlanViewDto> options(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "true") boolean activeOnly
    ) {
        return service.subscriptionPlanOptions(activeOnly);
    }
}
