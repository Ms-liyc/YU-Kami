package com.yuchen.kami.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class OrderSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureOrderTypeColumn();
    }

    private void ensureOrderTypeColumn() {
        if (columnExists("shop_order", "order_type")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE shop_order ADD COLUMN order_type VARCHAR(32) NOT NULL DEFAULT 'PRODUCT' AFTER user_id");
        log.info("已迁移 shop_order.order_type 字段（商品/充值订单）");
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}
