-- =============================================================
-- V1 BASELINE — represents the schema created by Hibernate
-- ddl-auto=update before Flyway was introduced.
-- This migration is NEVER executed (baseline-on-migrate=true
-- marks the DB at version 1 on first run).
-- It exists purely as documentation of the starting schema.
-- =============================================================

CREATE TABLE IF NOT EXISTS clients (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(255) NOT NULL UNIQUE,
    client_id         BIGINT REFERENCES clients(id),
    product_id        BIGINT REFERENCES products(id),
    creation_date     TIMESTAMP,
    delivery_date     TIMESTAMP,
    total_quantity    INTEGER,
    produced_quantity INTEGER,
    status            VARCHAR(50),
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP
);

CREATE TABLE IF NOT EXISTS devices (
    id              BIGSERIAL PRIMARY KEY,
    mac_address     VARCHAR(255) UNIQUE,
    ip_address      VARCHAR(255),
    status          VARCHAR(50),
    current_stage   VARCHAR(50),
    current_order_id BIGINT REFERENCES orders(id),
    process_status  VARCHAR(50),
    last_seen       TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255),
    username   VARCHAR(255) UNIQUE,
    password   VARCHAR(255),
    role       VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_devices (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id),
    device_id   BIGINT REFERENCES devices(id),
    custom_name VARCHAR(255),
    pos_x       DOUBLE PRECISION,
    pos_y       DOUBLE PRECISION,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    UNIQUE (user_id, device_id)
);

CREATE TABLE IF NOT EXISTS order_logs (
    id                BIGSERIAL PRIMARY KEY,
    created_at        TIMESTAMP NOT NULL,
    quantity_produced BIGINT,
    quantity_paused   BIGINT,
    cycle_time        DOUBLE PRECISION,
    paused_time       DOUBLE PRECISION,
    stage             VARCHAR(50),
    order_id          BIGINT REFERENCES orders(id),
    device_id         BIGINT REFERENCES devices(id)
);
