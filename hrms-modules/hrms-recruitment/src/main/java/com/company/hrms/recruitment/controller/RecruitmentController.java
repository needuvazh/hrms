package com.company.hrms.recruitment.controller;

import com.company.hrms.recruitment.model.*;
import com.company.hrms.recruitment.service.*;

import com.company.hrms.recruitment.model.CandidateSearchQueryDto;
import com.company.hrms.recruitment.model.CandidateStatus;
import com.company.hrms.recruitment.model.CandidateViewDto;
import com.company.hrms.recruitment.model.CreateCandidateCommandDto;
import com.company.hrms.recruitment.model.HireCandidateCommandDto;
import com.company.hrms.recruitment.service.RecruitmentModuleApi;
import com.company.hrms.recruitment.model.UpdateCandidateStatusCommandDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/recruitment/candidates")
@Tag(name = "Recruitment", description = "CandidateDto lifecycle and hiring APIs")
public class RecruitmentController {

    private final RecruitmentModuleApi recruitmentModuleApi;

    public RecruitmentController(RecruitmentModuleApi recruitmentModuleApi) {
        this.recruitmentModuleApi = recruitmentModuleApi;
    }

    @PostMapping
    @Operation(summary = "Create candidate", description = "Registers a candidate profile linked to an existing person record.")
    public Mono<CandidateViewDto> createCandidate(@Valid @RequestBody CreateCandidateRequest request) {
        return recruitmentModuleApi.createCandidate(new CreateCandidateCommandDto(
                request.personId(),
                request.candidateCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.jobPostingCode()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate", description = "Loads one candidate using candidate UUID.")
    public Mono<CandidateViewDto> getCandidate(
            @Parameter(description = "Unique candidate identifier", example = "44db3cb5-1f53-4554-af5f-01199f12cae4")
            @PathVariable("id") UUID candidateId
    ) {
        return recruitmentModuleApi.getCandidate(candidateId);
    }

    @GetMapping
    @Operation(summary = "Search candidates", description = "Searches candidates by free-text keyword, optional status, and pagination controls.")
    public Flux<CandidateViewDto> searchCandidates(
            @Parameter(description = "Free-text query for candidate code, name, or email", example = "engineer")
            @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "CandidateDto process status filter", example = "SCREENING")
            @RequestParam(name = "status", required = false) CandidateStatus status,
            @Parameter(description = "Maximum rows to return", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
            @Parameter(description = "Zero-based result offset", example = "0")
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset
    ) {
        return recruitmentModuleApi.searchCandidates(new CandidateSearchQueryDto(query, status, limit, offset));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update candidate status", description = "Moves a candidate to a new status with optional justification.")
    public Mono<CandidateViewDto> updateStatus(
            @Parameter(description = "Unique candidate identifier", example = "44db3cb5-1f53-4554-af5f-01199f12cae4")
            @PathVariable("id") UUID candidateId,
            @Valid @RequestBody UpdateCandidateStatusRequest request
    ) {
        return recruitmentModuleApi.updateCandidateStatus(new UpdateCandidateStatusCommandDto(candidateId, request.status(), request.reason()));
    }

    @PostMapping("/{id}/hire")
    @Operation(summary = "Hire candidate", description = "Converts a candidate into an employee profile and marks recruitment flow as hired.")
    public Mono<CandidateViewDto> hireCandidate(
            @Parameter(description = "Unique candidate identifier", example = "44db3cb5-1f53-4554-af5f-01199f12cae4")
            @PathVariable("id") UUID candidateId,
            @Valid @RequestBody HireCandidateRequest request
    ) {
        return recruitmentModuleApi.hireCandidate(new HireCandidateCommandDto(candidateId, request.employeeCode(), request.departmentCode(), request.jobTitle()));
    }

    public record CreateCandidateRequest(
            @Schema(description = "Linked person profile identifier", example = "ecf8c677-9899-41d6-a2fe-b4bf4a47aa04")
            @NotNull UUID personId,
            @Schema(description = "Business candidate code", example = "CAND-1101")
            @NotBlank String candidateCode,
            @Schema(description = "Given name", example = "Fatima")
            @NotBlank String firstName,
            @Schema(description = "Family name", example = "Ali")
            String lastName,
            @Schema(description = "Primary candidate email", example = "fatima.ali@example.com")
            @NotBlank @Email String email,
            @Schema(description = "Job posting code being applied for", example = "POST-ENG-01")
            String jobPostingCode
    ) {
    }

    public record UpdateCandidateStatusRequest(
            @Schema(description = "Target candidate status", example = "INTERVIEW")
            @NotNull CandidateStatus status,
            @Schema(description = "Optional reason for the status transition", example = "Cleared technical screening")
            String reason
    ) {
    }

    public record HireCandidateRequest(
            @Schema(description = "New employee code to assign", example = "EMP-20077")
            @NotBlank String employeeCode,
            @Schema(description = "Department code for employee placement", example = "ENG")
            String departmentCode,
            @Schema(description = "Job title for employee placement", example = "Software Engineer")
            String jobTitle
    ) {
    }
}
