package com.company.hrms.payroll.service.impl;

import com.company.hrms.payroll.model.*;
import com.company.hrms.payroll.repository.*;
import com.company.hrms.payroll.service.*;

import com.company.hrms.document.model.AttachDocumentCommandDto;
import com.company.hrms.document.model.DocumentType;
import com.company.hrms.document.service.DocumentModuleApi;
import com.company.hrms.document.model.ExpiryDateDto;
import com.company.hrms.document.model.VerificationStatus;
import com.company.hrms.notification.model.CreateNotificationCommandDto;
import com.company.hrms.notification.model.NotificationChannel;
import com.company.hrms.notification.service.NotificationModuleApi;
import com.company.hrms.payroll.model.AttachPayrollEmployeeRecordCommandDto;
import com.company.hrms.payroll.model.DefinePayrollPeriodCommandDto;
import com.company.hrms.payroll.model.PayrollAmountComponentDto;
import com.company.hrms.payroll.model.PayrollApprovalDecision;
import com.company.hrms.payroll.model.PayrollEmployeeRecordViewDto;
import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.payroll.model.PayrollPeriodViewDto;
import com.company.hrms.payroll.model.PayrollRunViewDto;
import com.company.hrms.payroll.model.PayslipViewDto;
import com.company.hrms.payroll.model.ReviewPayrollRunCommandDto;
import com.company.hrms.payroll.model.StartPayrollRunCommandDto;
import com.company.hrms.payroll.model.SubmitPayrollRunCommandDto;
import com.company.hrms.payroll.model.DeductionComponentDto;
import com.company.hrms.payroll.model.EarningComponentDto;
import com.company.hrms.payroll.model.PayrollEmployeeRecordDto;
import com.company.hrms.payroll.model.PayrollPeriodDto;
import com.company.hrms.payroll.model.PayrollPeriodStatus;
import com.company.hrms.payroll.repository.PayrollRepository;
import com.company.hrms.payroll.model.PayrollRunDto;
import com.company.hrms.payroll.model.PayrollRunStatus;
import com.company.hrms.payroll.model.PayslipDto;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.workflow.model.AdvanceWorkflowCommandDto;
import com.company.hrms.workflow.model.StartWorkflowCommandDto;
import com.company.hrms.workflow.model.WorkflowAction;
import com.company.hrms.workflow.service.WorkflowModuleApi;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Primary
public class PayrollApplicationService implements PayrollModuleApi {

    private static final String PAYROLL_WORKFLOW_KEY = "payroll.approval";

    private final PayrollRepository payrollRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final WorkflowModuleApi workflowModuleApi;
    private final DocumentModuleApi documentModuleApi;
    private final NotificationModuleApi notificationModuleApi;
    private final AuditEventPublisher auditEventPublisher;
    private final OutboxPublisher outboxPublisher;
    private final MeterRegistry meterRegistry;

