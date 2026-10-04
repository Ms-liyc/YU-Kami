package com.yuchen.kami.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.crypto.HmacSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WechatOAuthServiceTest {

    @Mock private PaymentChannelHelper helper;
    @Mock private StringRedisTemplate redisTemplate;

    private WechatOAuthService service;

    @BeforeEach
    void setUp() {
        YuKamiProperties properties = new YuKamiProperties();
        properties.getCrypto().setHmacSecret("test-hmac-secret");
        properties.getPayment().setBaseUrl("http://localhost:8080");
        HmacSigner signer = new HmacSigner(properties);
        service = new WechatOAuthService(helper, properties, redisTemplate, new ObjectMapper(), signer);
    }

    @Test
    void buildAndParseState_shouldRoundTrip() {
        String state = service.buildState(42L, 99L, "/shop/orders");
        WechatOAuthService.OAuthState parsed = service.parseState(state);
        assertEquals(42L, parsed.userId());
        assertEquals(99L, parsed.orderId());
        assertEquals("/shop/orders", parsed.redirectPath());
    }

    @Test
    void parseState_shouldRejectTamperedSignature() {
        String state = service.buildState(1L, 2L, "/shop/buy/1");
        String tampered = state.substring(0, state.lastIndexOf('|') + 1) + "bad-signature";
        assertThrows(Exception.class, () -> service.parseState(tampered));
    }

    @Test
    void sanitizeRedirect_shouldBlockExternalUrl() {
        String state = service.buildState(1L, 2L, "https://evil.com/phish");
        WechatOAuthService.OAuthState parsed = service.parseState(state);
        assertEquals("/shop/orders", parsed.redirectPath());
    }
}
