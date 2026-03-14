package com.company.hrms.payroll.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRepository {

    Mono<PayrollPeriod> savePayrollPeriod(PayrollPeriod payrollPeriod);

    Mono<PayrollPeriod> updatePayrollPeriod(PayrollPeriod payrollPeriod);

    Mono<PayrollPeriod> findPayrollPeriodById(String tenantId, UUID payrollPeriodId);

    Mono<PayrollRun> savePayrollRun(PayrollRun payrollRun);

    Mono<PayrollRun> updatePayrollRun(PayrollRun payrollRun);

    Mono<PayrollRun> findPayrollRunById(String tenantId, UUID payrollRunId);

    Mono<PayrollEmployeeRecord> savePayrollEmployeeRecord(PayrollEmployeeRecord payrollEmployeeRecord);

    Flux<EarningComponent> saveEarningComponents(String tenantId, Flux<EarningComponent> components);

    Flux<DeductionComponent> saveDeductionComponents(String tenantId, Flux<DeductionComponent> components);

    Mono<Payslip> savePayslip(Payslip payslip);

    Mono<Payslip> updatePayslip(Payslip payslip);

    Flux<Payslip> findPayslipsByPayrollRunId(String tenantId, UUID payrollRunId);

    Flux<EarningComponent> findEarningComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId);

    Flux<DeductionComponent> findDeductionComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId);

    Mono<PayrollEmployeeRecord> findPayrollEmployeeRecordById(String tenantId, UUID payrollEmployeeRecordId);

    Mono<Boolean> existsPayrollRunRecordForEmployee(String tenantId, UUID payrollRunId, UUID employeeId);
}
