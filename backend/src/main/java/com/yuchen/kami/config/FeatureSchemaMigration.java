package com.yuchen.kami.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class FeatureSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final YuKamiProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.getFeatureSchemaMigration().isEnabled()) {
            log.info("特性 schema 迁移已禁用，跳过");
            return;
        }
        ensureProductCategoryColumn();
        ensureSysUserTotpColumns();
        ensureShopUserEmailVerifiedColumn();
    }

    private void ensureProductCategoryColumn() {
        if (columnExists("product", "category")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE product ADD COLUMN category VARCHAR(64) NULL AFTER code");
        log.info("已迁移 product.category 字段");
    }

    private void ensureSysUserTotpColumns() {
        if (!columnExists("sys_user", "totp_secret")) {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN totp_secret VARCHAR(64) NULL");
            log.info("已迁移 sys_user.totp_secret 字段");
        }
        if (!columnExists("sys_user", "totp_enabled")) {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN totp_enabled TINYINT NOT NULL DEFAULT 0");
            log.info("已迁移 sys_user.totp_enabled 字段");
        }
    }

    private void ensureShopUserEmailVerifiedColumn() {
        if (columnExists("shop_user", "email_verified")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE shop_user ADD COLUMN email_verified TINYINT NOT NULL DEFAULT 0");
        log.info("已迁移 shop_user.email_verified 字段");
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}
