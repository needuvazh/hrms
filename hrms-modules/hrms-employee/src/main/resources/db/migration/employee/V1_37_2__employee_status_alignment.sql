-- Align employee status values to new lifecycle enum.
-- Kept as enum-style text in employee.employee_profiles (no status-id exposure in API).

-- Map legacy status value to new value.
UPDATE employee.employee_profiles
SET employee_status = 'ON_PROBATION'
WHERE employee_status = 'PROBATION';

-- Recreate status check constraint with the new allowed statuses.
ALTER TABLE employee.employee_profiles
    DROP CONSTRAINT IF EXISTS ck_employee_profiles_status;

ALTER TABLE employee.employee_profiles
    ADD CONSTRAINT ck_employee_profiles_status CHECK (
        employee_status IN (
            'DRAFT',
            'ACTIVE',
            'ON_PROBATION',
            'CONFIRMED',
            'NOTICE_PERIOD',
            'RESIGNED',
            'TERMINATED',
            'RETIRED',
            'SUSPENDED',
            'INACTIVE'
        )
    );
