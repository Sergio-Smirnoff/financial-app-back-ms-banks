-- V19: alias is optional; drop the NOT NULL constraint added in V7.
-- When omitted, the application layer defaults alias to the account CBU.
ALTER TABLE banks.accounts
    ALTER COLUMN alias DROP NOT NULL;
