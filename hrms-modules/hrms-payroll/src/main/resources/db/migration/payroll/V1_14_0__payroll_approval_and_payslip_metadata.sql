ALTER TABLE payroll.payroll_runs
    ADD COLUMN IF NOT EXISTS workflow_instance_id UUID,
    ADD COLUMN IF NOT EXISTS submitted_by VARCHAR(200),
    ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(200),
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE payroll.payslips
    ADD COLUMN IF NOT EXISTS document_record_id UUID,
    ADD COLUMN IF NOT EXISTS artifact_object_key VARCHAR(500),
    ADD COLUMN IF NOT EXISTS artifact_content_type VARCHAR(150);
