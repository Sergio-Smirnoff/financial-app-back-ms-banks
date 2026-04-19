-- ═══════════════════════════════════════════════════════════════════
-- V2: Cards and Installments
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE banks.cards
(
    id            BIGSERIAL    PRIMARY KEY,
    account_id    BIGINT       NOT NULL REFERENCES banks.accounts (id) ON DELETE CASCADE,
    user_id       BIGINT       NOT NULL,
    brand         VARCHAR(20)  NOT NULL,
    card_type     VARCHAR(20)  NOT NULL,
    behavior      VARCHAR(20)  NOT NULL,
    last_4_digits VARCHAR(4)   NOT NULL,
    expiring_date DATE         NOT NULL,
    closing_day   INT          NOT NULL,
    due_day       INT          NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uq_cards_account_brand_type_last4 UNIQUE (account_id, brand, card_type, last_4_digits)
);

CREATE INDEX idx_cards_account_id ON banks.cards (account_id);
CREATE INDEX idx_cards_user_id    ON banks.cards (user_id);

CREATE TABLE banks.card_installments
(
    id                 BIGSERIAL     PRIMARY KEY,
    card_id            BIGINT        NOT NULL REFERENCES banks.cards (id) ON DELETE CASCADE,
    description        VARCHAR(255)  NOT NULL,
    total_amount       NUMERIC(15,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL,
    installment_number INT           NOT NULL,
    total_installments INT           NOT NULL,
    amount             NUMERIC(15,2) NOT NULL,
    due_date           DATE          NOT NULL,
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_date          DATE,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_card_installments_card_id ON banks.card_installments (card_id);
CREATE INDEX idx_card_installments_due_date ON banks.card_installments (due_date);
