package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.payment.AlipayPaymentService;
import com.yuchen.kami.payment.WechatOAuthService;
import com.yuchen.kami.payment.WechatPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private ShopOrderMapper shopOrderMapper;
    @Mock private PaymentConfigMapper paymentConfigMapper;
    @Mock private CardKeyService cardKeyService;
    @Mock private OrderService orderService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private AlipayPaymentService alipayPaymentService;
    @Mock private WechatPaymentService wechatPaymentService;
    @Mock private WechatOAuthService wechatOAuthService;
    @Mock private PromotionService promotionService;
    @Mock private WalletService walletService;
    @Mock private WebhookDispatchService webhookDispatchService;
    @Mock private CryptoService cryptoService;
    @Mock private ValueOperations<String, String> valueOperations;
    @Spy private YuKamiProperties properties = new YuKamiProperties();

    @InjectMocks private PaymentService paymentService;

    @BeforeEach
    void initProperties() {
        properties.getPayment().setMockEnabled(true);
    }

    @Test
    void completePayment_shouldReject_whenCancelled() {
        ShopOrder order = new ShopOrder();
        order.setId(3L);
        order.setOrderNo("O20260101003");
        order.setStatus(ShopOrder.STATUS_CANCELLED);

        when(shopOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        boolean result = paymentService.completePayment("O20260101003", "PAY789");

        assertFalse(result);
        verify(cardKeyService, never()).generateForOrder(any(), any(), any());
    }

    @Test
    void completePayment_shouldBeIdempotent_whenAlreadyDelivered() {
        ShopOrder order = new ShopOrder();
        order.setId(2L);
        order.setOrderNo("O20260101002");
        order.setStatus(ShopOrder.STATUS_DELIVERED);

        when(shopOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        boolean result = paymentService.completePayment("O20260101002", "PAY456");

        assertTrue(result);
        verify(cardKeyService, never()).generateForOrder(any(), any(), any());
    }

    @Test
    void completePayment_shouldFail_whenOrderMissing() {
        when(shopOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        boolean result = paymentService.completePayment("O-NOT-EXIST", "PAY000");

        assertFalse(result);
    }

    @Test
    void completePayment_shouldReject_whenPaidAmountMismatch() {
        ShopOrder order = new ShopOrder();
        order.setId(4L);
        order.setOrderNo("O20260101004");
        order.setStatus(ShopOrder.STATUS_PENDING);
        order.setAmount(new BigDecimal("29.90"));

        when(shopOrderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(order);

        boolean result = paymentService.completePayment("O20260101004", "PAY111", new BigDecimal("1.00"));

        assertFalse(result);
        verify(cardKeyService, never()).generateForOrder(any(), any(), any());
    }
}
