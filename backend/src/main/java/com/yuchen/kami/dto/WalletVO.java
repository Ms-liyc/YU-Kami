package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletVO {
    private BigDecimal balance;
}
