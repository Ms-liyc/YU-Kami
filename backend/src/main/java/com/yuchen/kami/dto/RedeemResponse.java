package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RedeemResponse {

    private String productName;
    private String productCode;
    private String cardType;
    private BigDecimal value;
    private Integer durationDays;
    private String message;
}
