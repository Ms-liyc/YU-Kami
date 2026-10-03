package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletTransactionVO {
    private Long id;
    private String type;
    private String typeLabel;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String orderNo;
    private String remark;
    private LocalDateTime createdAt;
}
