package com.company.hrms.payroll.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollModuleApi {

    Mono<PayrollPeriodView> definePayrollPeriod(DefinePayrollPeriodCommand command);

    Mono<PayrollRunView> startPayrollRun(StartPayrollRunCommand command);

    Mono<PayslipView> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommand command);

    Mono<PayrollRunView> submitPayrollRun(SubmitPayrollRunCommand command);

    Mono<PayrollRunView> reviewPayrollRun(ReviewPayrollRunCommand command);

    Mono<PayrollRunView> lockPayrollRun(UUID payrollRunId);

    Mono<PayrollRunView> finalizePayrollRun(UUID payrollRunId);

    Mono<PayrollRunView> getPayrollRun(UUID payrollRunId);

    Flux<PayslipView> payslips(UUID payrollRunId);
}
