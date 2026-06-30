-- V15__alter_loan_applications_add_tenure_and_purpose.sql
-- Add tenure_months and purpose columns to loan_applications

ALTER TABLE loan_applications ADD COLUMN tenure_months INT NOT NULL DEFAULT 12;
ALTER TABLE loan_applications ADD COLUMN purpose VARCHAR(255);
