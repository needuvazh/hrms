package com.company.hrms.pasi.application;

import com.company.hrms.employee.api.EmployeeModuleClient;
import com.company.hrms.pasi.api.ComputePasiContributionCommand;
import com.company.hrms.pasi.api.DefinePasiContributionRuleCommand;
import com.company.hrms.pasi.api.PasiContributionRuleView;
import com.company.hrms.pasi.api.PasiEmployeeContributionView;
import com.company.hrms.pasi.api.PasiModuleApi;
import com.company.hrms.pasi.api.PasiPeriodRecordView;
import com.company.hrms.pasi.domain.PasiCalculator;
import com.company.hrms.pasi.domain.PasiContributionRule;
import com.company.hrms.pasi.domain.PasiEmployeeContribution;
import com.company.hrms.pasi.domain.PasiPeriodRecord;
import com.company.hrms.pasi.domain.PasiRepository;
import com.company.hrms.pasi.domain.PasiStatus;
import com.company.hrms.payroll.api.PayrollModuleClient;
import com.company.hrms.payroll.api.PayrollRunView;
import com.company.hrms.payroll.api.PayrollRunStatus;
import com.company.hrms.payroll.api.PayslipView;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PasiApplicationService implements PasiModuleApi {

    private final PasiRepository pasiRepository;
    private final PayrollModuleClient payrollModuleClient;
    private final EmployeeModuleClient employeeModuleClient;
    private final PasiCalculator pasiCalculator;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;

    public PasiApplicationService(
            PasiRepository pasiRepository,
            PayrollModuleClient payrollModuleClient,
            EmployeeModuleClient employeeModuleClient,
            PasiCalculator pasiCalculator,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard
    ) {
        this.pasiRepository = pasiRepository;
        this.payrollModuleClient = payrollModuleClient;
        this.employeeModuleClient = employeeModuleClient;
        this.pasiCalculator = pasiCalculator;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
    }

    @Override
    public Mono<PasiContributionRuleView> defineContributionRule(DefinePasiContributionRuleCommand command) {
        validateRule(command);

        return enablementGuard.requireModuleEnabled("pasi")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    PasiContributionRule rule = new PasiContributionRule(
                            UUID.randomUUID(),
                            tenantId,
                            command.ruleCode().trim().toUpperCase(),
                            command.name().trim(),
                            command.employeeRatePercent(),
                            command.employerRatePercent(),
                            command.salaryCap(),
                            command.active(),
                            now,
                            now);
                    return pasiRepository.saveContributionRule(rule).map(this::toRuleView);
                });
    }

    @Override
    public Mono<PasiPeriodRecordView> computeContributions(ComputePasiContributionCommand command) {
        validateCompute(command);

        return enablementGuard.requireModuleEnabled("pasi")
                .then(requireTenant())
                .flatMap(tenantId -> pasiRepository.existsPeriodRecordForPayrollRun(tenantId, command.payrollRunId())
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PASI_PERIOD_ALREADY_EXISTS", "PASI period record already exists for payroll run"))
                                : resolveRule(tenantId, command.ruleCode())
                                .flatMap(rule -> payrollModuleClient.getPayrollRun(command.payrollRunId())
                                        .flatMap(this::ensurePayrollFinalized)
                                        .then(payrollModuleClient.payslips(command.payrollRunId()).collectList())
                                        .flatMap(payslips -> computeAndStore(tenantId, command, rule, payslips)))));
    }

    @Override
    public Flux<PasiEmployeeContributionView> contributionsByPeriod(UUID pasiPeriodRecordId) {
        if (pasiPeriodRecordId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "PASI_PERIOD_REQUIRED", "PASI period record id is required"));
        }

        return enablementGuard.requireModuleEnabled("pasi")
                .then(requireTenant())
                .flatMapMany(tenantId -> pasiRepository.findEmployeeContributionsByPeriod(tenantId, pasiPeriodRecordId)
                        .map(this::toContributionView));
    }

    @Override
    public Mono<PasiPeriodRecordView> getPeriodRecord(UUID pasiPeriodRecordId) {
        if (pasiPeriodRecordId == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PASI_PERIOD_REQUIRED", "PASI period record id is required"));
        }

        return enablementGuard.requireModuleEnabled("pasi")
                .then(requireTenant())
                .flatMap(tenantId -> pasiRepository.findPeriodRecordById(tenantId, pasiPeriodRecordId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PASI_PERIOD_NOT_FOUND", "PASI period record not found")))
                        .map(this::toPeriodView));
    }

    private Mono<PasiContributionRule> resolveRule(String tenantId, String ruleCode) {
        if (StringUtils.hasText(ruleCode)) {
            return pasiRepository.findActiveContributionRuleByCode(tenantId, ruleCode.trim().toUpperCase())
                    .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PASI_RULE_NOT_FOUND", "PASI rule not found: " + ruleCode)));
        }
        return pasiRepository.findDefaultActiveContributionRule(tenantId)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PASI_RULE_NOT_FOUND", "No active PASI rule configured")));
    }

    private Mono<PasiPeriodRecordView> computeAndStore(
            String tenantId,
            ComputePasiContributionCommand command,
            PasiContributionRule rule,
            List<PayslipView> payslips
    ) {
        if (payslips.isEmpty()) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYSLIPS_REQUIRED", "No payslips found for payroll run"));
        }

        Instant now = Instant.now();
        PasiPeriodRecord initialRecord = new PasiPeriodRecord(
                UUID.randomUUID(),
                tenantId,
                command.payrollRunId(),
                command.periodCode().trim().toUpperCase(),
                rule.id(),
                PasiStatus.CALCULATED,
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                command.calculatedBy().trim(),
                now,
                now,
                now);

        return pasiRepository.savePeriodRecord(initialRecord)
                .flatMap(savedRecord -> Flux.fromIterable(payslips)
                        .concatMap(payslip -> employeeModuleClient.getEmployee(payslip.payrollEmployeeRecord().employeeId())
                                .switchIfEmpty(Mono.error(new HrmsException(
                                        HttpStatus.BAD_REQUEST,
                                        "EMPLOYEE_NOT_FOUND",
                                        "Employee not found for payroll record: " + payslip.payrollEmployeeRecord().employeeId())))
                                .thenReturn(buildEmployeeContribution(tenantId, savedRecord.id(), payslip, rule)))
                        .as(pasiRepository::saveEmployeeContributions)
                        .collectList()
                        .flatMap(contributions -> {
                            BigDecimal totalEmployee = contributions.stream()
                                    .map(PasiEmployeeContribution::employeeContribution)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalEmployer = contributions.stream()
                                    .map(PasiEmployeeContribution::employerContribution)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal total = contributions.stream()
                                    .map(PasiEmployeeContribution::totalContribution)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            PasiPeriodRecord completed = new PasiPeriodRecord(
                                    savedRecord.id(),
                                    savedRecord.tenantId(),
                                    savedRecord.payrollRunId(),
                                    savedRecord.periodCode(),
                                    savedRecord.contributionRuleId(),
                                    PasiStatus.CALCULATED,
                                    contributions.size(),
                                    totalEmployee,
                                    totalEmployer,
                                    total,
                                    savedRecord.calculatedBy(),
                                    savedRecord.calculatedAt(),
                                    savedRecord.createdAt(),
                                    Instant.now());
                            return pasiRepository.updatePeriodRecord(completed).map(this::toPeriodView);
                        })
                        .onErrorResume(error -> pasiRepository.updatePeriodRecord(savedRecord.fail(Instant.now()))
                                .then(Mono.error(error))));
    }

    private PasiEmployeeContribution buildEmployeeContribution(
            String tenantId,
            UUID periodRecordId,
            PayslipView payslip,
            PasiContributionRule rule
    ) {
        PasiCalculator.CalculationResult result = pasiCalculator.calculate(rule, payslip.payrollEmployeeRecord());
        return new PasiEmployeeContribution(
                UUID.randomUUID(),
                tenantId,
                periodRecordId,
                payslip.payrollEmployeeRecord().id(),
                payslip.payrollEmployeeRecord().employeeId(),
                result.contributableSalary(),
                result.employeeContribution(),
                result.employerContribution(),
                result.totalContribution(),
                Instant.now());
    }

    private Mono<PayrollRunView> ensurePayrollFinalized(PayrollRunView payrollRun) {
        if (payrollRun.status() != PayrollRunStatus.FINALIZED) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_FINALIZED", "Payroll run must be finalized for PASI calculation"));
        }
        return Mono.just(payrollRun);
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateRule(DefinePasiContributionRuleCommand command) {
        if (!StringUtils.hasText(command.ruleCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RULE_CODE_REQUIRED", "Rule code is required");
        }
        if (!StringUtils.hasText(command.name())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RULE_NAME_REQUIRED", "Rule name is required");
        }
        if (command.employeeRatePercent() == null || command.employeeRatePercent().compareTo(BigDecimal.ZERO) < 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_RATE_INVALID", "Employee rate percent must be zero or positive");
        }
        if (command.employerRatePercent() == null || command.employerRatePercent().compareTo(BigDecimal.ZERO) < 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYER_RATE_INVALID", "Employer rate percent must be zero or positive");
        }
        if (command.salaryCap() != null && command.salaryCap().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SALARY_CAP_INVALID", "Salary cap must be greater than zero");
        }
    }

    private void validateCompute(ComputePasiContributionCommand command) {
        if (command.payrollRunId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required");
        }
        if (!StringUtils.hasText(command.periodCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERIOD_CODE_REQUIRED", "Period code is required");
        }
        if (!StringUtils.hasText(command.calculatedBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CALCULATED_BY_REQUIRED", "Calculated by is required");
        }
    }

    private PasiContributionRuleView toRuleView(PasiContributionRule rule) {
        return new PasiContributionRuleView(
                rule.id(),
                rule.tenantId(),
                rule.ruleCode(),
                rule.name(),
                rule.employeeRatePercent(),
                rule.employerRatePercent(),
                rule.salaryCap(),
                rule.active(),
                rule.createdAt(),
                rule.updatedAt());
    }

    private PasiPeriodRecordView toPeriodView(PasiPeriodRecord record) {
        return new PasiPeriodRecordView(
                record.id(),
                record.tenantId(),
                record.payrollRunId(),
                record.periodCode(),
                record.contributionRuleId(),
                record.status(),
                record.totalEmployees(),
                record.totalEmployeeContribution(),
                record.totalEmployerContribution(),
                record.totalContribution(),
                record.calculatedBy(),
                record.calculatedAt(),
                record.createdAt(),
                record.updatedAt());
    }

    private PasiEmployeeContributionView toContributionView(PasiEmployeeContribution contribution) {
        return new PasiEmployeeContributionView(
                contribution.id(),
                contribution.tenantId(),
                contribution.pasiPeriodRecordId(),
                contribution.payrollEmployeeRecordId(),
                contribution.employeeId(),
                contribution.contributableSalary(),
                contribution.employeeContribution(),
                contribution.employerContribution(),
                contribution.totalContribution(),
                contribution.createdAt());
    }
}
