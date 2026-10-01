package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionRequest {

    private Long id;
    private String kind = KIND_ACTIVITY;

    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "优惠类型不能为空")
    private String type;

    @NotNull(message = "优惠值不能为空")
    private BigDecimal discountValue;

    private BigDecimal minAmount;
    private BigDecimal maxDiscount;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startAt;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endAt;

    private Boolean holiday = false;
    private Integer priority = 0;
    private Integer usageLimit;
    private Integer perUserLimit = 1;
    private Integer status = 1;
    private List<Long> productIds;

    // 供校验消息引用
    public static final String KIND_ACTIVITY = "ACTIVITY";
    public static final String KIND_COUPON = "COUPON";
}
