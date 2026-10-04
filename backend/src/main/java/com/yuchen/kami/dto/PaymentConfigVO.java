package com.yuchen.kami.dto;

import com.yuchen.kami.entity.PaymentConfig;
import lombok.Data;

@Data
public class PaymentConfigVO {

    public static final String SECRET_MASK = "******";

    private Long id;
    private String channel;
    private String appId;
    /** 脱敏后的 appSecret；未配置时为 null */
    private String appSecret;
    private boolean hasAppSecret;
    private String notifyUrl;
    private Integer status;
    private String configJson;

    public static PaymentConfigVO from(PaymentConfig config) {
        PaymentConfigVO vo = new PaymentConfigVO();
        vo.setId(config.getId());
        vo.setChannel(config.getChannel());
        vo.setAppId(config.getAppId());
        boolean hasSecret = config.getAppSecret() != null && !config.getAppSecret().isBlank();
        vo.setHasAppSecret(hasSecret);
        vo.setAppSecret(hasSecret ? SECRET_MASK : null);
        vo.setNotifyUrl(config.getNotifyUrl());
        vo.setStatus(config.getStatus());
        vo.setConfigJson(config.getConfigJson());
        return vo;
    }
}
