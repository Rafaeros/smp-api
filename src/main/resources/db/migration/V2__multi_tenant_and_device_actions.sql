-- =============================================================
-- V2 — Multi-tenant (Company/Sector) + Device Actions
-- =============================================================

-- ── COMPANY ──────────────────────────────────────────────────
CREATE TABLE companies (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    logo_url   VARCHAR(512),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

-- ── SECTOR ───────────────────────────────────────────────────
CREATE TABLE sectors (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    company_id BIGINT       NOT NULL REFERENCES companies(id),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE sector_authorities (
    sector_id  BIGINT      NOT NULL REFERENCES sectors(id) ON DELETE CASCADE,
    authority  VARCHAR(50) NOT NULL,
    PRIMARY KEY (sector_id, authority)
);

-- ── ADD sector_id TO users (nullable — platform ADMIN has none) ─
ALTER TABLE users ADD COLUMN sector_id BIGINT REFERENCES sectors(id);

-- ── ADD company_id TO tenant-scoped tables (nullable) ─────────
ALTER TABLE devices  ADD COLUMN company_id BIGINT REFERENCES companies(id);
ALTER TABLE orders   ADD COLUMN company_id BIGINT REFERENCES companies(id);
ALTER TABLE products ADD COLUMN company_id BIGINT REFERENCES companies(id);
ALTER TABLE clients  ADD COLUMN company_id BIGINT REFERENCES companies(id);

-- ── DEVICE ACTIONS ────────────────────────────────────────────
CREATE TABLE device_actions (
    id                   BIGSERIAL    PRIMARY KEY,
    device_id            BIGINT       NOT NULL REFERENCES devices(id),
    type                 VARCHAR(50)  NOT NULL,
    status               VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    triggered_by_user_id BIGINT       REFERENCES users(id),
    resolved_by_user_id  BIGINT       REFERENCES users(id),
    started_at           TIMESTAMP    NOT NULL,
    ended_at             TIMESTAMP,
    duration_seconds     BIGINT,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP
);

CREATE INDEX idx_device_actions_device_status ON device_actions(device_id, status);

-- ── DEVICE ACTION REPORTS ─────────────────────────────────────
CREATE TABLE device_action_reports (
    id                BIGSERIAL PRIMARY KEY,
    device_action_id  BIGINT    NOT NULL UNIQUE REFERENCES device_actions(id),
    technician_user_id BIGINT   REFERENCES users(id),
    report_notes      TEXT,
    observations      TEXT,
    resolved_at       TIMESTAMP NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP
);
