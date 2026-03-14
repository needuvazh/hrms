package com.company.hrms.pasi.infrastructure.persistence;

import com.company.hrms.pasi.domain.PasiContributionRule;
import com.company.hrms.pasi.domain.PasiEmployeeContribution;
import com.company.hrms.pasi.domain.PasiPeriodRecord;
import com.company.hrms.pasi.domain.PasiRepository;
import com.company.hrms.pasi.domain.PasiStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PasiR2dbcRepository implements PasiRepository {

    private final DatabaseClient databaseClient;

    public PasiR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PasiContributionRule> saveContributionRule(PasiContributionRule rule) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO pasi.pasi_contribution_rules(
                            id, tenant_id, rule_code, name, employee_rate_percent, employer_rate_percent,
                            salary_cap, is_active, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :ruleCode, :name, :employeeRatePercent, :employerRatePercent,
                            :salaryCap, :isActive, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, rule_code, name, employee_rate_percent, employer_rate_percent,
                                  salary_cap, is_active, created_at, updated_at
                        """)
                .bind("id", rule.id())
                .bind("tenantId", rule.tenantId())
                .bind("ruleCode", rule.ruleCode())
                .bind("name", rule.name())
                .bind("employeeRatePercent", rule.employeeRatePercent())
                .bind("employerRatePercent", rule.employerRatePercent())
                .bind("isActive", rule.active())
                .bind("createdAt", rule.createdAt())
                .bind("updatedAt", rule.updatedAt());

        spec = bindNullable(spec, "salaryCap", rule.salaryCap(), BigDecimal.class);

        return spec.map((row, metadata) -> mapRule(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("rule_code", String.class),
                        row.get("name", String.class),
                        row.get("employee_rate_percent", BigDecimal.class),
                        row.get("employer_rate_percent", BigDecimal.class),
                        row.get("salary_cap", BigDecimal.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PasiContributionRule> findActiveContributionRuleByCode(String tenantId, String ruleCode) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, rule_code, name, employee_rate_percent, employer_rate_percent,
                               salary_cap, is_active, created_at, updated_at
                        FROM pasi.pasi_contribution_rules
                        WHERE tenant_id = :tenantId
                          AND rule_code = :ruleCode
                          AND is_active = true
                        """)
                .bind("tenantId", tenantId)
                .bind("ruleCode", ruleCode)
                .map((row, metadata) -> mapRule(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("rule_code", String.class),
                        row.get("name", String.class),
                        row.get("employee_rate_percent", BigDecimal.class),
                        row.get("employer_rate_percent", BigDecimal.class),
                        row.get("salary_cap", BigDecimal.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PasiContributionRule> findDefaultActiveContributionRule(String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, rule_code, name, employee_rate_percent, employer_rate_percent,
                               salary_cap, is_active, created_at, updated_at
                        FROM pasi.pasi_contribution_rules
                        WHERE tenant_id = :tenantId
                          AND is_active = true
                        ORDER BY created_at ASC
                        LIMIT 1
                        """)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapRule(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("rule_code", String.class),
                        row.get("name", String.class),
                        row.get("employee_rate_percent", BigDecimal.class),
                        row.get("employer_rate_percent", BigDecimal.class),
                        row.get("salary_cap", BigDecimal.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PasiPeriodRecord> savePeriodRecord(PasiPeriodRecord periodRecord) {
        return databaseClient.sql("""
                        INSERT INTO pasi.pasi_period_records(
                            id, tenant_id, payroll_run_id, period_code, contribution_rule_id, status,
                            total_employees, total_employee_contribution, total_employer_contribution, total_contribution,
                            calculated_by, calculated_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :payrollRunId, :periodCode, :contributionRuleId, :status,
                            :totalEmployees, :totalEmployeeContribution, :totalEmployerContribution, :totalContribution,
                            :calculatedBy, :calculatedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, payroll_run_id, period_code, contribution_rule_id, status,
                                  total_employees, total_employee_contribution, total_employer_contribution, total_contribution,
                                  calculated_by, calculated_at, created_at, updated_at
                        """)
                .bind("id", periodRecord.id())
                .bind("tenantId", periodRecord.tenantId())
                .bind("payrollRunId", periodRecord.payrollRunId())
                .bind("periodCode", periodRecord.periodCode())
                .bind("contributionRuleId", periodRecord.contributionRuleId())
                .bind("status", periodRecord.status().name())
                .bind("totalEmployees", periodRecord.totalEmployees())
                .bind("totalEmployeeContribution", periodRecord.totalEmployeeContribution())
                .bind("totalEmployerContribution", periodRecord.totalEmployerContribution())
                .bind("totalContribution", periodRecord.totalContribution())
                .bind("calculatedBy", periodRecord.calculatedBy())
                .bind("calculatedAt", periodRecord.calculatedAt())
                .bind("createdAt", periodRecord.createdAt())
                .bind("updatedAt", periodRecord.updatedAt())
                .map((row, metadata) -> mapPeriodRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("period_code", String.class),
                        row.get("contribution_rule_id", UUID.class),
                        row.get("status", String.class),
                        row.get("total_employees", Integer.class),
                        row.get("total_employee_contribution", BigDecimal.class),
                        row.get("total_employer_contribution", BigDecimal.class),
                        row.get("total_contribution", BigDecimal.class),
                        row.get("calculated_by", String.class),
                        row.get("calculated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PasiPeriodRecord> updatePeriodRecord(PasiPeriodRecord periodRecord) {
        return databaseClient.sql("""
                        UPDATE pasi.pasi_period_records
                        SET status = :status,
                            total_employees = :totalEmployees,
                            total_employee_contribution = :totalEmployeeContribution,
                            total_employer_contribution = :totalEmployerContribution,
                            total_contribution = :totalContribution,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, payroll_run_id, period_code, contribution_rule_id, status,
                                  total_employees, total_employee_contribution, total_employer_contribution, total_contribution,
                                  calculated_by, calculated_at, created_at, updated_at
                        """)
                .bind("id", periodRecord.id())
                .bind("tenantId", periodRecord.tenantId())
                .bind("status", periodRecord.status().name())
                .bind("totalEmployees", periodRecord.totalEmployees())
                .bind("totalEmployeeContribution", periodRecord.totalEmployeeContribution())
                .bind("totalEmployerContribution", periodRecord.totalEmployerContribution())
                .bind("totalContribution", periodRecord.totalContribution())
                .bind("updatedAt", periodRecord.updatedAt())
                .map((row, metadata) -> mapPeriodRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("period_code", String.class),
                        row.get("contribution_rule_id", UUID.class),
                        row.get("status", String.class),
                        row.get("total_employees", Integer.class),
                        row.get("total_employee_contribution", BigDecimal.class),
                        row.get("total_employer_contribution", BigDecimal.class),
                        row.get("total_contribution", BigDecimal.class),
                        row.get("calculated_by", String.class),
                        row.get("calculated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PasiPeriodRecord> findPeriodRecordById(String tenantId, UUID periodRecordId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, payroll_run_id, period_code, contribution_rule_id, status,
                               total_employees, total_employee_contribution, total_employer_contribution, total_contribution,
                               calculated_by, calculated_at, created_at, updated_at
                        FROM pasi.pasi_period_records
                        WHERE tenant_id = :tenantId
                          AND id = :periodRecordId
                        """)
                .bind("tenantId", tenantId)
                .bind("periodRecordId", periodRecordId)
                .map((row, metadata) -> mapPeriodRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("period_code", String.class),
                        row.get("contribution_rule_id", UUID.class),
                        row.get("status", String.class),
                        row.get("total_employees", Integer.class),
                        row.get("total_employee_contribution", BigDecimal.class),
                        row.get("total_employer_contribution", BigDecimal.class),
                        row.get("total_contribution", BigDecimal.class),
                        row.get("calculated_by", String.class),
                        row.get("calculated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<PasiEmployeeContribution> saveEmployeeContributions(Flux<PasiEmployeeContribution> contributions) {
        return contributions.concatMap(contribution -> databaseClient.sql("""
                        INSERT INTO pasi.pasi_employee_contributions(
                            id, tenant_id, pasi_period_record_id, payroll_employee_record_id, employee_id,
                            contributable_salary, employee_contribution, employer_contribution, total_contribution, created_at
                        ) VALUES (
                            :id, :tenantId, :pasiPeriodRecordId, :payrollEmployeeRecordId, :employeeId,
                            :contributableSalary, :employeeContribution, :employerContribution, :totalContribution, :createdAt
                        )
                        RETURNING id, tenant_id, pasi_period_record_id, payroll_employee_record_id, employee_id,
                                  contributable_salary, employee_contribution, employer_contribution, total_contribution, created_at
                        """)
                .bind("id", contribution.id())
                .bind("tenantId", contribution.tenantId())
                .bind("pasiPeriodRecordId", contribution.pasiPeriodRecordId())
                .bind("payrollEmployeeRecordId", contribution.payrollEmployeeRecordId())
                .bind("employeeId", contribution.employeeId())
                .bind("contributableSalary", contribution.contributableSalary())
                .bind("employeeContribution", contribution.employeeContribution())
                .bind("employerContribution", contribution.employerContribution())
                .bind("totalContribution", contribution.totalContribution())
                .bind("createdAt", contribution.createdAt())
                .map((row, metadata) -> mapEmployeeContribution(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("pasi_period_record_id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("contributable_salary", BigDecimal.class),
                        row.get("employee_contribution", BigDecimal.class),
                        row.get("employer_contribution", BigDecimal.class),
                        row.get("total_contribution", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .one());
    }

    @Override
    public Flux<PasiEmployeeContribution> findEmployeeContributionsByPeriod(String tenantId, UUID periodRecordId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, pasi_period_record_id, payroll_employee_record_id, employee_id,
                               contributable_salary, employee_contribution, employer_contribution, total_contribution, created_at
                        FROM pasi.pasi_employee_contributions
                        WHERE tenant_id = :tenantId
                          AND pasi_period_record_id = :periodRecordId
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("periodRecordId", periodRecordId)
                .map((row, metadata) -> mapEmployeeContribution(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("pasi_period_record_id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("contributable_salary", BigDecimal.class),
                        row.get("employee_contribution", BigDecimal.class),
                        row.get("employer_contribution", BigDecimal.class),
                        row.get("total_contribution", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<Boolean> existsPeriodRecordForPayrollRun(String tenantId, UUID payrollRunId) {
        return databaseClient.sql("""
                        SELECT COUNT(*) AS total
                        FROM pasi.pasi_period_records
                        WHERE tenant_id = :tenantId
                          AND payroll_run_id = :payrollRunId
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollRunId", payrollRunId)
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .map(total -> total != null && total > 0)
                .defaultIfEmpty(false);
    }

    private PasiContributionRule mapRule(
            UUID id,
            String tenantId,
            String ruleCode,
            String name,
            BigDecimal employeeRatePercent,
            BigDecimal employerRatePercent,
            BigDecimal salaryCap,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PasiContributionRule(
                id,
                tenantId,
                ruleCode,
                name,
                employeeRatePercent,
                employerRatePercent,
                salaryCap,
                active,
                createdAt,
                updatedAt);
    }

    private PasiPeriodRecord mapPeriodRecord(
            UUID id,
            String tenantId,
            UUID payrollRunId,
            String periodCode,
            UUID contributionRuleId,
            String status,
            Integer totalEmployees,
            BigDecimal totalEmployeeContribution,
            BigDecimal totalEmployerContribution,
            BigDecimal totalContribution,
            String calculatedBy,
            Instant calculatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PasiPeriodRecord(
                id,
                tenantId,
                payrollRunId,
                periodCode,
                contributionRuleId,
                PasiStatus.valueOf(status),
                totalEmployees == null ? 0 : totalEmployees,
                totalEmployeeContribution,
                totalEmployerContribution,
                totalContribution,
                calculatedBy,
                calculatedAt,
                createdAt,
                updatedAt);
    }

    private PasiEmployeeContribution mapEmployeeContribution(
            UUID id,
            String tenantId,
            UUID periodRecordId,
            UUID payrollEmployeeRecordId,
            UUID employeeId,
            BigDecimal contributableSalary,
            BigDecimal employeeContribution,
            BigDecimal employerContribution,
            BigDecimal totalContribution,
            Instant createdAt
    ) {
        return new PasiEmployeeContribution(
                id,
                tenantId,
                periodRecordId,
                payrollEmployeeRecordId,
                employeeId,
                contributableSalary,
                employeeContribution,
                employerContribution,
                totalContribution,
                createdAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
