package com.company.hrms.pasi.service;

import com.company.hrms.pasi.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PasiModuleApi {

    Mono<PasiContributionRuleViewDto> defineContributionRule(DefinePasiContributionRuleCommandDto command);

    Mono<PasiPeriodRecordViewDto> computeContributions(ComputePasiContributionCommandDto command);

    Flux<PasiEmployeeContributionViewDto> contributionsByPeriod(UUID pasiPeriodRecordId);

    Mono<PasiPeriodRecordViewDto> getPeriodRecord(UUID pasiPeriodRecordId);
}
