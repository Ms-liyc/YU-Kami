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
}
