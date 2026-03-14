INSERT INTO notification.notification_templates (
    tenant_id,
    template_code,
    channel,
    subject_template,
    body_template,
    is_active
)
VALUES
    (NULL, 'leave.submitted', 'EMAIL', 'Leave request submitted', 'Leave request {{requestId}} submitted by {{employeeName}}.', TRUE),
    (NULL, 'payroll.approval.required', 'EMAIL', 'Payroll approval required', 'Payroll batch {{batchCode}} is waiting for your approval.', TRUE),
    (NULL, 'attendance.correction.review', 'SMS', NULL, 'Attendance correction {{requestId}} requires review.', TRUE)
ON CONFLICT DO NOTHING;
