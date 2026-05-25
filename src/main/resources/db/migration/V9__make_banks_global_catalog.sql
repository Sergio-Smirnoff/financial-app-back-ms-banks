-- V9: Make banks a global catalog (one row per BankName enum value)

-- 1. Reassign accounts that point to a duplicate bank to the canonical (min id) row
UPDATE banks.accounts a
SET bank_id = canonical.min_id
FROM (
    SELECT name, MIN(id) AS min_id
    FROM banks.banks
    GROUP BY name
) canonical
JOIN banks.banks b ON b.name = canonical.name
WHERE a.bank_id = b.id
  AND b.id > canonical.min_id;

-- 2. Reassign cards that point to a duplicate bank
UPDATE banks.cards c
SET bank_id = canonical.min_id
FROM (
    SELECT name, MIN(id) AS min_id
    FROM banks.banks
    GROUP BY name
) canonical
JOIN banks.banks b ON b.name = canonical.name
WHERE c.bank_id = b.id
  AND b.id > canonical.min_id;

-- 3. Reassign loans that point to a duplicate bank
UPDATE banks.loans l
SET bank_id = canonical.min_id
FROM (
    SELECT name, MIN(id) AS min_id
    FROM banks.banks
    GROUP BY name
) canonical
JOIN banks.banks b ON b.name = canonical.name
WHERE l.bank_id = b.id
  AND b.id > canonical.min_id;

-- 4. Delete all duplicate bank rows (keep the lowest id per name)
DELETE FROM banks.banks
WHERE id NOT IN (
    SELECT MIN(id) FROM banks.banks GROUP BY name
);

-- 5. Drop the per-user unique constraint
ALTER TABLE banks.banks DROP CONSTRAINT IF EXISTS uq_banks_user_name;

-- 6. Drop user_id column (banks are now global, not per-user)
ALTER TABLE banks.banks DROP COLUMN IF EXISTS user_id;

-- 7. Enforce global uniqueness on name
ALTER TABLE banks.banks ADD CONSTRAINT uq_banks_name UNIQUE (name);
