package com.company.hrms.payroll.service;

import com.company.hrms.payroll.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollModuleApi {

    Mono<PayrollPeriodViewDto> definePayrollPeriod(DefinePayrollPeriodCommandDto command);

    Mono<PayrollRunViewDto> startPayrollRun(StartPayrollRunCommandDto command);

    Mono<PayslipViewDto> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommandDto command);

    Mono<PayrollRunViewDto> submitPayrollRun(SubmitPayrollRunCommandDto command);

    Mono<PayrollRunViewDto> reviewPayrollRun(ReviewPayrollRunCommandDto command);

    Mono<PayrollRunViewDto> lockPayrollRun(UUID payrollRunId);

    Mono<PayrollRunViewDto> finalizePayrollRun(UUID payrollRunId);

    Mono<PayrollRunViewDto> getPayrollRun(UUID payrollRunId);

    Flux<PayslipViewDto> payslips(UUID payrollRunId);
}
