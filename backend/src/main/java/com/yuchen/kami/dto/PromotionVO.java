package com.yuchen.kami.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionVO {

    private Long id;
    private String kind;
    private String code;
    private String name;
    private String description;
    private String type;
    private String typeLabel;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private BigDecimal maxDiscount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String scope;
    private Boolean holiday;
    private Integer priority;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer perUserLimit;
    private Integer status;
    private List<Long> productIds;
    private List<String> productNames;
    private LocalDateTime createdAt;
}
