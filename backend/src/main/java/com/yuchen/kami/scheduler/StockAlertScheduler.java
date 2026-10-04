package com.yuchen.kami.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.ProductStockAlert;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.service.EmailService;
import com.yuchen.kami.service.SmsService;
import com.yuchen.kami.service.WebhookDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private static final String ALERT_KEY_PREFIX = "stock:alert:";

    private final ProductMapper productMapper;
    private final CardKeyMapper cardKeyMapper;
    private final StringRedisTemplate redisTemplate;
    private final WebhookDispatchService webhookDispatchService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final YuKamiProperties properties;

    @Scheduled(cron = "0 0 * * * *")
    public void checkLowStock() {
        int threshold = properties.getStock().getLowThreshold();
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1));
        for (Product product : products) {
            long unused = cardKeyMapper.selectCount(new LambdaQueryWrapper<CardKey>()
                    .eq(CardKey::getProductId, product.getId())
                    .eq(CardKey::getStatus, CardKey.STATUS_UNUSED));
            if (unused > threshold) {
                continue;
            }
            String key = ALERT_KEY_PREFIX + product.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                continue;
            }
            ProductStockAlert alert = ProductStockAlert.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productCode(product.getCode())
                    .unusedCount(unused)
                    .threshold(threshold)
                    .build();
            webhookDispatchService.dispatch("LOW_STOCK", Map.of(
                    "productId", alert.getProductId(),
                    "productName", alert.getProductName(),
                    "productCode", alert.getProductCode(),
                    "unusedCount", alert.getUnusedCount(),
                    "threshold", alert.getThreshold()
            ));
            redisTemplate.opsForValue().set(key, "1", Duration.ofHours(24));
            log.info("低库存告警已发送: {} (剩余 {})", product.getName(), unused);
            sendEmailAlert(product.getName(), product.getCode(), unused, threshold);
            sendSmsAlert(product.getName(), product.getCode(), unused, threshold);
        }
    }

    private void sendSmsAlert(String name, String code, long unused, int threshold) {
        String recipients = properties.getStock().getAlertPhone();
        if (!StringUtils.hasText(recipients)) {
            return;
        }
        for (String to : recipients.split(",")) {
            String phone = to.trim();
            if (!phone.isBlank()) {
                smsService.sendLowStockAlert(phone, name, code, unused, threshold);
            }
        }
    }

    private void sendEmailAlert(String name, String code, long unused, int threshold) {
        String recipients = properties.getStock().getAlertEmail();
        if (!StringUtils.hasText(recipients)) {
            return;
        }
        for (String to : recipients.split(",")) {
            String email = to.trim();
            if (!email.isBlank()) {
                emailService.sendLowStockAlert(email, name, code, unused, threshold);
            }
        }
    }
}
