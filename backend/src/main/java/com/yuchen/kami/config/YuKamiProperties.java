package com.yuchen.kami.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yukami")
public class YuKamiProperties {

    private Jwt jwt = new Jwt();
    private Crypto crypto = new Crypto();
    private Redeem redeem = new Redeem();
    private Card card = new Card();
    private Stock stock = new Stock();
    private Payment payment = new Payment();
    private Security security = new Security();
    private Captcha captcha = new Captcha();
    private Mail mail = new Mail();
    private FeatureSchemaMigration featureSchemaMigration = new FeatureSchemaMigration();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    @Data
    public static class Crypto {
        private String hmacSecret;
        private String aesKey;
        private String rsaPrivateKeyPath;
        private String rsaPublicKeyPath;
    }

    @Data
    public static class Redeem {
        private int rateLimitPerMinute;
        private int lockTtlSeconds;
    }

    @Data
    public static class Card {
        private int defaultLength;
        private String charset;
    }

    @Data
    public static class Payment {
        private String baseUrl;
        private String returnUrl;
        /** 是否允许 MOCK 模拟支付（生产环境应设为 false） */
        private boolean mockEnabled = true;
    }

    @Data
    public static class Security {
        /** 逗号分隔；设为 * 则允许任意来源（不携带 Cookie 凭证） */
        private String corsAllowedOrigins = "http://localhost:5173,http://localhost:80,http://127.0.0.1:5173";
        private boolean swaggerEnabled = true;
        private int loginRateLimitPerMinute = 20;
        private int registerRateLimitPerMinute = 5;
    }

    @Data
    public static class Stock {
        /** 未使用卡密低于此值时在仪表盘告警 */
        private int lowThreshold = 10;
        /** 低库存邮件通知地址（可选，逗号分隔） */
        private String alertEmail;
    }

    @Data
    public static class Captcha {
        private boolean enabled = true;
    }

    @Data
    public static class Mail {
        private String from;
        private String appUrl;
    }

    @Data
    public static class FeatureSchemaMigration {
        private boolean enabled = true;
    }
}
