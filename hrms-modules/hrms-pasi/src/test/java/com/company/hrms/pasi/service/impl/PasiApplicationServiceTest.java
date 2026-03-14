package com.company.hrms.pasi.service.impl;

import com.company.hrms.pasi.model.*;
import com.company.hrms.pasi.repository.*;
import com.company.hrms.pasi.service.*;

import com.company.hrms.employee.model.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleClient;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.pasi.model.ComputePasiContributionCommandDto;
import com.company.hrms.pasi.model.DefinePasiContributionRuleCommandDto;
import com.company.hrms.pasi.model.PasiCalculator;
import com.company.hrms.pasi.model.PasiContributionRuleDto;
import com.company.hrms.pasi.model.PasiEmployeeContributionDto;
import com.company.hrms.pasi.model.PasiPeriodRecordDto;
import com.company.hrms.pasi.repository.PasiRepository;
import com.company.hrms.pasi.model.PasiStatus;
import com.company.hrms.payroll.model.AttachPayrollEmployeeRecordCommandDto;
import com.company.hrms.payroll.model.DefinePayrollPeriodCommandDto;
import com.company.hrms.payroll.service.PayrollModuleClient;
import com.company.hrms.payroll.model.PayrollEmployeeRecordViewDto;
import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.payroll.model.PayrollPeriodViewDto;
import com.company.hrms.payroll.model.PayrollRunViewDto;
import com.company.hrms.payroll.model.PayslipViewDto;
import com.company.hrms.payroll.model.ReviewPayrollRunCommandDto;
import com.company.hrms.payroll.model.StartPayrollRunCommandDto;
import com.company.hrms.payroll.model.SubmitPayrollRunCommandDto;
import com.company.hrms.payroll.model.PayrollPeriodStatus;
import com.company.hrms.payroll.model.PayrollRunStatus;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PasiApplicationServiceTest {

    private final InMemoryPasiRepository pasiRepository = new InMemoryPasiRepository();
    private final StubPayrollModuleApi payrollModuleApi = new StubPayrollModuleApi();
    private final StubEmployeeModuleApi employeeModuleApi = new StubEmployeeModuleApi();

    private final PasiApplicationService pasiApplicationService = new PasiApplicationService(
            pasiRepository,
            payrollModuleApi,
            employeeModuleApi,
            new PasiCalculator(),
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new EnabledFeatureToggleService()));

    @Test
    void definesRuleAndComputesContributionsForFinalizedPayroll() {
        UUID payrollRunId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        payrollModuleApi.run = new PayrollRunViewDto(
                payrollRunId,
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.FINALIZED,
                UUID.randomUUID(),
                "payroll-admin",
                "payroll-manager",
                "finance-head",
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
                        new BigDecimal("2000.000"),
                        new BigDecimal("200.000"),
                        new BigDecimal("1800.000"),
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

        employeeModuleApi.employees.put(employeeId, new EmployeeViewDto(
                employeeId,
                "default",
                "EMP-1",
                "John",
                "Doe",
                "john@default.hrms",
                "ENG",
                "Engineer",
                Instant.now(),
                Instant.now()));

        StepVerifier.create(pasiApplicationService.defineContributionRule(new DefinePasiContributionRuleCommandDto(
                                "PASI-TEST",
                                "PASI Test",
                                new BigDecimal("7.000"),
                                new BigDecimal("10.500"),
                                new BigDecimal("3000.000"),
                                true))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(rule -> assertEquals("PASI-TEST", rule.ruleCode()))
                .verifyComplete();

        StepVerifier.create(pasiApplicationService.computeContributions(new ComputePasiContributionCommandDto(
                                payrollRunId,
                                "2026-03",
                                "statutory-user",
                                "PASI-TEST"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(period -> {
                    assertEquals(PasiStatus.CALCULATED, period.status());
                    assertEquals(1, period.totalEmployees());
                    assertEquals(new BigDecimal("140.000"), period.totalEmployeeContribution());
                    assertEquals(new BigDecimal("210.000"), period.totalEmployerContribution());
                    assertEquals(new BigDecimal("350.000"), period.totalContribution());
                })
                .verifyComplete();
    }

    @Test
    void rejectsNonFinalizedPayrollRun() {
        UUID payrollRunId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        pasiRepository.rules.put(ruleId, new PasiContributionRuleDto(
                ruleId,
                "default",
                "PASI-STD",
                "PASI Standard",
                new BigDecimal("7.000"),
                new BigDecimal("10.500"),
                new BigDecimal("3000.000"),
                true,
                Instant.now(),
                Instant.now()));

        payrollModuleApi.run = new PayrollRunViewDto(
                payrollRunId,
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.APPROVED,
                UUID.randomUUID(),
                "payroll-admin",
                "payroll-manager",
                "finance-head",
                null,
                Instant.now(),
                Instant.now(),
                null,
                Instant.now(),
                Instant.now());

        StepVerifier.create(pasiApplicationService.computeContributions(new ComputePasiContributionCommandDto(
                                payrollRunId,
                                "2026-04",
                                "statutory-user",
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("PAYROLL_RUN_NOT_FINALIZED", ex.getErrorCode());
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

    static class StubPayrollModuleApi implements PayrollModuleClient {
        private PayrollRunViewDto run;
        private List<PayslipViewDto> payslips = List.of();

        @Override
        public Mono<PayrollPeriodViewDto> definePayrollPeriod(DefinePayrollPeriodCommandDto command) {
            return Mono.just(new PayrollPeriodViewDto(
                    UUID.randomUUID(),
                    "default",
                    command.periodCode(),
                    command.startDate(),
                    command.endDate(),
                    PayrollPeriodStatus.OPEN,
                    command.description(),
                    Instant.now(),
                    Instant.now()));
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
            return Mono.justOrEmpty(run);
        }

        @Override
        public Flux<PayslipViewDto> payslips(UUID payrollRunId) {
            return Flux.fromIterable(payslips);
        }
    }

    static class StubEmployeeModuleApi implements EmployeeModuleClient {
        private final Map<UUID, EmployeeViewDto> employees = new ConcurrentHashMap<>();

        @Override
        public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
            return Mono.justOrEmpty(employees.get(employeeId));
        }

        @Override
        public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
            return Flux.fromIterable(new ArrayList<>(employees.values()));
        }
    }

    static class InMemoryPasiRepository implements PasiRepository {
        private final Map<UUID, PasiContributionRuleDto> rules = new ConcurrentHashMap<>();
        private final Map<UUID, PasiPeriodRecordDto> periods = new ConcurrentHashMap<>();
        private final Map<UUID, PasiEmployeeContributionDto> contributions = new ConcurrentHashMap<>();

        @Override
        public Mono<PasiContributionRuleDto> saveContributionRule(PasiContributionRuleDto rule) {
            rules.put(rule.id(), rule);
            return Mono.just(rule);
        }

        @Override
        public Mono<PasiContributionRuleDto> findActiveContributionRuleByCode(String tenantId, String ruleCode) {
            return Flux.fromIterable(rules.values())
                    .filter(PasiContributionRuleDto::active)
                    .filter(rule -> tenantId.equals(rule.tenantId()))
                    .filter(rule -> ruleCode.equals(rule.ruleCode()))
                    .next();
        }

        @Override
        public Mono<PasiContributionRuleDto> findDefaultActiveContributionRule(String tenantId) {
            return Flux.fromIterable(rules.values())
                    .filter(PasiContributionRuleDto::active)
                    .filter(rule -> tenantId.equals(rule.tenantId()))
                    .next();
        }

        @Override
        public Mono<PasiPeriodRecordDto> savePeriodRecord(PasiPeriodRecordDto periodRecord) {
            periods.put(periodRecord.id(), periodRecord);
            return Mono.just(periodRecord);
        }

        @Override
        public Mono<PasiPeriodRecordDto> updatePeriodRecord(PasiPeriodRecordDto periodRecord) {
            periods.put(periodRecord.id(), periodRecord);
            return Mono.just(periodRecord);
        }

        @Override
        public Mono<PasiPeriodRecordDto> findPeriodRecordById(String tenantId, UUID periodRecordId) {
            PasiPeriodRecordDto record = periods.get(periodRecordId);
            if (record == null || !tenantId.equals(record.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(record);
        }

        @Override
        public Flux<PasiEmployeeContributionDto> saveEmployeeContributions(Flux<PasiEmployeeContributionDto> data) {
            return data.doOnNext(contribution -> contributions.put(contribution.id(), contribution));
        }

        @Override
        public Flux<PasiEmployeeContributionDto> findEmployeeContributionsByPeriod(String tenantId, UUID periodRecordId) {
            return Flux.fromIterable(contributions.values())
                    .filter(contribution -> tenantId.equals(contribution.tenantId()))
                    .filter(contribution -> periodRecordId.equals(contribution.pasiPeriodRecordId()));
        }

        @Override
        public Mono<Boolean> existsPeriodRecordForPayrollRun(String tenantId, UUID payrollRunId) {
            boolean exists = periods.values().stream()
                    .anyMatch(period -> tenantId.equals(period.tenantId()) && payrollRunId.equals(period.payrollRunId()));
            return Mono.just(exists);
        }
    }
}
