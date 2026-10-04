package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final ShopOrderMapper shopOrderMapper;
    private final CardKeyService cardKeyService;
    private final WalletService walletService;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;
    private final WebhookDispatchService webhookDispatchService;

    @Transactional
    public OrderVO refund(Long orderId, String remark) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (ShopOrder.TYPE_RECHARGE.equals(order.getOrderType())) {
            throw new BusinessException("充值订单不支持此退款方式");
        }
        if (ShopOrder.STATUS_REFUNDED.equals(order.getStatus())) {
            throw new BusinessException("订单已退款");
        }
        if (!ShopOrder.STATUS_DELIVERED.equals(order.getStatus())
                && !ShopOrder.STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException("仅已支付或已发货订单可退款");
        }

        if (order.getCardId() != null) {
            cardKeyService.revokeForRefund(order.getCardId());
        }

        if ("BALANCE".equals(order.getPaymentMethod())) {
            walletService.refundOrder(order.getUserId(), order.getAmount(), order.getId(), order.getOrderNo(), remark);
        } else {
            walletService.refundOrder(order.getUserId(), order.getAmount(), order.getId(), order.getOrderNo(),
                    remark != null && !remark.isBlank() ? remark : "订单退款至余额");
        }

        shopOrderMapper.update(null, new LambdaUpdateWrapper<ShopOrder>()
                .eq(ShopOrder::getId, order.getId())
                .set(ShopOrder::getStatus, ShopOrder.STATUS_REFUNDED)
                .set(ShopOrder::getRemark, remark));

        redisTemplate.delete("order:card:" + order.getId());

        order.setStatus(ShopOrder.STATUS_REFUNDED);
        webhookDispatchService.dispatch("ORDER_REFUNDED", Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo(),
                "userId", order.getUserId(),
                "productId", order.getProductId() != null ? order.getProductId() : 0L,
                "productName", order.getProductName() != null ? order.getProductName() : "",
                "amount", order.getAmount(),
                "paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "",
                "remark", remark != null ? remark : ""
        ));

        ShopOrder updated = shopOrderMapper.selectById(orderId);
        return orderService.toVO(updated);
    }
}
