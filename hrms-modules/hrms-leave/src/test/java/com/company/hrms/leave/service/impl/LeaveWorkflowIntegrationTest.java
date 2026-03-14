package com.company.hrms.leave.service.impl;

import com.company.hrms.leave.model.*;
import com.company.hrms.leave.repository.*;
import com.company.hrms.leave.service.*;

import com.company.hrms.leave.model.ApplyLeaveCommandDto;
import com.company.hrms.leave.model.DefineLeaveTypeCommandDto;
import com.company.hrms.leave.model.InitializeLeaveBalanceCommandDto;
import com.company.hrms.leave.model.ReviewLeaveCommandDto;
import com.company.hrms.leave.model.LeaveBalanceDto;
import com.company.hrms.leave.repository.LeaveRepository;
import com.company.hrms.leave.model.LeaveRequestDto;
import com.company.hrms.leave.model.LeaveStatus;
import com.company.hrms.leave.model.LeaveTypeDto;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.workflow.service.impl.WorkflowApplicationService;
import com.company.hrms.workflow.model.WorkflowDefinitionDto;
import com.company.hrms.workflow.model.WorkflowInstanceDto;
import com.company.hrms.workflow.repository.WorkflowRepository;
import com.company.hrms.workflow.model.WorkflowStepDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LeaveWorkflowIntegrationTest {

    private final InMemoryWorkflowRepository workflowRepository = new InMemoryWorkflowRepository();
    private final WorkflowApplicationService workflowApplicationService = new WorkflowApplicationService(
            workflowRepository,
            new DefaultTenantContextAccessor(),
            new SimpleMeterRegistry());

    private final InMemoryLeaveRepository leaveRepository = new InMemoryLeaveRepository();
    private final RecordingOutboxPublisher outboxPublisher = new RecordingOutboxPublisher();

    private final LeaveApplicationService leaveApplicationService = new LeaveApplicationService(
            leaveRepository,
            workflowApplicationService,
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new EnabledFeatureToggleService()),
            outboxPublisher);

    @Test
    void applyAndApproveLeaveThroughWorkflow() {
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(leaveApplicationService.defineLeaveType(new DefineLeaveTypeCommandDto("ANNUAL", "Annual Leave", true, 30))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID leaveTypeId = leaveRepository.lastLeaveTypeId;

        StepVerifier.create(leaveApplicationService.initializeLeaveBalance(new InitializeLeaveBalanceCommandDto(
                                employeeId,
                                leaveTypeId,
                                2026,
                                10))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(balance -> assertEquals(10, balance.remainingDays()))
                .verifyComplete();

        StepVerifier.create(leaveApplicationService.applyLeave(new ApplyLeaveCommandDto(
                                employeeId,
                                leaveTypeId,
                                LocalDate.parse("2026-03-10"),
                                LocalDate.parse("2026-03-12"),
                                "Vacation",
                                "emp-1"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(request -> {
                    assertEquals(LeaveStatus.SUBMITTED, request.status());
                    assertEquals(3, request.requestedDays());
                })
                .verifyComplete();

        UUID leaveRequestId = leaveRepository.lastLeaveRequestId;

        StepVerifier.create(leaveApplicationService.reviewLeave(new ReviewLeaveCommandDto(
                                leaveRequestId,
                                LeaveStatus.APPROVED,
                                "manager-1",
                                "approved"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(request -> assertEquals(LeaveStatus.APPROVED, request.status()))
                .verifyComplete();

        StepVerifier.create(leaveApplicationService.balances(employeeId, 2026)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(balance -> {
                    assertEquals(3, balance.usedDays());
                    assertEquals(7, balance.remainingDays());
                })
                .verifyComplete();

        WorkflowInstanceDto workflowInstance = workflowRepository.instances.get(leaveRepository.lastWorkflowInstanceId);
        assertEquals("APPROVED", workflowInstance.status().name());
        org.junit.jupiter.api.Assertions.assertTrue(outboxPublisher.eventTypes.contains("LeaveRequested"));
    }

    @Test
    void rejectLeaveDoesNotConsumeBalance() {
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(leaveApplicationService.defineLeaveType(new DefineLeaveTypeCommandDto("SICK", "Sick Leave", true, 12))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID leaveTypeId = leaveRepository.lastLeaveTypeId;

        StepVerifier.create(leaveApplicationService.initializeLeaveBalance(new InitializeLeaveBalanceCommandDto(
                                employeeId,
                                leaveTypeId,
                                2026,
                                5))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(leaveApplicationService.applyLeave(new ApplyLeaveCommandDto(
                                employeeId,
                                leaveTypeId,
                                LocalDate.parse("2026-04-01"),
                                LocalDate.parse("2026-04-02"),
                                "Sick",
                                "emp-2"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID leaveRequestId = leaveRepository.lastLeaveRequestId;

        StepVerifier.create(leaveApplicationService.reviewLeave(new ReviewLeaveCommandDto(
                                leaveRequestId,
                                LeaveStatus.REJECTED,
                                "manager-2",
                                "rejected"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(request -> assertEquals(LeaveStatus.REJECTED, request.status()))
                .verifyComplete();

        StepVerifier.create(leaveApplicationService.balances(employeeId, 2026)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(balance -> {
                    assertEquals(0, balance.usedDays());
                    assertEquals(5, balance.remainingDays());
                })
                .verifyComplete();
    }

    @Test
    void applyFailsWhenBalanceInsufficient() {
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(leaveApplicationService.defineLeaveType(new DefineLeaveTypeCommandDto("ANNUAL", "Annual Leave", true, 30))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID leaveTypeId = leaveRepository.lastLeaveTypeId;

        StepVerifier.create(leaveApplicationService.initializeLeaveBalance(new InitializeLeaveBalanceCommandDto(
                                employeeId,
                                leaveTypeId,
                                2026,
                                1))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(leaveApplicationService.applyLeave(new ApplyLeaveCommandDto(
                                employeeId,
                                leaveTypeId,
                                LocalDate.parse("2026-06-01"),
                                LocalDate.parse("2026-06-03"),
                                "Trip",
                                "emp-1"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("INSUFFICIENT_LEAVE_BALANCE", ex.getErrorCode());
                })
                .verify();
    }

    static class EnabledFeatureToggleService implements FeatureToggleService {
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

    static class RecordingOutboxPublisher implements OutboxPublisher {
        private final CopyOnWriteArrayList<String> eventTypes = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(OutboxEvent event) {
            eventTypes.add(event.eventType());
            return Mono.empty();
        }
    }

    static class InMemoryWorkflowRepository implements WorkflowRepository {
        private final Map<UUID, WorkflowInstanceDto> instances = new ConcurrentHashMap<>();
        private final Map<UUID, Integer> stepCount = new ConcurrentHashMap<>();
        private final WorkflowDefinitionDto definition = new WorkflowDefinitionDto(
                UUID.randomUUID(),
                "default",
                "leave.approval",
                "Leave Approval",
                true,
                Instant.now(),
                Instant.now());

        @Override
        public Mono<WorkflowDefinitionDto> findActiveDefinitionByKey(String tenantId, String workflowKey) {
            if (definition.tenantId().equals(tenantId) && definition.workflowKey().equals(workflowKey)) {
                return Mono.just(definition);
            }
            return Mono.empty();
        }

        @Override
        public Mono<WorkflowInstanceDto> saveInstance(WorkflowInstanceDto instance) {
            instances.put(instance.id(), instance);
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstanceDto> updateInstance(WorkflowInstanceDto instance) {
            instances.put(instance.id(), instance);
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstanceDto> findInstanceById(String tenantId, UUID workflowInstanceId) {
            WorkflowInstanceDto instance = instances.get(workflowInstanceId);
            if (instance == null || !tenantId.equals(instance.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(instance);
        }

        @Override
        public Mono<Integer> countSteps(String tenantId, UUID workflowInstanceId) {
            return Mono.just(stepCount.getOrDefault(workflowInstanceId, 0));
        }

        @Override
        public Mono<WorkflowStepDto> saveStep(String tenantId, WorkflowStepDto step) {
            stepCount.put(step.workflowInstanceId(), stepCount.getOrDefault(step.workflowInstanceId(), 0) + 1);
            return Mono.just(step);
        }
    }

    static class InMemoryLeaveRepository implements LeaveRepository {
        private final Map<UUID, LeaveTypeDto> leaveTypes = new ConcurrentHashMap<>();
        private final Map<UUID, LeaveBalanceDto> leaveBalances = new ConcurrentHashMap<>();
        private final Map<UUID, LeaveRequestDto> leaveRequests = new ConcurrentHashMap<>();

        private volatile UUID lastLeaveTypeId;
        private volatile UUID lastLeaveRequestId;
        private volatile UUID lastWorkflowInstanceId;

        @Override
        public Mono<LeaveTypeDto> saveLeaveType(LeaveTypeDto leaveType) {
            leaveTypes.put(leaveType.id(), leaveType);
            lastLeaveTypeId = leaveType.id();
            return Mono.just(leaveType);
        }

        @Override
        public Mono<LeaveTypeDto> findLeaveTypeById(String tenantId, UUID leaveTypeId) {
            LeaveTypeDto leaveType = leaveTypes.get(leaveTypeId);
            if (leaveType == null || !tenantId.equals(leaveType.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(leaveType);
        }

        @Override
        public Mono<LeaveBalanceDto> saveLeaveBalance(LeaveBalanceDto leaveBalance) {
            leaveBalances.put(leaveBalance.id(), leaveBalance);
            return Mono.just(leaveBalance);
        }

        @Override
        public Mono<LeaveBalanceDto> updateLeaveBalance(LeaveBalanceDto leaveBalance) {
            leaveBalances.put(leaveBalance.id(), leaveBalance);
            return Mono.just(leaveBalance);
        }

        @Override
        public Mono<LeaveBalanceDto> findLeaveBalance(String tenantId, UUID employeeId, UUID leaveTypeId, int leaveYear) {
            return Flux.fromIterable(leaveBalances.values())
                    .filter(balance -> tenantId.equals(balance.tenantId()))
                    .filter(balance -> employeeId.equals(balance.employeeId()))
                    .filter(balance -> leaveTypeId.equals(balance.leaveTypeId()))
                    .filter(balance -> leaveYear == balance.leaveYear())
                    .next();
        }

        @Override
        public Flux<LeaveBalanceDto> findLeaveBalances(String tenantId, UUID employeeId, int leaveYear) {
            return Flux.fromIterable(leaveBalances.values())
                    .filter(balance -> tenantId.equals(balance.tenantId()))
                    .filter(balance -> employeeId.equals(balance.employeeId()))
                    .filter(balance -> leaveYear == balance.leaveYear());
        }

        @Override
        public Mono<LeaveRequestDto> saveLeaveRequest(LeaveRequestDto leaveRequest) {
            leaveRequests.put(leaveRequest.id(), leaveRequest);
            lastLeaveRequestId = leaveRequest.id();
            return Mono.just(leaveRequest);
        }

        @Override
        public Mono<LeaveRequestDto> updateLeaveRequest(LeaveRequestDto leaveRequest) {
            leaveRequests.put(leaveRequest.id(), leaveRequest);
            lastLeaveRequestId = leaveRequest.id();
            lastWorkflowInstanceId = leaveRequest.workflowInstanceId();
            return Mono.just(leaveRequest);
        }

        @Override
        public Mono<LeaveRequestDto> findLeaveRequestById(String tenantId, UUID leaveRequestId) {
            LeaveRequestDto leaveRequest = leaveRequests.get(leaveRequestId);
            if (leaveRequest == null || !tenantId.equals(leaveRequest.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(leaveRequest);
        }

        @Override
        public Flux<LeaveRequestDto> findLeaveRequests(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.fromIterable(leaveRequests.values())
                    .filter(request -> tenantId.equals(request.tenantId()))
                    .filter(request -> employeeId.equals(request.employeeId()))
                    .filter(request -> !request.fromDate().isBefore(fromDate))
                    .filter(request -> !request.toDate().isAfter(toDate));
        }
    }
}
