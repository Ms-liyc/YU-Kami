package com.yuchen.kami.sms;

/**
 * 短信发送提供者 SPI。用户通过 {@code SMS_PROVIDER} 选择实现（http / aliyun / log 等）。
 */
public interface SmsProvider {

    /** 提供者类型标识，与环境变量 SMS_PROVIDER 对应 */
    String type();

    /** 当前提供者是否已完成必要配置 */
    boolean isConfigured();

    /** 发送短信，失败时抛出 {@link com.yuchen.kami.common.BusinessException} */
    void send(SmsMessage message);
}
