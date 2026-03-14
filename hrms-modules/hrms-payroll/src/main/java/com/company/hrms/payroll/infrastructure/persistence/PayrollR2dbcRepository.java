package com.company.hrms.payroll.infrastructure.persistence;

import com.company.hrms.payroll.domain.DeductionComponent;
import com.company.hrms.payroll.domain.EarningComponent;
import com.company.hrms.payroll.domain.PayrollEmployeeRecord;
import com.company.hrms.payroll.domain.PayrollPeriod;
import com.company.hrms.payroll.domain.PayrollPeriodStatus;
import com.company.hrms.payroll.domain.PayrollRepository;
import com.company.hrms.payroll.domain.PayrollRun;
import com.company.hrms.payroll.domain.PayrollRunStatus;
import com.company.hrms.payroll.domain.Payslip;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PayrollR2dbcRepository implements PayrollRepository {

    private final DatabaseClient databaseClient;

    public PayrollR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PayrollPeriod> savePayrollPeriod(PayrollPeriod payrollPeriod) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO payroll.payroll_periods(
                            id, tenant_id, period_code, start_date, end_date, status, description, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :periodCode, :startDate, :endDate, :status, :description, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, period_code, start_date, end_date, status, description, created_at, updated_at
                        """)
                .bind("id", payrollPeriod.id())
                .bind("tenantId", payrollPeriod.tenantId())
                .bind("periodCode", payrollPeriod.periodCode())
                .bind("startDate", payrollPeriod.startDate())
                .bind("endDate", payrollPeriod.endDate())
                .bind("status", payrollPeriod.status().name())
                .bind("createdAt", payrollPeriod.createdAt())
                .bind("updatedAt", payrollPeriod.updatedAt());

        spec = bindNullable(spec, "description", payrollPeriod.description(), String.class);

        return spec.map((row, metadata) -> mapPayrollPeriod(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("period_code", String.class),
                        row.get("start_date", LocalDate.class),
                        row.get("end_date", LocalDate.class),
                        row.get("status", String.class),
                        row.get("description", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollPeriod> updatePayrollPeriod(PayrollPeriod payrollPeriod) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE payroll.payroll_periods
                        SET status = :status,
                            description = :description,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, period_code, start_date, end_date, status, description, created_at, updated_at
                        """)
                .bind("id", payrollPeriod.id())
                .bind("tenantId", payrollPeriod.tenantId())
                .bind("status", payrollPeriod.status().name())
                .bind("updatedAt", payrollPeriod.updatedAt());

        spec = bindNullable(spec, "description", payrollPeriod.description(), String.class);

        return spec.map((row, metadata) -> mapPayrollPeriod(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("period_code", String.class),
                        row.get("start_date", LocalDate.class),
                        row.get("end_date", LocalDate.class),
                        row.get("status", String.class),
                        row.get("description", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollPeriod> findPayrollPeriodById(String tenantId, UUID payrollPeriodId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, period_code, start_date, end_date, status, description, created_at, updated_at
                        FROM payroll.payroll_periods
                        WHERE tenant_id = :tenantId
                          AND id = :payrollPeriodId
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollPeriodId", payrollPeriodId)
                .map((row, metadata) -> mapPayrollPeriod(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("period_code", String.class),
                        row.get("start_date", LocalDate.class),
                        row.get("end_date", LocalDate.class),
                        row.get("status", String.class),
                        row.get("description", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollRun> savePayrollRun(PayrollRun payrollRun) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO payroll.payroll_runs(
                            id, tenant_id, payroll_period_id, status, workflow_instance_id, initiated_by,
                            submitted_by, reviewed_by, notes, submitted_at, reviewed_at, finalized_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :payrollPeriodId, :status, :workflowInstanceId, :initiatedBy,
                            :submittedBy, :reviewedBy, :notes, :submittedAt, :reviewedAt, :finalizedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, payroll_period_id, status, workflow_instance_id, initiated_by,
                                  submitted_by, reviewed_by, notes, submitted_at, reviewed_at, finalized_at, created_at, updated_at
                        """)
                .bind("id", payrollRun.id())
                .bind("tenantId", payrollRun.tenantId())
                .bind("payrollPeriodId", payrollRun.payrollPeriodId())
                .bind("status", payrollRun.status().name())
                .bind("initiatedBy", payrollRun.initiatedBy())
                .bind("createdAt", payrollRun.createdAt())
                .bind("updatedAt", payrollRun.updatedAt());

        spec = bindNullable(spec, "workflowInstanceId", payrollRun.workflowInstanceId(), UUID.class);
        spec = bindNullable(spec, "submittedBy", payrollRun.submittedBy(), String.class);
        spec = bindNullable(spec, "reviewedBy", payrollRun.reviewedBy(), String.class);
        spec = bindNullable(spec, "notes", payrollRun.notes(), String.class);
        spec = bindNullable(spec, "submittedAt", payrollRun.submittedAt(), Instant.class);
        spec = bindNullable(spec, "reviewedAt", payrollRun.reviewedAt(), Instant.class);
        spec = bindNullable(spec, "finalizedAt", payrollRun.finalizedAt(), Instant.class);

        return spec.map((row, metadata) -> mapPayrollRun(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_period_id", UUID.class),
                        row.get("status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("initiated_by", String.class),
                        row.get("submitted_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("notes", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("reviewed_at", Instant.class),
                        row.get("finalized_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollRun> updatePayrollRun(PayrollRun payrollRun) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE payroll.payroll_runs
                        SET status = :status,
                            workflow_instance_id = :workflowInstanceId,
                            submitted_by = :submittedBy,
                            reviewed_by = :reviewedBy,
                            notes = :notes,
                            submitted_at = :submittedAt,
                            reviewed_at = :reviewedAt,
                            finalized_at = :finalizedAt,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, payroll_period_id, status, workflow_instance_id, initiated_by,
                                  submitted_by, reviewed_by, notes, submitted_at, reviewed_at, finalized_at, created_at, updated_at
                        """)
                .bind("id", payrollRun.id())
                .bind("tenantId", payrollRun.tenantId())
                .bind("status", payrollRun.status().name())
                .bind("updatedAt", payrollRun.updatedAt());

        spec = bindNullable(spec, "workflowInstanceId", payrollRun.workflowInstanceId(), UUID.class);
        spec = bindNullable(spec, "submittedBy", payrollRun.submittedBy(), String.class);
        spec = bindNullable(spec, "reviewedBy", payrollRun.reviewedBy(), String.class);
        spec = bindNullable(spec, "notes", payrollRun.notes(), String.class);
        spec = bindNullable(spec, "submittedAt", payrollRun.submittedAt(), Instant.class);
        spec = bindNullable(spec, "reviewedAt", payrollRun.reviewedAt(), Instant.class);
        spec = bindNullable(spec, "finalizedAt", payrollRun.finalizedAt(), Instant.class);

        return spec.map((row, metadata) -> mapPayrollRun(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_period_id", UUID.class),
                        row.get("status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("initiated_by", String.class),
                        row.get("submitted_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("notes", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("reviewed_at", Instant.class),
                        row.get("finalized_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollRun> findPayrollRunById(String tenantId, UUID payrollRunId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, payroll_period_id, status, workflow_instance_id, initiated_by,
                               submitted_by, reviewed_by, notes, submitted_at, reviewed_at, finalized_at, created_at, updated_at
                        FROM payroll.payroll_runs
                        WHERE tenant_id = :tenantId
                          AND id = :payrollRunId
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollRunId", payrollRunId)
                .map((row, metadata) -> mapPayrollRun(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_period_id", UUID.class),
                        row.get("status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("initiated_by", String.class),
                        row.get("submitted_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("notes", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("reviewed_at", Instant.class),
                        row.get("finalized_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PayrollEmployeeRecord> savePayrollEmployeeRecord(PayrollEmployeeRecord payrollEmployeeRecord) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO payroll.payroll_employee_records(
                            id, tenant_id, payroll_run_id, employee_id, gross_amount,
                            total_deduction_amount, net_amount, remarks, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :payrollRunId, :employeeId, :grossAmount,
                            :totalDeductionAmount, :netAmount, :remarks, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, payroll_run_id, employee_id, gross_amount,
                                  total_deduction_amount, net_amount, remarks, created_at, updated_at
                        """)
                .bind("id", payrollEmployeeRecord.id())
                .bind("tenantId", payrollEmployeeRecord.tenantId())
                .bind("payrollRunId", payrollEmployeeRecord.payrollRunId())
                .bind("employeeId", payrollEmployeeRecord.employeeId())
                .bind("grossAmount", payrollEmployeeRecord.grossAmount())
                .bind("totalDeductionAmount", payrollEmployeeRecord.totalDeductionAmount())
                .bind("netAmount", payrollEmployeeRecord.netAmount())
                .bind("createdAt", payrollEmployeeRecord.createdAt())
                .bind("updatedAt", payrollEmployeeRecord.updatedAt());

        spec = bindNullable(spec, "remarks", payrollEmployeeRecord.remarks(), String.class);

        return spec.map((row, metadata) -> mapPayrollEmployeeRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("gross_amount", BigDecimal.class),
                        row.get("total_deduction_amount", BigDecimal.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("remarks", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<EarningComponent> saveEarningComponents(String tenantId, Flux<EarningComponent> components) {
        return components.concatMap(component -> databaseClient.sql("""
                        INSERT INTO payroll.payroll_components(
                            id, tenant_id, payroll_employee_record_id, component_type, component_code,
                            component_name, amount, created_at
                        ) VALUES (
                            :id, :tenantId, :payrollEmployeeRecordId, :componentType, :componentCode,
                            :componentName, :amount, :createdAt
                        )
                        RETURNING id, payroll_employee_record_id, component_code, component_name, amount, created_at
                        """)
                .bind("id", component.id())
                .bind("tenantId", tenantId)
                .bind("payrollEmployeeRecordId", component.payrollEmployeeRecordId())
                .bind("componentType", "EARNING")
                .bind("componentCode", component.code())
                .bind("componentName", component.name())
                .bind("amount", component.amount())
                .bind("createdAt", component.createdAt())
                .map((row, metadata) -> new EarningComponent(
                        row.get("id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("component_code", String.class),
                        row.get("component_name", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .one());
    }

    @Override
    public Flux<DeductionComponent> saveDeductionComponents(String tenantId, Flux<DeductionComponent> components) {
        return components.concatMap(component -> databaseClient.sql("""
                        INSERT INTO payroll.payroll_components(
                            id, tenant_id, payroll_employee_record_id, component_type, component_code,
                            component_name, amount, created_at
                        ) VALUES (
                            :id, :tenantId, :payrollEmployeeRecordId, :componentType, :componentCode,
                            :componentName, :amount, :createdAt
                        )
                        RETURNING id, payroll_employee_record_id, component_code, component_name, amount, created_at
                        """)
                .bind("id", component.id())
                .bind("tenantId", tenantId)
                .bind("payrollEmployeeRecordId", component.payrollEmployeeRecordId())
                .bind("componentType", "DEDUCTION")
                .bind("componentCode", component.code())
                .bind("componentName", component.name())
                .bind("amount", component.amount())
                .bind("createdAt", component.createdAt())
                .map((row, metadata) -> new DeductionComponent(
                        row.get("id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("component_code", String.class),
                        row.get("component_name", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .one());
    }

    @Override
    public Mono<Payslip> savePayslip(Payslip payslip) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO payroll.payslips(
                            id, tenant_id, payroll_run_id, payroll_employee_record_id, employee_id,
                            document_record_id, artifact_object_key, artifact_content_type,
                            gross_amount, total_deduction_amount, net_amount, generated_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :payrollRunId, :payrollEmployeeRecordId, :employeeId,
                            :documentRecordId, :artifactObjectKey, :artifactContentType,
                            :grossAmount, :totalDeductionAmount, :netAmount, :generatedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, payroll_run_id, payroll_employee_record_id, employee_id,
                                  document_record_id, artifact_object_key, artifact_content_type,
                                  gross_amount, total_deduction_amount, net_amount, generated_at, created_at, updated_at
                        """)
                .bind("id", payslip.id())
                .bind("tenantId", payslip.tenantId())
                .bind("payrollRunId", payslip.payrollRunId())
                .bind("payrollEmployeeRecordId", payslip.payrollEmployeeRecordId())
                .bind("employeeId", payslip.employeeId())
                .bind("grossAmount", payslip.grossAmount())
                .bind("totalDeductionAmount", payslip.totalDeductionAmount())
                .bind("netAmount", payslip.netAmount())
                .bind("generatedAt", payslip.generatedAt())
                .bind("createdAt", payslip.createdAt())
                .bind("updatedAt", payslip.updatedAt());

        spec = bindNullable(spec, "documentRecordId", payslip.documentRecordId(), UUID.class);
        spec = bindNullable(spec, "artifactObjectKey", payslip.artifactObjectKey(), String.class);
        spec = bindNullable(spec, "artifactContentType", payslip.artifactContentType(), String.class);

        return spec.map((row, metadata) -> mapPayslip(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("document_record_id", UUID.class),
                        row.get("artifact_object_key", String.class),
                        row.get("artifact_content_type", String.class),
                        row.get("gross_amount", BigDecimal.class),
                        row.get("total_deduction_amount", BigDecimal.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("generated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Payslip> updatePayslip(Payslip payslip) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE payroll.payslips
                        SET document_record_id = :documentRecordId,
                            artifact_object_key = :artifactObjectKey,
                            artifact_content_type = :artifactContentType,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, payroll_run_id, payroll_employee_record_id, employee_id,
                                  document_record_id, artifact_object_key, artifact_content_type,
                                  gross_amount, total_deduction_amount, net_amount, generated_at, created_at, updated_at
                        """)
                .bind("id", payslip.id())
                .bind("tenantId", payslip.tenantId())
                .bind("updatedAt", payslip.updatedAt());

        spec = bindNullable(spec, "documentRecordId", payslip.documentRecordId(), UUID.class);
        spec = bindNullable(spec, "artifactObjectKey", payslip.artifactObjectKey(), String.class);
        spec = bindNullable(spec, "artifactContentType", payslip.artifactContentType(), String.class);

        return spec.map((row, metadata) -> mapPayslip(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("document_record_id", UUID.class),
                        row.get("artifact_object_key", String.class),
                        row.get("artifact_content_type", String.class),
                        row.get("gross_amount", BigDecimal.class),
                        row.get("total_deduction_amount", BigDecimal.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("generated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<Payslip> findPayslipsByPayrollRunId(String tenantId, UUID payrollRunId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, payroll_run_id, payroll_employee_record_id, employee_id,
                               document_record_id, artifact_object_key, artifact_content_type,
                               gross_amount, total_deduction_amount, net_amount, generated_at, created_at, updated_at
                        FROM payroll.payslips
                        WHERE tenant_id = :tenantId
                          AND payroll_run_id = :payrollRunId
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollRunId", payrollRunId)
                .map((row, metadata) -> mapPayslip(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("document_record_id", UUID.class),
                        row.get("artifact_object_key", String.class),
                        row.get("artifact_content_type", String.class),
                        row.get("gross_amount", BigDecimal.class),
                        row.get("total_deduction_amount", BigDecimal.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("generated_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Flux<EarningComponent> findEarningComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId) {
        return databaseClient.sql("""
                        SELECT id, payroll_employee_record_id, component_code, component_name, amount, created_at
                        FROM payroll.payroll_components
                        WHERE tenant_id = :tenantId
                          AND payroll_employee_record_id = :payrollEmployeeRecordId
                          AND component_type = 'EARNING'
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollEmployeeRecordId", payrollEmployeeRecordId)
                .map((row, metadata) -> new EarningComponent(
                        row.get("id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("component_code", String.class),
                        row.get("component_name", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .all();
    }

    @Override
    public Flux<DeductionComponent> findDeductionComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId) {
        return databaseClient.sql("""
                        SELECT id, payroll_employee_record_id, component_code, component_name, amount, created_at
                        FROM payroll.payroll_components
                        WHERE tenant_id = :tenantId
                          AND payroll_employee_record_id = :payrollEmployeeRecordId
                          AND component_type = 'DEDUCTION'
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollEmployeeRecordId", payrollEmployeeRecordId)
                .map((row, metadata) -> new DeductionComponent(
                        row.get("id", UUID.class),
                        row.get("payroll_employee_record_id", UUID.class),
                        row.get("component_code", String.class),
                        row.get("component_name", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("created_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<PayrollEmployeeRecord> findPayrollEmployeeRecordById(String tenantId, UUID payrollEmployeeRecordId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, payroll_run_id, employee_id, gross_amount,
                               total_deduction_amount, net_amount, remarks, created_at, updated_at
                        FROM payroll.payroll_employee_records
                        WHERE tenant_id = :tenantId
                          AND id = :payrollEmployeeRecordId
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollEmployeeRecordId", payrollEmployeeRecordId)
                .map((row, metadata) -> mapPayrollEmployeeRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("gross_amount", BigDecimal.class),
                        row.get("total_deduction_amount", BigDecimal.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("remarks", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Boolean> existsPayrollRunRecordForEmployee(String tenantId, UUID payrollRunId, UUID employeeId) {
        return databaseClient.sql("""
                        SELECT COUNT(*) AS total
                        FROM payroll.payroll_employee_records
                        WHERE tenant_id = :tenantId
                          AND payroll_run_id = :payrollRunId
                          AND employee_id = :employeeId
                        """)
                .bind("tenantId", tenantId)
                .bind("payrollRunId", payrollRunId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .map(total -> total != null && total > 0)
                .defaultIfEmpty(false);
    }

    private PayrollPeriod mapPayrollPeriod(
            UUID id,
            String tenantId,
            String periodCode,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PayrollPeriod(
                id,
                tenantId,
                periodCode,
                startDate,
                endDate,
                PayrollPeriodStatus.valueOf(status),
                description,
                createdAt,
                updatedAt);
    }

    private PayrollRun mapPayrollRun(
            UUID id,
            String tenantId,
            UUID payrollPeriodId,
            String status,
            UUID workflowInstanceId,
            String initiatedBy,
            String submittedBy,
            String reviewedBy,
            String notes,
            Instant submittedAt,
            Instant reviewedAt,
            Instant finalizedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PayrollRun(
                id,
                tenantId,
                payrollPeriodId,
                PayrollRunStatus.valueOf(status),
                workflowInstanceId,
                initiatedBy,
                submittedBy,
                reviewedBy,
                notes,
                submittedAt,
                reviewedAt,
                finalizedAt,
                createdAt,
                updatedAt);
    }

    private PayrollEmployeeRecord mapPayrollEmployeeRecord(
            UUID id,
            String tenantId,
            UUID payrollRunId,
            UUID employeeId,
            BigDecimal grossAmount,
            BigDecimal totalDeductionAmount,
            BigDecimal netAmount,
            String remarks,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PayrollEmployeeRecord(
                id,
                tenantId,
                payrollRunId,
                employeeId,
                grossAmount,
                totalDeductionAmount,
                netAmount,
                remarks,
                createdAt,
                updatedAt);
    }

    private Payslip mapPayslip(
            UUID id,
            String tenantId,
            UUID payrollRunId,
            UUID payrollEmployeeRecordId,
            UUID employeeId,
            UUID documentRecordId,
            String artifactObjectKey,
            String artifactContentType,
            BigDecimal grossAmount,
            BigDecimal totalDeductionAmount,
            BigDecimal netAmount,
            Instant generatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Payslip(
                id,
                tenantId,
                payrollRunId,
                payrollEmployeeRecordId,
                employeeId,
                documentRecordId,
                artifactObjectKey,
                artifactContentType,
                grossAmount,
                totalDeductionAmount,
                netAmount,
                generatedAt,
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
