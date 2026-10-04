package com.yuchen.kami.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionSecurityChecker {

    private static final Set<String> DEFAULT_JWT_SECRETS = Set.of(
            "YuChen-Kami-Enterprise-JWT-Secret-Key-Change-In-Production-2026",
            "YuChen-Kami-Docker-JWT-Secret-Change-In-Production"
    );
    private static final Set<String> DEFAULT_HMAC_SECRETS = Set.of(
            "YuChen-HMAC-Master-Secret-Change-In-Production",
            "YuChen-HMAC-Docker-Secret-Change-In-Production"
    );
    private static final Set<String> DEFAULT_AES_KEYS = Set.of(
            "YuChenAES256KeyChangeInProd!!"
    );
    private static final Set<String> DEFAULT_DB_PASSWORDS = Set.of(
            "yukami123",
            "change-me-db-password",
            "liyuchen@123"
    );

    private final YuKamiProperties properties;
    private final PaymentConfigMapper paymentConfigMapper;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void checkDefaultSecrets() {
        List<String> warnings = new ArrayList<>();
        if (DEFAULT_JWT_SECRETS.contains(properties.getJwt().getSecret())) {
            warnings.add("JWT_SECRET 仍为默认值");
        }
        if (DEFAULT_HMAC_SECRETS.contains(properties.getCrypto().getHmacSecret())) {
            warnings.add("HMAC_SECRET 仍为默认值");
        }
        if (DEFAULT_AES_KEYS.contains(properties.getCrypto().getAesKey())) {
            warnings.add("AES_KEY 仍为默认值");
        }
        if (dbPassword == null || dbPassword.isBlank() || DEFAULT_DB_PASSWORDS.contains(dbPassword)) {
            warnings.add("DB_PASSWORD 仍为默认值或未设置，请通过环境变量配置强密码");
        }
        if (properties.getSecurity().isSwaggerEnabled()) {
            warnings.add("SWAGGER_ENABLED=true，生产环境建议设为 false");
        }
        String cors = properties.getSecurity().getCorsAllowedOrigins();
        if (cors == null || cors.isBlank() || "*".equals(cors.trim())) {
            warnings.add("CORS 允许任意来源（*），生产环境请设置 CORS_ALLOWED_ORIGINS 为实际域名");
        }
        boolean productionSecrets = !DEFAULT_JWT_SECRETS.contains(properties.getJwt().getSecret());
        if (productionSecrets && properties.getPayment().isMockEnabled()) {
            warnings.add("MOCK_PAYMENT_ENABLED=true，生产环境请设置 MOCK_PAYMENT_ENABLED=false");
        }
        if (productionSecrets) {
            PaymentConfig mock = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                    .eq(PaymentConfig::getChannel, "MOCK"));
            if (mock != null && mock.getStatus() != null && mock.getStatus() == 1) {
                warnings.add("数据库中 MOCK 支付渠道仍为启用状态，生产环境请在管理后台禁用");
            }
        }
        String stockAlertEmail = properties.getStock().getAlertEmail();
        if (stockAlertEmail != null && !stockAlertEmail.isBlank()
                && (properties.getMail().getFrom() == null || properties.getMail().getFrom().isBlank())) {
            warnings.add("已设置 STOCK_ALERT_EMAIL 但未配置 MAIL_FROM/MAIL_HOST，库存邮件告警将无法发送");
        }
        String stockAlertPhone = properties.getStock().getAlertPhone();
        if (stockAlertPhone != null && !stockAlertPhone.isBlank()) {
            String provider = properties.getSms().getProvider();
            if (provider == null || provider.isBlank() || "none".equalsIgnoreCase(provider.trim())) {
                warnings.add("已设置 STOCK_ALERT_PHONE 但 SMS_PROVIDER 未配置，库存短信告警将无法发送");
            }
        }
        if (warnings.isEmpty()) {
            return;
        }
        log.warn("============================================================");
        log.warn("安全警告：检测到以下配置仍使用默认值，生产环境请务必修改！");
        warnings.forEach(item -> log.warn("  - {}", item));
        log.warn("参考 .env.example 与 docs/SECURITY.md");
        log.warn("============================================================");
    }
}
