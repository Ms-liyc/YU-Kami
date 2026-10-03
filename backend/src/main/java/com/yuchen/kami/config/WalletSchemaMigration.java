package com.yuchen.kami.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class WalletSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureShopUserBalanceColumn();
        ensureWalletTransactionTable();
    }

    private void ensureShopUserBalanceColumn() {
        if (columnExists("shop_user", "balance")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE shop_user ADD COLUMN balance DECIMAL(12,2) NOT NULL DEFAULT 0");
        log.info("已迁移 shop_user.balance 字段（钱包余额）");
    }

    private void ensureWalletTransactionTable() {
        if (tableExists("wallet_transaction")) {
            return;
        }
        jdbcTemplate.execute("""
                CREATE TABLE wallet_transaction (
                    id BIGINT NOT NULL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    amount DECIMAL(12,2) NOT NULL,
                    balance_after DECIMAL(12,2) NOT NULL,
                    order_id BIGINT NULL,
                    order_no VARCHAR(64) NULL,
                    remark VARCHAR(256) NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_wallet_tx_user (user_id),
                    KEY idx_wallet_tx_created (created_at),
                    CONSTRAINT fk_wallet_tx_user FOREIGN KEY (user_id) REFERENCES shop_user(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
        log.info("已创建 wallet_transaction 表");
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }

    private boolean tableExists(String table) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, table);
        return count != null && count > 0;
    }
}
