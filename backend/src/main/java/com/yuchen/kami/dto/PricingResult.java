package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PricingResult {

    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private Long promotionId;
    private String promotionName;
    private String couponCode;
    /** ACTIVITY / COUPON / NONE */
    private String appliedType;
    private boolean onSale;
}
