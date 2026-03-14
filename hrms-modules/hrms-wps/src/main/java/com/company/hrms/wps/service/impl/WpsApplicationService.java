package com.company.hrms.wps.service.impl;

import com.company.hrms.wps.model.*;
import com.company.hrms.wps.repository.*;
import com.company.hrms.wps.service.*;

import com.company.hrms.contracts.payroll.PayrollRunStatus;
import com.company.hrms.contracts.payroll.PayrollRunViewDto;
import com.company.hrms.contracts.payroll.PayslipViewDto;
import com.company.hrms.payroll.service.PayrollModuleApi;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.wps.model.CreateWpsBatchCommandDto;
import com.company.hrms.wps.model.GenerateWpsExportCommandDto;
import com.company.hrms.wps.model.MarkWpsExportedCommandDto;
import com.company.hrms.wps.model.WpsBatchViewDto;
import com.company.hrms.wps.model.WpsEmployeeEntryViewDto;
import com.company.hrms.wps.model.WpsExportFileViewDto;
import com.company.hrms.wps.service.WpsModuleApi;
import com.company.hrms.wps.model.WpsBatchDto;
import com.company.hrms.wps.model.WpsEmployeeEntryDto;
import com.company.hrms.wps.model.WpsExportFileDto;
import com.company.hrms.wps.model.WpsExportFormatter;
import com.company.hrms.wps.model.WpsExportPayloadDto;
import com.company.hrms.wps.model.WpsPayrollValidator;
import com.company.hrms.wps.repository.WpsRepository;
import com.company.hrms.wps.model.WpsStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WpsApplicationService implements WpsModuleApi {

    private static final String DEFAULT_EXPORT_TYPE = "WPS-TXT";

    private final WpsRepository wpsRepository;
    private final PayrollModuleApi payrollModuleApi;
    private final TenantContextAccessor tenantContextAccessor;
    private final WpsPayrollValidator wpsPayrollValidator;
    private final Map<String, WpsExportFormatter> formatterByType;

    public WpsApplicationService(
            WpsRepository wpsRepository,
            PayrollModuleApi payrollModuleApi,
            TenantContextAccessor tenantContextAccessor,
            WpsPayrollValidator wpsPayrollValidator,
            List<WpsExportFormatter> wpsExportFormatters
    ) {
        this.wpsRepository = wpsRepository;
        this.payrollModuleApi = payrollModuleApi;
        this.tenantContextAccessor = tenantContextAccessor;
        this.wpsPayrollValidator = wpsPayrollValidator;
        this.formatterByType = wpsExportFormatters.stream()
                .collect(Collectors.toMap(formatter -> formatter.type().toUpperCase(), Function.identity()));
    }

    @Override
    public Mono<WpsBatchViewDto> createBatchFromPayrollRun(CreateWpsBatchCommandDto command) {
        validateCreateBatch(command);

        return requireTenant()
                .flatMap(tenantId -> payrollModuleApi.getPayrollRun(command.payrollRunId())
                        .flatMap(run -> validateFinalizedPayrollRun(run)
                                .then(payrollModuleApi.payslips(run.id()).collectList())
                                .flatMap(payslips -> buildBatchFromPayslips(tenantId, run, command.createdBy().trim(), payslips)))
                        .map(this::toBatchView));
    }

    @Override
    public Mono<WpsExportFileViewDto> generateExport(GenerateWpsExportCommandDto command) {
        validateGenerateExport(command);

        String exportType = StringUtils.hasText(command.exportType())
                ? command.exportType().trim().toUpperCase()
                : DEFAULT_EXPORT_TYPE;

        return requireTenant()
                .flatMap(tenantId -> wpsRepository.findBatchById(tenantId, command.batchId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WPS_BATCH_NOT_FOUND", "WPS batch not found")))
                        .flatMap(batch -> {
                            if (batch.status() != WpsStatus.VALIDATED && batch.status() != WpsStatus.GENERATED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_NOT_READY", "WPS batch is not ready for export"));
                            }

                            WpsExportFormatter formatter = formatterByType.get(exportType);
                            if (formatter == null) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_EXPORT_FORMAT_UNSUPPORTED", "Unsupported WPS export type: " + exportType));
                            }

                            return wpsRepository.findEntriesByBatchId(tenantId, batch.id())
                                    .collectList()
                                    .flatMap(entries -> {
                                        if (entries.isEmpty()) {
                                            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_ENTRIES_REQUIRED", "No WPS entries available for export"));
                                        }

                                        Instant now = Instant.now();
                                        WpsExportPayloadDto payload = formatter.format(batch, entries);
                                        WpsExportFileDto exportFile = new WpsExportFileDto(
                                                UUID.randomUUID(),
                                                tenantId,
                                                batch.id(),
                                                exportType,
                                                payload.fileName(),
                                                payload.contentType(),
                                                payload.contentHash(),
                                                payload.payload(),
                                                WpsStatus.GENERATED,
                                                now,
                                                now);

                                        WpsBatchDto generatedBatch = batch.generated(now);
                                        return wpsRepository.saveExportFile(exportFile)
                                                .flatMap(saved -> wpsRepository.updateBatch(generatedBatch)
                                                        .thenReturn(saved));
                                    });
                        })
                        .map(this::toExportFileView));
    }

    @Override
    public Mono<WpsBatchViewDto> markExported(MarkWpsExportedCommandDto command) {
        validateMarkExported(command);

        return requireTenant()
                .flatMap(tenantId -> wpsRepository.findBatchById(tenantId, command.batchId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WPS_BATCH_NOT_FOUND", "WPS batch not found")))
                        .flatMap(batch -> {
                            if (batch.status() != WpsStatus.GENERATED && batch.status() != WpsStatus.EXPORTED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_NOT_GENERATED", "WPS export has not been generated"));
                            }
                            return wpsRepository.updateBatch(batch.exported(command.exportedBy().trim(), Instant.now()));
                        })
                        .map(this::toBatchView));
    }

    @Override
    public Mono<WpsBatchViewDto> getBatch(UUID batchId) {
        if (batchId == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_REQUIRED", "WPS batch id is required"));
        }

        return requireTenant()
                .flatMap(tenantId -> wpsRepository.findBatchById(tenantId, batchId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WPS_BATCH_NOT_FOUND", "WPS batch not found")))
                        .map(this::toBatchView));
    }

    @Override
    public Flux<WpsEmployeeEntryViewDto> entries(UUID batchId) {
        if (batchId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_REQUIRED", "WPS batch id is required"));
        }

        return requireTenant()
                .flatMapMany(tenantId -> wpsRepository.findEntriesByBatchId(tenantId, batchId).map(this::toEntryView));
    }

    private Mono<WpsBatchDto> buildBatchFromPayslips(String tenantId, PayrollRunViewDto run, String createdBy, List<PayslipViewDto> payslips) {
        Instant now = Instant.now();
        WpsBatchDto initialBatch = new WpsBatchDto(
                UUID.randomUUID(),
                tenantId,
                run.id(),
                WpsStatus.CREATED,
                null,
                createdBy,
                null,
                null,
                now,
                now);

        return wpsRepository.saveBatch(initialBatch)
                .flatMap(savedBatch -> {
                    var validation = wpsPayrollValidator.validate(payslips);
                    if (!validation.valid()) {
                        return wpsRepository.updateBatch(savedBatch.failed(validation.summary(), Instant.now()))
                                .then(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WPS_VALIDATION_FAILED", validation.summary())));
                    }

                    return Flux.fromIterable(payslips)
                            .concatMap(payslip -> wpsRepository.saveEntry(new WpsEmployeeEntryDto(
                                    UUID.randomUUID(),
                                    tenantId,
                                    savedBatch.id(),
                                    payslip.payrollEmployeeRecord().employeeId(),
                                    payslip.payrollEmployeeRecord().netAmount(),
                                    "PAY-" + payslip.payrollEmployeeRecord().employeeId(),
                                    Instant.now())))
                            .then(wpsRepository.updateBatch(savedBatch.validated(validation.summary(), Instant.now())));
                });
    }

    private Mono<Void> validateFinalizedPayrollRun(PayrollRunViewDto run) {
        if (run.status() != PayrollRunStatus.FINALIZED) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_NOT_FINALIZED", "Payroll run must be finalized for WPS batch creation"));
        }
        return Mono.empty();
    }

    private void validateCreateBatch(CreateWpsBatchCommandDto command) {
        if (command.payrollRunId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PAYROLL_RUN_REQUIRED", "Payroll run id is required");
        }
        if (!StringUtils.hasText(command.createdBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CREATED_BY_REQUIRED", "Created by is required");
        }
    }

    private void validateGenerateExport(GenerateWpsExportCommandDto command) {
        if (command.batchId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_REQUIRED", "WPS batch id is required");
        }
        if (!StringUtils.hasText(command.generatedBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "GENERATED_BY_REQUIRED", "Generated by is required");
        }
    }

    private void validateMarkExported(MarkWpsExportedCommandDto command) {
        if (command.batchId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WPS_BATCH_REQUIRED", "WPS batch id is required");
        }
        if (!StringUtils.hasText(command.exportedBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EXPORTED_BY_REQUIRED", "Exported by is required");
        }
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private WpsBatchViewDto toBatchView(WpsBatchDto batch) {
        return new WpsBatchViewDto(
                batch.id(),
                batch.tenantId(),
                batch.payrollRunId(),
                batch.status(),
                batch.validationSummary(),
                batch.createdBy(),
                batch.exportedBy(),
                batch.exportedAt(),
                batch.createdAt(),
                batch.updatedAt());
    }

    private WpsEmployeeEntryViewDto toEntryView(WpsEmployeeEntryDto entry) {
        return new WpsEmployeeEntryViewDto(
                entry.id(),
                entry.tenantId(),
                entry.wpsBatchId(),
                entry.employeeId(),
                entry.netAmount(),
                entry.paymentReference(),
                entry.createdAt());
    }

    private WpsExportFileViewDto toExportFileView(WpsExportFileDto exportFile) {
        return new WpsExportFileViewDto(
                exportFile.id(),
                exportFile.tenantId(),
                exportFile.wpsBatchId(),
                exportFile.exportType(),
                exportFile.fileName(),
                exportFile.contentType(),
                exportFile.contentHash(),
                exportFile.payload(),
                exportFile.status(),
                exportFile.createdAt(),
                exportFile.updatedAt());
    }
}
