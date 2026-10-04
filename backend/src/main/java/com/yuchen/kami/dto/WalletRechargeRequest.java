package com.yuchen.kami.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRechargeRequest {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "1.00", message = "充值金额至少 ¥1")
    @DecimalMax(value = "10000.00", message = "单次充值不超过 ¥10000")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    private String openid;
    private Boolean wechatJsapi;
}
