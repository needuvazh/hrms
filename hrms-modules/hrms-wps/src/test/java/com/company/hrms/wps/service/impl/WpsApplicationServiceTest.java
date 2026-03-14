package com.company.hrms.wps.service.impl;

import com.company.hrms.wps.model.*;
import com.company.hrms.wps.repository.*;
import com.company.hrms.wps.service.*;

import com.company.hrms.payroll.model.AttachPayrollEmployeeRecordCommandDto;
import com.company.hrms.payroll.model.DefinePayrollPeriodCommandDto;
import com.company.hrms.payroll.model.PayrollEmployeeRecordViewDto;
import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.payroll.model.PayrollPeriodViewDto;
import com.company.hrms.payroll.model.PayrollRunViewDto;
import com.company.hrms.payroll.model.PayslipViewDto;
import com.company.hrms.payroll.model.ReviewPayrollRunCommandDto;
import com.company.hrms.payroll.model.StartPayrollRunCommandDto;
import com.company.hrms.payroll.model.SubmitPayrollRunCommandDto;
import com.company.hrms.payroll.model.PayrollRunStatus;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.wps.model.CreateWpsBatchCommandDto;
import com.company.hrms.wps.model.GenerateWpsExportCommandDto;
import com.company.hrms.wps.model.MarkWpsExportedCommandDto;
import com.company.hrms.wps.model.WpsBatchDto;
import com.company.hrms.wps.model.WpsEmployeeEntryDto;
import com.company.hrms.wps.model.WpsExportFileDto;
import com.company.hrms.wps.model.WpsExportFormatter;
import com.company.hrms.wps.model.WpsPayrollValidator;
import com.company.hrms.wps.repository.WpsRepository;
import com.company.hrms.wps.model.WpsStatus;
import com.company.hrms.wps.service.impl.export.DelimitedWpsExportFormatter;
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

        payrollModuleApi.payrollRun = new PayrollRunViewDto(
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

        payrollModuleApi.payslips = List.of(new PayslipViewDto(
                UUID.randomUUID(),
                "default",
                new PayrollEmployeeRecordViewDto(
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

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommandDto(
                                payrollRunId,
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(batch -> {
                    assertEquals(WpsStatus.VALIDATED, batch.status());
                    assertTrue(batch.validationSummary().contains("Validated"));
                })
                .verifyComplete();

        UUID batchId = wpsRepository.lastBatchId;

        StepVerifier.create(wpsApplicationService.generateExport(new GenerateWpsExportCommandDto(
                                batchId,
                                "WPS-TXT",
                                "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(file -> {
                    assertEquals(WpsStatus.GENERATED, file.status());
                    assertTrue(file.payload().contains("EMPLOYEE_ID|NET_AMOUNT|REFERENCE"));
                })
                .verifyComplete();

        StepVerifier.create(wpsApplicationService.markExported(new MarkWpsExportedCommandDto(batchId, "ops-user"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(batch -> assertEquals(WpsStatus.EXPORTED, batch.status()))
                .verifyComplete();
    }

    @Test
    void failsWhenPayrollRunNotFinalized() {
        UUID payrollRunId = UUID.randomUUID();

        payrollModuleApi.payrollRun = new PayrollRunViewDto(
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

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommandDto(
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

        payrollModuleApi.payrollRun = new PayrollRunViewDto(
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

        payrollModuleApi.payslips = List.of(new PayslipViewDto(
                UUID.randomUUID(),
                "default",
                new PayrollEmployeeRecordViewDto(
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

        StepVerifier.create(wpsApplicationService.createBatchFromPayrollRun(new CreateWpsBatchCommandDto(
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
        private PayrollRunViewDto payrollRun;
        private List<PayslipViewDto> payslips = List.of();

        @Override
        public Mono<PayrollPeriodViewDto> definePayrollPeriod(DefinePayrollPeriodCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> startPayrollRun(StartPayrollRunCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayslipViewDto> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> submitPayrollRun(SubmitPayrollRunCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> reviewPayrollRun(ReviewPayrollRunCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> lockPayrollRun(UUID payrollRunId) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> finalizePayrollRun(UUID payrollRunId) {
            return Mono.empty();
        }

        @Override
        public Mono<PayrollRunViewDto> getPayrollRun(UUID payrollRunId) {
            return Mono.justOrEmpty(payrollRun);
        }

        @Override
        public Flux<PayslipViewDto> payslips(UUID payrollRunId) {
            return Flux.fromIterable(payslips);
        }
    }

    static class InMemoryWpsRepository implements WpsRepository {
        private final ConcurrentHashMap<UUID, WpsBatchDto> batches = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, WpsEmployeeEntryDto> entries = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, WpsExportFileDto> exports = new ConcurrentHashMap<>();
        private volatile UUID lastBatchId;

        @Override
        public Mono<WpsBatchDto> saveBatch(WpsBatchDto batch) {
            batches.put(batch.id(), batch);
            lastBatchId = batch.id();
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsBatchDto> updateBatch(WpsBatchDto batch) {
            batches.put(batch.id(), batch);
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsBatchDto> findBatchById(String tenantId, UUID batchId) {
            WpsBatchDto batch = batches.get(batchId);
            if (batch == null || !tenantId.equals(batch.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(batch);
        }

        @Override
        public Mono<WpsEmployeeEntryDto> saveEntry(WpsEmployeeEntryDto entry) {
            entries.put(entry.id(), entry);
            return Mono.just(entry);
        }

        @Override
        public Flux<WpsEmployeeEntryDto> findEntriesByBatchId(String tenantId, UUID batchId) {
            return Flux.fromIterable(new ArrayList<>(entries.values()))
                    .filter(entry -> tenantId.equals(entry.tenantId()))
                    .filter(entry -> batchId.equals(entry.wpsBatchId()));
        }

        @Override
        public Mono<WpsExportFileDto> saveExportFile(WpsExportFileDto exportFile) {
            exports.put(exportFile.id(), exportFile);
            return Mono.just(exportFile);
        }

        @Override
        public Flux<WpsExportFileDto> findExportFilesByBatchId(String tenantId, UUID batchId) {
            return Flux.fromIterable(new ArrayList<>(exports.values()))
                    .filter(exportFile -> tenantId.equals(exportFile.tenantId()))
                    .filter(exportFile -> batchId.equals(exportFile.wpsBatchId()));
        }
    }
}
