package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecentOrderItem {
    private String user;
    private String product;
    private BigDecimal price;
    private String time;
}
