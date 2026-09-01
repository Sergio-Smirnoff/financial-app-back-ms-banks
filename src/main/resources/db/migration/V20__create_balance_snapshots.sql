CREATE TABLE banks.balance_snapshots (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT      NOT NULL,
    snapshot_date         DATE        NOT NULL,
    cash_by_currency      JSONB       NOT NULL DEFAULT '{}',
    card_debt_by_currency JSONB       NOT NULL DEFAULT '{}',
    loan_debt_by_currency JSONB       NOT NULL DEFAULT '{}',
    created_at            TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (user_id, snapshot_date)
);
