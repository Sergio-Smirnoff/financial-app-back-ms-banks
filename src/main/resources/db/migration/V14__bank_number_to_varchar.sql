-- V14: bank_number was created as CHAR(3) in V13, which Postgres reports as
-- bpchar (blank-padded). The JPA entity maps it as String length=3, so
-- Hibernate schema-validation expects VARCHAR(3). Convert to match.
ALTER TABLE banks.banks ALTER COLUMN bank_number TYPE VARCHAR(3);
