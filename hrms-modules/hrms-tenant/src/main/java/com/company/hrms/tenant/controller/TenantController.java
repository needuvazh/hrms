package com.company.hrms.tenant.controller;

import com.company.hrms.tenant.model.*;
import com.company.hrms.tenant.service.*;

import com.company.hrms.tenant.model.CountryViewDto;
import com.company.hrms.tenant.model.TenantCountryConfigViewDto;
import com.company.hrms.tenant.service.TenantModuleApi;
import com.company.hrms.tenant.model.TenantViewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "TenantDto", description = "TenantDto onboarding and country configuration APIs")
public class TenantController {

    private final TenantModuleApi tenantModuleApi;

    public TenantController(TenantModuleApi tenantModuleApi) {
        this.tenantModuleApi = tenantModuleApi;
    }

    @GetMapping("/{tenantCode}")
    @Operation(summary = "Get tenant by code", description = "Loads one tenant using its business tenant code.")
    public Mono<TenantViewDto> getTenant(
            @Parameter(description = "TenantDto code assigned during tenant onboarding", example = "acme")
            @PathVariable("tenantCode") String tenantCode
    ) {
        return tenantModuleApi.getTenantByCode(tenantCode);
    }

    @GetMapping("/countries")
    @Operation(summary = "List supported countries", description = "Returns all available countries supported by the tenant module.")
    public Flux<CountryViewDto> listCountries() {
        return tenantModuleApi.listCountries();
    }

    @GetMapping("/{tenantCode}/countries")
    @Operation(summary = "List tenant countries", description = "Returns country-level configuration enabled for the target tenant.")
    public Flux<TenantCountryConfigViewDto> listTenantCountries(
            @Parameter(description = "TenantDto code assigned during tenant onboarding", example = "acme")
            @PathVariable("tenantCode") String tenantCode
    ) {
        return tenantModuleApi.listTenantCountries(tenantCode);
    }
}
