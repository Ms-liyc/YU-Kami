package com.yuchen.kami.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}
