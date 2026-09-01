ALTER TABLE banks.cards ADD CONSTRAINT uq_cards_card_number UNIQUE USING INDEX idx_cards_card_number;

CREATE TABLE banks.card_fee_schedules (
    id                          BIGSERIAL PRIMARY KEY,
    card_number                 VARCHAR(22) NOT NULL REFERENCES banks.cards (card_number),
    annual_fee                  NUMERIC(15,2),
    international_surcharge_pct NUMERIC(5,2),
    iva_treatment               VARCHAR(10) NOT NULL DEFAULT 'SEPARATE',
    currency                    CHAR(3)     NOT NULL DEFAULT 'ARS',
    UNIQUE (card_number)
);
