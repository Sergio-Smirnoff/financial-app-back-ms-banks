ALTER TABLE banks.accounts ADD CONSTRAINT uq_accounts_cbu UNIQUE USING INDEX idx_accounts_cbu;

CREATE TABLE banks.account_fee_schedules (
    id              BIGSERIAL PRIMARY KEY,
    account_cbu     VARCHAR(22) NOT NULL REFERENCES banks.accounts (cbu),
    maintenance_fee NUMERIC(15,2),
    transfer_fee    NUMERIC(15,2),
    iva_treatment   VARCHAR(10) NOT NULL DEFAULT 'SEPARATE',
    currency        CHAR(3)     NOT NULL DEFAULT 'ARS',
    UNIQUE (account_cbu)
);
