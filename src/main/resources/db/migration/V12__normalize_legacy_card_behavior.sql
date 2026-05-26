-- ═══════════════════════════════════════════════════════════════════
-- V12: Normalize legacy card behavior values
-- CardBehavior was renamed from {INSTANT_PAYMENT, INSTALLMENTS} to
-- {CREDIT, INSTANT_PAYMENT}. Rows persisted under the old enum still
-- hold 'INSTALLMENTS', which no longer maps and breaks entity loading.
-- ═══════════════════════════════════════════════════════════════════

UPDATE banks.cards
SET behavior = 'CREDIT'
WHERE behavior = 'INSTALLMENTS';
