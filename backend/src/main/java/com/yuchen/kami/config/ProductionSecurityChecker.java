package com.yuchen.kami.config;

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
            "yukami123"
    );

    private final YuKamiProperties properties;

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
        if (DEFAULT_DB_PASSWORDS.contains(dbPassword)) {
            warnings.add("DB_PASSWORD 仍为默认值 yukami123");
        }
        if (warnings.isEmpty()) {
            return;
        }
        log.warn("============================================================");
        log.warn("安全警告：检测到以下配置仍使用默认值，生产环境请务必修改！");
        warnings.forEach(item -> log.warn("  - {}", item));
        log.warn("参考项目根目录 .env.example 配置环境变量");
        log.warn("============================================================");
    }
}
