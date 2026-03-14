package com.company.hrms.payroll.infrastructure.client;

import com.company.hrms.payroll.api.AttachPayrollEmployeeRecordCommand;
import com.company.hrms.payroll.api.DefinePayrollPeriodCommand;
import com.company.hrms.payroll.api.PayrollModuleApi;
import com.company.hrms.payroll.api.PayrollModuleClient;
import com.company.hrms.payroll.api.PayrollPeriodView;
import com.company.hrms.payroll.api.PayrollRunView;
import com.company.hrms.payroll.api.PayslipView;
import com.company.hrms.payroll.api.ReviewPayrollRunCommand;
import com.company.hrms.payroll.api.StartPayrollRunCommand;
import com.company.hrms.payroll.api.SubmitPayrollRunCommand;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalPayrollModuleClient implements PayrollModuleClient {

    private final PayrollModuleApi delegate;

    public LocalPayrollModuleClient(PayrollModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<PayrollPeriodView> definePayrollPeriod(DefinePayrollPeriodCommand command) {
        return delegate.definePayrollPeriod(command);
    }

    @Override
    public Mono<PayrollRunView> startPayrollRun(StartPayrollRunCommand command) {
        return delegate.startPayrollRun(command);
    }

    @Override
    public Mono<PayslipView> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommand command) {
        return delegate.attachEmployeePayrollRecord(command);
    }

    @Override
    public Mono<PayrollRunView> submitPayrollRun(SubmitPayrollRunCommand command) {
        return delegate.submitPayrollRun(command);
    }

    @Override
    public Mono<PayrollRunView> reviewPayrollRun(ReviewPayrollRunCommand command) {
        return delegate.reviewPayrollRun(command);
    }

    @Override
    public Mono<PayrollRunView> lockPayrollRun(UUID payrollRunId) {
        return delegate.lockPayrollRun(payrollRunId);
    }

    @Override
    public Mono<PayrollRunView> finalizePayrollRun(UUID payrollRunId) {
        return delegate.finalizePayrollRun(payrollRunId);
    }

    @Override
    public Mono<PayrollRunView> getPayrollRun(UUID payrollRunId) {
        return delegate.getPayrollRun(payrollRunId);
    }

    @Override
    public Flux<PayslipView> payslips(UUID payrollRunId) {
        return delegate.payslips(payrollRunId);
    }
}
