package com.yuchen.kami.sms;

import com.yuchen.kami.common.SensitiveMaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 开发/演示用：仅写日志，不实际发信。设置 {@code SMS_PROVIDER=log}。
 */
@Slf4j
@Component
public class LoggingSmsProvider implements SmsProvider {

    @Override
    public String type() {
        return "log";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public void send(SmsMessage message) {
        log.info("[SMS:log] event={} phone={} content={}",
                message.getEvent(),
                SensitiveMaskUtils.maskPhone(message.getPhone()),
                message.getContent());
    }
}
