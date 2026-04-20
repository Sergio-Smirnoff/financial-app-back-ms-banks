-- 1. Add bank_id column to cards and loans
ALTER TABLE banks.cards ADD COLUMN bank_id BIGINT;
ALTER TABLE banks.loans ADD COLUMN bank_id BIGINT;

-- 2. Populate bank_id based on account_id
UPDATE banks.cards c
SET bank_id = a.bank_id
FROM banks.accounts a
WHERE c.account_id = a.id;

UPDATE banks.loans l
SET bank_id = a.bank_id
FROM banks.accounts a
WHERE l.account_id = a.id;

-- 3. Make bank_id NOT NULL and add foreign key
ALTER TABLE banks.cards ALTER COLUMN bank_id SET NOT NULL;
ALTER TABLE banks.loans ALTER COLUMN bank_id SET NOT NULL;

ALTER TABLE banks.cards ADD CONSTRAINT fk_cards_bank FOREIGN KEY (bank_id) REFERENCES banks.banks(id);
ALTER TABLE banks.loans ADD CONSTRAINT fk_loans_bank FOREIGN KEY (bank_id) REFERENCES banks.banks(id);

-- 4. Drop account_id column
ALTER TABLE banks.cards DROP COLUMN account_id;
ALTER TABLE banks.loans DROP COLUMN account_id;
