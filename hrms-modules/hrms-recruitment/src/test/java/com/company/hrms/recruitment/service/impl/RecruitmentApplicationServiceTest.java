package com.company.hrms.recruitment.service.impl;

import com.company.hrms.recruitment.model.*;
import com.company.hrms.recruitment.repository.*;
import com.company.hrms.recruitment.service.*;

import com.company.hrms.employee.model.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleClient;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.recruitment.model.CandidateSearchQueryDto;
import com.company.hrms.recruitment.model.CandidateStatus;
import com.company.hrms.recruitment.model.CreateCandidateCommandDto;
import com.company.hrms.recruitment.model.HireCandidateCommandDto;
import com.company.hrms.recruitment.model.CandidateDto;
import com.company.hrms.recruitment.repository.RecruitmentRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentApplicationServiceTest {

    private final InMemoryRecruitmentRepository recruitmentRepository = new InMemoryRecruitmentRepository();
    private final RecordingEmployeeModuleApi employeeModuleApi = new RecordingEmployeeModuleApi();
    private final RecordingAuditEventPublisher auditEventPublisher = new RecordingAuditEventPublisher();
    private final RecordingOutboxPublisher outboxPublisher = new RecordingOutboxPublisher();
    private final RecruitmentApplicationService recruitmentApplicationService = new RecruitmentApplicationService(
            recruitmentRepository,
            employeeModuleApi,
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new AlwaysEnabledFeatureToggleService()),
            auditEventPublisher,
            outboxPublisher);

    @Test
    void createAndHireCandidateProducesDetailedAuditAndEmployeeCommand() {
        CreateCandidateCommandDto createCommand = new CreateCandidateCommandDto(
                UUID.randomUUID(),
                "CAN-1001",
                "Aisha",
                "Khan",
                "aisha.khan@example.com",
                "ENG-001");

        StepVerifier.create(recruitmentApplicationService.createCandidate(createCommand)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(created -> {
                    assertEquals("default", created.tenantId());
                    assertEquals(CandidateStatus.APPLIED, created.status());
                })
                .verifyComplete();

        UUID candidateId = recruitmentRepository.lastCreatedId;
        assertNotNull(candidateId);

        StepVerifier.create(recruitmentApplicationService.updateCandidateStatus(
                                new com.company.hrms.recruitment.model.UpdateCandidateStatusCommandDto(
                                        candidateId,
                                        CandidateStatus.OFFER_ACCEPTED,
                                        "Offer accepted by candidate"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(updated -> assertEquals(CandidateStatus.OFFER_ACCEPTED, updated.status()))
                .verifyComplete();

        StepVerifier.create(recruitmentApplicationService.hireCandidate(
                                new HireCandidateCommandDto(candidateId, "EMP-5001", "ENG", "Engineer"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(hired -> assertEquals(CandidateStatus.HIRED, hired.status()))
                .verifyComplete();

        assertNotNull(employeeModuleApi.lastCreateCommand);
        assertEquals("EMP-5001", employeeModuleApi.lastCreateCommand.employeeCode());
        assertEquals(createCommand.personId(), employeeModuleApi.lastCreateCommand.personId());

        assertTrue(auditEventPublisher.events.stream().anyMatch(event ->
                "CANDIDATE_HIRED".equals(event.action())
                        && "recruitment".equals(event.moduleName())
                        && event.changedFields().contains("status")));

        assertTrue(outboxPublisher.eventTypes.contains("CandidateCreated"));
        assertTrue(outboxPublisher.eventTypes.contains("CandidateHired"));
    }

    static class RecordingAuditEventPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<AuditEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            events.add(event);
            return Mono.empty();
        }
    }

    static class RecordingOutboxPublisher implements OutboxPublisher {
        private final CopyOnWriteArrayList<String> eventTypes = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(OutboxEvent event) {
            eventTypes.add(event.eventType());
            return Mono.empty();
        }
    }

    static class AlwaysEnabledFeatureToggleService implements FeatureToggleService {
        @Override
        public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> currentTenantHasModule(String moduleKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> currentTenantHasFeature(String featureKey) {
            return Mono.just(true);
        }
    }

    static class RecordingEmployeeModuleApi implements EmployeeModuleClient {
        private volatile CreateEmployeeCommandDto lastCreateCommand;

        @Override
        public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
            this.lastCreateCommand = command;
            Instant now = Instant.now();
            return Mono.just(new EmployeeViewDto(
                    UUID.randomUUID(),
                    "default",
                    command.employeeCode(),
                    command.personId(),
                    command.firstName(),
                    command.lastName(),
                    command.email(),
                    command.departmentCode(),
                    command.jobTitle(),
                    now,
                    now));
        }

        @Override
        public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
            return Mono.empty();
        }

        @Override
        public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
            return Flux.empty();
        }
    }

    static class InMemoryRecruitmentRepository implements RecruitmentRepository {
        private final Map<UUID, CandidateDto> storage = new ConcurrentHashMap<>();
        private volatile UUID lastCreatedId;

        @Override
        public Mono<CandidateDto> save(CandidateDto candidate) {
            storage.put(candidate.id(), candidate);
            lastCreatedId = candidate.id();
            return Mono.just(candidate);
        }

        @Override
        public Mono<CandidateDto> findById(UUID candidateId, String tenantId) {
            CandidateDto candidate = storage.get(candidateId);
            if (candidate == null || !tenantId.equals(candidate.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(candidate);
        }

        @Override
        public Flux<CandidateDto> search(String query, CandidateStatus status, int limit, int offset, String tenantId) {
            return Flux.fromIterable(storage.values())
                    .filter(candidate -> tenantId.equals(candidate.tenantId()))
                    .filter(candidate -> status == null || status == candidate.status())
                    .skip(offset)
                    .take(limit);
        }

        @Override
        public Mono<CandidateDto> updateStatus(UUID candidateId, CandidateStatus status, String reason, String tenantId) {
            CandidateDto existing = storage.get(candidateId);
            if (existing == null || !tenantId.equals(existing.tenantId())) {
                return Mono.empty();
            }

            CandidateDto updated = new CandidateDto(
                    existing.id(),
                    existing.tenantId(),
                    existing.personId(),
                    existing.candidateCode(),
                    existing.firstName(),
                    existing.lastName(),
                    existing.email(),
                    existing.jobPostingCode(),
                    status,
                    existing.createdAt(),
                    Instant.now());
            storage.put(candidateId, updated);
            return Mono.just(updated);
        }
    }
}
