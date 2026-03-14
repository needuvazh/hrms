package com.company.hrms.pasi.application;

import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleClient;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.pasi.api.ComputePasiContributionCommand;
import com.company.hrms.pasi.api.DefinePasiContributionRuleCommand;
import com.company.hrms.pasi.domain.PasiCalculator;
import com.company.hrms.pasi.domain.PasiContributionRule;
import com.company.hrms.pasi.domain.PasiEmployeeContribution;
import com.company.hrms.pasi.domain.PasiPeriodRecord;
import com.company.hrms.pasi.domain.PasiRepository;
import com.company.hrms.pasi.domain.PasiStatus;
import com.company.hrms.payroll.api.AttachPayrollEmployeeRecordCommand;
import com.company.hrms.payroll.api.DefinePayrollPeriodCommand;
import com.company.hrms.payroll.api.PayrollModuleClient;
import com.company.hrms.payroll.api.PayrollEmployeeRecordView;
import com.company.hrms.payroll.api.PayrollModuleApi;
import com.company.hrms.payroll.api.PayrollPeriodView;
import com.company.hrms.payroll.api.PayrollRunView;
import com.company.hrms.payroll.api.PayslipView;
import com.company.hrms.payroll.api.ReviewPayrollRunCommand;
import com.company.hrms.payroll.api.StartPayrollRunCommand;
import com.company.hrms.payroll.api.SubmitPayrollRunCommand;
import com.company.hrms.payroll.api.PayrollPeriodStatus;
import com.company.hrms.payroll.api.PayrollRunStatus;
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

        payrollModuleApi.run = new PayrollRunView(
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

        payrollModuleApi.payslips = List.of(new PayslipView(
                UUID.randomUUID(),
                "default",
                new PayrollEmployeeRecordView(
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

        employeeModuleApi.employees.put(employeeId, new EmployeeView(
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

        StepVerifier.create(pasiApplicationService.defineContributionRule(new DefinePasiContributionRuleCommand(
                                "PASI-TEST",
                                "PASI Test",
                                new BigDecimal("7.000"),
                                new BigDecimal("10.500"),
                                new BigDecimal("3000.000"),
                                true))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(rule -> assertEquals("PASI-TEST", rule.ruleCode()))
                .verifyComplete();

        StepVerifier.create(pasiApplicationService.computeContributions(new ComputePasiContributionCommand(
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

        pasiRepository.rules.put(ruleId, new PasiContributionRule(
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

        payrollModuleApi.run = new PayrollRunView(
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

        StepVerifier.create(pasiApplicationService.computeContributions(new ComputePasiContributionCommand(
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
        private PayrollRunView run;
        private List<PayslipView> payslips = List.of();

        @Override
        public Mono<PayrollPeriodView> definePayrollPeriod(DefinePayrollPeriodCommand command) {
            return Mono.just(new PayrollPeriodView(
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
            return Mono.justOrEmpty(run);
        }

        @Override
        public Flux<PayslipView> payslips(UUID payrollRunId) {
            return Flux.fromIterable(payslips);
        }
    }

    static class StubEmployeeModuleApi implements EmployeeModuleClient {
        private final Map<UUID, EmployeeView> employees = new ConcurrentHashMap<>();

        @Override
        public Mono<EmployeeView> createEmployee(CreateEmployeeCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<EmployeeView> getEmployee(UUID employeeId) {
            return Mono.justOrEmpty(employees.get(employeeId));
        }

        @Override
        public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
            return Flux.fromIterable(new ArrayList<>(employees.values()));
        }
    }

    static class InMemoryPasiRepository implements PasiRepository {
        private final Map<UUID, PasiContributionRule> rules = new ConcurrentHashMap<>();
        private final Map<UUID, PasiPeriodRecord> periods = new ConcurrentHashMap<>();
        private final Map<UUID, PasiEmployeeContribution> contributions = new ConcurrentHashMap<>();

        @Override
        public Mono<PasiContributionRule> saveContributionRule(PasiContributionRule rule) {
            rules.put(rule.id(), rule);
            return Mono.just(rule);
        }

        @Override
        public Mono<PasiContributionRule> findActiveContributionRuleByCode(String tenantId, String ruleCode) {
            return Flux.fromIterable(rules.values())
                    .filter(PasiContributionRule::active)
                    .filter(rule -> tenantId.equals(rule.tenantId()))
                    .filter(rule -> ruleCode.equals(rule.ruleCode()))
                    .next();
        }

        @Override
        public Mono<PasiContributionRule> findDefaultActiveContributionRule(String tenantId) {
            return Flux.fromIterable(rules.values())
                    .filter(PasiContributionRule::active)
                    .filter(rule -> tenantId.equals(rule.tenantId()))
                    .next();
        }

        @Override
        public Mono<PasiPeriodRecord> savePeriodRecord(PasiPeriodRecord periodRecord) {
            periods.put(periodRecord.id(), periodRecord);
            return Mono.just(periodRecord);
        }

        @Override
        public Mono<PasiPeriodRecord> updatePeriodRecord(PasiPeriodRecord periodRecord) {
            periods.put(periodRecord.id(), periodRecord);
            return Mono.just(periodRecord);
        }

        @Override
        public Mono<PasiPeriodRecord> findPeriodRecordById(String tenantId, UUID periodRecordId) {
            PasiPeriodRecord record = periods.get(periodRecordId);
            if (record == null || !tenantId.equals(record.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(record);
        }

        @Override
        public Flux<PasiEmployeeContribution> saveEmployeeContributions(Flux<PasiEmployeeContribution> data) {
            return data.doOnNext(contribution -> contributions.put(contribution.id(), contribution));
        }

        @Override
        public Flux<PasiEmployeeContribution> findEmployeeContributionsByPeriod(String tenantId, UUID periodRecordId) {
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
