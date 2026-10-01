package com.yuchen.kami.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopProductVO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String cardType;
    private BigDecimal value;
    private Integer durationDays;
    private Integer status;
    private BigDecimal salePrice;
    private BigDecimal discountAmount;
    private String promotionName;
    private Boolean onSale;
    private Boolean holiday;
}
