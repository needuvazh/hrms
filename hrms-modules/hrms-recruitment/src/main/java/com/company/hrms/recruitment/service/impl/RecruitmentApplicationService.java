package com.company.hrms.recruitment.service.impl;

import com.company.hrms.recruitment.model.*;
import com.company.hrms.recruitment.repository.*;
import com.company.hrms.recruitment.service.*;

import com.company.hrms.employee.model.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleClient;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.recruitment.model.CandidateSearchQueryDto;
import com.company.hrms.recruitment.model.CandidateStatus;
import com.company.hrms.recruitment.model.CandidateViewDto;
import com.company.hrms.recruitment.model.CreateCandidateCommandDto;
import com.company.hrms.recruitment.model.HireCandidateCommandDto;
import com.company.hrms.recruitment.service.RecruitmentModuleApi;
import com.company.hrms.recruitment.model.UpdateCandidateStatusCommandDto;
import com.company.hrms.recruitment.model.CandidateDto;
import com.company.hrms.recruitment.repository.RecruitmentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RecruitmentApplicationService implements RecruitmentModuleApi {

    private static final int DEFAULT_LIMIT = 50;

    private final RecruitmentRepository recruitmentRepository;
    private final EmployeeModuleClient employeeModuleClient;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;
    private final OutboxPublisher outboxPublisher;

    public RecruitmentApplicationService(
            RecruitmentRepository recruitmentRepository,
            EmployeeModuleClient employeeModuleClient,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher,
            OutboxPublisher outboxPublisher
    ) {
        this.recruitmentRepository = recruitmentRepository;
        this.employeeModuleClient = employeeModuleClient;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public Mono<CandidateViewDto> createCandidate(CreateCandidateCommandDto command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    validate(command);
                    Instant now = Instant.now();
                    CandidateDto candidate = new CandidateDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.personId(),
                            command.candidateCode(),
                            command.firstName(),
                            command.lastName(),
                            command.email(),
                            command.jobPostingCode(),
                            CandidateStatus.APPLIED,
                            now,
                            now);

                    return recruitmentRepository.save(candidate)
                            .flatMap(saved -> auditEventPublisher.publish(AuditEvent.detailed(
                                            "system",
                                            tenantId,
                                            "CANDIDATE_CREATED",
                                            "CANDIDATE",
                                            saved.id().toString(),
                                            "recruitment",
                                            1L,
                                            List.of("personId", "candidateCode", "firstName", "lastName", "email", "jobPostingCode", "status"),
                                            Map.of(),
                                            candidateSnapshot(saved),
                                            "system",
                                            "system",
                                            null,
                                            "CandidateDto profile created",
                                            "hrms-recruitment",
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            Map.of(
                                                    "candidateCode", saved.candidateCode(),
                                                    "personId", saved.personId().toString()),
                                            false))
                                    .then(outboxPublisher.publish(new OutboxEvent(
                                            tenantId,
                                            "CANDIDATE",
                                            saved.id().toString(),
                                            "CandidateCreated",
                                            candidateCreatedPayload(saved),
                                            now)))
                                    .thenReturn(saved))
                            .map(this::toView);
                });
    }

    @Override
    public Mono<CandidateViewDto> getCandidate(UUID candidateId) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(candidateId, tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "CandidateDto not found")))
                        .map(this::toView));
    }

    @Override
    public Flux<CandidateViewDto> searchCandidates(CandidateSearchQueryDto query) {
        int limit = query.limit() > 0 ? query.limit() : DEFAULT_LIMIT;
        int offset = Math.max(query.offset(), 0);

        return enablementGuard.requireModuleEnabled("recruitment")
                .thenMany(requireTenant().flatMapMany(tenantId -> recruitmentRepository.search(
                                query.query(),
                                query.status(),
                                limit,
                                offset,
                                tenantId)
                        .map(this::toView)));
    }

    @Override
    public Mono<CandidateViewDto> updateCandidateStatus(UpdateCandidateStatusCommandDto command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(command.candidateId(), tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "CandidateDto not found")))
                        .flatMap(before -> recruitmentRepository.updateStatus(command.candidateId(), command.status(), command.reason(), tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "CandidateDto not found")))
                                .flatMap(updated -> auditEventPublisher.publish(AuditEvent.detailed(
                                                "system",
                                                tenantId,
                                                "CANDIDATE_STATUS_UPDATED",
                                                "CANDIDATE",
                                                updated.id().toString(),
                                                "recruitment",
                                                2L,
                                                List.of("status"),
                                                Map.of("status", before.status().name()),
                                                Map.of("status", updated.status().name()),
                                                "system",
                                                "system",
                                                null,
                                                command.reason() == null ? "CandidateDto status transition" : command.reason(),
                                                "hrms-recruitment",
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                Map.of("status", updated.status().name()),
                                                false))
                                        .thenReturn(updated))))
                .map(this::toView);
    }

    @Override
    public Mono<CandidateViewDto> hireCandidate(HireCandidateCommandDto command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(enablementGuard.requireModuleEnabled("employee"))
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(command.candidateId(), tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "CandidateDto not found")))
                        .flatMap(candidate -> {
                            if (!(candidate.status() == CandidateStatus.OFFER_ACCEPTED || candidate.status() == CandidateStatus.OFFER)) {
                                return Mono.error(new HrmsException(
                                        HttpStatus.CONFLICT,
                                        "CANDIDATE_NOT_READY_FOR_HIRE",
                                        "CandidateDto must be in OFFER or OFFER_ACCEPTED status before hiring"));
                            }

                            return employeeModuleClient.createEmployee(new CreateEmployeeCommandDto(
                                            command.employeeCode(),
                                            candidate.firstName(),
                                            candidate.lastName(),
                                            candidate.email(),
                                            command.departmentCode(),
                                            command.jobTitle(),
                                            candidate.personId()))
                                    .then(recruitmentRepository.updateStatus(candidate.id(), CandidateStatus.HIRED, "CandidateDto hired", tenantId))
                                    .switchIfEmpty(Mono.error(new HrmsException(
                                            HttpStatus.INTERNAL_SERVER_ERROR,
                                            "CANDIDATE_HIRE_FAILED",
                                            "Unable to update candidate status to HIRED")))
                                    .flatMap(updated -> auditEventPublisher.publish(AuditEvent.detailed(
                                                    "system",
                                                    tenantId,
                                                    "CANDIDATE_HIRED",
                                                    "CANDIDATE",
                                                    updated.id().toString(),
                                                    "recruitment",
                                                    3L,
                                                    List.of("status", "employeeCode"),
                                                    Map.of("status", candidate.status().name()),
                                                    Map.of(
                                                            "status", updated.status().name(),
                                                            "employeeCode", command.employeeCode()),
                                                    "system",
                                                    "system",
                                                    null,
                                                    "CandidateDto moved to employee lifecycle",
                                                    "hrms-recruitment",
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    Map.of(
                                                            "personId", updated.personId().toString(),
                                                            "employeeCode", command.employeeCode()),
                                                    false))
                                            .then(outboxPublisher.publish(new OutboxEvent(
                                                    tenantId,
                                                    "CANDIDATE",
                                                    updated.id().toString(),
                                                    "CandidateHired",
                                                    candidateHiredPayload(updated, command.employeeCode()),
                                                    Instant.now())))
                                            .thenReturn(updated));
                        })
                        .map(this::toView));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validate(CreateCandidateCommandDto command) {
        if (command.personId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERSON_ID_REQUIRED", "PersonDto id is required");
        }
        if (!StringUtils.hasText(command.candidateCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CANDIDATE_CODE_REQUIRED", "CandidateDto code is required");
        }
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
    }

    private CandidateViewDto toView(CandidateDto candidate) {
        return new CandidateViewDto(
                candidate.id(),
                candidate.tenantId(),
                candidate.personId(),
                candidate.candidateCode(),
                candidate.firstName(),
                candidate.lastName(),
                candidate.email(),
                candidate.jobPostingCode(),
                candidate.status(),
                candidate.createdAt(),
                candidate.updatedAt());
    }

    private String candidateCreatedPayload(CandidateDto candidate) {
        return "{\"candidateId\":\"%s\",\"personId\":\"%s\",\"status\":\"%s\"}"
                .formatted(candidate.id(), candidate.personId(), candidate.status().name());
    }

    private String candidateHiredPayload(CandidateDto candidate, String employeeCode) {
        return "{\"candidateId\":\"%s\",\"personId\":\"%s\",\"employeeCode\":\"%s\"}"
                .formatted(candidate.id(), candidate.personId(), employeeCode);
    }

    private Map<String, Object> candidateSnapshot(CandidateDto candidate) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("personId", candidate.personId());
        snapshot.put("candidateCode", candidate.candidateCode());
        snapshot.put("firstName", candidate.firstName());
        snapshot.put("lastName", candidate.lastName());
        snapshot.put("email", candidate.email());
        snapshot.put("jobPostingCode", candidate.jobPostingCode());
        snapshot.put("status", candidate.status().name());
        return snapshot;
    }
}
