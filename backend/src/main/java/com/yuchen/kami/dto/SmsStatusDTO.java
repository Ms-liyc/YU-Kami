package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsStatusDTO {

    private boolean configured;
    /** 当前提供者类型：none / http / aliyun / log */
    private String provider;
    private boolean hasStockAlertRecipient;
}
