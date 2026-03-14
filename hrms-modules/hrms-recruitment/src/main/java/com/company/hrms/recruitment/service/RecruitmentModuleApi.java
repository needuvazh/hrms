package com.company.hrms.recruitment.service;

import com.company.hrms.recruitment.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecruitmentModuleApi {

    Mono<CandidateViewDto> createCandidate(CreateCandidateCommandDto command);

    Mono<CandidateViewDto> getCandidate(UUID candidateId);

    Flux<CandidateViewDto> searchCandidates(CandidateSearchQueryDto query);

    Mono<CandidateViewDto> updateCandidateStatus(UpdateCandidateStatusCommandDto command);

    Mono<CandidateViewDto> hireCandidate(HireCandidateCommandDto command);
}
