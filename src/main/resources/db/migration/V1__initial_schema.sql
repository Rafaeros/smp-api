CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(255)
);

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    code VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    code VARCHAR(255) NOT NULL UNIQUE,
    client_id BIGINT,
    product_id BIGINT,
    creation_date TIMESTAMP(6) WITHOUT TIME ZONE,
    delivery_date TIMESTAMP(6) WITHOUT TIME ZONE,
    total_quantity INT,
    produced_quantity INT,
    status VARCHAR(255),
    CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    mac_address VARCHAR(255) UNIQUE,
    ip_address VARCHAR(255),
    status VARCHAR(255),
    current_stage VARCHAR(255),
    current_order_id BIGINT,
    process_status VARCHAR(255),
    last_seen TIMESTAMP(6) WITHOUT TIME ZONE,
    CONSTRAINT fk_device_order FOREIGN KEY (current_order_id) REFERENCES orders(id)
);

CREATE TABLE user_devices (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    user_id BIGINT,
    device_id BIGINT,
    custom_name VARCHAR(255),
    pos_x DOUBLE PRECISION,
    pos_y DOUBLE PRECISION,
    CONSTRAINT uk_user_device UNIQUE (user_id, device_id),
    CONSTRAINT fk_user_device_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_device_device FOREIGN KEY (device_id) REFERENCES devices(id)
);

CREATE TABLE order_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    quantity_produced BIGINT,
    quantity_paused BIGINT,
    cycle_time DOUBLE PRECISION,
    paused_time DOUBLE PRECISION,
    stage VARCHAR(255),
    order_id BIGINT,
    device_id BIGINT,
    CONSTRAINT fk_order_log_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_log_device FOREIGN KEY (device_id) REFERENCES devices(id)
);
