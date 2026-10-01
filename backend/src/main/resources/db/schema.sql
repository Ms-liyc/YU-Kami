-- YU-Kami 数据库唯一初始化脚本 (PostgreSQL)
-- 新环境只需执行本文件；初始账号/支付渠道由应用启动时 DataInitializer 写入

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

CREATE TABLE IF NOT EXISTS webhook_config (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    url         VARCHAR(512) NOT NULL,
    secret      VARCHAR(128) NOT NULL,
    events      VARCHAR(256) NOT NULL DEFAULT 'REDEEM_SUCCESS',
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS webhook_log (
    id          BIGINT PRIMARY KEY,
    webhook_id  BIGINT       NOT NULL,
    event       VARCHAR(64)  NOT NULL,
    payload     TEXT,
    response    VARCHAR(512),
    status_code INTEGER,
    success     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_webhook_log_webhook ON webhook_log(webhook_id);
CREATE INDEX IF NOT EXISTS idx_webhook_log_created ON webhook_log(created_at);

CREATE TABLE IF NOT EXISTS shop_user (
    id          BIGINT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    email       VARCHAR(128),
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 营销规则（促销活动 + 优惠券共用一张表，kind 区分）
CREATE TABLE IF NOT EXISTS promotion (
    id              BIGINT PRIMARY KEY,
    kind            VARCHAR(16)  NOT NULL DEFAULT 'ACTIVITY',
    code            VARCHAR(64)  UNIQUE,
    name            VARCHAR(128) NOT NULL,
    description     TEXT,
    type            VARCHAR(32)  NOT NULL,
    discount_value  DECIMAL(12,2) NOT NULL,
    min_amount      DECIMAL(12,2),
    max_discount    DECIMAL(12,2),
    start_at        TIMESTAMP    NOT NULL,
    end_at          TIMESTAMP    NOT NULL,
    product_ids     VARCHAR(512),
    is_holiday      SMALLINT     NOT NULL DEFAULT 0,
    priority        INTEGER      NOT NULL DEFAULT 0,
    usage_limit     INTEGER,
    used_count      INTEGER      NOT NULL DEFAULT 0,
    per_user_limit  INTEGER      NOT NULL DEFAULT 1,
    status          SMALLINT     NOT NULL DEFAULT 1,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_promotion_time ON promotion(start_at, end_at);
CREATE INDEX IF NOT EXISTS idx_promotion_kind ON promotion(kind);

CREATE TABLE IF NOT EXISTS shop_order (
    id              BIGINT PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL UNIQUE,
    user_id         BIGINT       NOT NULL,
    product_id      BIGINT       NOT NULL,
    product_name    VARCHAR(128) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    original_amount DECIMAL(12,2),
    discount_amount DECIMAL(12,2) DEFAULT 0,
    coupon_code     VARCHAR(64),
    promotion_id    BIGINT,
    quantity        INTEGER      NOT NULL DEFAULT 1,
    status          VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(32),
    payment_no      VARCHAR(128),
    card_id         BIGINT,
    remark          VARCHAR(256),
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP,
    delivered_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shop_order_user ON shop_order(user_id);
CREATE INDEX IF NOT EXISTS idx_shop_order_status ON shop_order(status);
CREATE INDEX IF NOT EXISTS idx_shop_order_created ON shop_order(created_at);

CREATE TABLE IF NOT EXISTS payment_config (
    id          BIGINT PRIMARY KEY,
    channel     VARCHAR(32)  NOT NULL UNIQUE,
    app_id      VARCHAR(128),
    app_secret  VARCHAR(256),
    notify_url  VARCHAR(512),
    status      SMALLINT     NOT NULL DEFAULT 1,
    config_json TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 以下为全库表清单（共 14 张）：
-- sys_user, product, card_batch, card_key, redeem_record, audit_log,
-- api_client, webhook_config, webhook_log, shop_user, promotion, shop_order, payment_config
