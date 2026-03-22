package com.company.hrms.jobarchitecture.controller;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import com.company.hrms.jobarchitecture.service.JobArchitectureModuleApi;
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
@RequestMapping("/api/masters/reference/worker-types")
public class WorkerTypesController {

    private final JobArchitectureModuleApi moduleApi;

    public WorkerTypesController(JobArchitectureModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping
    public Mono<JobArchitectureModels.MasterViewDto> create(@Valid @RequestBody WorkerTypeRequest request) {
        return moduleApi.create(JobArchitectureModels.Resource.WORKER_TYPES, request.toModel());
    }

    @PutMapping("/{id}")
    public Mono<JobArchitectureModels.MasterViewDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody WorkerTypeRequest request
    ) {
        return moduleApi.update(JobArchitectureModels.Resource.WORKER_TYPES, id, request.toModel());
    }

    @PutMapping
    public Mono<JobArchitectureModels.MasterViewDto> updateByQueryId(
            @RequestParam("id") UUID id,
            @Valid @RequestBody WorkerTypeRequest request
    ) {
        return moduleApi.update(JobArchitectureModels.Resource.WORKER_TYPES, id, request.toModel());
    }

    @GetMapping("/{id}")
    public Mono<JobArchitectureModels.MasterViewDto> get(@PathVariable("id") UUID id) {
        return moduleApi.get(JobArchitectureModels.Resource.WORKER_TYPES, id);
    }

    @GetMapping
    public Flux<JobArchitectureModels.MasterViewDto> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        return moduleApi.list(
                JobArchitectureModels.Resource.WORKER_TYPES,
                new JobArchitectureModels.SearchQuery(
                        q,
                        active,
                        limit,
                        offset,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
    }

    @PatchMapping("/{id}/status")
    public Mono<JobArchitectureModels.MasterViewDto> updateStatus(
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(
                JobArchitectureModels.Resource.WORKER_TYPES,
                id,
                new JobArchitectureModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/options")
    public Flux<JobArchitectureModels.OptionViewDto> options(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit
    ) {
        return moduleApi.options(JobArchitectureModels.Resource.WORKER_TYPES, q, limit);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record WorkerTypeRequest(
            @NotBlank String code,
            @NotBlank String name,
            String shortName,
            String description,
            Boolean active
    ) {
        JobArchitectureModels.MasterUpsertRequest toModel() {
            return new JobArchitectureModels.MasterUpsertRequest(
                    code,
                    name,
                    shortName,
                    null,
                    null,
                    description,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    active);
        }
    }
}
