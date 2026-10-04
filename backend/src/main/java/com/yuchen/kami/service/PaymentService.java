package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.dto.PaymentConfigUpdateRequest;
import com.yuchen.kami.dto.PaymentConfigVO;
import com.yuchen.kami.dto.PrepayResponse;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.entity.WalletTransaction;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.dto.CardDeliveryResult;
import com.yuchen.kami.dto.JsapiPayParams;
import com.yuchen.kami.dto.PaymentChannelVO;
import com.yuchen.kami.payment.AlipayPaymentService;
import com.yuchen.kami.payment.WechatOAuthService;
import com.yuchen.kami.payment.WechatPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final AlipayPaymentService alipayPaymentService;
    private final WechatPaymentService wechatPaymentService;
    private final WechatOAuthService wechatOAuthService;
    private final PromotionService promotionService;
    private final WalletService walletService;
    private final WebhookDispatchService webhookDispatchService;
    private final YuKamiProperties properties;

    public List<PaymentConfig> availableChannels() {
        return paymentConfigMapper.selectList(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getStatus, 1))
                .stream()
                .filter(this::isChannelVisible)
                .toList();
    }

    @Transactional
    public PrepayResponse prepay(Long userId, Long orderId, String paymentMethod,
                                 String openid, Boolean wechatJsapi) {
        ShopOrder order = orderService.getOrder(orderId, userId);
        if (!ShopOrder.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        if (ShopOrder.TYPE_RECHARGE.equals(order.getOrderType()) && "BALANCE".equals(paymentMethod)) {
            throw new BusinessException("余额充值不能使用余额支付");
        }

        order.setPaymentMethod(paymentMethod);
        shopOrderMapper.updateById(order);

        return switch (paymentMethod) {
            case "MOCK" -> {
                if (!properties.getPayment().isMockEnabled()) {
                    throw new BusinessException("模拟支付未启用");
                }
                yield prepayMock(order, userId);
            }
            case "BALANCE" -> prepayBalance(order, userId);
            case "ALIPAY" -> prepayAlipay(getEnabledConfig("ALIPAY"), order);
            case "WECHAT" -> prepayWechat(getEnabledConfig("WECHAT"), order, userId, openid, wechatJsapi);
            default -> throw new BusinessException("不支持的支付方式");
        };
    }

    /** 兼容旧接口：MOCK 即时支付 */
    @Transactional
    public OrderVO pay(Long userId, Long orderId, String paymentMethod) {
        PrepayResponse prepay = prepay(userId, orderId, paymentMethod, null, null);
        if ("INSTANT".equals(prepay.getPayType())) {
            OrderVO vo = orderService.toVO(shopOrderMapper.selectById(orderId));
            vo.setCardKey(prepay.getCardKey());
            return vo;
        }
        throw new BusinessException("请使用预支付接口完成 " + paymentMethod + " 支付");
    }

    public OrderVO getOrderStatus(Long orderId, Long userId) {
        ShopOrder order = orderService.getOrder(orderId, userId);
        OrderVO vo = orderService.toVO(order);
        if (ShopOrder.STATUS_DELIVERED.equals(order.getStatus())
                && ShopOrder.TYPE_PRODUCT.equals(order.getOrderType() != null ? order.getOrderType() : ShopOrder.TYPE_PRODUCT)) {
            String key = redisTemplate.opsForValue().get("order:card:" + orderId);
            vo.setCardKey(key);
        }
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

    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params) {
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return true;
        }
        PaymentConfig config = getConfigForNotify("ALIPAY");
        if (!alipayPaymentService.verifyNotify(config, params)) {
            log.warn("支付宝回调验签失败: {}", orderNo);
            return false;
        }
        String paymentNo = params.get("trade_no");
        BigDecimal paidAmount = parseAmount(params.get("total_amount"));
        if (paidAmount == null) {
            log.warn("支付宝回调缺少 total_amount: {}", orderNo);
            return false;
        }
        return completePayment(orderNo, paymentNo, paidAmount);
    }

    @Transactional
    public void handleWechatNotify(String body, String serial, String nonce,
                                   String timestamp, String signature) {
        PaymentConfig config = getConfigForNotify("WECHAT");
        Transaction transaction = wechatPaymentService.parseNotify(config, body, serial, nonce, timestamp, signature);
        if (transaction.getTradeState() != null
                && "SUCCESS".equals(transaction.getTradeState().name())) {
            if (transaction.getAmount() == null || transaction.getAmount().getTotal() == null) {
                log.warn("微信回调缺少金额: {}", transaction.getOutTradeNo());
                return;
            }
            BigDecimal paidAmount = new BigDecimal(transaction.getAmount().getTotal()).movePointLeft(2);
            completePayment(transaction.getOutTradeNo(), transaction.getTransactionId(), paidAmount);
        }
    }

    @Transactional
    public PrepayResponse recharge(Long userId, java.math.BigDecimal amount, String paymentMethod,
                                   String openid, Boolean wechatJsapi) {
        ShopOrder order = orderService.createRechargeOrder(userId, amount);
        return prepay(userId, order.getId(), paymentMethod, openid, wechatJsapi);
    }

    @Transactional
    public boolean completePayment(String orderNo, String paymentNo) {
        return completePayment(orderNo, paymentNo, null);
    }

    @Transactional
    public boolean completePayment(String orderNo, String paymentNo, BigDecimal paidAmount) {
        ShopOrder order = shopOrderMapper.selectOne(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getOrderNo, orderNo));
        if (order == null) {
            log.warn("回调订单不存在: {}", orderNo);
            return false;
        }
        if (ShopOrder.STATUS_DELIVERED.equals(order.getStatus())) {
            return true;
        }
        if (!ShopOrder.STATUS_PENDING.equals(order.getStatus())) {
            log.warn("订单状态异常: {} status={}", orderNo, order.getStatus());
            return false;
        }
        if (paidAmount != null && !amountsMatch(order.getAmount(), paidAmount)) {
            log.warn("回调金额不匹配: order={} expected={} actual={}", orderNo, order.getAmount(), paidAmount);
            return false;
        }

        int paidRows = shopOrderMapper.update(null, new LambdaUpdateWrapper<ShopOrder>()
                .eq(ShopOrder::getId, order.getId())
                .eq(ShopOrder::getStatus, ShopOrder.STATUS_PENDING)
                .set(ShopOrder::getPaymentNo, paymentNo)
                .set(ShopOrder::getStatus, ShopOrder.STATUS_PAID)
                .set(ShopOrder::getPaidAt, LocalDateTime.now()));
        if (paidRows == 0) {
            ShopOrder latest = shopOrderMapper.selectById(order.getId());
            return latest != null && (ShopOrder.STATUS_DELIVERED.equals(latest.getStatus())
                    || ShopOrder.STATUS_REFUNDED.equals(latest.getStatus()));
        }

        if (ShopOrder.TYPE_RECHARGE.equals(order.getOrderType())) {
            walletService.credit(order.getUserId(), order.getAmount(), WalletTransaction.TYPE_RECHARGE,
                    "余额充值 " + orderNo, order.getId(), orderNo);
            shopOrderMapper.update(null, new LambdaUpdateWrapper<ShopOrder>()
                    .eq(ShopOrder::getId, order.getId())
                    .eq(ShopOrder::getStatus, ShopOrder.STATUS_PAID)
                    .set(ShopOrder::getStatus, ShopOrder.STATUS_DELIVERED)
                    .set(ShopOrder::getDeliveredAt, LocalDateTime.now()));
            log.info("余额充值完成: {}", orderNo);
            dispatchOrderWebhook(order);
            return true;
        }

        CardDeliveryResult delivery = cardKeyService.generateForOrder(order.getProductId(), order.getUserId(), order.getOrderNo());
        shopOrderMapper.update(null, new LambdaUpdateWrapper<ShopOrder>()
                .eq(ShopOrder::getId, order.getId())
                .eq(ShopOrder::getStatus, ShopOrder.STATUS_PAID)
                .set(ShopOrder::getStatus, ShopOrder.STATUS_DELIVERED)
                .set(ShopOrder::getDeliveredAt, LocalDateTime.now())
                .set(ShopOrder::getCardId, delivery.getCardId()));

        redisTemplate.opsForValue().set("order:card:" + order.getId(), delivery.getPlainKey(), Duration.ofHours(24));
        promotionService.confirmByOrder(order);
        log.info("订单发货完成: {}", orderNo);
        dispatchOrderWebhook(order);
        return true;
    }

    public void updatePaymentConfig(Long id, PaymentConfigUpdateRequest request) {
        PaymentConfig existing = paymentConfigMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("支付配置不存在");
        }
        if ("MOCK".equals(existing.getChannel()) && request.getStatus() != null
                && request.getStatus() == 1 && !properties.getPayment().isMockEnabled()) {
            throw new BusinessException("模拟支付已在服务端禁用，无法启用 MOCK 渠道");
        }
        if (request.getAppId() != null) {
            existing.setAppId(request.getAppId());
        }
        if (request.getNotifyUrl() != null) {
            existing.setNotifyUrl(request.getNotifyUrl());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getConfigJson() != null) {
            existing.setConfigJson(request.getConfigJson());
        }
        if (request.getAppSecret() != null && !request.getAppSecret().isBlank()
                && !PaymentConfigVO.SECRET_MASK.equals(request.getAppSecret())) {
            existing.setAppSecret(request.getAppSecret());
        }
        paymentConfigMapper.updateById(existing);
    }

    public List<PaymentConfigVO> listConfigViews() {
        return paymentConfigMapper.selectList(null).stream()
                .map(PaymentConfigVO::from)
                .toList();
    }

    private PrepayResponse prepayMock(ShopOrder order, Long userId) {
        String paymentNo = "MOCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        completePayment(order.getOrderNo(), paymentNo);
        if (ShopOrder.TYPE_RECHARGE.equals(order.getOrderType())) {
            return PrepayResponse.builder()
                    .payType("INSTANT")
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .message("充值成功")
                    .build();
        }
        String cardKey = redisTemplate.opsForValue().get("order:card:" + order.getId());
        return PrepayResponse.builder()
                .payType("INSTANT")
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .cardKey(cardKey)
                .message("模拟支付成功")
                .build();
    }

    @Transactional
    private PrepayResponse prepayBalance(ShopOrder order, Long userId) {
        String paymentNo = walletService.payOrder(userId, order);
        completePayment(order.getOrderNo(), paymentNo);
        String cardKey = redisTemplate.opsForValue().get("order:card:" + order.getId());
        return PrepayResponse.builder()
                .payType("INSTANT")
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .cardKey(cardKey)
                .message("余额支付成功")
                .build();
    }

    private PrepayResponse prepayAlipay(PaymentConfig config, ShopOrder order) {
        String payUrl = alipayPaymentService.createPagePay(config, order);
        return PrepayResponse.builder()
                .payType("REDIRECT")
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .payUrl(payUrl)
                .message("请跳转支付宝完成支付")
                .build();
    }

    private PrepayResponse prepayWechat(PaymentConfig config, ShopOrder order, Long userId,
                                        String openid, Boolean wechatJsapi) {
        boolean useJsapi = Boolean.TRUE.equals(wechatJsapi);
        String resolvedOpenid = openid;
        if (useJsapi && (resolvedOpenid == null || resolvedOpenid.isBlank())) {
            resolvedOpenid = wechatOAuthService.getStoredOpenId(userId);
        }
        if (useJsapi && resolvedOpenid != null && !resolvedOpenid.isBlank()) {
            JsapiPayParams params = wechatPaymentService.createJsapiPay(config, order, resolvedOpenid);
            return PrepayResponse.builder()
                    .payType("JSAPI")
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .jsapiParams(params)
                    .message("请完成微信支付")
                    .build();
        }
        String codeUrl = wechatPaymentService.createNativePay(config, order);
        return PrepayResponse.builder()
                .payType("QRCODE")
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .codeUrl(codeUrl)
                .message("请使用微信扫码支付")
                .build();
    }

    private PaymentConfig getEnabledConfig(String channel) {
        PaymentConfig config = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, channel)
                .eq(PaymentConfig::getStatus, 1));
        if (config == null) {
            throw new BusinessException("支付渠道未启用: " + channel);
        }
        return config;
    }

    public List<PaymentChannelVO> availableChannelsForUser(Long userId) {
        List<PaymentChannelVO> channels = availableChannels().stream()
                .map(PaymentChannelVO::from)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        channels.add(PaymentChannelVO.balance());
        return channels;
    }

    public List<PaymentChannelVO> availableRechargeChannels() {
        return availableChannels().stream()
                .map(PaymentChannelVO::from)
                .toList();
    }

    /** 回调验签用：不要求渠道当前处于启用状态 */
    private PaymentConfig getConfigForNotify(String channel) {
        PaymentConfig config = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, channel));
        if (config == null) {
            throw new BusinessException("支付渠道未配置: " + channel);
        }
        return config;
    }

    private boolean isChannelVisible(PaymentConfig config) {
        if ("MOCK".equals(config.getChannel()) && !properties.getPayment().isMockEnabled()) {
            return false;
        }
        return config.getStatus() != null && config.getStatus() == 1;
    }

    private boolean amountsMatch(BigDecimal expected, BigDecimal actual) {
        if (expected == null || actual == null) {
            return false;
        }
        BigDecimal exp = expected.setScale(2, RoundingMode.HALF_UP);
        BigDecimal act = actual.setScale(2, RoundingMode.HALF_UP);
        return exp.compareTo(act) == 0;
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void dispatchOrderWebhook(ShopOrder order) {
        String event = ShopOrder.TYPE_RECHARGE.equals(order.getOrderType())
                ? "RECHARGE_SUCCESS" : "ORDER_DELIVERED";
        webhookDispatchService.dispatch(event, orderWebhookPayload(order));
    }

    private Map<String, Object> orderWebhookPayload(ShopOrder order) {
        return Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo(),
                "orderType", order.getOrderType() != null ? order.getOrderType() : ShopOrder.TYPE_PRODUCT,
                "userId", order.getUserId(),
                "productId", order.getProductId() != null ? order.getProductId() : 0L,
                "productName", order.getProductName() != null ? order.getProductName() : "",
                "amount", order.getAmount(),
                "quantity", order.getQuantity() != null ? order.getQuantity() : 1,
                "paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "",
                "status", order.getStatus() != null ? order.getStatus() : ""
        );
    }
}
