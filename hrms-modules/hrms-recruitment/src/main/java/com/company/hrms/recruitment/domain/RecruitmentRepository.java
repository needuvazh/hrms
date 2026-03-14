package com.company.hrms.recruitment.domain;

import com.company.hrms.recruitment.api.CandidateStatus;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecruitmentRepository {

    Mono<Candidate> save(Candidate candidate);

    Mono<Candidate> findById(UUID candidateId, String tenantId);

    Flux<Candidate> search(String query, CandidateStatus status, int limit, int offset, String tenantId);

    Mono<Candidate> updateStatus(UUID candidateId, CandidateStatus status, String reason, String tenantId);
}
