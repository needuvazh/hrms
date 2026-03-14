CREATE SCHEMA IF NOT EXISTS recruitment;

CREATE TABLE IF NOT EXISTS recruitment.candidates (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    person_id UUID NOT NULL,
    candidate_code VARCHAR(64) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128),
    email VARCHAR(255) NOT NULL,
    job_posting_code VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_recruitment_candidates_code UNIQUE (tenant_id, candidate_code)
);

CREATE TABLE IF NOT EXISTS recruitment.candidate_applications (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    candidate_id UUID NOT NULL,
    position_code VARCHAR(64) NOT NULL,
    source_channel VARCHAR(64),
    application_status VARCHAR(32) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_applications_candidate FOREIGN KEY (candidate_id) REFERENCES recruitment.candidates(id)
);

CREATE TABLE IF NOT EXISTS recruitment.interview_rounds (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    candidate_id UUID NOT NULL,
    round_number INT NOT NULL,
    interview_type VARCHAR(32) NOT NULL,
    scheduled_at TIMESTAMPTZ,
    interviewer VARCHAR(128),
    result_status VARCHAR(32),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_rounds_candidate FOREIGN KEY (candidate_id) REFERENCES recruitment.candidates(id)
);

CREATE TABLE IF NOT EXISTS recruitment.interview_feedback (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    interview_round_id UUID NOT NULL,
    reviewer VARCHAR(128) NOT NULL,
    rating SMALLINT,
    recommendation VARCHAR(32),
    feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_feedback_round FOREIGN KEY (interview_round_id) REFERENCES recruitment.interview_rounds(id)
);

CREATE TABLE IF NOT EXISTS recruitment.offers (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    candidate_id UUID NOT NULL,
    offered_position VARCHAR(128) NOT NULL,
    offered_salary NUMERIC(18, 2),
    offer_status VARCHAR(32) NOT NULL,
    offered_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offers_candidate FOREIGN KEY (candidate_id) REFERENCES recruitment.candidates(id)
);

CREATE TABLE IF NOT EXISTS recruitment.candidate_status_history (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    candidate_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason TEXT,
    changed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_status_history_candidate FOREIGN KEY (candidate_id) REFERENCES recruitment.candidates(id)
);

CREATE INDEX IF NOT EXISTS idx_recruitment_candidates_tenant_status
    ON recruitment.candidates (tenant_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_recruitment_candidates_tenant_person
    ON recruitment.candidates (tenant_id, person_id);

CREATE INDEX IF NOT EXISTS idx_recruitment_status_history_candidate
    ON recruitment.candidate_status_history (tenant_id, candidate_id, changed_at DESC);
