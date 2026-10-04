package com.yuchen.kami.dto;

import lombok.Data;

@Data
public class PaymentConfigUpdateRequest {
    private String appId;
    private String appSecret;
    private String notifyUrl;
    private Integer status;
    private String configJson;
}
