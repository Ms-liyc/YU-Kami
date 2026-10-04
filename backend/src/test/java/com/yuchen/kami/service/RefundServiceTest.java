package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.OrderRefundRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.payment.PaymentRefundService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock private ShopOrderMapper shopOrderMapper;
    @Mock private CardKeyService cardKeyService;
    @Mock private WalletService walletService;
    @Mock private OrderService orderService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private WebhookDispatchService webhookDispatchService;
    @Mock private PaymentConfigMapper paymentConfigMapper;
    @Mock private PaymentRefundService paymentRefundService;

    @InjectMocks private RefundService refundService;

    @BeforeAll
    static void initMybatisPlusEntityCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, ShopOrder.class);
    }

    @Test
    void refund_shouldReject_whenOrderNotFound() {
        when(shopOrderMapper.selectById(1L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> refundService.refund(1L, new OrderRefundRequest()));
    }

    @Test
    void refund_shouldReject_whenAlreadyRefunded() {
        ShopOrder order = deliveredOrder();
        order.setStatus(ShopOrder.STATUS_REFUNDED);
        when(shopOrderMapper.selectById(1L)).thenReturn(order);
        assertThrows(BusinessException.class, () -> refundService.refund(1L, new OrderRefundRequest()));
    }

    @Test
    void refund_shouldReject_whenPending() {
        ShopOrder order = deliveredOrder();
        order.setStatus(ShopOrder.STATUS_PENDING);
        when(shopOrderMapper.selectById(1L)).thenReturn(order);
        assertThrows(BusinessException.class, () -> refundService.refund(1L, new OrderRefundRequest()));
    }

    @Test
    void refundProductOrder_balanceMode_shouldRefundWalletAndDispatchWebhook() {
        ShopOrder order = deliveredOrder();
        order.setCardId(99L);
        order.setPaymentMethod("MOCK");
        OrderRefundRequest request = new OrderRefundRequest();
        request.setRefundMode("BALANCE");
        request.setRemark("test refund");

        when(shopOrderMapper.selectById(1L)).thenReturn(order, order);
        when(shopOrderMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderService.toVO(any())).thenReturn(new OrderVO());

        OrderVO result = refundService.refund(1L, request);

        assertNotNull(result);
        verify(cardKeyService).revokeForRefund(99L);
        verify(walletService).refundOrder(eq(10L), eq(new BigDecimal("29.90")), eq(1L), eq("O001"), eq("test refund"));
        verify(redisTemplate).delete("order:card:1");
        verify(shopOrderMapper).update(isNull(), any(LambdaUpdateWrapper.class));

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(webhookDispatchService).dispatch(eq("ORDER_REFUNDED"), payloadCaptor.capture());
        assertEquals("BALANCE", payloadCaptor.getValue().get("refundMode"));
        assertEquals("O001", payloadCaptor.getValue().get("orderNo"));
    }

    @Test
    void refundRechargeOrder_originalMode_shouldDeductBalance() {
        ShopOrder order = deliveredOrder();
        order.setOrderType(ShopOrder.TYPE_RECHARGE);
        order.setProductId(null);
        order.setProductName(null);
        order.setPaymentMethod("MOCK");
        OrderRefundRequest request = new OrderRefundRequest();
        request.setRefundMode("ORIGINAL");

        when(shopOrderMapper.selectById(2L)).thenReturn(order, order);
        when(shopOrderMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(orderService.toVO(any())).thenReturn(new OrderVO());

        refundService.refund(2L, request);

        verify(walletService).adjustBalance(eq(10L), eq(new BigDecimal("-29.90")), contains("充值"));
        verify(paymentRefundService, never()).refundAlipay(any(), any(), any());
    }

    private ShopOrder deliveredOrder() {
        ShopOrder order = new ShopOrder();
        order.setId(1L);
        order.setOrderNo("O001");
        order.setUserId(10L);
        order.setOrderType(ShopOrder.TYPE_PRODUCT);
        order.setProductId(5L);
        order.setProductName("VIP");
        order.setAmount(new BigDecimal("29.90"));
        order.setStatus(ShopOrder.STATUS_DELIVERED);
        order.setPaymentMethod("MOCK");
        return order;
    }
}
