-- V12: No-op migration (historical fix)
-- This migration was originally created to add min_amount, max_amount,
-- min_tenure_months, max_tenure_months columns to loan_products on an
-- existing database where V7 had already been applied without these columns.
-- V7 was subsequently updated to include these columns, making this migration
-- redundant for fresh installs. Kept as a no-op to preserve migration history.
SELECT 1;
