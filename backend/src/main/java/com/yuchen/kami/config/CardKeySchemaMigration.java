package com.yuchen.kami.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class CardKeySchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final YuKamiProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.getFeatureSchemaMigration().isEnabled()) {
            return;
        }
        ensureChecksumIndex();
    }

    private void ensureChecksumIndex() {
        if (indexExists("card_key", "idx_card_key_checksum")) {
            return;
        }
        jdbcTemplate.execute("CREATE INDEX idx_card_key_checksum ON card_key (key_checksum, status)");
        log.info("已迁移 card_key.key_checksum 索引");
    }

    private boolean indexExists(String table, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class, table, indexName);
        return count != null && count > 0;
    }
}
