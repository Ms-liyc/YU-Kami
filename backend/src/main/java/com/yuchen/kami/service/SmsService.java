package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.SensitiveMaskUtils;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.SmsStatusDTO;
import com.yuchen.kami.sms.SmsMessage;
import com.yuchen.kami.sms.SmsProvider;
import com.yuchen.kami.sms.SmsProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final YuKamiProperties properties;
    private final SmsProviderFactory smsProviderFactory;

    public boolean isConfigured() {
        SmsProvider provider = smsProviderFactory.resolve();
        return provider.isConfigured();
    }

    public SmsStatusDTO getStatus() {
        SmsProvider provider = smsProviderFactory.resolve();
        return SmsStatusDTO.builder()
                .configured(provider.isConfigured())
                .provider(provider.type())
                .hasStockAlertRecipient(hasStockAlertRecipient())
                .build();
    }

    public void sendTestSms(String phone) {
        validatePhone(phone);
        sendInternal("TEST", phone,
                "【YU-Kami】这是一条测试短信。若收到说明短信通道配置正确。",
                Map.of("content", "测试短信"),
                true);
        log.info("测试短信已发送至 {}", SensitiveMaskUtils.maskPhone(phone));
    }

    public void sendLowStockAlert(String phone, String productName, String productCode, long unused, int threshold) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("product", productName);
        params.put("code", productCode);
        params.put("unused", String.valueOf(unused));
        params.put("threshold", String.valueOf(threshold));
        String content = String.format("【YU-Kami】库存告警：产品「%s」(%s) 可用卡密仅剩 %d 张，低于阈值 %d，请及时补货。",
                productName, productCode, unused, threshold);
        sendInternal("LOW_STOCK", phone, content, params, false);
    }

    private void sendInternal(String event, String phone, String content,
                              Map<String, String> templateParams, boolean throwOnError) {
        validatePhone(phone);
        SmsProvider provider = smsProviderFactory.resolve();
        if (!provider.isConfigured()) {
            if (throwOnError) {
                throw new BusinessException("短信服务未配置，请在服务器环境变量中设置 SMS_PROVIDER 及相关参数");
            }
            log.warn("短信未配置，跳过发送 [{}]", event);
            return;
        }
        try {
            provider.send(SmsMessage.builder()
                    .event(event)
                    .phone(phone.trim())
                    .content(content)
                    .templateParams(templateParams)
                    .build());
            log.info("短信已发送 [{}] 至 {}", event, SensitiveMaskUtils.maskPhone(phone));
        } catch (BusinessException e) {
            if (throwOnError) {
                throw e;
            }
            log.warn("短信发送失败 [{}] 至 {}: {}", event, SensitiveMaskUtils.maskPhone(phone), e.getMessage());
        } catch (Exception e) {
            log.warn("短信发送失败 [{}] 至 {}: {}", event, SensitiveMaskUtils.maskPhone(phone), e.getClass().getSimpleName());
            if (throwOnError) {
                throw new BusinessException("短信发送失败，请检查配置");
            }
        }
    }

    private boolean hasStockAlertRecipient() {
        String phones = properties.getStock().getAlertPhone();
        return phones != null && !phones.isBlank();
    }

    private void validatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        String digits = phone.trim().replaceAll("\\s+", "");
        if (!digits.matches("^1\\d{10}$") && !digits.matches("^\\+?\\d{8,15}$")) {
            throw new BusinessException("手机号格式不正确");
        }
    }
}
