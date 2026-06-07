CREATE TABLE banks.inbound_events (
    event_id     VARCHAR(64) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT now()
);
