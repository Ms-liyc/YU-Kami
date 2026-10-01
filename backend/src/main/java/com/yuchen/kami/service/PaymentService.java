package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ShopOrderMapper shopOrderMapper;
    private final PaymentConfigMapper paymentConfigMapper;
    private final CardKeyService cardKeyService;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    public List<PaymentConfig> availableChannels() {
        return paymentConfigMapper.selectList(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getStatus, 1));
    }

    @Transactional
    public OrderVO pay(Long userId, Long orderId, String paymentMethod) {
        ShopOrder order = orderService.getOrder(orderId, userId);
        if (!ShopOrder.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }

        PaymentConfig config = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, paymentMethod)
                .eq(PaymentConfig::getStatus, 1));
        if (config == null) {
            throw new BusinessException("不支持的支付方式");
        }

        String paymentNo = processPayment(config, order);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentNo(paymentNo);
        order.setStatus(ShopOrder.STATUS_PAID);
        order.setPaidAt(LocalDateTime.now());
        shopOrderMapper.updateById(order);

        String cardKey = cardKeyService.generateForOrder(order.getProductId(), userId, order.getOrderNo());
        order.setStatus(ShopOrder.STATUS_DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        shopOrderMapper.updateById(order);

        redisTemplate.opsForValue().set("order:card:" + order.getId(), cardKey, Duration.ofHours(24));

        OrderVO vo = orderService.toVO(order);
        vo.setCardKey(cardKey);
        return vo;
    }

    public String getDeliveredCardKey(Long orderId, Long userId) {
        ShopOrder order = orderService.getOrder(orderId, userId);
        if (!ShopOrder.STATUS_DELIVERED.equals(order.getStatus())) {
            throw new BusinessException("订单未发货");
        }
        String key = redisTemplate.opsForValue().get("order:card:" + orderId);
        if (key == null) {
            throw new BusinessException("卡密已过期，请联系客服");
        }
        return key;
    }

    private String processPayment(PaymentConfig config, ShopOrder order) {
        String paymentNo = "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        switch (config.getChannel()) {
            case "MOCK" -> log.info("模拟支付成功: 订单={}, 金额={}", order.getOrderNo(), order.getAmount());
            case "ALIPAY" -> log.info("支付宝支付(对接中): 订单={}", order.getOrderNo());
            case "WECHAT" -> log.info("微信支付(对接中): 订单={}", order.getOrderNo());
            default -> throw new BusinessException("支付渠道未实现");
        }
        return paymentNo;
    }

    public Map<String, Object> getPaymentParams(Long orderId, String channel) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", order.getOrderNo());
        params.put("amount", order.getAmount());
        params.put("channel", channel);
        if ("ALIPAY".equals(channel) || "WECHAT".equals(channel)) {
            params.put("message", "正式对接请配置 payment_config 中的 app_id 和 app_secret");
            params.put("payUrl", "/shop/orders?pay=" + orderId);
        }
        return params;
    }

    public void updatePaymentConfig(PaymentConfig config) {
        paymentConfigMapper.updateById(config);
    }

    public List<PaymentConfig> listConfigs() {
        return paymentConfigMapper.selectList(null);
    }
}
