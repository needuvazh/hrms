package com.company.hrms.recruitment.infrastructure.persistence;

import com.company.hrms.recruitment.api.CandidateStatus;
import com.company.hrms.recruitment.domain.Candidate;
import com.company.hrms.recruitment.domain.RecruitmentRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class RecruitmentR2dbcRepository implements RecruitmentRepository {

    private final DatabaseClient databaseClient;

    public RecruitmentR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Candidate> save(Candidate candidate) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO recruitment.candidates(
                            id, tenant_id, person_id, candidate_code, first_name, last_name, email, job_posting_code, status, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :personId, :candidateCode, :firstName, :lastName, :email, :jobPostingCode, :status, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, person_id, candidate_code, first_name, last_name, email, job_posting_code, status, created_at, updated_at
                        """)
                .bind("id", candidate.id())
                .bind("tenantId", candidate.tenantId())
                .bind("personId", candidate.personId())
                .bind("candidateCode", candidate.candidateCode())
                .bind("firstName", candidate.firstName())
                .bind("lastName", candidate.lastName())
                .bind("email", candidate.email())
                .bind("status", candidate.status().name())
                .bind("createdAt", candidate.createdAt())
                .bind("updatedAt", candidate.updatedAt());

        spec = bindNullable(spec, "jobPostingCode", candidate.jobPostingCode(), String.class);

        return spec.map((row, metadata) -> mapCandidate(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_id", UUID.class),
                        row.get("candidate_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("job_posting_code", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Candidate> findById(UUID candidateId, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, person_id, candidate_code, first_name, last_name, email, job_posting_code, status, created_at, updated_at
                        FROM recruitment.candidates
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        """)
                .bind("id", candidateId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapCandidate(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_id", UUID.class),
                        row.get("candidate_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("job_posting_code", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<Candidate> search(String query, CandidateStatus status, int limit, int offset, String tenantId) {
        String likeQuery = StringUtils.hasText(query) ? "%" + query.trim() + "%" : "%";

        GenericExecuteSpec spec = databaseClient.sql("""
                        SELECT id, tenant_id, person_id, candidate_code, first_name, last_name, email, job_posting_code, status, created_at, updated_at
                        FROM recruitment.candidates
                        WHERE tenant_id = :tenantId
                          AND (
                            lower(first_name) LIKE lower(:searchQuery)
                            OR lower(last_name) LIKE lower(:searchQuery)
                            OR lower(email) LIKE lower(:searchQuery)
                            OR lower(candidate_code) LIKE lower(:searchQuery)
                          )
                          AND (:status IS NULL OR status = :status)
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("tenantId", tenantId)
                .bind("searchQuery", likeQuery)
                .bind("limit", limit)
                .bind("offset", offset);

        spec = bindNullable(spec, "status", status == null ? null : status.name(), String.class);

        return spec.map((row, metadata) -> mapCandidate(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_id", UUID.class),
                        row.get("candidate_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("job_posting_code", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<Candidate> updateStatus(UUID candidateId, CandidateStatus status, String reason, String tenantId) {
        Instant now = Instant.now();
        return databaseClient.sql("""
                        UPDATE recruitment.candidates
                        SET status = :status,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, person_id, candidate_code, first_name, last_name, email, job_posting_code, status, created_at, updated_at
                        """)
                .bind("status", status.name())
                .bind("updatedAt", now)
                .bind("id", candidateId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapCandidate(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_id", UUID.class),
                        row.get("candidate_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("job_posting_code", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one()
                .flatMap(updated -> databaseClient.sql("""
                                INSERT INTO recruitment.candidate_status_history(
                                    id, tenant_id, candidate_id, status, reason, changed_at
                                ) VALUES (
                                    :id, :tenantId, :candidateId, :status, :reason, :changedAt
                                )
                                """)
                        .bind("id", UUID.randomUUID())
                        .bind("tenantId", tenantId)
                        .bind("candidateId", candidateId)
                        .bind("status", status.name())
                        .bind("reason", reason == null ? "status update" : reason)
                        .bind("changedAt", now)
                        .fetch()
                        .rowsUpdated()
                        .thenReturn(updated));
    }

    private Candidate mapCandidate(
            UUID id,
            String tenantId,
            UUID personId,
            String candidateCode,
            String firstName,
            String lastName,
            String email,
            String jobPostingCode,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Candidate(
                id,
                tenantId,
                personId,
                candidateCode,
                firstName,
                lastName,
                email,
                jobPostingCode,
                CandidateStatus.valueOf(status),
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
