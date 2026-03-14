package com.company.hrms.recruitment.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecruitmentModuleApi {

    Mono<CandidateView> createCandidate(CreateCandidateCommand command);

    Mono<CandidateView> getCandidate(UUID candidateId);

    Flux<CandidateView> searchCandidates(CandidateSearchQuery query);

    Mono<CandidateView> updateCandidateStatus(UpdateCandidateStatusCommand command);

    Mono<CandidateView> hireCandidate(HireCandidateCommand command);
}
