package com.company.hrms.masterdata.reference.interfaces.rest;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.api.ReferenceStatusUpdateRequest;
import com.company.hrms.masterdata.reference.application.ReferenceControllerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/masters/reference/nationalities")
@Tag(name = "Reference Nationalities", description = "Nationality master APIs")
public class NationalitiesController {

    private final ReferenceControllerService service;

    public NationalitiesController(@Qualifier("nationalitiesService") ReferenceControllerService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<NationalitiesViewDto> create(@Valid @RequestBody NationalitiesUpsertRequestDto request) {
        return service.create(request.toReferenceRequest()).map(ReferenceViewDtoMapper::toNationalities);
    }

    @PutMapping("/{id}")
    public Mono<NationalitiesViewDto> update(@PathVariable("id") UUID id, @Valid @RequestBody NationalitiesUpsertRequestDto request) {
        return service.update(id, request.toReferenceRequest()).map(ReferenceViewDtoMapper::toNationalities);
    }

    @GetMapping("/{id}")
    public Mono<NationalitiesViewDto> getById(@PathVariable("id") UUID id) {
        return service.getById(id).map(ReferenceViewDtoMapper::toNationalities);
    }

    @GetMapping
    public Mono<PagedResult<NationalitiesViewDto>> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "skillCategoryId", required = false) UUID skillCategoryId,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all
    ) {
        return service.list(new ReferenceSearchQuery(q, active, page, size, sort, skillCategoryId, all))
                .map(result -> new PagedResult<>(
                        result.items().stream().map(ReferenceViewDtoMapper::toNationalities).toList(),
                        result.page(),
                        result.size(),
                        result.totalElements(),
                        result.totalPages()));
    }

    @PatchMapping("/{id}/status")
    public Mono<Void> updateStatus(@PathVariable("id") UUID id, @Valid @RequestBody ReferenceStatusUpdateRequest request) {
        return service.updateStatus(id, request.active());
    }

    @GetMapping("/options")
    public Flux<ReferenceOptionViewDto> options(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "true") boolean activeOnly
    ) {
        return service.options(activeOnly);
    }
}
