package com.company.hrms.payroll.service.impl;

import com.company.hrms.payroll.model.*;
import com.company.hrms.payroll.repository.*;
import com.company.hrms.payroll.service.*;

import com.company.hrms.payroll.model.AttachPayrollEmployeeRecordCommandDto;
import com.company.hrms.payroll.model.DefinePayrollPeriodCommandDto;
import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.payroll.service.PayrollModuleClient;
import com.company.hrms.payroll.model.PayrollPeriodViewDto;
import com.company.hrms.payroll.model.PayrollRunViewDto;
import com.company.hrms.payroll.model.PayslipViewDto;
import com.company.hrms.payroll.model.ReviewPayrollRunCommandDto;
import com.company.hrms.payroll.model.StartPayrollRunCommandDto;
import com.company.hrms.payroll.model.SubmitPayrollRunCommandDto;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalPayrollModuleClient implements PayrollModuleClient {

    private final PayrollModuleApi delegate;

    public LocalPayrollModuleClient(PayrollModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<PayrollPeriodViewDto> definePayrollPeriod(DefinePayrollPeriodCommandDto command) {
        return delegate.definePayrollPeriod(command);
    }

    @Override
    public Mono<PayrollRunViewDto> startPayrollRun(StartPayrollRunCommandDto command) {
        return delegate.startPayrollRun(command);
    }

    @Override
    public Mono<PayslipViewDto> attachEmployeePayrollRecord(AttachPayrollEmployeeRecordCommandDto command) {
        return delegate.attachEmployeePayrollRecord(command);
    }

    @Override
    public Mono<PayrollRunViewDto> submitPayrollRun(SubmitPayrollRunCommandDto command) {
        return delegate.submitPayrollRun(command);
    }

    @Override
    public Mono<PayrollRunViewDto> reviewPayrollRun(ReviewPayrollRunCommandDto command) {
        return delegate.reviewPayrollRun(command);
    }

    @Override
    public Mono<PayrollRunViewDto> lockPayrollRun(UUID payrollRunId) {
        return delegate.lockPayrollRun(payrollRunId);
    }

    @Override
    public Mono<PayrollRunViewDto> finalizePayrollRun(UUID payrollRunId) {
        return delegate.finalizePayrollRun(payrollRunId);
    }

    @Override
    public Mono<PayrollRunViewDto> getPayrollRun(UUID payrollRunId) {
        return delegate.getPayrollRun(payrollRunId);
    }

    @Override
    public Flux<PayslipViewDto> payslips(UUID payrollRunId) {
        return delegate.payslips(payrollRunId);
    }
}
