-- ═══════════════════════════════════════════════════════════════════
-- V11: Drop orphaned last_4_digits column from banks.cards
-- card_number (V8) is now the sole card identifier. The old
-- last_4_digits column is no longer mapped by the entity but was left
-- NOT NULL, causing every insert to fail with a not-null violation.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE banks.cards
    DROP COLUMN last_4_digits;
