-- YU-Kami 数据库唯一初始化脚本 (MySQL 8.0+)
-- 新环境只需执行本文件；初始账号/支付渠道由应用启动时 DataInitializer 写入

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64)  NULL,
    role        VARCHAR(32)  NOT NULL DEFAULT 'ADMIN',
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS product (
    id            BIGINT         NOT NULL PRIMARY KEY,
    name          VARCHAR(128)   NOT NULL,
    code          VARCHAR(64)    NOT NULL,
    description   TEXT           NULL,
    card_type     VARCHAR(32)    NOT NULL DEFAULT 'SINGLE',
    value         DECIMAL(12,2)  NULL,
    duration_days INT            NULL,
    status        SMALLINT       NOT NULL DEFAULT 1,
    deleted       SMALLINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS card_batch (
    id          BIGINT       NOT NULL PRIMARY KEY,
    batch_no    VARCHAR(64)  NOT NULL,
    product_id  BIGINT       NOT NULL,
    total_count INT          NOT NULL,
    used_count  INT          NOT NULL DEFAULT 0,
    prefix      VARCHAR(16)  NULL,
    remark      VARCHAR(256) NULL,
    created_by  BIGINT       NULL,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_card_batch_no (batch_no),
    KEY idx_card_batch_product (product_id),
    CONSTRAINT fk_card_batch_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS card_key (
    id              BIGINT       NOT NULL PRIMARY KEY,
    batch_id        BIGINT       NOT NULL,
    product_id      BIGINT       NOT NULL,
    key_hash        VARCHAR(128) NOT NULL,
    key_pepper      VARCHAR(64)  NOT NULL,
    key_checksum    VARCHAR(8)   NOT NULL,
    encrypted_meta  TEXT         NULL,
    status          SMALLINT     NOT NULL DEFAULT 0,
    redeem_user     VARCHAR(128) NULL,
    redeem_ip       VARCHAR(64)  NULL,
    redeem_at       TIMESTAMP    NULL,
    expire_at       TIMESTAMP    NULL,
    version         INT          NOT NULL DEFAULT 0,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_card_key_hash (key_hash),
    KEY idx_card_key_batch (batch_id),
    KEY idx_card_key_product_status (product_id, status),
    KEY idx_card_key_redeem_user (redeem_user),
    CONSTRAINT fk_card_key_batch FOREIGN KEY (batch_id) REFERENCES card_batch(id),
    CONSTRAINT fk_card_key_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS redeem_record (
    id          BIGINT       NOT NULL PRIMARY KEY,
    card_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    batch_id    BIGINT       NOT NULL,
    redeem_user VARCHAR(128) NOT NULL,
    redeem_ip   VARCHAR(64)  NULL,
    user_agent  VARCHAR(512) NULL,
    result      VARCHAR(32)  NOT NULL,
    message     VARCHAR(256) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_redeem_record_user (redeem_user),
    KEY idx_redeem_record_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT       NOT NULL PRIMARY KEY,
    user_id     BIGINT       NULL,
    username    VARCHAR(64)  NULL,
    action      VARCHAR(64)  NOT NULL,
    target      VARCHAR(128) NULL,
    detail      TEXT         NULL,
    ip          VARCHAR(64)  NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS api_client (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    app_key     VARCHAR(64)  NOT NULL,
    app_secret  VARCHAR(128) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_client_key (app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS webhook_config (
    id          BIGINT       NOT NULL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    url         VARCHAR(512) NOT NULL,
    secret      VARCHAR(128) NOT NULL,
    events      VARCHAR(256) NOT NULL DEFAULT 'REDEEM_SUCCESS',
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS webhook_log (
    id          BIGINT       NOT NULL PRIMARY KEY,
    webhook_id  BIGINT       NOT NULL,
    event       VARCHAR(64)  NOT NULL,
    payload     TEXT         NULL,
    response    VARCHAR(512) NULL,
    status_code INT          NULL,
    success     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_webhook_log_webhook (webhook_id),
    KEY idx_webhook_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shop_user (
    id          BIGINT       NOT NULL PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    email       VARCHAR(128) NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64)  NULL,
    balance     DECIMAL(12,2) NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_shop_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS promotion (
    id              BIGINT         NOT NULL PRIMARY KEY,
    kind            VARCHAR(16)    NOT NULL DEFAULT 'ACTIVITY',
    code            VARCHAR(64)    NULL,
    name            VARCHAR(128)   NOT NULL,
    description     TEXT           NULL,
    type            VARCHAR(32)    NOT NULL,
    discount_value  DECIMAL(12,2)  NOT NULL,
    min_amount      DECIMAL(12,2)  NULL,
    max_discount    DECIMAL(12,2)  NULL,
    start_at        TIMESTAMP      NOT NULL,
    end_at          TIMESTAMP      NOT NULL,
    product_ids     VARCHAR(512)   NULL,
    is_holiday      SMALLINT       NOT NULL DEFAULT 0,
    priority        INT            NOT NULL DEFAULT 0,
    usage_limit     INT            NULL,
    used_count      INT            NOT NULL DEFAULT 0,
    per_user_limit  INT            NOT NULL DEFAULT 1,
    status          SMALLINT       NOT NULL DEFAULT 1,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_promotion_code (code),
    KEY idx_promotion_time (start_at, end_at),
    KEY idx_promotion_kind (kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS shop_order (
    id              BIGINT         NOT NULL PRIMARY KEY,
    order_no        VARCHAR(64)    NOT NULL,
    user_id         BIGINT         NOT NULL,
    order_type      VARCHAR(32)    NOT NULL DEFAULT 'PRODUCT',
    product_id      BIGINT         NOT NULL,
    product_name    VARCHAR(128)   NOT NULL,
    amount          DECIMAL(12,2)  NOT NULL,
    original_amount DECIMAL(12,2)  NULL,
    discount_amount DECIMAL(12,2)  DEFAULT 0,
    coupon_code     VARCHAR(64)    NULL,
    promotion_id    BIGINT         NULL,
    quantity        INT            NOT NULL DEFAULT 1,
    status          VARCHAR(32)    NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(32)    NULL,
    payment_no      VARCHAR(128)   NULL,
    card_id         BIGINT         NULL,
    remark          VARCHAR(256)   NULL,
    deleted         SMALLINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP      NULL,
    delivered_at    TIMESTAMP      NULL,
    UNIQUE KEY uk_shop_order_no (order_no),
    KEY idx_shop_order_user (user_id),
    KEY idx_shop_order_status (status),
    KEY idx_shop_order_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_config (
    id          BIGINT       NOT NULL PRIMARY KEY,
    channel     VARCHAR(32)  NOT NULL,
    app_id      VARCHAR(128) NULL,
    app_secret  VARCHAR(256) NULL,
    notify_url  VARCHAR(512) NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    config_json TEXT         NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_config_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wallet_transaction (
    id          BIGINT         NOT NULL PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    type        VARCHAR(32)    NOT NULL,
    amount      DECIMAL(12,2)  NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    order_id    BIGINT         NULL,
    order_no    VARCHAR(64)    NULL,
    remark      VARCHAR(256)   NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_wallet_tx_user (user_id),
    KEY idx_wallet_tx_created (created_at),
    CONSTRAINT fk_wallet_tx_user FOREIGN KEY (user_id) REFERENCES shop_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
