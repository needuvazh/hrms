package com.company.hrms.pasi.repository;

import com.company.hrms.pasi.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PasiRepository {

    Mono<PasiContributionRuleDto> saveContributionRule(PasiContributionRuleDto rule);

    Mono<PasiContributionRuleDto> findActiveContributionRuleByCode(String tenantId, String ruleCode);

    Mono<PasiContributionRuleDto> findDefaultActiveContributionRule(String tenantId);

    Mono<PasiPeriodRecordDto> savePeriodRecord(PasiPeriodRecordDto periodRecord);

    Mono<PasiPeriodRecordDto> updatePeriodRecord(PasiPeriodRecordDto periodRecord);

    Mono<PasiPeriodRecordDto> findPeriodRecordById(String tenantId, UUID periodRecordId);

    Flux<PasiEmployeeContributionDto> saveEmployeeContributions(Flux<PasiEmployeeContributionDto> contributions);

    Flux<PasiEmployeeContributionDto> findEmployeeContributionsByPeriod(String tenantId, UUID periodRecordId);

    Mono<Boolean> existsPeriodRecordForPayrollRun(String tenantId, UUID payrollRunId);
}
