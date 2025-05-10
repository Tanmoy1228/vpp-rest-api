
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

create table if not exists batteries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    postcode VARCHAR(10) NOT NULL,
    capacity INTEGER NOT NULL
);
