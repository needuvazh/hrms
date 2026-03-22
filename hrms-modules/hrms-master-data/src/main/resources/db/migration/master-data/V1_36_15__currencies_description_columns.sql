ALTER TABLE master_data.currencies
    ADD COLUMN IF NOT EXISTS short_description VARCHAR(64),
    ADD COLUMN IF NOT EXISTS description VARCHAR(255);
