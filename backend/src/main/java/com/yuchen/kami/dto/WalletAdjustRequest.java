package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletAdjustRequest {

    /** 正数充值，负数扣减 */
    @NotNull(message = "调整金额不能为空")
    private BigDecimal amount;

    private String remark;
}
