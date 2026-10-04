package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.SetupStatusDTO;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.payment.PaymentChannelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final PaymentConfigMapper paymentConfigMapper;
    private final PaymentChannelHelper paymentChannelHelper;
    private final YuKamiProperties properties;
    private final EmailService emailService;

    public SetupStatusDTO getStatus() {
        PaymentConfig alipay = findChannel("ALIPAY");
        PaymentConfig wechat = findChannel("WECHAT");
        PaymentConfig mock = findChannel("MOCK");

        boolean alipayReady = isAlipayReady(alipay);
        boolean wechatReady = isWechatReady(wechat);
        boolean mockEnabled = mock != null && mock.getStatus() != null && mock.getStatus() == 1;

        return SetupStatusDTO.builder()
                .mockOnly(mockEnabled && !alipayReady && !wechatReady)
                .alipayReady(alipayReady)
                .wechatReady(wechatReady)
                .needsPaymentSetup(!alipayReady && !wechatReady)
                .paymentBaseUrl(properties.getPayment().getBaseUrl())
                .paymentReturnUrl(properties.getPayment().getReturnUrl())
                .paymentGuidePath("docs/PAYMENT.md")
                .mailConfigured(emailService.isConfigured())
                .hasStockAlertRecipient(hasStockAlertRecipient())
                .build();
    }

    private PaymentConfig findChannel(String channel) {
        return paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, channel));
    }

    private boolean isAlipayReady(PaymentConfig config) {
        if (config == null || config.getStatus() == null || config.getStatus() != 1) {
            return false;
        }
        return hasText(config.getAppId())
                && hasText(paymentChannelHelper.getJsonField(config, "privateKey"))
                && hasText(paymentChannelHelper.getJsonField(config, "alipayPublicKey"));
    }

    private boolean isWechatReady(PaymentConfig config) {
        if (config == null || config.getStatus() == null || config.getStatus() != 1) {
            return false;
        }
        return hasText(config.getAppId())
                && hasText(paymentChannelHelper.getJsonField(config, "mchId"))
                && hasText(paymentChannelHelper.getJsonField(config, "privateKey"))
                && hasText(paymentChannelHelper.getJsonField(config, "merchantSerialNumber"))
                && hasText(paymentChannelHelper.getJsonField(config, "apiV3Key"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasStockAlertRecipient() {
        String recipients = properties.getStock().getAlertEmail();
        return recipients != null && !recipients.isBlank();
    }
}
