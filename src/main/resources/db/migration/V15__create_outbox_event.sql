CREATE TABLE banks.outbox_event (
    id            BIGSERIAL PRIMARY KEY,
    event_id      VARCHAR(64)  UNIQUE NOT NULL,
    topic         VARCHAR(249) NOT NULL,
    aggregate_key VARCHAR(64)  NOT NULL,
    ce_type       VARCHAR(120) NOT NULL,
    ce_source     VARCHAR(255) NOT NULL,
    data_schema   VARCHAR(512) NOT NULL,
    data_json     JSONB        NOT NULL,
    sent          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    sent_at       TIMESTAMP
);

CREATE INDEX idx_banks_outbox_unsent ON banks.outbox_event (sent, id);
