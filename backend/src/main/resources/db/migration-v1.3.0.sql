-- v1.3.0 升级脚本

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

CREATE TABLE IF NOT EXISTS shop_order (
    id              BIGINT PRIMARY KEY,
    order_no        VARCHAR(64)  NOT NULL UNIQUE,
    user_id         BIGINT       NOT NULL,
    product_id      BIGINT       NOT NULL,
    product_name    VARCHAR(128) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
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

INSERT INTO payment_config (id, channel, status, config_json)
VALUES (1, 'MOCK', 1, '{"description":"模拟支付，开发测试用"}')
ON CONFLICT (channel) DO NOTHING;
