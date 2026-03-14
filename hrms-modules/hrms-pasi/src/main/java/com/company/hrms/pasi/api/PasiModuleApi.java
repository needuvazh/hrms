package com.company.hrms.pasi.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PasiModuleApi {

    Mono<PasiContributionRuleView> defineContributionRule(DefinePasiContributionRuleCommand command);

    Mono<PasiPeriodRecordView> computeContributions(ComputePasiContributionCommand command);

    Flux<PasiEmployeeContributionView> contributionsByPeriod(UUID pasiPeriodRecordId);

    Mono<PasiPeriodRecordView> getPeriodRecord(UUID pasiPeriodRecordId);
}
