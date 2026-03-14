package com.company.hrms.reporting.application;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.reporting.api.AttendanceSummaryReportView;
import com.company.hrms.reporting.api.EmployeeSummaryReportView;
import com.company.hrms.reporting.api.LeaveSummaryReportView;
import com.company.hrms.reporting.api.PayrollRunSummaryReportView;
import com.company.hrms.reporting.api.ReportDateRangeQuery;
import com.company.hrms.reporting.api.ReportingModuleApi;
import com.company.hrms.reporting.domain.ReportingQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReportingApplicationService implements ReportingModuleApi {

    private final ReportingQueryRepository reportingQueryRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public ReportingApplicationService(
            ReportingQueryRepository reportingQueryRepository,
            TenantContextAccessor tenantContextAccessor
    ) {
        this.reportingQueryRepository = reportingQueryRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Mono<EmployeeSummaryReportView> employeeSummary() {
        return requireTenant().flatMap(reportingQueryRepository::employeeSummary);
    }

    @Override
    public Mono<AttendanceSummaryReportView> attendanceSummary(ReportDateRangeQuery query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingQueryRepository.attendanceSummary(tenantId, query.fromDate(), query.toDate()));
    }

    @Override
    public Mono<LeaveSummaryReportView> leaveSummary(ReportDateRangeQuery query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingQueryRepository.leaveSummary(tenantId, query.fromDate(), query.toDate()));
    }

    @Override
    public Mono<PayrollRunSummaryReportView> payrollRunSummary(ReportDateRangeQuery query) {
        validateRange(query);
        return requireTenant().flatMap(tenantId -> reportingQueryRepository.payrollRunSummary(tenantId, query.fromDate(), query.toDate()));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateRange(ReportDateRangeQuery query) {
        if (query.fromDate() == null || query.toDate() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_REQUIRED", "fromDate and toDate are required");
        }
        if (query.fromDate().isAfter(query.toDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_INVALID", "fromDate must be on or before toDate");
        }
    }
}
