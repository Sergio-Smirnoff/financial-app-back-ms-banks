-- ═══════════════════════════════════════════════════════════════════
-- V3: Loans and Installments
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE banks.loans
(
    id                     BIGSERIAL     PRIMARY KEY,
    account_id             BIGINT        NOT NULL REFERENCES banks.accounts (id) ON DELETE CASCADE,
    user_id                BIGINT        NOT NULL,
    name                   VARCHAR(255)  NOT NULL,
    principal              NUMERIC(15,2) NOT NULL,
    currency               VARCHAR(3)    NOT NULL,
    interest_rate          NUMERIC(5,2)  NOT NULL,
    total_installments     INT           NOT NULL,
    remaining_installments INT           NOT NULL,
    start_date             DATE          NOT NULL,
    active                 BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP     NOT NULL,
    updated_at             TIMESTAMP     NOT NULL
);

CREATE INDEX idx_loans_account_id ON banks.loans (account_id);
CREATE INDEX idx_loans_user_id    ON banks.loans (user_id);

CREATE TABLE banks.loan_installments
(
    id                 BIGSERIAL     PRIMARY KEY,
    loan_id            BIGINT        NOT NULL REFERENCES banks.loans (id) ON DELETE CASCADE,
    installment_number INT           NOT NULL,
    amount             NUMERIC(15,2) NOT NULL,
    due_date           DATE          NOT NULL,
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_date          DATE,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_loan_installments_loan_id ON banks.loan_installments (loan_id);
CREATE INDEX idx_loan_installments_due_date ON banks.loan_installments (due_date);
