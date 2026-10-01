package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PricingPreviewRequest {

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    private Integer quantity = 1;
    private String couponCode;
}
