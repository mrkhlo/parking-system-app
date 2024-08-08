DROP SCHEMA IF EXISTS "payment" CASCADE;
CREATE SCHEMA "payment";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS payment_status;
CREATE TYPE payment_status AS ENUM ('DEBITED', 'DEBIT_FAILED', 'REFUNDED', 'REFUNDED_NOOP', 'REFUND_FAILED');

DROP TABLE IF EXISTS "payment".payments CASCADE;
CREATE TABLE "payment".payments
(
    id uuid NOT NULL,
    parking_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    payment_status payment_status NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id),
    CONSTRAINT unique_parking_id UNIQUE (parking_id)
);

DROP TYPE IF EXISTS transaction_type;
CREATE TYPE transaction_type AS ENUM ('DEBIT', 'CREDIT');

DROP TYPE IF EXISTS transaction_status;
CREATE TYPE transaction_status AS ENUM ('SUCCESS', 'FAILURE');

DROP TABLE IF EXISTS "payment".payment_transactions CASCADE;
CREATE TABLE "payment".payment_transactions(
    id uuid NOT NULL,
    payment_id uuid NOT NULL,
    provider_transaction_id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    transaction_type transaction_type NOT NULL,
    transaction_status transaction_status NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT payment_transactions_pkey PRIMARY KEY (id)
);

ALTER TABLE "payment".payment_transactions
    ADD CONSTRAINT "FK_PAYMENT_ID" FOREIGN KEY (payment_id)
        REFERENCES "payment".payments (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID;

DROP TYPE IF EXISTS outbox_status;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

DROP TYPE IF EXISTS payment_event_type;
CREATE TYPE payment_event_type AS ENUM ('REFUND', 'DEBIT');

DROP TABLE IF EXISTS "payment".payment_outbox CASCADE;
CREATE TABLE "payment".payment_outbox
(
    id uuid NOT NULL,
    payment_id uuid NOT NULL,
    parking_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    payment_event_type payment_event_type NOT NULL,
    version integer NOT NULL,
    CONSTRAINT payment_outbox_pkey PRIMARY KEY (id),
    CONSTRAINT unique_payment_event_per_payment UNIQUE (payment_id, payment_event_type),
    CONSTRAINT unique_payment_event_per_parking UNIQUE (parking_id, payment_event_type)
);

DROP TABLE IF EXISTS "payment".processed_events CASCADE;
CREATE TABLE "payment".processed_events (
    event_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT processed_events_pkey PRIMARY KEY (event_id)
);
