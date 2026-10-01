-- YU-Kami 企业级卡密系统数据库初始化脚本 (PostgreSQL)

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    role        VARCHAR(32)  NOT NULL DEFAULT 'ADMIN',
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    description TEXT,
    card_type   VARCHAR(32)  NOT NULL DEFAULT 'SINGLE',
    value       DECIMAL(12,2),
    duration_days INTEGER,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS card_batch (
    id          BIGINT PRIMARY KEY,
    batch_no    VARCHAR(64)  NOT NULL UNIQUE,
    product_id  BIGINT       NOT NULL REFERENCES product(id),
    total_count INTEGER      NOT NULL,
    used_count  INTEGER      NOT NULL DEFAULT 0,
    prefix      VARCHAR(16),
    remark      VARCHAR(256),
    created_by  BIGINT,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_card_batch_product ON card_batch(product_id);

CREATE TABLE IF NOT EXISTS card_key (
    id              BIGINT PRIMARY KEY,
    batch_id        BIGINT       NOT NULL REFERENCES card_batch(id),
    product_id      BIGINT       NOT NULL REFERENCES product(id),
    key_hash        VARCHAR(128) NOT NULL,
    key_pepper      VARCHAR(64)  NOT NULL,
    key_checksum    VARCHAR(8)   NOT NULL,
    encrypted_meta  TEXT,
    status          SMALLINT     NOT NULL DEFAULT 0,
    redeem_user     VARCHAR(128),
    redeem_ip       VARCHAR(64),
    redeem_at       TIMESTAMP,
    expire_at       TIMESTAMP,
    version         INTEGER      NOT NULL DEFAULT 0,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_card_key_hash ON card_key(key_hash);
CREATE INDEX IF NOT EXISTS idx_card_key_batch ON card_key(batch_id);
CREATE INDEX IF NOT EXISTS idx_card_key_product_status ON card_key(product_id, status);
CREATE INDEX IF NOT EXISTS idx_card_key_redeem_user ON card_key(redeem_user);

CREATE TABLE IF NOT EXISTS redeem_record (
    id          BIGINT PRIMARY KEY,
    card_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    batch_id    BIGINT       NOT NULL,
    redeem_user VARCHAR(128) NOT NULL,
    redeem_ip   VARCHAR(64),
    user_agent  VARCHAR(512),
    result      VARCHAR(32)  NOT NULL,
    message     VARCHAR(256),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_redeem_record_user ON redeem_record(redeem_user);
CREATE INDEX IF NOT EXISTS idx_redeem_record_created ON redeem_record(created_at);

CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT,
    username    VARCHAR(64),
    action      VARCHAR(64)  NOT NULL,
    target      VARCHAR(128),
    detail      TEXT,
    ip          VARCHAR(64),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_created ON audit_log(created_at);

CREATE TABLE IF NOT EXISTS api_client (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    app_key     VARCHAR(64)  NOT NULL UNIQUE,
    app_secret  VARCHAR(128) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
