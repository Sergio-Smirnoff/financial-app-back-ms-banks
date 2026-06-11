-- Account name uniqueness must be scoped per user, not global per bank.
-- Two different users may reuse the same account name within the same bank.
ALTER TABLE banks.accounts DROP CONSTRAINT IF EXISTS uq_accounts_bank_name;
ALTER TABLE banks.accounts ADD CONSTRAINT uq_accounts_user_bank_name UNIQUE (user_id, bank_id, name);
