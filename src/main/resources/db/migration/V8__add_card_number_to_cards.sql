-- ═══════════════════════════════════════════════════════════════════
-- V8: Add card_number column to banks.cards
-- Replaces last_4_digits as the primary card identifier.
-- Existing rows get a placeholder derived from last_4_digits.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE banks.cards
    ADD COLUMN card_number VARCHAR(22);

UPDATE banks.cards
SET card_number = LPAD(last_4_digits, 22, '0')
WHERE card_number IS NULL;

ALTER TABLE banks.cards
    ALTER COLUMN card_number SET NOT NULL;

CREATE UNIQUE INDEX idx_cards_card_number ON banks.cards (card_number);
