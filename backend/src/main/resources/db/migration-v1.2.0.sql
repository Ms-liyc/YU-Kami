-- v1.2.0 升级脚本（已有数据库请手动执行）

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
