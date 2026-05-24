-- ═══════════════════════════════════════════════════════════════════
-- V7: Add cbu and alias columns to banks.accounts
-- cbu  — unique 22-char account identifier (CBU in Argentina)
-- alias — user-defined account alias
-- Existing rows get placeholder values derived from their id.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE banks.accounts
    ADD COLUMN cbu   VARCHAR(100),
    ADD COLUMN alias VARCHAR(100);

UPDATE banks.accounts
SET cbu   = LPAD(id::text, 22, '0'),
    alias = 'account-' || id
WHERE cbu IS NULL;

ALTER TABLE banks.accounts
    ALTER COLUMN cbu   SET NOT NULL,
    ALTER COLUMN alias SET NOT NULL;

CREATE UNIQUE INDEX idx_accounts_cbu ON banks.accounts (cbu);
