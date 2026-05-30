-- V13: identify banks by their 3-digit BCRA bank number
ALTER TABLE banks.banks ADD COLUMN bank_number CHAR(3);

-- Seed/patch the catalog. Idempotent: works on a fresh DB and on one the old
-- BankCatalogSeeder already populated (uq_banks_name from V9 backs ON CONFLICT).
-- created_at/updated_at are NOT NULL with no defaults, so we supply now().
INSERT INTO banks.banks (name, bank_number, created_at, updated_at) VALUES
    ('GALICIA','007', now(), now()),
    ('NACION','011', now(), now()),
    ('ICBC','015', now(), now()),
    ('CITIBANK','016', now(), now()),
    ('BBVA','017', now(), now()),
    ('SUPERVIELLE','027', now(), now()),
    ('PATAGONIA','034', now(), now()),
    ('HIPOTECARIO','044', now(), now()),
    ('BANCO_DEL_CHUBUT','083', now(), now()),
    ('SANTANDER','072', now(), now()),
    ('HSBC','150', now(), now()),
    ('MACRO','285', now(), now()),
    ('BANCO_COMAFI','299', now(), now())
ON CONFLICT (name) DO UPDATE SET bank_number = EXCLUDED.bank_number;

ALTER TABLE banks.banks ALTER COLUMN bank_number SET NOT NULL;
ALTER TABLE banks.banks ADD CONSTRAINT uq_banks_bank_number UNIQUE (bank_number);
