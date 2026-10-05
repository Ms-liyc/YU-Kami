package com.yuchen.kami.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class PaymentSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final YuKamiProperties properties;

    @Override
    public void run(String... args) {
        if (!properties.getFeatureSchemaMigration().isEnabled()) {
            return;
        }
        removeMockChannel();
    }

    private void removeMockChannel() {
        int deleted = jdbcTemplate.update("DELETE FROM payment_config WHERE channel = 'MOCK'");
        if (deleted > 0) {
            log.info("已移除模拟支付渠道 MOCK（{} 条）", deleted);
        }
    }
}
