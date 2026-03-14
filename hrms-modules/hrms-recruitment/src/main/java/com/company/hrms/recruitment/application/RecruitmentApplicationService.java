package com.company.hrms.recruitment.application;

import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.recruitment.api.CandidateSearchQuery;
import com.company.hrms.recruitment.api.CandidateStatus;
import com.company.hrms.recruitment.api.CandidateView;
import com.company.hrms.recruitment.api.CreateCandidateCommand;
import com.company.hrms.recruitment.api.HireCandidateCommand;
import com.company.hrms.recruitment.api.RecruitmentModuleApi;
import com.company.hrms.recruitment.api.UpdateCandidateStatusCommand;
import com.company.hrms.recruitment.domain.Candidate;
import com.company.hrms.recruitment.domain.RecruitmentRepository;
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
    private final EmployeeModuleApi employeeModuleApi;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;
    private final OutboxPublisher outboxPublisher;

    public RecruitmentApplicationService(
            RecruitmentRepository recruitmentRepository,
            EmployeeModuleApi employeeModuleApi,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher,
            OutboxPublisher outboxPublisher
    ) {
        this.recruitmentRepository = recruitmentRepository;
        this.employeeModuleApi = employeeModuleApi;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public Mono<CandidateView> createCandidate(CreateCandidateCommand command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    validate(command);
                    Instant now = Instant.now();
                    Candidate candidate = new Candidate(
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
                                            "Candidate profile created",
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
    public Mono<CandidateView> getCandidate(UUID candidateId) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(candidateId, tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "Candidate not found")))
                        .map(this::toView));
    }

    @Override
    public Flux<CandidateView> searchCandidates(CandidateSearchQuery query) {
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
    public Mono<CandidateView> updateCandidateStatus(UpdateCandidateStatusCommand command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(command.candidateId(), tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "Candidate not found")))
                        .flatMap(before -> recruitmentRepository.updateStatus(command.candidateId(), command.status(), command.reason(), tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "Candidate not found")))
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
                                                command.reason() == null ? "Candidate status transition" : command.reason(),
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
    public Mono<CandidateView> hireCandidate(HireCandidateCommand command) {
        return enablementGuard.requireModuleEnabled("recruitment")
                .then(enablementGuard.requireModuleEnabled("employee"))
                .then(requireTenant())
                .flatMap(tenantId -> recruitmentRepository.findById(command.candidateId(), tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "CANDIDATE_NOT_FOUND", "Candidate not found")))
                        .flatMap(candidate -> {
                            if (!(candidate.status() == CandidateStatus.OFFER_ACCEPTED || candidate.status() == CandidateStatus.OFFER)) {
                                return Mono.error(new HrmsException(
                                        HttpStatus.CONFLICT,
                                        "CANDIDATE_NOT_READY_FOR_HIRE",
                                        "Candidate must be in OFFER or OFFER_ACCEPTED status before hiring"));
                            }

                            return employeeModuleApi.createEmployee(new CreateEmployeeCommand(
                                            command.employeeCode(),
                                            candidate.firstName(),
                                            candidate.lastName(),
                                            candidate.email(),
                                            command.departmentCode(),
                                            command.jobTitle(),
                                            candidate.personId()))
                                    .then(recruitmentRepository.updateStatus(candidate.id(), CandidateStatus.HIRED, "Candidate hired", tenantId))
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
                                                    "Candidate moved to employee lifecycle",
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

    private void validate(CreateCandidateCommand command) {
        if (command.personId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERSON_ID_REQUIRED", "Person id is required");
        }
        if (!StringUtils.hasText(command.candidateCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CANDIDATE_CODE_REQUIRED", "Candidate code is required");
        }
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
    }

    private CandidateView toView(Candidate candidate) {
        return new CandidateView(
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

    private String candidateCreatedPayload(Candidate candidate) {
        return "{\"candidateId\":\"%s\",\"personId\":\"%s\",\"status\":\"%s\"}"
                .formatted(candidate.id(), candidate.personId(), candidate.status().name());
    }

    private String candidateHiredPayload(Candidate candidate, String employeeCode) {
        return "{\"candidateId\":\"%s\",\"personId\":\"%s\",\"employeeCode\":\"%s\"}"
                .formatted(candidate.id(), candidate.personId(), employeeCode);
    }

    private Map<String, Object> candidateSnapshot(Candidate candidate) {
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
