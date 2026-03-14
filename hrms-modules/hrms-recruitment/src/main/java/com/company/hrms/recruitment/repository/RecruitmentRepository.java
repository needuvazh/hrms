package com.company.hrms.recruitment.repository;

import com.company.hrms.recruitment.model.*;

import com.company.hrms.recruitment.model.CandidateStatus;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecruitmentRepository {

    Mono<CandidateDto> save(CandidateDto candidate);

    Mono<CandidateDto> findById(UUID candidateId, String tenantId);

    Flux<CandidateDto> search(String query, CandidateStatus status, int limit, int offset, String tenantId);

    Mono<CandidateDto> updateStatus(UUID candidateId, CandidateStatus status, String reason, String tenantId);
}
