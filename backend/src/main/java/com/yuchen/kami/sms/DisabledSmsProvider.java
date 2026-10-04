package com.yuchen.kami.sms;

import org.springframework.stereotype.Component;

@Component
public class DisabledSmsProvider implements SmsProvider {

    @Override
    public String type() {
        return "none";
    }

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public void send(SmsMessage message) {
        // 未启用短信，静默跳过
    }
}
