package com.company.hrms.payroll.application;

import com.company.hrms.document.api.AttachDocumentCommand;
import com.company.hrms.document.api.DocumentExpiryQuery;
import com.company.hrms.document.api.DocumentListQuery;
import com.company.hrms.document.api.DocumentModuleApi;
import com.company.hrms.document.api.DocumentRecordView;
import com.company.hrms.document.api.ExpiryDate;
import com.company.hrms.document.domain.StorageReference;
import com.company.hrms.document.api.VerificationStatus;
import com.company.hrms.notification.api.CreateNotificationCommand;
import com.company.hrms.notification.api.NotificationModuleApi;
import com.company.hrms.notification.api.NotificationView;
import com.company.hrms.notification.api.NotificationChannel;
import com.company.hrms.notification.api.NotificationStatus;
import com.company.hrms.payroll.api.AttachPayrollEmployeeRecordCommand;
import com.company.hrms.payroll.api.DefinePayrollPeriodCommand;
import com.company.hrms.payroll.api.PayrollAmountComponent;
import com.company.hrms.payroll.api.PayrollApprovalDecision;
import com.company.hrms.payroll.api.ReviewPayrollRunCommand;
import com.company.hrms.payroll.api.StartPayrollRunCommand;
import com.company.hrms.payroll.api.SubmitPayrollRunCommand;
import com.company.hrms.payroll.domain.DeductionComponent;
import com.company.hrms.payroll.domain.EarningComponent;
import com.company.hrms.payroll.domain.PayrollEmployeeRecord;
import com.company.hrms.payroll.domain.PayrollPeriod;
import com.company.hrms.payroll.api.PayrollPeriodStatus;
import com.company.hrms.payroll.domain.PayrollRepository;
import com.company.hrms.payroll.domain.PayrollRun;
import com.company.hrms.payroll.api.PayrollRunStatus;
import com.company.hrms.payroll.domain.Payslip;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.workflow.api.AdvanceWorkflowCommand;
import com.company.hrms.workflow.api.StartWorkflowCommand;
import com.company.hrms.workflow.api.WorkflowInstanceView;
import com.company.hrms.workflow.api.WorkflowModuleApi;
import com.company.hrms.workflow.api.ApprovalStatus;
import com.company.hrms.workflow.api.WorkflowAction;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollApplicationServiceTest {

    private final InMemoryPayrollRepository repository = new InMemoryPayrollRepository();
    private final StubWorkflowModuleApi workflowModuleApi = new StubWorkflowModuleApi();
    private final StubDocumentModuleApi documentModuleApi = new StubDocumentModuleApi();
    private final StubNotificationModuleApi notificationModuleApi = new StubNotificationModuleApi();
    private final RecordingAuditEventPublisher auditPublisher = new RecordingAuditEventPublisher();
    private final RecordingOutboxPublisher outboxPublisher = new RecordingOutboxPublisher();

    private final PayrollApplicationService payrollApplicationService = new PayrollApplicationService(
            repository,
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new EnabledFeatureToggleService()),
            workflowModuleApi,
            documentModuleApi,
            notificationModuleApi,
            auditPublisher,
            outboxPublisher,
            new SimpleMeterRegistry());

    @Test
    void approvalLifecycleFinalizesAndGeneratesPayslipMetadata() {
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(payrollApplicationService.definePayrollPeriod(new DefinePayrollPeriodCommand(
                                "2026-03",
                                LocalDate.parse("2026-03-01"),
                                LocalDate.parse("2026-03-31"),
                                "March payroll"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID payrollPeriodId = repository.lastPayrollPeriodId;

        StepVerifier.create(payrollApplicationService.startPayrollRun(new StartPayrollRunCommand(
                                payrollPeriodId,
                                "payroll-admin",
                                "monthly run"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(run -> assertEquals(PayrollRunStatus.DRAFT, run.status()))
                .verifyComplete();

        UUID payrollRunId = repository.lastPayrollRunId;

        StepVerifier.create(payrollApplicationService.attachEmployeePayrollRecord(new AttachPayrollEmployeeRecordCommand(
                                payrollRunId,
                                employeeId,
                                List.of(
                                        new PayrollAmountComponent("BASIC", "Basic Salary", new BigDecimal("1000.00")),
                                        new PayrollAmountComponent("HRA", "House Rent", new BigDecimal("500.00"))),
                                List.of(
                                        new PayrollAmountComponent("TAX", "Tax", new BigDecimal("200.00"))),
                                "baseline payroll"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(payslip -> {
                    assertEquals(new BigDecimal("1500.00"), payslip.payrollEmployeeRecord().grossAmount());
                    assertEquals(new BigDecimal("1300.00"), payslip.payrollEmployeeRecord().netAmount());
                })
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.submitPayrollRun(new SubmitPayrollRunCommand(
                                payrollRunId,
                                "payroll-manager",
                                "submit for approval"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(run -> {
                    assertEquals(PayrollRunStatus.SUBMITTED, run.status());
                    assertNotNull(run.workflowInstanceId());
                })
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.reviewPayrollRun(new ReviewPayrollRunCommand(
                                payrollRunId,
                                PayrollApprovalDecision.APPROVE,
                                "finance-head",
                                "approved"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(run -> assertEquals(PayrollRunStatus.APPROVED, run.status()))
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.finalizePayrollRun(payrollRunId)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(run -> assertEquals(PayrollRunStatus.FINALIZED, run.status()))
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.payslips(payrollRunId)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(payslip -> {
                    assertNotNull(payslip.documentRecordId());
                    assertTrue(payslip.artifactObjectKey().contains(payrollRunId.toString()));
                })
                .verifyComplete();

        assertEquals(1, notificationModuleApi.requests.size());
        assertTrue(auditPublisher.actions.contains("PAYROLL_SUBMITTED"));
        assertTrue(auditPublisher.actions.contains("PAYROLL_APPROVED"));
        assertTrue(auditPublisher.actions.contains("PAYROLL_FINALIZED"));
        assertTrue(outboxPublisher.eventTypes.contains("PayrollFinalized"));
    }

    @Test
    void rejectedRunCannotBeFinalized() {
        repository.seedPayrollPeriod(new PayrollPeriod(
                UUID.randomUUID(),
                "default",
                "2026-04",
                LocalDate.parse("2026-04-01"),
                LocalDate.parse("2026-04-30"),
                com.company.hrms.payroll.domain.PayrollPeriodStatus.OPEN,
                "April",
                Instant.now(),
                Instant.now()));

        UUID periodId = repository.lastPayrollPeriodId;

        StepVerifier.create(payrollApplicationService.startPayrollRun(new StartPayrollRunCommand(periodId, "admin", null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        UUID runId = repository.lastPayrollRunId;
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(payrollApplicationService.attachEmployeePayrollRecord(new AttachPayrollEmployeeRecordCommand(
                                runId,
                                employeeId,
                                List.of(new PayrollAmountComponent("BASIC", "Basic", new BigDecimal("1000.00"))),
                                List.of(),
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.submitPayrollRun(new SubmitPayrollRunCommand(runId, "manager", "submit"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.reviewPayrollRun(new ReviewPayrollRunCommand(
                                runId,
                                PayrollApprovalDecision.REJECT,
                                "finance-head",
                                "reject"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(run -> assertEquals(PayrollRunStatus.REJECTED, run.status()))
                .verifyComplete();

        StepVerifier.create(payrollApplicationService.finalizePayrollRun(runId)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("PAYROLL_RUN_NOT_APPROVED", ex.getErrorCode());
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

    static class RecordingAuditEventPublisher implements AuditEventPublisher {
        private final List<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }

    static class RecordingOutboxPublisher implements OutboxPublisher {
        private final List<String> eventTypes = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(OutboxEvent event) {
            eventTypes.add(event.eventType());
            return Mono.empty();
        }
    }

    static class StubWorkflowModuleApi implements WorkflowModuleApi {
        private final Map<UUID, WorkflowInstanceView> instances = new ConcurrentHashMap<>();

        @Override
        public Mono<WorkflowInstanceView> startWorkflow(StartWorkflowCommand command) {
            WorkflowInstanceView view = new WorkflowInstanceView(
                    UUID.randomUUID(),
                    "default",
                    command.workflowKey(),
                    command.targetType(),
                    command.targetId(),
                    ApprovalStatus.SUBMITTED,
                    command.actor(),
                    null,
                    Instant.now(),
                    null,
                    Instant.now(),
                    Instant.now());
            instances.put(view.id(), view);
            return Mono.just(view);
        }

        @Override
        public Mono<WorkflowInstanceView> advanceWorkflow(AdvanceWorkflowCommand command) {
            WorkflowInstanceView existing = instances.get(command.workflowInstanceId());
            if (existing == null) {
                return Mono.error(new IllegalStateException("Workflow instance missing"));
            }

            ApprovalStatus status = command.action() == WorkflowAction.APPROVE
                    ? ApprovalStatus.APPROVED
                    : ApprovalStatus.REJECTED;
            WorkflowInstanceView updated = new WorkflowInstanceView(
                    existing.id(),
                    existing.tenantId(),
                    existing.workflowKey(),
                    existing.targetType(),
                    existing.targetId(),
                    status,
                    existing.requestedBy(),
                    command.actor(),
                    existing.submittedAt(),
                    Instant.now(),
                    existing.createdAt(),
                    Instant.now());
            instances.put(updated.id(), updated);
            return Mono.just(updated);
        }

        @Override
        public Mono<WorkflowInstanceView> getWorkflowInstance(UUID workflowInstanceId) {
            return Mono.justOrEmpty(instances.get(workflowInstanceId));
        }
    }

    static class StubDocumentModuleApi implements DocumentModuleApi {
        @Override
        public Mono<DocumentRecordView> attachDocument(AttachDocumentCommand command) {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();
            return Mono.just(new DocumentRecordView(
                    id,
                    "default",
                    command.documentType(),
                    command.entityType(),
                    command.entityId(),
                    command.fileName(),
                    new StorageReference("local-dev", "hrms-local", command.objectKey(), command.checksum(), command.contentType(), command.sizeBytes()),
                    command.expiryDate(),
                    VerificationStatus.PENDING,
                    false,
                    now,
                    command.createdBy(),
                    now,
                    command.createdBy()));
        }

        @Override
        public Flux<DocumentRecordView> listDocuments(DocumentListQuery query) {
            return Flux.empty();
        }

        @Override
        public Flux<DocumentRecordView> findExpiringDocuments(DocumentExpiryQuery query) {
            return Flux.empty();
        }

        @Override
        public Mono<DocumentRecordView> archiveDocument(UUID documentId) {
            return Mono.empty();
        }

        @Override
        public Mono<DocumentRecordView> getDocument(UUID documentId) {
            return Mono.empty();
        }
    }

    static class StubNotificationModuleApi implements NotificationModuleApi {
        private final List<CreateNotificationCommand> requests = new CopyOnWriteArrayList<>();

        @Override
        public Mono<NotificationView> createNotification(CreateNotificationCommand command) {
            requests.add(command);
            Instant now = Instant.now();
            return Mono.just(new NotificationView(
                    UUID.randomUUID(),
                    "default",
                    NotificationChannel.EMAIL,
                    command.recipient(),
                    command.subject(),
                    command.body(),
                    command.templateCode(),
                    command.referenceType(),
                    command.referenceId(),
                    NotificationStatus.QUEUED,
                    null,
                    null,
                    now,
                    now));
        }

        @Override
        public Flux<NotificationView> dispatchQueuedNotifications(int limit) {
            return Flux.empty();
        }

        @Override
        public Mono<NotificationView> getNotification(UUID notificationId) {
            return Mono.empty();
        }
    }

    static class InMemoryPayrollRepository implements PayrollRepository {
        private final Map<UUID, PayrollPeriod> payrollPeriods = new ConcurrentHashMap<>();
        private final Map<UUID, PayrollRun> payrollRuns = new ConcurrentHashMap<>();
        private final Map<UUID, PayrollEmployeeRecord> payrollEmployeeRecords = new ConcurrentHashMap<>();
        private final Map<UUID, Payslip> payslips = new ConcurrentHashMap<>();
        private final Map<UUID, EarningComponent> earningComponents = new ConcurrentHashMap<>();
        private final Map<UUID, DeductionComponent> deductionComponents = new ConcurrentHashMap<>();

        private volatile UUID lastPayrollPeriodId;
        private volatile UUID lastPayrollRunId;

        @Override
        public Mono<PayrollPeriod> savePayrollPeriod(PayrollPeriod payrollPeriod) {
            payrollPeriods.put(payrollPeriod.id(), payrollPeriod);
            lastPayrollPeriodId = payrollPeriod.id();
            return Mono.just(payrollPeriod);
        }

        @Override
        public Mono<PayrollPeriod> updatePayrollPeriod(PayrollPeriod payrollPeriod) {
            payrollPeriods.put(payrollPeriod.id(), payrollPeriod);
            return Mono.just(payrollPeriod);
        }

        @Override
        public Mono<PayrollPeriod> findPayrollPeriodById(String tenantId, UUID payrollPeriodId) {
            PayrollPeriod period = payrollPeriods.get(payrollPeriodId);
            if (period == null || !tenantId.equals(period.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(period);
        }

        @Override
        public Mono<PayrollRun> savePayrollRun(PayrollRun payrollRun) {
            payrollRuns.put(payrollRun.id(), payrollRun);
            lastPayrollRunId = payrollRun.id();
            return Mono.just(payrollRun);
        }

        @Override
        public Mono<PayrollRun> updatePayrollRun(PayrollRun payrollRun) {
            payrollRuns.put(payrollRun.id(), payrollRun);
            return Mono.just(payrollRun);
        }

        @Override
        public Mono<PayrollRun> findPayrollRunById(String tenantId, UUID payrollRunId) {
            PayrollRun run = payrollRuns.get(payrollRunId);
            if (run == null || !tenantId.equals(run.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(run);
        }

        @Override
        public Mono<PayrollEmployeeRecord> savePayrollEmployeeRecord(PayrollEmployeeRecord payrollEmployeeRecord) {
            payrollEmployeeRecords.put(payrollEmployeeRecord.id(), payrollEmployeeRecord);
            return Mono.just(payrollEmployeeRecord);
        }

        @Override
        public Flux<EarningComponent> saveEarningComponents(String tenantId, Flux<EarningComponent> components) {
            return components.doOnNext(component -> earningComponents.put(component.id(), component));
        }

        @Override
        public Flux<DeductionComponent> saveDeductionComponents(String tenantId, Flux<DeductionComponent> components) {
            return components.doOnNext(component -> deductionComponents.put(component.id(), component));
        }

        @Override
        public Mono<Payslip> savePayslip(Payslip payslip) {
            payslips.put(payslip.id(), payslip);
            return Mono.just(payslip);
        }

        @Override
        public Mono<Payslip> updatePayslip(Payslip payslip) {
            payslips.put(payslip.id(), payslip);
            return Mono.just(payslip);
        }

        @Override
        public Flux<Payslip> findPayslipsByPayrollRunId(String tenantId, UUID payrollRunId) {
            return Flux.fromIterable(payslips.values())
                    .filter(payslip -> tenantId.equals(payslip.tenantId()))
                    .filter(payslip -> payrollRunId.equals(payslip.payrollRunId()));
        }

        @Override
        public Flux<EarningComponent> findEarningComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId) {
            return Flux.fromIterable(earningComponents.values())
                    .filter(component -> payrollEmployeeRecordId.equals(component.payrollEmployeeRecordId()));
        }

        @Override
        public Flux<DeductionComponent> findDeductionComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId) {
            return Flux.fromIterable(deductionComponents.values())
                    .filter(component -> payrollEmployeeRecordId.equals(component.payrollEmployeeRecordId()));
        }

        @Override
        public Mono<PayrollEmployeeRecord> findPayrollEmployeeRecordById(String tenantId, UUID payrollEmployeeRecordId) {
            PayrollEmployeeRecord record = payrollEmployeeRecords.get(payrollEmployeeRecordId);
            if (record == null || !tenantId.equals(record.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(record);
        }

        @Override
        public Mono<Boolean> existsPayrollRunRecordForEmployee(String tenantId, UUID payrollRunId, UUID employeeId) {
            boolean exists = payrollEmployeeRecords.values().stream()
                    .anyMatch(record -> tenantId.equals(record.tenantId())
                            && payrollRunId.equals(record.payrollRunId())
                            && employeeId.equals(record.employeeId()));
            return Mono.just(exists);
        }

        void seedPayrollPeriod(PayrollPeriod payrollPeriod) {
            payrollPeriods.put(payrollPeriod.id(), payrollPeriod);
            lastPayrollPeriodId = payrollPeriod.id();
        }
    }
}
