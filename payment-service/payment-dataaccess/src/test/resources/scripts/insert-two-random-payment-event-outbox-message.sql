-- Insert data into payment_outbox table

DELETE from "payment".payment_outbox;


INSERT INTO "payment".payment_outbox (
    id,
    payment_id,
    parking_id,
    customer_id,
    created_at,
    processed_at,
    payload,
    outbox_status,
    payment_event_type,
    version
) VALUES (
             uuid_generate_v4(),
             uuid_generate_v4(),
             uuid_generate_v4(),
             uuid_generate_v4(),
             now(),
             NULL,
             '{"example": "payload1"}',
             'STARTED',
             'REFUND',
             1
         );

INSERT INTO "payment".payment_outbox (
    id,
    payment_id,
    parking_id,
    customer_id,
    created_at,
    processed_at,
    payload,
    outbox_status,
    payment_event_type,
    version
) VALUES (
             uuid_generate_v4(),
             uuid_generate_v4(),
             uuid_generate_v4(),
             uuid_generate_v4(),
             now(),
             NULL,
             '{"example": "payload2"}',
             'STARTED',
             'DEBIT',
             1
         );
