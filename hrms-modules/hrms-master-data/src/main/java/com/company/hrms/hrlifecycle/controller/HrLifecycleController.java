package com.company.hrms.hrlifecycle.controller;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import com.company.hrms.hrlifecycle.service.HrLifecycleModuleApi;
import com.company.hrms.masterdata.reference.api.PagedResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
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
@RequestMapping("/api/hr-lifecycle")
public class HrLifecycleController {

    private final HrLifecycleModuleApi moduleApi;

    public HrLifecycleController(HrLifecycleModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping("/{resource}")
    public Mono<HrLifecycleModels.MasterViewDto> create(@PathVariable("resource") String resource, @Valid @RequestBody MasterRequest request) {
        return moduleApi.create(resolve(resource), request.toModel());
    }

    @PutMapping("/{resource}/{id}")
    public Mono<HrLifecycleModels.MasterViewDto> update(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.update(resolve(resource), id, request.toModel());
    }

    @GetMapping("/{resource}/{id}")
    public Mono<HrLifecycleModels.MasterViewDto> get(@PathVariable("resource") String resource, @PathVariable("id") UUID id) {
        return moduleApi.get(resolve(resource), id);
    }

    @GetMapping("/{resource}")
    public Mono<PagedResult<HrLifecycleModels.MasterViewDto>> list(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "countryCode", required = false) String countryCode,
            @RequestParam(name = "calendarYear", required = false) Integer calendarYear,
            @RequestParam(name = "calendarType", required = false) String calendarType,
            @RequestParam(name = "hijriEnabledFlag", required = false) Boolean hijriEnabledFlag,
            @RequestParam(name = "weekendAdjustmentFlag", required = false) Boolean weekendAdjustmentFlag,
            @RequestParam(name = "leaveCategory", required = false) String leaveCategory,
            @RequestParam(name = "paidFlag", required = false) Boolean paidFlag,
            @RequestParam(name = "supportingDocumentRequiredFlag", required = false) Boolean supportingDocumentRequiredFlag,
            @RequestParam(name = "genderApplicability", required = false) String genderApplicability,
            @RequestParam(name = "religionApplicability", required = false) String religionApplicability,
            @RequestParam(name = "nationalisationApplicability", required = false) String nationalisationApplicability,
            @RequestParam(name = "shiftType", required = false) String shiftType,
            @RequestParam(name = "overnightFlag", required = false) Boolean overnightFlag,
            @RequestParam(name = "sourceType", required = false) String sourceType,
            @RequestParam(name = "trustedSourceFlag", required = false) Boolean trustedSourceFlag,
            @RequestParam(name = "manualOverrideFlag", required = false) Boolean manualOverrideFlag,
            @RequestParam(name = "assigneeType", required = false) String assigneeType,
            @RequestParam(name = "mandatoryFlag", required = false) Boolean mandatoryFlag,
            @RequestParam(name = "taskCategory", required = false) String taskCategory,
            @RequestParam(name = "eventGroup", required = false) String eventGroup,
            @RequestParam(name = "employmentActiveFlag", required = false) Boolean employmentActiveFlag,
            @RequestParam(name = "selfServiceAccessFlag", required = false) Boolean selfServiceAccessFlag,
            @RequestParam(name = "entryStageFlag", required = false) Boolean entryStageFlag,
            @RequestParam(name = "exitStageFlag", required = false) Boolean exitStageFlag
    ) {
        return moduleApi.list(resolve(resource), new HrLifecycleModels.SearchQuery(
                q,
                active,
                limit,
                offset,
                sort,
                countryCode,
                calendarYear,
                calendarType,
                hijriEnabledFlag,
                weekendAdjustmentFlag,
                leaveCategory,
                paidFlag,
                supportingDocumentRequiredFlag,
                genderApplicability,
                religionApplicability,
                nationalisationApplicability,
                shiftType,
                overnightFlag,
                sourceType,
                trustedSourceFlag,
                manualOverrideFlag,
                assigneeType,
                mandatoryFlag,
                taskCategory,
                eventGroup,
                employmentActiveFlag,
                selfServiceAccessFlag,
                entryStageFlag,
                exitStageFlag));
    }

    @PatchMapping("/{resource}/{id}/status")
    public Mono<HrLifecycleModels.MasterViewDto> updateStatus(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(resolve(resource), id, new HrLifecycleModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/{resource}/options")
    public Flux<HrLifecycleModels.OptionViewDto> options(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return moduleApi.options(resolve(resource), q, limit, activeOnly);
    }

    private HrLifecycleModels.Resource resolve(String resource) {
        return HrLifecycleModels.Resource.fromPath(resource);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record MasterRequest(
            String code,
            String name,
            String countryCode,
            Integer calendarYear,
            String calendarType,
            Boolean hijriEnabledFlag,
            Boolean weekendAdjustmentFlag,
            String leaveCategory,
            Boolean paidFlag,
            Boolean supportingDocumentRequiredFlag,
            String genderApplicability,
            String religionApplicability,
            String nationalisationApplicability,
            String shiftType,
            LocalTime startTime,
            LocalTime endTime,
            Integer breakDurationMinutes,
            Boolean overnightFlag,
            Integer graceInMinutes,
            Integer graceOutMinutes,
            String sourceType,
            Boolean trustedSourceFlag,
            Boolean manualOverrideFlag,
            String taskCategory,
            Boolean mandatoryFlag,
            String assigneeType,
            String eventGroup,
            Boolean employmentActiveFlag,
            Boolean selfServiceAccessFlag,
            Integer stageOrder,
            Boolean entryStageFlag,
            Boolean exitStageFlag,
            String description,
            Boolean active
    ) {
        HrLifecycleModels.MasterUpsertRequest toModel() {
            return new HrLifecycleModels.MasterUpsertRequest(
                    code,
                    name,
                    countryCode,
                    calendarYear,
                    calendarType,
                    hijriEnabledFlag,
                    weekendAdjustmentFlag,
                    leaveCategory,
                    paidFlag,
                    supportingDocumentRequiredFlag,
                    genderApplicability,
                    religionApplicability,
                    nationalisationApplicability,
                    shiftType,
                    startTime,
                    endTime,
                    breakDurationMinutes,
                    overnightFlag,
                    graceInMinutes,
                    graceOutMinutes,
                    sourceType,
                    trustedSourceFlag,
                    manualOverrideFlag,
                    taskCategory,
                    mandatoryFlag,
                    assigneeType,
                    eventGroup,
                    employmentActiveFlag,
                    selfServiceAccessFlag,
                    stageOrder,
                    entryStageFlag,
                    exitStageFlag,
                    description,
                    active);
        }
    }
}
