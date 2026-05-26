-- V10: logo now lives in the BankName enum; drop the unused column.
ALTER TABLE banks.banks DROP COLUMN IF EXISTS logo_url;
