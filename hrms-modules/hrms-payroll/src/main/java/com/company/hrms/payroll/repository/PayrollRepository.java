package com.company.hrms.payroll.repository;

import com.company.hrms.payroll.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollRepository {

    Mono<PayrollPeriodDto> savePayrollPeriod(PayrollPeriodDto payrollPeriod);

    Mono<PayrollPeriodDto> updatePayrollPeriod(PayrollPeriodDto payrollPeriod);

    Mono<PayrollPeriodDto> findPayrollPeriodById(String tenantId, UUID payrollPeriodId);

    Mono<PayrollRunDto> savePayrollRun(PayrollRunDto payrollRun);

    Mono<PayrollRunDto> updatePayrollRun(PayrollRunDto payrollRun);

    Mono<PayrollRunDto> findPayrollRunById(String tenantId, UUID payrollRunId);

    Mono<PayrollEmployeeRecordDto> savePayrollEmployeeRecord(PayrollEmployeeRecordDto payrollEmployeeRecord);

    Flux<EarningComponentDto> saveEarningComponents(String tenantId, Flux<EarningComponentDto> components);

    Flux<DeductionComponentDto> saveDeductionComponents(String tenantId, Flux<DeductionComponentDto> components);

    Mono<PayslipDto> savePayslip(PayslipDto payslip);

    Mono<PayslipDto> updatePayslip(PayslipDto payslip);

    Flux<PayslipDto> findPayslipsByPayrollRunId(String tenantId, UUID payrollRunId);

    Flux<EarningComponentDto> findEarningComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId);

    Flux<DeductionComponentDto> findDeductionComponentsByPayrollEmployeeRecordId(String tenantId, UUID payrollEmployeeRecordId);

    Mono<PayrollEmployeeRecordDto> findPayrollEmployeeRecordById(String tenantId, UUID payrollEmployeeRecordId);

    Mono<Boolean> existsPayrollRunRecordForEmployee(String tenantId, UUID payrollRunId, UUID employeeId);
}
