package com.company.hrms.pasi.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PasiRepository {

    Mono<PasiContributionRule> saveContributionRule(PasiContributionRule rule);

    Mono<PasiContributionRule> findActiveContributionRuleByCode(String tenantId, String ruleCode);

    Mono<PasiContributionRule> findDefaultActiveContributionRule(String tenantId);

    Mono<PasiPeriodRecord> savePeriodRecord(PasiPeriodRecord periodRecord);

    Mono<PasiPeriodRecord> updatePeriodRecord(PasiPeriodRecord periodRecord);

    Mono<PasiPeriodRecord> findPeriodRecordById(String tenantId, UUID periodRecordId);

    Flux<PasiEmployeeContribution> saveEmployeeContributions(Flux<PasiEmployeeContribution> contributions);

    Flux<PasiEmployeeContribution> findEmployeeContributionsByPeriod(String tenantId, UUID periodRecordId);

    Mono<Boolean> existsPeriodRecordForPayrollRun(String tenantId, UUID payrollRunId);
}
