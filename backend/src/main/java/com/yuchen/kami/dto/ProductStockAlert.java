package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStockAlert {

    private Long productId;
    private String productName;
    private String productCode;
    private long unusedCount;
    private int threshold;
}
