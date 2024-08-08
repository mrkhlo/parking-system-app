DROP SCHEMA IF EXISTS "parking" CASCADE;

CREATE SCHEMA "parking";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS parking_status;
CREATE TYPE parking_status AS ENUM ('CREATE_PENDING', 'CREATED', 'CANCELLED', 'STOP_PENDING', 'STOPPED');


DROP TABLE IF EXISTS "parking".parkings CASCADE;
CREATE TABLE "parking".parkings
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    tracking_id uuid NOT NULL,
    zone_id uuid NOT NULL,
    starting_fee numeric(10,2) NOT NULL,
    closing_fee numeric(10,2),
    license_plate_number character varying COLLATE pg_catalog."default" NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    stopped_at TIMESTAMP WITH TIME ZONE,
    parking_status parking_status NOT NULL,
    CONSTRAINT parkings_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS outbox_status;
CREATE TYPE outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

DROP TYPE IF EXISTS parking_event_type;
CREATE TYPE parking_event_type AS ENUM ('CREATED', 'APPROVED', 'STOPPED');

DROP TABLE IF EXISTS "parking".parking_outbox CASCADE;
CREATE TABLE "parking".parking_outbox
(
    id uuid NOT NULL,
    parking_id uuid NOT NULL,
    customer_id uuid NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    payload jsonb NOT NULL,
    outbox_status outbox_status NOT NULL,
    parking_event_type parking_event_type NOT NULL,
    version integer NOT NULL,
    CONSTRAINT payment_outbox_pkey PRIMARY KEY (id),
    CONSTRAINT unique_parking_event_per_parking UNIQUE (parking_id, parking_event_type)
);
