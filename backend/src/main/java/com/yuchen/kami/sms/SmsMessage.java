package com.yuchen.kami.sms;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SmsMessage {

    /** 事件类型，如 LOW_STOCK、TEST */
    private String event;

    /** 目标手机号（国内 11 位或带区号） */
    private String phone;

    /** 纯文本内容（HTTP / log 提供者使用） */
    private String content;

    /** 模板参数（阿里云等模板短信使用） */
    @Builder.Default
    private Map<String, String> templateParams = Map.of();
}
