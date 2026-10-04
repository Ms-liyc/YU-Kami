package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.OrderRefundRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.payment.PaymentRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final ShopOrderMapper shopOrderMapper;
    private final CardKeyService cardKeyService;
    private final WalletService walletService;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;
    private final WebhookDispatchService webhookDispatchService;
    private final PaymentConfigMapper paymentConfigMapper;
    private final PaymentRefundService paymentRefundService;

    @Transactional
    public OrderVO refund(Long orderId, OrderRefundRequest request) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (ShopOrder.STATUS_REFUNDED.equals(order.getStatus())) {
            throw new BusinessException("订单已退款");
        }
        if (!ShopOrder.STATUS_DELIVERED.equals(order.getStatus())
                && !ShopOrder.STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException("仅已支付或已发货订单可退款");
        }

        String refundMode = request != null && request.getRefundMode() != null
                ? request.getRefundMode() : "BALANCE";
        String remark = request != null ? request.getRemark() : null;

        if (ShopOrder.TYPE_RECHARGE.equals(order.getOrderType())) {
            refundRechargeOrder(order, refundMode, remark);
        } else {
            refundProductOrder(order, refundMode, remark);
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
                "refundMode", refundMode,
                "remark", remark != null ? remark : ""
        ));

        ShopOrder updated = shopOrderMapper.selectById(orderId);
        return orderService.toVO(updated);
    }

    private void refundRechargeOrder(ShopOrder order, String refundMode, String remark) {
        walletService.adjustBalance(order.getUserId(), order.getAmount().negate(),
                remark != null && !remark.isBlank() ? remark : "充值订单退款扣减");
        if ("ORIGINAL".equals(refundMode)) {
            tryOriginalRefund(order, remark);
        }
    }

    private void refundProductOrder(ShopOrder order, String refundMode, String remark) {
        if (order.getCardId() != null) {
            cardKeyService.revokeForRefund(order.getCardId());
        }
        if ("ORIGINAL".equals(refundMode)) {
            if (!tryOriginalRefund(order, remark)) {
                refundToWallet(order, remark);
            }
        } else {
            refundToWallet(order, remark);
        }
    }

    private void refundToWallet(ShopOrder order, String remark) {
        walletService.refundOrder(order.getUserId(), order.getAmount(), order.getId(), order.getOrderNo(),
                remark != null && !remark.isBlank() ? remark : "订单退款至余额");
    }

    private boolean tryOriginalRefund(ShopOrder order, String remark) {
        String method = order.getPaymentMethod();
        if (method == null || "BALANCE".equals(method) || "MOCK".equals(method)) {
            return false;
        }
        PaymentConfig config = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, method));
        if (config == null) {
            log.warn("原路退款失败，支付渠道未配置: {}", method);
            return false;
        }
        return switch (method) {
            case "ALIPAY" -> paymentRefundService.refundAlipay(config, order, remark);
            case "WECHAT" -> paymentRefundService.refundWechat(config, order, remark);
            default -> false;
        };
    }
}