    public PayrollApplicationService(
            PayrollRepository payrollRepository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            WorkflowModuleApi workflowModuleApi,
            DocumentModuleApi documentModuleApi,
            NotificationModuleApi notificationModuleApi,
            AuditEventPublisher auditEventPublisher,
            OutboxPublisher outboxPublisher,
            MeterRegistry meterRegistry
    ) {
        this.payrollRepository = payrollRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.workflowModuleApi = workflowModuleApi;
        this.documentModuleApi = documentModuleApi;
        this.notificationModuleApi = notificationModuleApi;
        this.auditEventPublisher = auditEventPublisher;
        this.outboxPublisher = outboxPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<PayrollPeriodViewDto> definePayrollPeriod(DefinePayrollPeriodCommandDto command) {
        validatePayrollPeriod(command);

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    PayrollPeriodDto payrollPeriod = new PayrollPeriodDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.periodCode().trim().toUpperCase(),
                            command.startDate(),
                            command.endDate(),
                            PayrollPeriodStatus.OPEN,
                            command.description(),
                            now,
                            now);
                    return payrollRepository.savePayrollPeriod(payrollPeriod).map(this::toPayrollPeriodView);
                });
    }

    @Override
    public Mono<PayrollRunViewDto> startPayrollRun(StartPayrollRunCommandDto command) {
        validateStartRun(command);

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollPeriodById(tenantId, command.payrollPeriodId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_PERIOD_NOT_FOUND", "Payroll period not found")))
                        .flatMap(period -> {
                            if (period.status() != PayrollPeriodStatus.OPEN) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_PERIOD_CLOSED", "Payroll period is closed"));
                            }

                            Instant now = Instant.now();
                            PayrollRunDto payrollRun = new PayrollRunDto(
                                    UUID.randomUUID(),
                                    tenantId,
                                    command.payrollPeriodId(),
                                    PayrollRunStatus.DRAFT,
                                    null,
                                    command.initiatedBy().trim(),
                                    null,
                                    null,
                                    command.notes(),
                                    null,
                                    null,
                                    null,
                                    now,
                                    now);
                            return payrollRepository.savePayrollRun(payrollRun)
                                    .doOnNext(saved -> recordLifecycleMetric("started", saved.status()))
                                    .flatMap(saved -> auditEventPublisher.publish(AuditEvent.of(
                                                    command.initiatedBy().trim(),
                                                    tenantId,
                                                    "PAYROLL_RUN_STARTED",
                                                    "PAYROLL_RUN",
                                                    saved.id().toString(),
                                                    Map.of("periodId", saved.payrollPeriodId().toString())))
                                            .thenReturn(saved))
                                    .map(this::toPayrollRunView);
                        }));
    }

    @Override
    public Mono<PayslipViewDto> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommandDto command) {
        validateAttachRecord(command);

        List<PayrollAmountComponentDto> earnings = normalize(command.earnings());
        List<PayrollAmountComponentDto> deductions = normalize(command.deductions());

        BigDecimal grossAmount = sumAmounts(earnings);
        BigDecimal totalDeductionAmount = sumAmounts(deductions);
        BigDecimal netAmount = grossAmount.subtract(totalDeductionAmount);

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollRunById(tenantId, command.payrollRunId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RUN_NOT_FOUND", "Payroll run not found")))
                        .flatMap(run -> {
                            if (run.status() != PayrollRunStatus.DRAFT && run.status() != PayrollRunStatus.REJECTED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_EDITABLE", "Payroll run is not editable"));
                            }
                            return payrollRepository.existsPayrollRunRecordForEmployee(tenantId, run.id(), command.employeeId())
                                    .flatMap(exists -> exists
                                            ? Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RECORD_EXISTS", "EmployeeDto payroll record already exists"))
                                            : createPayrollRecordWithComponents(
                                            tenantId,
                                            run.id(),
                                            command,
                                            earnings,
                                            deductions,
                                            grossAmount,
                                            totalDeductionAmount,
                                            netAmount));
                        }));
    }

    @Override
    public Mono<PayrollRunViewDto> submitPayrollRun(SubmitPayrollRunCommandDto command) {
        validateSubmit(command);

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollRunById(tenantId, command.payrollRunId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RUN_NOT_FOUND", "Payroll run not found")))
                        .flatMap(run -> {
                            if (run.status() != PayrollRunStatus.DRAFT && run.status() != PayrollRunStatus.REJECTED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_SUBMITTABLE", "Payroll run is not submittable"));
                            }

                            String actor = command.actor().trim();
                            return workflowModuleApi.startWorkflow(new StartWorkflowCommandDto(
                                            PAYROLL_WORKFLOW_KEY,
                                            "PAYROLL_RUN",
                                            run.id().toString(),
                                            actor,
                                            command.comments()))
                                    .flatMap(workflow -> payrollRepository.updatePayrollRun(run.submit(workflow.id(), actor, Instant.now())))
                                    .doOnNext(saved -> recordLifecycleMetric("submitted", saved.status()))
                                    .flatMap(saved -> auditEventPublisher.publish(AuditEvent.of(
                                                    actor,
                                                    tenantId,
                                                    "PAYROLL_SUBMITTED",
                                                    "PAYROLL_RUN",
                                                    saved.id().toString(),
                                                    Map.of("workflowInstanceId", saved.workflowInstanceId().toString())))
                                            .thenReturn(saved))
                                    .map(this::toPayrollRunView);
                        }));
    }

    @Override
    public Mono<PayrollRunViewDto> reviewPayrollRun(ReviewPayrollRunCommandDto command) {
        validateReview(command);

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollRunById(tenantId, command.payrollRunId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RUN_NOT_FOUND", "Payroll run not found")))
                        .flatMap(run -> {
                            if (run.status() != PayrollRunStatus.SUBMITTED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_IN_REVIEW", "Payroll run is not submitted"));
                            }
                            if (run.workflowInstanceId() == null) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_INSTANCE_REQUIRED", "Workflow instance is required"));
                            }

                            String actor = command.actor().trim();
                            WorkflowAction workflowAction = command.decision() == PayrollApprovalDecision.APPROVE
                                    ? WorkflowAction.APPROVE
                                    : WorkflowAction.REJECT;

                            return workflowModuleApi.advanceWorkflow(new AdvanceWorkflowCommandDto(
                                            run.workflowInstanceId(),
                                            workflowAction,
                                            actor,
                                            command.comments()))
                                    .then(Mono.defer(() -> {
                                        Instant now = Instant.now();
                                        PayrollRunDto updated = command.decision() == PayrollApprovalDecision.APPROVE
                                                ? run.approve(actor, now)
                                                : run.reject(actor, now);
                                        return payrollRepository.updatePayrollRun(updated);
                                    }))
                                    .doOnNext(saved -> recordLifecycleMetric(
                                            command.decision() == PayrollApprovalDecision.APPROVE ? "approved" : "rejected",
                                            saved.status()))
                                    .flatMap(saved -> auditEventPublisher.publish(AuditEvent.of(
                                                    actor,
                                                    tenantId,
                                                    command.decision() == PayrollApprovalDecision.APPROVE ? "PAYROLL_APPROVED" : "PAYROLL_REJECTED",
                                                    "PAYROLL_RUN",
                                                    saved.id().toString(),
                                                    Map.of("workflowInstanceId", saved.workflowInstanceId().toString())))
                                            .thenReturn(saved))
                                    .map(this::toPayrollRunView);
                        }));
    }

    @Override
    public Mono<PayrollRunViewDto> lockPayrollRun(UUID payrollRunId) {
        return submitPayrollRun(new SubmitPayrollRunCommandDto(
                payrollRunId,
                "system",
                "Submitted via lock alias"));
    }

    @Override
    public Mono<PayrollRunViewDto> finalizePayrollRun(UUID payrollRunId) {
        if (payrollRunId == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required"));
        }

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollRunById(tenantId, payrollRunId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RUN_NOT_FOUND", "Payroll run not found")))
                        .flatMap(run -> {
                            if (run.status() != PayrollRunStatus.APPROVED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_APPROVED", "Payroll run must be approved before finalization"));
                            }

                            Instant now = Instant.now();
                            PayrollRunDto finalized = run.finalizeRun(now);
                            return payrollRepository.updatePayrollRun(finalized)
                                    .doOnNext(saved -> recordLifecycleMetric("finalized", saved.status()))
                                    .flatMap(savedRun -> payrollRepository.findPayrollPeriodById(tenantId, savedRun.payrollPeriodId())
                                            .flatMap(period -> payrollRepository.updatePayrollPeriod(period.close(now)))
                                            .thenReturn(savedRun))
                                    .flatMap(savedRun -> finalizePayslipsWithMetadataAndNotifications(tenantId, savedRun)
                                            .flatMap(count -> auditEventPublisher.publish(AuditEvent.of(
                                                            resolveActor(savedRun.reviewedBy()),
                                                            tenantId,
                                                            "PAYROLL_FINALIZED",
                                                            "PAYROLL_RUN",
                                                            savedRun.id().toString(),
                                                            Map.of("payslipCount", Long.toString(count))))
                                                    .then(outboxPublisher.publish(new OutboxEvent(
                                                            tenantId,
                                                            "PAYROLL_RUN",
                                                            savedRun.id().toString(),
                                                            "PayrollFinalized",
                                                            payrollFinalizedPayload(savedRun, count),
                                                            now)))
                                                    .thenReturn(savedRun)))
                                    .map(this::toPayrollRunView);
                        }));
    }

    @Override
    public Mono<PayrollRunViewDto> getPayrollRun(UUID payrollRunId) {
        if (payrollRunId == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required"));
        }
        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMap(tenantId -> payrollRepository.findPayrollRunById(tenantId, payrollRunId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RUN_NOT_FOUND", "Payroll run not found")))
                        .map(this::toPayrollRunView));
    }

    @Override
    public Flux<PayslipViewDto> payslips(UUID payrollRunId) {
        if (payrollRunId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required"));
        }

        return enablementGuard.requireModuleEnabled("payroll")
                .then(requireTenant())
                .flatMapMany(tenantId -> payrollRepository.findPayslipsByPayrollRunId(tenantId, payrollRunId)
                        .concatMap(payslip -> toPayslipView(tenantId, payslip)));
    }

    private Mono<Long> finalizePayslipsWithMetadataAndNotifications(String tenantId, PayrollRunDto payrollRun) {
        return payrollRepository.findPayslipsByPayrollRunId(tenantId, payrollRun.id())
                .concatMap(payslip -> ensurePayslipMetadata(tenantId, payrollRun, payslip)
                        .flatMap(enriched -> sendPayrollCompletionNotification(tenantId, payrollRun, enriched)
                                .thenReturn(enriched)))
                .count();
    }

    private Mono<PayslipDto> ensurePayslipMetadata(String tenantId, PayrollRunDto payrollRun, PayslipDto payslip) {
        if (payslip.documentRecordId() != null && StringUtils.hasText(payslip.artifactObjectKey())) {
            return Mono.just(payslip);
        }

        String objectKey = "payroll/%s/payslip-%s.txt".formatted(payrollRun.id(), payslip.employeeId());
        String fileName = "payslip-%s.txt".formatted(payslip.employeeId());

        return documentModuleApi.attachDocument(new AttachDocumentCommandDto(
                        DocumentType.PAYROLL_DOCUMENT,
                        "PAYROLL_RUN",
                        payrollRun.id().toString(),
                        fileName,
                        "text/plain",
                        0,
                        objectKey,
                        null,
                        ExpiryDateDto.empty(),
                        VerificationStatus.PENDING,
                        resolveActor(payrollRun.reviewedBy())))
                .flatMap(document -> payrollRepository.updatePayslip(
                        payslip.withArtifact(document.id(), objectKey, "text/plain", Instant.now())))
                .onErrorResume(error -> auditEventPublisher.publish(AuditEvent.of(
                                resolveActor(payrollRun.reviewedBy()),
                                tenantId,
                                "PAYSLIP_METADATA_FAILED",
                                "PAYSLIP",
                                payslip.id().toString(),
                                Map.of("error", error.getClass().getSimpleName(), "message", safeMessage(error))))
                        .thenReturn(payslip));
    }

    private Mono<Void> sendPayrollCompletionNotification(String tenantId, PayrollRunDto payrollRun, PayslipDto payslip) {
        String recipient = "employee-" + payslip.employeeId() + "@hrms.local";
        String objectKey = StringUtils.hasText(payslip.artifactObjectKey()) ? payslip.artifactObjectKey() : "pending";

        return notificationModuleApi.createNotification(new CreateNotificationCommandDto(
                        NotificationChannel.EMAIL,
                        recipient,
                        "Payroll finalized",
                        "Your payroll is finalized. PayslipDto reference: " + objectKey,
                        null,
                        Map.of("payrollRunId", payrollRun.id().toString(), "payslipId", payslip.id().toString()),
                        "PAYSLIP",
                        payslip.id().toString()))
                .then()
                .onErrorResume(error -> auditEventPublisher.publish(AuditEvent.of(
                                resolveActor(payrollRun.reviewedBy()),
                                tenantId,
                                "PAYROLL_NOTIFICATION_FAILED",
                                "PAYSLIP",
                                payslip.id().toString(),
                                Map.of("error", error.getClass().getSimpleName(), "message", safeMessage(error))))
                        .then());
    }

    private Mono<PayslipViewDto> createPayrollRecordWithComponents(
            String tenantId,
            UUID payrollRunId,
            AttachPayrollEmployeeRecordCommandDto command,
            List<PayrollAmountComponentDto> earnings,
            List<PayrollAmountComponentDto> deductions,
            BigDecimal grossAmount,
            BigDecimal totalDeductionAmount,
            BigDecimal netAmount
    ) {
        Instant now = Instant.now();
        PayrollEmployeeRecordDto payrollEmployeeRecord = new PayrollEmployeeRecordDto(
                UUID.randomUUID(),
                tenantId,
                payrollRunId,
                command.employeeId(),
                grossAmount,
                totalDeductionAmount,
                netAmount,
                command.remarks(),
                now,
                now);

        return payrollRepository.savePayrollEmployeeRecord(payrollEmployeeRecord)
                .flatMap(savedRecord -> {
                    Flux<EarningComponentDto> earningFlux = Flux.fromIterable(earnings)
                            .map(component -> new EarningComponentDto(
                                    UUID.randomUUID(),
                                    savedRecord.id(),
                                    component.code().trim().toUpperCase(),
                                    component.name().trim(),
                                    component.amount(),
                                    now));

                    Flux<DeductionComponentDto> deductionFlux = Flux.fromIterable(deductions)
                            .map(component -> new DeductionComponentDto(
                                    UUID.randomUUID(),
                                    savedRecord.id(),
                                    component.code().trim().toUpperCase(),
                                    component.name().trim(),
                                    component.amount(),
                                    now));

                    PayslipDto payslip = new PayslipDto(
                            UUID.randomUUID(),
                            tenantId,
                            payrollRunId,
                            savedRecord.id(),
                            savedRecord.employeeId(),
                            null,
                            null,
                            null,
                            savedRecord.grossAmount(),
                            savedRecord.totalDeductionAmount(),
                            savedRecord.netAmount(),
                            now,
                            now,
                            now);

                    return payrollRepository.saveEarningComponents(tenantId, earningFlux).then()
                            .then(payrollRepository.saveDeductionComponents(tenantId, deductionFlux).then())
                            .then(payrollRepository.savePayslip(payslip))
                            .flatMap(savedPayslip -> toPayslipView(tenantId, savedPayslip));
                });
    }

    private Mono<PayslipViewDto> toPayslipView(String tenantId, PayslipDto payslip) {
        return payrollRepository.findPayrollEmployeeRecordById(tenantId, payslip.payrollEmployeeRecordId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PAYROLL_RECORD_NOT_FOUND", "Payroll employee record not found")))
                .flatMap(record -> Mono.zip(
                                payrollRepository.findEarningComponentsByPayrollEmployeeRecordId(tenantId, record.id())
                                        .map(component -> new PayrollAmountComponentDto(component.code(), component.name(), component.amount()))
                                        .collectList(),
                                payrollRepository.findDeductionComponentsByPayrollEmployeeRecordId(tenantId, record.id())
                                        .map(component -> new PayrollAmountComponentDto(component.code(), component.name(), component.amount()))
                                        .collectList())
                        .map(tuple -> new PayslipViewDto(
                                payslip.id(),
                                payslip.tenantId(),
                                toPayrollEmployeeRecordView(record),
                                payslip.documentRecordId(),
                                payslip.artifactObjectKey(),
                                payslip.artifactContentType(),
                                tuple.getT1(),
                                tuple.getT2(),
                                payslip.generatedAt(),
                                payslip.createdAt(),
                                payslip.updatedAt())));
    }

    private PayrollPeriodViewDto toPayrollPeriodView(PayrollPeriodDto payrollPeriod) {
        return new PayrollPeriodViewDto(
                payrollPeriod.id(),
                payrollPeriod.tenantId(),
                payrollPeriod.periodCode(),
                payrollPeriod.startDate(),
                payrollPeriod.endDate(),
                toApiPayrollPeriodStatus(payrollPeriod.status()),
                payrollPeriod.description(),
                payrollPeriod.createdAt(),
                payrollPeriod.updatedAt());
    }

    private PayrollRunViewDto toPayrollRunView(PayrollRunDto payrollRun) {
        return new PayrollRunViewDto(
                payrollRun.id(),
                payrollRun.tenantId(),
                payrollRun.payrollPeriodId(),
                toApiPayrollRunStatus(payrollRun.status()),
                payrollRun.workflowInstanceId(),
                payrollRun.initiatedBy(),
                payrollRun.submittedBy(),
                payrollRun.reviewedBy(),
                payrollRun.notes(),
                payrollRun.submittedAt(),
                payrollRun.reviewedAt(),
                payrollRun.finalizedAt(),
                payrollRun.createdAt(),
                payrollRun.updatedAt());
    }

    private PayrollEmployeeRecordViewDto toPayrollEmployeeRecordView(PayrollEmployeeRecordDto payrollEmployeeRecord) {
        return new PayrollEmployeeRecordViewDto(
                payrollEmployeeRecord.id(),
                payrollEmployeeRecord.tenantId(),
                payrollEmployeeRecord.payrollRunId(),
                payrollEmployeeRecord.employeeId(),
                payrollEmployeeRecord.grossAmount(),
                payrollEmployeeRecord.totalDeductionAmount(),
                payrollEmployeeRecord.netAmount(),
                payrollEmployeeRecord.remarks(),
                payrollEmployeeRecord.createdAt(),
                payrollEmployeeRecord.updatedAt());
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private BigDecimal sumAmounts(List<PayrollAmountComponentDto> components) {
        return components.stream()
                .map(PayrollAmountComponentDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PayrollAmountComponentDto> normalize(List<PayrollAmountComponentDto> components) {
        return components == null ? List.of() : components;
    }

    private void validatePayrollPeriod(DefinePayrollPeriodCommandDto command) {
        if (!StringUtils.hasText(command.periodCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERIOD_CODE_REQUIRED", "Payroll period code is required");
        }
        if (command.startDate() == null || command.endDate() == null || command.endDate().isBefore(command.startDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_PAYROLL_PERIOD_DATES", "Invalid payroll period dates");
        }
    }

    private void validateStartRun(StartPayrollRunCommandDto command) {
        if (command.payrollPeriodId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_PERIOD_REQUIRED", "Payroll period id is required");
        }
        if (!StringUtils.hasText(command.initiatedBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INITIATED_BY_REQUIRED", "Initiated by is required");
        }
    }

    private void validateSubmit(SubmitPayrollRunCommandDto command) {
        if (command.payrollRunId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required");
        }
        if (!StringUtils.hasText(command.actor())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTOR_REQUIRED", "Actor is required");
        }
    }

    private void validateReview(ReviewPayrollRunCommandDto command) {
        if (command.payrollRunId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required");
        }
        if (command.decision() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_DECISION_REQUIRED", "Payroll decision is required");
        }
        if (!StringUtils.hasText(command.actor())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTOR_REQUIRED", "Actor is required");
        }
    }

    private void validateAttachRecord(AttachPayrollEmployeeRecordCommandDto command) {
        if (command.payrollRunId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required");
        }
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }

        validateComponents(command.earnings(), "EARNING");
        validateComponents(command.deductions(), "DEDUCTION");
    }

    private void validateComponents(List<PayrollAmountComponentDto> components, String type) {
        if (components == null) {
            return;
        }
        for (PayrollAmountComponentDto component : components) {
            if (component == null) {
                throw new HrmsException(HttpStatus.BAD_REQUEST, type + "_COMPONENT_REQUIRED", type + " component is required");
            }
            if (!StringUtils.hasText(component.code())) {
                throw new HrmsException(HttpStatus.BAD_REQUEST, type + "_CODE_REQUIRED", type + " code is required");
            }
            if (!StringUtils.hasText(component.name())) {
                throw new HrmsException(HttpStatus.BAD_REQUEST, type + "_NAME_REQUIRED", type + " name is required");
            }
            if (component.amount() == null || component.amount().compareTo(BigDecimal.ZERO) < 0) {
                throw new HrmsException(HttpStatus.BAD_REQUEST, type + "_AMOUNT_INVALID", type + " amount must be zero or positive");
            }
        }
    }

    private String resolveActor(String actor) {
        return StringUtils.hasText(actor) ? actor : "system";
    }

    private String safeMessage(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private String payrollFinalizedPayload(PayrollRunDto payrollRun, long payslipCount) {
        return "{\"payrollRunId\":\"%s\",\"payrollPeriodId\":\"%s\",\"status\":\"%s\",\"payslipCount\":%d}"
                .formatted(
                        payrollRun.id(),
                        payrollRun.payrollPeriodId(),
                        payrollRun.status().name(),
                        payslipCount);
    }

    private void recordLifecycleMetric(String transition, PayrollRunStatus status) {
        meterRegistry.counter(
                "hrms.payroll.run.lifecycle",
                "transition", transition,
                "status", status.name())
                .increment();
    }

    private com.company.hrms.payroll.model.PayrollRunStatus toApiPayrollRunStatus(PayrollRunStatus status) {
        return com.company.hrms.payroll.model.PayrollRunStatus.valueOf(status.name());
    }

    private com.company.hrms.payroll.model.PayrollPeriodStatus toApiPayrollPeriodStatus(PayrollPeriodStatus status) {
        return com.company.hrms.payroll.model.PayrollPeriodStatus.valueOf(status.name());
    }
}
