-- ═══════════════════════════════════════════════════════════════════
-- V1: Banks schema — institutions and accounts
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE banks.banks
(
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    logo_url   VARCHAR(500),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT uq_banks_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_banks_user_id ON banks.banks (user_id);

CREATE TABLE banks.accounts
(
    id         BIGSERIAL     PRIMARY KEY,
    bank_id    BIGINT        NOT NULL REFERENCES banks.banks (id) ON DELETE CASCADE,
    user_id    BIGINT        NOT NULL,
    name       VARCHAR(100)  NOT NULL,
    type       VARCHAR(20)   NOT NULL,
    balance    NUMERIC(15,2) NOT NULL DEFAULT 0,
    currency   VARCHAR(3)    NOT NULL,
    is_active  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL,
    CONSTRAINT uq_accounts_bank_name UNIQUE (bank_id, name)
);

CREATE INDEX idx_accounts_bank_id  ON banks.accounts (bank_id);
CREATE INDEX idx_accounts_user_id  ON banks.accounts (user_id);
CREATE INDEX idx_accounts_type     ON banks.accounts (type);
CREATE INDEX idx_accounts_currency ON banks.accounts (currency);
