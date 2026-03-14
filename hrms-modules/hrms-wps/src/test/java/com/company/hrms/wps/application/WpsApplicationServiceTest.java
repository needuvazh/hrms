package com.company.hrms.wps.application;

import com.company.hrms.payroll.api.AttachPayrollEmployeeRecordCommand;
import com.company.hrms.payroll.api.DefinePayrollPeriodCommand;
import com.company.hrms.payroll.api.PayrollEmployeeRecordView;
import com.company.hrms.payroll.api.PayrollModuleApi;
import com.company.hrms.payroll.api.PayrollPeriodView;
import com.company.hrms.payroll.api.PayrollRunView;
import com.company.hrms.payroll.api.PayslipView;
import com.company.hrms.payroll.api.ReviewPayrollRunCommand;
import com.company.hrms.payroll.api.StartPayrollRunCommand;
import com.company.hrms.payroll.api.SubmitPayrollRunCommand;
import com.company.hrms.payroll.api.PayrollRunStatus;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.wps.api.CreateWpsBatchCommand;
import com.company.hrms.wps.api.GenerateWpsExportCommand;
import com.company.hrms.wps.api.MarkWpsExportedCommand;
import com.company.hrms.wps.domain.WpsBatch;
import com.company.hrms.wps.domain.WpsEmployeeEntry;
import com.company.hrms.wps.domain.WpsExportFile;
import com.company.hrms.wps.domain.WpsExportFormatter;
import com.company.hrms.wps.domain.WpsPayrollValidator;
import com.company.hrms.wps.domain.WpsRepository;
import com.company.hrms.wps.domain.WpsStatus;
import com.company.hrms.wps.infrastructure.export.DelimitedWpsExportFormatter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WpsApplicationServiceTest {

    private final InMemoryWpsRepository wpsRepository = new InMemoryWpsRepository();
    private final StubPayrollModuleApi payrollModuleApi = new StubPayrollModuleApi();
    private final WpsExportFormatter formatter = new DelimitedWpsExportFormatter();
    private final WpsApplicationService wpsApplicationService = new WpsApplicationService(
            wpsRepository,
            payrollModuleApi,
            new DefaultTenantContextAccessor(),
            new WpsPayrollValidator(),
            List.of(formatter));

    @Test
    void createsBatchAndGeneratesExportFromFinalizedPayrollRun() {
        UUID payrollRunId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        payrollModuleApi.payrollRun = new PayrollRunView(
                payrollRunId,
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.FINALIZED,
                UUID.randomUUID(),
                "payroll-admin",
                "manager",
                "reviewer",
                null,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now());

        payrollModuleApi.payslips = List.of(new PayslipView(
                UUID.randomUUID(),
                "default",
                new PayrollEmployeeRecordView(
                        UUID.randomUUID(),
                        "default",
                        payrollRunId,
                        employeeId,
                        new BigDecimal("1000.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("900.00"),
                        null,
                        Instant.now(),
                        Instant.now()),
                null,
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now(),
                Instant.now()));

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommand(
                                payrollRunId,
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(batch -> {
                    assertEquals(WpsStatus.VALIDATED, batch.status());
                    assertTrue(batch.validationSummary().contains("Validated"));
                })
                .verifyComplete();

        UUID batchId = wpsRepository.lastBatchId;

        StepVerifier.create(wpsApplicationService.generateExport(new GenerateWpsExportCommand(
                                batchId,
                                "WPS-TXT",
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(file -> {
                    assertEquals(WpsStatus.GENERATED, file.status());
                    assertTrue(file.payload().contains("EMPLOYEE_ID|NET_AMOUNT|REFERENCE"));
                })
                .verifyComplete();

        StepVerifier.create(wpsApplicationService.markExported(new MarkWpsExportedCommand(batchId, "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(batch -> assertEquals(WpsStatus.EXPORTED, batch.status()))
                .verifyComplete();
    }

    @Test
    void failsWhenPayrollRunNotFinalized() {
        UUID payrollRunId = UUID.randomUUID();

        payrollModuleApi.payrollRun = new PayrollRunView(
                payrollRunId,
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.APPROVED,
                UUID.randomUUID(),
                "payroll-admin",
                "manager",
                "reviewer",
                null,
                Instant.now(),
                Instant.now(),
                null,
                Instant.now(),
                Instant.now());
        payrollModuleApi.payslips = List.of();

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommand(
                                payrollRunId,
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("PAYROLL_RUN_NOT_FINALIZED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void validationFailsForInvalidNetAmount() {
        UUID payrollRunId = UUID.randomUUID();

        payrollModuleApi.payrollRun = new PayrollRunView(
                payrollRunId,
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.FINALIZED,
                UUID.randomUUID(),
                "payroll-admin",
                "manager",
                "reviewer",
                null,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now());

        payrollModuleApi.payslips = List.of(new PayslipView(
                UUID.randomUUID(),
                "default",
                new PayrollEmployeeRecordView(
                        UUID.randomUUID(),
                        "default",
                        payrollRunId,
                        UUID.randomUUID(),
                        new BigDecimal("1000.00"),
                        new BigDecimal("1000.00"),
                        BigDecimal.ZERO,
                        null,
                        Instant.now(),
                        Instant.now()),
                null,
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now(),
                Instant.now()));

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommand(
                                payrollRunId,
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("WPS_VALIDATION_FAILED", ex.getErrorCode());
                })
                .verify();
    }

    static class StubPayrollModuleApi implements PayrollModuleApi {
        private PayrollRunView payrollRun;
        private List<PayslipView> payslips = List.of();

        @Override
        public Mono<PayrollPeriodView> definePayrollPeriod(DefinePayrollPeriodCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> startPayrollRun(StartPayrollRunCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayslipView> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> submitPayrollRun(SubmitPayrollRunCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> reviewPayrollRun(ReviewPayrollRunCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> lockPayrollRun(UUID payrollRunId) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> finalizePayrollRun(UUID payrollRunId) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunView> getPayrollRun(UUID payrollRunId) {
            return Mono.justOrEmpty(payrollRun);
        }

        @Override
        public Flux<PayslipView> payslips(UUID payrollRunId) {
            return Flux.fromIterable(payslips);
        }
    }

    static class InMemoryWpsRepository implements WpsRepository {
        private final ConcurrentHashMap<UUID, WpsBatch> batches = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, WpsEmployeeEntry> entries = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, WpsExportFile> exports = new ConcurrentHashMap<>();
        private volatile UUID lastBatchId;

        @Override
        public Mono<WpsBatch> saveBatch(WpsBatch batch) {
            batches.put(batch.id(), batch);
            lastBatchId = batch.id();
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsBatch> updateBatch(WpsBatch batch) {
            batches.put(batch.id(), batch);
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsBatch> findBatchById(String tenantId, UUID batchId) {
            WpsBatch batch = batches.get(batchId);
            if (batch == null || !tenantId.equals(batch.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsEmployeeEntry> saveEntry(WpsEmployeeEntry entry) {
            entries.put(entry.id(), entry);
            return Mono.just(entry);
        }

        @Override
        public Flux<WpsEmployeeEntry> findEntriesByBatchId(String tenantId, UUID batchId) {
            return Flux.fromIterable(new ArrayList<>(entries.values()))
                    .filter(entry -> tenantId.equals(entry.tenantId()))
                    .filter(entry -> batchId.equals(entry.wpsBatchId()));
        }

        @Override
        public Mono<WpsExportFile> saveExportFile(WpsExportFile exportFile) {
            exports.put(exportFile.id(), exportFile);
            return Mono.just(exportFile);
        }

        @Override
        public Flux<WpsExportFile> findExportFilesByBatchId(String tenantId, UUID batchId) {
            return Flux.fromIterable(new ArrayList<>(exports.values()))
                    .filter(exportFile -> tenantId.equals(exportFile.tenantId()))
                    .filter(exportFile -> batchId.equals(exportFile.wpsBatchId()));
        }
    }
}
