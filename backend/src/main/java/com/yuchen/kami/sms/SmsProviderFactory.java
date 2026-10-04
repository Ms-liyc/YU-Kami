package com.yuchen.kami.sms;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmsProviderFactory {

    private final YuKamiProperties properties;
    private final DisabledSmsProvider disabledSmsProvider;
    private final HttpSmsProvider httpSmsProvider;
    private final LoggingSmsProvider loggingSmsProvider;
    private final AliyunSmsProvider aliyunSmsProvider;

    public SmsProvider resolve() {
        String type = properties.getSms().getProvider();
        if (type == null || type.isBlank()) {
            return disabledSmsProvider;
        }
        return switch (type.trim().toLowerCase()) {
            case "http" -> httpSmsProvider;
            case "log" -> loggingSmsProvider;
            case "aliyun" -> aliyunSmsProvider;
            default -> disabledSmsProvider;
        };
    }
}
