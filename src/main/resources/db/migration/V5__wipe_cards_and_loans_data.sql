-- Wipe all card and loan data due to bank-level migration and architectural changes
DELETE FROM banks.card_installments;
DELETE FROM banks.loan_installments;
DELETE FROM banks.cards;
DELETE FROM banks.loans;
