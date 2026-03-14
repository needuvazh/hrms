package com.company.hrms.reporting.service.impl;

import com.company.hrms.reporting.model.*;
import com.company.hrms.reporting.repository.*;
import com.company.hrms.reporting.service.*;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import com.company.hrms.reporting.model.ReportDateRangeQueryDto;
import com.company.hrms.reporting.repository.ReportingRepository;
import com.company.hrms.reporting.service.ReportingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReportingServiceImpl implements ReportingService {

    private final ReportingRepository reportingRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public ReportingServiceImpl(
            ReportingRepository reportingRepository,
            TenantContextAccessor tenantContextAccessor
    ) {
        this.reportingRepository = reportingRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Mono<EmployeeSummaryReportViewDto> employeeSummary() {
        return requireTenant().flatMap(reportingRepository::employeeSummary);
    }

    @Override
    public Mono<AttendanceSummaryReportViewDto> attendanceSummary(ReportDateRangeQueryDto query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingRepository.attendanceSummary(tenantId, query.fromDate(), query.toDate()));
    }

    @Override
    public Mono<LeaveSummaryReportViewDto> leaveSummary(ReportDateRangeQueryDto query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingRepository.leaveSummary(tenantId, query.fromDate(), query.toDate()));
    }

    @Override
    public Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(ReportDateRangeQueryDto query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingRepository.payrollRunSummary(tenantId, query.fromDate(), query.toDate()));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateRange(ReportDateRangeQueryDto query) {
        if (query.fromDate() == null || query.toDate() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_REQUIRED", "fromDate and toDate are required");
        }
        if (query.fromDate().isAfter(query.toDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_INVALID", "fromDate must be on or before toDate");
        }
    }
}
