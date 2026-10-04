package com.yuchen.kami.service;

import com.yuchen.kami.entity.WebhookConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebhookDispatchServiceTest {

    @InjectMocks
    private WebhookDispatchService webhookDispatchService;

    @Test
    void shouldDispatch_shouldMatchSubscribedEvent() {
        WebhookConfig config = new WebhookConfig();
        config.setEvents("REDEEM_SUCCESS,ORDER_DELIVERED, LOW_STOCK");

        boolean redeem = ReflectionTestUtils.invokeMethod(webhookDispatchService, "shouldDispatch", config, "REDEEM_SUCCESS");
        boolean lowStock = ReflectionTestUtils.invokeMethod(webhookDispatchService, "shouldDispatch", config, "LOW_STOCK");
        boolean missing = ReflectionTestUtils.invokeMethod(webhookDispatchService, "shouldDispatch", config, "ORDER_REFUNDED");

        assertTrue(redeem);
        assertTrue(lowStock);
        assertFalse(missing);
    }

    @Test
    void shouldDispatch_shouldReturnFalse_whenEventsNull() {
        WebhookConfig config = new WebhookConfig();
        config.setEvents(null);
        Boolean result = ReflectionTestUtils.invokeMethod(webhookDispatchService, "shouldDispatch", config, "REDEEM_SUCCESS");
        assertFalse(result);
    }

    @Test
    void sign_shouldProduceStableHmac() throws Exception {
        String body = "{\"event\":\"REDEEM_SUCCESS\",\"data\":{}}";
        String secret = "test-secret";

        String sig1 = ReflectionTestUtils.invokeMethod(webhookDispatchService, "sign", body, secret);
        String sig2 = ReflectionTestUtils.invokeMethod(webhookDispatchService, "sign", body, secret);

        assertNotNull(sig1);
        assertEquals(64, sig1.length());
        assertEquals(sig1, sig2);
    }

    @Test
    void truncate_shouldLimitLength() {
        String result = ReflectionTestUtils.invokeMethod(webhookDispatchService, "truncate", "abcdefgh", 5);
        assertEquals("abcde", result);
    }
}
